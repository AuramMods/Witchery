package art.arcane.witchery.world;

import art.arcane.witchery.entity.LordOfTorment;
import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
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

    public static int randomFloor(RandomSource random) {
        return random.nextInt(TormentMaze.LEVELS);
    }

    public static void requestBossEncounter(ServerPlayer player, int floor) {
        if (!player.isAlive() || player.isSpectator()) return;
        CompoundTag data = data(player);
        data.putInt(ACTION, ENTER_PRESET);
        data.putInt(FLOOR, floor);
    }

    public static boolean requestSpellEncounter(ServerPlayer player) {
        if (!player.isAlive() || player.isSpectator() || player.level().dimension().equals(WitcheryDimensions.TORMENT)) return false;
        data(player).putInt(ACTION, ENTER);
        return true;
    }

    public static void touchPortal(ServerPlayer player) {
        CompoundTag data = data(player);
        boolean leave = player.level().dimension().equals(WitcheryDimensions.TORMENT)
                && player.level().getRandom().nextDouble() >= 0.05;
        data.putInt(ACTION, leave ? LEAVE : ENTER);
        if (leave) data.remove(FLOOR);
    }

    public static boolean process(ServerPlayer player) {
        ServerLevel origin = player.serverLevel();
        if (origin.getGameTime() % 20 != 0) return false;
        CompoundTag data = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        int action = data.getInt(ACTION);
        if (player.isSpectator() || !player.isAlive() || action == 0) return false;
        data.remove(ACTION);

        ServerLevel destination;
        Vec3 arrival;
        int floor = -1;
        if (action == ENTER || action == ENTER_PRESET) {
            destination = player.server.getLevel(WitcheryDimensions.TORMENT);
            if (destination == null) return false;
            floor = action == ENTER ? randomFloor(origin.getRandom())
                    : Mth.clamp(data.getInt(FLOOR), 0, TormentMaze.LEVELS - 1);
            int mazeOffset = MazeLocation.resolve(destination).offset;
            BlockPos entry = new BlockPos(mazeOffset + TormentMaze.CENTER_X, TormentMaze.floorY(floor) + 1, TormentMaze.ENTRANCE_Z);
            arrival = standNear(destination, entry);
        } else if (action == LEAVE) {
            destination = player.server.overworld();
            arrival = overworldArrival(player, destination);
        } else {
            return false;
        }
        if (arrival == null || !destination.getWorldBorder().isWithinBounds(BlockPos.containing(arrival))) return false;
        WitcheryNetwork.sendTormentPortalCue(origin, player.position());
        player.teleportTo(destination, arrival.x, arrival.y, arrival.z, player.getYRot(), player.getXRot());
        if (player.serverLevel() != destination) return false;
        player.connection.resetPosition();
        player.resetFallDistance();
        if (action == LEAVE) data.remove(FLOOR);
        if (action == ENTER_PRESET) MazeLocation.resolve(destination).queueBoss(floor);
        WitcheryNetwork.sendTormentPortalCue(destination, player.position());
        return true;
    }

    public static void tickEncounters(ServerLevel level) {
        if (!level.dimension().equals(WitcheryDimensions.TORMENT) || level.players().isEmpty() || level.getGameTime() % 10 != 0) return;
        MazeLocation location = level.getDataStorage().get(MazeLocation::load, MazeLocation.ID);
        if (location == null || location.pendingBossFloors == 0) return;
        for (int floor = 0; floor < TormentMaze.LEVELS; floor++) {
            if ((location.pendingBossFloors & 1 << floor) != 0 && spawnEncounterBoss(level, location.offset, floor)) {
                location.pendingBossFloors &= ~(1 << floor);
                location.setDirty();
            }
        }
    }

    private static boolean spawnEncounterBoss(ServerLevel level, int offset, int floor) {
        BlockPos chamber = new BlockPos(offset + 9, TormentMaze.floorY(floor) + 1, 36);
        if (!level.areEntitiesLoaded(ChunkPos.asLong(chamber))) return false;
        int bottom = chamber.getY() - 1;
        int top = chamber.getY() + 5;
        for (var entity : level.getAllEntities()) {
            if (entity instanceof LordOfTorment && !entity.isRemoved()
                    && entity.getY() >= bottom && entity.getY() <= top) return true;
        }
        LordOfTorment lord = WitcheryEntities.LORD_OF_TORMENT.get().create(level);
        if (lord == null) return false;
        lord.moveTo(chamber.getX() + 0.5, chamber.getY(), chamber.getZ() + 0.5, 0, 0);
        lord.setHealth(lord.getMaxHealth() * 0.5F);
        return level.addFreshEntity(lord);
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

    public static void transitionEffect(ServerLevel level, Vec3 pos) {
        level.sendParticles(ParticleTypes.PORTAL, pos.x, pos.y + 0.8, pos.z, 16, 0.6, 0.9, 0.6, 0.03);
        level.playSound(null, pos.x, pos.y, pos.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1, 1);
    }

    private static final class MazeLocation extends SavedData {
        private static final String ID = "witchery_torment_maze";
        private int offset = -1;
        private int pendingBossFloors;

        private static MazeLocation resolve(ServerLevel level) {
            MazeLocation location = level.getDataStorage().computeIfAbsent(MazeLocation::load, MazeLocation::new, ID);
            if (location.offset < 0) {
                location.offset = hasOriginalMaze(level) ? 0 : TormentMaze.UPGRADE_MAZE_OFFSET;
                location.setDirty();
            }
            return location;
        }

        private void queueBoss(int floor) {
            pendingBossFloors |= 1 << floor;
            setDirty();
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
            location.pendingBossFloors = tag.getInt("PendingBossFloors");
            return location;
        }

        @Override
        public CompoundTag save(CompoundTag tag) {
            tag.putInt("Offset", offset);
            tag.putInt("PendingBossFloors", pendingBossFloors);
            return tag;
        }
    }
}
