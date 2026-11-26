package art.arcane.witchery.brewing;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.BrewDispersalBlock;
import art.arcane.witchery.block.entity.BrewFluidBlockEntity;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class BrewLiquidFlowTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_liquid_terrain")
    public static void terrainContactFollowsTheLiquidUpdate(GameTestHelper helper) {
        var level = helper.getLevel();
        var block = (BrewDispersalBlock) WitcheryBlocks.getRequired("brewliquid").get();
        var ingredients = List.of(Items.NETHER_WART.getDefaultInstance(), Items.DIRT.getDefaultInstance(),
                WitcheryIngredient.WORMWOOD.item().getDefaultInstance());
        var mixture = BrewMixture.from(ingredients);
        var tag = BrewMixture.ingredientTag(ingredients, 0x123456);

        var pool = prepare(helper, new BlockPos(2, 2, 2));
        var fall = prepare(helper, new BlockPos(8, 2, 2));
        var edge = prepare(helper, new BlockPos(2, 2, 8));
        var column = prepare(helper, new BlockPos(8, 2, 8));
        level.removeBlock(fall.below(), false);
        liquid(level, block, edge.west(), 4, mixture, tag);
        liquid(level, block, column.above(), 0, mixture, tag);
        liquid(level, block, column.below(), 1, mixture, tag);

        liquid(level, block, pool, 0, mixture, tag);
        liquid(level, block, fall, 0, mixture, tag);
        liquid(level, block, edge, 5, mixture, tag);
        liquid(level, block, column, 1, mixture, tag);
        for (var pos : List.of(pool, fall, edge, column)) {
            // In the original direction order, only the east neighbor passes the terrain roll.
            level.random.setSeed(1);
            block.tick(level.getBlockState(pos), level, pos, RandomSource.create(1));
        }

        helper.assertTrue(level.getBlockState(pool.east()).is(Blocks.FARMLAND), "The pool tills adjacent soil");
        helper.assertTrue(level.getBlockState(column.east()).is(Blocks.FARMLAND),
                "A column already flowing below still samples terrain");
        helper.assertTrue(level.getBlockState(fall.below()).is(block)
                        && level.getBlockState(fall.east()).is(Blocks.DIRT),
                "A flow into the cell below skips adjacent terrain");
        helper.assertTrue(level.getBlockState(edge).getValue(LiquidBlock.LEVEL) == 5
                        && level.getBlockState(edge.east()).is(Blocks.DIRT),
                "The outermost flow stays supplied without affecting adjacent terrain");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_liquid_displacement")
    public static void liquidWashesAwayOriginalBlocksAndDropsTheirItems(GameTestHelper helper) {
        var level = helper.getLevel();
        var block = (BrewDispersalBlock) WitcheryBlocks.getRequired("brewliquid").get();
        var ingredients = List.of(Items.NETHER_WART.getDefaultInstance(), Items.SUGAR.getDefaultInstance(),
                WitcheryIngredient.WORMWOOD.item().getDefaultInstance());
        var mixture = BrewMixture.from(ingredients);
        var tag = BrewMixture.ingredientTag(ingredients, 0x123456);
        var cases = List.of(
                new Displacement(Blocks.TORCH.defaultBlockState(), true, List.of(Items.TORCH)),
                new Displacement(Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, 7),
                        false, List.of(Items.WHEAT, Items.WHEAT_SEEDS)),
                new Displacement(Blocks.COBWEB.defaultBlockState(), true, List.of(Items.STRING)),
                new Displacement(Blocks.POTTED_POPPY.defaultBlockState(), true, List.of(Items.FLOWER_POT, Items.POPPY)),
                new Displacement(Blocks.LEVER.defaultBlockState().setValue(LeverBlock.FACE, AttachFace.FLOOR)
                        .setValue(LeverBlock.FACING, Direction.NORTH).setValue(LeverBlock.POWERED, true), true, List.of(Items.LEVER)),
                new Displacement(Blocks.OAK_SIGN.defaultBlockState(), true, List.of()),
                new Displacement(Blocks.WATER.defaultBlockState(), false, List.of()),
                new Displacement(Blocks.SUGAR_CANE.defaultBlockState(), true, List.of()),
                new Displacement(Blocks.FLOWERING_AZALEA_LEAVES.defaultBlockState()
                        .setValue(LeavesBlock.PERSISTENT, true), true, List.of()));

        for (int index = 0; index < cases.size(); index++) {
            var test = cases.get(index);
            var target = helper.absolutePos(new BlockPos(2 + index % 3 * 4, 2, 2 + index / 3 * 4)).atY(196);
            var source = test.down ? target.above() : target.west();
            level.setBlockAndUpdate(target.below(), (test.state.is(Blocks.WHEAT) ? Blocks.FARMLAND : Blocks.STONE).defaultBlockState());
            if (test.state.is(Blocks.SUGAR_CANE)) {
                level.setBlockAndUpdate(target.east().below(), Blocks.WATER.defaultBlockState());
                level.setBlockAndUpdate(target.below(), Blocks.SAND.defaultBlockState());
            }
            if (!test.down) {
                for (var wall : List.of(source.below(), source.west(), source.north(), source.south())) {
                    level.setBlockAndUpdate(wall, Blocks.STONE.defaultBlockState());
                }
            }
            level.setBlockAndUpdate(target, test.state);
            if (test.state.is(Blocks.LEVER)) {
                level.setBlockAndUpdate(target.east().below(), Blocks.STONE.defaultBlockState());
                level.setBlockAndUpdate(target.east(), Blocks.REDSTONE_WIRE.defaultBlockState());
                helper.assertTrue(level.getBlockState(target.east()).getValue(RedStoneWireBlock.POWER) == 15,
                        "The lever starts with the adjacent wire powered");
            }

            liquid(level, block, source, 0, mixture, tag);
            level.getBlockState(source).tick(level, source, RandomSource.create(1));
            var state = level.getBlockState(target);
            var drops = level.getEntitiesOfClass(ItemEntity.class, new AABB(target).inflate(1));
            if (test.drops.isEmpty()) {
                helper.assertTrue(state.is(test.state.getBlock()) && drops.isEmpty(),
                        "Liquid preserves " + test.state.getBlock() + " and creates no drops");
            } else {
                helper.assertTrue(state.is(block) && state.getValue(LiquidBlock.LEVEL) == 1,
                        "Liquid washes away " + test.state.getBlock());
                var fluid = (BrewFluidBlockEntity) level.getBlockEntity(target);
                helper.assertTrue(fluid.color() == 0x123456 && fluid.saveWithoutMetadata().getCompound("Effect").equals(tag),
                        "The displaced cell receives the original brew and tint");
                for (Item item : test.drops) {
                    int count = drops.stream().filter(drop -> drop.getItem().is(item)).mapToInt(drop -> drop.getItem().getCount()).sum();
                    helper.assertTrue(item == Items.WHEAT_SEEDS ? count >= 1 : count == 1,
                            "The displaced block drops the expected amount of " + item);
                }
                helper.assertTrue(drops.stream().allMatch(drop -> test.drops.contains(drop.getItem().getItem())),
                        "Displacement creates no unrelated drops");
            }
            if (test.state.is(Blocks.LEVER)) {
                helper.assertTrue(level.getBlockState(target.east()).getValue(RedStoneWireBlock.POWER) == 0,
                        "The displaced lever immediately depowers the wire");
            }
            drops.forEach(ItemEntity::discard);
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void liquidRoutesThroughExistingFlowWithoutOverwritingItsSource(GameTestHelper helper) {
        var level = helper.getLevel();
        var block = (BrewDispersalBlock) WitcheryBlocks.getRequired("brewliquid").get();
        var ingredients = List.of(Items.NETHER_WART.getDefaultInstance(), Items.SUGAR.getDefaultInstance(),
                WitcheryIngredient.WORMWOOD.item().getDefaultInstance());
        var mixture = BrewMixture.from(ingredients);
        var tag = BrewMixture.ingredientTag(ingredients, 0x123456);
        var pos = helper.absolutePos(new BlockPos(5, 2, 5)).atY(196);
        for (var wall : List.of(pos.north(), pos.south(), pos.below(), pos.east().below())) {
            level.setBlockAndUpdate(wall, Blocks.STONE.defaultBlockState());
        }
        level.removeBlock(pos.east().east().below(), false);
        liquid(level, block, pos, 0, mixture, tag);
        liquid(level, block, pos.west(), 1, mixture, tag);
        liquid(level, block, pos.west().below(), 0, mixture, tag);

        level.getBlockState(pos).tick(level, pos, RandomSource.create(1));
        helper.assertTrue(level.isEmptyBlock(pos.east())
                        && level.getBlockState(pos.west()).getValue(LiquidBlock.LEVEL) == 1,
                "The short route into existing liquid wins without overwriting it");

        liquid(level, block, pos.west(), 0, mixture, tag);
        level.getBlockState(pos).tick(level, pos, RandomSource.create(1));
        helper.assertTrue(level.getBlockState(pos.east()).is(block)
                        && level.getBlockState(pos.west()).getValue(LiquidBlock.LEVEL) == 0,
                "An adjacent source stays intact and is excluded from the lateral route search");
        helper.succeed();
    }

    private record Displacement(BlockState state, boolean down, List<Item> drops) {}

    private static BlockPos prepare(GameTestHelper helper, BlockPos local) {
        var pos = helper.absolutePos(local).atY(196);
        var level = helper.getLevel();
        for (var wall : List.of(pos.north(), pos.south(), pos.west(), pos.below())) {
            level.setBlockAndUpdate(wall, Blocks.STONE.defaultBlockState());
        }
        level.setBlockAndUpdate(pos.east(), Blocks.DIRT.defaultBlockState());
        return pos;
    }

    private static void liquid(ServerLevel level, BrewDispersalBlock block, BlockPos pos, int depth,
                               BrewMixture mixture, CompoundTag tag) {
        level.setBlockAndUpdate(pos, block.defaultBlockState().setValue(LiquidBlock.LEVEL, depth));
        var fluid = (BrewFluidBlockEntity) level.getBlockEntity(pos);
        fluid.initialize(tag, mixture, 0x123456, null);
    }
}
