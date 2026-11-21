package art.arcane.witchery.item;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import org.jetbrains.annotations.Nullable;

import java.util.List;

final class LegacyBiomeAtlas {
    // The 1.7.10 atlas stored the position in Forge's grouped list, including repeated biomes.
    // Forge 10.13.4.1614 supplies the order below. Vanilla's biome data fixes supply the current names.
    private static final List<ResourceKey<Biome>> PAGES = List.of(
            // Beach
            Biomes.BEACH, Biomes.MUSHROOM_FIELDS, Biomes.STONY_SHORE, Biomes.SNOWY_BEACH,
            // Forest
            Biomes.FOREST, Biomes.TAIGA, Biomes.TAIGA, Biomes.FOREST, Biomes.SPARSE_JUNGLE, Biomes.BIRCH_FOREST,
            Biomes.BIRCH_FOREST, Biomes.DARK_FOREST, Biomes.SNOWY_TAIGA, Biomes.SNOWY_TAIGA, Biomes.OLD_GROWTH_PINE_TAIGA,
            Biomes.OLD_GROWTH_PINE_TAIGA, Biomes.WINDSWEPT_FOREST, Biomes.FLOWER_FOREST, Biomes.OLD_GROWTH_SPRUCE_TAIGA,
            Biomes.WOODED_BADLANDS,
            // Hills
            Biomes.WINDSWEPT_HILLS, Biomes.TAIGA, Biomes.DESERT, Biomes.JUNGLE, Biomes.FOREST, Biomes.BIRCH_FOREST,
            Biomes.SNOWY_TAIGA, Biomes.OLD_GROWTH_PINE_TAIGA, Biomes.WINDSWEPT_GRAVELLY_HILLS, Biomes.FLOWER_FOREST,
            Biomes.TAIGA, Biomes.ICE_SPIKES, Biomes.JUNGLE, Biomes.SPARSE_JUNGLE, Biomes.OLD_GROWTH_BIRCH_FOREST,
            Biomes.OLD_GROWTH_BIRCH_FOREST, Biomes.DARK_FOREST, Biomes.SNOWY_TAIGA, Biomes.WINDSWEPT_GRAVELLY_HILLS,
            Biomes.WINDSWEPT_SAVANNA, Biomes.WINDSWEPT_SAVANNA,
            // Mesa, mountain and plains
            Biomes.BADLANDS, Biomes.WOODED_BADLANDS, Biomes.BADLANDS,
            Biomes.WINDSWEPT_HILLS, Biomes.SNOWY_PLAINS, Biomes.WINDSWEPT_HILLS, Biomes.WINDSWEPT_FOREST,
            Biomes.PLAINS, Biomes.SAVANNA, Biomes.SAVANNA_PLATEAU, Biomes.SUNFLOWER_PLAINS, Biomes.DESERT,
            Biomes.ERODED_BADLANDS, Biomes.BADLANDS,
            // Sandy and snowy
            Biomes.DESERT, Biomes.DESERT, Biomes.BADLANDS, Biomes.WOODED_BADLANDS, Biomes.BADLANDS, Biomes.DESERT,
            Biomes.ERODED_BADLANDS, Biomes.WOODED_BADLANDS, Biomes.BADLANDS,
            Biomes.FROZEN_OCEAN, Biomes.FROZEN_RIVER, Biomes.SNOWY_PLAINS, Biomes.SNOWY_PLAINS, Biomes.SNOWY_BEACH,
            Biomes.SNOWY_TAIGA, Biomes.SNOWY_TAIGA, Biomes.ICE_SPIKES, Biomes.SNOWY_TAIGA,
            // Swamp, wasteland, river, ocean and spooky
            Biomes.SWAMP, Biomes.SWAMP, Biomes.SNOWY_PLAINS, Biomes.RIVER, Biomes.FROZEN_RIVER,
            Biomes.OCEAN, Biomes.FROZEN_OCEAN, Biomes.DEEP_OCEAN, Biomes.DARK_FOREST);

    private LegacyBiomeAtlas() {}

    @Nullable
    static ResourceLocation biomeAt(int page) {
        return page >= 0 && page < PAGES.size() ? PAGES.get(page).location() : null;
    }
}
