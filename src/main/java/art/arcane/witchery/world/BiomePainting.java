package art.arcane.witchery.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public final class BiomePainting {
    private static final int QUART_WIDTH = QuartPos.fromBlock(16);
    private static final int QUART_BLOCKS = QuartPos.toBlock(1);

    private BiomePainting() {}

    public static void paint(ServerLevel level, BlockPos center, int radius, Holder<Biome> biome) {
        paint(level, center, radius, biome, (minX, minZ) -> {
            int x = minX + QUART_BLOCKS / 2;
            int z = minZ + QUART_BLOCKS / 2;
            return insideCircle(center, radius, x, z) && level.getWorldBorder().isWithinBounds(x, z);
        });
    }

    public static void paint(ServerLevel level, BlockPos center, int[] rowWidths, Holder<Biome> biome) {
        int radius = rowWidths.length - 1;
        paint(level, center, radius, biome, (minX, minZ) -> {
            int closestX = Mth.clamp(center.getX(), minX, minX + QUART_BLOCKS - 1);
            int deltaX = Math.abs(closestX - center.getX());
            int startZ = Math.max(minZ, center.getZ() - radius);
            int endZ = Math.min(minZ + QUART_BLOCKS - 1, center.getZ() + radius);
            for (int z = startZ; z <= endZ; z++) {
                if (deltaX <= rowWidths[Math.abs(z - center.getZ())] && level.getWorldBorder().isWithinBounds(closestX, z)) {
                    return true;
                }
            }
            return false;
        });
    }

    private static void paint(ServerLevel level, BlockPos center, int radius, Holder<Biome> biome, QuartMask mask) {
        List<ChunkAccess> changed = new ArrayList<>();
        var chunks = level.getChunkSource();
        var sampler = chunks.randomState().sampler();
        int minChunkX = SectionPos.blockToSectionCoord(center.getX() - radius);
        int maxChunkX = SectionPos.blockToSectionCoord(center.getX() + radius);
        int minChunkZ = SectionPos.blockToSectionCoord(center.getZ() - radius);
        int maxChunkZ = SectionPos.blockToSectionCoord(center.getZ() + radius);
        for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
            for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
                var columns = quartColumns(mask, chunkX, chunkZ);
                if (columns.isEmpty()) continue;
                ChunkAccess chunk = level.getChunk(chunkX, chunkZ);
                chunk.fillBiomesFromNoise((x, y, z, ignored) -> columns.get(columnIndex(x, z))
                        ? biome : chunk.getNoiseBiome(x, y, z), sampler);
                chunk.setUnsaved(true);
                changed.add(chunk);
            }
        }
        chunks.chunkMap.resendBiomesForChunks(changed);
    }

    private static BitSet quartColumns(QuartMask mask, int chunkX, int chunkZ) {
        var result = new BitSet(QUART_WIDTH * QUART_WIDTH);
        for (int qz = 0; qz < QUART_WIDTH; qz++) {
            for (int qx = 0; qx < QUART_WIDTH; qx++) {
                int x = SectionPos.sectionToBlockCoord(chunkX) + QuartPos.toBlock(qx);
                int z = SectionPos.sectionToBlockCoord(chunkZ) + QuartPos.toBlock(qz);
                if (mask.includes(x, z)) result.set(columnIndex(qx, qz));
            }
        }
        return result;
    }

    private static int columnIndex(int quartX, int quartZ) {
        return Math.floorMod(quartZ, QUART_WIDTH) * QUART_WIDTH + Math.floorMod(quartX, QUART_WIDTH);
    }

    public static boolean insideCircle(BlockPos center, int radius, int x, int z) {
        int dx = x - center.getX();
        int dz = z - center.getZ();
        return dx * dx + dz * dz <= radius * radius;
    }

    private interface QuartMask {
        boolean includes(int minX, int minZ);
    }
}
