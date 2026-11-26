package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.WitchLeavesBlock;
import art.arcane.witchery.block.WitchWoodType;
import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class LegacyWitchLeafTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_legacy_leaf_decay")
    public static void importedLeavesKeepFourBlockReachAndTheirSavedFlags(GameTestHelper helper) {
        var level = helper.getLevel();
        var root = helper.absolutePos(new BlockPos(5, 3, 5)).atY(196);
        loadCanopy(helper, root);
        var pending = fromMetadata(9);
        var unchecked = fromMetadata(2);
        var persistent = fromMetadata(14);
        helper.assertTrue(pending.getValue(WitchLeavesBlock.CHECK_DECAY) && !pending.getValue(LeavesBlock.PERSISTENT)
                        && pending.getValue(WitchWoodType.PROPERTY) == WitchWoodType.ALDER
                        && !unchecked.getValue(WitchLeavesBlock.CHECK_DECAY)
                        && persistent.getValue(LeavesBlock.PERSISTENT) && persistent.getValue(WitchLeavesBlock.CHECK_DECAY),
                "Import the species, player-placement bit, and pending check independently");
        level.setBlockAndUpdate(root, Blocks.OAK_LOG.defaultBlockState());
        level.setBlockAndUpdate(root.east(), Blocks.BIRCH_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true));
        for (int i = 2; i <= 5; ++i) level.setBlockAndUpdate(root.east(i), pending);
        checkNow(helper, root.east(4));
        var checked = level.getBlockState(root.east(4));
        helper.assertTrue(checked.is(pending.getBlock()) && !checked.getValue(WitchLeavesBlock.CHECK_DECAY),
                "The fourth linked leaf clears its pending check through a mixed canopy");
        checkNow(helper, root.east(5));
        helper.assertTrue(level.isEmptyBlock(root.east(5))
                        && level.getBlockState(root.east(4)).getValue(WitchLeavesBlock.CHECK_DECAY),
                "The fifth leaf decays and marks its neighbor for another check");

        level.setBlockAndUpdate(root.south(6), unchecked);
        level.setBlockAndUpdate(root.south(6).east(2), persistent);
        checkNow(helper, root.south(6));
        checkNow(helper, root.south(6).east(2));
        helper.assertTrue(level.getBlockState(root.south(6)) == unchecked
                        && level.getBlockState(root.south(6).east(2)) == persistent,
                "Unchecked and player-placed leaves stay even when isolated");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_legacy_leaf_decay")
    public static void removingLogsMarksNearbyLeavesAndOlderPalettesRetainChecks(GameTestHelper helper) throws Exception {
        var level = helper.getLevel();
        var root = helper.absolutePos(new BlockPos(5, 3, 5)).atY(196);
        loadCanopy(helper, root);
        var inside = root.offset(4, 0, 4);
        var outside = root.offset(5, 0, 4);
        var leaf = fromMetadata(0);
        level.setBlockAndUpdate(root, Blocks.OAK_LOG.defaultBlockState());
        level.setBlockAndUpdate(inside, leaf);
        level.setBlockAndUpdate(outside, leaf);
        level.setBlockAndUpdate(root, Blocks.OAK_LOG.defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.X));
        helper.assertTrue(level.getBlockState(inside) == leaf, "A state update on the same log leaves the canopy unchecked");
        level.removeBlock(root, false);
        helper.assertTrue(level.getBlockState(inside).getValue(WitchLeavesBlock.CHECK_DECAY)
                        && level.getBlockState(outside) == leaf,
                "Removing a vanilla log marks Witchery leaves throughout its original four-block box");
        checkNow(helper, inside);
        helper.assertTrue(level.isEmptyBlock(inside) && level.getBlockState(outside).getValue(WitchLeavesBlock.CHECK_DECAY),
                "The first detached leaf marks the neighboring leaf when it decays");

        var chunk = TagParser.parseTag("""
                {sections:[{block_states:{palette:[
                {Name:"witchery:witchleaves",Properties:{distance:"7",persistent:"false"}},
                {Name:"witchery:witchleaves",Properties:{distance:"6",persistent:"false"}},
                {Name:"witchery:witchleaves",Properties:{distance:"7",persistent:"true"}},
                {Name:"witchery:witchleaves",Properties:{distance:"7",persistent:"false",check_decay:"false"}},
                {Name:"minecraft:oak_leaves",Properties:{distance:"7",persistent:"false"}}
                ]}}]}
                """);
        LegacyChunkUpgrade.restorePendingLeafChecks(chunk);
        var palette = chunk.getList("sections", Tag.TAG_COMPOUND).getCompound(0).getCompound("block_states")
                .getList("palette", Tag.TAG_COMPOUND);
        helper.assertTrue(palette.getCompound(0).getCompound("Properties").getString("check_decay").equals("true"),
                "An earlier unsupported Witchery leaf retains its pending vanilla check");
        for (int i : new int[]{1, 2, 4}) {
            helper.assertTrue(!palette.getCompound(i).getCompound("Properties").contains("check_decay"),
                    "An earlier supported or persistent leaf and a vanilla leaf are left alone");
        }
        helper.assertTrue(palette.getCompound(3).getCompound("Properties").getString("check_decay").equals("false"),
                "An explicit modern unchecked state takes precedence");
        helper.succeed();
    }

    private static BlockState fromMetadata(int metadata) {
        var entry = WitcheryBlocks.getRequired("witchleaves");
        return NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), LegacyChunkUpgrade.restoreState(entry.getId(), metadata, 0));
    }

    private static void loadCanopy(GameTestHelper helper, BlockPos root) {
        ChunkPos.rangeClosed(new ChunkPos(root.offset(-7, 0, -7)), new ChunkPos(root.offset(12, 0, 13)))
                .forEach(pos -> helper.getLevel().getChunk(pos.x, pos.z));
    }

    private static void checkNow(GameTestHelper helper, BlockPos pos) {
        var level = helper.getLevel();
        var state = level.getBlockState(pos);
        state.getBlock().randomTick(state, level, pos, level.random);
    }
}
