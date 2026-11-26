package art.arcane.witchery.crafting;

import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryRecipes;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;

public final class SilverDustRecipe extends ShapelessRecipe {
    private static final TagKey<Item> SILVER_DUSTS = TagKey.create(Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath("forge", "dusts/silver"));

    public SilverDustRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, "", category, ItemStack.EMPTY,
                NonNullList.withSize(9, Ingredient.of(WitcheryIngredient.SILVER_DUST.item())));
    }

    @Override
    public boolean matches(CraftingContainer grid, Level level) {
        return super.matches(grid, level) && outputDust() != Items.AIR;
    }

    @Override
    public ItemStack assemble(CraftingContainer grid, RegistryAccess registries) {
        return getResultItem(registries);
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registries) {
        Item result = outputDust();
        return result == Items.AIR ? ItemStack.EMPTY : new ItemStack(result);
    }

    @Override
    public boolean isIncomplete() {
        return outputDust() == Items.AIR || super.isIncomplete();
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return WitcheryRecipes.SILVER_DUST_CONVERSION.get();
    }

    private static Item outputDust() {
        Item witcheryDust = WitcheryIngredient.SILVER_DUST.item();
        // The original Ore Dictionary conversion used its first external silver dust.
        for (var holder : BuiltInRegistries.ITEM.getTagOrEmpty(SILVER_DUSTS)) {
            Item candidate = holder.value();
            if (candidate != witcheryDust && candidate != Items.AIR) return candidate;
        }
        return Items.AIR;
    }
}
