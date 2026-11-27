package art.arcane.witchery.block;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.entity.PlacedItemBlockEntity;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class LegacyPickBlockTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_legacy_pick_block")
    public static void technicalBlockPicksAreEmpty(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = FakePlayerFactory.getMinecraft(level);
        var target = helper.absolutePos(new BlockPos(5, 4, 5));
        for (var pos : BlockPos.betweenClosed(target.north(2), target.south(2))) {
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        }
        var center = Vec3.atCenterOf(target);
        var ray = new ClipContext(center.add(0, 0, 1.5), center.add(0, 0, -1.5),
                ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player);
        for (String path : List.of("force", "tormentstone", "spiritportal", "tormentportal", "brewgas", "brewliquid")) {
            var block = WitcheryBlocks.getRequired(path).get();
            level.setBlock(target, block.defaultBlockState(), Block.UPDATE_CLIENTS);
            var state = level.getBlockState(target);
            helper.assertTrue(state.is(block) && block.asItem() != Items.AIR, path + " keeps its registered block and item");
            var hit = level.clip(ray);
            if (block instanceof BrewDispersalBlock) {
                helper.assertTrue(hit.getType() == HitResult.Type.MISS, path + " stays outside normal block targeting");
            } else {
                helper.assertTrue(hit.getType() == HitResult.Type.BLOCK && hit.getBlockPos().equals(target),
                        path + " retains its targeting outline");
            }
            helper.assertTrue(state.getCloneItemStack(hit, level, target, player).isEmpty(),
                    "The Forge pick hook returns no item for " + path);
            if (path.equals("force")) {
                helper.assertTrue(Block.isShapeFullBlock(state.getCollisionShape(level, target))
                        && state.getOcclusionShape(level, target).isEmpty(), "Force retains solid collision and empty occlusion");
                helper.assertTrue(state.getLightBlock(level, target) == 0, "Force leaves the light level unchanged");
            }
            if (path.equals("tormentstone")) {
                helper.assertTrue(Block.isShapeFullBlock(state.getCollisionShape(level, target))
                        && state.isSolidRender(level, target), "Torment Stone has full collision and renders as opaque stone");
                helper.assertTrue(state.getLightBlock(level, target) == 0, "Torment Stone keeps its original light opacity");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_legacy_pick_block")
    public static void mountedItemPicksKeepDataAndTheOriginalFallback(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = FakePlayerFactory.getMinecraft(level);
        var pos = helper.absolutePos(new BlockPos(5, 4, 5));
        var block = WitcheryBlocks.getRequired("placeditem").get();
        level.setBlockAndUpdate(pos.below(), Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(pos.above(), Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(pos, block.defaultBlockState());
        var center = Vec3.atBottomCenterOf(pos);
        var hit = level.clip(new ClipContext(center.add(0, 1.5, 0), center.add(0, 0.001, 0),
                ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        helper.assertTrue(hit.getType() == HitResult.Type.BLOCK && hit.getBlockPos().equals(pos), "Target the mounted item");
        var state = level.getBlockState(pos);
        helper.assertTrue(state.getCloneItemStack(hit, level, pos, player).is(WitcheryItems.getRequired("arthana").get()),
                "An empty legacy mount picks an Arthana");
        var placed = (PlacedItemBlockEntity) level.getBlockEntity(pos);
        var stored = new ItemStack(Items.DIAMOND_SWORD).setHoverName(Component.literal("Placed keepsake"));
        stored.setDamageValue(17);
        stored.getOrCreateTag().putBoolean("Keepsake", true);
        placed.place(stored);
        var picked = state.getCloneItemStack(hit, level, pos, player);
        helper.assertTrue(ItemStack.matches(stored, picked), "A populated mount preserves the exact item, wear and tag");
        picked.setDamageValue(42);
        helper.assertTrue(placed.stack().getDamageValue() == 17, "The picked stack is independent of the stored item");
        helper.succeed();
    }
}
