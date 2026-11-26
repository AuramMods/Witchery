package art.arcane.witchery.world.migration;

import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.RegionFile;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.regex.Pattern;

final class LegacyDimensionRegions {
    private static final Pattern REGION_NAME = Pattern.compile("r\\.(-?\\d+)\\.(-?\\d+)\\.mca");
    private static final int REGION_SIDE = 32;

    private LegacyDimensionRegions() {}

    static int recoverMissing(Path archive, Path destination) throws IOException {
        int copied = 0;
        Path legacyRegions = archive.resolve("region");
        Path currentRegions = destination.resolve("region");
        if (Files.isDirectory(legacyRegions)) {
            Files.createDirectories(currentRegions);
            try (var files = Files.newDirectoryStream(legacyRegions, "r.*.*.mca")) {
                for (Path source : files) {
                    String name = source.getFileName().toString();
                    var region = REGION_NAME.matcher(name);
                    if (region.matches() && Files.isRegularFile(source)) {
                        int firstX = Integer.parseInt(region.group(1)) * REGION_SIDE;
                        int firstZ = Integer.parseInt(region.group(2)) * REGION_SIDE;
                        copied += recoverRegion(source, currentRegions.resolve(name), firstX, firstZ);
                    }
                }
            }
        }
        copyMissing(archive.resolve("forcedchunks.dat"), destination.resolve("forcedchunks.dat"));
        Path legacyData = archive.resolve("data");
        if (Files.isDirectory(legacyData)) {
            try (var files = Files.newDirectoryStream(legacyData, "*.dat")) {
                for (Path source : files) copyMissing(source, destination.resolve("data").resolve(source.getFileName()));
            }
        }
        return copied;
    }

    private static int recoverRegion(Path source, Path destination, int firstX, int firstZ) throws IOException {
        // RegionFile opens its input for writing and can pad it on close.
        Path workingCopy = Files.createTempFile(destination.getParent(), "witchery-legacy-region-", ".tmp");
        try {
            Files.copy(source, workingCopy, StandardCopyOption.REPLACE_EXISTING);
            try (var legacy = new RegionFile(workingCopy, source.getParent(), false);
                 var current = new RegionFile(destination, destination.getParent(), false)) {
                int copied = 0;
                for (int x = firstX; x < firstX + REGION_SIDE; x++) {
                    for (int z = firstZ; z < firstZ + REGION_SIDE; z++) {
                        ChunkPos position = new ChunkPos(x, z);
                        if (!legacy.hasChunk(position) || current.hasChunk(position)) continue;
                        byte[] payload;
                        try (var input = legacy.getChunkDataInputStream(position)) {
                            if (input == null) throw new IOException("Could not read legacy chunk " + position + " in " + source);
                            payload = input.readAllBytes();
                        }
                        try (var output = current.getChunkDataOutputStream(position)) {
                            output.write(payload);
                        }
                        copied++;
                    }
                }
                return copied;
            }
        } finally {
            Files.deleteIfExists(workingCopy);
        }
    }

    private static void copyMissing(Path source, Path destination) throws IOException {
        if (!Files.isRegularFile(source) || Files.exists(destination)) return;
        Files.createDirectories(destination.getParent());
        Path copy = Files.createTempFile(destination.getParent(), "witchery-legacy-data-", ".tmp");
        try {
            Files.copy(source, copy, StandardCopyOption.REPLACE_EXISTING);
            try {
                Files.move(copy, destination);
            } catch (FileAlreadyExistsException existing) {
                // Keep the destination's saved data.
            }
        } finally {
            Files.deleteIfExists(copy);
        }
    }
}
