package art.arcane.witchery.crafting;

import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryRecipes;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public record DistillingRecipe(ResourceLocation id, Ingredient first, Ingredient second, int jars,
                               List<ItemStack> outputs) implements Recipe<Container> {
    public static final int OUTPUTS = 4;

    @Override
    public boolean matches(Container container, Level level) {
        if (container.getContainerSize() < 3) {
            return false;
        }
        ItemStack jarStack = container.getItem(2);
        if (jars > 0 && (!jarStack.is(WitcheryIngredient.CLAY_JAR.item()) || jarStack.getCount() < jars)) {
            return false;
        }
        ItemStack a = container.getItem(0);
        ItemStack b = container.getItem(1);
        return first.test(a) && second.test(b) || first.test(b) && second.test(a);
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
        return getResultItem(registryAccess).copy();
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registryAccess) {
        return outputs.stream().filter(stack -> !stack.isEmpty()).findFirst().orElse(ItemStack.EMPTY);
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> result = NonNullList.create();
        result.add(first);
        if (!second.isEmpty()) {
            result.add(second);
        }
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= (second.isEmpty() ? 1 : 2);
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return WitcheryRecipes.DISTILLING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return WitcheryRecipes.DISTILLING.get();
    }

    public static final class Serializer implements RecipeSerializer<DistillingRecipe> {
        @Override
        public DistillingRecipe fromJson(ResourceLocation id, JsonObject json) {
            JsonArray inputs = GsonHelper.getAsJsonArray(json, "ingredients");
            JsonArray results = GsonHelper.getAsJsonArray(json, "results");
            int jars = GsonHelper.getAsInt(json, "jars");
            if (inputs.size() < 1 || inputs.size() > 2 || results.isEmpty() || results.size() > OUTPUTS || jars < 0 || jars > 64) {
                throw new JsonSyntaxException("Invalid distilling ingredients, results, or jar count for " + id);
            }
            List<ItemStack> outputs = new ArrayList<>(OUTPUTS);
            for (int i = 0; i < OUTPUTS; i++) {
                outputs.add(i < results.size() && !results.get(i).isJsonNull()
                        ? ShapedRecipe.itemStackFromJson(GsonHelper.convertToJsonObject(results.get(i), "result")) : ItemStack.EMPTY);
            }
            if (outputs.stream().allMatch(ItemStack::isEmpty)) {
                throw new JsonSyntaxException("Missing distilling results for " + id);
            }
            return new DistillingRecipe(id, Ingredient.fromJson(inputs.get(0), false),
                    inputs.size() == 2 ? Ingredient.fromJson(inputs.get(1), false) : Ingredient.EMPTY, jars, List.copyOf(outputs));
        }

        @Override
        public DistillingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            Ingredient first = Ingredient.fromNetwork(buffer);
            Ingredient second = buffer.readBoolean() ? Ingredient.fromNetwork(buffer) : Ingredient.EMPTY;
            int jars = buffer.readVarInt();
            List<ItemStack> outputs = new ArrayList<>(OUTPUTS);
            for (int i = 0; i < OUTPUTS; i++) {
                outputs.add(buffer.readItem());
            }
            return new DistillingRecipe(id, first, second, jars, List.copyOf(outputs));
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, DistillingRecipe recipe) {
            recipe.first.toNetwork(buffer);
            buffer.writeBoolean(!recipe.second.isEmpty());
            if (!recipe.second.isEmpty()) {
                recipe.second.toNetwork(buffer);
            }
            buffer.writeVarInt(recipe.jars);
            for (ItemStack output : recipe.outputs) {
                buffer.writeItem(output);
            }
        }
    }
}
