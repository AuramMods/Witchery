package art.arcane.witchery.crafting;

import art.arcane.witchery.item.TaglockItem;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryItems;
import art.arcane.witchery.registry.WitcheryRecipes;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public final class TaglockBindingRecipe extends CustomRecipe {
    public enum Binding {
        OWNERSHIP(WitcheryIngredient.CONTRACT_OF_OWNERSHIP::item, () -> WitcheryRecipes.SIGN_OWNERSHIP_CONTRACT.get()),
        EARTH_POPPET(() -> WitcheryItems.getRequired("earth_protection_poppet").get(), () -> WitcheryRecipes.BIND_EARTH_POPPET.get()),
        WATER_POPPET(() -> WitcheryItems.getRequired("water_protection_poppet").get(), () -> WitcheryRecipes.BIND_WATER_POPPET.get()),
        FIRE_POPPET(() -> WitcheryItems.getRequired("fire_protection_poppet").get(), () -> WitcheryRecipes.BIND_FIRE_POPPET.get()),
        HUNGER_POPPET(() -> WitcheryItems.getRequired("hunger_protection_poppet").get(), () -> WitcheryRecipes.BIND_HUNGER_POPPET.get()),
        TOOL_POPPET(() -> WitcheryItems.getRequired("tool_protection_poppet").get(), () -> WitcheryRecipes.BIND_TOOL_POPPET.get()),
        ARMOR_POPPET(() -> WitcheryItems.getRequired("armor_protection_poppet").get(), () -> WitcheryRecipes.BIND_ARMOR_POPPET.get()),
        DEATH_POPPET(() -> WitcheryItems.getRequired("death_protection_poppet").get(), () -> WitcheryRecipes.BIND_DEATH_POPPET.get()),
        VOODOO_POPPET(() -> WitcheryItems.getRequired("voodoo_poppet").get(), () -> WitcheryRecipes.BIND_VOODOO_POPPET.get()),
        VOODOO_PROTECTION(() -> WitcheryItems.getRequired("voodoo_protection_poppet").get(), () -> WitcheryRecipes.BIND_VOODOO_PROTECTION.get());

        private final Supplier<Item> item;
        private final Supplier<RecipeSerializer<?>> serializer;

        Binding(Supplier<Item> item, Supplier<RecipeSerializer<?>> serializer) {
            this.item = item;
            this.serializer = serializer;
        }
    }

    private final Binding binding;

    public TaglockBindingRecipe(ResourceLocation id, CraftingBookCategory category, Binding binding) {
        super(id, category);
        this.binding = binding;
    }

    @Override
    public boolean matches(CraftingContainer grid, Level level) {
        return inputs(grid) != null;
    }

    @Override
    public ItemStack assemble(CraftingContainer grid, RegistryAccess registries) {
        Inputs inputs = inputs(grid);
        if (inputs == null) return ItemStack.EMPTY;
        ItemStack signed = inputs.bindable.copyWithCount(1);
        TaglockItem.copyBinding(inputs.taglock, signed);
        return signed;
    }

    @Nullable
    private Inputs inputs(CraftingContainer grid) {
        Item target = binding.item.get();
        ItemStack bindable = ItemStack.EMPTY;
        ItemStack taglock = ItemStack.EMPTY;
        for (int slot = 0; slot < grid.getContainerSize(); slot++) {
            ItemStack stack = grid.getItem(slot);
            if (stack.isEmpty()) continue;
            if (stack.is(target) && bindable.isEmpty()) {
                bindable = stack;
            } else if (stack.getItem() instanceof TaglockItem && TaglockItem.isBound(stack) && taglock.isEmpty()) {
                taglock = stack;
            } else {
                return null;
            }
        }
        return bindable.isEmpty() || taglock.isEmpty() ? null : new Inputs(bindable, taglock);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return binding.serializer.get();
    }

    private record Inputs(ItemStack bindable, ItemStack taglock) {}
}
