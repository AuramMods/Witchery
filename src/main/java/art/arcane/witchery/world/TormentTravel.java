package art.arcane.witchery.world;

import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class TormentTravel {
    private static final String ACTION = "WITCForceTorment";
    private static final String FLOOR = "WITCForceTormentLevel";
    private static final int ENTER = 1;
    private static final int ENTER_PRESET = 2;
    private static final int LEAVE = 3;

    private TormentTravel() {}

    public static void touchPortal(ServerPlayer player) {
        CompoundTag data = data(player);
        if (data.getInt(ACTION) == 0) {
            boolean leave = player.level().dimension().equals(WitcheryDimensions.TORMENT)
                    && player.getRandom().nextDouble() >= 0.05;
            data.putInt(ACTION, leave ? LEAVE : ENTER);
        }
    }

    public static boolean process(ServerPlayer player) {
        CompoundTag data = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        int action = data.getInt(ACTION);
        if (player.isSpectator() || !player.isAlive() || action == 0) return false;
        data.remove(ACTION);

        ServerLevel destination;
        Vec3 arrival;
        if (action == ENTER || action == ENTER_PRESET) {
            destination = player.server.getLevel(WitcheryDimensions.TORMENT);
            if (destination == null) return false;
            int slot = action == ENTER ? player.getRandom().nextInt(TormentMaze.LEVELS)
                    : Mth.clamp(data.getInt(FLOOR), 0, TormentMaze.LEVELS - 1);
            int mazeOffset = MazeLocation.resolve(destination);
            BlockPos entry = new BlockPos(mazeOffset + TormentMaze.CENTER_X, TormentMaze.floorY(slot) + 1, TormentMaze.ENTRANCE_Z);
            arrival = standNear(destination, entry);
        } else if (action == LEAVE) {
            destination = player.server.overworld();
            arrival = overworldArrival(player, destination);
        } else {
            return false;
        }
        if (arrival == null || !destination.getWorldBorder().isWithinBounds(BlockPos.containing(arrival))) return false;
        ServerLevel origin = player.serverLevel();
        Vec3 start = player.position();
        player.teleportTo(destination, arrival.x, arrival.y, arrival.z, player.getYRot(), player.getXRot());
        if (player.serverLevel() != destination) return false;
        player.connection.resetPosition();
        player.resetFallDistance();
        if (action == LEAVE) data.remove(FLOOR);
        transitionEffect(origin, start);
        transitionEffect(destination, player.position());
        return true;
    }

    private static CompoundTag data(ServerPlayer player) {
        CompoundTag root = player.getPersistentData();
        CompoundTag data = root.getCompound(Player.PERSISTED_NBT_TAG);
        root.put(Player.PERSISTED_NBT_TAG, data);
        return data;
    }

    @Nullable
    private static Vec3 overworldArrival(ServerPlayer player, ServerLevel overworld) {
        BlockPos saved = player.getRespawnPosition();
        if (saved != null && player.getRespawnDimension().equals(Level.OVERWORLD)) {
            Vec3 bed = Player.findRespawnPositionAndUseSpawnBlock(overworld, saved,
                    player.getRespawnAngle(), player.isRespawnForced(), true).orElse(null);
            if (bed != null) return bed;
        }
        return standNear(overworld, overworld.getSharedSpawnPos());
    }

    @Nullable
    private static Vec3 standNear(ServerLevel level, BlockPos origin) {
        for (BlockPos pos : BlockPos.withinManhattan(origin, 3, 4, 3)) {
            BlockPos below = pos.below();
            if (level.isEmptyBlock(pos) && level.isEmptyBlock(pos.above())
                    && level.getBlockState(below).isFaceSturdy(level, below, Direction.UP)) {
                return Vec3.atBottomCenterOf(pos);
            }
        }
        return null;
    }

    private static void transitionEffect(ServerLevel level, Vec3 pos) {
        level.sendParticles(ParticleTypes.PORTAL, pos.x, pos.y + 0.8, pos.z, 16, 0.6, 0.9, 0.6, 0.03);
        level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1, 1);
    }

    private static final class MazeLocation extends SavedData {
        private int offset = -1;

        private static int resolve(ServerLevel level) {
            MazeLocation location = level.getDataStorage().computeIfAbsent(MazeLocation::load, MazeLocation::new, "witchery_torment_maze");
            if (location.offset < 0) {
                location.offset = hasOriginalMaze(level) ? 0 : TormentMaze.UPGRADE_MAZE_OFFSET;
                location.setDirty();
            }
            return location.offset;
        }

        private static boolean hasOriginalMaze(ServerLevel level) {
            Block force = WitcheryBlocks.getRequired("force").get();
            int ceiling = TormentMaze.FIRST_FLOOR + 5;
            // Each original maze chunk has an unbroken ceiling. The old Nether terrain has none.
            for (int x : new int[]{-23, -8, 8, 24, 39}) {
                for (int z : new int[]{8, 24, 40, 56, 68}) {
                    if (!level.getBlockState(new BlockPos(x, ceiling, z)).is(force)) return false;
                }
            }
            return true;
        }

        private static MazeLocation load(CompoundTag tag) {
            MazeLocation location = new MazeLocation();
            location.offset = tag.contains("Offset") ? tag.getInt("Offset") : -1;
            return location;
        }

        @Override
        public CompoundTag save(CompoundTag tag) {
            tag.putInt("Offset", offset);
            return tag;
        }
    }
}
