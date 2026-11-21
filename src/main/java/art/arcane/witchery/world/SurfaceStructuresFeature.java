package art.arcane.witchery.world;

import art.arcane.witchery.config.WitcheryServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.common.Tags;

import java.util.function.BooleanSupplier;

public final class SurfaceStructuresFeature extends Feature<NoneFeatureConfiguration> {
    private static final ResourceLocation TWILIGHT_FOREST = ResourceLocation.parse("twilightforest:twilight_forest");
    private static final int SPACING_MARGIN = 8;
    private static final int REGION_SALT = 10387312;

    private enum Structure {
        WICKER_MAN(6, 5, 7, WitcheryServerConfig.GENERATE_WICKER_MEN::get),
        COVEN(11, 11, 4, WitcheryServerConfig.GENERATE_COVENS::get),
        SHACK(7, 7, 6, WitcheryServerConfig.GENERATE_SHACKS::get),
        HOBGOBLIN_HUT(7, 7, 7, WitcheryServerConfig.GENERATE_HOBGOBLIN_HUTS::get);

        private final int width;
        private final int depth;
        private final int height;
        private final BooleanSupplier enabled;

        Structure(int width, int depth, int height, BooleanSupplier enabled) {
            this.width = width;
            this.depth = depth;
            this.height = height;
            this.enabled = enabled;
        }

        boolean generate(FeaturePlaceContext<NoneFeatureConfiguration> context, BlockPos center, boolean sandy,
                         RandomSource random) {
            if (!enabled.getAsBoolean()) return false;
            Direction facing = Direction.Plane.HORIZONTAL.getRandomDirection(random);
            if (this == WICKER_MAN) facing = facing.getOpposite();
            SurfaceStructureSite site = SurfaceStructureSite.atSurface(context.level(), center, facing, width, depth,
                    sandy, context.chunkGenerator().getSeaLevel() + 1);
            if (site.level().isOutsideBuildHeight(site.center().getY())
                    || site.level().isOutsideBuildHeight(site.center().getY() + height)) return false;
            if (this != WICKER_MAN && !site.hasDryCorners()) return false;
            switch (this) {
                case WICKER_MAN -> WickerManFeature.build(site, random);
                case COVEN -> CovenStructures.circle(site, random);
                case SHACK -> CovenStructures.shack(site, random);
                case HOBGOBLIN_HUT -> HobgoblinHut.build(site);
            }
            return true;
        }
    }

    private static final Structure[] STRUCTURES = Structure.values();

    public SurfaceStructuresFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        return place(context, false);
    }

    static boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context, boolean wickerOnly) {
        WorldGenLevel level = context.level();
        var dimension = level.getLevel().dimension();
        if (!dimension.equals(Level.OVERWORLD) && !(WitcheryServerConfig.WORLD_GEN_TWILIGHT_FOREST.get()
                && dimension.location().equals(TWILIGHT_FOREST))) return false;

        ChunkPos chunk = new ChunkPos(context.origin());
        int spread = WitcheryServerConfig.WORLD_GEN_FREQUENCY.get();
        int spacing = SPACING_MARGIN + spread;
        int regionX = Math.floorDiv(chunk.x, spacing);
        int regionZ = Math.floorDiv(chunk.z, spacing);
        WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(0));
        random.setLargeFeatureWithSalt(level.getSeed(), regionX, regionZ, REGION_SALT);
        int candidateX = regionX * spacing + random.nextInt(spread);
        int candidateZ = regionZ * spacing + random.nextInt(spread);
        if (chunk.x != candidateX || chunk.z != candidateZ) return false;

        int first = random.nextInt(STRUCTURES.length);
        if (wickerOnly && first != Structure.WICKER_MAN.ordinal()) return false;
        int x = chunk.getMiddleBlockX();
        int z = chunk.getMiddleBlockZ();
        var center = new BlockPos(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1, z);
        var biome = level.getBiome(center);
        if (biome.is(BiomeTags.IS_OCEAN) || biome.is(BiomeTags.IS_RIVER) || biome.is(BiomeTags.IS_BEACH)
                || biome.is(BiomeTags.IS_JUNGLE) || biome.is(BiomeTags.IS_MOUNTAIN) || biome.is(Tags.Biomes.IS_MOUNTAIN)
                || biome.is(Tags.Biomes.IS_SWAMP)) return false;
        boolean sandy = biome.is(Biomes.DESERT);
        if (STRUCTURES[first].generate(context, center, sandy, random)) return true;
        if (wickerOnly) return false;
        Structure[] remaining = STRUCTURES.clone();
        remaining[first] = remaining[remaining.length - 1];
        for (int count = remaining.length - 1; count > 0; count--) {
            int index = random.nextInt(count);
            Structure alternate = remaining[index];
            remaining[index] = remaining[count - 1];
            if (alternate.generate(context, center, sandy, random)) return true;
        }
        return false;
    }
}
