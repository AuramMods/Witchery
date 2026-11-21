package art.arcane.witchery.block.entity;

import art.arcane.witchery.brewing.BrewMixture;
import art.arcane.witchery.registry.WitcheryEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class BrewFluidBlockEntity extends BlockEntity {
    private static final int DEFAULT_COLOR = 0x33AA33;
    // Five independent one-in-ten collision rolls.
    private static final double STATIONARY_CONTACT_CHANCE = 0.40951;
    private final Set<Integer> recentContacts = new HashSet<>();
    @Nullable
    private CompoundTag brew;
    @Nullable
    private BrewMixture mixture;
    @Nullable
    private UUID thrower;
    private String throwerName = "";
    private int color = DEFAULT_COLOR;
    private int duration;
    private int expansion = 4;
    private int updates;

    public BrewFluidBlockEntity(BlockEntityType<BrewFluidBlockEntity> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void initialize(CompoundTag tag, BrewMixture mixture, int color, @Nullable Entity caster) {
        brew = tag.copy();
        this.mixture = mixture;
        this.color = color;
        duration = 5 + 5 * mixture.lifetime() * mixture.lifetime();
        expansion = 4 + mixture.extent();
        thrower = caster != null ? caster.getUUID() : null;
        throwerName = caster != null ? caster.getScoreboardName() : "";
        sync();
    }

    public void spreadFrom(BrewFluidBlockEntity source) {
        brew = source.brew != null ? source.brew.copy() : null;
        mixture = source.mixture;
        color = source.color;
        duration = source.duration;
        expansion = source.expansion;
        thrower = source.thrower;
        throwerName = source.throwerName;
        sync();
    }

    public boolean hasContents() { return brew != null; }
    public int color() { return color; }
    public int duration() { return duration; }
    public int expansion() { return expansion; }

    public int tickAge() {
        if (level != null) level.blockEntityChanged(worldPosition);
        return ++updates;
    }

    public void contact(ServerLevel server, LivingEntity target, boolean gas) {
        if (mixture == null) return;
        recentContacts.add(target.getId());
        if (server.random.nextInt(10) == 4) expose(server, target, gas);
    }

    public void sampleStationaryContacts(ServerLevel server, boolean gas) {
        if (mixture == null) return;
        for (LivingEntity target : server.getEntitiesOfClass(LivingEntity.class, new AABB(worldPosition))) {
            if (!recentContacts.contains(target.getId()) && server.random.nextDouble() < STATIONARY_CONTACT_CHANCE) {
                expose(server, target, gas);
            }
        }
        recentContacts.clear();
    }

    public void expose(ServerLevel server, LivingEntity target, boolean gas) {
        if (mixture == null) return;
        LivingEntity caster = resolveCaster(server);
        boolean mask = gas && target.hasEffect(WitcheryEffects.GAS_MASK.get());
        mixture.applyAt(target, caster, caster, 0.25, 0.5, mask, Vec3.atCenterOf(worldPosition));
    }

    public void sampleTerrainContacts(ServerLevel server) {
        if (mixture == null || !mixture.hasTerrainEffects()) return;
        Vec3 impact = Vec3.atBottomCenterOf(worldPosition);
        for (Direction direction : Direction.values()) {
            if (server.random.nextDouble() >= 0.01) continue;
            BlockPos target = worldPosition.relative(direction);
            if (!server.isInWorldBounds(target) || !server.hasChunkAt(target)) continue;
            BlockState state = server.getBlockState(target);
            if (state.is(getBlockState().getBlock()) || state.isAir() && !server.getBlockState(target.below()).blocksMotion()) continue;
            LivingEntity caster = resolveCaster(server);
            mixture.applyLiquidToBlock(server, target, direction.getOpposite(), impact, caster instanceof Player player ? player : null);
        }
    }

    @Nullable
    private LivingEntity resolveCaster(ServerLevel server) {
        Entity owner = thrower != null ? server.getEntity(thrower) : null;
        if (owner == null && thrower != null) owner = server.getServer().getPlayerList().getPlayer(thrower);
        if (owner == null && !throwerName.isEmpty()) owner = server.getServer().getPlayerList().getPlayerByName(throwerName);
        return owner instanceof LivingEntity living ? living : null;
    }

    private void sync() {
        setChanged();
        if (level instanceof ServerLevel server) {
            BlockState state = getBlockState();
            server.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (brew != null) tag.put("Effect", brew.copy());
        if (thrower != null) tag.putUUID("ThrowerUUID", thrower);
        if (!throwerName.isEmpty()) tag.putString("Thrower", throwerName);
        tag.putInt("Color", color);
        tag.putInt("Duration", duration);
        tag.putInt("Expansion", expansion);
        tag.putInt("UpdateCount", updates);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        brew = tag.contains("Effect", Tag.TAG_COMPOUND) ? tag.getCompound("Effect").copy() : null;
        mixture = BrewMixture.from(brew);
        thrower = tag.hasUUID("ThrowerUUID") ? tag.getUUID("ThrowerUUID") : null;
        throwerName = tag.getString("Thrower");
        color = readColor(tag);
        duration = Mth.clamp(tag.getInt("Duration"), 0, 100);
        expansion = tag.contains("Expansion") ? Mth.clamp(tag.getInt("Expansion"), 1, 10) : 4;
        updates = Math.max(0, tag.getInt("UpdateCount"));
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("Color", color);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        color = readColor(tag);
        if (level != null && level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) {
        if (packet.getTag() != null) handleUpdateTag(packet.getTag());
    }

    private static int readColor(CompoundTag tag) {
        return tag.contains("Color", Tag.TAG_ANY_NUMERIC) ? tag.getInt("Color") & 0xFFFFFF : DEFAULT_COLOR;
    }
}
