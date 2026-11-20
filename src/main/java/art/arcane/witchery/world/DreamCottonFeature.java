package art.arcane.witchery.world;

import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.common.Tags;

public final class DreamCottonFeature extends Feature<NoneFeatureConfiguration> {
    public DreamCottonFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        if (!level.getLevel().dimension().equals(WitcheryDimensions.DREAM)) {
            return false;
        }
        var biome = level.getBiome(context.origin());
        if (!biome.is(BiomeTags.IS_JUNGLE) && (biome.is(BiomeTags.IS_OCEAN) || biome.is(BiomeTags.IS_RIVER)
                || biome.is(BiomeTags.IS_BEACH) || biome.is(BiomeTags.IS_MOUNTAIN) || biome.is(Tags.Biomes.IS_SWAMP))) {
            return false;
        }
        RandomSource random = context.random();
        if (random.nextInt(4) == 0) {
            return false;
        }
        BlockPos corner = context.origin();
        int x = corner.getX() + random.nextInt(16) + 8;
        int z = corner.getZ() + random.nextInt(16) + 8;
        int lowest = level.getMinBuildHeight() + 1;
        int highest = Math.min(level.getMaxBuildHeight(), level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) + 32);
        BlockPos centre = new BlockPos(x, lowest + random.nextInt(Math.max(1, highest - lowest)), z);
        BlockState cotton = WitcheryBlocks.getRequired("somniancotton").get().defaultBlockState();
        boolean placed = false;
        for (int attempt = 0; attempt < 64; attempt++) {
            BlockPos candidate = centre.offset(random.nextInt(8) - random.nextInt(8), random.nextInt(4) - random.nextInt(4),
                    random.nextInt(8) - random.nextInt(8));
            if (!level.isOutsideBuildHeight(candidate) && level.ensureCanWrite(candidate)
                    && level.isEmptyBlock(candidate) && cotton.canSurvive(level, candidate)) {
                placed |= level.setBlock(candidate, cotton, Block.UPDATE_CLIENTS);
            }
        }
        return placed;
    }
}
