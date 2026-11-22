package art.arcane.witchery.item;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryItems;
import com.mojang.authlib.GameProfile;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class AntidoteTests {
    @GameTest(template = "qa_coven_arena")
    public static void antidoteRequiresTwoStandardPoisonPotions(GameTestHelper helper) throws Exception {
        var grid = new TransientCraftingContainer(new AbstractContainerMenu(MenuType.CRAFTING, 0) {
            @Override
            public ItemStack quickMoveStack(Player player, int slot) {
                return ItemStack.EMPTY;
            }

            @Override
            public boolean stillValid(Player player) {
                return true;
            }
        }, 3, 3);
        grid.setItem(1, new ItemStack(WitcheryIngredient.NULL_CATALYST.item()));
        grid.setItem(3, PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.POISON));
        grid.setItem(7, PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.POISON));
        var level = helper.getLevel();
        var recipe = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, grid, level).orElseThrow();
        var result = recipe.assemble(grid, level.registryAccess());
        var antidote = WitcheryItems.getRequired("potion").get();
        helper.assertTrue(result.is(antidote) && result.getCount() == 2, "The original mixture makes two Antidotes");
        grid.setItem(7, PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.LONG_POISON));
        helper.assertFalse(recipe.matches(grid, level), "A longer Poison potion changes the recipe");
        grid.setItem(7, PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.STRONG_POISON));
        helper.assertFalse(recipe.matches(grid, level), "Poison II is rejected");
        grid.setItem(7, PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), Potions.POISON));
        helper.assertFalse(recipe.matches(grid, level), "Drinkable poison is required");

        var imported = WitcheryIngredient.loadInventoryStack(TagParser.parseTag(
                "{id:\"witchery:potion\",Damage:0s,Count:4b,tag:{Keepsake:1b}}"));
        helper.assertTrue(imported.is(antidote) && imported.getCount() == 4 && imported.getMaxStackSize() == 4
                        && imported.getTag().getBoolean("Keepsake"),
                "Original potion stacks retain their ID, tag, count and four-item limit");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void antidoteCuresPoisonAndWitherAndReturnsBottles(GameTestHelper helper) {
        var level = helper.getLevel();
        var antidote = WitcheryItems.getRequired("potion").get();
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "AntidoteQA"));
        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(antidote, 2));
        try {
            player.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 1));
            player.addEffect(new MobEffectInstance(MobEffects.WITHER, 200, 1));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200));
            var stack = player.getMainHandItem();
            helper.assertTrue(antidote.use(level, player, InteractionHand.MAIN_HAND).getResult().consumesAction()
                            && stack.getUseAnimation() == UseAnim.DRINK && stack.getUseDuration() == 32,
                    "Drinking follows the normal potion use");
            stack = antidote.finishUsingItem(stack, level, player);
            player.stopUsingItem();
            helper.assertTrue(!player.hasEffect(MobEffects.POISON) && !player.hasEffect(MobEffects.WITHER)
                            && player.hasEffect(MobEffects.MOVEMENT_SLOWDOWN),
                    "Drinking removes Poison and Wither and leaves Slowness");
            helper.assertTrue(stack.is(antidote) && stack.getCount() == 1
                            && player.getInventory().countItem(Items.GLASS_BOTTLE) == 1,
                    "Drinking from a stack puts the first bottle in the inventory");
            stack = antidote.finishUsingItem(stack, level, player);
            helper.assertTrue(stack.is(Items.GLASS_BOTTLE) && stack.getCount() == 1
                            && player.getInventory().countItem(Items.GLASS_BOTTLE) == 1,
                    "The last serving replaces the held stack with its bottle");
            helper.succeed();
        } finally {
            player.discard();
        }
    }
}
