package art.arcane.witchery.crafting;

import art.arcane.witchery.item.BrewBagItem;
import art.arcane.witchery.registry.WitcheryRecipes;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class DyeBrewBagRecipe extends CustomRecipe {
    public DyeBrewBagRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer grid, Level level) {
        return ingredients(grid) != null;
    }

    @Override
    public ItemStack assemble(CraftingContainer grid, RegistryAccess registries) {
        var ingredients = ingredients(grid);
        if (ingredients == null) return ItemStack.EMPTY;
        var dyed = ingredients.bag().copyWithCount(1);
        BrewBagItem.dye(dyed, ingredients.color());
        return dyed;
    }

    @Nullable
    private static Ingredients ingredients(CraftingContainer grid) {
        ItemStack bag = ItemStack.EMPTY;
        DyeColor color = null;
        for (ItemStack stack : grid.getItems()) {
            if (stack.isEmpty()) continue;
            if (bag.isEmpty() && stack.getItem() instanceof BrewBagItem) bag = stack;
            else if (color == null && stack.getItem() instanceof DyeItem dye) color = dye.getDyeColor();
            else return null;
        }
        return bag.isEmpty() || color == null ? null : new Ingredients(bag, color);
    }

    private record Ingredients(ItemStack bag, DyeColor color) {}

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return WitcheryRecipes.DYE_BREW_BAG.get();
    }
}
