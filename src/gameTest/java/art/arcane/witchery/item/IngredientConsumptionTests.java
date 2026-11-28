package art.arcane.witchery.item;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryItems;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.PlayLevelSoundEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class IngredientConsumptionTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_ingredient_consumption")
    public static void ingredientsCompleteWithOneOriginalCue(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "IngredientFood"));
        player.setPos(Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(5, 3, 5))));
        var sounds = new ArrayList<PlayLevelSoundEvent.AtPosition>();
        Consumer<PlayLevelSoundEvent.AtPosition> listener = event -> {
            if (event.getPosition().equals(player.position())) sounds.add(event);
        };
        MinecraftForge.EVENT_BUS.addListener(listener);
        try {
            player.getFoodData().setFoodLevel(12);
            player.getFoodData().setSaturation(0);
            for (String item : List.of("redstone_soup", "wormy_apple", "globe_artichoke")) {
                var stack = new ItemStack(WitcheryItems.getRequired(item).get(), 2);
                level.random.setSeed(42);
                stack.finishUsingItem(level, player);
                helper.assertTrue(stack.getCount() == 1, "Survival consumes one serving of " + item);
                assertBurp(helper, sounds, 0.9727563F);
                helper.assertTrue(level.random.nextInt(1000) == 763, item + " uses one world float for the original burp");
                if (item.equals("redstone_soup")) {
                    helper.assertTrue(stack.getRarity() == Rarity.UNCOMMON && player.hasEffect(MobEffects.HEALTH_BOOST),
                            "Soup applies Health Boost and retains its original rarity");
                } else if (item.equals("wormy_apple")) {
                    var poison = player.getEffect(MobEffects.POISON);
                    helper.assertTrue(stack.getUseAnimation() == UseAnim.EAT && player.getFoodData().getFoodLevel() == 12
                            && poison != null && poison.getDuration() == 60 && poison.getAmplifier() == 0,
                            "A Wormy Apple applies its fixed poison without filling hunger");
                } else {
                    var hunger = player.getEffect(MobEffects.HUNGER);
                    helper.assertTrue(player.getFoodData().getFoodLevel() == 20 && hunger != null
                                    && hunger.getDuration() == 480 && hunger.getAmplifier() == 2,
                            "An Artichoke uses the actual eight restored hunger points");
                }
            }
        } finally {
            MinecraftForge.EVENT_BUS.unregister(listener);
            player.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_ingredient_consumption")
    public static void purifiedMilkDrawsFromTheWorldBeforePlayingItsCue(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "IngredientMilk"));
        var item = WitcheryItems.getRequired("purified_milk").get();
        try {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 1));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 400));
            var milk = new ItemStack(item, 2);
            level.random.setSeed(4096);
            milk.finishUsingItem(level, player);
            helper.assertTrue(milk.getCount() == 1 && player.hasEffect(MobEffects.MOVEMENT_SPEED)
                            && !player.hasEffect(MobEffects.MOVEMENT_SLOWDOWN) && level.random.nextInt(1000) == 951,
                    "Milk removes the seeded effect with the world roll before the completion cue");

            player.removeAllEffects();
            player.getAbilities().instabuild = true;
            level.random.setSeed(42);
            milk.finishUsingItem(level, player);
            var expected = new java.util.Random(42);
            expected.nextInt(2);
            expected.nextFloat();
            helper.assertTrue(milk.getCount() == 1 && level.random.nextInt(1000) == expected.nextInt(1000),
                    "Creative preserves the stack, and even an empty effect list takes the first purification roll");
        } finally {
            player.discard();
        }
        helper.succeed();
    }

    private static void assertBurp(GameTestHelper helper, List<PlayLevelSoundEvent.AtPosition> cues, float pitch) {
        helper.assertTrue(cues.size() == 1, "The completed serving makes one sound");
        var cue = cues.remove(0);
        helper.assertTrue(cue.getSound() != null && cue.getSound().value() == SoundEvents.PLAYER_BURP
                        && cue.getSource() == SoundSource.PLAYERS && cue.getNewVolume() == 0.5F
                        && Math.abs(cue.getNewPitch() - pitch) < 0.000001F,
                "The burp uses the original category, volume and pitch");
    }
}
