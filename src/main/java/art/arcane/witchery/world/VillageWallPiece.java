package art.arcane.witchery.world;

import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcheryRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraftforge.common.world.PieceBeardifierModifier;

import java.util.ArrayList;
import java.util.List;

public final class VillageWallPiece extends StructurePiece implements PieceBeardifierModifier {
    private static final int COLUMN_SIZE = 8;
    private final List<Column> columns;
    private final Palette palette;

    VillageWallPiece(List<Column> columns, Palette palette, int minY) {
        super(WitcheryRegistries.VILLAGE_WALL.get(), 0, bounds(columns, minY));
        this.columns = List.copyOf(columns);
        this.palette = palette;
    }

    public VillageWallPiece(CompoundTag tag) {
        super(WitcheryRegistries.VILLAGE_WALL.get(), tag);
        this.palette = Palette.fromTag(tag.getString("Palette"));
        int[] saved = tag.getIntArray("Columns");
        var loaded = new ArrayList<Column>(saved.length / COLUMN_SIZE);
        for (int i = 0; i + COLUMN_SIZE <= saved.length; i += COLUMN_SIZE) {
            loaded.add(new Column(saved[i], saved[i + 1], saved[i + 2], saved[i + 3], saved[i + 4],
                    saved[i + 5], saved[i + 6], saved[i + 7] != 0));
        }
        this.columns = List.copyOf(loaded);
    }

    @Override
    protected void addAdditionalSaveData(StructurePieceSerializationContext context, CompoundTag tag) {
        int[] saved = new int[columns.size() * COLUMN_SIZE];
        int index = 0;
        for (var column : columns) {
            saved[index++] = column.x();
            saved[index++] = column.z();
            saved[index++] = column.floor();
            saved[index++] = column.crest();
            saved[index++] = column.edgeMask();
            saved[index++] = column.gateMask();
            saved[index++] = column.gateAxis();
            saved[index++] = column.guard() ? 1 : 0;
        }
        tag.putIntArray("Columns", saved);
        tag.putString("Palette", palette.name());
    }

