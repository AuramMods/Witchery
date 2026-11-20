package art.arcane.witchery.world;

import art.arcane.witchery.registry.WitcheryBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.blending.Blender;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public final class MirrorChunkGenerator extends ChunkGenerator {
    public static final Codec<MirrorChunkGenerator> CODEC = BiomeSource.CODEC.fieldOf("biome_source")
            .xmap(MirrorChunkGenerator::new, MirrorChunkGenerator::getBiomeSource).codec();
    private static final int LEGACY_TOP = 255;

    public MirrorChunkGenerator(BiomeSource source) {
        super(source);
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    private static boolean wallAt(int x, int y, int z) {
        int cellY = y & 15;
        int cellX = x & 15;
        int cellZ = z & 15;
        return y >= 0 && y < LEGACY_TOP && (cellY <= 5 || cellY == 15
                || cellX < 4 || cellX > 12 || cellZ < 4 || cellZ > 12);
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender, RandomState random,
                                                        StructureManager structures, ChunkAccess chunk) {
        BlockState wall = WitcheryBlocks.getRequired("mirrorwall").get().defaultBlockState();
        BlockPos.MutableBlockPos place = new BlockPos.MutableBlockPos();
        Heightmap surface = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        Heightmap floor = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        for (int y = Math.max(0, chunk.getMinBuildHeight()); y < Math.min(LEGACY_TOP, chunk.getMaxBuildHeight()); y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    if (wallAt(x, y, z)) {
                        chunk.setBlockState(place.set(x, y, z), wall, false);
                        surface.update(x, y, z, wall);
                        floor.update(x, y, z, wall);
                    }
                }
            }
        }
        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor height, RandomState random) {
        if (type.isOpaque().test(WitcheryBlocks.getRequired("mirrorwall").get().defaultBlockState())) {
            for (int y = Math.min(LEGACY_TOP, height.getMaxBuildHeight()) - 1; y >= height.getMinBuildHeight(); y--) {
                if (wallAt(x, y, z)) return y + 1;
            }
        }
        return height.getMinBuildHeight();
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor height, RandomState random) {
        BlockState[] states = new BlockState[height.getHeight()];
        BlockState wall = WitcheryBlocks.getRequired("mirrorwall").get().defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();
        int bottom = height.getMinBuildHeight();
        for (int index = 0; index < states.length; index++) {
            states[index] = wallAt(x, bottom + index, z) ? wall : air;
        }
        return new NoiseColumn(bottom, states);
    }

    @Override
    public int getGenDepth() {
        return 384;
    }

    @Override
    public int getSeaLevel() {
        return 0;
    }

    @Override
    public int getMinY() {
        return -64;
    }

    @Override
    public void applyCarvers(WorldGenRegion region, long seed, RandomState random, BiomeManager biomes,
                             StructureManager structures, ChunkAccess chunk, GenerationStep.Carving step) {
    }

    @Override
    public void buildSurface(WorldGenRegion region, StructureManager structures, RandomState random, ChunkAccess chunk) {
    }

    @Override
    public void spawnOriginalMobs(WorldGenRegion region) {
    }

    @Override
    public void addDebugScreenInfo(List<String> lines, RandomState random, BlockPos position) {
    }
}
