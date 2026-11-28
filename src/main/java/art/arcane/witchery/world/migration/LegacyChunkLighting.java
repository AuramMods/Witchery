package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.level.ChunkDataEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Collections;
import java.util.Queue;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Mod.EventBusSubscriber(modid = Witchery.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class LegacyChunkLighting {
    private static final String REVISION_KEY = "witchery:light_revision";
    private static final int CURRENT_REVISION = 1;
    private static final Set<ChunkAccess> PENDING = Collections.synchronizedSet(
            Collections.newSetFromMap(new WeakHashMap<>()));
    private static final Queue<LevelChunk> READY = new ConcurrentLinkedQueue<>();
    private static final Set<String> RELIT_BLOCKS = Set.of(
            "witchery:brew", "witchery:brewliquid", "witchery:coffinblock", "witchery:distilleryburning",
            "witchery:distilleryidle", "witchery:force", "witchery:hollowtears", "witchery:icedoubleslab",
            "witchery:icefencegate", "witchery:icepressureplate", "witchery:iceslab", "witchery:icestairs",
            "witchery:kettle", "witchery:mirrorblock", "witchery:mirrorblock2", "witchery:mirrorwall",
            "witchery:pitdirt", "witchery:pitgrass", "witchery:snowslab", "witchery:snowstairs", "witchery:spinningwheel",
            "witchery:spiritflowing", "witchery:stairswoodalder", "witchery:stairswoodhawthorn", "witchery:stairswoodrowan",
            "witchery:tormentstone", "witchery:witchesovenburning", "witchery:witchesovenidle", "witchery:witchwoodslab");

    private LegacyChunkLighting() {
    }

    @SubscribeEvent
    public static void onChunkRead(ChunkDataEvent.Load event) {
        if (event.getData().getInt(REVISION_KEY) < CURRENT_REVISION && event.getChunk() instanceof LevelChunk) {
            PENDING.add(event.getChunk());
        }
    }

    @SubscribeEvent
    public static void onChunkSave(ChunkDataEvent.Save event) {
        if (event.getData().getBoolean("isLightOn") && !PENDING.contains(unwrap(event.getChunk()))) {
            event.getData().putInt(REVISION_KEY, CURRENT_REVISION);
        }
    }

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (event.getLevel() instanceof ServerLevel && event.getChunk() instanceof LevelChunk chunk
                && PENDING.contains(chunk)) {
            READY.add(chunk);
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        for (LevelChunk chunk; (chunk = READY.poll()) != null; ) {
            ServerLevel level = (ServerLevel) chunk.getLevel();
            var chunks = level.getChunkSource();
            var chunkPos = chunk.getPos();
            if (chunks.getChunkNow(chunkPos.x, chunkPos.z) != chunk) continue;
            refreshLight(level, chunk);
        }
    }

    private static void refreshLight(ServerLevel level, LevelChunk chunk) {
        var positions = new java.util.ArrayList<net.minecraft.core.BlockPos>();
        chunk.findBlocks(state -> RELIT_BLOCKS.contains(state.getBlock().builtInRegistryHolder().key().location().toString()),
                (pos, state) -> positions.add(pos.immutable()));
        if (positions.isEmpty()) {
            PENDING.remove(chunk);
        } else {
            var chunks = level.getChunkSource();
            var light = chunks.getLightEngine();
            // The stored arrays are now available to remove old light on both sides of the chunk border.
            positions.forEach(light::checkBlock);
            // An already lit chunk uses this pass to finish the queued checks and retain its existing light sources.
            light.lightChunk(chunk, true).thenRunAsync(() -> {
                PENDING.remove(chunk);
                chunk.setUnsaved(true);
            }, level.getServer());
        }
    }

    @SubscribeEvent
    public static void onServerStopped(ServerStoppedEvent event) {
        READY.clear();
    }

    private static ChunkAccess unwrap(ChunkAccess chunk) {
        return chunk instanceof ImposterProtoChunk wrapper ? wrapper.getWrapped() : chunk;
    }
}
