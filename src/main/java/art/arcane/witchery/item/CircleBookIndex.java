package art.arcane.witchery.item;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryRecipes;
import art.arcane.witchery.ritual.ItemRiteRecipe;
import art.arcane.witchery.ritual.RiteActivation;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class CircleBookIndex {
    private static final String BOOKMARK = "WitcheryRite";
    private static final ResourceLocation FORESTATION = ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "forestation/oak");
    private static final ResourceLocation BINDING = ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "rite_of_binding_scarecrow");
    // The 1.7.10 book sorts by bookIndex, keeping registration order within a shared index.
    private static final List<Integer> BOOK_ORDER = List.of(
            1, 45, 2, 41, 54, 83, 84, 88, 89, 90, 34, 42,
            3, 4, 67, 5, 91, 6, 7, 68, 69, 8, 9, 10,
            11, 12, 65, 13, 77, 14, 15, 16, 17, 18, 19, 20,
            46, 47, 51, 58, 35, 36, 43, 64, 70, 21, 22, 23,
            24, 87, 25, 26, 96, 40, 33, 55, 27, 28, 29, 30,
            31, 50, 57, 61, 80, 81, 95, 32, 44, 82, 37, 38,
            66, 39, 48, 52, 59, 62, 78, 92, 49, 53, 60, 63,
            79, 93, 94, 56);
    private static final Comparator<ResourceLocation> PAGE_ORDER = Comparator.comparingInt(CircleBookIndex::order)
            .thenComparing(Comparator.naturalOrder());

    private CircleBookIndex() {}

    record Chapter(ResourceLocation id, ItemRiteRecipe recipe, List<Ingredient> firstOfferings) {}

    static List<Chapter> chapters(Level level) {
        Map<ResourceLocation, List<ItemRiteRecipe>> grouped = new HashMap<>();
        for (var recipe : recipes(level)) {
            grouped.computeIfAbsent(chapterId(recipe.id()), ignored -> new ArrayList<>()).add(recipe);
        }
        return grouped.entrySet().stream().sorted(Map.Entry.comparingByKey(PAGE_ORDER)).map(entry -> {
            var recipes = entry.getValue();
            recipes.sort(Comparator.comparingInt(recipe -> RiteActivation.legacyId(recipe.id())));
            var alternatives = recipes.size() < 2 ? List.<Ingredient>of() : recipes.stream()
                    .filter(recipe -> !recipe.sacrifices().isEmpty()).map(recipe -> recipe.sacrifices().get(0)).toList();
            return new Chapter(entry.getKey(), recipes.get(0), alternatives);
        }).toList();
    }

    public static int bookmark(ItemStack book, Level level, List<GuideBookPages.Page> pages) {
        var tag = book.getTag();
        if (tag != null && tag.contains(BOOKMARK, Tag.TAG_STRING)) {
            return Math.max(0, find(pages, ResourceLocation.tryParse(tag.getString(BOOKMARK))));
        }
        int page = GuideBookItem.bookmark(book);
        if (page == 0) return 0;
        // Earlier 1.20.1 builds kept a numeric bookmark into every recipe in ID order.
        var previous = recipes(level).stream().sorted(Comparator.comparing(ItemRiteRecipe::id)).toList();
        if (previous.isEmpty()) return 0;
        return Math.max(0, find(pages, previous.get(Math.min(page, previous.size()) - 1).id()));
    }

    public static int saveSelection(ItemStack book, List<GuideBookPages.Page> pages, int pageHint,
                                    @Nullable ResourceLocation entryId) {
        int page = entryId == null ? (pageHint == 0 ? 0 : -1) : find(pages, entryId);
        if (page < 0) return -1;
        GuideBookItem.setBookmark(book, page);
        if (page == 0) book.getOrCreateTag().remove(BOOKMARK);
        else book.getOrCreateTag().putString(BOOKMARK, pages.get(page).entryId().toString());
        return page;
    }

    static void importLegacyBookmark(ItemStack book) {
        int page = Math.min(GuideBookItem.bookmark(book), BOOK_ORDER.size());
        if (page == 0 || book.getOrCreateTag().contains(BOOKMARK, Tag.TAG_STRING)) return;
        var id = RiteActivation.legacyRecipeId(BOOK_ORDER.get(page - 1));
        if (id != null) book.getOrCreateTag().putString(BOOKMARK, id.toString());
    }

    private static List<ItemRiteRecipe> recipes(Level level) {
        return level.getRecipeManager().getAllRecipesFor(WitcheryRecipes.ITEM_RITE.get());
    }

    private static int order(ResourceLocation id) {
        int index = BOOK_ORDER.indexOf(RiteActivation.legacyId(id));
        return index < 0 ? Integer.MAX_VALUE : index;
    }

    private static int find(List<GuideBookPages.Page> pages, @Nullable ResourceLocation entryId) {
        if (entryId == null) return -1;
        var chapterId = chapterId(entryId);
        for (int page = 1; page < pages.size(); page++) {
            if (chapterId.equals(pages.get(page).entryId())) return page;
        }
        return -1;
    }

    private static ResourceLocation chapterId(ResourceLocation recipe) {
        return switch (RiteActivation.legacyId(recipe)) {
            case 71, 72, 73, 74, 75, 76 -> FORESTATION;
            case 85, 86 -> BINDING;
            default -> recipe;
        };
    }
}
