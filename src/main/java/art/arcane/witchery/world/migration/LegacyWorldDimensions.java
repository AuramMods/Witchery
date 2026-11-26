package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.world.WitcheryDimensions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

final class LegacyWorldDimensions {
    private static final Pattern CONFIG_ID = Pattern.compile("\\s*I:(Dream|Torment|Mirror)DimensionID\\s*=\\s*(-?\\d+)\\s*");
    private final Map<Integer, ResourceKey<Level>> dimensions = new LinkedHashMap<>();

    LegacyWorldDimensions(CompoundTag saved) {
        for (ResourceKey<Level> dimension : WitcheryDimensions.levels()) {
            String name = dimension.location().getPath();
            int id = saved.contains(name, Tag.TAG_ANY_NUMERIC) ? saved.getInt(name) : WitcheryDimensions.legacyId(dimension);
            if ((id >= -1 && id <= 1) || dimensions.putIfAbsent(id, dimension) != null) {
                throw new IllegalArgumentException("Invalid legacy Witchery dimension ID " + id + " for " + name);
            }
        }
    }

    static CompoundTag capture(List<String> config) {
        CompoundTag saved = new CompoundTag();
        for (String line : config) {
            var entry = CONFIG_ID.matcher(line);
            if (entry.matches()) saved.putInt(entry.group(1).toLowerCase(Locale.ROOT), Integer.parseInt(entry.group(2)));
        }
        return saved;
    }

    void relocate(Path world) throws IOException {
        for (var entry : dimensions.entrySet()) {
            Path source = world.resolve("DIM" + entry.getKey());
            if (!Files.isDirectory(source)) continue;
            Path target = DimensionType.getStorageFolder(entry.getValue(), world);
            Files.createDirectories(target.getParent());
            try {
                Files.move(source, target);
                Witchery.LOGGER.info("Moved legacy dimension {} to {}", source, target);
            } catch (FileAlreadyExistsException | DirectoryNotEmptyException existing) {
                int recovered = LegacyDimensionRegions.recoverMissing(source, target);
                Witchery.LOGGER.info("Recovered {} legacy dimension chunks from {} into {}", recovered, source, target);
            }
        }
    }

    @Nullable
    private ResourceKey<Level> resolve(int id) {
        return id >= -1 && id <= 1 ? WitcheryDimensions.fromLegacyId(id) : dimensions.get(id);
    }

    void upgradePlayerLocation(CompoundTag player) {
        if (!player.contains("SpawnDimension") && player.contains("Dimension", Tag.TAG_ANY_NUMERIC)) {
            int current = player.getInt("Dimension");
            var dimension = resolve(current);
            if (dimension != null && current != 0) upgradeForgeSpawn(player, current, dimension);
        }
        upgradeReference(player, "Dimension", "Dimension");
        upgradeReference(player, "SpawnDimension", "SpawnDimension");
    }

    private static void upgradeForgeSpawn(CompoundTag player, int current, ResourceKey<Level> dimension) {
        // Forge saved one bed per dimension. Prefer the bed in the saved dimension, then the original Overworld spawn.
        for (Tag entry : player.getList("Spawns", Tag.TAG_COMPOUND)) {
            CompoundTag spawn = (CompoundTag) entry;
            if (!spawn.contains("Dim", Tag.TAG_ANY_NUMERIC) || spawn.getInt("Dim") != current
                    || !spawn.contains("SpawnX", Tag.TAG_ANY_NUMERIC) || !spawn.contains("SpawnY", Tag.TAG_ANY_NUMERIC)
                    || !spawn.contains("SpawnZ", Tag.TAG_ANY_NUMERIC)) continue;
            player.putString("SpawnDimension", dimension.location().toString());
            player.putInt("SpawnX", spawn.getInt("SpawnX"));
            player.putInt("SpawnY", spawn.getInt("SpawnY"));
            player.putInt("SpawnZ", spawn.getInt("SpawnZ"));
            player.putBoolean("SpawnForced", spawn.getBoolean("SpawnForced"));
            break;
        }
    }

    void upgradeReference(CompoundTag tag, String numericKey, String locationKey) {
        if (tag.contains(locationKey, Tag.TAG_STRING) || !tag.contains(numericKey, Tag.TAG_ANY_NUMERIC)) return;
        ResourceKey<Level> dimension = resolve(tag.getInt(numericKey));
        if (dimension != null) tag.putString(locationKey, dimension.location().toString());
    }
}
