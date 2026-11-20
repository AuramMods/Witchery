package art.arcane.witchery.crafting;

import art.arcane.witchery.item.WitchClothingItem;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryRecipes;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public final class ClothingRepairRecipe extends CustomRecipe {
    public ClothingRepairRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer grid, Level level) {
        return !clothingToRepair(grid).isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingContainer grid, RegistryAccess registries) {
        ItemStack result = clothingToRepair(grid).copyWithCount(1);
        if (!result.isEmpty()) result.setDamageValue(0);
        return result;
    }

    private static ItemStack clothingToRepair(CraftingContainer grid) {
        ItemStack clothing = ItemStack.EMPTY;
        int leather = 0;
        for (ItemStack stack : grid.getItems()) {
            if (stack.isEmpty()) continue;
            if (stack.is(WitcheryIngredient.IMPREGNATED_LEATHER.item())) {
                leather++;
            } else if (stack.getItem() instanceof WitchClothingItem && clothing.isEmpty()) {
                clothing = stack;
            } else {
                return ItemStack.EMPTY;
            }
        }
        return clothing.getItem() instanceof WitchClothingItem item && leather == item.repairLeather()
                ? clothing : ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 4;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return WitcheryRecipes.CLOTHING_REPAIR.get();
    }
}
