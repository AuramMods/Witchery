package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.item.WitcheryIngredient;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
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
    private final Set<ResourceLocation> unmappedBlocks = ConcurrentHashMap.newKeySet();
    private final Set<ResourceLocation> unmappedItems = ConcurrentHashMap.newKeySet();

    private LegacyWorldIds(ListTag entries) {
        for (Tag tag : entries) {
            CompoundTag entry = (CompoundTag) tag;
            String key = entry.getString("K");
            if (key.length() < 2 || !entry.contains("V", Tag.TAG_ANY_NUMERIC)) continue;
            ResourceLocation id = ResourceLocation.tryParse(key.substring(1));
            if (id == null || !id.getNamespace().equals(Witchery.MODID)) continue;
            int number = entry.getInt("V");
            if (key.charAt(0) == 1) blocks.put(number, id);
            if (key.charAt(0) == 2) items.put(number, id);
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
