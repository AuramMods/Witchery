package art.arcane.witchery.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.common.world.MobSpawnSettingsBuilder;

public final class WitcheryCreatureSpawns {
    private WitcheryCreatureSpawns() {
    }

    public static boolean passesTameableLightCheck(LevelReader level, BlockPos pos) {
        // The original path weight accepted grass at any light and other ground at brightness 0.5.
        return level.getBlockState(pos.below()).is(Blocks.GRASS_BLOCK) || level.getMaxLocalRawBrightness(pos) >= 12;
    }

    public static boolean isClearForSpawn(LevelReader level, EntityType<?> type, BlockPos pos) {
        var bounds = type.getAABB(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        return level.isUnobstructed(null, Shapes.create(bounds)) && level.noCollision(bounds)
                && !level.containsAnyLiquid(bounds);
    }

    static void addWeightedSpawn(MobSpawnSettingsBuilder spawns, EntityType<?> type, long weight, int min, int max) {
        var category = type.getCategory();
        int available = Integer.MAX_VALUE - WeightedRandom.getTotalWeight(spawns.getSpawner(category));
        int appliedWeight = (int) Math.min(available, weight);
        if (appliedWeight > 0) spawns.addSpawn(category, new MobSpawnSettings.SpawnerData(type, appliedWeight, min, max));
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
