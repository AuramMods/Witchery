package art.arcane.witchery.world;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.SomnianCottonBlock;
import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class SomnianCottonTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_cotton_spread")
    public static void cottonSpreadUsesWorldRollAndLegacyDirtVariants(GameTestHelper helper) {
        var overworld = helper.getLevel();
        var dream = overworld.getServer().getLevel(WitcheryDimensions.DREAM);
        var seedling = (SomnianCottonBlock) WitcheryBlocks.getRequired("somniancotton").get();
        var state = seedling.defaultBlockState();
        var spirit = WitcheryBlocks.getRequired("spiritflowing").get().defaultBlockState();
        var anchor = helper.absolutePos(new BlockPos(3, 2, 3)).atY(150);

        for (Block ground : new Block[]{Blocks.COARSE_DIRT, Blocks.PODZOL}) {
            var start = ground == Blocks.COARSE_DIRT ? anchor : anchor.south(16);
            for (var corner : List.of(start.offset(-5, 0, -5), start.offset(-5, 0, 5),
                    start.offset(5, 0, -5), start.offset(5, 0, 5))) dream.getChunkAt(corner);
            for (int x = 0; x <= 5; x++) {
                var pos = start.east(x);
                dream.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                dream.setBlockAndUpdate(pos.below(), ground.defaultBlockState());
            }
            dream.setBlockAndUpdate(start.north().below(), spirit);
            dream.setBlockAndUpdate(start, state);

            var offsets = new EastSteps();
            dream.getRandom().setSeed(42);
            seedling.randomTick(state, dream, start, offsets);
            helper.assertTrue(offsets.draws == 0 && dream.isEmptyBlock(start.east(5)),
                    "An unsuccessful roll on the world's random source leaves the offset source untouched");

            dream.getRandom().setSeed(0);
            seedling.randomTick(state, dream, start, offsets);
            helper.assertTrue(offsets.draws == 20 && dream.getBlockState(start.east(5)).is(seedling),
                    "Cotton spreads five steps over the original dirt variants using only the supplied offset source");
        }

        var expected = new LegacyRandomSource(0);
        expected.nextInt(6);
        overworld.getRandom().setSeed(0);
        var offsets = new EastSteps();
        seedling.randomTick(state, overworld, helper.absolutePos(BlockPos.ZERO), offsets);
        helper.assertTrue(offsets.draws == 0 && overworld.getRandom().nextInt(1000) == expected.nextInt(1000),
                "The original world roll still occurs outside Dream without a spread attempt");
        helper.succeed();
    }

    private static final class EastSteps extends LegacyRandomSource {
        private int draws;

        private EastSteps() {
            super(0);
        }

        @Override
        public int nextInt(int bound) {
            int axis = draws++ % 4;
            int expected = axis == 0 || axis == 3 ? 3 : 2;
            if (bound != expected) throw new AssertionError("Unexpected cotton spread bound " + bound);
            return axis == 0 ? 2 : axis == 3 ? 1 : 0;
        }
    }
}