    @Override
    public void postProcess(WorldGenLevel level, StructureManager structures, ChunkGenerator generator, RandomSource random,
                            BoundingBox chunkBounds, ChunkPos chunk, BlockPos pivot) {
        var wall = palette.wall().defaultBlockState();
        var stair = palette.stair().defaultBlockState();
        var rail = palette.rail().defaultBlockState();
        var pos = new BlockPos.MutableBlockPos();
        for (var column : columns) {
            if (!chunkBounds.intersects(column.x() - 2, column.z() - 2, column.x() + 2, column.z() + 2)) continue;
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (dx != 0 && column.gateAt(dx, 0) || dz != 0 && column.gateAt(0, dz)
                            || dx != 0 && dz != 0 && column.gateAt(dx, dz)) continue;
                    int x = column.x() + dx;
                    int z = column.z() + dz;
                    if (!chunkBounds.intersects(x, z, x, z)) continue;
                    int firstCourse = column.gateAxis() != 0 ? column.crest() - 2 : Math.min(column.floor() + 1,
                            level.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z));
                    for (int y = firstCourse; y < column.crest(); y++) {
                        place(level, chunkBounds, pos.set(x, y, z), wall, false);
                    }
                }
            }
        }
        for (var column : columns) {
            if (!chunkBounds.intersects(column.x() - 2, column.z() - 2, column.x() + 2, column.z() + 2)) continue;
            parapet(level, chunkBounds, pos, column, wall, stair);
            if (column.gateAxis() != 0) arch(level, chunkBounds, pos, column, rail, stair);
        }
        for (var column : columns) {
            if (column.guard()) spawnGuard(level, chunkBounds, pos.set(column.x(), column.crest(), column.z()));
        }
    }

    private static void parapet(WorldGenLevel level, BoundingBox bounds, BlockPos.MutableBlockPos pos, Column column,
                                 BlockState wall, BlockState stair) {
        for (Direction face : Direction.Plane.HORIZONTAL) {
            var side = face.getClockWise();
            int dx = face.getStepX();
            int dz = face.getStepZ();
            int sx = side.getStepX();
            int sz = side.getStepZ();
            if (column.edgeAt(dx, dz)) continue;
            if (!column.edgeAt(sx, sz) && !column.edgeAt(dx + sx, dz + sz)) {
                // Wrap three blocks around the projecting corner.
                parapetBlock(level, bounds, pos, column, wall, wall, 2 * (dx + sx), 2 * (dz + sz));
                parapetBlock(level, bounds, pos, column, wall, wall, 2 * dx + sx, 2 * dz + sz);
                parapetBlock(level, bounds, pos, column, wall, wall, dx + 2 * sx, dz + 2 * sz);
            }
            if (!column.edgeAt(dx + sx, dz + sz) && !column.edgeAt(dx - sx, dz - sz)) {
                var facing = face.getAxis() == Direction.Axis.X ? Direction.SOUTH : Direction.EAST;
                parapetBlock(level, bounds, pos, column, wall, stair.setValue(StairBlock.FACING, facing), 2 * dx, 2 * dz);
            }
        }
    }

    private static void parapetBlock(WorldGenLevel level, BoundingBox bounds, BlockPos.MutableBlockPos pos, Column column,
                                      BlockState wall, BlockState crest, int dx, int dz) {
        int x = column.x() + dx;
        int z = column.z() + dz;
        place(level, bounds, pos.set(x, column.crest(), z), wall, false);
        place(level, bounds, pos.set(x, column.crest() + 1, z), crest, crest.hasProperty(StairBlock.FACING));
    }

    private static void arch(WorldGenLevel level, BoundingBox bounds, BlockPos.MutableBlockPos pos, Column column,
                             BlockState rail, BlockState stair) {
        int y = column.crest() - 3;
        place(level, bounds, pos.set(column.x(), y, column.z()), rail, true);
        var width = column.gateAxis() == 1 ? Direction.SOUTH : Direction.EAST;
        boolean positive = column.gateAt(width.getStepX(), width.getStepZ());
        boolean negative = column.gateAt(-width.getStepX(), -width.getStepZ());
        if (positive == negative) return;
        var facing = positive ? width.getOpposite() : width;
        var trim = stair.setValue(StairBlock.HALF, Half.TOP).setValue(StairBlock.FACING, facing);
        int dx = width.getStepZ();
        int dz = width.getStepX();
        place(level, bounds, pos.set(column.x() + dx, y, column.z() + dz), trim, true);
        place(level, bounds, pos.set(column.x() - dx, y, column.z() - dz), trim, true);
    }

    private static void place(WorldGenLevel level, BoundingBox bounds, BlockPos pos, BlockState block, boolean updateShape) {
        if (!bounds.isInside(pos)) return;
        var existing = level.getBlockState(pos);
        if (!existing.canBeReplaced() && !existing.is(BlockTags.LEAVES) && !existing.is(BlockTags.LOGS)
                && !existing.is(BlockTags.SAPLINGS) && !existing.is(BlockTags.FLOWERS)) return;
        boolean generating = level instanceof WorldGenRegion;
        int flags = updateShape && !generating ? Block.UPDATE_ALL : Block.UPDATE_CLIENTS;
        if (level.setBlock(pos, block, flags) && updateShape && generating) {
            level.getChunk(pos).markPosForPostprocessing(pos);
        }
    }

    private static void spawnGuard(WorldGenLevel level, BoundingBox bounds, BlockPos pos) {
        if (!bounds.isInside(pos) || !level.getBlockState(pos).isAir() || !level.getBlockState(pos.above()).isAir()
                || !level.getBlockState(pos.below()).isSolidRender(level, pos.below())) return;
        var guard = WitcheryEntities.VILLAGE_GUARD.get().create(level.getLevel());
        if (guard == null) return;
        guard.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0.0F, 0.0F);
        guard.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.STRUCTURE, null, null);
        level.addFreshEntity(guard);
    }

    private static BoundingBox bounds(List<Column> columns, int minY) {
        var blocks = columns.stream().map(column -> new BoundingBox(column.x() - 2, minY, column.z() - 2,
                column.x() + 2, column.crest() + 1, column.z() + 2)).toList();
        return BoundingBox.encapsulatingBoxes(blocks).orElseThrow();
    }

    @Override public BoundingBox getBeardifierBox() { return getBoundingBox(); }
    @Override public TerrainAdjustment getTerrainAdjustment() { return TerrainAdjustment.NONE; }
    @Override public int getGroundLevelDelta() { return 0; }

    record Column(int x, int z, int floor, int crest, int edgeMask, int gateMask, int gateAxis, boolean guard) {
        static int bit(int dx, int dz) {
            return 1 << ((dx + 1) * 3 + dz + 1);
        }

        boolean edgeAt(int dx, int dz) { return (edgeMask & bit(dx, dz)) != 0; }
        boolean gateAt(int dx, int dz) { return (gateMask & bit(dx, dz)) != 0; }
    }

    enum Palette {
        STONE, DESERT, SNOWY;

        static Palette forVillage(String path) {
            if (path.startsWith("village/desert/")) return DESERT;
            if (path.startsWith("village/snowy/")) return SNOWY;
            return STONE;
        }

        static Palette fromTag(String id) {
            return switch (id) {
                case "DESERT" -> DESERT;
                case "SNOWY" -> SNOWY;
                default -> STONE;
            };
        }

        Block wall() {
            return switch (this) {
                case STONE -> Blocks.STONE_BRICKS;
                case DESERT -> Blocks.CUT_SANDSTONE;
                case SNOWY -> Blocks.SNOW_BLOCK;
            };
        }

        Block stair() {
            return switch (this) {
                case STONE -> Blocks.STONE_BRICK_STAIRS;
                case DESERT -> Blocks.SANDSTONE_STAIRS;
                case SNOWY -> WitcheryBlocks.getRequired("snowstairs").get();
            };
        }

        Block rail() {
            return this == SNOWY ? WitcheryBlocks.getRequired("icefence").get() : Blocks.OAK_FENCE;
        }
    }
}
