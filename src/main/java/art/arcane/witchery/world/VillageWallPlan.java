package art.arcane.witchery.world;

import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.JigsawBlock;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.IntBinaryOperator;

public final class VillageWallPlan {
    private static final int ROAD_SIDE_MARGIN = 20;
    private static final int ROAD_END_MARGIN = 7;
    private static final int BUILDING_MARGIN = 3;
    private static final int REFERENCE_RADIUS = 8 * 16;
    private static final int GATE_PIER_DISTANCE = 4;
    private static final int TERRAIN_SAMPLE_STEP = 4;

    private VillageWallPlan() {}

    public static void assemble(Structure.GenerationContext context, List<StructurePiece> pieces, StructurePiecesBuilder builder) {
        boolean hasWall = false;
        for (var piece : pieces) {
            if (piece instanceof PoolElementStructurePiece poolPiece
                    && WitcheryVillagePools.templateId(poolPiece.getElement()).filter(WitcheryVillagePools.WALL_ANCHOR::equals).isPresent()) {
                hasWall = true;
            } else {
                builder.addPiece(piece);
            }
        }
        if (!hasWall) return;

        var footprint = new Footprint(context.chunkPos());
        var palette = VillageWallPiece.Palette.STONE;
        var connectors = new LinkedHashSet<Port>();
        var extensions = new ArrayList<Port>();
        var jigsawRandom = RandomSource.create(0);
        boolean hasRoad = false;
        for (var piece : builder.build().pieces()) {
            if (!(piece instanceof PoolElementStructurePiece poolPiece)) continue;
            var box = piece.getBoundingBox();
            var path = WitcheryVillagePools.templateId(poolPiece.getElement()).map(id -> id.getPath()).orElse("");
            boolean street = path.contains("/streets/");
            boolean terminator = path.contains("/terminators/");
            boolean center = path.contains("/town_centers/");
            if (center) palette = VillageWallPiece.Palette.forVillage(path);

            if (street || terminator || center) {
                var ports = poolPiece.getElement().getShuffledJigsawBlocks(context.structureTemplateManager(),
                        poolPiece.getPosition(), poolPiece.getRotation(), jigsawRandom).stream()
                        .filter(info -> info.nbt() != null && info.nbt().getString("name").equals("minecraft:street"))
                        .map(info -> new Port(info.pos(), JigsawBlock.getFrontFacing(info.state())))
                        .filter(port -> port.facing().getAxis().isHorizontal())
                        .toList();
                connectors.addAll(ports);
                if (street && !ports.isEmpty()) {
                    hasRoad = true;
                    footprint.coverStreet(ports);
                    continue;
                }
                if (terminator) {
                    for (var port : ports) {
                        var outward = port.facing().getOpposite();
                        var pos = port.pos().mutable();
                        while (box.isInside(pos.relative(outward))) pos.move(outward);
                        extensions.add(new Port(pos.immutable(), outward));
                    }
                }
            }
            if (center || box.getXSpan() >= 3 && box.getZSpan() >= 3) {
                footprint.cover(box.minX() - BUILDING_MARGIN, box.minZ() - BUILDING_MARGIN,
                        box.maxX() + BUILDING_MARGIN, box.maxZ() + BUILDING_MARGIN);
            }
        }
        if (!hasRoad) return;

        // Vertical projections move some connecting pieces relative to each other.
        // Join street connectors by their horizontal positions and facing.
        Set<Port> horizontal = new LinkedHashSet<>();
        for (var port : connectors) horizontal.add(new Port(port.pos().atY(0), port.facing()));
        for (var port : horizontal) {
            var mate = new Port(port.pos().relative(port.facing()), port.facing().getOpposite());
            if (!horizontal.contains(mate)) extensions.add(port);
        }

        var generator = context.chunkGenerator();
        var heights = context.heightAccessor();
        var samples = new Long2IntOpenHashMap();
        IntBinaryOperator sample = (x, z) -> samples.computeIfAbsent(ChunkPos.asLong(x, z), (long key) ->
                generator.getBaseHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, heights, context.randomState()));
        var columns = footprint.outline(extensions, (x, z) -> interpolateHeight(sample, x, z),
                heights.getMinBuildHeight(), heights.getMaxBuildHeight());
        if (!columns.isEmpty()) builder.addPiece(new VillageWallPiece(columns, palette, heights.getMinBuildHeight()));
    }

    private static int interpolateHeight(IntBinaryOperator sample, int x, int z) {
        // A network of noisy terrain-column queries per village stalls structure searches.
        // Sample the underlying terrain at noise-cell intervals and let placement extend support to the actual floor.
        int step = TERRAIN_SAMPLE_STEP;
        int dx = Math.floorMod(x, step);
        int dz = Math.floorMod(z, step);
        int left = x - dx;
        int north = z - dz;
        int top = sample.applyAsInt(left, north) * (step - dx) + sample.applyAsInt(left + step, north) * dx;
        int bottom = sample.applyAsInt(left, north + step) * (step - dx) + sample.applyAsInt(left + step, north + step) * dx;
        return Math.floorDiv(top * (step - dz) + bottom * dz, step * step);
    }

    private record Port(BlockPos pos, Direction facing) {}

    private static final class Footprint {
        private final int minX;
        private final int minZ;
        private final boolean[][] town;
        private final int size;

        private Footprint(ChunkPos center) {
            // Keep the two-block parapet inside the village start's vanilla reference radius.
            minX = center.getMinBlockX() - REFERENCE_RADIUS + 2;
            minZ = center.getMinBlockZ() - REFERENCE_RADIUS + 2;
            size = 16 + REFERENCE_RADIUS * 2 - 4;
            town = new boolean[size + 2][size + 2];
        }

        private void cover(int worldMinX, int worldMinZ, int worldMaxX, int worldMaxZ) {
            int firstX = Math.max(1, worldMinX - minX + 1);
            int lastX = Math.min(size, worldMaxX - minX + 1);
            int firstZ = Math.max(1, worldMinZ - minZ + 1);
            int lastZ = Math.min(size, worldMaxZ - minZ + 1);
            if (firstZ > lastZ) return;
            for (int x = firstX; x <= lastX; x++) Arrays.fill(town[x], firstZ, lastZ + 1, true);
        }

        private void coverStreet(List<Port> ports) {
            // A bend or crossing contributes a leg for each outgoing connector.
            // Use the centers of the connecting road lanes, which can be offset from the template box.
            var allX = ports.stream().mapToInt(port -> port.pos().getX()).average().orElseThrow();
            var allZ = ports.stream().mapToInt(port -> port.pos().getZ()).average().orElseThrow();
            int centerX = (int) Math.round(ports.stream().filter(port -> port.facing().getAxis() == Direction.Axis.Z)
                    .mapToInt(port -> port.pos().getX()).average().orElse(allX));
            int centerZ = (int) Math.round(ports.stream().filter(port -> port.facing().getAxis() == Direction.Axis.X)
                    .mapToInt(port -> port.pos().getZ()).average().orElse(allZ));
            for (var port : ports) {
                boolean eastWest = port.facing().getAxis() == Direction.Axis.X;
                int marginX = eastWest ? ROAD_END_MARGIN : ROAD_SIDE_MARGIN + 1;
                int marginZ = eastWest ? ROAD_SIDE_MARGIN + 1 : ROAD_END_MARGIN;
                var pos = port.pos();
                cover(Math.min(centerX, pos.getX()) - marginX, Math.min(centerZ, pos.getZ()) - marginZ,
                        Math.max(centerX, pos.getX()) + marginX, Math.max(centerZ, pos.getZ()) + marginZ);
            }
        }

        private List<VillageWallPiece.Column> outline(List<Port> outlets, IntBinaryOperator height, int minY, int maxY) {
            bridgeShortGaps();
            boolean[][] edge = outerBoundary();
            byte[][] gates = new byte[size + 2][size + 2];
            for (var outlet : outlets) markGate(outlet, edge, gates);
            int[][] terrain = new int[size + 2][size + 2];
            int[][] crests = new int[size + 2][size + 2];
            for (int[] row : crests) Arrays.fill(row, Integer.MIN_VALUE);

            var result = new ArrayList<VillageWallPiece.Column>();
            int guardDistance = 0;
            for (int x = 1; x <= size; x++) {
                for (int z = 1; z <= size; z++) {
                    if (!edge[x][z]) continue;
                    int floor = floor(x, z, terrain, height, minY);
                    int crest = floor + 9;
                    int priorCrest = Math.max(crests[x - 1][z], crests[x][z - 1]);
                    if (priorCrest != Integer.MIN_VALUE) crest = priorCrest + Integer.compare(crest, priorCrest);
                    // Leave a passable arch even when the road end climbs faster than the adjacent rampart.
                    if (gates[x][z] != 0) crest = Math.max(crest, surface(x, z, terrain, height, minY) + 6);
                    crest = Math.min(crest, maxY - 2);
                    if (crest <= floor + 1) continue;
                    crests[x][z] = crest;
                    boolean guard = ++guardDistance == 201;
                    if (guard) guardDistance = 0;
                    result.add(new VillageWallPiece.Column(minX + x - 1, minZ + z - 1, floor, crest,
                            mask(x, z, edge, gates, false), mask(x, z, edge, gates, true), gates[x][z], guard));
                }
            }
            return result;
        }

        private void bridgeShortGaps() {
            for (int x = 1; x <= size; x++) {
                for (int z = 1; z <= size; z++) {
                    if (!town[x][z]) continue;
                    for (int gap = 2; gap <= 6; gap++) {
                        if (x + gap <= size && town[x + gap][z] && !town[x + gap - 1][z]) {
                            for (int fill = 1; fill < gap; fill++) town[x + fill][z] = true;
                        }
                        if (z + gap <= size && town[x][z + gap] && !town[x][z + gap - 1]) {
                            for (int fill = 1; fill < gap; fill++) town[x][z + fill] = true;
                        }
                    }
                }
            }
        }

        private boolean[][] outerBoundary() {
            boolean[][] outside = new boolean[size + 2][size + 2];
            boolean[][] edge = new boolean[size + 2][size + 2];
            var search = new ArrayDeque<Integer>();
            int stride = size + 2;
            search.add(0);
            outside[0][0] = true;
            while (!search.isEmpty()) {
                int cell = search.removeFirst();
                int x = cell / stride;
                int z = cell % stride;
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        int adjacentX = x + dx;
                        int adjacentZ = z + dz;
                        if (adjacentX < 0 || adjacentX >= stride || adjacentZ < 0 || adjacentZ >= stride) continue;
                        if (town[adjacentX][adjacentZ]) {
                            edge[adjacentX][adjacentZ] = true;
                        } else if ((dx == 0 || dz == 0) && !outside[adjacentX][adjacentZ]) {
                            outside[adjacentX][adjacentZ] = true;
                            search.addLast(adjacentX * stride + adjacentZ);
                        }
                    }
                }
            }
            return edge;
        }

        private void markGate(Port outlet, boolean[][] edge, byte[][] gates) {
            var facing = outlet.facing();
            int x = outlet.pos().getX() - minX + 1;
            int z = outlet.pos().getZ() - minZ + 1;
            while (inBounds(x, z) && town[x][z]) {
                if (!town[x + facing.getStepX()][z + facing.getStepZ()]) {
                    var side = facing.getClockWise();
                    int dx = side.getStepX();
                    int dz = side.getStepZ();
                    for (int offset = -GATE_PIER_DISTANCE; offset <= GATE_PIER_DISTANCE; offset++) {
                        int pierX = x + offset * dx;
                        int pierZ = z + offset * dz;
                        if (!inBounds(pierX, pierZ) || !edge[pierX][pierZ] || gates[pierX][pierZ] != 0) return;
                    }
                    byte axis = (byte) (facing.getAxis() == Direction.Axis.X ? 1 : 2);
                    for (int offset = -1; offset <= 1; offset++) gates[x + offset * dx][z + offset * dz] = axis;
                    return;
                }
                x += facing.getStepX();
                z += facing.getStepZ();
            }
        }

        private boolean inBounds(int x, int z) {
            return x > 0 && z > 0 && x <= size && z <= size;
        }

        private int floor(int x, int z, int[][] terrain, IntBinaryOperator height, int minY) {
            int floor = Integer.MAX_VALUE;
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    floor = Math.min(floor, surface(x + dx, z + dz, terrain, height, minY) - 2);
                }
            }
            return Math.max(floor, minY - 1);
        }

        private int surface(int x, int z, int[][] terrain, IntBinaryOperator height, int minY) {
            if (terrain[x][z] == 0) terrain[x][z] = Math.max(0, height.applyAsInt(minX + x - 1, minZ + z - 1) - minY) + 1;
            return terrain[x][z] + minY - 1;
        }

        private static int mask(int x, int z, boolean[][] edge, byte[][] gates, boolean wantGate) {
            int result = 0;
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (wantGate ? gates[x + dx][z + dz] != 0 : edge[x + dx][z + dz]) {
                        result |= VillageWallPiece.Column.bit(dx, dz);
                    }
                }
            }
            return result;
        }
    }
}
