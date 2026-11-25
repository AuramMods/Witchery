package art.arcane.witchery.brewing;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.entity.AltarBlockEntity;
import art.arcane.witchery.block.entity.WitchesCauldronBlockEntity;
import art.arcane.witchery.entity.Leonard;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryEffects;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcheryItems;
import art.arcane.witchery.ritual.RitualCircle;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ComparatorBlock;
import net.minecraft.world.level.block.entity.ComparatorBlockEntity;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class CauldronMisfortuneTests {
    private static final BlockPos CAULDRON = new BlockPos(6, 4, 5);

    @GameTest(template = "qa_coven_arena", batch = "witchery_cauldron_power_threshold")
    public static void fixedCastChecksFractionalPowerBeforeDebiting(GameTestHelper helper) {
        BlockPos pos = placeCauldron(helper);
        var altar = placeAltar(helper);
        setSmallCircle(helper, pos, WitcheryBlocks.getRequired("circleglyphinfernal").get());
        prepareGoldenChalk(helper, pos);
        finishCast(helper, pos, altar, 2111, 3840);
        var waiting = cauldron(helper, pos).saveWithFullMetadata();
        helper.assertTrue(waiting.getString("Ritual").equals("witchery:golden_chalk") && waiting.getInt("RitualTicks") == 0
                        && altar.power() == 2111,
                "The Infernal circle should wait at the rounded debit of 2,111 power");

        prepareGoldenChalk(helper, pos);
        finishCast(helper, pos, altar, 2111.5F, 3840);
        helper.assertTrue(altar.power() == 0.5F, "The cast at 2,111.5 should spend 2,111 power");
        helper.startSequence().thenWaitUntil(() -> helper.assertItemEntityCountIs(
                WitcheryItems.getRequired("chalkheart").get(), CAULDRON.above(), 2, 1)).thenSucceed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_cauldron_power_threshold")
    public static void handBottlingUsesSampledPowerAndDebitsBaseCost(GameTestHelper helper) {
        BlockPos pos = placeCauldron(helper);
        var altar = placeAltar(helper);
        setSmallCircle(helper, pos, WitcheryBlocks.getRequired("circleglyphinfernal").get());
        prepareGoldenChalk(helper, pos);
        var cauldron = cauldron(helper, pos);
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "ChalkBottler"));
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GLASS_BOTTLE));

        setAltarPower(altar, 2111);
        tickCauldron(helper, cauldron, 7);
        helper.assertTrue(!cauldron.bottle(player, InteractionHand.MAIN_HAND)
                        && player.getMainHandItem().is(Items.GLASS_BOTTLE) && altar.power() == 2111,
                "The first power sample uses the full cast's fractional circle threshold");
        setAltarPower(altar, 2111.5F);
        helper.assertTrue(!cauldron.bottle(player, InteractionHand.MAIN_HAND) && altar.power() == 2111.5F,
                "Charge added after the power sample waits for the next one");
        tickCauldron(helper, cauldron, 20);
        setAltarPower(altar, 2049);
        helper.assertTrue(!cauldron.bottle(player, InteractionHand.MAIN_HAND) && altar.power() == 2049,
                "The sampled connection still needs enough live charge for the base debit");
        setAltarPower(altar, 2050);
        helper.assertTrue(cauldron.bottle(player, InteractionHand.MAIN_HAND) && altar.power() == 0,
                "A successful hand bottling spends the exact base cost between power samples");
        var bottle = player.getMainHandItem();
        helper.assertTrue(bottle.is(WitcheryItems.getRequired("brewbottle").get())
                        && bottle.getOrCreateTag().getString("FixedRitual").equals("witchery:golden_chalk"),
                "The novice bottles the whole saved Golden Chalk cast");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", timeoutTicks = 180)
    public static void activeCastComparatorUsesTheBrewAndItsAvailablePower(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = placeCauldron(helper);
        var altar = placeAltar(helper);
        prepareCast(helper, pos, "golden_chalk", WitcheryIngredient.MANDRAKE_ROOT.item().getDefaultInstance(),
                new ItemStack(Items.GOLD_NUGGET), new ItemStack(Items.WHEAT));
        setAltarPower(altar, 0);
        var output = pos.north();
        level.setBlockAndUpdate(output.below(), Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(output, Blocks.COMPARATOR.defaultBlockState().setValue(ComparatorBlock.FACING, Direction.SOUTH));
        var comparator = (ComparatorBlockEntity) level.getBlockEntity(output);
        var bowl = cauldron(helper, pos);

        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(comparator.getOutputSignal() == 9,
                        "An active Moonshine batch reports missing power"))
                .thenIdle(25)
                .thenExecute(() -> {
                    var stored = altar.saveWithFullMetadata();
                    stored.putInt("MaxPower", 2050);
                    stored.putFloat("Power", 2050);
                    altar.load(stored);
                })
                .thenIdle(25)
                .thenExecute(() -> {
                    helper.assertTrue(altar.power() == 2050, "The altar holds the base cost between ticks");
                    helper.assertTrue(comparator.getOutputSignal() == 9, "The full cast still needs its circle-scaled threshold");
                    var tank = bowl.getCapability(ForgeCapabilities.FLUID_HANDLER).orElseThrow(IllegalStateException::new);
                    helper.assertTrue(tank.drain(500, IFluidHandler.FluidAction.EXECUTE).getAmount() == 500,
                            "The active cast retains a 2,500 mB remainder");
                })
                .thenWaitUntil(() -> helper.assertTrue(comparator.getOutputSignal() == 15,
                        "The pending partial cast is powered at base cost and its brew capacity is exhausted"))
                .thenExecute(() -> level.setBlockAndUpdate(pos.below(), Blocks.AIR.defaultBlockState()))
                .thenWaitUntil(() -> helper.assertTrue(comparator.getOutputSignal() == 3,
                        "Cooling the same active cast produces the cold cauldron output"))
                .thenSucceed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_cauldron_misfortune")
    public static void fixedCastUsesCircleAndLeonardRisk(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos pos = placeCauldron(helper);
        var altar = placeAltar(helper);
        helper.setBlock(CAULDRON.above(4), Blocks.GLASS);
        var pig = helper.spawnWithNoFreeWill(EntityType.PIG, CAULDRON.south());
        var farPig = helper.spawnWithNoFreeWill(EntityType.PIG, CAULDRON.east(17));
        var highPig = helper.spawnWithNoFreeWill(EntityType.PIG, CAULDRON.above(5));
        var zombie = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, CAULDRON.north());
        var leonard = helper.spawnWithNoFreeWill(WitcheryEntities.LEONARD.get(), CAULDRON.south(3));

        var area = new AABB(pos).inflate(22);
        var subjects = List.of(pig, farPig, highPig, zombie, leonard);
        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(
                level.getEntitiesOfClass(LivingEntity.class, area).containsAll(subjects),
                "The cauldron can query all the test mobs")).thenExecute(() -> {
            leonard.setHealth(1);
            prepareGoldenChalk(helper, pos);
            finishCast(helper, pos, altar, 4000, 512);
            assertEffect(helper, pig, MobEffects.MOVEMENT_SLOWDOWN, 1200, 1);
            helper.assertTrue(zombie.getActiveEffects().isEmpty() && farPig.getActiveEffects().isEmpty()
                            && highPig.getActiveEffects().isEmpty(),
                    "The misfortune affects nearby non-hostile creatures in the original horizontal and vertical range");
            helper.assertTrue(altar.power() == 1130, "The completed cast pays exactly 2,870 without circles");
            helper.assertItemEntityCountIs(WitcheryItems.getRequired("chalkheart").get(), CAULDRON.above(), 2, 1);
            pig.removeAllEffects();

            setSmallCircle(helper, pos, WitcheryBlocks.getRequired("circleglyphritual").get());
            prepareGoldenChalk(helper, pos);
            finishCast(helper, pos, altar, 4000, 512);
            helper.assertTrue(altar.power() == 1540 && pig.getActiveEffects().isEmpty(),
                    "A white circle removes the risk and discounts the cost to 2,460 when Leonard has one health");

            leonard.setHealth(2);
            prepareGoldenChalk(helper, pos);
            finishCast(helper, pos, altar, 4000, 512);
            assertEffect(helper, pig, MobEffects.MOVEMENT_SLOWDOWN, 1200, 1);
            helper.assertTrue(leonard.getActiveEffects().isEmpty(), "A healthy nearby Leonard restores risk but is immune to the effect");
            leonard.discard();
            pig.removeAllEffects();

            setSmallCircle(helper, pos, WitcheryBlocks.getRequired("circleglyphinfernal").get());
            prepareGoldenChalk(helper, pos);
            finishCast(helper, pos, altar, 4000, 3840);
            assertEffect(helper, pig, WitcheryEffects.INSANITY.get(), 3600, 2);
            helper.assertTrue(altar.power() == 1889, "The Infernal discount lowers the cost to 2,111 and raises the misfortune roll");
        }).thenSucceed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_cauldron_lightning")
    public static void legacyLightningOnlyFollowsCompletedPoweredCasts(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos pos = placeCauldron(helper);
        var altar = placeAltar(helper);
        var pig = helper.spawnWithNoFreeWill(EntityType.PIG, CAULDRON.south());
        var affectedArea = new AABB(pos).inflate(5);

        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(
                !level.getEntitiesOfClass(pig.getClass(), affectedArea, candidate -> candidate == pig).isEmpty(),
                "The target pig is visible to the cauldron")).thenExecute(() -> {
            prepareGoldenChalk(helper, pos);
            finishCast(helper, pos, altar, 0, 5189);
            helper.assertTrue(cauldron(helper, pos).saveWithFullMetadata().getInt("RitualTicks") == 0
                            && pig.getActiveEffects().isEmpty() && level.getEntitiesOfClass(LightningBolt.class, affectedArea).isEmpty(),
                    "An unpowered cast keeps its brew without a lightning or potion penalty");

            prepareCast(helper, pos, "mutandis", WitcheryIngredient.MANDRAKE_ROOT.item().getDefaultInstance(),
                    WitcheryIngredient.EXHALE_OF_THE_HORNED_ONE.item().getDefaultInstance());
            finishCast(helper, pos, altar, 0, 5189);
        }).thenWaitUntil(() -> helper.assertItemEntityCountIs(WitcheryIngredient.MUTANDIS.item(), CAULDRON.above(), 2, 6))
                .thenExecute(() -> {
                    helper.assertTrue(pig.getActiveEffects().isEmpty() && level.getEntitiesOfClass(LightningBolt.class, affectedArea).isEmpty(),
                            "Completing a free recipe skips misfortune even with no chalk circles");
                    prepareGoldenChalk(helper, pos);
                    finishCast(helper, pos, altar, 4000, 5189);
                    assertEffect(helper, pig, WitcheryEffects.PARALYSIS.get(), 200, 2);
                }).thenWaitUntil(() -> {
                    var bolts = level.getEntitiesOfClass(LightningBolt.class, affectedArea);
                    var actual = bolts.stream().map(Entity::blockPosition).collect(Collectors.toSet());
                    var expected = Set.of(pos.offset(-2, 0, -2), pos.offset(-4, 0, -3), pos.offset(-1, 0, 3), pos.offset(-1, 0, -3));
                    helper.assertTrue(bolts.size() == 4 && actual.equals(expected), "The original roll sequence produces four offset lightning bolts");
                    bolts.forEach(Entity::discard);
                }).thenSucceed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_cauldron_summon_risk")
    public static void aSummonedLeonardDoesNotRaiseItsOwnCastRisk(GameTestHelper helper) {
        BlockPos pos = placeCauldron(helper);
        var altar = placeAltar(helper);
        var pig = helper.spawn(EntityType.PIG, CAULDRON.south());
        setSmallCircle(helper, pos, WitcheryBlocks.getRequired("circleglyphritual").get());
        prepareCast(helper, pos, "summon_leonard", new ItemStack(Items.NETHER_WART),
                WitcheryIngredient.TEAR_OF_THE_GODDESS.item().getDefaultInstance(),
                WitcheryIngredient.DIAMOND_VAPOUR.item().getDefaultInstance(), new ItemStack(Items.DIAMOND), new ItemStack(Items.NETHER_STAR));
        var level = helper.getLevel();
        var surroundings = new AABB(pos).inflate(2);
        helper.startSequence().thenWaitUntil(() -> {
            helper.assertTrue(level.getEntity(pig.getUUID()) == pig, "The nearby pig is tracked before the cast");
        }).thenExecute(() -> finishCast(helper, pos, altar, 20000, 512)).thenWaitUntil(() -> {
            var summoned = level.getEntitiesOfClass(Leonard.class, surroundings);
            helper.assertTrue(summoned.size() == 1, "Exactly one Leonard is tracked after the cast");
            helper.assertTrue(pig.getActiveEffects().isEmpty(),
                    "Risk is based on Leonard's presence before the ritual produces its result");
            summoned.forEach(Entity::discard);
        }).thenSucceed();
    }

    private static BlockPos placeCauldron(GameTestHelper helper) {
        helper.setBlock(CAULDRON.below(2), Blocks.NETHERRACK);
        helper.setBlock(CAULDRON.below(), Blocks.FIRE);
        helper.setBlock(CAULDRON, WitcheryBlocks.getRequired("cauldron").get());
        return helper.absolutePos(CAULDRON);
    }

    private static void setSmallCircle(GameTestHelper helper, BlockPos center, Block glyph) {
        var level = helper.getLevel();
        for (BlockPos offset : RitualCircle.SMALL.offsets()) {
            BlockPos pos = center.offset(offset);
            level.setBlockAndUpdate(pos.below(), Blocks.STONE.defaultBlockState());
            level.setBlockAndUpdate(pos, glyph.defaultBlockState());
        }
    }

    private static AltarBlockEntity placeAltar(GameTestHelper helper) {
        BlockPos core = CAULDRON.east(6);
        var block = WitcheryBlocks.getRequired("altar").get();
        for (int x = 0; x < 3; x++) {
            for (int z = 0; z < 2; z++) helper.setBlock(core.offset(x, 0, z), block);
        }
        return (AltarBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(core));
    }

    private static WitchesCauldronBlockEntity cauldron(GameTestHelper helper, BlockPos pos) {
        return (WitchesCauldronBlockEntity) helper.getLevel().getBlockEntity(pos);
    }

    private static void prepareGoldenChalk(GameTestHelper helper, BlockPos pos) {
        prepareCast(helper, pos, "golden_chalk", WitcheryIngredient.MANDRAKE_ROOT.item().getDefaultInstance(),
                new ItemStack(Items.GOLD_NUGGET));
    }

    private static void prepareCast(GameTestHelper helper, BlockPos pos, String recipe, ItemStack... offerings) {
        var cauldron = cauldron(helper, pos);
        var saved = cauldron.saveWithFullMetadata();
        saved.put("Tank", new FluidStack(Fluids.WATER, 3000).writeToNBT(new CompoundTag()));
        var items = new ListTag();
        for (var stack : offerings) items.add(stack.save(new CompoundTag()));
        saved.put("Ingredients", items);
        saved.putInt("HeatTicks", 100);
        saved.putInt("RitualTicks", 209);
        saved.putString("Ritual", "witchery:" + recipe);
        cauldron.load(saved);
    }

    private static void finishCast(GameTestHelper helper, BlockPos pos, AltarBlockEntity altar, float power, long seed) {
        setAltarPower(altar, power);
        helper.getLevel().getRandom().setSeed(seed);
        tickCauldron(helper, cauldron(helper, pos), 20);
    }

    private static void tickCauldron(GameTestHelper helper, WitchesCauldronBlockEntity bowl, int ticks) {
        var level = helper.getLevel();
        for (int tick = 0; tick < ticks; tick++) {
            WitchesCauldronBlockEntity.serverTick(level, bowl.getBlockPos(), bowl.getBlockState(), bowl);
        }
    }

    private static void setAltarPower(AltarBlockEntity altar, float power) {
        var stored = altar.saveWithFullMetadata();
        stored.putFloat("Power", power);
        altar.load(stored);
    }

    private static void assertEffect(GameTestHelper helper, LivingEntity entity, MobEffect type, int duration, int amplifier) {
        var effect = entity.getEffect(type);
        helper.assertTrue(effect != null && effect.getDuration() == duration && effect.getAmplifier() == amplifier,
                "Expected " + type.getDescriptionId() + " for " + duration + " ticks at amplifier " + amplifier
                        + " but received " + entity.getActiveEffects());
    }
}
