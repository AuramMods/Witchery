package art.arcane.witchery.crafting;

import art.arcane.witchery.brewing.LousePotion;
import art.arcane.witchery.item.BitingBeltItem;
import art.arcane.witchery.registry.WitcheryItems;
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

public final class PotionChargeRecipe extends CustomRecipe {
    private final boolean belt;

    public PotionChargeRecipe(ResourceLocation id, CraftingBookCategory category, boolean belt) {
        super(id, category);
        this.belt = belt;
    }

    @Override
    public boolean matches(CraftingContainer grid, Level level) {
        return ingredients(grid) != null;
    }

    @Override
    public ItemStack assemble(CraftingContainer grid, RegistryAccess registries) {
        var inputs = ingredients(grid);
        if (inputs == null) return ItemStack.EMPTY;
        ItemStack result = inputs.item.copyWithCount(1);
        if (belt) BitingBeltItem.charge(result, inputs.potion);
        else LousePotion.set(result, inputs.potion.id());
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return belt ? WitcheryRecipes.CHARGE_BITING_BELT.get() : WitcheryRecipes.CHARGE_LOUSE.get();
    }

    @Nullable
    private Ingredients ingredients(CraftingContainer grid) {
        var chargeable = WitcheryItems.getRequired(belt ? "bitingbelt" : "louse").get();
        ItemStack item = ItemStack.EMPTY;
        LousePotion potion = null;
        for (ItemStack stack : grid.getItems()) {
            if (stack.isEmpty()) continue;
            if (item.isEmpty() && stack.is(chargeable)) {
                item = stack;
            } else if (potion == null) {
                potion = LousePotion.fromBottle(stack);
                if (potion == null) return null;
            } else {
                return null;
            }
        }
        return item.isEmpty() || potion == null ? null : new Ingredients(item, potion);
    }

    private record Ingredients(ItemStack item, LousePotion potion) {}
}
