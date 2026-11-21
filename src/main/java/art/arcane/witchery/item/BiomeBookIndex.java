package art.arcane.witchery.item;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
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
import java.util.List;

public final class BiomeBookIndex {
    private static final List<TagKey<Biome>> SECTIONS = List.of(BiomeTags.IS_BEACH, BiomeTags.IS_FOREST, BiomeTags.IS_HILL,
            BiomeTags.IS_BADLANDS, BiomeTags.IS_MOUNTAIN, Tags.Biomes.IS_PLAINS, Tags.Biomes.IS_SANDY, Tags.Biomes.IS_SNOWY,
            Tags.Biomes.IS_SWAMP, Tags.Biomes.IS_WASTELAND, BiomeTags.IS_RIVER, BiomeTags.IS_OCEAN, Tags.Biomes.IS_SPOOKY,
            Tags.Biomes.IS_MAGICAL);

    public record Entry(ResourceLocation id, Biome biome) {}

    private BiomeBookIndex() {}

    public static List<Entry> pages(RegistryAccess access) {
        var registry = access.registryOrThrow(Registries.BIOME);
        List<Entry> pages = new ArrayList<>();
        for (TagKey<Biome> section : SECTIONS) {
            registry.getTag(section).ifPresent(biomes -> biomes.stream()
                    .filter(biome -> !biome.is(BiomeTags.IS_NETHER) && !biome.is(BiomeTags.IS_END))
                    .sorted(Comparator.comparing(biome -> biome.unwrapKey().orElseThrow().location()))
                    .map(BiomeBookIndex::entry).forEachOrdered(pages::add));
        }
        return List.copyOf(pages);
    }

    private static Entry entry(Holder<Biome> biome) {
        return new Entry(biome.unwrapKey().orElseThrow().location(), biome.value());
    }

    public static int bookmark(ItemStack book, List<Entry> pages) {
        int page = Mth.clamp(GuideBookItem.bookmark(book), 0, Math.max(0, pages.size() - 1));
        var id = BiomeNoteItem.biomeId(book);
        if (pages.isEmpty() || id == null || pages.get(page).id.equals(id)) return page;
        for (int i = 0; i < pages.size(); i++) if (pages.get(i).id.equals(id)) return i;
        return page;
    }

    @Nullable
    public static ResourceLocation selectedBiome(ItemStack book, RegistryAccess access) {
        List<Entry> pages = pages(access);
        return pages.isEmpty() ? null : pages.get(bookmark(book, pages)).id;
    }
}
