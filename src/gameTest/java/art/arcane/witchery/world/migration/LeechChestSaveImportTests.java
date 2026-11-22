package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.LeechChestBlock;
import art.arcane.witchery.block.entity.LeechChestBlockEntity;
import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class LeechChestSaveImportTests {
    @GameTest(template = "qa_coven_arena")
    public static void legacyHalvesReconnectWithoutLosingTheirInventories(GameTestHelper helper) {
        var level = helper.getLevel();
        var chest = (LeechChestBlock) WitcheryBlocks.getRequired("leechchest").get();
        var pending = chest.defaultBlockState().setValue(ChestBlock.FACING, Direction.NORTH)
                .setValue(LeechChestBlock.LEGACY_PAIRING, true);
        BlockPos left = helper.absolutePos(new BlockPos(4, 2, 4));
        BlockPos right = left.east();
        int flags = Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE;
        level.setBlock(left, pending, flags);
        level.setBlock(right, pending, flags);
        ((LeechChestBlockEntity) level.getBlockEntity(left)).setItem(5, new ItemStack(Items.DIAMOND));
        ((LeechChestBlockEntity) level.getBlockEntity(right)).setItem(7, new ItemStack(Items.GOLD_INGOT));

        // Chunk post-processing resolves one block at a time and can see an already resolved neighbor.
        level.setBlock(left, Block.updateFromNeighbourShapes(pending, level, left), flags);
        level.setBlock(right, Block.updateFromNeighbourShapes(pending, level, right), flags);
        var leftState = level.getBlockState(left);
        var rightState = level.getBlockState(right);
        helper.assertTrue(leftState.getValue(ChestBlock.TYPE) == ChestType.LEFT && rightState.getValue(ChestBlock.TYPE) == ChestType.RIGHT
                        && !leftState.getValue(LeechChestBlock.LEGACY_PAIRING) && !rightState.getValue(LeechChestBlock.LEGACY_PAIRING),
                "An original pair resolves into complementary vanilla chest halves");
        var joined = ChestBlock.getContainer(chest, rightState, level, right, true);
        helper.assertTrue(joined != null && joined.getContainerSize() == 54 && joined.getItem(7).is(Items.GOLD_INGOT)
                        && joined.getItem(32).is(Items.DIAMOND), "The joined container exposes both original inventories");

        level.setBlockAndUpdate(left, Blocks.AIR.defaultBlockState());
        helper.assertTrue(level.getBlockState(right).getValue(ChestBlock.TYPE) == ChestType.SINGLE
                        && ((LeechChestBlockEntity) level.getBlockEntity(right)).getItem(7).is(Items.GOLD_INGOT),
                "Removing one half keeps the other's contents and returns it to a single chest");
        level.setBlock(left, pending, flags);
        var single = Block.updateFromNeighbourShapes(pending, level, left);
        helper.assertTrue(single.getValue(ChestBlock.TYPE) == ChestType.SINGLE && !single.getValue(LeechChestBlock.LEGACY_PAIRING),
                "An unpaired original chest leaves a neighboring single chest independent");
        helper.succeed();
    }
}
