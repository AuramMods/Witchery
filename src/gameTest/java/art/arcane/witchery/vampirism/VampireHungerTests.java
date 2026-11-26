package art.arcane.witchery.vampirism;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.capability.WitcheryPlayerDataProvider;
import com.mojang.authlib.GameProfile;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class VampireHungerTests {
    @GameTest(template = "qa_coven_arena")
    public static void hungerUsesTheOriginalPlayerTickOrder(GameTestHelper helper) {
        var nether = helper.getLevel().getServer().getLevel(Level.NETHER);
        var human = player(nether, 0, 400);
        var vampire = player(nether, 5, 400);
        var activeEffect = player(nether, 5, 400);
        var meal = player(nether, 5, 0);
        var mealDuringRefill = player(nether, 5, 400);
        try {
            MobEffects.SATURATION.applyEffectTick(human, 1);
            MobEffects.SATURATION.applyEffectTick(vampire, 1);
            tick(human, 2);
            tick(vampire, 2);
            helper.assertTrue(human.getFoodData().getFoodLevel() == 7 && vampire.getFoodData().getFoodLevel() == 0
                            && vampire.getFoodData().getSaturationLevel() == 0,
                    "The vampire discards a food gain between ticks while the mortal keeps it");

            tick(vampire, 41);
            tick(vampire, 42);
            helper.assertTrue(vampire.getFoodData().getFoodLevel() == 20 && blood(vampire) == 300,
                    "One hundred blood fills the empty bar and the next tick retains that food");

            activeEffect.addEffect(new MobEffectInstance(MobEffects.SATURATION, 1, 1));
            tick(activeEffect, 2);
            tick(activeEffect, 3);
            helper.assertTrue(activeEffect.getFoodData().getFoodLevel() == 7,
                    "A Saturation effect processed during the living tick survives as in the original");

            eatApple(meal, 2);
            helper.assertTrue(meal.getFoodData().getFoodLevel() == 0 && meal.getMainHandItem().isEmpty(),
                    "The vampire finishes its normal meal and discards the food gain");

            eatApple(mealDuringRefill, 10);
            tick(mealDuringRefill, 42);
            helper.assertTrue(mealDuringRefill.getMainHandItem().isEmpty()
                            && mealDuringRefill.getFoodData().getFoodLevel() == 20 && blood(mealDuringRefill) == 300,
                    "A meal finished on the refill tick is discarded before the bar is filled with blood");
        } finally {
            human.discard();
            vampire.discard();
            activeEffect.discard();
            meal.discard();
            mealDuringRefill.discard();
        }
        helper.succeed();
    }

    private static FakePlayer player(ServerLevel level, int rank, int blood) {
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "VampireHunger"));
        var data = WitcheryPlayerDataProvider.get(player).orElseThrow(IllegalStateException::new);
        data.setVampireLevel(rank);
        data.setBloodPower(blood);
        player.getFoodData().setFoodLevel(5);
        player.getFoodData().setSaturation(0);
        player.getFoodData().tick(player);
        return player;
    }

    private static int blood(FakePlayer player) {
        return WitcheryPlayerDataProvider.get(player).orElseThrow(IllegalStateException::new).getBloodPower();
    }

    private static void tick(FakePlayer player, int age) {
        player.tickCount = age;
        player.doTick();
    }

    private static void eatApple(FakePlayer player, int start) {
        var apple = new ItemStack(Items.APPLE);
        player.setItemInHand(InteractionHand.MAIN_HAND, apple);
        player.startUsingItem(InteractionHand.MAIN_HAND);
        for (int age = start; age < start + apple.getUseDuration(); age++) tick(player, age);
    }
}
