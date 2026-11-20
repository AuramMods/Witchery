package art.arcane.witchery.crafting;

import art.arcane.witchery.item.HunterClothingItem;
import art.arcane.witchery.registry.WitcheryRecipes;
import com.google.gson.JsonObject;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;

public final class HunterClothingUpgradeRecipe extends ShapedRecipe {
    private HunterClothingUpgradeRecipe(ShapedRecipe recipe) {
        super(recipe.getId(), recipe.getGroup(), recipe.category(), recipe.getWidth(), recipe.getHeight(),
                recipe.getIngredients(), recipe.getResultItem(RegistryAccess.EMPTY), recipe.showNotification());
    }

    @Override
    public ItemStack assemble(CraftingContainer input, RegistryAccess registries) {
        ItemStack result = super.assemble(input, registries);
        for (int slot = 0; slot < input.getContainerSize(); slot++) {
            ItemStack clothing = input.getItem(slot);
            if (clothing.getItem() instanceof HunterClothingItem && clothing.getTag() != null) {
                result.getOrCreateTag().merge(clothing.getTag().copy());
                break;
            }
        }
        return result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return WitcheryRecipes.HUNTER_CLOTHING_UPGRADE.get();
    }

    public static final class Serializer implements RecipeSerializer<HunterClothingUpgradeRecipe> {
        @Override
        public HunterClothingUpgradeRecipe fromJson(ResourceLocation id, JsonObject json) {
            return new HunterClothingUpgradeRecipe(RecipeSerializer.SHAPED_RECIPE.fromJson(id, json));
        }

        @Override
        public HunterClothingUpgradeRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            return new HunterClothingUpgradeRecipe(RecipeSerializer.SHAPED_RECIPE.fromNetwork(id, buffer));
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, HunterClothingUpgradeRecipe recipe) {
            RecipeSerializer.SHAPED_RECIPE.toNetwork(buffer, recipe);
        }
    }
}
