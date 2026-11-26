package art.arcane.witchery.brewing;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.item.BiomeNoteItem;
import art.arcane.witchery.item.BrewBottleItem;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcheryItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class SplashImpactPlanTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_splash_impact_plan")
    public static void originalSplashExtentAndGlancingScale(GameTestHelper helper) {
        inTickingChunk(helper, center -> {
            var level = helper.getLevel();
            Vec3 origin = Vec3.atBottomCenterOf(center);
            var direct = pig(level, origin.add(5, 0, 0));
            var glancing = pig(level, origin.add(0, 0, 3));
            var edge = pig(level, origin.add(-6, 0, 0));
            var outside = pig(level, origin.add(-6.125, 0, 0));
            var ingredients = List.of(new ItemStack(Items.NETHER_WART), new ItemStack(Items.GLOWSTONE_DUST),
                    new ItemStack(Items.REDSTONE), new ItemStack(Items.SUGAR), new ItemStack(WitcheryIngredient.WOOD_ASH.item()),
                    new ItemStack(Items.COCOA_BEANS), new ItemStack(WitcheryBlocks.getRequired("somniancotton").get()),
                    new ItemStack(Items.GUNPOWDER));
            try {
                impact(level, ingredients, origin, new EntityHitResult(direct));
                assertSpeed(helper, direct, 3600, 1);
                assertSpeed(helper, glancing, 1800, 1);
                assertSpeed(helper, edge, 0, 1);
                assertSpeed(helper, outside, -1, -1);
            } finally {
                List.of(direct, glancing, edge, outside).forEach(Pig::discard);
            }
        });
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_splash_impact_plan")
    public static void stealBuffsStopsAtTheFirstDonorAtTheSplashBoundary(GameTestHelper helper) {
        inTickingChunk(helper, center -> {
            var level = helper.getLevel();
            var origin = Vec3.atBottomCenterOf(center);
            var recipe = List.of(new ItemStack(WitcheryIngredient.TEAR_OF_THE_GODDESS.item()), new ItemStack(Items.NETHER_STAR),
                    new ItemStack(Items.SKELETON_SKULL), new ItemStack(Items.GUNPOWDER));
            record Trial(double distance, boolean weakerFirstBuff, boolean firstHasRegeneration, boolean secondKeepsRegeneration) {}
            for (var trial : List.of(new Trial(3, false, false, true), new Trial(3, true, false, true),
                    new Trial(2.25, false, false, false), new Trial(3, false, true, true))) {
                var target = pig(level, origin.add(trial.distance(), 0, 0));
                var first = pig(level, origin.add(5, 0, 0));
                var second = pig(level, origin.add(6.5, 0, 0));
                try {
                    if (trial.weakerFirstBuff()) {
                        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 600, 1));
                        first.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200));
                    }
                    if (trial.firstHasRegeneration()) first.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 400));
                    second.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200));
                    impact(level, recipe, origin, new BlockHitResult(origin, Direction.UP, center.below(), false));
                    int expectedTicks = trial.firstHasRegeneration() ? 400 : trial.secondKeepsRegeneration() ? 0 : 200;
                    var stolen = target.getEffect(MobEffects.REGENERATION);
                    helper.assertTrue((expectedTicks == 0 ? stolen == null : stolen != null && stolen.getDuration() == expectedTicks)
                                    && !first.hasEffect(MobEffects.REGENERATION)
                                    && second.hasEffect(MobEffects.REGENERATION) == trial.secondKeepsRegeneration(),
                            "The splash exposure controls how far the original donor scan proceeds: " + trial);
                    if (trial.weakerFirstBuff()) {
                        assertSpeed(helper, target, 600, 1);
                        assertSpeed(helper, first, 200, 0);
                    }
                } finally {
                    List.of(target, first, second).forEach(Pig::discard);
                }
            }
        });
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_splash_impact_plan")
    public static void biomeNotePreparesImpactBeforeEffectsSpendCapacity(GameTestHelper helper) {
        inTickingChunk(helper, center -> {
            var level = helper.getLevel();
            var wart = new ItemStack(Items.NETHER_WART);
            var sugar = new ItemStack(Items.SUGAR);
            var note = WitcheryItems.getRequired("biomenote").get().getDefaultInstance();
            note.getOrCreateTag().putString(BiomeNoteItem.BIOME, "minecraft:desert");
            helper.assertTrue(!BrewMixture.canAdd(List.of(wart), note), "Fresh mixing still needs room for the Biome Note action");
            for (boolean gas : List.of(true, false)) {
                var dispersal = new ItemStack((gas ? WitcheryIngredient.BAT_WOOL : WitcheryIngredient.WORMWOOD).item());
                var fluid = WitcheryBlocks.getRequired(gas ? "brewgas" : "brewliquid").get();
                for (boolean seasons : List.of(false, true)) {
                    var pos = center.offset(seasons ? 4 : -4, 0, gas ? -4 : 4);
                    level.setBlockAndUpdate(pos.below(), Blocks.STONE.defaultBlockState());
                    level.removeBlock(pos, false);
                    var origin = Vec3.atBottomCenterOf(pos);
                    var target = pig(level, origin);
                    var biome = level.getBiome(pos);
                    var recipe = seasons ? List.of(wart, note, sugar, dispersal) : List.of(wart, sugar, dispersal);
                    try {
                        impact(level, recipe, origin, new BlockHitResult(origin, Direction.UP, pos.below(), false));
                        helper.assertTrue(seasons ? level.isEmptyBlock(pos) : level.getBlockState(pos).is(fluid),
                                "Only the Biome Note replaces the selected dispersal with an instant splash");
                        helper.assertTrue(biome.equals(level.getBiome(pos)), "The batch lacks the capacity to change the biome");
                        assertSpeed(helper, target, seasons ? 1800 : -1, seasons ? 0 : -1);
                    } finally {
                        target.discard();
                        level.removeBlock(pos, false);
                    }
                }
            }

            for (boolean seasons : List.of(false, true)) {
                var tongue = new ItemStack(WitcheryIngredient.DOG_TONGUE.item());
                var wool = new ItemStack(WitcheryIngredient.BAT_WOOL.item());
                var recipe = seasons ? List.of(wart, note, sugar, wool, tongue) : List.of(wart, sugar, wool, tongue);
                var mixture = BrewMixture.from(recipe);
                level.setBlockAndUpdate(center.below(), Blocks.STONE.defaultBlockState());
                var target = pig(level, Vec3.atBottomCenterOf(center));
                try {
                    var status = BrewRituals.pulse(level, center.below(), mixture, mixture.bottleTag(recipe), tongue, 1, false);
                    helper.assertTrue(status == (seasons ? BrewRituals.Result.COMPLETE : BrewRituals.Result.ONGOING),
                            "The Biome Note selects the single instant pulse before doing effect-capacity checks");
                    assertSpeed(helper, target, seasons ? -1 : 3600, seasons ? -1 : 0);
                } finally {
                    target.discard();
                }
            }
        });
    }

    private static void inTickingChunk(GameTestHelper helper, java.util.function.Consumer<BlockPos> test) {
        var level = helper.getLevel();
        var chunk = new ChunkPos(helper.absolutePos(new BlockPos(5, 4, 5)));
        var center = new BlockPos(chunk.getMiddleBlockX(), 196, chunk.getMiddleBlockZ());
        boolean forced = level.setChunkForced(chunk.x, chunk.z, true);
        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(level.isPositionEntityTicking(center),
                "The splash targets must be in an entity-ticking chunk")).thenExecute(() -> {
            try {
                test.accept(center);
            } finally {
                if (forced) level.setChunkForced(chunk.x, chunk.z, false);
            }
        }).thenSucceed();
    }

    private static Pig pig(ServerLevel level, Vec3 position) {
        var pig = EntityType.PIG.create(level);
        pig.setPos(position);
        pig.setNoAi(true);
        pig.setNoGravity(true);
        level.addFreshEntity(pig);
        return pig;
    }

    private static void impact(ServerLevel level, List<ItemStack> recipe, Vec3 origin, HitResult hit) {
        var bottle = WitcheryItems.getRequired("brewbottle").get().getDefaultInstance();
        var tag = BrewMixture.ingredientTag(recipe, 0x442233);
        tag.putBoolean("Splash", true);
        bottle.setTag(tag);
        var shot = WitcheryEntities.THROWN_BREW.get().create(level);
        shot.setItem(bottle);
        shot.setPos(origin);
        ((BrewBottleItem) bottle.getItem()).impact(level, shot, hit);
    }

    private static void assertSpeed(GameTestHelper helper, Pig pig, int duration, int amplifier) {
        var speed = pig.getEffect(MobEffects.MOVEMENT_SPEED);
        helper.assertTrue(duration < 0 ? speed == null
                        : speed != null && speed.getDuration() == duration && speed.getAmplifier() == amplifier,
                "Expected Speed duration " + duration + " and amplifier " + amplifier + ", got " + speed);
    }
}
