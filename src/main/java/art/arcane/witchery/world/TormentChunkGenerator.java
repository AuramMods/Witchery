package art.arcane.witchery.world;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryBlocks;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
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

public final class TormentChunkGenerator extends ChunkGenerator {
    public static final Codec<TormentChunkGenerator> CODEC = BiomeSource.CODEC.fieldOf("biome_source")
            .xmap(TormentChunkGenerator::new, TormentChunkGenerator::getBiomeSource).codec();

    private static final ResourceLocation MAZE_SEED = new ResourceLocation(Witchery.MODID, "torment_maze");
    private static final int MIN_FLOOR = TormentMaze.FIRST_FLOOR - 1;
    private static final int MAX_CEILING = TormentMaze.floorY(TormentMaze.LEVELS - 1) + 5;
    private volatile SeededMaze cached;

    private record SeededMaze(long seed, TormentMaze maze) {}

    public TormentChunkGenerator(BiomeSource source) {
        super(source);
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    private TormentMaze maze(RandomState random) {
        long seed = random.getOrCreateRandomFactory(MAZE_SEED).at(0, 0, 0).nextLong();
        SeededMaze existing = cached;
        if (existing != null && existing.seed() == seed) return existing.maze();
        synchronized (this) {
            existing = cached;
            if (existing == null || existing.seed() != seed) {
                existing = new SeededMaze(seed, new TormentMaze(seed));
                cached = existing;
            }
            return existing.maze();
        }
    }

    private static BlockState state(TormentMaze.BlockKind kind) {
        return switch (kind) {
            case AIR -> Blocks.AIR.defaultBlockState();
            case FORCE -> WitcheryBlocks.getRequired("force").get().defaultBlockState();
            case STONE -> WitcheryBlocks.getRequired("tormentstone").get().defaultBlockState();
            case MYCELIUM -> Blocks.MYCELIUM.defaultBlockState();
            case CHEST -> WitcheryBlocks.getRequired("refillingchest").get().defaultBlockState();
            case PORTAL -> WitcheryBlocks.getRequired("tormentportal").get().defaultBlockState();
        };
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender, RandomState random,
                                                        StructureManager structures, ChunkAccess chunk) {
        int xMin = chunk.getPos().getMinBlockX();
        int zMin = chunk.getPos().getMinBlockZ();
        int mazeX = TormentMaze.originalX(xMin);
        if (!TormentMaze.intersects(mazeX, zMin, mazeX + 15, zMin + 15)) {
            return CompletableFuture.completedFuture(chunk);
        }

        TormentMaze maze = maze(random);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        Heightmap surface = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);
        Heightmap floor = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        int bottom = Math.max(chunk.getMinBuildHeight(), MIN_FLOOR);
        int top = Math.min(chunk.getMaxBuildHeight() - 1, MAX_CEILING);
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                if (!TormentMaze.covers(mazeX + x, zMin + z)) continue;
                for (int y = bottom; y <= top; y++) {
                    TormentMaze.BlockKind kind = maze.at(mazeX + x, y, zMin + z);
                    if (kind == TormentMaze.BlockKind.AIR) continue;
                    BlockState state = state(kind);
                    chunk.setBlockState(pos.set(xMin + x, y, zMin + z), state, false);
                    surface.update(x, y, z, state);
                    floor.update(x, y, z, state);
                    if (state.hasBlockEntity()) {
                        CompoundTag pending = new CompoundTag();
                        pending.putInt("x", pos.getX());
                        pending.putInt("y", pos.getY());
                        pending.putInt("z", pos.getZ());
                        pending.putString("id", "DUMMY");
                        chunk.setBlockEntityNbt(pending);
                    }
                }
            }
        }
        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types type, LevelHeightAccessor height, RandomState random) {
        int mazeX = TormentMaze.originalX(x);
        if (TormentMaze.covers(mazeX, z)) {
            TormentMaze maze = maze(random);
            for (int y = Math.min(MAX_CEILING, height.getMaxBuildHeight() - 1); y >= Math.max(MIN_FLOOR, height.getMinBuildHeight()); y--) {
                if (type.isOpaque().test(state(maze.at(mazeX, y, z)))) return y + 1;
            }
        }
        return height.getMinBuildHeight();
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor height, RandomState random) {
        int mazeX = TormentMaze.originalX(x);
        TormentMaze maze = TormentMaze.covers(mazeX, z) ? maze(random) : null;
        BlockState[] states = new BlockState[height.getHeight()];
        int bottom = height.getMinBuildHeight();
        for (int index = 0; index < states.length; index++) {
            states[index] = maze == null ? Blocks.AIR.defaultBlockState() : state(maze.at(mazeX, bottom + index, z));
        }
        return new NoiseColumn(bottom, states);
    }

    @Override
    public int getGenDepth() {
        return 256;
    }

    @Override
    public int getSeaLevel() {
        return 0;
    }

    @Override
    public int getMinY() {
        return 0;
    }

    @Override
    public void applyCarvers(WorldGenRegion region, long seed, RandomState random, BiomeManager biomes,
                             StructureManager structures, ChunkAccess chunk, GenerationStep.Carving step) {}

    @Override
    public void buildSurface(WorldGenRegion region, StructureManager structures, RandomState random, ChunkAccess chunk) {}

    @Override
    public void spawnOriginalMobs(WorldGenRegion region) {}

    @Override
    public void addDebugScreenInfo(List<String> lines, RandomState random, BlockPos position) {}
}
