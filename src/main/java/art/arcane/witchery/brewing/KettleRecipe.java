package art.arcane.witchery.brewing;

import art.arcane.witchery.familiar.FamiliarBinding;
import art.arcane.witchery.registry.WitcheryRecipes;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
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
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

public record KettleRecipe(ResourceLocation id, List<Ingredient> ingredients, ItemStack result, int color, int power,
                           ResourceLocation dimension, int bottlingBonus, @Nullable FamiliarBinding.Type bottlingFamiliar)
        implements Recipe<Container> {
    public static final int INGREDIENT_SLOTS = 6;

    public boolean canBrewIn(Level level) {
        return dimension == null || dimension.equals(level.dimension().location());
    }

    public boolean canBottle(@Nullable ServerPlayer player) {
        return bottlingFamiliar == null || player != null && FamiliarBinding.activeType(player) == bottlingFamiliar;
    }

    public boolean matchesPartial(List<ItemStack> contents) {
        List<Predicate<ItemStack>> tests = ingredients.stream()
                .<Predicate<ItemStack>>map(ingredient -> stack -> stack.isEmpty() || ingredient.test(stack)).toList();
        return RecipeMatcher.findMatches(contents, tests) != null;
    }

    @Override
    public boolean matches(Container container, Level level) {
        if (container.getContainerSize() != INGREDIENT_SLOTS || !canBrewIn(level)) {
            return false;
        }
        List<ItemStack> contents = new ArrayList<>(INGREDIENT_SLOTS);
        for (int slot = 0; slot < INGREDIENT_SLOTS; slot++) {
            ItemStack stack = container.getItem(slot);
            if (stack.isEmpty()) {
                return false;
            }
            contents.add(stack);
        }
        return RecipeMatcher.findMatches(contents, ingredients) != null;
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= INGREDIENT_SLOTS;
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
        return WitcheryRecipes.KETTLE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return WitcheryRecipes.KETTLE.get();
    }

    public static final class Serializer implements RecipeSerializer<KettleRecipe> {
        @Override
        public KettleRecipe fromJson(ResourceLocation id, JsonObject json) {
            JsonArray entries = GsonHelper.getAsJsonArray(json, "ingredients");
            ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            int power = GsonHelper.getAsInt(json, "power", 0);
            int bonus = GsonHelper.getAsInt(json, "bottling_bonus", 0);
            if (entries.size() != INGREDIENT_SLOTS || power < 0 || result.getCount() > result.getMaxStackSize()
                    || bonus < 0 || bonus >= result.getMaxStackSize()) {
                throw new JsonSyntaxException("Invalid kettle recipe ingredients, power, result or bottling bonus for " + id);
            }
            List<Ingredient> ingredients = new ArrayList<>(INGREDIENT_SLOTS);
            entries.forEach(entry -> ingredients.add(Ingredient.fromJson(entry, false)));
            ResourceLocation dimension = json.has("dimension")
                    ? new ResourceLocation(GsonHelper.getAsString(json, "dimension")) : null;
            FamiliarBinding.Type familiar = null;
            if (json.has("bottling_familiar")) {
                String name = GsonHelper.getAsString(json, "bottling_familiar");
                try {
                    familiar = FamiliarBinding.Type.valueOf(name.toUpperCase(Locale.ROOT));
                } catch (IllegalArgumentException invalid) {
                    throw new JsonSyntaxException("Unknown bottling familiar '" + name + "' for " + id, invalid);
                }
            }
            return new KettleRecipe(id, List.copyOf(ingredients), result, GsonHelper.getAsInt(json, "color"), power, dimension, bonus, familiar);
        }

        @Override
        public KettleRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            List<Ingredient> ingredients = new ArrayList<>(INGREDIENT_SLOTS);
            for (int slot = 0; slot < INGREDIENT_SLOTS; slot++) {
                ingredients.add(Ingredient.fromNetwork(buffer));
            }
            return new KettleRecipe(id, List.copyOf(ingredients), buffer.readItem(), buffer.readInt(), buffer.readVarInt(),
                    buffer.readNullable(FriendlyByteBuf::readResourceLocation), buffer.readVarInt(),
                    buffer.readNullable(data -> data.readEnum(FamiliarBinding.Type.class)));
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, KettleRecipe recipe) {
            recipe.ingredients.forEach(ingredient -> ingredient.toNetwork(buffer));
            buffer.writeItem(recipe.result);
            buffer.writeInt(recipe.color);
            buffer.writeVarInt(recipe.power);
            buffer.writeNullable(recipe.dimension, FriendlyByteBuf::writeResourceLocation);
            buffer.writeVarInt(recipe.bottlingBonus);
            buffer.writeNullable(recipe.bottlingFamiliar, FriendlyByteBuf::writeEnum);
        }
    }
}
