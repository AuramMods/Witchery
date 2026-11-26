package art.arcane.witchery.brewing;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.ThrownBrew;
import art.arcane.witchery.item.ThrowableBrewItem;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class BrewSolidificationTests {
    private static final BlockPos SOURCE = new BlockPos(5, 5, 7);

    @GameTest(template = "qa_coven_arena", batch = "witchery_brew_break_cues")
    public static void solidificationOnlyStartsAtNeighboringTears(GameTestHelper helper) {
        var tears = WitcheryBlocks.getRequired("hollowtears").get();
        helper.setBlock(SOURCE, tears);
        helper.setBlock(SOURCE.east(3), tears);

        cast(helper, SOURCE, WitcheryIngredient.BREW_OF_SOLID_DIRT);
        helper.assertBlockPresent(tears, SOURCE);
        helper.assertTrue(helper.getLevel().getEntitiesOfClass(ItemEntity.class,
                new AABB(helper.absolutePos(SOURCE)).inflate(2)).isEmpty(),
                "A valid isolated impact spends the brew");

        helper.setBlock(SOURCE.east(), tears);
        cast(helper, SOURCE, WitcheryIngredient.BREW_OF_SOLID_DIRT);
        helper.assertBlockPresent(Blocks.DIRT, SOURCE);
        helper.assertBlockPresent(Blocks.DIRT, SOURCE.east());
        helper.assertBlockPresent(tears, SOURCE.east(3));
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_brew_break_cues")
    public static void erosionStartsAboveTheAnchorAndSpendsItsTearsSupport(GameTestHelper helper) {
        var tears = WitcheryBlocks.getRequired("hollowtears").get();
        helper.setBlock(SOURCE, tears);
        helper.setBlock(SOURCE.above(), tears);
        helper.setBlock(SOURCE.below(), Blocks.STONE);

        cast(helper, SOURCE, WitcheryIngredient.BREW_OF_SOLID_EROSION);
        helper.assertBlockPresent(Blocks.AIR, SOURCE.above());
        helper.assertBlockPresent(Blocks.AIR, SOURCE);
        helper.assertBlockPresent(Blocks.STONE, SOURCE.below());
        helper.succeed();
    }

    private static void cast(GameTestHelper helper, BlockPos relativePos, WitcheryIngredient brew) {
        var level = helper.getLevel();
        var impact = helper.absolutePos(relativePos);
        var center = Vec3.atCenterOf(impact);
        var item = (ThrowableBrewItem) brew.item();
        var projectile = new ThrownBrew(WitcheryEntities.THROWN_BREW.get(), level);
        projectile.setItem(item.getDefaultInstance());
        projectile.setPos(center.add(0, 0, -1));
        item.impact(projectile, new BlockHitResult(center.add(0, 0, -0.5), Direction.NORTH, impact, false));
    }
}
