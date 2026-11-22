package art.arcane.witchery.power;

import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;

import java.util.HashMap;
import java.util.Map;

public final class AltarNature {
    private static final int RADIUS = 14;
    private static final int MUSHROOM_BLOCK_LIMIT = 20;
    private static Map<Block, Source> sources;

    private AltarNature() {
    }

    public static int capacity(ServerLevel level, BlockPos altar) {
        if (sources == null) {
            sources = createSources();
        }
        Map<Source, Integer> counts = new HashMap<>();
        int minX = altar.getX() - RADIUS;
        int maxX = altar.getX() + RADIUS;
        int minZ = altar.getZ() - RADIUS;
        int maxZ = altar.getZ() + RADIUS;
        int minY = Math.max(level.getMinBuildHeight(), altar.getY() - RADIUS);
        int maxY = Math.min(level.getMaxBuildHeight() - 1, altar.getY() + RADIUS);
        for (int chunkX = SectionPos.blockToSectionCoord(minX); chunkX <= SectionPos.blockToSectionCoord(maxX); chunkX++) {
            for (int chunkZ = SectionPos.blockToSectionCoord(minZ); chunkZ <= SectionPos.blockToSectionCoord(maxZ); chunkZ++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(chunkX, chunkZ);
                if (chunk == null) {
                    continue;
                }
                int chunkMinX = Math.max(minX, SectionPos.sectionToBlockCoord(chunkX));
                int chunkMaxX = Math.min(maxX, SectionPos.sectionToBlockCoord(chunkX, 15));
                int chunkMinZ = Math.max(minZ, SectionPos.sectionToBlockCoord(chunkZ));
                int chunkMaxZ = Math.min(maxZ, SectionPos.sectionToBlockCoord(chunkZ, 15));
                for (BlockPos pos : BlockPos.betweenClosed(chunkMinX, minY, chunkMinZ, chunkMaxX, maxY, chunkMaxZ)) {
                    BlockState state = chunk.getBlockState(pos);
                    if (!state.isAir()) {
                        count(counts, state);
                    }
                }
            }
        }
        // Flattening merged the two original stem variants into a single block.
        // Stems use the slots left by the identifiable brown and red blocks.
        int unusedMushroomSlots = 2 * MUSHROOM_BLOCK_LIMIT
                - counts.getOrDefault(sources.get(Blocks.BROWN_MUSHROOM_BLOCK), 0)
                - counts.getOrDefault(sources.get(Blocks.RED_MUSHROOM_BLOCK), 0);
        counts.computeIfPresent(sources.get(Blocks.MUSHROOM_STEM), (source, count) -> Math.min(count, unusedMushroomSlots));
        return counts.entrySet().stream().mapToInt(entry -> entry.getKey().power() * entry.getValue()).sum();
    }

    private static void count(Map<Source, Integer> counts, BlockState state) {
        Block block = state.getBlock();
        if (block == Blocks.WATER && !state.getFluidState().isSource()) {
            return;
        }
        Source source = sources.get(block);
        if (source == null) {
            if (state.is(BlockTags.SAPLINGS)) {
                source = new Source(block, 4, 20);
            } else if (state.is(BlockTags.LOGS)) {
                source = new Source(block, 2, 50);
            } else if (state.is(BlockTags.LEAVES)) {
                source = new Source(block, 3, 100);
            } else if (block instanceof FlowerBlock || block instanceof CropBlock || state.is(BlockTags.FLOWERS) || state.is(BlockTags.CROPS)) {
                source = new Source(block, 2, 4);
            }
        }
        if (source != null && counts.getOrDefault(source, 0) < source.limit()) {
            counts.merge(source, 1, Integer::sum);
        }
    }

