package art.arcane.witchery.crafting;

import art.arcane.witchery.item.ImpSpellContractItem;
import art.arcane.witchery.item.TaglockItem;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryItems;
import art.arcane.witchery.registry.WitcheryRecipes;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;
import java.util.function.Supplier;

public final class TaglockBindingRecipe extends CustomRecipe {
    public enum Binding {
        OWNERSHIP(WitcheryIngredient.CONTRACT_OF_OWNERSHIP::item, () -> WitcheryRecipes.SIGN_OWNERSHIP_CONTRACT.get()),
        IMP_SPELL(stack -> ImpSpellContractItem.spellFor(stack) != null, () -> WitcheryRecipes.SIGN_IMP_SPELL_CONTRACT.get()),
        EARTH_POPPET(() -> WitcheryItems.getRequired("earth_protection_poppet").get(), () -> WitcheryRecipes.BIND_EARTH_POPPET.get()),
        WATER_POPPET(() -> WitcheryItems.getRequired("water_protection_poppet").get(), () -> WitcheryRecipes.BIND_WATER_POPPET.get()),
        FIRE_POPPET(() -> WitcheryItems.getRequired("fire_protection_poppet").get(), () -> WitcheryRecipes.BIND_FIRE_POPPET.get()),
        HUNGER_POPPET(() -> WitcheryItems.getRequired("hunger_protection_poppet").get(), () -> WitcheryRecipes.BIND_HUNGER_POPPET.get()),
        TOOL_POPPET(() -> WitcheryItems.getRequired("tool_protection_poppet").get(), () -> WitcheryRecipes.BIND_TOOL_POPPET.get()),
        ARMOR_POPPET(() -> WitcheryItems.getRequired("armor_protection_poppet").get(), () -> WitcheryRecipes.BIND_ARMOR_POPPET.get()),
        DEATH_POPPET(() -> WitcheryItems.getRequired("death_protection_poppet").get(), () -> WitcheryRecipes.BIND_DEATH_POPPET.get()),
        VOODOO_POPPET(() -> WitcheryItems.getRequired("voodoo_poppet").get(), () -> WitcheryRecipes.BIND_VOODOO_POPPET.get()),
        VOODOO_PROTECTION(() -> WitcheryItems.getRequired("voodoo_protection_poppet").get(), () -> WitcheryRecipes.BIND_VOODOO_PROTECTION.get()),
        VAMPIRIC_POPPET(() -> WitcheryItems.getRequired("vampiric_poppet").get(), () -> WitcheryRecipes.BIND_VAMPIRIC_POPPET.get()),
        POPPET_PROTECTION(() -> WitcheryItems.getRequired("poppet_protection").get(), () -> WitcheryRecipes.BIND_POPPET_PROTECTION.get()),
        PLAYER_COMPASS(() -> WitcheryItems.getRequired("playercompass").get(), () -> WitcheryRecipes.BIND_PLAYER_COMPASS.get());

        private final Predicate<ItemStack> accepts;
        private final Supplier<RecipeSerializer<?>> serializer;

        Binding(Supplier<Item> item, Supplier<RecipeSerializer<?>> serializer) {
            this(stack -> stack.is(item.get()), serializer);
        }

        Binding(Predicate<ItemStack> accepts, Supplier<RecipeSerializer<?>> serializer) {
            this.accepts = accepts;
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
        if (binding == Binding.IMP_SPELL && !(signed.getItem() instanceof ImpSpellContractItem)) {
            signed = WitcheryIngredient.loadInventoryStack(signed.save(new CompoundTag()));
        }
        TaglockItem.copyBinding(inputs.taglock, signed);
        if (!inputs.secondTaglock.isEmpty()) TaglockItem.copyBinding(inputs.secondTaglock, signed, 2);
        return signed;
    }

    @Nullable
    private Inputs inputs(CraftingContainer grid) {
        ItemStack bindable = ItemStack.EMPTY;
        ItemStack taglock = ItemStack.EMPTY;
        ItemStack secondTaglock = ItemStack.EMPTY;
        boolean needsSecond = binding == Binding.VAMPIRIC_POPPET;
        for (int slot = 0; slot < grid.getContainerSize(); slot++) {
            ItemStack stack = grid.getItem(slot);
            if (stack.isEmpty()) continue;
            if (bindable.isEmpty() && binding.accepts.test(stack)) {
                bindable = stack;
            } else if (stack.getItem() instanceof TaglockItem && TaglockItem.isBound(stack)) {
                if (taglock.isEmpty()) taglock = stack;
                else if (needsSecond && secondTaglock.isEmpty()) secondTaglock = stack;
                else return null;
            } else {
                return null;
            }
        }
        return bindable.isEmpty() || taglock.isEmpty() || (needsSecond && secondTaglock.isEmpty())
                ? null : new Inputs(bindable, taglock, secondTaglock);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= (binding == Binding.VAMPIRIC_POPPET ? 3 : 2);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return binding.serializer.get();
    }

    private record Inputs(ItemStack bindable, ItemStack taglock, ItemStack secondTaglock) {}
}
