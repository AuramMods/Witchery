package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.SpiritPortalBlock;
import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class SpiritPortalSaveImportTests {
    @GameTest(template = "qa_coven_arena")
    public static void legacyFrameSurvivesMixedResolvedAndUnresolvedCells(GameTestHelper helper) {
        var bottom = helper.absolutePos(new BlockPos(4, 2, 4));
        verifyFrame(helper, bottom, Direction.EAST);
        verifyFrame(helper, bottom, Direction.SOUTH);
        helper.succeed();
    }

    private static void verifyFrame(GameTestHelper helper, BlockPos bottom, Direction direction) {
        var level = helper.getLevel();
        var portal = WitcheryBlocks.getRequired("spiritportal").get().defaultBlockState()
                .setValue(SpiritPortalBlock.LEGACY_ORIENTATION, true);
        for (int along = -1; along <= 2; along++) {
            for (int height = -1; height <= 2; height++) {
                boolean edge = along == -1 || along == 2 || height == -1 || height == 2;
                level.setBlock(bottom.relative(direction, along).above(height),
                        edge ? Blocks.SNOW_BLOCK.defaultBlockState() : portal, Block.UPDATE_KNOWN_SHAPE);
            }
        }
        var cells = List.of(bottom, bottom.relative(direction), bottom.above(), bottom.relative(direction).above());
        for (BlockPos cell : cells) {
            var resolved = Block.updateFromNeighbourShapes(portal, level, cell);
            helper.assertTrue(resolved.is(portal.getBlock()) && resolved.getValue(SpiritPortalBlock.AXIS) == direction.getAxis()
                            && !resolved.getValue(SpiritPortalBlock.LEGACY_ORIENTATION),
                    "Resolve each imported portal cell using the same complete snow frame");
            level.setBlock(cell, resolved, Block.UPDATE_KNOWN_SHAPE);
        }
        level.setBlockAndUpdate(bottom.relative(direction, -1), Blocks.AIR.defaultBlockState());
        helper.assertTrue(cells.stream().allMatch(level::isEmptyBlock), "The resolved portal collapses when its snow frame breaks");
    }
}
