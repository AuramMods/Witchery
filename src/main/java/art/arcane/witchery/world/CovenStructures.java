package art.arcane.witchery.world;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.data.WitcheryLootTables;
import art.arcane.witchery.registry.WitcheryEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraftforge.event.ForgeEventFactory;

final class CovenStructures {
    private static final ResourceLocation SHACK_LOOT = ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "chests/witch_shack");
    private static final int[][] PILLARS = {
            {1, 2}, {2, 1}, {4, 0}, {6, 0}, {8, 1}, {9, 2}, {10, 4}, {10, 6},
            {9, 8}, {8, 9}, {6, 10}, {4, 10}, {2, 9}, {1, 8}, {0, 4}, {0, 6}
    };
    private static final int[][] ARCHES = {
            {3, 1}, {4, 0}, {5, 0}, {6, 0}, {7, 1}, {9, 3}, {10, 4}, {10, 5}, {10, 6}, {9, 7},
            {7, 9}, {6, 10}, {5, 10}, {4, 10}, {3, 9}, {1, 3}, {0, 4}, {0, 5}, {0, 6}, {1, 7}
    };

    private CovenStructures() {}

    static void circle(SurfaceStructureSite site, RandomSource random) {
        for (int z = 0; z < site.depth(); z++) {
            for (int x = 0; x < site.width(); x++) {
                boolean floor = (x >= 3 && x <= 7) || (z >= 3 && z <= 7)
                        || (x > 0 && x < 10 && z > 0 && z < 10);
                if (floor) site.put(x, 0, z, site.surface());
                if (floor || !site.level().isEmptyBlock(site.at(x, 0, z))) site.support(x, z);
                site.clearAbove(x, z, floor ? 1 : 4, 23);
            }
        }

        site.fill(4, 1, 4, 6, 1, 6, Blocks.CHISELED_STONE_BRICKS);
        site.put(5, 1, 5, Blocks.WATER);
        site.put(5, 0, 5, Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING, Direction.EAST));
        RandomizableContainerBlockEntity.setLootTable(site.level(), random, site.at(5, 0, 5), WitcheryLootTables.MINESHAFT_CORRIDOR);
        site.spawner(4, 0, 4, EntityType.WITCH, random);
        site.spawner(6, 0, 6, EntityType.WITCH, random);

        Block stone = site.sandy() ? Blocks.SANDSTONE : Blocks.COBBLESTONE;
        Block brick = site.sandy() ? Blocks.CUT_SANDSTONE : Blocks.CRACKED_STONE_BRICKS;
        for (int[] pillar : PILLARS) {
            int x = pillar[0], z = pillar[1];
            boolean edge = x == 0 || x == 10 || z == 0 || z == 10;
            site.put(x, 1, z, stone);
            site.put(x, 2, z, edge ? stone : brick);
            site.put(x, 3, z, stone);
        }
        for (int[] arch : ARCHES) site.put(arch[0], 3, arch[1], brick);
        site.put(3, 1, 0, Blocks.COBWEB);
        site.put(10, 2, 7, Blocks.COBWEB);
        site.put(0, 3, 7, Blocks.COBWEB);
        if (random.nextInt(10) != 0) spawnWitch(site, site.at(7, 1, 4));
    }

    static void shack(SurfaceStructureSite site, RandomSource random) {
        site.fill(0, 1, 0, 6, 5, 6, Blocks.AIR);
        site.fill(0, 0, 1, 6, 1, 5, Blocks.COBBLESTONE);
        site.fill(0, 2, 1, 6, 3, 5, Blocks.OAK_PLANKS);
        site.fill(1, 1, 2, 5, 3, 4, Blocks.AIR);
        for (int z = 0; z < site.depth(); z++) {
            for (int x = 0; x < site.width(); x++) {
                if (!site.level().isEmptyBlock(site.at(x, 0, z))) site.support(x, z);
                site.clearAbove(x, z, 6, 25);
            }
        }

        for (int x : new int[]{0, 6}) {
            site.fill(x, 1, 1, x, 3, 1, Blocks.SPRUCE_LOG);
            site.fill(x, 1, 5, x, 3, 5, Blocks.SPRUCE_LOG);
            for (int z = 2; z <= 4; z++) {
                site.put(x, 4, z, Blocks.SPRUCE_LOG.defaultBlockState()
                        .setValue(RotatedPillarBlock.AXIS, site.facing().getAxis()));
            }
        }
        var northRoof = Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, site.direction(Direction.NORTH));
        var southRoof = Blocks.SPRUCE_STAIRS.defaultBlockState().setValue(StairBlock.FACING, site.direction(Direction.SOUTH));
        for (int x = 0; x < site.width(); x++) {
            for (int z = 0; z < 3; z++) {
                site.put(x, 3 + z, z, northRoof);
                site.put(x, 3 + z, 6 - z, southRoof);
            }
            site.put(x, 5, 3, Blocks.SPRUCE_PLANKS);
        }

        for (int[] window : new int[][]{{2, 1}, {2, 5}, {4, 5}, {0, 3}, {6, 3}}) {
            site.putConnected(window[0], 2, window[1], Blocks.GLASS_PANE);
        }
        var door = Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING, site.direction(Direction.SOUTH));
        site.put(4, 1, 1, door);
        site.put(4, 2, 1, door.setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
        site.put(1, 1, 4, Blocks.BIRCH_PLANKS);
        site.put(1, 2, 4, Blocks.TORCH);
        site.put(1, 1, 3, Blocks.BIRCH_STAIRS.defaultBlockState()
                .setValue(StairBlock.FACING, site.direction(Direction.WEST)));
        site.put(2, 1, 4, Blocks.BIRCH_STAIRS.defaultBlockState()
                .setValue(StairBlock.FACING, site.direction(Direction.NORTH)));
        site.putConnected(2, 1, 3, Blocks.OAK_FENCE);
        site.put(2, 2, 3, Blocks.OAK_PRESSURE_PLATE);
        site.put(1, 1, 2, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, site.direction(Direction.EAST)));
        RandomizableContainerBlockEntity.setLootTable(site.level(), random, site.at(1, 1, 2), SHACK_LOOT);
        if (random.nextInt(4) != 0) spawnWitch(site, site.at(4, 1, 3));
    }

    private static void spawnWitch(SurfaceStructureSite site, BlockPos position) {
        var level = site.level();
        var witch = WitcheryEntities.COVEN_WITCH.get().create(level.getLevel());
        if (witch == null) return;
        witch.moveTo(position.getX() + 0.5, position.getY(), position.getZ() + 0.5, site.facing().toYRot(), 0);
        ForgeEventFactory.onFinalizeSpawn(witch, level, level.getCurrentDifficultyAt(position), MobSpawnType.STRUCTURE, null, null);
        witch.setPersistenceRequired();
        level.addFreshEntity(witch);
    }
}