    private static Map<Block, Source> createSources() {
        Map<Block, Source> values = new HashMap<>();
        add(values, 4, 20, Blocks.OAK_SAPLING, Blocks.SPRUCE_SAPLING, Blocks.BIRCH_SAPLING,
                Blocks.JUNGLE_SAPLING, Blocks.ACACIA_SAPLING, Blocks.DARK_OAK_SAPLING);
        add(values, 2, 50, Blocks.OAK_LOG, Blocks.SPRUCE_LOG, Blocks.BIRCH_LOG, Blocks.JUNGLE_LOG,
                Blocks.OAK_WOOD, Blocks.SPRUCE_WOOD, Blocks.BIRCH_WOOD, Blocks.JUNGLE_WOOD);
        add(values, 2, 50, Blocks.ACACIA_LOG, Blocks.DARK_OAK_LOG, Blocks.ACACIA_WOOD, Blocks.DARK_OAK_WOOD);
        add(values, 3, 100, Blocks.OAK_LEAVES, Blocks.SPRUCE_LEAVES, Blocks.BIRCH_LEAVES, Blocks.JUNGLE_LEAVES);
        add(values, 3, 100, Blocks.ACACIA_LEAVES, Blocks.DARK_OAK_LEAVES);

        add(values, 2, 80, Blocks.GRASS_BLOCK);
        add(values, 1, 80, Blocks.DIRT, Blocks.COARSE_DIRT, Blocks.PODZOL);
        add(values, 1, 100, Blocks.FARMLAND);
        add(values, 3, 50, Blocks.GRASS, Blocks.FERN);
        add(values, 4, 30, Blocks.DANDELION);
        add(values, 4, 30, Blocks.POPPY, Blocks.BLUE_ORCHID, Blocks.ALLIUM, Blocks.AZURE_BLUET,
                Blocks.RED_TULIP, Blocks.ORANGE_TULIP, Blocks.WHITE_TULIP, Blocks.PINK_TULIP, Blocks.OXEYE_DAISY);
        add(values, 4, 20, Blocks.WHEAT);
        add(values, 1, 50, Blocks.WATER);
        add(values, 3, 20, Blocks.RED_MUSHROOM);
        add(values, 3, 20, Blocks.BROWN_MUSHROOM);
        add(values, 3, 50, Blocks.CACTUS);
        add(values, 3, 50, Blocks.SUGAR_CANE);
        add(values, 4, 20, Blocks.PUMPKIN, Blocks.CARVED_PUMPKIN);
        add(values, 3, 20, Blocks.PUMPKIN_STEM, Blocks.ATTACHED_PUMPKIN_STEM);
        add(values, 3, MUSHROOM_BLOCK_LIMIT, Blocks.BROWN_MUSHROOM_BLOCK);
        add(values, 3, MUSHROOM_BLOCK_LIMIT, Blocks.RED_MUSHROOM_BLOCK);
        add(values, 3, 2 * MUSHROOM_BLOCK_LIMIT, Blocks.MUSHROOM_STEM);
        add(values, 4, 20, Blocks.MELON);
        add(values, 3, 20, Blocks.MELON_STEM, Blocks.ATTACHED_MELON_STEM);
        add(values, 2, 50, Blocks.VINE);
        add(values, 1, 80, Blocks.MYCELIUM);
        add(values, 250, 1, Blocks.DRAGON_EGG);
        add(values, 3, 20, Blocks.COCOA);
        add(values, 4, 20, Blocks.CARROTS);
        add(values, 4, 20, Blocks.POTATOES);
        // These legacy plants were outside the original flower and crop classes.
        add(values, 0, 0, Blocks.SUNFLOWER, Blocks.LILAC, Blocks.ROSE_BUSH, Blocks.PEONY,
                Blocks.TALL_GRASS, Blocks.LARGE_FERN, Blocks.NETHER_WART);

        addWitchery(values, 40, 2, "demonheart");
        addWitchery(values, 4, 20, "belladonna", "mandrake", "artichoke", "snowbell", "embermoss", "witchsapling");
        addWitchery(values, 4, 50, "witchleaves");
        addWitchery(values, 3, 100, "witchlog");
        addWitchery(values, 3, 20, "spanishmoss", "somniancotton");
        addWitchery(values, 2, 20, "glintweed");
        addWitchery(values, 2, 10, "crittersnare", "bloodrose", "grassper");
        addWitchery(values, 1000, 1, "infinityegg");
        return Map.copyOf(values);
    }

    private static void addWitchery(Map<Block, Source> values, int power, int limit, String... blocks) {
        for (String block : blocks) {
            add(values, power, limit, WitcheryBlocks.getRequired(block).get());
        }
    }

    private static void add(Map<Block, Source> values, int power, int limit, Block first, Block... variants) {
        Source source = new Source(first, power, limit);
        values.put(first, source);
        for (Block variant : variants) {
            values.put(variant, source);
        }
    }

    private record Source(Block group, int power, int limit) {
    }
}
