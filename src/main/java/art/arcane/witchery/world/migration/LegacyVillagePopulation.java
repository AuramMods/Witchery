package art.arcane.witchery.world.migration;

import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.visitors.CollectFields;
import net.minecraft.nbt.visitors.FieldSelector;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.RegionFileVersion;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class LegacyVillagePopulation {
    private static final String SNAPSHOT = "data/witchery-legacy-village-population.dat";
    private static final int SECTOR_SIZE = 4096;
    private static final int LEGACY_OFFSET = 8;

    private LegacyVillagePopulation() {}

    static boolean hasSnapshot(Path dimension) {
        return Files.isRegularFile(dimension.resolve(SNAPSHOT));
    }

    static void capture(Path dimension, Set<Long> candidateChunks) throws IOException {
        Path snapshot = dimension.resolve(SNAPSHOT);
        if (Files.exists(snapshot)) return;
        var owners = new HashSet<ChunkPos>();
        for (long key : candidateChunks) {
            var chunk = new ChunkPos(key);
            owners.add(owner(chunk.getMinBlockX(), chunk.getMinBlockZ()));
            owners.add(owner(chunk.getMaxBlockX(), chunk.getMinBlockZ()));
            owners.add(owner(chunk.getMinBlockX(), chunk.getMaxBlockZ()));
            owners.add(owner(chunk.getMaxBlockX(), chunk.getMaxBlockZ()));
        }
        Map<Path, List<ChunkPos>> regions = new HashMap<>();
        for (var pos : owners) {
            var file = dimension.resolve("region/r." + pos.getRegionX() + "." + pos.getRegionZ() + ".mca");
            if (Files.isRegularFile(file)) regions.computeIfAbsent(file, ignored -> new ArrayList<>()).add(pos);
        }
        var populated = new ArrayList<Long>();
        for (var entry : regions.entrySet()) {
            // Read only the source records. Opening a RegionFile for writing can pad the original save.
            try (var file = new RandomAccessFile(entry.getKey().toFile(), "r")) {
                for (var position : entry.getValue()) {
                    if (wasPopulated(file, position)) populated.add(position.toLong());
                }
            } catch (IOException exception) {
                throw new IOException("Could not read legacy village population from " + entry.getKey(), exception);
            }
        }
        var tag = new CompoundTag();
        tag.putLongArray("Populated", populated);
        Path temporary = Files.createTempFile(snapshot.getParent(), "witchery-village-population-", ".tmp");
        try {
            NbtIo.writeCompressed(tag, temporary.toFile());
            Files.move(temporary, snapshot, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    static Set<Long> read(Path dimension) throws IOException {
        Path snapshot = dimension.resolve(SNAPSHOT);
        if (!Files.isRegularFile(snapshot)) return Set.of();
        var owners = new HashSet<Long>();
        for (long chunk : NbtIo.readCompressed(snapshot.toFile()).getLongArray("Populated")) owners.add(chunk);
        return Set.copyOf(owners);
    }

    static ChunkPos owner(int x, int z) {
        // The original population box begins eight blocks past the start of its owner chunk.
        return new ChunkPos((x - LEGACY_OFFSET) >> 4, (z - LEGACY_OFFSET) >> 4);
    }

    private static boolean wasPopulated(RandomAccessFile file, ChunkPos position) throws IOException {
        int index = position.getRegionLocalX() + position.getRegionLocalZ() * 32;
        if (file.length() < SECTOR_SIZE * 2L) return false;
        file.seek(index * 4L);
        int location = file.readInt();
        if (location == 0) return false;
        int sectors = location & 255;
        file.seek((location >>> 8) * (long) SECTOR_SIZE);
        int length = file.readInt();
        int compression = file.readUnsignedByte();
        if ((compression & 128) != 0) return false; // External chunks were introduced after 1.7.10.
        var version = RegionFileVersion.fromId(compression);
        if (version == null || length <= 1 || length > sectors * SECTOR_SIZE - 4) {
            throw new IOException("Invalid region record at " + position);
        }
        var buffer = new byte[length - 1];
        file.readFully(buffer);
        try (var input = new DataInputStream(version.wrap(new ByteArrayInputStream(buffer)))) {
            var fields = new CollectFields(new FieldSelector(IntTag.TYPE, "DataVersion"),
                    new FieldSelector("Level", ByteTag.TYPE, "TerrainPopulated"));
            NbtIo.parse(input, fields);
            return fields.getResult() instanceof CompoundTag root
                    && root.getInt("DataVersion") < 100 && root.getCompound("Level").getBoolean("TerrainPopulated");
        }
    }
}
