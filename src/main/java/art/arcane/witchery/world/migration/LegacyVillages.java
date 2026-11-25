package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelResource;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

public final class LegacyVillages {
    private static final Map<ServerLevel, LegacyVillages> LOADED = Collections.synchronizedMap(new WeakHashMap<>());
    private static final LegacyVillages EMPTY = new LegacyVillages(List.of());
    private final List<Village> villages;

    private LegacyVillages(List<Village> villages) {
        this.villages = List.copyOf(villages);
    }

    public static Optional<Village> nearest(ServerLevel world, BlockPos position, int range) {
        return LOADED.computeIfAbsent(world, LegacyVillages::read).nearest(position, range);
    }

    private static LegacyVillages read(ServerLevel world) {
        var root = world.getServer().getWorldPath(LevelResource.ROOT);
        var file = DimensionType.getStorageFolder(world.dimension(), root).resolve("data/villages.dat");
        if (!Files.isRegularFile(file)) return EMPTY;
        try {
            return load(NbtIo.readCompressed(file.toFile()).getCompound("data"));
        } catch (IOException invalid) {
            Witchery.LOGGER.warn("Could not read legacy village reputation from {}", file, invalid);
            return EMPTY;
        }
    }

    static LegacyVillages load(CompoundTag data) {
        var villages = new ArrayList<Village>();
        for (Tag entry : data.getList("Villages", Tag.TAG_COMPOUND)) {
            var saved = (CompoundTag) entry;
            int radius = saved.getInt("Radius");
            if (radius <= 0 || !saved.contains("CX", Tag.TAG_ANY_NUMERIC) || !saved.contains("CY", Tag.TAG_ANY_NUMERIC)
                    || !saved.contains("CZ", Tag.TAG_ANY_NUMERIC)) continue;
            var scores = new HashMap<String, Integer>();
            for (Tag playerTag : saved.getList("Players", Tag.TAG_COMPOUND)) {
                var player = (CompoundTag) playerTag;
                String name = player.getString("Name");
                if (!name.isEmpty() && player.contains("S", Tag.TAG_ANY_NUMERIC)) {
                    scores.put(name, Mth.clamp(player.getInt("S"), -30, 10));
                }
            }
            var center = new BlockPos(saved.getInt("CX"), saved.getInt("CY"), saved.getInt("CZ"));
            villages.add(new Village(center, radius, Map.copyOf(scores)));
        }
        return new LegacyVillages(villages);
    }

    Optional<Village> nearest(BlockPos position, int range) {
        return villages.stream().filter(village -> village.contains(position, range))
                .min(Comparator.comparingDouble(village -> village.center().distSqr(position)));
    }

    public record Village(BlockPos center, int radius, Map<String, Integer> scores) {
        public boolean contains(BlockPos position, int range) {
            double limit = (double) radius + range;
            return center.distSqr(position) <= limit * limit;
        }

        public int reputation(String name) {
            return scores.getOrDefault(name, 0);
        }
    }
}
