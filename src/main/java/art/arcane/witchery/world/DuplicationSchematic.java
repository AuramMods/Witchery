package art.arcane.witchery.world;

import art.arcane.witchery.world.migration.LegacySchematicBlocks;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

public final class DuplicationSchematic {
    // Item stacks use the vanilla two-megabyte network NBT quota.
    public static final int ITEM_TAG_QUOTA = 2 * 1024 * 1024;

    private DuplicationSchematic() {}

    @Nullable
    public static CompoundTag capture(ServerLevel level, BlockPos first, BlockPos second) {
        var size = new Size(Math.abs(first.getX() - second.getX()) - 1,
                Math.abs(first.getY() - second.getY()) - 1, Math.abs(first.getZ() - second.getZ()) - 1);
        long volume = size.volume();
        if (volume == 0) return null;
        var origin = new BlockPos(Math.min(first.getX(), second.getX()) + 1,
                Math.min(first.getY(), second.getY()) + 1, Math.min(first.getZ(), second.getZ()) + 1);
        var indices = new HashMap<BlockState, Integer>();
        var palette = new ListTag();
        var runs = new IntArrayList();
        for (long i = 0; i < volume; i++) {
            BlockState state = level.getBlockState(origin.offset(size.position(i)));
            int index = indices.computeIfAbsent(state, block -> {
                palette.add(NbtUtils.writeBlockState(block));
                return palette.size() - 1;
            });
            int last = runs.size() - 1;
            if (last >= 1 && runs.getInt(last - 1) == index && runs.getInt(last) < Integer.MAX_VALUE) {
                runs.set(last, runs.getInt(last) + 1);
            } else {
                runs.add(index);
                runs.add(1);
                if (runs.size() > ITEM_TAG_QUOTA / Integer.BYTES) return null;
            }
        }
        var saved = new CompoundTag();
        saved.putInt("xMax", size.width());
        saved.putInt("yMax", size.height());
        saved.putInt("zMax", size.depth());
        saved.put("Palette", palette);
        saved.putIntArray("Runs", runs.toIntArray());
        return saved;
    }

    public static boolean place(ServerLevel level, BlockPos origin, CompoundTag saved, int rotation) {
        var size = Size.read(saved);
        long volume = size.volume();
        if (volume == 0) return false;
        if (!saved.contains("Runs", Tag.TAG_INT_ARRAY)) {
            return placeLegacy(level, origin, saved.getList("blocks", Tag.TAG_COMPOUND), size, rotation);
        }

        var palette = new ArrayList<BlockState>();
        for (Tag entry : saved.getList("Palette", Tag.TAG_COMPOUND)) {
            var tag = (CompoundTag) entry;
            var name = ResourceLocation.tryParse(tag.getString("Name"));
            palette.add(name != null && BuiltInRegistries.BLOCK.containsKey(name)
                    ? NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), tag) : null);
        }
        int[] runs = saved.getIntArray("Runs");
        if ((runs.length & 1) != 0) return false;
        long count = 0;
        for (int i = 0; i < runs.length; i += 2) {
            if (runs[i] < 0 || runs[i] >= palette.size() || runs[i + 1] <= 0) return false;
            count += runs[i + 1];
        }
        if (count != volume) return false;
        long offset = 0;
        Rotation facing = blockRotation(rotation);
        for (int i = 0; i < runs.length; i += 2) {
            BlockState state = palette.get(runs[i]);
            long end = offset + runs[i + 1];
            if (state != null) {
                BlockState rotated = state.rotate(facing);
                for (long cell = offset; cell < end; cell++) {
                    setBlock(level, origin.offset(size.rotatedPosition(cell, rotation)), rotated);
                }
            }
            offset = end;
        }
        return true;
    }

    private static boolean placeLegacy(ServerLevel level, BlockPos origin, ListTag blocks, Size size, int rotation) {
        if (blocks.size() != size.volume()) return false;
        var states = new HashMap<LegacyState, Optional<BlockState>>();
        Rotation facing = blockRotation(rotation);
        for (int i = 0; i < blocks.size(); i++) {
            var block = blocks.getCompound(i);
            String name = block.getString("n");
            int metadata = block.getInt("m") & 15;
            boolean upper = (metadata & 8) != 0;
            int y = i % size.height();
            int mate = 0;
            if (upper ? y > 0 : y + 1 < size.height()) {
                var adjacent = blocks.getCompound(i + (upper ? -1 : 1));
                if (name.equals(adjacent.getString("n")) && ((adjacent.getInt("m") ^ metadata) & 8) != 0) {
                    mate = adjacent.getInt("m") & 15;
                }
            }
            var key = new LegacyState(name, metadata, mate);
            BlockState state = states.computeIfAbsent(key,
                    k -> Optional.ofNullable(LegacySchematicBlocks.read(k.name(), k.metadata(), k.mate()))).orElse(null);
            if (state == null) continue;
            if (state.getBlock() instanceof StairBlock || state.getBlock() instanceof RotatedPillarBlock
                    || state.getBlock() instanceof DoorBlock) {
                state = state.rotate(facing);
            }
            setBlock(level, origin.offset(size.rotatedPosition(i, rotation)), state);
        }
        return true;
    }

    private static void setBlock(ServerLevel level, BlockPos pos, BlockState state) {
        // Place both halves of paired blocks before neighbor updates can remove them.
        level.setBlock(pos, state, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
    }

    private static Rotation blockRotation(int rotation) {
        return switch (rotation) {
            case 1 -> Rotation.COUNTERCLOCKWISE_90;
            case 2 -> Rotation.CLOCKWISE_180;
            case 3 -> Rotation.CLOCKWISE_90;
            default -> Rotation.NONE;
        };
    }

    public static void export(CompoundTag saved, Path config) throws IOException {
        Files.writeString(config.resolve("schematic.txt"), saved.toString() + "\n");
        var size = Size.read(saved);
        var palette = saved.getList("Palette", Tag.TAG_COMPOUND);
        int[] runs = saved.getIntArray("Runs");
        try (var writer = Files.newBufferedWriter(config.resolve("schematic2.txt"))) {
            long offset = 0;
            for (int i = 0; i < runs.length; i += 2) {
                var state = palette.getCompound(runs[i]);
                long end = offset + runs[i + 1];
                if (!state.getString("Name").equals("minecraft:air")) {
                    for (long cell = offset; cell < end; cell++) {
                        writer.write(size.position(cell).toShortString() + " " + state);
                        writer.newLine();
                    }
                }
                offset = end;
            }
        }
    }

    private record LegacyState(String name, int metadata, int mate) {}

    private record Size(int width, int height, int depth) {
        static Size read(CompoundTag saved) {
            return new Size(saved.getInt("xMax"), saved.getInt("yMax"), saved.getInt("zMax"));
        }

        long volume() {
            if (width <= 0 || height <= 0 || depth <= 0) return 0;
            long plane = (long) height * depth;
            return Long.MAX_VALUE / plane >= width ? plane * width : 0;
        }

        BlockPos position(long index) {
            return new BlockPos((int) (index / height / depth), (int) (index % height), (int) (index / height % depth));
        }

        BlockPos rotatedPosition(long index, int rotation) {
            var local = position(index);
            return switch (rotation) {
                case 1 -> new BlockPos(local.getZ(), local.getY(), width - 1 - local.getX());
                case 2 -> new BlockPos(width - 1 - local.getX(), local.getY(), depth - 1 - local.getZ());
                case 3 -> new BlockPos(depth - 1 - local.getZ(), local.getY(), local.getX());
                default -> local;
            };
        }
    }
}
