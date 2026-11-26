package art.arcane.witchery.brewing;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.function.Consumer;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class BrewTreeTests {
    @GameTest(template = "qa_coven_arena")
    public static void treeBreakChecksUseTheRootAndOriginalTerrain(GameTestHelper helper) {
        var level = helper.getLevel();
        var root = helper.absolutePos(new BlockPos(6, 5, 6));
        var caster = FakePlayerFactory.getMinecraft(level);
        var leaves = WitcheryBlocks.getRequired("witchleaves").get();
        var log = WitcheryBlocks.getRequired("witchlog").get();
        for (BlockPos pos : BlockPos.betweenClosed(root.offset(-4, 0, -4), root.offset(4, 10, 4))) {
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        }
        level.setBlockAndUpdate(root.below(), Blocks.DIRT.defaultBlockState());

        Consumer<BlockEvent.BreakEvent> protectRoot = event -> {
            if (event.getLevel() == level && event.getPos().equals(root) && event.getPlayer() == caster) {
                event.setCanceled(true);
            }
        };
        MinecraftForge.EVENT_BUS.addListener(protectRoot);
        try {
            BrewTrees.grow(level, root.below(), Direction.UP, BrewTrees.Species.ROWAN, 1, caster);
            helper.assertTrue(level.isEmptyBlock(root), "A protected air cell rejects the tree before growth");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(protectRoot);
        }

        Consumer<BlockEvent.BreakEvent> protectLeaves = event -> {
            if (event.getLevel() == level && event.getPlayer() == caster && event.getState().is(leaves)
                    && event.getPos().closerThan(root, 12)) {
                event.setCanceled(true);
            }
        };
        MinecraftForge.EVENT_BUS.addListener(protectLeaves);
        try {
            BrewTrees.grow(level, root.below(), Direction.UP, BrewTrees.Species.ROWAN, 1, caster);
            helper.assertTrue(level.getBlockState(root).is(log), "Tree growth checks the original terrain before placement");
            helper.assertTrue(level.getBlockState(root.above(4).east()).is(leaves),
                    "The original Rowan has leaves beside its upper trunk");
            helper.succeed();
        } finally {
            MinecraftForge.EVENT_BUS.unregister(protectLeaves);
        }
    }
}
