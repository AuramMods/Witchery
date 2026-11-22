package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.brewing.DeathBrewEffects;
import art.arcane.witchery.capability.WitcheryCapabilities;
import art.arcane.witchery.item.DoorKeyItem;
import art.arcane.witchery.item.WaystoneItem;
import art.arcane.witchery.item.WitcheryIngredient;
import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public final class LegacyWorldIds {
    private static final String IMPORT_PROPERTY = "witchery.importLegacyWorld";
    private static final String CONFIG_PROPERTY = "witchery.legacyConfig";
    private static final String REGISTRY_FILE = "witchery-legacy-ids.dat";
    private static final String ENTRIES = "ItemData";
    private static final String DIMENSIONS = "Dimensions";
    private static final String POTIONS = "Potions";
    private final Map<Integer, ResourceLocation> blocks = new HashMap<>();
    private final Map<Integer, ResourceLocation> items = new HashMap<>();
    private final Map<Integer, ResourceLocation> vanillaItems = new HashMap<>();
    private final Set<ResourceLocation> unmappedBlocks = ConcurrentHashMap.newKeySet();
    private final Set<ResourceLocation> unmappedItems = ConcurrentHashMap.newKeySet();
    private final LegacyWorldDimensions dimensions;
    private final LegacyWorldEffects effects;

    private LegacyWorldIds(CompoundTag snapshot) {
        dimensions = new LegacyWorldDimensions(snapshot.getCompound(DIMENSIONS));
        effects = new LegacyWorldEffects(snapshot.getCompound(POTIONS));
        for (Tag tag : snapshot.getList(ENTRIES, Tag.TAG_COMPOUND)) {
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
        if (!Boolean.getBoolean(IMPORT_PROPERTY)) return;
        try {
            boolean existing = Files.isRegularFile(registry);
            CompoundTag snapshot;
            if (existing) {
                snapshot = NbtIo.readCompressed(registry.toFile());
            } else {
                if (!Files.isRegularFile(level)) return;
                snapshot = new CompoundTag();
                snapshot.put(ENTRIES, NbtIo.readCompressed(level.toFile()).getCompound("FML").getList(ENTRIES, Tag.TAG_COMPOUND));
            }
            boolean captureDimensions = !snapshot.contains(DIMENSIONS, Tag.TAG_COMPOUND);
            boolean capturePotions = !snapshot.contains(POTIONS, Tag.TAG_COMPOUND);
            if (captureDimensions || capturePotions) {
                String configPath = System.getProperty(CONFIG_PROPERTY);
                List<String> config = configPath == null ? List.of() : Files.readAllLines(Path.of(configPath));
                if (captureDimensions) snapshot.put(DIMENSIONS, LegacyWorldDimensions.capture(config));
                if (capturePotions) snapshot.put(POTIONS, LegacyWorldEffects.capture(config));
            }
            LegacyWorldIds ids = new LegacyWorldIds(snapshot);
            if (ids.blocks.isEmpty() && ids.items.isEmpty()) return;
            if (captureDimensions || capturePotions) {
                saveSnapshot(world, registry, snapshot);
                Witchery.LOGGER.info("Captured {} Witchery block IDs and {} item IDs for {}", ids.blocks.size(), ids.items.size(), world);
            }
            ids.dimensions.relocate(world);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not prepare legacy Witchery save at " + world, e);
        }
    }

    private static void saveSnapshot(Path world, Path registry, CompoundTag snapshot) throws IOException {
        Path temporary = Files.createTempFile(world, "witchery-legacy-ids-", ".tmp");
        try {
            NbtIo.writeCompressed(snapshot, temporary.toFile());
            Files.move(temporary, registry, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    @Nullable
    public static LegacyWorldIds find(Path storage) {
        for (Path path = storage.toAbsolutePath().normalize(); path != null; path = path.getParent()) {
            Path registry = path.resolve(REGISTRY_FILE);
            try {
                if (Files.isRegularFile(registry)) {
                    return new LegacyWorldIds(NbtIo.readCompressed(registry.toFile()));
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

    public void upgradePlayer(CompoundTag player, DataFixer fixer, int from) {
        dimensions.upgradePlayerLocation(player);
        upgradeItems(player);
        var forgeData = player.getCompound("ForgeData");
        var persisted = forgeData.getCompound(Player.PERSISTED_NBT_TAG);
        int to = SharedConstants.getCurrentVersion().getDataVersion().getVersion();
        upgradeEntityEffects(player);
        effects.upgrade(persisted.getList("WITCPoSpawn", Tag.TAG_COMPOUND));
        upgradeVanillaItems(persisted.getCompound("WITCSpiritWorld"), fixer, from, to);
        if (player.contains("WitcheryExtendedPlayer", Tag.TAG_COMPOUND)) {
            var legacy = player.getCompound("WitcheryExtendedPlayer");
            if (legacy.getBoolean("CanRestoreInventory") && legacy.contains("CachedInventory2", Tag.TAG_LIST)) {
                var inventory = legacy.getList("CachedInventory2", Tag.TAG_COMPOUND);
                upgradeVanillaItems(inventory, fixer, from, to);
                DeathBrewEffects.storeLegacyInventory(persisted, inventory);
                forgeData.put(Player.PERSISTED_NBT_TAG, persisted);
                player.put("ForgeData", forgeData);
            }
            var capabilities = player.getCompound("ForgeCaps");
            String playerDataId = WitcheryCapabilities.playerDataId().toString();
            if (!capabilities.contains(playerDataId, Tag.TAG_COMPOUND)) {
                var imported = new CompoundTag();
                imported.put("WitcheryExtendedPlayer", legacy.copy());
                capabilities.put(playerDataId, imported);
                player.put("ForgeCaps", capabilities);
            }
        }
    }

    void upgradeEntityEffects(CompoundTag entity) {
        effects.upgrade(entity.getList("ActiveEffects", Tag.TAG_COMPOUND));
    }

    public void upgradeItems(Tag value) {
        if (value instanceof CompoundTag compound) {
            for (String key : compound.getAllKeys()) upgradeItems(compound.get(key));
            if (compound.contains("Count", Tag.TAG_ANY_NUMERIC)) upgradeStack(compound);
        } else if (value instanceof ListTag list) {
            for (Tag entry : list) upgradeItems(entry);
        }
    }

    void upgradeChunkItems(CompoundTag level, DataFixer fixer, int from, int to) {
        for (String key : level.getAllKeys()) {
            if (!key.equals("TileEntities")) upgradeItems(level.get(key));
        }
        for (Tag entry : level.getList("TileEntities", Tag.TAG_COMPOUND)) {
            CompoundTag tile = (CompoundTag) entry;
            var id = ResourceLocation.tryParse(tile.getString("id"));
            boolean witcheryTile = id != null && id.getNamespace().equals(Witchery.MODID);
            if (witcheryTile && id.getPath().equals("mirrorblock2") && tile.contains("DimCoords", Tag.TAG_COMPOUND)) {
                dimensions.upgradeReference(tile.getCompound("DimCoords"), "Dimension", "DimensionId");
            }
            // A cursed control has an item-like ID and its own integer Count at the tile root.
            for (String key : tile.getAllKeys()) {
                Tag value = tile.get(key);
                upgradeItems(value);
                if (witcheryTile) upgradeVanillaItems(value, fixer, from, to);
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
        // ItemStack.of can insert a modern Damage field into the decoded copy.
        if (stack.isDamageableItem() && stack.getDamageValue() == 0 && !compound.getCompound("tag").contains("Damage", Tag.TAG_ANY_NUMERIC)) {
            int damage = compound.getInt("Damage");
            if (damage > 0) stack.setDamageValue(Math.min(damage, stack.getMaxDamage()));
        }
        if (stack.getTag() != null) {
            CompoundTag tag = stack.getTag();
            if (stack.getItem() instanceof WaystoneItem) {
                dimensions.upgradeReference(tag, "PosD", "Dimension");
            } else if (stack.getItem() instanceof DoorKeyItem) {
                dimensions.upgradeReference(tag, "doorD", "doorDimension");
                for (Tag key : tag.getList("doorKeys", Tag.TAG_COMPOUND)) {
                    dimensions.upgradeReference((CompoundTag) key, "doorD", "doorDimension");
                }
            }
        }
        compound.remove("Damage");
        compound.remove("tag");
        stack.save(compound);
    }
}
