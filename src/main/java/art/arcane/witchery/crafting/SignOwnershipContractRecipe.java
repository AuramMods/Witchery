package art.arcane.witchery.crafting;

import art.arcane.witchery.item.OwnershipContractItem;
import art.arcane.witchery.item.TaglockItem;
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

public final class SignOwnershipContractRecipe extends CustomRecipe {
    public SignOwnershipContractRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer grid, Level level) {
        return inputs(grid) != null;
    }

    @Override
    public ItemStack assemble(CraftingContainer grid, RegistryAccess registries) {
        Inputs inputs = inputs(grid);
        if (inputs == null) return ItemStack.EMPTY;
        ItemStack signed = inputs.contract.copyWithCount(1);
        TaglockItem.copyBinding(inputs.taglock, signed);
        return signed;
    }

    @Nullable
    private static Inputs inputs(CraftingContainer grid) {
        ItemStack contract = ItemStack.EMPTY;
        ItemStack taglock = ItemStack.EMPTY;
        for (int slot = 0; slot < grid.getContainerSize(); slot++) {
            ItemStack stack = grid.getItem(slot);
            if (stack.isEmpty()) continue;
            if (stack.getItem() instanceof OwnershipContractItem && contract.isEmpty()) {
                contract = stack;
            } else if (stack.getItem() instanceof TaglockItem && TaglockItem.isBound(stack) && taglock.isEmpty()) {
                taglock = stack;
            } else {
                return null;
            }
        }
        return contract.isEmpty() || taglock.isEmpty() ? null : new Inputs(contract, taglock);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return WitcheryRecipes.SIGN_OWNERSHIP_CONTRACT.get();
    }

    private record Inputs(ItemStack contract, ItemStack taglock) {}
}
