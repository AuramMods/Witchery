package art.arcane.witchery.entity;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class VampireHuntTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_vampire_hunt_landings", timeoutTicks = 120)
    public static void huntSearchesTheOriginalColumnAboveAndBelow(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos column = helper.absolutePos(new BlockPos(8, 0, 8)).atY(205);
        BlockPos upper = column.above(3);
        BlockPos lower = column.below(3);
        BlockPos neighbor = column.east();
        level.setBlockAndUpdate(upper, Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(lower, Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(neighbor, Blocks.STONE.defaultBlockState());

        var vampire = WitcheryEntities.VAMPIRE.get().create(level);
        vampire.setNoAi(true);
        vampire.setNoGravity(true);
        vampire.setPos(Vec3.atBottomCenterOf(column.south(3)));
        level.addFreshEntity(vampire);
        helper.startSequence().thenWaitUntil(() -> {
            helper.assertTrue(level.getEntity(vampire.getUUID()) == vampire, "The vampire is tracked");
        }).thenExecute(() -> {
            try {
                helper.assertTrue(vampire.relocate(level, column)
                                && vampire.position().distanceToSqr(Vec3.atBottomCenterOf(upper.above()).add(0, 0.05, 0)) < 1e-8,
                        "The upper surface wins when both sides are equally distant");
                level.setBlockAndUpdate(upper.above(2), Blocks.GLASS.defaultBlockState());
                helper.assertTrue(vampire.relocate(level, column)
                                && vampire.position().distanceToSqr(Vec3.atBottomCenterOf(lower.above()).add(0, 0.05, 0)) < 1e-8,
                        "Blocked headroom makes the vampire use the lower surface");
                level.removeBlock(lower, false);
                Vec3 origin = vampire.position();
                helper.assertTrue(!vampire.relocate(level, column) && vampire.position().equals(origin),
                        "A clear neighboring column cannot substitute for the requested column");
            } finally {
                vampire.discard();
                level.removeBlock(upper, false);
                level.removeBlock(upper.above(2), false);
                level.removeBlock(lower, false);
                level.removeBlock(neighbor, false);
            }
        }).thenSucceed();
    }
}
