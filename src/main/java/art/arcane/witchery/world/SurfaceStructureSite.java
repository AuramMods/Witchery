package art.arcane.witchery.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

record SurfaceStructureSite(WorldGenLevel level, BlockPos center, Direction facing, int width, int depth, boolean sandy) {
    static SurfaceStructureSite atSurface(WorldGenLevel level, BlockPos center, Direction facing,
                                          int width, int depth, boolean sandy, int minimumSurface) {
        var site = new SurfaceStructureSite(level, center, facing, width, depth, sandy);
        int total = 0;
        for (int z = 0; z < depth; z++) {
            for (int x = 0; x < width; x++) {
                BlockPos pos = site.at(x, 0, z);
                int surface = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ());
                total += Math.max(minimumSurface, surface);
            }
        }
        return new SurfaceStructureSite(level, center.atY(Math.floorDiv(total, width * depth) - 1), facing, width, depth, sandy);
    }

    BlockPos at(int x, int y, int z) {
        return center.relative(facing.getCounterClockWise(), x - (width - 1) / 2)
                .relative(facing, z - (depth - 1) / 2).above(y);
    }

    Direction direction(Direction local) {
        return switch (local) {
            case NORTH -> facing.getOpposite();
            case SOUTH -> facing;
            case EAST -> facing.getCounterClockWise();
            case WEST -> facing.getClockWise();
            default -> local;
        };
    }

    boolean hasDryCorners() {
        return !wet(0, 0) && !wet(0, depth - 1) && !wet(width - 1, 0) && !wet(width - 1, depth - 1);
    }

    private boolean wet(int x, int z) {
        var state = level.getBlockState(at(x, -1, z));
        return !state.getFluidState().isEmpty() || state.is(Blocks.ICE);
    }

    Block surface() {
        return sandy ? Blocks.SAND : Blocks.GRASS_BLOCK;
    }

    void put(int x, int y, int z, Block block) {
        put(x, y, z, block.defaultBlockState());
    }

    void put(int x, int y, int z, BlockState state) {
        level.setBlock(at(x, y, z), state, Block.UPDATE_CLIENTS);
    }

    void putConnected(int x, int y, int z, Block block) {
        BlockPos pos = at(x, y, z);
        BlockState state = block.defaultBlockState();
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos adjacent = pos.relative(direction);
            state = state.updateShape(direction, level.getBlockState(adjacent), level, pos, adjacent);
        }
        put(x, y, z, state);
    }

    void fill(int x1, int y1, int z1, int x2, int y2, int z2, Block block) {
        BlockState state = block.defaultBlockState();
        for (int y = y1; y <= y2; y++) {
            for (int z = z1; z <= z2; z++) {
                for (int x = x1; x <= x2; x++) put(x, y, z, state);
            }
        }
    }

    void support(int x, int z) {
        BlockState foundation = (sandy ? Blocks.SAND : Blocks.DIRT).defaultBlockState();
        BlockPos.MutableBlockPos cursor = at(x, -1, z).mutable();
        while (!level.isOutsideBuildHeight(cursor)) {
            BlockState old = level.getBlockState(cursor);
            if (old.blocksMotion() && old.getFluidState().isEmpty() && !old.is(Blocks.ICE)) break;
            level.setBlock(cursor, foundation, Block.UPDATE_CLIENTS);
            cursor.move(Direction.DOWN);
        }
    }

    void clearAbove(int x, int z, int start, int clearance) {
        BlockPos.MutableBlockPos cursor = at(x, start, z).mutable();
        int stop = center.getY() + clearance;
        while (!level.isOutsideBuildHeight(cursor)) {
            if (level.isEmptyBlock(cursor)) {
                if (cursor.getY() >= stop) break;
            } else {
                level.setBlock(cursor, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
            cursor.move(Direction.UP);
        }
    }

    void spawner(int x, int y, int z, EntityType<?> mob, RandomSource random) {
        put(x, y, z, Blocks.SPAWNER);
        if (level.getBlockEntity(at(x, y, z)) instanceof SpawnerBlockEntity spawner) {
            spawner.setEntityId(mob, random);
            spawner.setChanged();
        }
    }
}
