package art.arcane.witchery.world;

import art.arcane.witchery.Witchery;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class ChunkPaletteTests {
    private static final List<BlockState> STATES = List.of(Blocks.STONE, Blocks.GRANITE, Blocks.DIORITE,
                    Blocks.ANDESITE, Blocks.GRASS_BLOCK, Blocks.DIRT, Blocks.COARSE_DIRT, Blocks.PODZOL,
                    Blocks.COBBLESTONE, Blocks.OAK_PLANKS, Blocks.SPRUCE_PLANKS, Blocks.BIRCH_PLANKS,
                    Blocks.JUNGLE_PLANKS, Blocks.ACACIA_PLANKS, Blocks.DARK_OAK_PLANKS, Blocks.MANGROVE_PLANKS)
            .stream().map(Block::defaultBlockState).toList();

    @GameTest(template = "qa_coven_arena", batch = "witchery_chunk_palette")
    public static void concurrentReadersOnlySeePublishedStates(GameTestHelper helper) throws InterruptedException {
        var biomes = helper.getLevel().registryAccess().registryOrThrow(Registries.BIOME);
        var section = new LevelChunkSection(biomes);
        section.setBlockState(8, 8, 8, STATES.get(0));
        var current = new AtomicReference<>(section);
        var writing = new AtomicBoolean(true);
        var ready = new CountDownLatch(1);
        var failure = new AtomicReference<RuntimeException>();
        var reader = new Thread(() -> {
            ready.countDown();
            try {
                while (writing.get()) {
                    var state = current.get().getBlockState(8, 8, 8);
                    if (!STATES.contains(state)) throw new IllegalStateException("Unexpected palette state " + state);
                }
            } catch (RuntimeException error) {
                failure.set(error);
            }
        }, "Witchery palette test reader");
        reader.setDaemon(true);
        reader.start();
        try {
            ready.await();
            for (int trial = 0; trial < 8192 && failure.get() == null; trial++) {
                section = new LevelChunkSection(biomes);
                section.setBlockState(8, 8, 8, STATES.get(0));
                current.set(section);
                for (int index = 1; index < STATES.size(); index++) {
                    if ((index & 1) == 0) section.getStates().set(8, 8, 8, STATES.get(index));
                    else section.setBlockState(8, 8, 8, STATES.get(index));
                }
            }
        } finally {
            writing.set(false);
            reader.join(5000);
        }
        helper.assertTrue(!reader.isAlive(), "The palette reader completes");
        var error = failure.get();
        if (error != null) throw new IllegalStateException("A reader saw an unpublished palette entry", error);
        helper.succeed();
    }
}
