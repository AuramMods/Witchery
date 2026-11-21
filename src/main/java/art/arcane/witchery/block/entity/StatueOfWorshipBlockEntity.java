package art.arcane.witchery.block.entity;

import art.arcane.witchery.entity.Hobgoblin;
import art.arcane.witchery.infusion.InfusionEnergy;
import art.arcane.witchery.registry.WitcheryBlockEntities;
import art.arcane.witchery.registry.WitcheryEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class StatueOfWorshipBlockEntity extends BlockEntity {
    private static final String BOUND_PLAYER = "WITCBoundPlayer";
    private static final String OWNER_UUID = "OwnerUUID";
    private static final int PULSE_TICKS = 100;
    private static final int RECHARGE_RADIUS = 64;
    private String owner = "";
    @Nullable
    private UUID ownerId;

    public StatueOfWorshipBlockEntity(BlockPos pos, BlockState state) {
        super(WitcheryBlockEntities.WORSHIP_STATUE.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, StatueOfWorshipBlockEntity statue) {
        if (statue.owner.isEmpty() || !(level instanceof ServerLevel server) || level.getGameTime() % PULSE_TICKS != 0) return;
        int worshippers = statue.updateWorshippers(server);
        var players = server.getServer().getPlayerList();
        ServerPlayer player = statue.ownerId != null ? players.getPlayer(statue.ownerId) : players.getPlayerByName(statue.owner);
        if (player != null) statue.bless(server, player, worshippers);
    }

    private int updateWorshippers(ServerLevel level) {
        int count = 0;
        for (Hobgoblin goblin : level.getEntitiesOfClass(Hobgoblin.class, new AABB(worldPosition).inflate(8), Hobgoblin::isAlive)) {
            if (goblin.isWorshipping()) count++;
            else goblin.offerWorship(worldPosition);
        }
        return count;
    }

    private void bless(ServerLevel level, ServerPlayer player, int worshippers) {
        Vec3 center = Vec3.atCenterOf(worldPosition);
        if (worshippers >= 5 && player.level() == level && player.distanceToSqr(center) <= RECHARGE_RADIUS * RECHARGE_RADIUS
                && InfusionEnergy.recharge(player, 30)) {
            level.sendParticles(ParticleTypes.INSTANT_EFFECT, player.getX(), player.getY(0.5), player.getZ(),
                    8, 0.5, 0.75, 0.5, 0);
            level.playSound(null, player, SoundEvents.NOTE_BLOCK_PLING.value(), SoundSource.PLAYERS, 1, 2);
        }
        if (worshippers >= 10) {
            player.addEffect(new MobEffectInstance(WitcheryEffects.WORSHIP.get(), 1200, worshippers >= 15 ? 1 : 0, true, true));
        }
    }

    public static void bindItem(ItemStack item, ServerPlayer player) {
        CompoundTag tag = item.getOrCreateTag();
        tag.putString(BOUND_PLAYER, player.getGameProfile().getName());
        tag.putUUID(OWNER_UUID, player.getUUID());
    }

    public static String itemOwner(ItemStack stack) {
        return stack.hasTag() ? stack.getTag().getString(BOUND_PLAYER) : "";
    }

    public String owner() {
        return owner;
    }

    public void loadItem(ItemStack stack) {
        owner = itemOwner(stack);
        CompoundTag tag = stack.getTag();
        ownerId = tag != null && tag.hasUUID(OWNER_UUID) ? tag.getUUID(OWNER_UUID) : null;
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }

    public void saveItem(ItemStack item) {
        if (owner.isEmpty()) return;
        CompoundTag tag = item.getOrCreateTag();
        tag.putString(BOUND_PLAYER, owner);
        if (ownerId != null) tag.putUUID(OWNER_UUID, ownerId);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        owner = tag.getString("Owner");
        ownerId = tag.hasUUID(OWNER_UUID) ? tag.getUUID(OWNER_UUID) : null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString("Owner", owner);
        if (ownerId != null) tag.putUUID(OWNER_UUID, ownerId);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
