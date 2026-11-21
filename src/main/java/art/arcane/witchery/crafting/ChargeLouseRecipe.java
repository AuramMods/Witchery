package art.arcane.witchery.crafting;

import art.arcane.witchery.brewing.LousePotion;
import art.arcane.witchery.item.ParasyticLouseItem;
import art.arcane.witchery.registry.WitcheryRecipes;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public final class ChargeLouseRecipe extends CustomRecipe {
    public ChargeLouseRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer grid, Level level) {
        return ingredients(grid) != null;
    }

    @Override
    public ItemStack assemble(CraftingContainer grid, RegistryAccess registries) {
        var inputs = ingredients(grid);
        if (inputs == null) return ItemStack.EMPTY;
        ItemStack louse = inputs.louse.copyWithCount(1);
        LousePotion.set(louse, inputs.potion.id());
        return louse;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return WitcheryRecipes.CHARGE_LOUSE.get();
    }

    @Nullable
    private static Ingredients ingredients(CraftingContainer grid) {
        ItemStack louse = ItemStack.EMPTY;
        LousePotion potion = null;
        for (ItemStack stack : grid.getItems()) {
            if (stack.isEmpty()) continue;
            if (louse.isEmpty() && stack.getItem() instanceof ParasyticLouseItem) {
                louse = stack;
            } else if (potion == null) {
                potion = LousePotion.fromBottle(stack);
                if (potion == null) return null;
            } else {
                return null;
            }
        }
        return louse.isEmpty() || potion == null ? null : new Ingredients(louse, potion);
    }

    private record Ingredients(ItemStack louse, LousePotion potion) {}
}
