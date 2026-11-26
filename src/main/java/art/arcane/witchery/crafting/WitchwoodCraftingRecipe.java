package art.arcane.witchery.crafting;

import art.arcane.witchery.block.WitchWoodType;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryRecipes;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.PartialNBTIngredient;

import java.util.List;

public final class WitchwoodCraftingRecipe extends ShapedRecipe {
    private final List<Ingredient> reservedPlanks;

    private WitchwoodCraftingRecipe(ShapedRecipe recipe, List<Ingredient> reservedPlanks) {
        super(recipe.getId(), recipe.getGroup(), recipe.category(), recipe.getWidth(), recipe.getHeight(),
                recipe.getIngredients(), recipe.getResultItem(RegistryAccess.EMPTY), recipe.showNotification());
        this.reservedPlanks = reservedPlanks;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        var inputs = super.getIngredients();
        var wood = WitcheryBlocks.getRequired("witchwood").get();
        var rowan = WitchWoodType.ROWAN.stack(wood);
        for (int slot = 0; slot < inputs.size(); slot++) {
            if (inputs.get(slot).test(rowan)) {
                var displayed = NonNullList.of(Ingredient.EMPTY, inputs.toArray(Ingredient[]::new));
                displayed.set(slot, reservedPlanks.isEmpty() ? Ingredient.of(wood)
                        : PartialNBTIngredient.of(wood.asItem(), WitchWoodType.HAWTHORN.stack(wood).getTag()));
                return displayed;
            }
        }
        return inputs;
    }

    @Override
    public boolean matches(CraftingContainer grid, Level level) {
        var witchwood = WitcheryBlocks.getRequired("witchwood").get().asItem();
        if (grid.getItems().stream().noneMatch(stack -> stack.is(witchwood)) || !super.matches(grid, level)) {
            return false;
        }
        // Uniform Rowan and Alder doors use their own recipes, independent of recipe load order.
        return reservedPlanks.stream().noneMatch(reserved -> grid.getItems().stream()
                .filter(stack -> !stack.isEmpty()).allMatch(reserved::test));
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return WitcheryRecipes.WITCHWOOD_CRAFTING.get();
    }

    public static final class Serializer implements RecipeSerializer<WitchwoodCraftingRecipe> {
        @Override
        public WitchwoodCraftingRecipe fromJson(ResourceLocation id, JsonObject json) {
            var reserved = GsonHelper.getAsJsonArray(json, "reserved_planks", new JsonArray());
            List<Ingredient> ingredients = reserved.asList().stream().map(Ingredient::fromJson).toList();
            return new WitchwoodCraftingRecipe(RecipeSerializer.SHAPED_RECIPE.fromJson(id, json), ingredients);
        }

        @Override
        public WitchwoodCraftingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            var shaped = RecipeSerializer.SHAPED_RECIPE.fromNetwork(id, buffer);
            return new WitchwoodCraftingRecipe(shaped, buffer.readList(Ingredient::fromNetwork));
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, WitchwoodCraftingRecipe recipe) {
            RecipeSerializer.SHAPED_RECIPE.toNetwork(buffer, recipe);
            buffer.writeCollection(recipe.reservedPlanks, (buf, ingredient) -> ingredient.toNetwork(buf));
        }
    }
}
