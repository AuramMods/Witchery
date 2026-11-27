package art.arcane.witchery.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;

public final class WitcheryCreatureSpawns {
    private WitcheryCreatureSpawns() {
    }

    public static boolean spawnNear(ServerLevel level, BlockPos near, Mob mob, int minRange, int maxRange,
                                    RandomSource random) {
        int span = maxRange - minRange;
        int dx = random.nextInt(span * 2 + 1);
        int dz = random.nextInt(span * 2 + 1);
        // The middle draw belongs to the negative range on each axis.
        if (dx > span) dx += 2 * minRange;
        if (dz > span) dz += 2 * minRange;
        BlockPos.MutableBlockPos floor = near.offset(dx - maxRange, 0, dz - maxRange).mutable();
        int highestFloor = Math.min(near.getY() + 8, level.getMaxBuildHeight() - 3);
        while (floor.getY() < highestFloor && !level.isEmptyBlock(floor)) {
            floor.move(Direction.UP);
        }
        while (floor.getY() > level.getMinBuildHeight() && level.isEmptyBlock(floor)) {
            floor.move(Direction.DOWN);
        }
        if (level.isOutsideBuildHeight(floor) || floor.getY() > highestFloor || level.isEmptyBlock(floor)
                || !level.isEmptyBlock(floor.above()) || !level.isEmptyBlock(floor.above(2))) {
            return false;
        }
        mob.moveTo(floor.getX() + 0.5, floor.getY() + 1.05, floor.getZ() + 0.5, 0, 0);
        return level.noCollision(mob) && !level.containsAnyLiquid(mob.getBoundingBox()) && level.addFreshEntity(mob);
    }
}
