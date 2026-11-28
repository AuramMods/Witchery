package art.arcane.witchery.block;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.world.WitcheryDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class SpiritPortalBlockTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_spirit_portal_ignition")
    public static void spiritPortalUsesOriginalSourceSideAndSnowAxis(GameTestHelper helper) {
        var dream = helper.getLevel().getServer().getLevel(WitcheryDimensions.DREAM);
        var base = helper.absolutePos(new BlockPos(4, 4, 4)).atY(170);
        var portal = WitcheryBlocks.getRequired("spiritportal").get();
        var spirit = WitcheryBlocks.getRequired("spiritflowing").get();
        for (Direction along : new Direction[]{Direction.EAST, Direction.SOUTH}) {
            var point = along == Direction.EAST ? base : base.east(24);
            var ambiguous = point.south(16);
            var partlyFilled = point.south(32);
            buildFrame(dream, point, along);
            helper.assertTrue(SpiritPortalBlock.tryIgnite(dream, point.relative(along)),
                    "An empty preceding slot locates the original two-wide frame");
            helper.assertTrue(dream.getBlockState(point).is(portal)
                            && dream.getBlockState(point).getValue(SpiritPortalBlock.AXIS) == along.getAxis(),
                    "The opening retains the original axis");

            buildFrame(dream, ambiguous, along);
            dream.setBlock(ambiguous.relative(along.getClockWise()), Blocks.SNOW_BLOCK.defaultBlockState(), Block.UPDATE_CLIENTS);
            helper.assertFalse(SpiritPortalBlock.tryIgnite(dream, ambiguous),
                    "Perpendicular snow makes the original trigger ambiguous");

            buildFrame(dream, partlyFilled, along);
            var flowing = spirit.defaultBlockState().setValue(net.minecraft.world.level.block.LiquidBlock.LEVEL, 2);
            dream.setBlock(partlyFilled, flowing, Block.UPDATE_CLIENTS);
            helper.assertFalse(SpiritPortalBlock.tryIgnite(dream, partlyFilled.relative(along)),
                    "A non-air preceding slot prevents the original origin shift");
            helper.assertTrue(SpiritPortalBlock.tryIgnite(dream, partlyFilled),
                    "The first column still accepts Flowing Spirit in its frame");
        }
        helper.succeed();
    }

    private static void buildFrame(ServerLevel level, BlockPos bottom, Direction along) {
        for (int x = -1; x <= 2; x++) {
            for (int y = -1; y <= 2; y++) {
                boolean side = x == -1 || x == 2;
                boolean edge = y == -1 || y == 2;
                var pos = bottom.relative(along, x).above(y);
                var block = side != edge ? Blocks.SNOW_BLOCK : Blocks.AIR;
                level.setBlock(pos, block.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
        }
    }
}
