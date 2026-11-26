package art.arcane.witchery.brewing;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.ThrownBrew;
import art.arcane.witchery.item.ThrowableBrewItem;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class BrewSproutingTests {
    private static final BlockPos SOURCE = new BlockPos(5, 5, 5);
    private static final String BATCH = "witchery_brew_sprouting";

    @GameTest(template = "qa_coven_arena", batch = BATCH)
    public static void fixedSproutingLiftsFromTheProjectileBoundsToTheOriginalHeight(GameTestHelper helper) {
        var level = helper.getLevel();
        var struck = helper.absolutePos(SOURCE);
        var corner = Vec3.atLowerCornerOf(struck);
        var included = pig(helper);
        var outside = pig(helper);
        var outsidePosition = corner.add(1.05, 1, 0.75);

        helper.setBlock(SOURCE, Blocks.BIRCH_LOG);
        for (int offset = 1; offset <= 18; offset++) level.removeBlock(struck.above(offset), false);
        helper.startSequence().thenWaitUntil(() -> {
            assertLoaded(helper, included);
            assertLoaded(helper, outside);
        }).thenExecute(() -> {
            included.setPos(corner.add(-0.15, 1, 0.20));
            outside.setPos(outsidePosition);
            cast(helper, struck, Direction.UP, corner.add(0.20, 1.25, 0.20), corner.add(0.75, 1, 0.75));

            for (int offset = 1; offset < 15; offset++) {
                helper.assertTrue(level.getBlockState(struck.above(offset)).is(Blocks.BIRCH_LOG),
                        "Sprouting produced a vertical birch log at offset " + offset);
            }
            helper.assertTrue(level.isEmptyBlock(struck.above(15)), "An ordinary fixed branch grows fourteen logs");
            helper.assertTrue(included.position().equals(corner.add(0.5, 16, 0.5)),
                    "The projectile-side pig lands two blocks above the final log");
            helper.assertTrue(outside.position().equals(outsidePosition), "The collision-side pig stays where it started");
            included.discard();
            outside.discard();
        }).thenSucceed();
    }

    @GameTest(template = "qa_coven_arena", batch = BATCH)
    public static void fixedSproutingUsesTwoClearCellsEvenWhenTheFirstLogIsBlocked(GameTestHelper helper) {
        var level = helper.getLevel();
        var struck = helper.absolutePos(SOURCE);
        var center = Vec3.atBottomCenterOf(struck);
        var target = pig(helper);
        helper.setBlock(SOURCE, Blocks.BIRCH_LOG);
        helper.setBlock(SOURCE.above(), Blocks.STONE);
        helper.setBlock(SOURCE.above(2), Blocks.AIR);
        helper.setBlock(SOURCE.above(3), Blocks.STONE);

        helper.startSequence().thenWaitUntil(() -> assertLoaded(helper, target)).thenExecute(() -> {
            Vec3 initial = center.add(0, 0.25, 0);
            target.setPos(initial);
            cast(helper, struck, Direction.UP, center.add(0, 1, 0), center.add(0, 1, 0));
            helper.assertTrue(target.position().equals(initial), "A blocked second headroom cell prevents the lift");

            level.removeBlock(struck.above(3), false);
            cast(helper, struck, Direction.UP, center.add(0, 1, 0), center.add(0, 1, 0));
            helper.assertTrue(target.position().equals(center.add(0, 2, 0)), "The creature is lifted just above the obstruction");
            helper.assertBlockPresent(Blocks.STONE, SOURCE.above());
            target.discard();
        }).thenSucceed();
    }

    @GameTest(template = "qa_coven_arena", batch = BATCH)
    public static void sproutingPreservesTheLegacyAcaciaPlankAndCauldronSpeciesRules(GameTestHelper helper) {
        var level = helper.getLevel();
        var struck = helper.absolutePos(SOURCE);
        var center = Vec3.atCenterOf(struck);
        var target = pig(helper);
        helper.setBlock(SOURCE, Blocks.ACACIA_PLANKS);
        helper.setBlock(SOURCE.east(4), Blocks.STONE);

        helper.startSequence().thenWaitUntil(() -> assertLoaded(helper, target)).thenExecute(() -> {
            cast(helper, struck, Direction.EAST, center.add(0.75, 0, 0), center.add(0.5, 0, 0));
            for (int offset = 1; offset < 4; offset++) {
                var state = level.getBlockState(struck.east(offset));
                helper.assertTrue(state.is(Blocks.OAK_LOG) && state.getValue(RotatedPillarBlock.AXIS) == Direction.Axis.X,
                        "Acacia planks select the original oak branch on the X axis");
            }

            level.setBlockAndUpdate(struck, Blocks.BIRCH_LOG.defaultBlockState());
            for (int offset = 1; offset <= 7; offset++) level.removeBlock(struck.above(offset), false);
            target.setPos(Vec3.atBottomCenterOf(struck.above()));
            BrewTerrain.sprout(level, struck, Direction.UP, 2, target.getBoundingBox().inflate(0.5), null);
            helper.assertBlockPresent(Blocks.BIRCH_LOG, SOURCE.above());
            helper.assertBlockPresent(Blocks.BIRCH_LOG, SOURCE.above(2));
            helper.assertBlockPresent(Blocks.AIR, SOURCE.above(3));
            helper.assertTrue(target.position().equals(Vec3.atBottomCenterOf(struck.above(5))),
                    "Cauldron Sprouting keeps its separate growth extent and landing");
            target.discard();
        }).thenSucceed();
    }

    private static Pig pig(GameTestHelper helper) {
        var pig = helper.spawnWithNoFreeWill(EntityType.PIG, SOURCE.north());
        pig.setNoGravity(true);
        return pig;
    }

    private static void assertLoaded(GameTestHelper helper, Pig pig) {
        helper.assertTrue(helper.getLevel().getEntity(pig.getUUID()) == pig, "The target pig is tracked by the level");
    }

    private static void cast(GameTestHelper helper, BlockPos struck, Direction face, Vec3 origin, Vec3 hit) {
        var brew = new ThrownBrew(WitcheryEntities.THROWN_BREW.get(), helper.getLevel());
        var item = (ThrowableBrewItem) WitcheryIngredient.BREW_OF_SPROUTING.item();
        brew.setItem(item.getDefaultInstance());
        brew.setPos(origin);
        item.impact(brew, new BlockHitResult(hit, face, struck, false));
    }
}
