package art.arcane.witchery.brewing;

import art.arcane.witchery.registry.WitcheryRecipes;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.util.RecipeMatcher;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

public record CauldronRitualRecipe(ResourceLocation id, List<Ingredient> ingredients, Ingredient trigger,
                                  ItemStack result, int time, int power, boolean legacy, int priority,
                                  @Nullable EntityType<?> summon) implements Recipe<Container> {
    public boolean acceptsIngredient(List<ItemStack> added, ItemStack offered) {
        if (legacy) {
            if (ingredients.stream().noneMatch(ingredient -> ingredient.test(offered))) return false;
            List<ItemStack> possible = new ArrayList<>(added);
            possible.add(offered);
            return matchedIngredients(possible) > matchedIngredients(added);
        }
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
        if (!trigger.test(offered) || added.size() < ingredients.size()) return false;
        if (added.size() == ingredients.size()) {
            return ingredients.isEmpty() || RecipeMatcher.findMatches(added, ingredients) != null;
        }
        return legacy && matchedIngredients(added) == ingredients.size();
    }

    private int matchedIngredients(List<ItemStack> added) {
        // Original recipes consume the entire batch and check only their required ingredients.
        int[] matched = new int[added.size()];
        Arrays.fill(matched, -1);
        int count = 0;
        for (int ingredient = 0; ingredient < ingredients.size(); ingredient++) {
            if (matchIngredient(ingredient, added, matched, new boolean[added.size()])) count++;
        }
        return count;
    }

    private boolean matchIngredient(int ingredient, List<ItemStack> added, int[] matched, boolean[] visited) {
        for (int index = 0; index < added.size(); index++) {
            if (visited[index] || !ingredients.get(ingredient).test(added.get(index))) continue;
            visited[index] = true;
            if (matched[index] == -1 || matchIngredient(matched[index], added, matched, visited)) {
                matched[index] = ingredient;
                return true;
            }
        }
        return false;
    }

    public int batchPower(List<ItemStack> added) {
        if (!legacy) return power;
        long total = power;
        for (ItemStack ingredient : added) total += BrewMixture.ingredientPower(ingredient);
        return (int) Math.min(Integer.MAX_VALUE, total);
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
            int power = GsonHelper.getAsInt(json, "power", 0);
            boolean legacy = GsonHelper.getAsBoolean(json, "legacy", false);
            int priority = GsonHelper.getAsInt(json, "priority", 0);
            if (ingredients.size() > MAX_INGREDIENTS || time < 1 || power < 0) {
                throw new JsonSyntaxException("Invalid cauldron ritual size, duration or power for " + id);
            }
            EntityType<?> summon = null;
            if (json.has("summon")) {
                var entityId = ResourceLocation.parse(GsonHelper.getAsString(json, "summon"));
                summon = BuiltInRegistries.ENTITY_TYPE.getOptional(entityId)
                        .orElseThrow(() -> new JsonSyntaxException("Unknown summoned entity " + entityId + " for " + id));
            }
            ItemStack result = summon == null ? CraftingHelper.getItemStack(GsonHelper.getAsJsonObject(json, "result"), true) : ItemStack.EMPTY;
            return new CauldronRitualRecipe(id, List.copyOf(ingredients), Ingredient.fromJson(json.get("trigger"), false),
                    result, time, power, legacy, priority, summon);
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
            Ingredient trigger = Ingredient.fromNetwork(buffer);
            ItemStack result = buffer.readItem();
            int time = buffer.readVarInt();
            int power = buffer.readVarInt();
            boolean legacy = buffer.readBoolean();
            int priority = buffer.readVarInt();
            EntityType<?> summon = buffer.readBoolean() ? buffer.readById(BuiltInRegistries.ENTITY_TYPE) : null;
            return new CauldronRitualRecipe(id, List.copyOf(ingredients), trigger, result, time, power, legacy, priority, summon);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, CauldronRitualRecipe recipe) {
            buffer.writeVarInt(recipe.ingredients.size());
            for (Ingredient ingredient : recipe.ingredients) {
                ingredient.toNetwork(buffer);
            }
            recipe.trigger.toNetwork(buffer);
            buffer.writeItemStack(recipe.result, false);
            buffer.writeVarInt(recipe.time);
            buffer.writeVarInt(recipe.power);
            buffer.writeBoolean(recipe.legacy);
            buffer.writeVarInt(recipe.priority);
            buffer.writeBoolean(recipe.summon != null);
            if (recipe.summon != null) buffer.writeId(BuiltInRegistries.ENTITY_TYPE, recipe.summon);
        }
    }
}
