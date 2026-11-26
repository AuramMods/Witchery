package art.arcane.witchery.crafting;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.WitchWoodType;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class LegacyCraftingTests {
    @GameTest(template = "qa_coven_arena")
    public static void originalMaterialConversionsUseTheOriginalCounts(GameTestHelper helper) {
        var grid = grid();
        grid.setItem(0, new ItemStack(Items.BONE));
        for (int slot = 1; slot < 9; slot++) {
            grid.setItem(slot, new ItemStack(WitcheryIngredient.WOOD_ASH.item()));
            if (slot % 2 == 0) assertResult(helper, grid, Items.BONE_MEAL, 3 + slot / 2);
        }
        grid.clearContent();
        grid.setItem(4, new ItemStack(WitcheryIngredient.CREEPER_HEART.item()));
        assertResult(helper, grid, Items.GUNPOWDER, 5);

        grid.setItem(4, new ItemStack(WitcheryIngredient.INFERNAL_BLOOD.item()));
        grid.setItem(8, new ItemStack(Items.GRAVEL));
        assertResult(helper, grid, Items.NETHERRACK, 1);

        grid.setItem(0, new ItemStack(Items.SPIDER_EYE));
        grid.setItem(4, new ItemStack(Items.POISONOUS_POTATO));
        grid.setItem(8, new ItemStack(Items.POTATO));
        assertResult(helper, grid, Items.POISONOUS_POTATO, 2);
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void originalMutagensKeepTheCookingPriority(GameTestHelper helper) {
        var grid = grid();
        grid.setItem(0, new ItemStack(WitcheryIngredient.MUTANDIS.item()));
        grid.setItem(2, new ItemStack(Items.ROTTEN_FLESH));
        assertResult(helper, grid, WitcheryIngredient.ODD_PORKCHOP_RAW.item(), 1);
        grid.setItem(2, new ItemStack(Items.CHICKEN));
        assertResult(helper, grid, Items.PORKCHOP, 1);
        grid.setItem(2, new ItemStack(Items.COOKED_PORKCHOP));
        assertResult(helper, grid, Items.COOKED_BEEF, 1);

        grid.setItem(0, new ItemStack(WitcheryIngredient.MUTANDIS_EXTREMIS.item()));
        grid.setItem(2, new ItemStack(Items.ROTTEN_FLESH));
        assertResult(helper, grid, WitcheryIngredient.ODD_PORKCHOP_COOKED.item(), 1);
        grid.setItem(2, new ItemStack(Items.CHICKEN));
        assertResult(helper, grid, Items.COOKED_PORKCHOP, 1);
        grid.setItem(2, new ItemStack(Items.COOKED_BEEF));
        assertResult(helper, grid, Items.COOKED_CHICKEN, 1);
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void witchwoodFallbackKeepsTheSpeciesDoorRecipes(GameTestHelper helper) {
        var grid = grid();
        var wood = WitcheryBlocks.getRequired("witchwood").get();
        var rowan = WitchWoodType.ROWAN.stack(wood);
        var alder = WitchWoodType.ALDER.stack(wood);
        fillDoor(grid, rowan);
        assertResult(helper, grid, WitcheryIngredient.ROWAN_DOOR.item(), 1);
        fillDoor(grid, alder);
        assertResult(helper, grid, WitcheryIngredient.ALDER_DOOR.item(), 1);
        fillDoor(grid, WitchWoodType.HAWTHORN.stack(wood));
        assertResult(helper, grid, Items.OAK_DOOR, 1);
        fillDoor(grid, new ItemStack(Items.SPRUCE_PLANKS));
        assertResult(helper, grid, Items.SPRUCE_DOOR, 3);
        grid.setItem(8, rowan);
        assertResult(helper, grid, Items.OAK_DOOR, 1);

        grid.clearContent();
        grid.setItem(6, rowan);
        grid.setItem(7, new ItemStack(Items.SPRUCE_PLANKS));
        grid.setItem(8, alder);
        assertResult(helper, grid, Items.OAK_SLAB, 6);
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void originalDreamweavesRequireExtendedSplashPotions(GameTestHelper helper) {
        var grid = grid();
        grid.setItem(1, new ItemStack(WitcheryIngredient.DIAMOND_VAPOUR.item()));
        grid.setItem(4, new ItemStack(Items.ITEM_FRAME));
        grid.setItem(6, new ItemStack(Items.FEATHER));
        grid.setItem(7, new ItemStack(WitcheryIngredient.TORMENTED_TWINE.item()));
        grid.setItem(8, new ItemStack(Items.FEATHER));
        grid.setItem(3, new ItemStack(WitcheryIngredient.FANCIFUL_THREAD.item()));
        grid.setItem(5, new ItemStack(WitcheryIngredient.FANCIFUL_THREAD.item()));
        assertWeave(helper, grid, WitcheryIngredient.DREAMWEAVE_OF_FLEET_FEET.item(),
                Potions.SWIFTNESS, Potions.LONG_SWIFTNESS, Potions.LONG_SLOWNESS);
        assertWeave(helper, grid, WitcheryIngredient.DREAMWEAVE_OF_THE_IRON_ARM.item(),
                Potions.STRENGTH, Potions.LONG_STRENGTH, Potions.LONG_WEAKNESS);

        grid.setItem(3, new ItemStack(WitcheryIngredient.TORMENTED_TWINE.item()));
        grid.setItem(5, new ItemStack(WitcheryIngredient.TORMENTED_TWINE.item()));
        assertWeave(helper, grid, WitcheryIngredient.DREAMWEAVE_OF_NIGHTMARES.item(),
                Potions.POISON, Potions.LONG_POISON, Potions.LONG_NIGHT_VISION);
        helper.succeed();
    }

    private static void assertWeave(GameTestHelper helper, CraftingContainer grid, ItemLike weave, Potion normal,
                                    Potion extended, Potion supporting) {
        grid.setItem(0, PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), normal));
        grid.setItem(2, PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), supporting));
        var level = helper.getLevel();
        helper.assertTrue(level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, grid, level).isEmpty(),
                "A short splash potion cannot craft " + weave);
        grid.setItem(0, PotionUtils.setPotion(new ItemStack(Items.SPLASH_POTION), extended));
        assertResult(helper, grid, weave, 1);
    }

    private static void fillDoor(CraftingContainer grid, ItemStack plank) {
        for (int slot = 0; slot < 9; slot++) {
            grid.setItem(slot, slot % 3 == 0 ? ItemStack.EMPTY : plank.copy());
        }
    }

    private static CraftingContainer grid() {
        return new TransientCraftingContainer(new AbstractContainerMenu(MenuType.CRAFTING, 0) {
            @Override public ItemStack quickMoveStack(Player player, int slot) { return ItemStack.EMPTY; }
            @Override public boolean stillValid(Player player) { return true; }
        }, 3, 3);
    }

    private static void assertResult(GameTestHelper helper, CraftingContainer grid, ItemLike item, int count) {
        var level = helper.getLevel();
        var result = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, grid, level)
                .map(recipe -> recipe.assemble(grid, level.registryAccess()))
                .orElse(ItemStack.EMPTY);
        helper.assertTrue(result.is(item.asItem()) && result.getCount() == count,
                "Expected " + new ItemStack(item, count) + " from " + grid.getItems() + ", got " + result);
    }
}
