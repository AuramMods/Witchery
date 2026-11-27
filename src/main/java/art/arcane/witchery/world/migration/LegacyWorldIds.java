package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.brewing.BrewItemCurse;
import art.arcane.witchery.brewing.DeathBrewEffects;
import art.arcane.witchery.capability.WitcheryCapabilities;
import art.arcane.witchery.capability.WitcheryPlayerData;
import art.arcane.witchery.entity.AnimalMerchant;
import art.arcane.witchery.integration.BiomesOPlentySaplings;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.mixin.LegacyEnchantmentNamesAccessor;
import art.arcane.witchery.world.MirrorTravel;
import art.arcane.witchery.world.WitcheryDimensions;
import com.google.gson.JsonParseException;
import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public final class LegacyWorldIds {
    private static final String IMPORT_PROPERTY = "witchery.importLegacyWorld";
    private static final String CONFIG_PROPERTY = "witchery.legacyConfig";
    private static final String EGG_IDS_PROPERTY = "witchery.legacyEggIds";
    private static final String REGISTRY_FILE = "witchery-legacy-ids.dat";
    private static final String ENTRIES = "ItemData";
    private static final String DIMENSIONS = "Dimensions";
    private static final String POTIONS = "Potions";
    private static final String SPAWN_EGGS = "SpawnEggs";
    private static final String APOTHECARY_PROFESSION = "ApothecaryProfession";
    private static final int DEFAULT_APOTHECARY_PROFESSION = 2435;
    private static final Pattern APOTHECARY_CONFIG_ID = Pattern.compile("\\s*I:ApothecaryVillagerID\\s*=\\s*(-?\\d+)\\s*");
    private static final int ITEM_LORE_COMPONENT_VERSION = 1803;
    private static final ResourceLocation VANILLA_POTION = new ResourceLocation("minecraft", "potion");
    private static final ResourceLocation VANILLA_SPAWN_EGG = new ResourceLocation("minecraft", "spawn_egg");
    private final Map<Integer, ResourceLocation> blocks = new HashMap<>();
    private final Map<Integer, ResourceLocation> items = new HashMap<>();
    private final Map<Integer, ResourceLocation> vanillaItems = new HashMap<>();
    private final Set<Integer> legacyBopSaplings = new HashSet<>();
    private final Map<Integer, ResourceLocation> spawnEggs = new HashMap<>();
    private final Set<ResourceLocation> unmappedBlocks = ConcurrentHashMap.newKeySet();
    private final Set<ResourceLocation> unmappedItems = ConcurrentHashMap.newKeySet();
    private final LegacyWorldDimensions dimensions;
    private final LegacyWorldEffects effects;
    private final int apothecaryProfession;

    private LegacyWorldIds(CompoundTag snapshot) {
        dimensions = new LegacyWorldDimensions(snapshot.getCompound(DIMENSIONS));
        effects = new LegacyWorldEffects(snapshot.getCompound(POTIONS));
        apothecaryProfession = snapshot.contains(APOTHECARY_PROFESSION, Tag.TAG_ANY_NUMERIC)
                ? snapshot.getInt(APOTHECARY_PROFESSION) : DEFAULT_APOTHECARY_PROFESSION;
        var eggs = snapshot.getCompound(SPAWN_EGGS);
        for (String metadata : eggs.getAllKeys()) {
            spawnEggs.put(Integer.parseInt(metadata), new ResourceLocation(Witchery.MODID, eggs.getString(metadata) + "_spawn_egg"));
        }
        for (Tag tag : snapshot.getList(ENTRIES, Tag.TAG_COMPOUND)) {
            CompoundTag entry = (CompoundTag) tag;
            String key = entry.getString("K");
            if (key.length() < 2 || !entry.contains("V", Tag.TAG_ANY_NUMERIC)) continue;
            String name = key.substring(1);
            int number = entry.getInt("V");
            if (key.charAt(0) == 2 && BiomesOPlentySaplings.isLegacySaplings(name)) {
                legacyBopSaplings.add(number);
                continue;
            }
            ResourceLocation id = ResourceLocation.tryParse(name);
            if (id == null) continue;
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
            boolean captureApothecary = !snapshot.contains(APOTHECARY_PROFESSION, Tag.TAG_ANY_NUMERIC);
            if (captureDimensions || capturePotions || captureApothecary) {
                String configPath = System.getProperty(CONFIG_PROPERTY);
                List<String> config = configPath == null ? List.of() : Files.readAllLines(Path.of(configPath));
                if (captureDimensions) snapshot.put(DIMENSIONS, LegacyWorldDimensions.capture(config));
                if (capturePotions) snapshot.put(POTIONS, LegacyWorldEffects.capture(config));
                if (captureApothecary) snapshot.putInt(APOTHECARY_PROFESSION, captureApothecaryProfession(config));
            }
            String eggMapping = System.getProperty(EGG_IDS_PROPERTY);
            boolean captureEggs = eggMapping != null || !existing;
            if (captureEggs) {
                snapshot.put(SPAWN_EGGS, LegacySpawnEggs.capture(forge, eggMapping == null ? null : Path.of(eggMapping)));
            }
            LegacyWorldIds ids = new LegacyWorldIds(snapshot);
            if (ids.blocks.isEmpty() && ids.items.isEmpty()) return;
            if (captureDimensions || capturePotions || captureApothecary || captureEggs) {
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

    static int captureApothecaryProfession(List<String> config) {
        int profession = DEFAULT_APOTHECARY_PROFESSION;
        for (String line : config) {
            var entry = APOTHECARY_CONFIG_ID.matcher(line);
            if (entry.matches()) profession = Integer.parseInt(entry.group(1));
        }
        return profession;
    }

    boolean isApothecary(CompoundTag entity, String professionKey) {
        return entity.contains(professionKey, Tag.TAG_ANY_NUMERIC) && entity.getInt(professionKey) == apothecaryProfession;
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
    static Path dimensionArchive(Path world, ResourceKey<Level> dimension) {
        if (!WitcheryDimensions.levels().contains(dimension)) return null;
        var ids = find(world);
        return ids == null ? null : ids.dimensions.archive(world, dimension);
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
        var persisted = preparePlayerState(player);
        var retainedInventory = DeathBrewEffects.pendingLegacyInventory(persisted);
        var expiredDrops = persisted.getList("WITCPriIncInv", Tag.TAG_COMPOUND);
        var dreamSession = persisted.getCompound("WITCSpiritWorld");
        upgradeEntityEffects(player);
        repairEntityEffects(player.getCompound("RootVehicle").getCompound("Entity"));
        // The pending list can keep legacy effect IDs after an earlier import updated the player version.
        effects.upgrade(persisted.getList("WITCPoSpawn", Tag.TAG_COMPOUND));
        int to = SharedConstants.getCurrentVersion().getDataVersion().getVersion();
        if (from >= 100) {
            // These lists are outside the player inventory schema and can outlive the first import.
            repairLegacyInventory(retainedInventory, fixer, to);
            repairLegacyInventory(expiredDrops, fixer, to);
            repairLegacyInventory(dreamSession.getList("OverworldInventory", Tag.TAG_COMPOUND), fixer, to);
            repairLegacyInventory(dreamSession.getList("SpiritInventory", Tag.TAG_COMPOUND), fixer, to);
            repairStoredItems(player, fixer);
            return;
        }

        upgradeItems(player);
        // The native player and standalone inventory fixes still need their original vanilla fields.
        repairStoredItems(player, fixer, false);
        upgradeUntypedItems(retainedInventory, fixer, from, to);
        upgradeUntypedItems(dreamSession, fixer, from, to);
        upgradeUntypedItems(expiredDrops, fixer, from, to);
    }

    public static void repairPlayerWithoutLegacyRegistry(CompoundTag player, DataFixer fixer, int from) {
        var persisted = preparePlayerState(player);
        repairNamedStoredItems(player);
        var dreamSession = persisted.getCompound("WITCSpiritWorld");
        int currentVersion = SharedConstants.getCurrentVersion().getDataVersion().getVersion();
        for (ListTag inventory : List.of(DeathBrewEffects.pendingLegacyInventory(persisted),
                persisted.getList("WITCPriIncInv", Tag.TAG_COMPOUND),
                dreamSession.getList("OverworldInventory", Tag.TAG_COMPOUND),
                dreamSession.getList("SpiritInventory", Tag.TAG_COMPOUND))) {
            visitStoredCompounds(inventory, stack -> {
                if (!stack.contains("id", Tag.TAG_STRING) || !stack.contains("Count", Tag.TAG_BYTE) || stack.getByte("Count") <= 0
                        || from >= 100 && !hasLegacyStackEncoding(stack)) return;
                var id = ResourceLocation.tryParse(stack.getString("id"));
                if (id != null && !id.getPath().isEmpty() && ResourceLocation.DEFAULT_NAMESPACE.equals(id.getNamespace())) {
                    upgradeVanillaStack(stack, id, fixer, from, currentVersion, from >= 100);
                    upgradeStackTags(stack, from >= 100, from < 100);
                }
            });
        }
    }

    private static CompoundTag preparePlayerState(CompoundTag player) {
        if (player.getCompound("RootVehicle").get("Entity") instanceof CompoundTag vehicle) {
            LegacyChunkEntities.restoreEntity(vehicle);
        }
        var legacy = captureLegacyPlayerProgress(player);
        var forgeData = player.getCompound("ForgeData");
        var persisted = forgeData.getCompound(Player.PERSISTED_NBT_TAG);
        if (!legacy.isEmpty()) {
            boolean insideMirror = WitcheryDimensions.MIRROR.location().toString().equals(player.getString("Dimension"));
            MirrorTravel.importLegacyPlayerState(legacy, persisted, insideMirror);
        }
        if (legacy.getBoolean("CanRestoreInventory") && legacy.get("CachedInventory2") instanceof ListTag cached
                && (cached.isEmpty() || cached.getElementType() == Tag.TAG_COMPOUND)) {
            DeathBrewEffects.storeLegacyInventory(persisted, cached);
        }
        if (!persisted.isEmpty()) {
            forgeData.put(Player.PERSISTED_NBT_TAG, persisted);
            player.put("ForgeData", forgeData);
        }
        return persisted;
    }

    private static CompoundTag captureLegacyPlayerProgress(CompoundTag player) {
        var capabilities = player.getCompound("ForgeCaps");
        String playerDataId = WitcheryCapabilities.playerDataId().toString();
        var progress = capabilities.getCompound(playerDataId);
        var legacy = player.getCompound(WitcheryPlayerData.LEGACY_TAG).copy()
                .merge(progress.getCompound(WitcheryPlayerData.LEGACY_TAG));
        if (!legacy.isEmpty()) {
            progress.put(WitcheryPlayerData.LEGACY_TAG, legacy);
            capabilities.put(playerDataId, progress);
            player.put("ForgeCaps", capabilities);
        }
        return legacy;
    }

    private void repairLegacyInventory(ListTag inventory, DataFixer fixer, int to) {
        for (Tag entry : inventory) {
            var stack = (CompoundTag) entry;
            if (hasLegacyStackEncoding(stack)) {
                upgradeUntypedItems(stack, fixer, to, to);
            }
        }
    }

    void upgradeEntityEffects(CompoundTag entity) {
        effects.upgradeActive(entity.getList("ActiveEffects", Tag.TAG_COMPOUND));
    }

    public void repairEntityEffects(ListTag entities) {
        for (Tag entry : entities) repairEntityEffects((CompoundTag) entry);
    }

    public void repairBlockSpawnerEffects(ListTag tiles) {
        for (Tag entry : tiles) {
            var tile = (CompoundTag) entry;
            if (tile.getString("id").equals("minecraft:mob_spawner")) {
                LegacyChunkEntities.forEachSpawnerEntity(tile, this::repairEntityEffects);
            }
        }
    }

    private void repairEntityEffects(CompoundTag entity) {
        upgradeEntityEffects(entity);
        if (entity.getString("id").equals(LegacyChunkEntities.SPAWNER_CART)) {
            LegacyChunkEntities.forEachSpawnerEntity(entity, this::repairEntityEffects);
        }
        repairEntityEffects(entity.getList("Passengers", Tag.TAG_COMPOUND));
    }

    public void upgradeItems(Tag value) {
        if (value instanceof CompoundTag compound) {
            for (String key : compound.getAllKeys()) upgradeItems(compound.get(key));
            if (compound.contains("Count", Tag.TAG_ANY_NUMERIC)) upgradeStack(compound);
        } else if (value instanceof ListTag list) {
            for (Tag entry : list) upgradeItems(entry);
        }
    }

    public void repairStoredItems(Tag value, DataFixer fixer) {
        repairStoredItems(value, fixer, true);
    }

    public static void repairNamedStoredItems(Tag value) {
        visitStoredCompounds(value, stack -> {
            repairWaypointTags(stack, false);
            if (isNamedWitcheryStack(stack) && (!hasLegacyStackEncoding(stack) || upgradeWitcheryStack(stack, stack.getString("id")))) {
                upgradeStackTags(stack, true, false);
            }
        });
    }

    private void repairStoredItems(Tag value, DataFixer fixer, boolean repairVanilla) {
        int currentVersion = SharedConstants.getCurrentVersion().getDataVersion().getVersion();
        visitStoredCompounds(value, compound -> repairStoredCompound(compound, fixer, currentVersion, repairVanilla));
    }

    private static void visitStoredCompounds(Tag value, Consumer<CompoundTag> repair) {
        if (value instanceof CompoundTag compound) {
            repair.accept(compound);
            // Descend into the current children after replacing a saved stack or bottle.
            for (String key : compound.getAllKeys()) visitStoredCompounds(compound.get(key), repair);
        } else if (value instanceof ListTag list) {
            for (Tag entry : list) visitStoredCompounds(entry, repair);
        }
    }

    private void repairStoredCompound(CompoundTag compound, DataFixer fixer, int currentVersion, boolean repairVanilla) {
        repairWaypointTags(compound, false);
        // Both legacy and current item stacks store Count as a byte. Some tiles have their own integer Count.
        if (compound.contains("Count", Tag.TAG_BYTE) && hasLegacyStackEncoding(compound)) {
            if (repairVanilla) {
                upgradeUntypedStack(compound, fixer, currentVersion, currentVersion);
            } else if (upgradeStack(compound)) {
                upgradeStackTags(compound, true, false);
            }
        } else {
            upgradeItemBinding(compound);
            // A previous conversion can flatten the stack without touching its display or enchantments.
            if (isNamedWitcheryStack(compound)) upgradeStackTags(compound, true, false);
        }
        String id = compound.getString("id");
        upgradeMirrorExit(compound, id);
        upgradeMerchantItems(compound, id, fixer, currentVersion, currentVersion);
        if (compound.get("Brew") instanceof CompoundTag brew) {
            if (id.equals("witchery:droplet")) {
                repairLegacyInventory(brew.getList("Items", Tag.TAG_COMPOUND), fixer, currentVersion);
            } else if (id.equals("witchery:brew2") && hasLegacyStackEncoding(brew)) {
                // Converted projectiles can retain the original bottle outside the vanilla Item field.
                upgradeStack(brew);
            }
        }
        if (compound.contains("Count", Tag.TAG_ANY_NUMERIC) && compound.get("tag") instanceof CompoundTag itemTag) {
            // Vanilla's fixer does not visit the recipe inside a curse attached to an arbitrary item.
            repairLegacyInventory(BrewItemCurse.savedBrew(itemTag).getList("Items", Tag.TAG_COMPOUND), fixer, currentVersion);
            switch (id) {
                case "minecraft:potion", "minecraft:splash_potion", "minecraft:lingering_potion", "minecraft:tipped_arrow" ->
                        effects.repairConvertedPotion(itemTag.getList("CustomPotionEffects", Tag.TAG_COMPOUND));
                case "witchery:brewbottle", "witchery:bucketbrew" ->
                        repairLegacyInventory(itemTag.getList("Items", Tag.TAG_COMPOUND), fixer, currentVersion);
                case "witchery:brewbag", "witchery:leonardsurn" ->
                        repairLegacyInventory(itemTag.getCompound("Inventory").getList("Items", Tag.TAG_COMPOUND), fixer, currentVersion);
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
            upgradeMirrorExit(tile, tile.getString("id"));
            // A cursed control has an item-like ID and its own integer Count at the tile root.
            for (String key : tile.getAllKeys()) {
                Tag value = tile.get(key);
                upgradeItems(value);
                if (witcheryTile) upgradeUntypedItems(value, fixer, from, to);
            }
        }
    }

    private void upgradeMirrorExit(CompoundTag tile, String id) {
        if (id.equals("witchery:mirrorblock2") && tile.get("DimCoords") instanceof CompoundTag coords) {
            dimensions.upgradeReference(coords, "Dimension", "DimensionId");
        }
    }

    public void repairConvertedTileItems(ListTag tiles, DataFixer fixer, int version) {
        for (Tag entry : tiles) {
            var tile = (CompoundTag) entry;
            var id = ResourceLocation.tryParse(tile.getString("id"));
            if (id != null && id.getNamespace().equals(Witchery.MODID)) {
                for (String key : tile.getAllKeys()) upgradeUntypedItems(tile.get(key), fixer, version, version);
            }
        }
    }

    void upgradeMerchantItems(CompoundTag entity, String id, DataFixer fixer, int from, int to) {
        // These offers live outside the vanilla merchant data-fix schema.
        if (entity.get("ForgeData") instanceof CompoundTag forgeData) {
            upgradeUntypedItems(forgeData.get(AnimalMerchant.STOCK_TAG), fixer, from, to);
        }
        switch (id) {
            case "witchery:demon" -> upgradeUntypedItems(entity.get("Bargains"), fixer, from, to);
            case "witchery:goblin" -> upgradeUntypedItems(entity.get("Offers"), fixer, from, to);
            case "witchery:wolfman" -> {
                upgradeUntypedItems(entity.get("Offers"), fixer, from, to);
                if (entity.get("FormerVillager") instanceof CompoundTag former) {
                    upgradeUntypedItems(former.get("Offers"), fixer, from, to);
                }
            }
        }
    }

    void upgradeUntypedItems(Tag value, DataFixer fixer, int from, int to) {
        if (value instanceof CompoundTag compound) {
            for (String key : compound.getAllKeys()) upgradeUntypedItems(compound.get(key), fixer, from, to);
            if (compound.contains("Count", Tag.TAG_ANY_NUMERIC)) upgradeUntypedStack(compound, fixer, from, to);
        } else if (value instanceof ListTag list) {
            for (Tag entry : list) upgradeUntypedItems(entry, fixer, from, to);
        }
    }

    private void upgradeUntypedStack(CompoundTag stack, DataFixer fixer, int from, int to) {
        // Full chunk and entity migrations skip old stacks once their containing records have a modern version.
        boolean partiallyConverted = from >= 100 && hasLegacyStackEncoding(stack);
        boolean mapped = partiallyConverted && upgradeStack(stack);
        ResourceLocation id = stack.contains("id", Tag.TAG_ANY_NUMERIC)
                ? vanillaItems.get(stack.getInt("id") & 0xFFFF) : ResourceLocation.tryParse(stack.getString("id"));
        if (id == null) return;
        boolean witcheryItem = id.getNamespace().equals(Witchery.MODID);
        boolean vanillaItem = id.getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE);
        if (!vanillaItem && !witcheryItem && !mapped) return;
        if (vanillaItem && !mapped && (from < 100 || partiallyConverted)) {
            upgradeVanillaStack(stack, id, fixer, from, to, partiallyConverted);
        }
        upgradeStackTags(stack, witcheryItem || from >= 100, from < 100);
    }

    private static void upgradeVanillaStack(CompoundTag stack, ResourceLocation id, DataFixer fixer,
                                            int from, int to, boolean partiallyConverted) {
        var named = stack.copy();
        named.putString("id", id.toString());
        if (fixer.update(References.ITEM_STACK, new Dynamic<>(NbtOps.INSTANCE, named), partiallyConverted ? -1 : from, to)
                .getValue() instanceof CompoundTag fixed) {
            if (partiallyConverted) {
                restoreMixedVanillaStackTags(stack.getCompound("tag"), fixed);
            } else if (to < ITEM_LORE_COMPONENT_VERSION) {
                // Untyped chunk inventories receive only the first vanilla data-fixer pass.
                upgradeItemLore(fixed.getCompound("tag").getCompound("display"), true);
            }
            for (String key : Set.copyOf(stack.getAllKeys())) stack.remove(key);
            stack.merge(fixed);
        }
    }

    private static void upgradeStackTags(CompoundTag stack, boolean upgradeDisplay, boolean originalFormat) {
        var tag = stack.getCompound("tag");
        if (upgradeDisplay) upgradeItemDisplay(tag.getCompound("display"), originalFormat);
        upgradeEnchantments(tag, "ench", "Enchantments");
        upgradeEnchantments(tag, "StoredEnchantments", "StoredEnchantments");
    }

    static void repairWaypointTags(CompoundTag entity, boolean originalFormat) {
        // Vanilla's entity schema visits the carried parcel, but the route is a separate Witchery stack.
        if (entity.get("WITCWaypoint") instanceof CompoundTag waypoint && waypoint.getByte("Count") > 0) {
            upgradeStackTags(waypoint, true, originalFormat);
        }
    }

    private static void restoreMixedVanillaStackTags(CompoundTag original, CompoundTag fixed) {
        if (original.isEmpty()) return;
        var tag = fixed.getCompound("tag");
        // The old fixer assumes top-level Damage and plain display text are the only sources.
        if (original.contains("Damage", Tag.TAG_ANY_NUMERIC)) tag.put("Damage", original.get("Damage").copy());
        if (original.contains("display", Tag.TAG_COMPOUND)) tag.put("display", original.getCompound("display").copy());
        var enchantments = original.copy();
        upgradeEnchantments(enchantments, "ench", "Enchantments");
        upgradeEnchantments(enchantments, "StoredEnchantments", "StoredEnchantments");
        for (String key : List.of("ench", "Enchantments", "StoredEnchantments")) {
            if (enchantments.contains(key, Tag.TAG_LIST)) tag.put(key, enchantments.get(key));
        }
        fixed.put("tag", tag);
    }

    private static boolean hasLegacyStackEncoding(CompoundTag stack) {
        return stack.contains("id", Tag.TAG_ANY_NUMERIC) || stack.contains("Damage", Tag.TAG_ANY_NUMERIC)
                || WitcheryIngredient.hasLegacyIngredientDamage(stack);
    }

    private static boolean isNamedWitcheryStack(CompoundTag stack) {
        return stack.contains("Count", Tag.TAG_BYTE) && stack.getByte("Count") > 0
                && stack.getString("id").startsWith(Witchery.MODID + ":");
    }

    private static void upgradeEnchantments(CompoundTag tag, String originalKey, String currentKey) {
        var original = tag.getList(originalKey, Tag.TAG_COMPOUND);
        if (original.isEmpty()) return;
        var current = tag.getList(currentKey, Tag.TAG_COMPOUND);
        var named = new HashSet<String>();
        for (Tag entry : current) {
            var enchantment = (CompoundTag) entry;
            if (enchantment.contains("id", Tag.TAG_STRING)) named.add(enchantment.getString("id"));
        }
        var converted = new ListTag();
        original.removeIf(entry -> {
            var enchantment = (CompoundTag) entry;
            if (!enchantment.contains("id", Tag.TAG_ANY_NUMERIC)) return false;
            String name = LegacyEnchantmentNamesAccessor.witchery$getNamesById().get(enchantment.getInt("id"));
            if (name == null) return false;
            if (named.add(name)) {
                enchantment.putString("id", name);
                converted.add(enchantment);
            }
            return true;
        });
        if (original.isEmpty() && !originalKey.equals(currentKey)) tag.remove(originalKey);
        if (!converted.isEmpty()) {
            current.addAll(converted);
            tag.put(currentKey, current);
        }
    }

    private static void upgradeItemDisplay(CompoundTag display, boolean originalFormat) {
        if (display.contains("Name", Tag.TAG_STRING)) {
            String name = display.getString("Name");
            if (!name.isEmpty() && (originalFormat || !isComponent(name))) {
                display.putString("Name", Component.Serializer.toJson(Component.literal(name)));
            }
        }
        upgradeItemLore(display, originalFormat);
    }

    private static void upgradeItemLore(CompoundTag display, boolean originalFormat) {
        ListTag lore = display.getList("Lore", Tag.TAG_STRING);
        for (int line = 0; line < lore.size(); line++) {
            String text = lore.getString(line);
            if (originalFormat || !isComponent(text)) {
                lore.set(line, StringTag.valueOf(Component.Serializer.toJson(Component.literal(text))));
            }
        }
    }

    private static boolean isComponent(String text) {
        try {
            return Component.Serializer.fromJson(text) != null;
        } catch (JsonParseException ignored) {
            return false;
        }
    }

    private boolean upgradeStack(CompoundTag compound) {
        ResourceLocation name;
        boolean legacySapling;
        if (compound.contains("id", Tag.TAG_ANY_NUMERIC)) {
            int id = compound.getInt("id") & 0xFFFF;
            legacySapling = legacyBopSaplings.contains(id);
            name = items.get(id);
            if (name == null) name = vanillaItems.get(id);
        } else {
            String id = compound.getString("id");
            legacySapling = BiomesOPlentySaplings.isLegacySaplings(id);
            name = ResourceLocation.tryParse(id);
        }
        if (legacySapling && compound.getInt("Damage") == BiomesOPlentySaplings.LEGACY_FIR_METADATA
                && compound.getByte("Count") > 0 && ForgeRegistries.ITEMS.containsKey(BiomesOPlentySaplings.FIR)) {
            compound.putString("id", BiomesOPlentySaplings.FIR.toString());
            compound.remove("Damage");
            return true;
        }
        if (VANILLA_POTION.equals(name)) {
            effects.upgrade(compound.getCompound("tag").getList("CustomPotionEffects", Tag.TAG_COMPOUND));
        }
        if (VANILLA_SPAWN_EGG.equals(name)) name = spawnEggs.get(compound.getInt("Damage") & 0xFFFF);
        if (name == null || !name.getNamespace().equals(Witchery.MODID) || compound.getByte("Count") <= 0) return false;
        if (!upgradeWitcheryStack(compound, name.toString())) {
            if (unmappedItems.add(name)) Witchery.LOGGER.warn("Legacy item {} has no Witchery world import mapping yet", name);
            return false;
        }
        upgradeItemBinding(compound);
        return true;
    }

    private static boolean upgradeWitcheryStack(CompoundTag compound, String name) {
        CompoundTag named = compound.copy();
        named.putString("id", name);
        ItemStack stack = WitcheryIngredient.loadInventoryStack(named);
        if (stack.isEmpty()) return false;
        // ItemStack.of can insert a modern Damage field into the decoded copy.
        if (stack.isDamageableItem() && stack.getDamageValue() == 0 && !compound.getCompound("tag").contains("Damage", Tag.TAG_ANY_NUMERIC)) {
            int damage = compound.getInt("Damage");
            if (damage > 0) stack.setDamageValue(Math.min(damage, stack.getMaxDamage()));
        }
        compound.remove("Damage");
        compound.remove("tag");
        stack.save(compound);
        return true;
    }

    private void upgradeItemBinding(CompoundTag stack) {
        if (!(stack.get("tag") instanceof CompoundTag tag)) return;
        switch (stack.getString("id")) {
            case "witchery:waystone", "witchery:bound_waystone", "witchery:creature_bound_waystone" ->
                    dimensions.upgradeReference(tag, "PosD", "Dimension");
            case "witchery:door_key", "witchery:door_keyring" -> {
                dimensions.upgradeReference(tag, "doorD", "doorDimension");
                for (Tag key : tag.getList("doorKeys", Tag.TAG_COMPOUND)) {
                    dimensions.upgradeReference((CompoundTag) key, "doorD", "doorDimension");
                }
            }
        }
    }
}
