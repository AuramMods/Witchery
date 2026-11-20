package art.arcane.witchery.world;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

final class TormentMaze {
    static final int LEVELS = 6;
    static final int FIRST_FLOOR = 10;
    static final int FLOOR_SPACING = 15;
    static final int CENTER_X = 8;
    static final int ENTRANCE_Z = 8;
    static final int EXIT_Z = 68;
    private static final int CELLS = 31;
    private static final int WIDTH = CELLS * 2 + 1;
    private static final int WEST_EDGE = CENTER_X - CELLS;
    private static final int NORTH_EDGE = ENTRANCE_Z - 2;

    enum BlockKind {
        AIR, FORCE, STONE, MYCELIUM, CHEST, PORTAL
    }

    private enum Column {
        WALL, PASSAGE, MYCELIUM, CHEST, PORTAL, PORTAL_FRAME
    }

    private record Step(int dx, int dz, int opening, int opposite) {}

    private static final List<Step> STEPS = List.of(
            new Step(0, -1, 1, 2), new Step(0, 1, 2, 1),
            new Step(1, 0, 4, 8), new Step(-1, 0, 8, 4));

    private final Column[][][] floors = new Column[LEVELS][WIDTH + 2][WIDTH];

    TormentMaze(long seed) {
        Random random = new Random(seed);
        for (Column[][] floor : floors) {
            buildFloor(floor, random);
        }
    }

    static int floorY(int level) {
        return FIRST_FLOOR + level * FLOOR_SPACING;
    }

    static boolean covers(int x, int z) {
        return x >= WEST_EDGE && x < WEST_EDGE + WIDTH
                && z >= NORTH_EDGE && z < NORTH_EDGE + WIDTH + 2;
    }

    static boolean intersects(int minX, int minZ, int maxX, int maxZ) {
        return maxX >= WEST_EDGE && minX < WEST_EDGE + WIDTH
                && maxZ >= NORTH_EDGE && minZ < NORTH_EDGE + WIDTH + 2;
    }

    BlockKind at(int x, int y, int z) {
        if (!covers(x, z) || y < FIRST_FLOOR - 1) return BlockKind.AIR;
        int floor = (y - FIRST_FLOOR + 1) / FLOOR_SPACING;
        if (floor >= LEVELS) return BlockKind.AIR;
        int height = y - floorY(floor);
        if (height > 5) return BlockKind.AIR;
        Column column = floors[floor][z - NORTH_EDGE][x - WEST_EDGE];
        if (column == null) return BlockKind.AIR;
        if (column == Column.WALL) return height < 0 ? BlockKind.AIR : BlockKind.FORCE;
        if (height == -1) return BlockKind.STONE;
        if (height == 5) return BlockKind.FORCE;
        if (height == 0) {
            return switch (column) {
                case MYCELIUM -> BlockKind.MYCELIUM;
                case CHEST -> BlockKind.CHEST;
                default -> BlockKind.STONE;
            };
        }
        if (height <= 3 && column == Column.PORTAL_FRAME) return BlockKind.STONE;
        if (height <= 3 && column == Column.PORTAL) return height < 3 ? BlockKind.PORTAL : BlockKind.STONE;
        return BlockKind.AIR;
    }

    private static void buildFloor(Column[][] columns, Random random) {
        int[][] openings = new int[CELLS][CELLS];
        carve(openings, 0, 0, random);
        for (int z = 0; z < WIDTH; z++) {
            for (int x = 0; x < WIDTH; x++) {
                boolean inside = x < WIDTH - 1 && z < WIDTH - 1;
                boolean passage = inside && ((x & 1) != 0
                        ? (z & 1) != 0 || (openings[z / 2][x / 2] & 1) != 0
                        : (z & 1) != 0 && (openings[z / 2][x / 2] & 8) != 0);
                columns[z][x] = passage ? passage(random) : Column.WALL;
            }
        }

        room(columns, CELLS - 3, 1, 7, 7, random);
        room(columns, CELLS - 3, 2 * CELLS - 7, 7, 9, random);
        columns[2 * CELLS][CELLS] = Column.PORTAL;
        columns[2 * CELLS][CELLS - 1] = Column.PORTAL_FRAME;
        columns[2 * CELLS][CELLS + 1] = Column.PORTAL_FRAME;

        int westChestZ = CELLS + random.nextInt(11) - 5;
        room(columns, 1, westChestZ - 2, 5, 5, random);
        columns[westChestZ][3] = Column.CHEST;
        int eastChestZ = CELLS + random.nextInt(11) - 5;
        room(columns, 2 * CELLS - 5, eastChestZ - 2, 5, 5, random);
        columns[eastChestZ][2 * CELLS - 3] = Column.CHEST;
        room(columns, CELLS - 3, CELLS - 3, 7, 7, random);
        columns[CELLS][CELLS] = Column.CHEST;
    }

    private static void room(Column[][] columns, int left, int top, int width, int depth, Random random) {
        for (int z = top; z < top + depth; z++) {
            for (int x = left; x < left + width; x++) {
                columns[z][x] = passage(random);
            }
        }
    }

    private static Column passage(Random random) {
        return random.nextInt(100) == 0 ? Column.MYCELIUM : Column.PASSAGE;
    }

    private static void carve(int[][] openings, int x, int z, Random random) {
        List<Step> steps = new ArrayList<>(STEPS);
        Collections.shuffle(steps, random);
        for (Step step : steps) {
            int nextX = x + step.dx();
            int nextZ = z + step.dz();
            if (nextX >= 0 && nextX < CELLS && nextZ >= 0 && nextZ < CELLS && openings[nextZ][nextX] == 0) {
                openings[z][x] |= step.opening();
                openings[nextZ][nextX] |= step.opposite();
                carve(openings, nextX, nextZ, random);
            }
        }
    }
}
