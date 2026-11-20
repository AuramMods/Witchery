package art.arcane.witchery.crafting;

import art.arcane.witchery.item.VampireBookItem;
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

public final class AssembleVampireBookRecipe extends CustomRecipe {
    public AssembleVampireBookRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer grid, Level level) {
        return !findIncompleteBook(grid).isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingContainer grid, RegistryAccess registries) {
        ItemStack result = findIncompleteBook(grid).copyWithCount(1);
        if (!result.isEmpty()) VampireBookItem.setTornPages(result, VampireBookItem.tornPages(result) + 1);
        return result;
    }

    private static ItemStack findIncompleteBook(CraftingContainer grid) {
        ItemStack book = ItemStack.EMPTY;
        boolean tornPage = false;
        for (ItemStack stack : grid.getItems()) {
            if (stack.isEmpty()) continue;
            if (!tornPage && stack.is(WitcheryIngredient.TORN_VAMPIRE_PAGE.item())) tornPage = true;
            else if (book.isEmpty() && stack.getItem() instanceof VampireBookItem) book = stack;
            else return ItemStack.EMPTY;
        }
        return tornPage && VampireBookItem.tornPages(book) < VampireBookItem.MAX_TORN_PAGES ? book : ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return WitcheryRecipes.ASSEMBLE_VAMPIRE_BOOK.get();
    }
}
