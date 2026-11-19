package art.arcane.witchery.crafting;

import art.arcane.witchery.registry.WitcheryRecipes;
import com.google.gson.JsonArray;
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
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public record SpinningRecipe(ResourceLocation id, Ingredient fibre, int fibreCount, List<Ingredient> modifiers, ItemStack result)
        implements Recipe<Container> {
    public static final int FIBRE_SLOT = 0;
    public static final int OUTPUT_SLOT = 2;
    public static final int SLOT_COUNT = 5;
    private static final List<Integer> MODIFIER_SLOTS = List.of(1, 3, 4);

    @Nullable
    public int[] matchModifiers(Container container) {
        List<ItemStack> stacks = MODIFIER_SLOTS.stream().map(container::getItem).toList();
        List<Predicate<ItemStack>> tests = new ArrayList<>(modifiers);
        while (tests.size() < MODIFIER_SLOTS.size()) {
            tests.add(stack -> true);
        }
        int[] matches = RecipeMatcher.findMatches(stacks, tests);
        if (matches == null) {
            return null;
        }
        int[] slots = new int[modifiers.size()];
        for (int index = 0; index < matches.length; index++) {
            if (matches[index] < modifiers.size()) {
                slots[matches[index]] = MODIFIER_SLOTS.get(index);
            }
        }
        return slots;
    }

    @Override
    public boolean matches(Container container, Level level) {
        if (container.getContainerSize() != SLOT_COUNT) {
            return false;
        }
        ItemStack stack = container.getItem(FIBRE_SLOT);
        return fibre.test(stack) && stack.getCount() >= fibreCount && matchModifiers(container) != null;
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 1 + modifiers.size();
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
        return WitcheryRecipes.SPINNING_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return WitcheryRecipes.SPINNING.get();
    }

    public static final class Serializer implements RecipeSerializer<SpinningRecipe> {
        @Override
        public SpinningRecipe fromJson(ResourceLocation id, JsonObject json) {
            Ingredient fibre = Ingredient.fromJson(json.get("fibre"), false);
            int count = GsonHelper.getAsInt(json, "fibre_count", 1);
            JsonArray inputModifiers = GsonHelper.getAsJsonArray(json, "modifiers");
            ItemStack result = ShapedRecipe.itemStackFromJson(GsonHelper.getAsJsonObject(json, "result"));
            if (count <= 0 || count > 64 || inputModifiers.size() > MODIFIER_SLOTS.size() || result.getCount() > result.getMaxStackSize()) {
                throw new JsonSyntaxException("Invalid fibre count, modifiers, or result for spinning recipe " + id);
            }
            List<Ingredient> modifiers = new ArrayList<>(inputModifiers.size());
            inputModifiers.forEach(entry -> modifiers.add(Ingredient.fromJson(entry, false)));
            return new SpinningRecipe(id, fibre, count, List.copyOf(modifiers), result);
        }

        @Override
        public SpinningRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
            Ingredient fibre = Ingredient.fromNetwork(buffer);
            int count = buffer.readVarInt();
            int modifierCount = buffer.readVarInt();
            if (modifierCount < 0 || modifierCount > MODIFIER_SLOTS.size()) {
                throw new IllegalArgumentException("Invalid modifier count for spinning recipe " + id);
            }
            List<Ingredient> modifiers = new ArrayList<>(modifierCount);
            for (int index = 0; index < modifierCount; index++) {
                modifiers.add(Ingredient.fromNetwork(buffer));
            }
            return new SpinningRecipe(id, fibre, count, List.copyOf(modifiers), buffer.readItem());
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, SpinningRecipe recipe) {
            recipe.fibre.toNetwork(buffer);
            buffer.writeVarInt(recipe.fibreCount);
            buffer.writeVarInt(recipe.modifiers.size());
            recipe.modifiers.forEach(ingredient -> ingredient.toNetwork(buffer));
            buffer.writeItem(recipe.result);
        }
    }
}
