package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.ThrownBrew;
import art.arcane.witchery.lycanthropy.WerewolfVillagers;
import art.arcane.witchery.registry.WitcheryApothecary;
import art.arcane.witchery.registry.WitcheryEntities;
import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.FloatTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

public final class LegacyChunkEntities {
    private static final String NAMESPACE = Witchery.MODID + ":";
    private static final String ORIGINAL_TYPE = "WitcheryLegacyEntityType";
    private static final String LEGACY_APOTHECARY = "WitcheryLegacyApothecary";
    static final String SPAWNER_CART = "minecraft:spawner_minecart";
    private static final String LEGACY_CART_SPAWNER = "WitcheryLegacyCartSpawner";
    static final ResourceLocation INFINITY_EGG = ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "infinityegg");
    private static final String FALLING_INFINITY_EGG = "WitcheryLegacyFallingInfinityEgg";

    private LegacyChunkEntities() {
    }

    static void prepare(CompoundTag level, LegacyWorldIds ids, DataFixer fixer, int from) {
        for (Tag tag : level.getList("Entities", Tag.TAG_COMPOUND)) prepareEntity((CompoundTag) tag, ids, fixer, from, level);
        for (Tag entry : level.getList("TileEntities", Tag.TAG_COMPOUND)) {
            CompoundTag tile = (CompoundTag) entry;
            if (tile.getString("id").equals("MobSpawner")) prepareSpawner(tile, ids, fixer, from);
        }
    }

    @Nullable
    static CompoundTag upgradePlayerVehicle(CompoundTag riding, LegacyWorldIds ids, DataFixer fixer, int from) {
        if (!riding.contains("UUIDMost", Tag.TAG_LONG) || !riding.contains("UUIDLeast", Tag.TAG_LONG)) return null;
        var attachment = new UUID(riding.getLong("UUIDMost"), riding.getLong("UUIDLeast"));
        int to = SharedConstants.getCurrentVersion().getDataVersion().getVersion();
        CompoundTag root = null;
        CompoundTag vehicle = riding.copy();
        while (vehicle != null) {
            CompoundTag next = vehicle.get("Riding") instanceof CompoundTag parent ? parent : null;
            vehicle.remove("Riding");
            prepareEntity(vehicle, ids, fixer, from, null);
            if (!(fixer.update(References.ENTITY_TREE, new Dynamic<>(NbtOps.INSTANCE, vehicle), from, to)
                    .getValue() instanceof CompoundTag fixed)) return null;
            restoreEntity(fixed);
            if (EntityType.by(fixed).isEmpty()) return null;
            if (root != null) {
                // The old rider held a vehicle tag. The modern vehicle holds its riders.
                var passengers = fixed.getList("Passengers", Tag.TAG_COMPOUND);
                passengers.add(root);
                fixed.put("Passengers", passengers);
            }
            root = fixed;
            vehicle = next;
        }
        var saved = new CompoundTag();
        saved.put("Attach", NbtUtils.createUUID(attachment));
        saved.put("Entity", root);
        return saved;
    }

    private static void prepareEntity(CompoundTag entity, LegacyWorldIds ids, DataFixer fixer, int from,
                                      @Nullable CompoundTag originalChunk) {
        ids.upgradeEntityEffects(entity, from < 100);
        if (entity.contains("Riding", Tag.TAG_COMPOUND)) {
            prepareEntity(entity.getCompound("Riding"), ids, fixer, from, originalChunk);
        }
        int targetVersion = SharedConstants.getCurrentVersion().getDataVersion().getVersion();
        String oldType = entity.getString("id");
        if (isFallingInfinityEgg(entity, ids)) {
            // The vanilla fixer interprets TileID using vanilla's numeric registry.
            entity.putBoolean(FALLING_INFINITY_EGG, true);
        }
        if (oldType.equals("MinecartSpawner") || oldType.equals("Minecart") && entity.getInt("Type") == 4) {
            prepareSpawnerCart(entity, ids, fixer, from);
        }
        if ((oldType.equals("Villager") || oldType.equals("minecraft:villager")) && ids.isApothecary(entity, "Profession")) {
            entity.putBoolean(LEGACY_APOTHECARY, true);
        }
        ResourceLocation name = WitcheryEntities.legacyEntityId(oldType);
        ids.upgradeMerchantItems(entity, name == null ? oldType : name.toString(), fixer, from, targetVersion);
        if (name == null) return;
        LegacyWorldIds.repairWaypointTags(entity, true);
        if ((name.getPath().equals("werevillager") && ids.isApothecary(entity, "Profession"))
                || (name.getPath().equals("wolfman") && ids.isApothecary(entity, "FormerProfession"))) {
            entity.putBoolean(LEGACY_APOTHECARY, true);
        }
        // The vanilla Witch schema does not visit these brew stacks.
        switch (name.getPath()) {
            case "droplet" -> ids.upgradeUntypedItems(entity.getCompound("Brew"), fixer, from, targetVersion);
            case "brew", "brew2" -> name = prepareBrewProjectile(entity, name, ids, fixer, from, targetVersion);
            case "broom" -> {
                if (oldType.startsWith("witchery.") || oldType.startsWith("minecraft:witchery.")) {
                    var rotation = entity.getList("Rotation", Tag.TAG_FLOAT);
                    if (!rotation.isEmpty()) rotation.set(0, FloatTag.valueOf(rotation.getFloat(0) + 90));
                }
            }
        }
        if (!ForgeRegistries.ENTITY_TYPES.containsKey(name)) return;
        if (name.getPath().equals("bolt")) LegacyBoltAnchors.prepare(entity, ids, originalChunk);

        // Vanilla's old entity schema needs a known type to fix the entity tree and its typed fields.
        // Keep the Witchery type in the remainder, which survives both passes of the chunk fixer.
        String schemaType = switch (name.getPath()) {
            case "owl", "toad", "covenwitch", "mindrake", "wingedmonkey", "imp" -> "Wolf";
            case "cat" -> "Ozelot";
            case "deathhorse" -> "EntityHorse";
            case "werevillager" -> "Villager";
            case "bolt" -> "Arrow";
            case "item" -> "Item";
            default -> "Witch";
        };
        entity.putString(ORIGINAL_TYPE, name.toString());
        entity.putString("id", schemaType);
    }

    static boolean isFallingInfinityEgg(CompoundTag entity, LegacyWorldIds ids) {
        if (!entity.getString("id").equals("FallingSand")) return false;
        int number = entity.contains("TileID", Tag.TAG_ANY_NUMERIC)
                ? entity.getInt("TileID") : Byte.toUnsignedInt(entity.getByte("Tile"));
        return INFINITY_EGG.equals(ids.block(number));
    }

    private static void prepareSpawner(CompoundTag spawner, LegacyWorldIds ids, DataFixer fixer, int from) {
        prepareSpawnEntry(spawner, "EntityId", "SpawnData", ids, fixer, from);
        for (Tag entry : spawner.getList("SpawnPotentials", Tag.TAG_COMPOUND)) {
            prepareSpawnEntry((CompoundTag) entry, "Type", "Properties", ids, fixer, from);
        }
    }

    private static void prepareSpawnerCart(CompoundTag cart, LegacyWorldIds ids, DataFixer fixer, int from) {
        // Vanilla's horse split fails while walking the spawner inside a legacy minecart.
        // Fix its spawn rules as a block spawner and its vehicle fields as a regular minecart.
        var spawner = new CompoundTag();
        spawner.putString("id", "MobSpawner");
        for (String key : new String[]{"EntityId", "SpawnData", "SpawnPotentials"}) {
            Tag value = cart.get(key);
            if (value != null) {
                spawner.put(key, value);
                cart.remove(key);
            }
        }
        prepareSpawner(spawner, ids, fixer, from);
        // The old spawner fixes expect the block entity schema inside a chunk.
        var tiles = new ListTag();
        tiles.add(spawner);
        var level = new CompoundTag();
        level.put("TileEntities", tiles);
        level.putBoolean("TerrainPopulated", true);
        var chunk = new CompoundTag();
        chunk.put("Level", level);
        int flattenedVersion = 1493;
        CompoundTag flattened = DataFixTypes.CHUNK.update(fixer, chunk, from, flattenedVersion);
        ChunkStorage.injectDatafixingContext(flattened, Level.OVERWORLD, Optional.empty());
        CompoundTag upgraded = DataFixTypes.CHUNK.updateToCurrentVersion(fixer, flattened, flattenedVersion);
        CompoundTag fixed = upgraded.getList("block_entities", Tag.TAG_COMPOUND).getCompound(0);
        fixed.remove("id");
        cart.put(LEGACY_CART_SPAWNER, fixed);
        cart.remove("Type");
        cart.putString("id", "MinecartRideable");
        cart.putString(ORIGINAL_TYPE, SPAWNER_CART);
    }

    private static void prepareSpawnEntry(CompoundTag entry, String typeKey, String dataKey,
                                          LegacyWorldIds ids, DataFixer fixer, int from) {
        String name = entry.getString(typeKey);
        if (name.isEmpty()) return;
        if (!entry.contains(dataKey, Tag.TAG_COMPOUND)) {
            ResourceLocation id = WitcheryEntities.legacyEntityId(name);
            if (id == null || !ForgeRegistries.ENTITY_TYPES.containsKey(id)) return;
        }
        CompoundTag entity = entry.getCompound(dataKey);
        entity.putString("id", name);
        // A vanilla mob template can also carry a Witchery effect or an Apothecary profession.
        prepareEntity(entity, ids, fixer, from, null);
        // Vanilla copies the type field over the entity ID before it fixes the embedded entity.
        entry.putString(typeKey, entity.getString("id"));
        entry.put(dataKey, entity);
    }

    private static ResourceLocation prepareBrewProjectile(CompoundTag entity, ResourceLocation legacyType,
                                                          LegacyWorldIds ids, DataFixer fixer, int from, int to) {
        boolean fixedRecipe = legacyType.getPath().equals("brew");
        if (!fixedRecipe) ids.upgradeUntypedItems(entity.getCompound("Brew"), fixer, from, to);
        ItemStack stack = ThrownBrew.readLegacyPayload(entity, fixedRecipe);
        if (stack.isEmpty()) return legacyType;
        if (!fixedRecipe && entity.getBoolean("Spell")) entity.putBoolean("NoGravity", true);
        entity.put("Item", stack.save(new CompoundTag()));
        entity.remove("Brew");
        entity.remove("Spell");
        entity.remove("damageValue");
        return WitcheryEntities.THROWN_BREW.getId();
    }

    public static void restore(ListTag entities) {
        for (Tag tag : entities) restoreEntity((CompoundTag) tag);
    }

    static void restoreSpawner(CompoundTag spawner) {
        forEachSpawnerEntity(spawner, LegacyChunkEntities::restoreEntity);
    }

    static void forEachSpawnerEntity(CompoundTag spawner, Consumer<CompoundTag> visitor) {
        visitSpawnEntry(spawner.getCompound("SpawnData"), visitor);
        for (Tag entry : spawner.getList("SpawnPotentials", Tag.TAG_COMPOUND)) {
            visitSpawnEntry(((CompoundTag) entry).getCompound("data"), visitor);
        }
    }

    private static void visitSpawnEntry(CompoundTag data, Consumer<CompoundTag> visitor) {
        if (data.get("entity") instanceof CompoundTag entity) visitor.accept(entity);
    }

    static void restoreEntity(CompoundTag entity) {
        if (entity.getBoolean(FALLING_INFINITY_EGG)) {
            var state = new CompoundTag();
            state.putString("Name", INFINITY_EGG.toString());
            entity.put("BlockState", state);
            entity.remove(FALLING_INFINITY_EGG);
        }
        if (entity.contains(ORIGINAL_TYPE, Tag.TAG_STRING)) {
            String type = entity.getString(ORIGINAL_TYPE);
            entity.putString("id", type);
            entity.remove(ORIGINAL_TYPE);
            restoreEquipmentSlots(entity);
            if (type.equals(NAMESPACE + "bolt")) LegacyBoltAnchors.finishImport(entity);
            if (type.equals(NAMESPACE + "cat") && !entity.contains("variant")) {
                entity.putString("variant", "minecraft:tabby");
            } else if (type.equals(NAMESPACE + "werevillager") && entity.getInt("Xp") == 0
                    && !entity.getCompound("Offers").getList("Recipes", Tag.TAG_COMPOUND).isEmpty()) {
                // Legacy villagers kept their profession even without a modern job site.
                entity.putInt("Xp", 1);
            }
        } else {
            String type = entity.getString("id");
            ResourceLocation legacy = WitcheryEntities.legacyEntityId(type);
            if (legacy != null && !legacy.toString().equals(type) && ForgeRegistries.ENTITY_TYPES.containsKey(legacy)) {
                entity.putString("id", legacy.toString());
            }
        }
        if (entity.getBoolean(LEGACY_APOTHECARY)) {
            entity.remove(LEGACY_APOTHECARY);
            if (entity.getString("id").equals(NAMESPACE + "wolfman")) {
                if (!entity.contains("FormerVillager", Tag.TAG_COMPOUND)) {
                    CompoundTag former = WerewolfVillagers.legacyProfession(entity);
                    if (former != null) {
                        restoreApothecary(former);
                        entity.put("FormerVillager", former);
                    }
                }
            } else {
                restoreApothecary(entity);
            }
        }
        String type = entity.getString("id");
        if (type.startsWith(NAMESPACE)) {
            restoreOriginalIdentity(entity);
            restoreOriginalEquipment(entity);
        }
        restoreOriginalLivingData(entity);
        if (type.equals(SPAWNER_CART)) {
            if (entity.get(LEGACY_CART_SPAWNER) instanceof CompoundTag spawner) {
                entity.merge(spawner);
                entity.remove(LEGACY_CART_SPAWNER);
            }
            restoreSpawner(entity);
        }
        restore(entity.getList("Passengers", Tag.TAG_COMPOUND));
    }

    private static void restoreApothecary(CompoundTag villager) {
        var data = villager.getCompound("VillagerData");
        data.putString("profession", WitcheryApothecary.PROFESSION.getId().toString());
        villager.put("VillagerData", data);
        // Villagers with experience retain their profession while looking for a workstation.
        if (villager.getInt("Xp") == 0) villager.putInt("Xp", 1);
        if (villager.getCompound("Offers").getList("Recipes", Tag.TAG_COMPOUND).isEmpty()) {
            villager.put("Offers", WitcheryApothecary.residentOffers().createTag());
        }
    }

    private static void restoreEquipmentSlots(CompoundTag entity) {
        ListTag hands = entity.getList("HandItems", Tag.TAG_COMPOUND);
        ListTag armor = entity.getList("ArmorItems", Tag.TAG_COMPOUND);
        // Vanilla's legacy equipment split can write the four armor slots under HandItems and the two hands under ArmorItems.
        if (hands.size() == 4 || armor.size() == 2) {
            entity.put("HandItems", armor);
            entity.put("ArmorItems", hands);
        }
    }

    private static void restoreOriginalIdentity(CompoundTag entity) {
        LegacyWorldIds.upgradeStoredName(entity, "CustomName", false);
        restoreOriginalUuid(entity);
        if (entity.get("Leash") instanceof CompoundTag leash
                && !(leash.contains("X", Tag.TAG_ANY_NUMERIC) && leash.contains("Y", Tag.TAG_ANY_NUMERIC)
                && leash.contains("Z", Tag.TAG_ANY_NUMERIC))) {
            restoreOriginalUuid(leash);
        }
    }

    private static void restoreOriginalUuid(CompoundTag data) {
        if (!data.contains("UUIDMost", Tag.TAG_LONG) || !data.contains("UUIDLeast", Tag.TAG_LONG)) return;
        if (!data.hasUUID("UUID")) data.putUUID("UUID", new UUID(data.getLong("UUIDMost"), data.getLong("UUIDLeast")));
        data.remove("UUIDMost");
        data.remove("UUIDLeast");
    }

    static void restoreOriginalLivingData(CompoundTag entity) {
        if (entity.contains("HealF", Tag.TAG_ANY_NUMERIC)) {
            // The original writer stored both exact health and a rounded short for older readers.
            if (!entity.contains("Health", Tag.TAG_ANY_NUMERIC) || entity.contains("Health", Tag.TAG_SHORT)) {
                entity.putFloat("Health", entity.getFloat("HealF"));
            }
            entity.remove("HealF");
        }
        ListTag attributes = entity.getList("Attributes", Tag.TAG_COMPOUND);
        for (var iterator = attributes.listIterator(); iterator.hasNext();) {
            var attribute = (CompoundTag) iterator.next();
            String path = originalAttributePath(attribute.getString("Name"));
            if (path != null) {
                String current = "minecraft:" + path;
                boolean alreadyConverted = attributes.stream().map(tag -> ((CompoundTag) tag).getString("Name"))
                        .anyMatch(name -> name.equals(current) || name.equals(path));
                if (alreadyConverted) {
                    iterator.remove();
                    continue;
                }
                attribute.putString("Name", current);
            }
            for (Tag modifier : attribute.getList("Modifiers", Tag.TAG_COMPOUND)) {
                restoreOriginalUuid((CompoundTag) modifier);
            }
        }
    }

    @Nullable
    private static String originalAttributePath(String name) {
        return switch (name) {
            case "generic.maxHealth" -> "generic.max_health";
            case "generic.followRange" -> "generic.follow_range";
            case "generic.knockbackResistance" -> "generic.knockback_resistance";
            case "generic.movementSpeed" -> "generic.movement_speed";
            case "generic.attackDamage" -> "generic.attack_damage";
            case "horse.jumpStrength" -> "horse.jump_strength";
            case "zombie.spawnReinforcements" -> "zombie.spawn_reinforcements";
            default -> null;
        };
    }

    private static void restoreOriginalEquipment(CompoundTag entity) {
        // Earlier conversions can leave the original main hand, boots, leggings, chest and head list.
        ListTag equipment = entity.getList("Equipment", Tag.TAG_COMPOUND);
        if (!equipment.isEmpty()) {
            restoreOriginalEquipmentSlots(entity, equipment, "HandItems", 0, 1);
            restoreOriginalEquipmentSlots(entity, equipment, "ArmorItems", 1, 4);
            entity.remove("Equipment");
        }
        ListTag chances = entity.getList("DropChances", Tag.TAG_FLOAT);
        if (!chances.isEmpty()) {
            var defaultChance = FloatTag.valueOf(Mob.DEFAULT_EQUIPMENT_DROP_CHANCE);
            if (!entity.contains("HandDropChances", Tag.TAG_LIST)) {
                var hand = new ListTag();
                hand.add(chances.get(0));
                hand.add(defaultChance);
                entity.put("HandDropChances", hand);
            }
            if (!entity.contains("ArmorDropChances", Tag.TAG_LIST)) {
                var armor = new ListTag();
                for (int i = 1; i <= 4; i++) armor.add(i < chances.size() ? chances.get(i) : defaultChance);
                entity.put("ArmorDropChances", armor);
            }
            entity.remove("DropChances");
        }
    }

    private static void restoreOriginalEquipmentSlots(CompoundTag entity, ListTag equipment, String key,
                                                      int start, int count) {
        if (entity.contains(key, Tag.TAG_LIST)) return;
        int version = SharedConstants.getCurrentVersion().getDataVersion().getVersion();
        var slots = new ListTag();
        for (int i = start; i < start + count; i++) {
            CompoundTag stack = equipment.getCompound(i);
            LegacyWorldIds.repairNamedVanillaStack(stack, DataFixers.getDataFixer(), version, version);
            slots.add(stack);
        }
        // The original list has no offhand slot.
        if (start == 0) slots.add(new CompoundTag());
        entity.put(key, slots);
    }
}
