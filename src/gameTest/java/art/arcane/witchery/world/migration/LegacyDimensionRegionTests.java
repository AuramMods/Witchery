package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.RegionFile;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class LegacyDimensionRegionTests {
    @GameTest(template = "qa_coven_arena")
    public static void preservesCurrentChunksAndRecoversMissingOriginalChunks(GameTestHelper helper) throws IOException {
        Path directory = Files.createTempDirectory("witchery-split-dimension");
        try {
            Path archive = directory.resolve("DIM-39");
            Path current = directory.resolve("dimensions/witchery/mirror");
            Path oldRegion = Files.createDirectories(archive.resolve("region")).resolve("r.-1.0.mca");
            Path newRegion = Files.createDirectories(current.resolve("region")).resolve(oldRegion.getFileName());
            ChunkPos overlap = new ChunkPos(-2, 4);
            ChunkPos missing = new ChunkPos(-1, 4);
            ChunkPos modernOnly = new ChunkPos(-3, 4);
            try (var legacy = new RegionFile(oldRegion, oldRegion.getParent(), false);
                 var destination = new RegionFile(newRegion, newRegion.getParent(), false)) {
                write(legacy, overlap, "old overlap");
                write(legacy, missing, "old neighbor");
                write(destination, overlap, "current overlap");
                write(destination, modernOnly, "current neighbor");
            }
            byte[] original = Files.readAllBytes(oldRegion);

            Files.createDirectories(archive.resolve("data"));
            Files.createDirectories(current.resolve("data"));
            Files.writeString(archive.resolve("data/villages.dat"), "archived villages");
            Files.writeString(archive.resolve("data/chunks.dat"), "archived data");
            Files.writeString(current.resolve("data/chunks.dat"), "current data");
            Files.writeString(archive.resolve("forcedchunks.dat"), "archived shelf tickets");

            helper.assertTrue(LegacyDimensionRegions.recoverMissing(archive, current) == 1,
                    "Recover the missing original chunk from the shared negative region");
            try (var destination = new RegionFile(newRegion, newRegion.getParent(), false)) {
                helper.assertTrue(read(destination, overlap).equals("current overlap")
                                && read(destination, missing).equals("old neighbor")
                                && read(destination, modernOnly).equals("current neighbor"),
                        "Keep both current chunk records and the original neighbor");
            }
            helper.assertTrue(Files.readString(current.resolve("forcedchunks.dat")).equals("archived shelf tickets")
                            && Files.readString(current.resolve("data/villages.dat")).equals("archived villages")
                            && Files.readString(current.resolve("data/chunks.dat")).equals("current data"),
                    "Copy missing world records and keep the destination's saved records");
            helper.assertTrue(LegacyDimensionRegions.recoverMissing(archive, current) == 0
                            && Arrays.equals(Files.readAllBytes(oldRegion), original),
                    "Repeating the import leaves the original region intact and skips recovered chunks");
        } finally {
            try (var files = Files.walk(directory)) {
                for (Path file : files.sorted(Comparator.reverseOrder()).toList()) Files.delete(file);
            }
        }
        helper.succeed();
    }

    private static void write(RegionFile file, ChunkPos position, String marker) throws IOException {
        var data = new CompoundTag();
        data.putString("Marker", marker);
        try (var output = file.getChunkDataOutputStream(position)) {
            NbtIo.write(data, output);
        }
    }

    private static String read(RegionFile file, ChunkPos position) throws IOException {
        try (var input = file.getChunkDataInputStream(position)) {
            if (input == null) throw new IOException("Missing fixture chunk " + position);
            return NbtIo.read(input).getString("Marker");
        }
    }
}
