package art.arcane.witchery.crafting;

import art.arcane.witchery.item.LeonardsUrnItem;
import art.arcane.witchery.registry.WitcheryRecipes;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import java.util.ArrayList;

public final class UrnUpgradeRecipe extends CustomRecipe {
    public UrnUpgradeRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer grid, Level level) {
        return matchingUrns(grid) != null;
    }

    @Override
    public ItemStack assemble(CraftingContainer grid, RegistryAccess registryAccess) {
        ItemStack[] pair = matchingUrns(grid);
        if (pair == null) return ItemStack.EMPTY;
        ItemStack first = pair[0];
        ItemStack result = first.copy();
        LeonardsUrnItem.setTier(result, LeonardsUrnItem.tier(first) + 1);
        var contents = new ArrayList<>(LeonardsUrnItem.contents(first));
        contents.addAll(LeonardsUrnItem.contents(pair[1]));
        LeonardsUrnItem.saveContents(result, contents);
        return result;
    }

    private static ItemStack[] matchingUrns(CraftingContainer grid) {
        ItemStack first = ItemStack.EMPTY;
        ItemStack second = ItemStack.EMPTY;
        for (int slot = 0; slot < grid.getContainerSize(); slot++) {
            ItemStack stack = grid.getItem(slot);
            if (stack.isEmpty()) continue;
            if (!(stack.getItem() instanceof LeonardsUrnItem) || !second.isEmpty()) return null;
            if (first.isEmpty()) first = stack;
            else second = stack;
        }
        if (second.isEmpty()) return null;
        if (LeonardsUrnItem.tier(first) < LeonardsUrnItem.tier(second)) {
            ItemStack swap = first;
            first = second;
            second = swap;
        }
        return LeonardsUrnItem.tier(first) < 3 && LeonardsUrnItem.tier(second) == 0
                ? new ItemStack[]{first, second} : null;
    }

    @Override public boolean canCraftInDimensions(int width, int height) { return width * height >= 2; }
    @Override public RecipeSerializer<?> getSerializer() { return WitcheryRecipes.URN_UPGRADE.get(); }
}
