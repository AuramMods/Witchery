package art.arcane.witchery.item;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;

public final class BiomeBookIndex {
    private static final List<TagKey<Biome>> SECTIONS = List.of(BiomeTags.IS_BEACH, BiomeTags.IS_FOREST, BiomeTags.IS_HILL,
            BiomeTags.IS_BADLANDS, BiomeTags.IS_MOUNTAIN, Tags.Biomes.IS_PLAINS, Tags.Biomes.IS_SANDY, Tags.Biomes.IS_SNOWY,
            Tags.Biomes.IS_SWAMP, Tags.Biomes.IS_WASTELAND, BiomeTags.IS_RIVER, BiomeTags.IS_OCEAN, Tags.Biomes.IS_SPOOKY,
            Tags.Biomes.IS_MAGICAL);

    public record Entry(ResourceLocation id, Biome biome) {}
    public record Section(String name, int firstPage) {}
    public record Index(List<Entry> pages, List<Section> sections) {}

    private BiomeBookIndex() {}

    public static List<Entry> pages(RegistryAccess access) {
        return index(access).pages();
    }

    public static Index index(RegistryAccess access) {
        var registry = access.registryOrThrow(Registries.BIOME);
        List<Entry> pages = new ArrayList<>();
        List<Section> sections = new ArrayList<>();
        for (TagKey<Biome> section : SECTIONS) {
            int firstPage = pages.size();
            registry.getTag(section).ifPresent(biomes -> biomes.stream()
                    .filter(BiomeBookIndex::eligible).map(BiomeBookIndex::entry)
                    .sorted(Comparator.comparing(Entry::id)).forEachOrdered(pages::add));
            if (pages.size() > firstPage) {
                sections.add(new Section(section.location().getPath().substring(3), firstPage));
            }
        }
        var listed = new HashSet<ResourceLocation>();
        pages.forEach(page -> listed.add(page.id));
        int firstUnlisted = pages.size();
        registry.holders().filter(BiomeBookIndex::eligible).map(BiomeBookIndex::entry)
                .filter(page -> !listed.contains(page.id)).sorted(Comparator.comparing(Entry::id)).forEachOrdered(pages::add);
        if (pages.size() > firstUnlisted) sections.add(new Section("other", firstUnlisted));
        return new Index(List.copyOf(pages), List.copyOf(sections));
    }

    private static boolean eligible(Holder<Biome> biome) {
        return !biome.is(BiomeTags.IS_NETHER) && !biome.is(BiomeTags.IS_END);
    }

    private static Entry entry(Holder<Biome> biome) {
        return new Entry(biome.unwrapKey().orElseThrow().location(), biome.value());
    }

    public static int bookmark(ItemStack book, List<Entry> pages) {
        int page = Mth.clamp(GuideBookItem.bookmark(book), 0, Math.max(0, pages.size() - 1));
        var id = BiomeNoteItem.biomeId(book);
        if (id == null && book.hasTag() && book.getTag().contains(GuideBookItem.CURRENT_PAGE, Tag.TAG_ANY_NUMERIC)) {
            id = LegacyBiomeAtlas.biomeAt(book.getTag().getInt(GuideBookItem.CURRENT_PAGE));
        }
        int resolved = pageFor(pages, page, id);
        return resolved < 0 ? page : resolved;
    }

    private static int pageFor(List<Entry> pages, int hint, @Nullable ResourceLocation id) {
        if (id == null) return -1;
        if (hint >= 0 && hint < pages.size() && pages.get(hint).id.equals(id)) return hint;
        for (int i = 0; i < pages.size(); i++) if (pages.get(i).id.equals(id)) return i;
        return -1;
    }

    public static int saveSelection(ItemStack book, RegistryAccess access, int pageHint, @Nullable ResourceLocation id) {
        int page = pageFor(pages(access), pageHint, id);
        if (page >= 0) {
            book.getOrCreateTag().putString(BiomeNoteItem.BIOME, id.toString());
            GuideBookItem.setBookmark(book, page);
        }
        return page;
    }

    @Nullable
    public static ResourceLocation selectedBiome(ItemStack book, RegistryAccess access) {
        List<Entry> pages = pages(access);
        var saved = BiomeNoteItem.biomeId(book);
        if (saved != null) return pageFor(pages, GuideBookItem.bookmark(book), saved) >= 0 ? saved : null;
        return pages.isEmpty() ? null : pages.get(bookmark(book, pages)).id;
    }
}
