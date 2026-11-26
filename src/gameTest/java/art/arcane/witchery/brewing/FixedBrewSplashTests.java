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
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.Arrays;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class FixedBrewSplashTests {
    private static final BlockPos CENTER = new BlockPos(5, 4, 5);
    private static final String BATCH = "witchery_fixed_brew_splashes";

    @GameTest(template = "qa_coven_arena", batch = BATCH)
    public static void inkUsesTheOriginalRadiusAndFalloff(GameTestHelper helper) {
        Vec3 center = center(helper);
        var direct = stationary(helper, EntityType.PIG, center.add(0, 0, 1.5));
        var glancing = stationary(helper, EntityType.ZOMBIE, center.add(0, 0, -3));
        var boundary = stationary(helper, EntityType.PIG, center.add(0, 0, 4));

        checkAfterSpawning(helper, () -> {
            glancing.setTarget(direct);
            splash(helper, WitcheryIngredient.BREW_OF_INK, center, new EntityHitResult(direct));
            assertDuration(helper, direct, MobEffects.BLINDNESS, 400);
            assertDuration(helper, glancing, MobEffects.BLINDNESS, 100);
            helper.assertTrue(glancing.getTarget() == null, "Ink clears the glancing mob's attack target");
            helper.assertTrue(!boundary.hasEffect(MobEffects.BLINDNESS), "The four-block boundary is excluded");

            splash(helper, WitcheryIngredient.BREW_OF_INK, center, new EntityHitResult(boundary));
            helper.assertTrue(!boundary.hasEffect(MobEffects.BLINDNESS), "A direct hit still needs to be inside the sphere");
            BrewImpacts.mineInk(helper.getLevel(), BlockPos.containing(center), boundary);
            helper.assertTrue(!boundary.hasEffect(MobEffects.BLINDNESS), "Ink mines also respect the outer radius");
        }, direct, glancing, boundary);
    }

    @GameTest(template = "qa_coven_arena", batch = BATCH)
    public static void loveResetsOnlyAdultAnimalsInTheOriginalSphere(GameTestHelper helper) {
        Vec3 center = center(helper);
        var adult = stationary(helper, EntityType.PIG, center.add(0, 0, -3.5));
        var boundary = stationary(helper, EntityType.PIG, center.add(0, 0, 4));
        var baby = stationary(helper, EntityType.PIG, center.add(1, 0, 0));

        checkAfterSpawning(helper, () -> {
            adult.setAge(3000);
            boundary.setAge(3000);
            baby.setAge(-400);
            Vec3 contact = center.add(0, 0, 1.5);
            splash(helper, WitcheryIngredient.BREW_OF_LOVE, center,
                    new BlockHitResult(contact, Direction.UP, BlockPos.containing(contact).below(), false));
            helper.assertTrue(adult.getAge() == 0 && adult.isInLove(), "The glancing adult's cooldown is reset");
            helper.assertTrue(boundary.getAge() == 3000 && !boundary.isInLove(), "The boundary adult keeps its cooldown");
            helper.assertTrue(baby.getAge() == -400 && !baby.isInLove(), "Young animals stay young");
        }, adult, boundary, baby);
    }

    @GameTest(template = "qa_coven_arena", batch = BATCH)
    public static void revealingIncludesTheUpperEdgeOfTheOriginalSphere(GameTestHelper helper) {
        Vec3 center = center(helper);
        var upperEdge = stationary(helper, EntityType.PIG, center.add(0, 5, 0));
        var outside = stationary(helper, EntityType.PIG, center.add(0, 0, 5.5));

        checkAfterSpawning(helper, () -> {
            upperEdge.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 120));
            outside.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 120));
            Vec3 contact = center.add(0, 0, 1.5);
            splash(helper, WitcheryIngredient.BREW_OF_REVEALING, center,
                    new BlockHitResult(contact, Direction.UP, BlockPos.containing(contact).below(), false));
            helper.assertTrue(!upperEdge.hasEffect(MobEffects.INVISIBILITY), "The full-height sphere includes its five-block edge");
            assertDuration(helper, outside, MobEffects.INVISIBILITY, 120);
        }, upperEdge, outside);
    }

    @GameTest(template = "qa_coven_arena", batch = BATCH)
    public static void wastingKeepsItsOriginalBoundsAndMinimumPoison(GameTestHelper helper) {
        BlockPos struck = helper.absolutePos(CENTER);
        Vec3 corner = Vec3.atLowerCornerOf(struck);
        Vec3 center = corner.add(0.5, 0, -1);
        var glancing = stationary(helper, EntityType.PIG, corner.add(2, 0, 0));
        var fringe = stationary(helper, EntityType.PIG, corner.add(0, 0, -3.999));
        var above = stationary(helper, EntityType.PIG, corner.add(1, 2.5, 0));

        checkAfterSpawning(helper, () -> {
            splash(helper, WitcheryIngredient.BREW_OF_WASTING, center,
                    new BlockHitResult(corner.add(0.5, 0.8, 0), Direction.NORTH, struck, false));
            assertDuration(helper, glancing, MobEffects.WITHER, 400);
            assertDuration(helper, glancing, MobEffects.POISON, 66);
            assertDuration(helper, fringe, MobEffects.WITHER, 0);
            assertDuration(helper, fringe, MobEffects.POISON, 40);
            helper.assertTrue(!above.hasEffect(MobEffects.POISON), "The higher collision point does not raise the search bounds");
        }, glancing, fringe, above);
    }

    private static Vec3 center(GameTestHelper helper) {
        return Vec3.atBottomCenterOf(helper.absolutePos(CENTER));
    }

    private static <T extends Mob> T stationary(GameTestHelper helper, EntityType<T> type, Vec3 pos) {
        T mob = helper.spawnWithNoFreeWill(type, CENTER);
        mob.setNoGravity(true);
        mob.setPos(pos);
        return mob;
    }

    private static void checkAfterSpawning(GameTestHelper helper, Runnable check, Entity... targets) {
        var level = helper.getLevel();
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(Arrays.stream(targets).allMatch(target -> level.getEntity(target.getUUID()) == target),
                        "All targets are tracked by the level"))
                .thenExecute(() -> {
                    try {
                        check.run();
                    } finally {
                        Arrays.stream(targets).forEach(Entity::discard);
                    }
                }).thenSucceed();
    }

    private static void splash(GameTestHelper helper, WitcheryIngredient ingredient, Vec3 center, HitResult hit) {
        var brew = new ThrownBrew(WitcheryEntities.THROWN_BREW.get(), helper.getLevel());
        var item = (ThrowableBrewItem) ingredient.item();
        brew.setItem(item.getDefaultInstance());
        brew.setPos(center);
        item.impact(brew, hit);
    }

    private static void assertDuration(GameTestHelper helper, LivingEntity target, MobEffect type, int ticks) {
        var effect = target.getEffect(type);
        helper.assertTrue(effect != null && effect.getDuration() == ticks,
                "Expected " + ticks + " ticks of " + type.getDescriptionId() + ", got " + effect);
    }
}
