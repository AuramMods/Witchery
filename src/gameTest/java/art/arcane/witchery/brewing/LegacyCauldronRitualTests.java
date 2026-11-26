package art.arcane.witchery.brewing;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.entity.AltarBlockEntity;
import art.arcane.witchery.block.entity.BrewFluidBlockEntity;
import art.arcane.witchery.block.entity.WitchesCauldronBlockEntity;
import art.arcane.witchery.capability.WitcheryPlayerDataProvider;
import art.arcane.witchery.entity.BrewDroplet;
import art.arcane.witchery.item.TaglockItem;
import art.arcane.witchery.item.WaystoneItem;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryEffects;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcheryItems;
import art.arcane.witchery.registry.WitcheryRecipes;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class LegacyCauldronRitualTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_cauldron_local_clock")
    public static void brewRitualWaitsForTheNextLocalAltarSample(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(5, 4, 5));
        var cauldron = placeCauldron(helper, pos, originalRitual(3000, 0, true,
                oldItem("minecraft:nether_wart", 0, 1), oldItem("minecraft:sugar", 0, 1),
                oldItem("minecraft:gunpowder", 0, 1), oldItem("witchery:dog_tongue", 0, 1)));
        var altar = formAltar(helper, pos.east(5));

        tickCauldron(helper, cauldron, 7);
        var charged = altar.saveWithoutMetadata();
        charged.putFloat("Power", 10_000);
        altar.load(charged);
        tickCauldron(helper, cauldron, 19);
        var waiting = cauldron.saveWithoutMetadata();
        helper.assertTrue(!waiting.getBoolean("Powered") && waiting.getInt("RitualTicks") == 0,
                "A charge added after tick 7 leaves both intervening ritual pulses waiting");

        tickCauldron(helper, cauldron, 1);
        var sampled = cauldron.saveWithoutMetadata();
        helper.assertTrue(sampled.getBoolean("Powered") && sampled.getInt("RitualTicks") == 0,
                "Tick 27 saves the restored altar connection before the next ritual pulse");
        cauldron.load(sampled);
        tickCauldron(helper, cauldron, 10);
        helper.assertTrue(cauldron.saveWithoutMetadata().getInt("RitualTicks") == 1 && altar.power() == 10_000,
                "The same local clock advances once between ticks 28 and 37 without spending charge");
        tickCauldron(helper, cauldron, 1);
        helper.assertTrue(cauldron.saveWithoutMetadata().getInt("RitualTicks") == 2,
                "The next loaded pulse advances on tick 38");

        tickCauldron(helper, cauldron, 10);
        var warmed = cauldron.saveWithoutMetadata();
        warmed.putInt("RitualTicks", 20);
        cauldron.load(warmed);
        charged.putFloat("Power", 0);
        altar.load(charged);
        tickCauldron(helper, cauldron, 10);
        helper.assertTrue(cauldron.saveWithoutMetadata().getInt("RitualTicks") == 21
                        && tank(cauldron).getFluidInTank(0).getAmount() == 3000,
                "The first failed altar debit keeps the warmed brew for another pulse");
        tickCauldron(helper, cauldron, 10);
        helper.assertTrue(tank(cauldron).getFluidInTank(0).isEmpty(),
                "The later interruption clears the active brew");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_cauldron_fixed_clock")
    public static void mutandisCompletesOnTheTwentyFirstLoadedPulse(GameTestHelper helper) {
        var relative = new BlockPos(5, 4, 5);
        var cauldron = placeCauldron(helper, helper.absolutePos(relative), originalRitual(3000, 0, true,
                oldItem("witchery:ingredient", 22, 1), oldItem("witchery:ingredient", 31, 1),
                oldItem("minecraft:egg", 0, 1)));
        tickCauldron(helper, cauldron, 207);
        helper.assertTrue(cauldron.saveWithoutMetadata().getInt("RitualTicks") == 200
                        && tank(cauldron).getFluidInTank(0).getAmount() == 3000,
                "The first twenty local pulses keep a full Cauldron at 200 saved warmup ticks");
        tickCauldron(helper, cauldron, 1);
        helper.assertTrue(tank(cauldron).getFluidInTank(0).isEmpty(),
                "The twenty-first pulse consumes the brew at loaded tick 208");
        helper.succeedWhen(() -> helper.assertItemEntityCountIs(
                WitcheryIngredient.MUTANDIS.item(), relative.above(), 2, 6));
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_cauldron_transfer_clock")
    public static void fixedRitualChecksItsVolumeOnThePulse(GameTestHelper helper) {
        var pos = helper.absolutePos(new BlockPos(5, 4, 5));
        var cauldron = placeCauldron(helper, pos, originalRitual(3000, 5, true,
                oldItem("witchery:ingredient", 22, 1), oldItem("witchery:ingredient", 31, 1),
                oldItem("minecraft:egg", 0, 1)));
        var tank = tank(cauldron);
        tickCauldron(helper, cauldron, 7);
        var portion = tank.drain(1000, IFluidHandler.FluidAction.EXECUTE);
        helper.assertTrue(cauldron.saveWithoutMetadata().getInt("RitualTicks") == 50,
                "A partial drain keeps five completed pulses until the next ritual check");
        helper.assertTrue(tank.fill(portion, IFluidHandler.FluidAction.EXECUTE) == 1000, "Return the same brew portion");
        tickCauldron(helper, cauldron, 1);
        helper.assertTrue(cauldron.saveWithoutMetadata().getInt("RitualTicks") == 60,
                "Refilling before the pulse allows the sixth pulse to advance");

        tank.drain(1000, IFluidHandler.FluidAction.EXECUTE);
        tickCauldron(helper, cauldron, 10);
        helper.assertTrue(tank.getFluidInTank(0).getAmount() == 2000
                        && cauldron.saveWithoutMetadata().getInt("RitualTicks") == 0,
                "The next pulse resets unfinished warmup when the cauldron is still partial");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_cauldron_empty_refill")
    public static void fullTransferKeepsHeatAndLocalWarmupBeforeTheNextSample(GameTestHelper helper) {
        var pos = helper.absolutePos(new BlockPos(5, 4, 5));
        var brew = placeCauldron(helper, pos, originalRitual(3000, 5, true,
                oldItem("minecraft:nether_wart", 0, 1), oldItem("minecraft:sugar", 0, 1),
                oldItem("minecraft:gunpowder", 0, 1), oldItem("witchery:dog_tongue", 0, 1)));
        var fixed = placeCauldron(helper, pos.south(3), originalRitual(3000, 5, true,
                oldItem("witchery:ingredient", 22, 1), oldItem("witchery:ingredient", 31, 1),
                oldItem("minecraft:egg", 0, 1)));
        var altar = formAltar(helper, pos.east(6));
        var charged = altar.saveWithoutMetadata();
        charged.putFloat("Power", 10_000);
        altar.load(charged);

        refillBetweenChecks(helper, brew);
        refillBetweenChecks(helper, fixed);
        helper.assertTrue(brew.saveWithoutMetadata().getInt("RitualTicks") == 6
                        && fixed.saveWithoutMetadata().getInt("RitualTicks") == 60,
                "Both rituals advance their sixth pulse after the full refill and new altar sample");

        tank(brew).drain(3000, IFluidHandler.FluidAction.EXECUTE);
        tickCauldron(helper, brew, 1);
        var cooled = brew.saveWithoutMetadata();
        helper.assertTrue(cooled.getInt("HeatTicks") == 0 && cooled.getInt("RitualTicks") == 6,
                "The first empty tick cools the cauldron while warmup waits for its own pulse");
        tickCauldron(helper, brew, 9);
        helper.assertTrue(brew.saveWithoutMetadata().getInt("RitualTicks") == 0,
                "The next ritual pulse clears unfinished warmup for a cauldron that stayed empty");
        helper.succeed();
    }

    private static void refillBetweenChecks(GameTestHelper helper, WitchesCauldronBlockEntity cauldron) {
        var tank = tank(cauldron);
        tickCauldron(helper, cauldron, 6);
        var portion = tank.drain(3000, IFluidHandler.FluidAction.EXECUTE);
        var empty = cauldron.saveWithoutMetadata();
        helper.assertTrue(tank.getFluidInTank(0).isEmpty() && empty.getInt("HeatTicks") == 100
                        && empty.getInt("RitualTicks") == 5 && !empty.getBoolean("Powered")
                        && cauldron.comparatorSignal() == 0,
                "The drained cauldron saves heat and completed pulses with its power and liquid cleared");
        cauldron.load(empty);
        helper.assertTrue(tank.fill(portion, IFluidHandler.FluidAction.EXECUTE) == 3000,
                "The drained brew can return after the save round trip");
        tickCauldron(helper, cauldron, 2);
        helper.assertTrue(cauldron.saveWithoutMetadata().getInt("HeatTicks") == 100
                        && cauldron.saveWithoutMetadata().getBoolean("Powered"),
                "Refilling before the next sample keeps the cauldron boiling and restores power");
    }

    private static AltarBlockEntity formAltar(GameTestHelper helper, BlockPos core) {
        var level = helper.getLevel();
        var state = WitcheryBlocks.getRequired("altar").get().defaultBlockState();
        for (var member : BlockPos.betweenClosed(core, core.offset(2, 0, 1))) {
            level.setBlockAndUpdate(member, state);
        }
        AltarBlockEntity.form(level, core);
        return (AltarBlockEntity) level.getBlockEntity(core);
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_taglock_modifier_order")
    public static void taglockCursePassesDurationAndParticleModifiersToTheNextEffect(GameTestHelper helper) {
        var level = helper.getLevel();
        var origin = helper.absolutePos(new BlockPos(5, 4, 5));
        var pig = helper.spawn(EntityType.PIG, new BlockPos(7, 4, 5));
        pig.setNoAi(true);
        var taglock = new ItemStack(WitcheryItems.getRequired("taglockkit").get());
        TaglockItem.bindSignature(taglock, pig);
        var bottleItem = WitcheryItems.getRequired("brewbottle").get();
        record Recipe(Item ingredient, MobEffect effect, boolean inverted, int targetedTicks, int drinkTicks) {}
        var recipes = List.of(
                new Recipe(WitcheryIngredient.DISTURBED_COTTON.item(), WitcheryEffects.SINKING.get(), false, 36_003_600, 7200),
                new Recipe(WitcheryIngredient.DROP_OF_LUCK.item(), WitcheryEffects.BREWING_EXPERTISE.get(), false, 14400, 14400),
                new Recipe(WitcheryIngredient.DROP_OF_LUCK.item(), WitcheryEffects.INSANITY.get(), true, 36_003_600, 7200),
                new Recipe(Items.RED_DYE, WitcheryEffects.COLORFUL.get(), false, 1800, 1800));
        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(level.getEntity(pig.getUUID()) == pig,
                "The creature is available to the Taglock"))
                .thenExecute(() -> {
                    for (var recipe : recipes) {
                        var ingredients = new ArrayList<>(List.of(new ItemStack(Items.NETHER_WART),
                                new ItemStack(WitcheryIngredient.TEAR_OF_THE_GODDESS.item()),
                                new ItemStack(WitcheryIngredient.DIAMOND_VAPOUR.item()), new ItemStack(Items.DIAMOND),
                                new ItemStack(Items.REDSTONE), new ItemStack(Items.GOLD_NUGGET)));
                        if (recipe.inverted()) ingredients.add(new ItemStack(Items.FERMENTED_SPIDER_EYE));
                        ingredients.add(new ItemStack(recipe.ingredient()));
                        ingredients.add(new ItemStack(Items.SUGAR));
                        ingredients.add(new ItemStack(Items.GHAST_TEAR));
                        var mixture = BrewMixture.from(ingredients);
                        var bottle = new ItemStack(bottleItem);
                        bottle.setTag(mixture.bottleTag(ingredients));
                        helper.assertTrue(bottleItem.finishUsingItem(bottle, level, pig).is(Items.GLASS_BOTTLE),
                                "Drinking the same mixture should return its bottle");
                        verifyBrewEffect(helper, pig, recipe.effect(), recipe.drinkTicks(), true);
                        verifyBrewEffect(helper, pig, MobEffects.MOVEMENT_SPEED, 3600, false);
                        verifyBrewEffect(helper, pig, MobEffects.REGENERATION, 900, false);
                        pig.removeAllEffects();

                        ingredients.add(taglock);
                        mixture = BrewMixture.from(ingredients);
                        helper.assertTrue(BrewRituals.pulse(level, origin, mixture, mixture.bottleTag(ingredients), taglock, 1, false)
                                        == BrewRituals.Result.COMPLETE, "The copied Taglock cast should complete");
                        verifyBrewEffect(helper, pig, recipe.effect(), recipe.targetedTicks(), true);
                        verifyBrewEffect(helper, pig, MobEffects.MOVEMENT_SPEED, 7200, true);
                        verifyBrewEffect(helper, pig, MobEffects.REGENERATION, 900, false);
                        pig.removeAllEffects();
                    }
                    pig.discard();
                }).thenSucceed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void goldNuggetMakesOnlyTheNextActionEffectAmbient(GameTestHelper helper) {
        var pig = helper.spawn(EntityType.PIG, new BlockPos(4, 2, 4));
        var ingredients = List.of(new ItemStack(Items.NETHER_WART), new ItemStack(WitcheryIngredient.TEAR_OF_THE_GODDESS.item()),
                new ItemStack(Items.GOLD_NUGGET), new ItemStack(Items.SNOWBALL), new ItemStack(Items.SUGAR));
        var brew = BrewMixture.from(ingredients);
        var restored = BrewMixture.from(brew.bottleTag(ingredients));
        restored.apply(pig, null, null, 1, 1);
        verifyBrewEffect(helper, pig, WitcheryEffects.SNOW_TRAIL.get(), 3600, true);
        verifyBrewEffect(helper, pig, MobEffects.MOVEMENT_SPEED, 3600, false);
        helper.succeed();
    }

    private static void verifyBrewEffect(GameTestHelper helper, LivingEntity target, MobEffect effect, int ticks, boolean ambient) {
        var applied = target.getEffect(effect);
        helper.assertTrue(applied != null && applied.getDuration() == ticks && applied.isAmbient() == ambient
                        && applied.isVisible() && applied.showIcon(),
                "Expected " + effect.getDescriptionId() + " for " + ticks + " ticks with ambient=" + ambient
                        + " and visible particles and icon, got " + applied);
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_taglock_purification")
    public static void taglockPurificationKeepsTheThreeOriginalWorseningThresholds(GameTestHelper helper) {
        var pig = helper.spawn(EntityType.PIG, new BlockPos(5, 2, 5));
        pig.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 1_200));
        record Trial(int amplifier, double low, double high, boolean worsens) {}
        var trials = List.of(new Trial(0, 0, 0.01, true), new Trial(0, 0.01, 0.25, false),
                new Trial(1, 0.01, 0.25, true), new Trial(1, 0.25, 0.75, false),
                new Trial(2, 0.25, 0.75, true), new Trial(2, 0.75, 1, false));
        var random = RandomSource.create();
        for (var trial : trials) {
            pig.removeEffect(MobEffects.WEAKNESS);
            pig.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 900, trial.amplifier(), true, false));
            long seed = 0;
            double roll;
            do {
                random.setSeed(seed++);
                roll = random.nextDouble();
            } while (roll <= trial.low() || roll >= trial.high());
            helper.getLevel().getRandom().setSeed(seed - 1);
            BrewStatusActions.ritualCleanse(pig, 1);
            if (trial.worsens()) {
                verifyBrewEffect(helper, pig, MobEffects.WEAKNESS, 900, false);
                helper.assertTrue(pig.getEffect(MobEffects.WEAKNESS).getAmplifier() == trial.amplifier() + 1,
                        "A worsened effect gains exactly one amplifier level");
            } else {
                helper.assertFalse(pig.hasEffect(MobEffects.WEAKNESS), "The original threshold clears this effect");
            }
            verifyBrewEffect(helper, pig, MobEffects.MOVEMENT_SPEED, 1_200, false);
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_gas_ritual_height")
    public static void gasWaveKeepsTheTargetHeightWhenFootingIsAboveOrBelow(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos target = helper.absolutePos(new BlockPos(8, 0, 8)).atY(198);
        var trigger = new ItemStack(WitcheryIngredient.DOG_TONGUE.item());
        var offerings = List.of(new ItemStack(WitcheryIngredient.MANDRAKE_ROOT.item()), new ItemStack(Items.NETHER_WART),
                new ItemStack(Items.SUGAR), new ItemStack(Items.COAL), new ItemStack(WitcheryIngredient.BAT_WOOL.item()), trigger);
        var brew = BrewMixture.from(offerings, null);
        var atTarget = EntityType.PIG.create(level);
        var onShelf = EntityType.PIG.create(level);
        atTarget.setPos(Vec3.atLowerCornerOf(target).add(0.25, 0.5, 0.25));
        onShelf.setPos(Vec3.atLowerCornerOf(target).add(0.25, 5, 0.25));
        for (var pig : List.of(atTarget, onShelf)) {
            pig.setNoAi(true);
            pig.setNoGravity(true);
            level.addFreshEntity(pig);
        }
        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(level.getEntity(atTarget.getUUID()) == atTarget
                        && level.getEntity(onShelf.getUUID()) == onShelf, "Both pigs are tracked"))
                .thenExecute(() -> {
                    for (int offset : new int[]{4, -4, -8}) {
                        BlockPos shelf = target.above(offset);
                        BlockPos targetFire = target.east(2);
                        BlockPos shelfFire = shelf.east(2).above();
                        level.setBlockAndUpdate(shelf, Blocks.STONE.defaultBlockState());
                        onShelf.setPos(Vec3.atLowerCornerOf(shelf.above()).add(0.25, 0, 0.25));
                        for (BlockPos fire : List.of(targetFire, shelfFire)) {
                            level.setBlockAndUpdate(fire.below(), Blocks.NETHERRACK.defaultBlockState());
                            level.setBlockAndUpdate(fire, Blocks.FIRE.defaultBlockState());
                        }
                        level.getRandom().setSeed(5120); // Make the first terrain roll less than one percent.
                        BrewRituals.pulse(level, target, brew, brew.bottleTag(offerings), trigger, 1, false);
                        helper.assertTrue(atTarget.hasEffect(MobEffects.MOVEMENT_SPEED) == (offset != -8)
                                        && !onShelf.hasEffect(MobEffects.MOVEMENT_SPEED),
                                "Only a shelf within seven blocks admits the wave at its original height: " + offset);
                        helper.assertTrue(level.isEmptyBlock(targetFire) == (offset != -8)
                                        && level.getBlockState(shelfFire).is(Blocks.FIRE),
                                "The terrain roll reaches the target row and preserves fire at the shelf: " + offset);
                        atTarget.removeEffect(MobEffects.MOVEMENT_SPEED);
                        for (BlockPos fire : List.of(targetFire, shelfFire)) {
                            level.removeBlock(fire, false);
                            level.removeBlock(fire.below(), false);
                        }
                        level.setBlockAndUpdate(shelf, Blocks.AIR.defaultBlockState());
                    }
                    atTarget.discard();
                    onShelf.discard();
                }).thenSucceed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_raise_land_gas")
    public static void raiseLandGasOnlyMovesTheColumnThatRollsTerrainContact(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos target = helper.absolutePos(new BlockPos(7, 0, 7)).atY(196);
        var trigger = new ItemStack(WitcheryIngredient.DOG_TONGUE.item());
        for (var delivery : List.of(WitcheryIngredient.BAT_WOOL.item(), Items.GUNPOWDER)) {
            boolean gas = delivery != Items.GUNPOWDER;
            var offerings = List.of(new ItemStack(Items.NETHER_WART),
                    new ItemStack(WitcheryIngredient.TEAR_OF_THE_GODDESS.item()), new ItemStack(Items.GLOWSTONE_DUST),
                    new ItemStack(Items.QUARTZ), new ItemStack(delivery), trigger);
            var mixture = BrewMixture.from(offerings);
            for (BlockPos pos : BlockPos.betweenClosed(target.offset(-4, -3, -4), target.offset(4, 4, 4))) {
                level.setBlockAndUpdate(pos, pos.getY() <= target.getY() ? Blocks.STONE.defaultBlockState() : Blocks.AIR.defaultBlockState());
            }
            level.getRandom().setSeed(5120); // The first gas terrain roll succeeds.
            var status = BrewRituals.pulse(level, target, mixture, mixture.bottleTag(offerings), trigger, 1, false);
            helper.assertTrue(status == (gas ? BrewRituals.Result.ONGOING : BrewRituals.Result.COMPLETE),
                    "The gas wave continues while the instant cast completes");
            for (int dx = -4; dx <= 4; dx++) {
                for (int dz = -4; dz <= 4; dz++) {
                    var column = target.offset(dx, 0, dz);
                    boolean raised = gas ? dx == 0 && dz == 0 : dx * dx + dz * dz < 16;
                    helper.assertTrue(raised ? level.getBlockState(column.above(4)).is(Blocks.STONE) && level.isEmptyBlock(column)
                                    : level.getBlockState(column).is(Blocks.STONE) && level.isEmptyBlock(column.above(4)),
                            "Only the selected columns move four blocks for " + delivery + " at offset " + dx + ", " + dz);
                }
            }
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_rain_block_actions")
    public static void rainImpactUsesOrdinaryBlockActionsWithinRitualStrength(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos ground = helper.absolutePos(new BlockPos(7, 0, 7)).atY(196);
        for (BlockPos pos : BlockPos.betweenClosed(ground.offset(-4, -3, -4), ground.offset(4, 4, 4))) {
            level.setBlockAndUpdate(pos, pos.getY() <= ground.getY() ? Blocks.STONE.defaultBlockState() : Blocks.AIR.defaultBlockState());
        }
        var ingredients = List.of(new ItemStack(Items.NETHER_WART), new ItemStack(WitcheryIngredient.TEAR_OF_THE_GODDESS.item()),
                new ItemStack(Items.NETHER_STAR), new ItemStack(Items.GLOWSTONE_DUST), new ItemStack(Items.BLAZE_ROD),
                new ItemStack(Items.QUARTZ), new ItemStack(WitcheryIngredient.SILVER_DUST.item()),
                new ItemStack(WitcheryIngredient.WORMWOOD.item()));
        var mixture = BrewMixture.from(ingredients);
        var droplet = WitcheryEntities.BREW_DROPLET.get().create(level);
        droplet.setBrew(mixture.bottleTag(ingredients));
        droplet.setPos(Vec3.atBottomCenterOf(ground).add(0, 1.25, 0));
        droplet.setDeltaMovement(0, -0.75, 0);

        var weather = (ServerLevelData) level.getLevelData();
        int clear = weather.getClearWeatherTime();
        int rain = weather.getRainTime();
        int thunder = weather.getThunderTime();
        boolean raining = weather.isRaining();
        boolean thundering = weather.isThundering();
        try {
            level.setWeatherParameters(4000, 0, false, false);
            droplet.tick();
            helper.assertTrue(droplet.isRemoved() && level.getBlockState(ground.above(4)).is(Blocks.STONE)
                            && level.isEmptyBlock(ground) && level.getBlockState(ground.east()).is(Blocks.STONE),
                    "Rain lifts one column and caps a strength-two recipe at strength one");
            helper.assertTrue(!weather.isRaining() && !weather.isThundering() && weather.getClearWeatherTime() == 4000,
                    "Silver Dust in a droplet keeps the current weather");
        } finally {
            weather.setClearWeatherTime(clear);
            weather.setRainTime(rain);
            weather.setThunderTime(thunder);
            weather.setRaining(raining);
            weather.setThundering(thundering);
            droplet.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_brew_caster")
    public static void ownerlessLiquidAndGasRitualUseTheOriginalCaster(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(5, 4, 5));
        var pig = helper.spawn(EntityType.PIG, new BlockPos(5, 5, 5));
        pig.setNoAi(true);
        var trigger = new ItemStack(WitcheryIngredient.DOG_TONGUE.item());
        var ingredients = List.of(new ItemStack(Items.NETHER_WART), new ItemStack(Items.PUMPKIN),
                new ItemStack(WitcheryIngredient.BAT_WOOL.item()), trigger);
        var brew = BrewMixture.from(ingredients);
        var payload = brew.bottleTag(ingredients);
        var fake = FakePlayerFactory.getMinecraft(level);
        var hits = new ArrayList<DamageSource>();
        Consumer<LivingHurtEvent> record = event -> {
            if (event.getEntity() == pig) hits.add(event.getSource());
        };

        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(level.getEntity(pig.getUUID()) == pig,
                "The pig is available to the gas pulse")).thenExecute(() -> {
            MinecraftForge.EVENT_BUS.addListener(record);
            try {
                level.setBlockAndUpdate(pos, Blocks.STONE.defaultBlockState());
                pig.setPos(pos.getX() + 0.25, pos.getY() + 1, pos.getZ() + 0.25);
                helper.assertTrue(BrewRituals.pulse(level, pos, brew, payload, trigger, 1, false)
                        == BrewRituals.Result.ONGOING, "The first gas pulse continues the ritual");
                helper.assertTrue(hits.size() == 1 && hits.get(0).getEntity() == fake,
                        "The gas ritual credits the Forge fake player");
                for (boolean gas : List.of(false, true)) {
                    var block = WitcheryBlocks.getRequired(gas ? "brewgas" : "brewliquid").get();
                    level.setBlockAndUpdate(pos, block.defaultBlockState());
                    var fluid = (BrewFluidBlockEntity) level.getBlockEntity(pos);
                    fluid.initialize(payload, brew, brew.color(), null);
                    pig.invulnerableTime = 0;
                    hits.clear();
                    fluid.expose(level, pig, gas);
                    helper.assertTrue(hits.size() == 1 && hits.get(0).getEntity() == (gas ? null : fake),
                            "Liquid contact uses the Forge fake player and cloud contact stays ownerless");
                }
            } finally {
                MinecraftForge.EVENT_BUS.unregister(record);
            }
            pig.discard();
        }).thenSucceed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_liquid_ritual_height")
    public static void liquidRitualOnlyRaisesDropsAboveObstructedColumns(GameTestHelper helper) {
        var level = helper.getLevel();
        var origin = helper.absolutePos(new BlockPos(5, 0, 5)).atY(214);
        var stone = new ItemStack(WitcheryIngredient.BOUND_WAYSTONE.item());
        WaystoneItem.bindLocation(stone, level, origin.offset(12, 16, 0));
        var trigger = new ItemStack(WitcheryIngredient.DOG_TONGUE.item());
        var offerings = List.of(new ItemStack(WitcheryIngredient.MANDRAKE_ROOT.item()), new ItemStack(Items.NETHER_WART),
                new ItemStack(Items.GLISTERING_MELON_SLICE), new ItemStack(WitcheryIngredient.WORMWOOD.item()), stone, trigger);
        var mixture = BrewMixture.from(offerings, null);

        // The seed 817 pulse recorded from Witchery 0.24.1, relative to the cauldron.
        var original = List.of(origin.offset(0, 0, -7).atY(114), origin.offset(5, 0, -2).atY(114),
                origin.offset(14, 0, -6).atY(119), origin.offset(6, 0, 13).atY(109), origin.offset(-3, 0, 9).atY(104));
        BlockPos obstruction = original.get(0).atY(205);
        var previous = level.getBlockState(obstruction);
        level.setBlockAndUpdate(obstruction, Blocks.STONE.defaultBlockState());
        var expected = original.stream().map(pos -> {
            int surface = level.getHeight(Heightmap.Types.MOTION_BLOCKING, pos.getX(), pos.getZ());
            return pos.getY() < surface ? pos.atY(surface + 16) : pos;
        }).toList();
        helper.assertTrue(expected.get(0).getY() >= 222, "The fixture blocks the first rain column");
        var actual = new ArrayList<BlockPos>();
        Consumer<EntityJoinLevelEvent> capture = event -> {
            if (event.getLevel() == level && event.getEntity() instanceof BrewDroplet drop) {
                actual.add(drop.blockPosition());
                event.setCanceled(true);
            }
        };
        MinecraftForge.EVENT_BUS.addListener(capture);
        try {
            level.getRandom().setSeed(817);
            helper.assertTrue(BrewRituals.pulse(level, origin, mixture, mixture.bottleTag(offerings), trigger, 1, false)
                            == BrewRituals.Result.ONGOING,
                    "The first pulse of the Waystone-targeted rain continues");
            helper.assertTrue(actual.equals(expected), "Rain should use the original columns and clear heights: " + actual);
        } finally {
            MinecraftForge.EVENT_BUS.unregister(capture);
            level.setBlockAndUpdate(obstruction, previous);
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void distinguishesMissingCirclesFromDistantTargets(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos origin = helper.absolutePos(new BlockPos(6, 4, 5));
        var trigger = new ItemStack(WitcheryIngredient.DOG_TONGUE.item());
        var missingCircles = List.of(new ItemStack(WitcheryIngredient.MANDRAKE_ROOT.item()), new ItemStack(Items.NETHER_WART),
                new ItemStack(WitcheryIngredient.TEAR_OF_THE_GODDESS.item()), new ItemStack(WitcheryIngredient.REFINED_EVIL.item()),
                new ItemStack(Items.GUNPOWDER), trigger);
        var hellGate = BrewMixture.from(missingCircles, null);
        helper.assertTrue(hellGate.requiresInfernalCircle(), "The fixture contains Hell Gate");
        helper.assertTrue(BrewRituals.pulse(level, origin, hellGate, hellGate.bottleTag(missingCircles), trigger, 1, false)
                        == BrewRituals.Result.INVALID_CIRCLES,
                "A Hell Gate without its Infernal ring reports invalid circles");

        var bound = new ItemStack(WitcheryIngredient.BOUND_WAYSTONE.item());
        WaystoneItem.bindLocation(bound, level, origin.east(33));
        var distant = List.of(new ItemStack(Items.NETHER_WART), new ItemStack(Items.SUGAR),
                new ItemStack(Items.GUNPOWDER), bound, trigger);
        var swiftness = BrewMixture.from(distant, null);
        helper.assertTrue(BrewRituals.pulse(level, origin, swiftness, swiftness.bottleTag(distant), trigger, 1, false)
                        == BrewRituals.Result.TOO_FAR,
                "An ordinary brew aimed one block past its range reports distance");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void legacyRecipeOfferingsNeedPreparedEffectCapacity(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(6, 4, 5));
        var water = boilingWater();
        var cauldron = placeCauldron(helper, pos, water);
        var mandrake = WitcheryIngredient.MANDRAKE_ROOT.item();
        var tear = WitcheryIngredient.TEAR_OF_THE_GODDESS.item();
        var evil = WitcheryIngredient.REFINED_EVIL.item();
        var extremis = WitcheryIngredient.MUTANDIS_EXTREMIS.item();

        helper.assertTrue(!offer(cauldron, evil) && !offer(cauldron, WitcheryIngredient.ENT_BRANCH.item()),
                "The empty brew should reject effect ingredients even when a fixed recipe calls for them");
        helper.assertTrue(offer(cauldron, Items.NETHER_WART) && offer(cauldron, mandrake) && offer(cauldron, tear)
                        && !offer(cauldron, evil) && !offer(cauldron, extremis),
                "Adding Wart before Mandrake leaves the mixture one effect level short of Drop of Luck");

        cauldron.load(water);
        helper.assertTrue(offer(cauldron, mandrake) && offer(cauldron, Items.NETHER_WART) && offer(cauldron, tear)
                        && offer(cauldron, evil) && offer(cauldron, extremis),
                "Adding Mandrake first supplies the capacity for all of the original offerings");
        helper.assertTrue(cauldron.saveWithFullMetadata().getString("Ritual").equals("witchery:drop_of_luck"),
                "The same fixed recipe should begin after the original ordered preparation");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void legacyFixedRecipesAcceptTheirPreparedOfferings(GameTestHelper helper) {
        var level = helper.getLevel();
        var water = boilingWater();
        var cauldron = placeCauldron(helper, helper.absolutePos(new BlockPos(6, 4, 5)), water);
        var recipes = level.getRecipeManager().getAllRecipesFor(WitcheryRecipes.CAULDRON_RITUAL.get()).stream()
                .filter(CauldronRitualRecipe::legacy).toList();
        helper.assertTrue(recipes.size() == 27, "Exercise all twenty-seven original fixed cauldron recipes");
        for (var recipe : recipes) {
            cauldron.load(water);
            for (var ingredient : recipe.ingredients()) {
                ItemStack stack = ingredient.getItems()[0];
                helper.assertTrue(offer(cauldron, stack.copyWithCount(1)), recipe.id() + " should accept " + stack.getItem());
            }
            helper.assertTrue(offer(cauldron, recipe.trigger().getItems()[0].copyWithCount(1)), recipe.id() + " should accept its trigger");
            helper.assertTrue(cauldron.saveWithFullMetadata().getString("Ritual").equals(recipe.id().toString()),
                    recipe.id() + " should start after all of its original offerings are in the cauldron");
        }
        helper.succeed();
    }

    private static CompoundTag boilingWater() {
        var water = new CompoundTag();
        water.putString("FluidName", "water");
        water.putInt("Amount", 3000);
        water.putInt("HeatTicks", 100);
        return water;
    }

    private static boolean offer(WitchesCauldronBlockEntity cauldron, Item item) {
        return offer(cauldron, new ItemStack(item));
    }

    private static boolean offer(WitchesCauldronBlockEntity cauldron, ItemStack stack) {
        Vec3 center = Vec3.atCenterOf(cauldron.getBlockPos());
        var dropped = new ItemEntity(cauldron.getLevel(), center.x, center.y, center.z, stack);
        cauldron.acceptDroppedItem(dropped);
        return dropped.isRemoved();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_legacy_healing")
    public static void originalMelonBrewsRetainTheirInstantEffects(GameTestHelper helper) {
        var wart = oldItem("minecraft:nether_wart", 0, 1);
        var melon = oldItem("minecraft:speckled_melon", 0, 1);
        var original = originalRitual(3000, 0, true, wart, melon);
        var originalBrew = original.getCompound("Tag");
        originalBrew.remove("RitualTriggered");
        var cauldron = placeCauldron(helper, helper.absolutePos(new BlockPos(6, 4, 5)), original);
        cauldron.load(cauldron.saveWithFullMetadata());
        var saved = cauldron.saveWithFullMetadata();
        var ingredients = saved.getList("Ingredients", Tag.TAG_COMPOUND);
        helper.assertTrue(ingredients.size() == 2 && ingredients.getCompound(0).getString("id").equals("minecraft:nether_wart")
                        && ingredients.getCompound(1).getString("id").equals("minecraft:glistering_melon_slice")
                        && saved.getInt("Color") == originalBrew.getInt("Color") && tank(cauldron).getFluidInTank(0).getAmount() == 3000,
                "The original cauldron should retain its ordered healing brew, amount and color through a save round trip");

        var bottle = new ItemStack(WitcheryItems.getRequired("brewbottle").get());
        bottle.setTag(originalBrew.copy());
        var healed = helper.spawn(EntityType.PIG, new BlockPos(4, 3, 5));
        healed.setHealth(4.0F);
        helper.assertTrue(bottle.finishUsingItem(helper.getLevel(), healed).is(Items.GLASS_BOTTLE) && healed.getHealth() == 8.0F,
                "A saved healing bottle should heal by four points and return glass");

        var inverted = originalRitual(3000, 0, true, wart, oldItem("minecraft:fermented_spider_eye", 0, 1), melon)
                .getCompound("Tag");
        inverted.remove("RitualTriggered");
        var second = new ItemStack(WitcheryItems.getRequired("brewbottle").get());
        second.setTag(inverted);
        var hurt = helper.spawn(EntityType.PIG, new BlockPos(8, 3, 5));
        helper.assertTrue(second.finishUsingItem(helper.getLevel(), hurt).is(Items.GLASS_BOTTLE) && hurt.getHealth() == 4.0F,
                "The original inverted bottle should deal six points of instant damage");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void resumesOriginalMutandisWarmupAcrossBothSaveFormats(GameTestHelper helper) {
        BlockPos relative = new BlockPos(6, 4, 5);
        BlockPos pos = helper.absolutePos(relative);
        var cauldron = placeCauldron(helper, pos, originalRitual(3000, 19, true,
                oldItem("witchery:ingredient", 22, 1), oldItem("witchery:ingredient", 31, 1), oldItem("minecraft:egg", 0, 1)));
        CompoundTag beforeFirstTick = cauldron.saveWithFullMetadata();
        helper.assertTrue(beforeFirstTick.getBoolean("LegacyRitualPending") && beforeFirstTick.getInt("RitualTicks") == 19,
                "Saving before recipe lookup preserves the original trigger and pulse count");
        cauldron.load(beforeFirstTick);
        helper.runAfterDelay(4, () -> {
            CompoundTag resumed = cauldron.saveWithFullMetadata();
            helper.assertTrue(resumed.getString("Ritual").equals("witchery:mutandis") && resumed.getInt("RitualTicks") >= 190
                            && resumed.getInt("RitualTicks") < 200 && resumed.getList("Ingredients", Tag.TAG_COMPOUND).size() == 2,
                    "The server selects Mutandis and converts warmup pulses to ticks without keeping the Egg as an ingredient");
            cauldron.load(resumed);
        });
        helper.runAfterDelay(28, () -> {
            helper.assertItemEntityCountIs(WitcheryIngredient.MUTANDIS.item(), relative.above(2), 3, 6);
            helper.assertTrue(tank(cauldron).getFluidInTank(0).isEmpty(), "The resumed recipe yields six Mutandis and clears the tank");
            helper.succeed();
        });
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_saved_brew_transport")
    public static void restoresTheFixedTriggerBeforeTrailingInactiveOfferings(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(6, 4, 5));
        var original = originalRitual(3000, 19, true, oldItem("witchery:ingredient", 22, 1),
                oldItem("witchery:ingredient", 31, 1), oldItem("minecraft:egg", 0, 4),
                oldItem("minecraft:diamond_pickaxe", 0, 1), oldItem("witchery_missing_fixture:unknown", 3, 2));
        var bowl = placeCauldron(helper, pos, original);
        var tank = tank(bowl);
        var before = tank.getFluidInTank(0).getTag();
        tickCauldron(helper, bowl, 1);
        var restored = bowl.saveWithFullMetadata();
        helper.assertTrue(restored.getString("Ritual").equals("witchery:mutandis") && restored.getInt("RitualTicks") == 190
                        && restored.getInt("RitualTriggerPosition") == 2,
                "The original fixed trigger must select Mutandis ahead of inactive entries");
        bowl.load(restored);
        var withdrawal = tank.drain(1000, IFluidHandler.FluidAction.EXECUTE);
        var after = withdrawal.getTag();
        helper.assertTrue(after != null && after.getBoolean("RitualTriggered")
                        && after.getList("Items", Tag.TAG_COMPOUND).equals(before.getList("Items", Tag.TAG_COMPOUND)),
                "Saving and withdrawing the fixed brew must retain the trigger's count and original position");
        helper.assertTrue(tank.fill(withdrawal, IFluidHandler.FluidAction.EXECUTE) == 1000
                        && tank.getFluidInTank(0).isFluidEqual(withdrawal),
                "The saved trigger and trailing entries must match when the same portion returns");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void readsComparatorEffectsFromAnOriginalPartialCast(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(6, 4, 5));
        var bowl = placeCauldron(helper, pos, originalRitual(2500, 0, true,
                oldItem("witchery:ingredient", 22, 1), oldItem("witchery:ingredient", 31, 1),
                oldItem("minecraft:wheat", 0, 1), oldItem("minecraft:egg", 0, 1)));
        bowl.load(bowl.saveWithFullMetadata());
        helper.succeedWhen(() -> {
            helper.assertTrue(bowl.comparatorSignal() == 15,
                    "The original-format cast reports full brew capacity while it waits for its final liquid");
            helper.assertTrue(bowl.saveWithoutMetadata().getBoolean("LegacyRitualPending"),
                    "Reading the comparator leaves the original trigger in its pending save format");
        });
    }

    @GameTest(template = "qa_coven_arena")
    public static void preservesFixedOfferingCountsThroughBrewTransfer(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(6, 4, 5));
        var source = placeCauldron(helper, pos, boilingWater());
        var receiver = placeCauldron(helper, pos.east(4), new CompoundTag());
        var root = new ItemStack(WitcheryIngredient.MANDRAKE_ROOT.item(), 3);
        var exhale = new ItemStack(WitcheryIngredient.EXHALE_OF_THE_HORNED_ONE.item(), 2);
        var eggs = new ItemStack(Items.EGG, 4).setHoverName(Component.literal("Stacked Trigger"));
        helper.assertTrue(offer(source, root) && offer(source, exhale) && offer(source, eggs),
                "A dropped stack is accepted as one offering for each ingredient and trigger");
        var saved = source.saveWithFullMetadata();
        helper.assertTrue(saved.getString("Ritual").equals("witchery:mutandis")
                        && ItemStack.matches(eggs, ItemStack.of(saved.getCompound("RitualTrigger"))),
                "The active fixed recipe keeps all four eggs and their name");
        var brew = tank(source).drain(3000, IFluidHandler.FluidAction.EXECUTE);
        helper.assertTrue(tank(receiver).fill(brew, IFluidHandler.FluidAction.EXECUTE) == 3000,
                "The destination can accept the full tagged cast");
        receiver.load(receiver.saveWithFullMetadata());
        helper.runAfterDelay(5, () -> {
            var restored = receiver.saveWithoutMetadata();
            var offerings = restored.getList("Ingredients", Tag.TAG_COMPOUND);
            helper.assertTrue(restored.getString("Ritual").equals("witchery:mutandis") && offerings.size() == 2
                            && ItemStack.matches(root, ItemStack.of(offerings.getCompound(0)))
                            && ItemStack.matches(exhale, ItemStack.of(offerings.getCompound(1)))
                            && ItemStack.matches(eggs, ItemStack.of(restored.getCompound("RitualTrigger"))),
                    "Resuming a transferred fixed cast retains the quantities and tags of all original offerings");
            helper.succeed();
        });
    }

    @GameTest(template = "qa_coven_arena")
    public static void withdrawsFromAnOriginalPartialCast(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(6, 4, 5));
        var original = originalRitual(2500, 0, true,
                oldItem("witchery:ingredient", 22, 1), oldItem("witchery:ingredient", 31, 1),
                oldItem("minecraft:wheat", 0, 1), oldItem("minecraft:egg", 0, 1));
        original.putBoolean("Powered", true);
        var bowl = placeCauldron(helper, pos, original);
        bowl.load(bowl.saveWithFullMetadata());
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "BottleImportedCast"));
        WitcheryPlayerDataProvider.get(player).orElseThrow(IllegalStateException::new).setPotionBottling(30);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GLASS_BOTTLE));
        helper.assertTrue(bowl.bottle(player, InteractionHand.MAIN_HAND), "The imported brewing remainder can be bottled before a refill");
        var bottled = player.getMainHandItem().getOrCreateTag();
        helper.assertTrue(bottled.getString("FixedRitual").equals("witchery:mutandis")
                        && bottled.getBoolean("RitualTriggered") && tank(bowl).getFluidInTank(0).getAmount() == 1000,
                "The bottle keeps the original cast and the trained player leaves exactly one bucket");
        var drained = tank(bowl).drain(1000, IFluidHandler.FluidAction.EXECUTE);
        helper.assertTrue(drained.getAmount() == 1000 && drained.getTag() != null && drained.getTag().getBoolean("RitualTriggered")
                        && drained.getTag().getList("Items", Tag.TAG_COMPOUND).equals(bottled.getList("Items", Tag.TAG_COMPOUND))
                        && !bowl.saveWithoutMetadata().contains("LegacyRitualPending"),
                "Draining the final bucket retains the original trigger and clears the cauldron");
        helper.assertTrue(tank(bowl).fill(drained, IFluidHandler.FluidAction.EXECUTE) == 1000
                        && bowl.saveWithoutMetadata().getBoolean("LegacyRitualPending"),
                "Returning the imported bucket restores the part-filled pending cast");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void refillsLegacyCastFromItsSavedBrewBucket(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(6, 4, 5));
        BlockPos receiver = pos.east(4);
        CompoundTag original = originalRitual(2000, 0, false, oldItem("witchery:ingredient", 22, 1),
                oldItem("minecraft:glowstone_dust", 0, 1), oldItem("minecraft:coal", 1, 1));
        var cauldron = placeCauldron(helper, pos, original);
        var emptyCauldron = placeCauldron(helper, receiver, new CompoundTag());
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "LegacyCauldron"));
        helper.runAfterDelay(3, () -> {
            cauldron.load(cauldron.saveWithFullMetadata());
            helper.assertTrue(cauldron.saveWithFullMetadata().getBoolean("LegacyRitualPending")
                            && tank(cauldron).getFluidInTank(0).getAmount() == 2000,
                    "A partially drained legacy cast retains its trigger and liquid across a saved round trip");
            var bucket = new ItemStack(WitcheryItems.getRequired("bucketbrew").get());
            bucket.setTag(original.getCompound("Tag").copy());
            player.setItemInHand(InteractionHand.MAIN_HAND, bucket);
            var hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
            level.getBlockState(pos).use(level, player, InteractionHand.MAIN_HAND, hit);
            helper.assertTrue(player.getMainHandItem().is(Items.BUCKET), "Using the original matching brew bucket restores the final bucket");

            for (int index = 0; index < 3; index++) {
                var portion = new ItemStack(WitcheryItems.getRequired("bucketbrew").get());
                portion.setTag(original.getCompound("Tag").copy());
                player.setItemInHand(InteractionHand.MAIN_HAND, portion);
                var receiverHit = new BlockHitResult(Vec3.atCenterOf(receiver), Direction.UP, receiver, false);
                level.getBlockState(receiver).use(level, player, InteractionHand.MAIN_HAND, receiverHit);
                helper.assertTrue(player.getMainHandItem().is(Items.BUCKET), "The empty cauldron accepts all three saved Brew Buckets");
            }
        });
        helper.runAfterDelay(6, () -> {
            CompoundTag restored = cauldron.saveWithFullMetadata();
            helper.assertTrue(restored.getString("Ritual").equals("witchery:brew_fuel_1") && !restored.contains("LegacyRitualPending")
                            && tank(cauldron).getFluidInTank(0).getAmount() == 3000,
                    "Charcoal resolves to the highest matching Brew Fuel recipe and the full cast waits for heat and power");
            helper.assertTrue(emptyCauldron.saveWithFullMetadata().getString("Ritual").equals("witchery:brew_fuel_1"),
                    "The legacy buckets also retain their cast in a previously empty cauldron");
            helper.succeed();
        });
    }

    @GameTest(template = "qa_coven_arena")
    public static void preservesLegacyGeneralBrewTrigger(GameTestHelper helper) {
        var pos = helper.absolutePos(new BlockPos(6, 4, 5));
        var cauldron = placeCauldron(helper, pos, originalRitual(3000, 0, false, oldItem("minecraft:nether_wart", 0, 1),
                oldItem("minecraft:sugar", 0, 1), oldItem("minecraft:gunpowder", 0, 1), oldItem("witchery:dog_tongue", 0, 1)));
        helper.runAfterDelay(5, () -> {
            var saved = cauldron.saveWithFullMetadata();
            helper.assertTrue(saved.getBoolean("RitualTriggered") && !saved.contains("LegacyRitualPending") && !saved.contains("Ritual")
                            && saved.getList("Ingredients", Tag.TAG_COMPOUND).size() == 4,
                    "The old Tongue of Dog retains its normal cast state and place at the end of the mixture");
            helper.succeed();
        });
    }

    @GameTest(template = "qa_coven_arena")
    public static void refundsUnmatchedLegacyTriggerAndKeepsMixture(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(6, 4, 5));
        var cauldron = placeCauldron(helper, pos, originalRitual(3000, 0, false, oldItem("minecraft:nether_wart", 0, 1),
                oldItem("minecraft:egg", 0, 3)));
        helper.runAfterDelay(5, () -> {
            cauldron.load(cauldron.saveWithFullMetadata());
            CompoundTag saved = cauldron.saveWithFullMetadata();
            helper.assertTrue(!saved.contains("Ritual") && !saved.contains("LegacyRitualPending")
                            && saved.getList("Ingredients", Tag.TAG_COMPOUND).size() == 1
                            && tank(cauldron).getFluidInTank(0).getAmount() == 3000,
                    "A recipe missing from the installed data releases the cauldron and preserves the brew");
        });
        helper.runAfterDelay(10, () -> {
            var eggs = level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(3), item -> item.getItem().is(Items.EGG));
            helper.assertTrue(eggs.size() == 1 && eggs.get(0).getItem().getCount() == 3,
                    "The original trigger stack is refunded once even across a saved round trip");
            helper.succeed();
        });
    }

    private static WitchesCauldronBlockEntity placeCauldron(GameTestHelper helper, BlockPos pos, CompoundTag original) {
        var level = helper.getLevel();
        if (original.getInt("TicksHeated") != 0) {
            level.setBlockAndUpdate(pos.below(2), Blocks.NETHERRACK.defaultBlockState());
            level.setBlockAndUpdate(pos.below(), Blocks.FIRE.defaultBlockState());
        }
        level.setBlockAndUpdate(pos, WitcheryBlocks.getRequired("cauldron").get().defaultBlockState());
        var cauldron = (WitchesCauldronBlockEntity) level.getBlockEntity(pos);
        cauldron.load(original);
        return cauldron;
    }

    private static void tickCauldron(GameTestHelper helper, WitchesCauldronBlockEntity cauldron, int ticks) {
        // Advance the tile while the world clock stays still.
        for (int tick = 0; tick < ticks; tick++) {
            WitchesCauldronBlockEntity.serverTick(helper.getLevel(), cauldron.getBlockPos(), cauldron.getBlockState(), cauldron);
        }
    }

    private static IFluidHandler tank(WitchesCauldronBlockEntity cauldron) {
        return cauldron.getCapability(ForgeCapabilities.FLUID_HANDLER).orElseThrow(IllegalStateException::new);
    }

    private static CompoundTag originalRitual(int fluid, int pulses, boolean heated, CompoundTag... offerings) {
        var root = new CompoundTag();
        root.putString("FluidName", "witchery:brew");
        root.putInt("Amount", fluid);
        root.putInt("TicksHeated", heated ? 100 : 0);
        root.putInt("RitualTicks", pulses);
        var brew = new CompoundTag();
        brew.putInt("Color", 0xb4a96c);
        brew.putBoolean("RitualTriggered", true);
        var items = new ListTag();
        for (var offering : offerings) items.add(offering);
        brew.put("Items", items);
        root.put("Tag", brew);
        return root;
    }

    private static CompoundTag oldItem(String id, int damage, int count) {
        var item = new CompoundTag();
        item.putString("id", id);
        item.putShort("Damage", (short) damage);
        item.putByte("Count", (byte) count);
        return item;
    }
}
