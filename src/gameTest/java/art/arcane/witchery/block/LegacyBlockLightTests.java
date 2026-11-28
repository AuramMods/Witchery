package art.arcane.witchery.block;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class LegacyBlockLightTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_legacy_block_light", timeoutTicks = 150)
    public static void constructionAndWorkstationsUpdateBothLightLayers(GameTestHelper helper) {
        var level = helper.getLevel();
        var beam = helper.absolutePos(new BlockPos(5, 4, 5));
        var shaft = helper.absolutePos(new BlockPos(5, 4, 10)).atY(216);
        var stone = Blocks.STONE.defaultBlockState();
        for (var pos : BlockPos.betweenClosed(beam.offset(-2, -1, -1), beam.offset(2, 1, 1))) {
            level.setBlockAndUpdate(pos, stone);
        }
        level.setBlockAndUpdate(beam.west(), Blocks.GLOWSTONE.defaultBlockState());
        level.setBlockAndUpdate(beam.east(), Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(beam, Blocks.AIR.defaultBlockState());
        for (var pos : BlockPos.betweenClosed(shaft.offset(-1, -2, -1), shaft.offset(1, 4, 1))) {
            boolean open = pos.getX() == shaft.getX() && pos.getZ() == shaft.getZ() && pos.getY() > shaft.getY() - 2;
            level.setBlockAndUpdate(pos, open ? Blocks.AIR.defaultBlockState() : stone);
        }

        helper.startSequence()
                .thenWaitUntil(() -> assertLight(helper, level, beam, shaft, 13, 15, "The open chambers"))
                .thenExecute(() -> place(level, beam, shaft, "iceslab"))
                .thenWaitUntil(() -> assertLight(helper, level, beam, shaft, 11, 11, "The Perpetual Ice slab"))
                .thenExecute(() -> place(level, beam, shaft, "snowstairs"))
                .thenWaitUntil(() -> assertLight(helper, level, beam, shaft, 0, 0, "The snow stairs"))
                .thenExecute(() -> place(level, beam, shaft, "kettle"))
                .thenWaitUntil(() -> assertLight(helper, level, beam, shaft, 13, 15, "The Kettle"))
                .thenSucceed();
    }

    private static void place(ServerLevel level, BlockPos beam, BlockPos shaft, String path) {
        BlockState state = WitcheryBlocks.getRequired(path).get().defaultBlockState();
        level.setBlock(beam, state, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        level.setBlock(shaft, state, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
    }

    private static void assertLight(GameTestHelper helper, ServerLevel level, BlockPos beam, BlockPos shaft,
                                    int block, int sky, String description) {
        int blockLight = level.getBrightness(LightLayer.BLOCK, beam.east());
        int skyLight = level.getBrightness(LightLayer.SKY, shaft.below());
        helper.assertTrue(blockLight == block && skyLight == sky,
                description + " pass " + block + " block light and " + sky + " skylight. Got " + blockLight + ", " + skyLight);
    }
}
