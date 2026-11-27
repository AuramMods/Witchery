package art.arcane.witchery.block.entity;

import art.arcane.witchery.block.HuntingTrapBlock;
import art.arcane.witchery.entity.Wolfman;
import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.registry.WitcheryBlockEntities;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryEffects;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcherySounds;
import art.arcane.witchery.ritual.ItemRiteRecipe;
import art.arcane.witchery.world.migration.LegacyGameProfiles;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class HuntingTrapBlockEntity extends BlockEntity {
    private static final int ARMING_TICKS = 20;
    private static final int BAIT_POLL_TICKS = 200;
    private static final double BAIT_RADIUS = 8;
    private final boolean silvered;
    private boolean sprung = true;
    private int baitPollTicks;
    private long armedAt;
    private long baitSince;
    @Nullable private GameProfile owner;
    @Nullable private UUID summonedWolf;

    public HuntingTrapBlockEntity(BlockPos pos, BlockState state) {
        super(WitcheryBlockEntities.HUNTING_TRAP.get(), pos, state);
        silvered = state.getBlock() instanceof HuntingTrapBlock trap && trap.silvered();
    }

    public boolean sprung() { return sprung; }

    public boolean visibleTo(@Nullable Player viewer) {
        if (silvered || sprung || owner == null) return true;
        if (viewer == null) return false;
        return owner.getId() != null ? owner.getId().equals(viewer.getUUID())
                : owner.getName().equalsIgnoreCase(viewer.getGameProfile().getName());
    }

    public void setOwner(Player player) {
        owner = player.getGameProfile();
        updateView();
    }

    public void toggle(Player player) {
        if (!(level instanceof ServerLevel server)) return;
        if (player instanceof ServerPlayer serverPlayer) {
            WitcherySounds.playLegacyPlayerCue(serverPlayer, WitcherySounds.BOW_CLICK.get(), SoundSource.BLOCKS, 0.5F);
        }
        sprung = !sprung;
        if (!sprung) armedAt = server.getGameTime();
        updateView();
    }

    public void tryCatch(LivingEntity occupant) {
        if (!(level instanceof ServerLevel server) || sprung || !occupant.isAlive()
                || server.getGameTime() <= armedAt + ARMING_TICKS) return;
        if (silvered) {
            if (!(occupant instanceof Wolfman wolf) || !occupant.getUUID().equals(summonedWolf)) return;
            WitcherySounds.playLegacyBlockCue(server, worldPosition, WitcherySounds.WOLFMAN_LORD.get(), SoundSource.BLOCKS, 1);
            wolf.makeInfectious();
        }
        sprung = true;
        if (!(occupant instanceof Player player && player.isCreative())) {
            occupant.addEffect(new MobEffectInstance(WitcheryEffects.PARALYSIS.get(), 600, 2, true, true));
        }
        occupant.hurt(occupant.damageSources().anvil(null), 4);
        WitcheryNetwork.sendHuntingTrapCatch(server, worldPosition);
        updateView();
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, HuntingTrapBlockEntity trap) {
        if (!(level instanceof ServerLevel server) || ++trap.baitPollTicks < BAIT_POLL_TICKS) return;
        trap.baitPollTicks = 0;
        if (!trap.sprung && trap.summonedWolf == null) trap.checkBait(server);
    }

    private void checkBait(ServerLevel server) {
        if (server.getDifficulty() == Difficulty.PEACEFUL || !ItemRiteRecipe.legacyFullMoon(server) || !hasBait(server)) {
            if (baitSince != 0) {
                baitSince = 0;
                setChanged();
            }
            return;
        }
        if (baitSince == 0) {
            baitSince = server.getGameTime();
            setChanged();
        } else if (server.getGameTime() > baitSince) {
            summonWolf(server);
        }
    }

    private boolean hasBait(ServerLevel server) {
        var altar = WitcheryBlocks.getRequired("wolfaltar").get();
        boolean adjacent = false;
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (server.getBlockState(worldPosition.relative(direction)).is(altar)) {
                adjacent = true;
                break;
            }
        }
        if (!adjacent) return false;
        return !server.getEntitiesOfClass(Sheep.class, new AABB(worldPosition).inflate(BAIT_RADIUS),
                sheep -> sheep.isAlive() && sheep.isLeashed() && sheep.distanceToSqr(worldPosition.getCenter()) <= BAIT_RADIUS * BAIT_RADIUS).isEmpty();
    }

    private void summonWolf(ServerLevel server) {
        var floor = worldPosition.offset(lureOffset(server.random), 0, lureOffset(server.random)).mutable();
        if (!server.getWorldBorder().isWithinBounds(floor)) return;
        int top = Math.min(server.getMaxBuildHeight() - 3, floor.getY() + 8);
        while (floor.getY() < top && !server.isEmptyBlock(floor)) floor.move(Direction.UP);
        while (floor.getY() > server.getMinBuildHeight() && server.isEmptyBlock(floor)) floor.move(Direction.DOWN);
        if (server.isEmptyBlock(floor) || !server.isEmptyBlock(floor.above()) || !server.isEmptyBlock(floor.above(2))) return;
        var wolf = WitcheryEntities.WOLFMAN.get().create(server);
        if (wolf == null) return;
        wolf.moveTo(floor.getX() + 0.5, floor.getY() + 1.05, floor.getZ() + 0.5, 0, 0);
        if (!server.noCollision(wolf) || server.containsAnyLiquid(wolf.getBoundingBox())) return;
        wolf.setPersistenceRequired();
        if (!server.addFreshEntity(wolf)) return;
        summonedWolf = wolf.getUUID();
        updateView();
        WitcheryNetwork.sendWolfTrapLure(server, wolf);
    }

    private static int lureOffset(RandomSource random) {
        int offset = random.nextInt(33) - 32;
        return offset > -16 ? offset + 32 : offset;
    }

    private void updateView() {
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("Sprung", sprung);
        tag.putLong("ArmedAt", armedAt);
        tag.putLong("WolftrapStart", baitSince);
        if (owner != null) tag.put("Owner", NbtUtils.writeGameProfile(new CompoundTag(), owner));
        if (summonedWolf != null) {
            tag.putLong("WolfMost", summonedWolf.getMostSignificantBits());
            tag.putLong("WolfLeast", summonedWolf.getLeastSignificantBits());
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        sprung = !tag.contains("Sprung", Tag.TAG_BYTE) || tag.getBoolean("Sprung");
        armedAt = tag.getLong("ArmedAt");
        baitSince = tag.getLong("WolftrapStart");
        owner = tag.contains("Owner", Tag.TAG_COMPOUND) ? LegacyGameProfiles.read(tag.getCompound("Owner")) : null;
        summonedWolf = tag.contains("WolfMost", Tag.TAG_LONG) && tag.contains("WolfLeast", Tag.TAG_LONG)
                ? new UUID(tag.getLong("WolfMost"), tag.getLong("WolfLeast")) : null;
    }

    @Override
    public CompoundTag getUpdateTag() { return saveWithoutMetadata(); }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }
}
