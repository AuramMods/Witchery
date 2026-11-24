package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.brewing.DeathBrewEffects;
import art.arcane.witchery.capability.WitcheryCapabilities;
import art.arcane.witchery.item.DoorKeyItem;
import art.arcane.witchery.item.WaystoneItem;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.world.MirrorTravel;
import art.arcane.witchery.world.WitcheryDimensions;
import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.datafix.DataFixTypes;
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
    private static final String EGG_IDS_PROPERTY = "witchery.legacyEggIds";
    private static final String REGISTRY_FILE = "witchery-legacy-ids.dat";
    private static final String ENTRIES = "ItemData";
    private static final String DIMENSIONS = "Dimensions";
    private static final String POTIONS = "Potions";
    private static final String SPAWN_EGGS = "SpawnEggs";
    private static final ResourceLocation VANILLA_POTION = new ResourceLocation("minecraft", "potion");
    private static final ResourceLocation VANILLA_SPAWN_EGG = new ResourceLocation("minecraft", "spawn_egg");
    private final Map<Integer, ResourceLocation> blocks = new HashMap<>();
    private final Map<Integer, ResourceLocation> items = new HashMap<>();
    private final Map<Integer, ResourceLocation> vanillaItems = new HashMap<>();
    private final Map<Integer, ResourceLocation> spawnEggs = new HashMap<>();
    private final Set<ResourceLocation> unmappedBlocks = ConcurrentHashMap.newKeySet();
    private final Set<ResourceLocation> unmappedItems = ConcurrentHashMap.newKeySet();
    private final LegacyWorldDimensions dimensions;
    private final LegacyWorldEffects effects;

    private LegacyWorldIds(CompoundTag snapshot) {
        dimensions = new LegacyWorldDimensions(snapshot.getCompound(DIMENSIONS));
        effects = new LegacyWorldEffects(snapshot.getCompound(POTIONS));
        var eggs = snapshot.getCompound(SPAWN_EGGS);
        for (String metadata : eggs.getAllKeys()) {
            spawnEggs.put(Integer.parseInt(metadata), new ResourceLocation(Witchery.MODID, eggs.getString(metadata) + "_spawn_egg"));
        }
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
        if (!isImportRequested()) return;
        try {
            boolean existing = Files.isRegularFile(registry);
            CompoundTag forge = new CompoundTag();
            CompoundTag snapshot;
            if (existing) {
                snapshot = NbtIo.readCompressed(registry.toFile());
            } else {
                if (!Files.isRegularFile(level)) return;
                snapshot = new CompoundTag();
                forge = NbtIo.readCompressed(level.toFile()).getCompound("FML");
                snapshot.put(ENTRIES, forge.getList(ENTRIES, Tag.TAG_COMPOUND));
            }
            boolean captureDimensions = !snapshot.contains(DIMENSIONS, Tag.TAG_COMPOUND);
            boolean capturePotions = !snapshot.contains(POTIONS, Tag.TAG_COMPOUND);
            if (captureDimensions || capturePotions) {
                String configPath = System.getProperty(CONFIG_PROPERTY);
                List<String> config = configPath == null ? List.of() : Files.readAllLines(Path.of(configPath));
                if (captureDimensions) snapshot.put(DIMENSIONS, LegacyWorldDimensions.capture(config));
                if (capturePotions) snapshot.put(POTIONS, LegacyWorldEffects.capture(config));
            }
            String eggMapping = System.getProperty(EGG_IDS_PROPERTY);
            boolean captureEggs = eggMapping != null || !existing;
            if (captureEggs) {
                snapshot.put(SPAWN_EGGS, LegacySpawnEggs.capture(forge, eggMapping == null ? null : Path.of(eggMapping)));
            }
            LegacyWorldIds ids = new LegacyWorldIds(snapshot);
            if (ids.blocks.isEmpty() && ids.items.isEmpty()) return;
            if (captureDimensions || capturePotions || captureEggs) {
                saveSnapshot(world, registry, snapshot);
                Witchery.LOGGER.info("Captured {} Witchery block IDs and {} item IDs for {}", ids.blocks.size(), ids.items.size(), world);
            }
            ids.dimensions.relocate(world);
        } catch (IOException e) {
            throw new UncheckedIOException("Could not prepare legacy Witchery save at " + world, e);
        }
    }

    static boolean isImportRequested() {
        return Boolean.getBoolean(IMPORT_PROPERTY);
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

    public CompoundTag upgradePlayer(CompoundTag player, DataFixer fixer, int from) {
        preparePlayer(player, fixer, from);
        CompoundTag updated = DataFixTypes.PLAYER.updateToCurrentVersion(fixer, player, from);
        if (from < 100 && !player.contains("RootVehicle", Tag.TAG_COMPOUND) && player.contains("Riding", Tag.TAG_COMPOUND)) {
            CompoundTag mount = LegacyChunkEntities.upgradePlayerVehicle(player.getCompound("Riding"), this, fixer, from);
            if (mount != null) {
                updated.put("RootVehicle", mount);
                // A dedicated login reads the vehicle from the original tag returned by PlayerDataStorage.
                player.put("RootVehicle", mount);
            }
        }
        return updated;
    }

    private void preparePlayer(CompoundTag player, DataFixer fixer, int from) {
        if (from < 100) dimensions.upgradePlayerLocation(player);
        var legacy = player.getCompound("WitcheryExtendedPlayer");
        var forgeData = player.getCompound("ForgeData");
        var persisted = forgeData.getCompound(Player.PERSISTED_NBT_TAG);
        if (!legacy.isEmpty()) {
            boolean insideMirror = WitcheryDimensions.MIRROR.location().toString().equals(player.getString("Dimension"));
            MirrorTravel.importLegacyPlayerState(legacy, persisted, insideMirror);
            if (!persisted.isEmpty()) {
                forgeData.put(Player.PERSISTED_NBT_TAG, persisted);
                player.put("ForgeData", forgeData);
            }
        }
        var expiredDrops = persisted.getList("WITCPriIncInv", Tag.TAG_COMPOUND);
        // The pending list can keep legacy effect IDs after an earlier import updated the player version.
        effects.upgrade(persisted.getList("WITCPoSpawn", Tag.TAG_COMPOUND));
        int to = SharedConstants.getCurrentVersion().getDataVersion().getVersion();
        if (from >= 100) {
            // Earlier port builds could resave the player while these stacks still used legacy IDs.
            for (Tag entry : expiredDrops) {
                if (((CompoundTag) entry).contains("id", Tag.TAG_ANY_NUMERIC)) {
                    upgradeItems(entry);
                    upgradeVanillaItems(entry, fixer, -1, to);
                }
            }
            upgradeStoredBrewIngredients(player, fixer);
            return;
        }

        upgradeItems(player);
        upgradeStoredBrewIngredients(player, fixer);
        upgradeEntityEffects(player);
        upgradeVanillaItems(persisted.getCompound("WITCSpiritWorld"), fixer, from, to);
        upgradeVanillaItems(expiredDrops, fixer, from, to);
        if (!legacy.isEmpty()) {
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

    public void upgradeStoredBrewIngredients(Tag value, DataFixer fixer) {
        if (value instanceof ListTag list) {
            for (Tag entry : list) upgradeStoredBrewIngredients(entry, fixer);
        } else if (value instanceof CompoundTag compound) {
            String item = compound.getString("id");
            if (!item.equals("witchery:brewbag") && !item.equals("witchery:leonardsurn")) {
                for (String key : compound.getAllKeys()) upgradeStoredBrewIngredients(compound.get(key), fixer);
                return;
            }

            // The item schema visits tag.Items, but the carriers put the bottles in tag.Inventory.Items.
            var bottles = compound.getCompound("tag").getCompound("Inventory").getList("Items", Tag.TAG_COMPOUND);
            int currentVersion = SharedConstants.getCurrentVersion().getDataVersion().getVersion();
            for (Tag entry : bottles) {
                var bottle = (CompoundTag) entry;
                if (!bottle.getString("id").equals("witchery:brewbottle")) continue;
                for (Tag ingredient : bottle.getCompound("tag").getList("Items", Tag.TAG_COMPOUND)) {
                    if (((CompoundTag) ingredient).contains("id", Tag.TAG_ANY_NUMERIC)) {
                        upgradeVanillaItems(ingredient, fixer, -1, currentVersion);
                    }
                }
            }
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
        ResourceLocation name;
        if (compound.contains("id", Tag.TAG_ANY_NUMERIC)) {
            int id = compound.getInt("id") & 0xFFFF;
            name = items.get(id);
            if (name == null) name = vanillaItems.get(id);
        } else {
            name = ResourceLocation.tryParse(compound.getString("id"));
        }
        if (VANILLA_POTION.equals(name)) {
            effects.upgrade(compound.getCompound("tag").getList("CustomPotionEffects", Tag.TAG_COMPOUND));
        }
        if (VANILLA_SPAWN_EGG.equals(name)) name = spawnEggs.get(compound.getInt("Damage") & 0xFFFF);
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
