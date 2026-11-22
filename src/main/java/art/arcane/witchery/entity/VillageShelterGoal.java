package art.arcane.witchery.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathComputationType;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;

final class VillageShelterGoal extends Goal {
    private static final int SEARCH_RADIUS = 32;
    private final PathfinderMob mob;
    private final boolean onlyUnleashed;
    @Nullable private Path route;
    @Nullable private BlockPos lastShelter;

    VillageShelterGoal(PathfinderMob mob, boolean onlyUnleashed) {
        this.mob = mob;
        this.onlyUnleashed = onlyUnleashed;
        setFlags(EnumSet.of(Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (!(mob.level() instanceof ServerLevel level) || onlyUnleashed && mob.isLeashed()
                || !level.dimensionType().hasSkyLight()) return false;
        BlockPos origin = mob.blockPosition();
        if (level.isDay() && !level.isRaining()
                && level.getBiome(origin).value().getPrecipitationAt(origin) == Biome.Precipitation.RAIN) return false;
        if (mob.getRandom().nextInt(50) != 0 || !level.isCloseToVillage(origin, 2)
                || lastShelter != null && lastShelter.closerToCenterThan(mob.position(), 2)) return false;

        var doors = new ArrayList<ShelterDoor>();
        ChunkPos.rangeClosed(new ChunkPos(origin), SEARCH_RADIUS / 16).forEach(chunkPos -> {
            var chunk = level.getChunkSource().getChunkNow(chunkPos.x, chunkPos.z);
            if (chunk == null) return;
            chunk.findBlocks(VillageShelterGoal::isWoodenThreshold, (pos, state) -> {
                if (pos.distSqr(origin) > SEARCH_RADIUS * SEARCH_RADIUS || Math.abs(pos.getY() - origin.getY()) > 8) return;
                Direction inside = coveredSide(level, pos, state.getValue(DoorBlock.FACING));
                if (inside != null) doors.add(new ShelterDoor(pos.immutable(), inside));
            });
        });
        doors.sort(Comparator.comparingDouble(door -> door.threshold().distSqr(origin)));
        for (ShelterDoor door : doors) {
            for (int distance = 2; distance > 0; distance--) {
                BlockPos inside = door.threshold().relative(door.inside(), distance);
                if (!canShelterAt(level, inside)) continue;
                Path path = mob.getNavigation().createPath(inside, 0, SEARCH_RADIUS);
                if (path != null && path.canReach() && path.getNodeCount() > 1) {
                    route = path;
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void start() {
        mob.getNavigation().moveTo(route, 1);
    }

    @Override
    public boolean canContinueToUse() {
        return (!onlyUnleashed || !mob.isLeashed()) && !mob.getNavigation().isDone();
    }

    @Override
    public void stop() {
        if (route != null && route.getTarget().closerToCenterThan(mob.position(), 2)) lastShelter = route.getTarget();
        mob.getNavigation().stop();
        route = null;
    }

    static boolean isWoodenThreshold(BlockState state) {
        return state.is(BlockTags.WOODEN_DOORS) && state.getBlock() instanceof DoorBlock
                && state.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER;
    }

    @Nullable
    static Direction coveredSide(ServerLevel level, BlockPos door, Direction facing) {
        Direction opposite = facing.getOpposite();
        int front = skyExposure(level, door, facing);
        int back = skyExposure(level, door, opposite);
        if (front < 0 || back < 0 || front == back) return null;
        return front < back ? facing : opposite;
    }

    private static int skyExposure(ServerLevel level, BlockPos door, Direction direction) {
        int exposed = 0;
        for (int distance = 1; distance <= 5; distance++) {
            BlockPos sample = door.relative(direction, distance);
            if (!level.hasChunkAt(sample)) return -1;
            if (openAbove(level, sample)) exposed++;
        }
        return exposed;
    }

    private static boolean canShelterAt(ServerLevel level, BlockPos pos) {
        return !openAbove(level, pos) && level.getFluidState(pos).isEmpty()
                && level.getBlockState(pos).isPathfindable(level, pos, PathComputationType.LAND)
                && level.getBlockState(pos.above()).isPathfindable(level, pos.above(), PathComputationType.LAND);
    }

    private static boolean openAbove(ServerLevel level, BlockPos pos) {
        return level.getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX(), pos.getZ()) <= pos.getY();
    }

    private record ShelterDoor(BlockPos threshold, Direction inside) {}
}
