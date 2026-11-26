package art.arcane.witchery.brewing;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.BrewDispersalBlock;
import art.arcane.witchery.block.entity.BrewFluidBlockEntity;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
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
