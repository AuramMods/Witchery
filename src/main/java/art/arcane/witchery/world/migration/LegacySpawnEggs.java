package art.arcane.witchery.world.migration;

import art.arcane.witchery.registry.WitcheryItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

final class LegacySpawnEggs {
    private static final Set<String> ORIGINAL_MODS = Set.of("mcp", "FML", "Forge", "witchery");

    private LegacySpawnEggs() {}

    static CompoundTag capture(CompoundTag forge, @Nullable Path mapping) throws IOException {
        var defaults = WitcheryItems.originalSpawnEggIds();
        var eggs = new CompoundTag();
        if (mapping != null) {
            readMapping(mapping, defaults, eggs);
            return eggs;
        }
        var mods = forge.getList("ModList", Tag.TAG_COMPOUND);
        boolean witchery = false;
        for (Tag entry : mods) {
            var mod = (CompoundTag) entry;
            String id = mod.getString("ModId");
            // The original allocator skipped occupied global entity IDs at runtime.
            if (!ORIGINAL_MODS.contains(id)) return eggs;
            if (id.equals("witchery")) witchery = mod.getString("ModVersion").equals("0.24.1");
        }
        if (witchery) defaults.forEach((id, entity) -> eggs.putString(Integer.toString(id), entity));
        return eggs;
    }

    private static void readMapping(Path path, Map<Integer, String> defaults, CompoundTag eggs) throws IOException {
        var properties = new Properties();
        try (var reader = Files.newBufferedReader(path)) {
            properties.load(reader);
        }
        for (String id : properties.stringPropertyNames()) {
            int metadata;
            try {
                metadata = Integer.parseInt(id);
            } catch (NumberFormatException e) {
                throw new IOException("Invalid legacy spawn egg metadata " + id + " in " + path, e);
            }
            String entity = properties.getProperty(id).strip();
            if (metadata < 6396 || metadata > 0xffff || !defaults.containsValue(entity)) {
                throw new IOException("Invalid legacy Witchery spawn egg " + id + "=" + entity + " in " + path);
            }
            eggs.putString(Integer.toString(metadata), entity);
        }
    }
}
