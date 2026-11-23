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
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
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
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class CauldronMisfortuneTests {
    private static final BlockPos CAULDRON = new BlockPos(6, 4, 5);

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
        finishCast(helper, pos, altar, 20000, 512);

        var summoned = helper.getLevel().getEntitiesOfClass(Leonard.class, new AABB(pos).inflate(2));
        helper.assertTrue(summoned.size() == 1 && pig.getActiveEffects().isEmpty(),
                "Risk is based on Leonard's presence before the ritual produces its result");
        summoned.forEach(Entity::discard);
        helper.succeed();
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

    private static void finishCast(GameTestHelper helper, BlockPos pos, AltarBlockEntity altar, int power, long seed) {
        var stored = altar.saveWithFullMetadata();
        stored.putFloat("Power", power);
        altar.load(stored);
        var level = helper.getLevel();
        level.getRandom().setSeed(seed);
        WitchesCauldronBlockEntity.serverTick(level, pos, level.getBlockState(pos), cauldron(helper, pos));
    }

    private static void assertEffect(GameTestHelper helper, LivingEntity entity, MobEffect type, int duration, int amplifier) {
        var effect = entity.getEffect(type);
        helper.assertTrue(effect != null && effect.getDuration() == duration && effect.getAmplifier() == amplifier,
                "Expected " + type.getDescriptionId() + " for " + duration + " ticks at amplifier " + amplifier
                        + " but received " + entity.getActiveEffects());
    }
}
