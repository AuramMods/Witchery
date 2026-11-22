package art.arcane.witchery.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.block.DoorBlock;

final class RestrictVillageDoorGoal extends Goal {
    private final PathfinderMob mob;
    private BlockPos door = BlockPos.ZERO;
    private Direction inside = Direction.NORTH;
    private boolean couldOpen;
    private boolean couldPass;
    private long nextSearch;

    RestrictVillageDoorGoal(PathfinderMob mob) {
        this.mob = mob;
    }

    @Override
    public boolean canUse() {
        if (!(mob.level() instanceof ServerLevel level) || !level.isDay() || level.getGameTime() < nextSearch) return false;
        nextSearch = level.getGameTime() + 10;
        BlockPos origin = mob.blockPosition();
        if (!level.isCloseToVillage(origin, 2)) return false;
        double nearest = Double.POSITIVE_INFINITY;
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-3, -1, -3), origin.offset(3, 1, 3))) {
            if (!level.hasChunkAt(pos)) continue;
            var state = level.getBlockState(pos);
            double distance = pos.distSqr(origin);
            if (distance >= nearest || !VillageShelterGoal.isWoodenThreshold(state)) continue;
            Direction covered = VillageShelterGoal.coveredSide(level, pos, state.getValue(DoorBlock.FACING));
            if (covered == null || pos.relative(covered, 2).distSqr(origin) >= 2.25) continue;
            nearest = distance;
            door = pos.immutable();
            inside = covered;
        }
        return nearest < Double.POSITIVE_INFINITY;
    }

    @Override
    public boolean canContinueToUse() {
        BlockPos pos = mob.blockPosition();
        return mob.level().isDay() && mob.level().hasChunkAt(door)
                && VillageShelterGoal.isWoodenThreshold(mob.level().getBlockState(door))
                && (pos.getX() - door.getX()) * inside.getStepX() + (pos.getZ() - door.getZ()) * inside.getStepZ() >= 0;
    }

    @Override
    public void start() {
        var navigation = (GroundPathNavigation) mob.getNavigation();
        couldOpen = navigation.getNodeEvaluator().canOpenDoors();
        couldPass = navigation.canPassDoors();
        navigation.setCanOpenDoors(false);
        navigation.setCanPassDoors(false);
    }

    @Override
    public void stop() {
        var navigation = (GroundPathNavigation) mob.getNavigation();
        navigation.setCanOpenDoors(couldOpen);
        navigation.setCanPassDoors(couldPass);
    }
}
