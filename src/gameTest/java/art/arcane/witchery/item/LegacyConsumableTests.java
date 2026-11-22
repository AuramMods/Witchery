package art.arcane.witchery.item;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.capability.WitcheryPlayerDataProvider;
import art.arcane.witchery.config.WitcheryServerConfig;
import art.arcane.witchery.infusion.ImpContractEffects;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class LegacyConsumableTests {
    @GameTest(template = "qa_coven_arena")
    public static void artichokeHungerTracksTheFoodActuallyRestored(GameTestHelper helper) {
        var player = player(helper);
        var artichoke = WitcheryIngredient.GLOBE_ARTICHOKE.item();
        try {
            var food = player.getFoodData();
            food.setFoodLevel(19);
            food.setSaturation(1);
            consume(helper, player, new ItemStack(artichoke));
            assertEffect(helper, player, MobEffects.HUNGER, 60, 2);
            helper.assertTrue(food.getFoodLevel() == 20 && food.getSaturationLevel() == 1F,
                    "One missing food point gives three seconds of Hunger and no extra saturation");

            player.removeEffect(MobEffects.HUNGER);
            food.setFoodLevel(8);
            consume(helper, player, new ItemStack(artichoke));
            assertEffect(helper, player, MobEffects.HUNGER, 720, 2);
            helper.assertTrue(food.getFoodLevel() == 20 && food.getSaturationLevel() == 1F,
                    "Twelve missing points give thirty-six seconds of Hunger");

            for (var ingredient : new WitcheryIngredient[]{WitcheryIngredient.GLOBE_ARTICHOKE,
                    WitcheryIngredient.ODD_PORKCHOP_RAW, WitcheryIngredient.ODD_PORKCHOP_COOKED}) {
                var stack = new ItemStack(ingredient.item());
                player.setItemInHand(InteractionHand.MAIN_HAND, stack);
                helper.assertFalse(stack.getItem().use(helper.getLevel(), player, InteractionHand.MAIN_HAND)
                        .getResult().consumesAction(), "Full players cannot eat " + ingredient.path());
            }

            food.setFoodLevel(3);
            consume(helper, player, new ItemStack(WitcheryIngredient.ODD_PORKCHOP_RAW.item()));
            helper.assertTrue(food.getFoodLevel() == 6, "Raw odd pork restores three food points");
            consume(helper, player, new ItemStack(WitcheryIngredient.ODD_PORKCHOP_COOKED.item()));
            helper.assertTrue(food.getFoodLevel() == 14, "Cooked odd pork restores eight food points");
            helper.succeed();
        } finally {
            player.discard();
        }
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_creeper_heart")
    public static void creeperHeartBlastsWithBothTerrainSettings(GameTestHelper helper) {
        var player = player(helper);
        var level = helper.getLevel();
        var center = helper.absolutePos(new BlockPos(6, 6, 6));
        var wool = center.east();
        player.setPos(Vec3.atBottomCenterOf(center));
        var pig = EntityType.PIG.create(level);
        pig.setPos(Vec3.atBottomCenterOf(center.north()));
        level.addFreshEntity(pig);
        var heart = WitcheryIngredient.CREEPER_HEART.item();
        var setting = WitcheryServerConfig.CREEPER_HEART_TERRAIN_DAMAGE;
        boolean original = setting.get();
        try {
            setting.set(false);
            level.setBlockAndUpdate(wool, Blocks.WHITE_WOOL.defaultBlockState());
            var stack = new ItemStack(heart, 2);
            helper.assertTrue(stack.getUseAnimation() == UseAnim.EAT && stack.getUseDuration() == 32,
                    "The Creeper Heart can be eaten without missing hunger");
            consume(helper, player, stack);
            assertEffect(helper, player, MobEffects.FIRE_RESISTANCE, 20, 0);
            helper.assertTrue(level.getBlockState(wool).is(Blocks.WHITE_WOOL) && pig.getHealth() < pig.getMaxHealth(),
                    "The small blast hurts nearby creatures and preserves the marker");

            setting.set(true);
            consume(helper, player, stack);
            helper.assertTrue(level.isEmptyBlock(wool) && stack.isEmpty(), "The large blast destroys the marker and spends one heart");
            helper.succeed();
        } finally {
            setting.set(original);
            player.discard();
            pig.discard();
        }
    }

    @GameTest(template = "qa_coven_arena")
    public static void frozenHeartClearsImpEffectsAndGrantsFireResistance(GameTestHelper helper) {
        var player = player(helper);
        try {
            for (var effect : ImpContractEffects.values()) effect.grant(player);
            consume(helper, player, new ItemStack(WitcheryIngredient.FROZEN_HEART.item()));
            assertEffect(helper, player, MobEffects.FIRE_RESISTANCE, 20, 0);
            for (var effect : ImpContractEffects.values()) {
                helper.assertFalse(effect.isActive(player), "The Frozen Heart clears " + effect);
            }
            helper.succeed();
        } finally {
            player.discard();
        }
    }

    @GameTest(template = "qa_coven_arena")
    public static void legacyBloodDrinksUseTheOriginalVampireRules(GameTestHelper helper) throws Exception {
        var player = player(helper);
        var data = WitcheryPlayerDataProvider.get(player).resolve().orElseThrow();
        try {
            var warm = WitcheryIngredient.loadInventoryStack(TagParser.parseTag(
                    "{id:\"witchery:ingredient\",Damage:163s,Count:2b,tag:{Keepsake:1b}}"));
            var lilith = WitcheryIngredient.loadInventoryStack(TagParser.parseTag(
                    "{id:\"witchery:ingredient\",Damage:164s,Count:3b}"));
            helper.assertTrue(WitcheryIngredient.WARM_BLOOD.matches(warm) && warm.getTag().getBoolean("Keepsake")
                    && WitcheryIngredient.LILITHS_BLOOD.matches(lilith), "Both original drink IDs preserve item data");
            consume(helper, player, warm);
            assertEffect(helper, player, MobEffects.HUNGER, 120, 0);
            helper.assertTrue(data.getVampireLevel() == 0 && warm.getCount() == 1, "Human Warm Blood consumes one dose");
            consume(helper, player, lilith);
            helper.assertTrue(data.getVampireLevel() == 1 && data.getBloodPower() == 125 && data.getHumanBlood() == 0,
                    "Lilith's Blood turns a human with rank-one starting blood");

            data.setBloodPower(400);
            consume(helper, player, warm);
            helper.assertTrue(data.getVampireLevel() == 2 && data.getBloodPower() == 750,
                    "Warm Blood fills the rank-one cap and triggers the existing feeding trial");

            data.setVampireLevel(9);
            consume(helper, player, lilith);
            helper.assertTrue(data.getVampireLevel() == 10 && lilith.getCount() == 1,
                    "A single dose grants the next rank above the book's cap");
            data.setBloodPower(100);
            consume(helper, player, lilith);
            helper.assertTrue(data.getVampireLevel() == 10 && data.getBloodPower() == 2100 && lilith.isEmpty(),
                    "At rank ten the last dose restores 2,000 blood");
            helper.succeed();
        } finally {
            player.discard();
        }
    }

    private static FakePlayer player(GameTestHelper helper) {
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "ConsumableQA"));
        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        return player;
    }

    private static void consume(GameTestHelper helper, FakePlayer player, ItemStack stack) {
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        var item = stack.getItem();
        helper.assertTrue(item.use(helper.getLevel(), player, InteractionHand.MAIN_HAND).getResult().consumesAction(),
                "Use starts consuming " + item);
        player.setItemInHand(InteractionHand.MAIN_HAND, item.finishUsingItem(stack, helper.getLevel(), player));
        player.stopUsingItem();
    }

    private static void assertEffect(GameTestHelper helper, FakePlayer player, MobEffect type, int ticks, int amplifier) {
        var effect = player.getEffect(type);
        helper.assertTrue(effect != null && effect.getDuration() == ticks && effect.getAmplifier() == amplifier,
                "Expected " + type.getDescriptionId() + " for " + ticks + " ticks at amplifier " + amplifier);
    }
}
