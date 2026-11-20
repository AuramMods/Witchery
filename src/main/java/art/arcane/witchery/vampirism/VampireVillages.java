package art.arcane.witchery.vampirism;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.StructureTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.phys.AABB;

import java.util.ArrayDeque;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class VampireVillages {
    private static final int VISIT_RANGE = 32;
    private static final int HOME_SEARCH_RANGE = 64;
    private static final Comparator<BlockPos> HOME_ORDER = Comparator.<BlockPos>comparingInt(BlockPos::getX)
            .thenComparingInt(BlockPos::getZ).thenComparingInt(BlockPos::getY);

    private VampireVillages() {}

    static Optional<ChunkPos> nearby(ServerLevel world, BlockPos position) {
        return nearestGeneratedVillage(world, position).map(StructureStart::getChunkPos)
                .or(() -> nearbyOccupiedHomes(world.getPoiManager(), position).map(ChunkPos::new));
    }

    public static Optional<Territory> territory(ServerLevel world, BlockPos position) {
        return nearestGeneratedVillage(world, position).map(start -> {
            BoundingBox bounds = start.getBoundingBox();
            BlockPos center = bounds.getCenter().atY(position.getY());
            int radius = Math.max(VISIT_RANGE, Math.max(bounds.getXSpan(), bounds.getZSpan()) / 2);
            return new Territory(center, radius);
        }).or(() -> nearbyOccupiedHomes(world.getPoiManager(), position)
                .map(anchor -> new Territory(anchor, VISIT_RANGE)));
    }

    public record Territory(BlockPos center, int radius) {
        public AABB residents() {
            return new AABB(center).inflate(radius, 4, radius);
        }
    }

    public static void releaseResidentClaims(Villager resident) {
        if (resident.isSleeping()) resident.stopSleeping();
        resident.releasePoi(MemoryModuleType.HOME);
        resident.releasePoi(MemoryModuleType.JOB_SITE);
        resident.releasePoi(MemoryModuleType.POTENTIAL_JOB_SITE);
        resident.releasePoi(MemoryModuleType.MEETING_POINT);
    }

    private static Optional<StructureStart> nearestGeneratedVillage(ServerLevel world, BlockPos position) {
        return loadedVillages(world, position, VISIT_RANGE)
                .filter(start -> distanceTo(start.getBoundingBox(), position) <= VISIT_RANGE * VISIT_RANGE)
                .min(Comparator.comparingDouble(start -> distanceTo(start.getBoundingBox(), position)));
    }

    private static Stream<StructureStart> loadedVillages(ServerLevel world, BlockPos position, int radius) {
        var structures = world.registryAccess().registryOrThrow(Registries.STRUCTURE);
        var manager = world.structureManager();
        return ChunkPos.rangeClosed(new ChunkPos(position), (radius + 15) / 16)
                .filter(chunk -> world.hasChunk(chunk.x, chunk.z))
                .flatMap(chunk -> manager.startsForStructure(chunk, type -> structures.wrapAsHolder(type).is(StructureTags.VILLAGE)).stream())
                .distinct();
    }

    static Optional<BlockPos> forFeedingGrounds(ServerLevel world, BlockPos position) {
        int range = 512;
        var poi = world.getPoiManager();
        var generated = loadedVillages(world, position, range)
                .filter(start -> distanceTo(start.getBoundingBox(), position) <= range * range)
                .map(start -> start.getBoundingBox().getCenter())
                .filter(world::hasChunkAt)
                .map(center -> world.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, center));
        var occupied = ChunkPos.rangeClosed(new ChunkPos(position), range / 16)
                .filter(chunk -> world.hasChunk(chunk.x, chunk.z))
                .flatMap(chunk -> poi.getInChunk(type -> type.is(PoiTypes.HOME), chunk, PoiManager.Occupancy.IS_OCCUPIED))
                .map(PoiRecord::getPos)
                .filter(home -> home.distSqr(position) <= range * range)
                .min(Comparator.comparingDouble(home -> home.distSqr(position)))
                .map(home -> clusterAnchor(poi, home));
        return Stream.concat(generated, occupied.stream()).min(Comparator.comparingDouble(candidate -> candidate.distSqr(position)));
    }

    private static double distanceTo(BoundingBox box, BlockPos position) {
        return position.distToLowCornerSqr(Mth.clamp(position.getX(), box.minX(), box.maxX()),
                Mth.clamp(position.getY(), box.minY(), box.maxY()), Mth.clamp(position.getZ(), box.minZ(), box.maxZ()));
    }

    private static Optional<BlockPos> nearbyOccupiedHomes(PoiManager poi, BlockPos position) {
        return poi.findClosest(type -> type.is(PoiTypes.HOME), position, VISIT_RANGE, PoiManager.Occupancy.IS_OCCUPIED)
                .map(home -> clusterAnchor(poi, home));
    }

    private static BlockPos clusterAnchor(PoiManager poi, BlockPos firstHome) {
        var unvisited = poi.getInRange(type -> type.is(PoiTypes.HOME), firstHome, HOME_SEARCH_RANGE, PoiManager.Occupancy.IS_OCCUPIED)
                .map(PoiRecord::getPos).collect(Collectors.toSet());
        unvisited.remove(firstHome);
        var connected = new ArrayDeque<BlockPos>();
        connected.add(firstHome);
        BlockPos anchor = firstHome;
        while (!connected.isEmpty()) {
            BlockPos home = connected.remove();
            if (HOME_ORDER.compare(home, anchor) < 0) anchor = home;
            for (var iterator = unvisited.iterator(); iterator.hasNext();) {
                BlockPos candidate = iterator.next();
                if (candidate.distSqr(home) <= VISIT_RANGE * VISIT_RANGE) {
                    connected.add(candidate);
                    iterator.remove();
                }
            }
        }
        return anchor;
    }

    static boolean isCaged(Villager villager) {
        BlockPos pos = villager.blockPosition();
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (isEnclosure(villager.level(), pos.offset(x, 0, z))) return true;
            }
        }
        return false;
    }

    private static boolean isEnclosure(Level world, BlockPos center) {
        int bars = 0;
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                if (world.getBlockState(center.offset(x, 2, z)).canBeReplaced()) return false;
                if (x == 0 && z == 0) continue;
                for (int y = 0; y <= 1; y++) {
                    if (world.getBlockState(center.offset(x, y, z)).is(Blocks.IRON_BARS)) bars++;
                }
            }
        }
        return bars >= 15;
    }
}
