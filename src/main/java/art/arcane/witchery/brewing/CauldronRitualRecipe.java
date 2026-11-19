package art.arcane.witchery.brewing;

import art.arcane.witchery.registry.WitcheryRecipes;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
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
import net.minecraftforge.common.util.RecipeMatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public record CauldronRitualRecipe(ResourceLocation id, List<Ingredient> ingredients, Ingredient trigger,
                                  ItemStack result, int time) implements Recipe<Container> {
    public boolean acceptsIngredient(List<ItemStack> added, ItemStack offered) {
        if (added.size() >= ingredients.size()) {
            return false;
        }
        List<ItemStack> possible = new ArrayList<>(ingredients.size());
        possible.addAll(added);
        possible.add(offered);
        while (possible.size() < ingredients.size()) {
            possible.add(ItemStack.EMPTY);
        }
        List<Predicate<ItemStack>> tests = ingredients.stream()
                .<Predicate<ItemStack>>map(ingredient -> stack -> stack.isEmpty() || ingredient.test(stack)).toList();
        return RecipeMatcher.findMatches(possible, tests) != null;
    }

    public boolean canStart(List<ItemStack> added, ItemStack offered) {
        return trigger.test(offered) && RecipeMatcher.findMatches(added, ingredients) != null;
    }

    @Override
    public boolean matches(Container container, Level level) {
        List<ItemStack> contents = new ArrayList<>();
        for (int i = 0; i < container.getContainerSize(); i++) {
            if (!container.getItem(i).isEmpty()) {
                contents.add(container.getItem(i));
            }
        }
        return !contents.isEmpty()
                && canStart(contents.subList(0, contents.size() - 1), contents.get(contents.size() - 1));
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height > ingredients.size();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess registries) {
        return result;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return WitcheryRecipes.CAULDRON_RITUAL_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return WitcheryRecipes.CAULDRON_RITUAL.get();
    }

    public static final class Serializer implements RecipeSerializer<CauldronRitualRecipe> {
        private static final int MAX_INGREDIENTS = 24;

        @Override
        public CauldronRitualRecipe fromJson(ResourceLocation id, JsonObject json) {
            List<Ingredient> ingredients = new ArrayList<>();
            GsonHelper.getAsJsonArray(json, "ingredients").forEach(entry -> ingredients.add(Ingredient.fromJson(entry, false)));
            int time = GsonHelper.getAsInt(json, "time", 210);
            if (ingredients.size() > MAX_INGREDIENTS || time < 1) {
                throw new JsonSyntaxException("Invalid cauldron ritual size or duration for " + id);
            }
            return new CauldronRitualRecipe(id, List.copyOf(ingredients), Ingredient.fromJson(json.get("trigger"), false),
                    ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result")), time);
        }

        @Override
        public CauldronRitualRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            int count = buffer.readVarInt();
            if (count < 0 || count > MAX_INGREDIENTS) {
                throw new IllegalArgumentException("Invalid cauldron ritual ingredient count " + count);
            }
            List<Ingredient> ingredients = new ArrayList<>(count);
            for (int i = 0; i < count; i++) {
                ingredients.add(Ingredient.fromNetwork(buffer));
            }
            return new CauldronRitualRecipe(id, List.copyOf(ingredients), Ingredient.fromNetwork(buffer),
                    buffer.readItem(), buffer.readVarInt());
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, CauldronRitualRecipe recipe) {
            buffer.writeVarInt(recipe.ingredients.size());
            for (Ingredient ingredient : recipe.ingredients) {
                ingredient.toNetwork(buffer);
            }
            recipe.trigger.toNetwork(buffer);
            buffer.writeItem(recipe.result);
            buffer.writeVarInt(recipe.time);
        }
    }
}
