package art.arcane.witchery.ritual;

import art.arcane.witchery.Witchery;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class VolcanoTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_volcano_layers")
    public static void legacyVolcanoLiftsOnlyEntitiesIntersectingTheGrowingLayer(GameTestHelper helper) {
        var level = helper.getLevel();
        var local = new BlockPos(12, 4, 5);
        var center = helper.absolutePos(local);
        var inside = helper.spawnWithNoFreeWill(EntityType.PIG, local);
        var outside = helper.spawnWithNoFreeWill(EntityType.PIG, local);
        var under = helper.spawnWithNoFreeWill(EntityType.PIG, local);
        inside.setPos(center.getX() + 1.15, center.getY() + 0.50, center.getZ() + 0.15);
        outside.setPos(center.getX() + 2.15, center.getY() + 0.50, center.getZ() + 0.15);
        under.setPos(center.getX() + 1.15, center.getY() + 0.05, center.getZ() + 0.15);
        var progress = new LandscapeRites.Progress(ItemRiteRecipe.Action.RAISE_PORTABLE_VOLCANO,
                null, center, 1, null, false, 0);

        level.getRandom().setSeed(29);
        LandscapeRites.pulse(level, center, progress, 0, null, Runnable::run, true);
        helper.assertTrue(inside.getY() == center.getY() + 1.50
                        && outside.getY() == center.getY() + 0.50 && under.getY() == center.getY() + 0.05,
                "Expected only the first pig to rise one block above " + center + ", got "
                        + inside.getY() + ", " + outside.getY() + ", " + under.getY());
        inside.discard();
        outside.discard();
        under.discard();
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_volcano_outlet")
    public static void legacyVolcanoOpensTheEastVentOnAZeroRoll(GameTestHelper helper) {
        var level = helper.getLevel();
        var local = new BlockPos(12, 2, 5);
        var center = helper.absolutePos(local);
        var summit = local.above(7);
        helper.setBlock(summit.east(), Blocks.STONE);
        helper.setBlock(summit.south(), Blocks.STONE);
        helper.setBlock(summit.west(), Blocks.STONE);
        helper.setBlock(summit.north(), Blocks.STONE);
        var progress = new LandscapeRites.Progress(ItemRiteRecipe.Action.RAISE_VOLCANO,
                null, center, 14, null, false, 0);

        level.getRandom().setSeed(4096);
        LandscapeRites.pulse(level, center, progress, 0, null, Runnable::run, true);
        helper.assertBlockPresent(Blocks.LAVA, summit);
        helper.assertBlockPresent(Blocks.AIR, summit.east());
        helper.assertBlockPresent(Blocks.STONE, summit.north());
        helper.succeed();
    }
}
