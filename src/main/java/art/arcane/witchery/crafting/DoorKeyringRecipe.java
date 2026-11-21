package art.arcane.witchery.crafting;

import art.arcane.witchery.item.DoorKeyItem;
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

public final class DoorKeyringRecipe extends CustomRecipe {
    public DoorKeyringRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer grid, Level level) {
        int keys = 0;
        int rings = 0;
        for (var stack : grid.getItems()) {
            if (stack.isEmpty()) continue;
            if (WitcheryIngredient.DOOR_KEY.matches(stack)) keys++;
            else if (WitcheryIngredient.DOOR_KEYRING.matches(stack)) rings++;
            else return false;
        }
        return keys > 0 && keys + rings == 2;
    }

    @Override
    public ItemStack assemble(CraftingContainer grid, RegistryAccess registries) {
        var ring = new ItemStack(WitcheryIngredient.DOOR_KEYRING.item());
        for (var stack : grid.getItems()) {
            if (WitcheryIngredient.DOOR_KEYRING.matches(stack) && stack.hasTag()) {
                ring.setTag(stack.getTag().copy());
                break;
            }
        }
        for (var stack : grid.getItems()) {
            if (WitcheryIngredient.DOOR_KEY.matches(stack)) DoorKeyItem.addToRing(ring, stack);
        }
        return ring;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return WitcheryRecipes.DOOR_KEYRING.get();
    }
}
