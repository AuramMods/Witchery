package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.item.WitcheryIngredient;
import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class LegacyWorldIds {
    private static final String IMPORT_PROPERTY = "witchery.importLegacyWorld";
    private static final String REGISTRY_FILE = "witchery-legacy-ids.dat";
    private static final String ENTRIES = "ItemData";
    private final Map<Integer, ResourceLocation> blocks = new HashMap<>();
    private final Map<Integer, ResourceLocation> items = new HashMap<>();
    private final Map<Integer, ResourceLocation> vanillaItems = new HashMap<>();
    private final Set<ResourceLocation> unmappedBlocks = ConcurrentHashMap.newKeySet();
    private final Set<ResourceLocation> unmappedItems = ConcurrentHashMap.newKeySet();

    private LegacyWorldIds(ListTag entries) {
        for (Tag tag : entries) {
            CompoundTag entry = (CompoundTag) tag;
            String key = entry.getString("K");
            if (key.length() < 2 || !entry.contains("V", Tag.TAG_ANY_NUMERIC)) continue;
            ResourceLocation id = ResourceLocation.tryParse(key.substring(1));
            if (id == null) continue;
            int number = entry.getInt("V");
            if (id.getNamespace().equals(Witchery.MODID)) {
                if (key.charAt(0) == 1) blocks.put(number, id);
                if (key.charAt(0) == 2) items.put(number, id);
            } else if (key.charAt(0) == 2 && id.getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE)) {
                vanillaItems.put(number, id);
            }
        }
    }

    /** Capture the old registry before Minecraft replaces the destination's level.dat. */
    public static void prepare(Path world) {
        Path registry = world.resolve(REGISTRY_FILE);
        Path level = world.resolve("level.dat");
        if (!Boolean.getBoolean(IMPORT_PROPERTY) || Files.exists(registry) || !Files.isRegularFile(level)) return;
        try {
            ListTag entries = NbtIo.readCompressed(level.toFile()).getCompound("FML").getList(ENTRIES, Tag.TAG_COMPOUND);
            LegacyWorldIds ids = new LegacyWorldIds(entries);
            if (ids.blocks.isEmpty() && ids.items.isEmpty()) return;
            CompoundTag snapshot = new CompoundTag();
            snapshot.put(ENTRIES, entries);
            Path temporary = Files.createTempFile(world, "witchery-legacy-ids-", ".tmp");
            try {
                NbtIo.writeCompressed(snapshot, temporary.toFile());
                Files.move(temporary, registry, StandardCopyOption.ATOMIC_MOVE);
            } finally {
                Files.deleteIfExists(temporary);
            }
            Witchery.LOGGER.info("Captured {} Witchery block IDs and {} item IDs for {}", ids.blocks.size(), ids.items.size(), world);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not capture legacy Witchery registry from " + level, e);
        }
    }

    @Nullable
    public static LegacyWorldIds find(Path storage) {
        for (Path path = storage.toAbsolutePath().normalize(); path != null; path = path.getParent()) {
            Path registry = path.resolve(REGISTRY_FILE);
            try {
                if (Files.isRegularFile(registry)) {
                    return new LegacyWorldIds(NbtIo.readCompressed(registry.toFile()).getList(ENTRIES, Tag.TAG_COMPOUND));
                }
            } catch (IOException e) {
                throw new UncheckedIOException("Could not read legacy Witchery registry from " + registry, e);
            }
            if (Files.isRegularFile(path.resolve("level.dat"))) return null;
        }
        return null;
    }

    @Nullable
    ResourceLocation block(int number) {
        return blocks.get(number);
    }

    void reportUnmappedBlock(ResourceLocation id) {
        if (unmappedBlocks.add(id)) Witchery.LOGGER.warn("Legacy block {} has no Witchery world import mapping yet", id);
    }

    public void upgradeItems(Tag value) {
        if (value instanceof CompoundTag compound) {
            for (String key : compound.getAllKeys()) upgradeItems(compound.get(key));
            if (compound.contains("Count", Tag.TAG_ANY_NUMERIC)) upgradeStack(compound);
        } else if (value instanceof ListTag list) {
            for (Tag entry : list) upgradeItems(entry);
        }
    }

    void upgradeTileInventories(CompoundTag level, DataFixer fixer, int from, int to) {
        for (Tag entry : level.getList("TileEntities", Tag.TAG_COMPOUND)) {
            CompoundTag tile = (CompoundTag) entry;
            var id = ResourceLocation.tryParse(tile.getString("id"));
            if (id != null && id.getNamespace().equals(Witchery.MODID)) {
                upgradeVanillaItems(tile, fixer, from, to);
            }
        }
    }

    void upgradeVanillaItems(Tag value, DataFixer fixer, int from, int to) {
        if (value instanceof CompoundTag compound) {
            for (String key : compound.getAllKeys()) upgradeVanillaItems(compound.get(key), fixer, from, to);
            if (!compound.contains("Count", Tag.TAG_ANY_NUMERIC)) return;
            ResourceLocation id = compound.contains("id", Tag.TAG_ANY_NUMERIC)
                    ? vanillaItems.get(compound.getInt("id") & 0xFFFF) : ResourceLocation.tryParse(compound.getString("id"));
            if (id == null || !id.getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE)) return;

            // Vanilla's chunk fixer cannot see containers outside its typed schema.
            var named = compound.copy();
            named.putString("id", id.toString());
            if (fixer.update(References.ITEM_STACK, new Dynamic<>(NbtOps.INSTANCE, named), from, to).getValue() instanceof CompoundTag fixed) {
                // Item fixes may move damage or remove legacy fields. Keep the slot and any remaining custom data.
                for (String key : Set.copyOf(compound.getAllKeys())) compound.remove(key);
                compound.merge(fixed);
            }
        } else if (value instanceof ListTag list) {
            for (Tag entry : list) upgradeVanillaItems(entry, fixer, from, to);
        }
    }

    private void upgradeStack(CompoundTag compound) {
        ResourceLocation name = compound.contains("id", Tag.TAG_ANY_NUMERIC)
                ? items.get(compound.getInt("id") & 0xFFFF) : ResourceLocation.tryParse(compound.getString("id"));
        if (name == null || !name.getNamespace().equals(Witchery.MODID) || compound.getByte("Count") <= 0) return;
        CompoundTag named = compound.copy();
        named.putString("id", name.toString());
        ItemStack stack = WitcheryIngredient.loadInventoryStack(named);
        if (stack.isEmpty()) {
            if (unmappedItems.add(name)) Witchery.LOGGER.warn("Legacy item {} has no Witchery world import mapping yet", name);
            return;
        }
        if (stack.isDamageableItem() && stack.getDamageValue() == 0 && !named.getCompound("tag").contains("Damage", Tag.TAG_ANY_NUMERIC)) {
            int damage = named.getInt("Damage");
            if (damage > 0) stack.setDamageValue(Math.min(damage, stack.getMaxDamage()));
        }
        compound.remove("Damage");
        compound.remove("tag");
        stack.save(compound);
    }
}
