package art.arcane.witchery.block;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.power.AltarNature;
import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class AltarNatureTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_altar_nature")
    public static void sharesLegacyTreeCapsAcrossFlattenedBlocks(GameTestHelper helper) {
        var level = helper.getLevel();
        var center = helper.absolutePos(new BlockPos(12, 0, 5)).atY(level.getMaxBuildHeight() - 32);
        assertCapacity(helper, center, 0);
        placeLayer(level, center, 0, 24, Blocks.OAK_SAPLING, Blocks.SPRUCE_SAPLING, Blocks.BIRCH_SAPLING,
                Blocks.JUNGLE_SAPLING, Blocks.ACACIA_SAPLING, Blocks.DARK_OAK_SAPLING);
        placeLayer(level, center, 1, 56, Blocks.OAK_LOG, Blocks.SPRUCE_LOG, Blocks.BIRCH_WOOD, Blocks.JUNGLE_WOOD);
        placeLayer(level, center, 2, 54, Blocks.ACACIA_LOG, Blocks.DARK_OAK_LOG, Blocks.ACACIA_WOOD, Blocks.DARK_OAK_WOOD);
        placeLayer(level, center, 3, 104, Blocks.OAK_LEAVES, Blocks.SPRUCE_LEAVES, Blocks.BIRCH_LEAVES, Blocks.JUNGLE_LEAVES);
        placeLayer(level, center, 4, 104, Blocks.ACACIA_LEAVES, Blocks.DARK_OAK_LEAVES);
        assertCapacity(helper, center, 880);

        placeLayer(level, center, 5, 4, WitcheryBlocks.getRequired("witchlog").get());
        placeLayer(level, center, 6, 2, Blocks.STRIPPED_OAK_WOOD);
        assertCapacity(helper, center, 896);
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_altar_garden")
    public static void countsSplitGardenBlocksAgainstTheirOriginalCaps(GameTestHelper helper) {
        var level = helper.getLevel();
        var center = helper.absolutePos(new BlockPos(12, 0, 5)).atY(level.getMaxBuildHeight() - 72);
        assertCapacity(helper, center, 0);
        placeLayer(level, center, 0, 84, Blocks.DIRT, Blocks.COARSE_DIRT, Blocks.PODZOL);
        placeLayer(level, center, 1, 24, Blocks.PUMPKIN, Blocks.CARVED_PUMPKIN);
        placeLayer(level, center, 2, 12, Blocks.BROWN_MUSHROOM_BLOCK);
        placeLayer(level, center, 3, 11, Blocks.RED_MUSHROOM_BLOCK);
        assertCapacity(helper, center, 229);
        placeLayer(level, center, 4, 24, Blocks.MUSHROOM_STEM);
        assertCapacity(helper, center, 280);

        placeLayer(level, center, 5, 54, Blocks.GRASS, Blocks.FERN);
        placeLayer(level, center, 6, 36, Blocks.POPPY, Blocks.BLUE_ORCHID, Blocks.ALLIUM, Blocks.OXEYE_DAISY);
        placeLayer(level, center, 7, 31, Blocks.DANDELION);
        assertCapacity(helper, center, 670);
        placeLayer(level, center, 8, 21, Blocks.SUNFLOWER, Blocks.LILAC, Blocks.ROSE_BUSH, Blocks.PEONY,
                Blocks.TALL_GRASS, Blocks.LARGE_FERN, Blocks.NETHER_WART);
        assertCapacity(helper, center, 670);
        helper.succeed();
    }

    private static void placeLayer(ServerLevel level, BlockPos center, int dy, int count, Block... palette) {
        for (int index = 0; index < count; index++) {
            var pos = center.offset(index % 22 - 11, dy, index / 22 - 4);
            level.setBlock(pos, palette[index % palette.length].defaultBlockState(), Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_CLIENTS);
        }
    }

    private static void assertCapacity(GameTestHelper helper, BlockPos center, int expected) {
        int actual = AltarNature.capacity(helper.getLevel(), center);
        helper.assertTrue(actual == expected, "Expected " + expected + " altar power capacity, received " + actual);
    }
}
