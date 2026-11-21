package art.arcane.witchery.world;

import art.arcane.witchery.Witchery;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public final class WitcheryDimensions {
    public static final ResourceKey<Level> DREAM = levelKey("dream");
    public static final ResourceKey<Level> TORMENT = levelKey("torment");
    public static final ResourceKey<Level> MIRROR = levelKey("mirror");

    public static final ResourceKey<LevelStem> DREAM_STEM = stemKey("dream");
    public static final ResourceKey<LevelStem> TORMENT_STEM = stemKey("torment");
    public static final ResourceKey<LevelStem> MIRROR_STEM = stemKey("mirror");

    public static final ResourceKey<DimensionType> DREAM_TYPE = dimensionTypeKey("dream");
    public static final ResourceKey<DimensionType> TORMENT_TYPE = dimensionTypeKey("torment");
    public static final ResourceKey<DimensionType> MIRROR_TYPE = dimensionTypeKey("mirror");

    private static final Map<Integer, ResourceKey<Level>> LEGACY_LEVELS = Map.of(
            -1, Level.NETHER, 0, Level.OVERWORLD, 1, Level.END, -37, DREAM, -38, TORMENT, -39, MIRROR);

    private WitcheryDimensions() {
    }

    public static List<ResourceKey<Level>> levels() {
        return List.of(DREAM, TORMENT, MIRROR);
    }

    @Nullable
    public static ResourceKey<Level> fromLegacyId(int id) {
        return LEGACY_LEVELS.get(id);
    }

    @Nullable
    public static Integer legacyId(ResourceKey<Level> dimension) {
        for (var entry : LEGACY_LEVELS.entrySet()) {
            if (entry.getValue().equals(dimension)) return entry.getKey();
        }
        return null;
    }

    public static List<ResourceKey<LevelStem>> stems() {
        return List.of(DREAM_STEM, TORMENT_STEM, MIRROR_STEM);
    }

    public static List<ResourceKey<DimensionType>> dimensionTypes() {
        return List.of(DREAM_TYPE, TORMENT_TYPE, MIRROR_TYPE);
    }

    private static ResourceKey<Level> levelKey(String path) {
        return ResourceKey.create(Registries.DIMENSION, key(path));
    }

    private static ResourceKey<LevelStem> stemKey(String path) {
        return ResourceKey.create(Registries.LEVEL_STEM, key(path));
    }

    private static ResourceKey<DimensionType> dimensionTypeKey(String path) {
        return ResourceKey.create(Registries.DIMENSION_TYPE, key(path));
    }

    private static ResourceLocation key(String path) {
        return ResourceLocation.parse(Witchery.MODID + ":" + path);
    }
}
