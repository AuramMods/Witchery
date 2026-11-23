package art.arcane.witchery.crafting;

import art.arcane.witchery.item.BiomeBookIndex;
import art.arcane.witchery.item.BiomeNoteItem;
import art.arcane.witchery.item.GuideBookItem;
import art.arcane.witchery.registry.WitcheryItems;
import art.arcane.witchery.registry.WitcheryRecipes;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

public final class CopyBiomeNoteRecipe extends CustomRecipe {
    public CopyBiomeNoteRecipe(ResourceLocation id, CraftingBookCategory category) {
        super(id, category);
    }

    @Override
    public boolean matches(CraftingContainer grid, Level level) {
        return !findAtlas(grid).isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingContainer grid, RegistryAccess registries) {
        var atlas = findAtlas(grid);
        var biome = atlas.isEmpty() ? null : BiomeBookIndex.selectedBiome(atlas, registries);
        return biome == null ? ItemStack.EMPTY : noteForBiome(biome);
    }

    public static ItemStack noteForBiome(ResourceLocation biome) {
        var note = new ItemStack(WitcheryItems.getRequired("biomenote").get());
        note.getOrCreateTag().putString(BiomeNoteItem.BIOME, biome.toString());
        return note;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingContainer grid) {
        var remaining = super.getRemainingItems(grid);
        for (int slot = 0; slot < grid.getContainerSize(); slot++) {
            var stack = grid.getItem(slot);
            if (GuideBookItem.volume(stack) == GuideBookItem.Volume.ATLAS) {
                remaining.set(slot, stack.copyWithCount(1));
            }
        }
        return remaining;
    }

    private static ItemStack findAtlas(CraftingContainer grid) {
        ItemStack atlas = ItemStack.EMPTY;
        boolean paper = false;
        for (var stack : grid.getItems()) {
            if (stack.isEmpty()) continue;
            if (!paper && stack.is(Items.PAPER)) paper = true;
            else if (atlas.isEmpty() && GuideBookItem.volume(stack) == GuideBookItem.Volume.ATLAS) atlas = stack;
            else return ItemStack.EMPTY;
        }
        return paper ? atlas : ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return WitcheryRecipes.COPY_BIOME_NOTE.get();
    }
}
