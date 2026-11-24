package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.AnimalMerchant;
import art.arcane.witchery.entity.ThrownBrew;
import art.arcane.witchery.registry.WitcheryEntities;
import com.mojang.datafixers.DataFixer;
import com.mojang.serialization.Dynamic;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.datafix.fixes.References;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.UUID;

final class LegacyChunkEntities {
    private static final String LEGACY_PREFIX = Witchery.MODID + ".";
    private static final String NAMESPACE = Witchery.MODID + ":";
    private static final String ORIGINAL_TYPE = "WitcheryLegacyEntityType";

    private LegacyChunkEntities() {
    }

    static void prepare(CompoundTag level, LegacyWorldIds ids, DataFixer fixer, int from) {
        for (Tag tag : level.getList("Entities", Tag.TAG_COMPOUND)) prepareEntity((CompoundTag) tag, ids, fixer, from);
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
            prepareEntity(vehicle, ids, fixer, from);
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

    private static void prepareEntity(CompoundTag entity, LegacyWorldIds ids, DataFixer fixer, int from) {
        ids.upgradeEntityEffects(entity);
        if (entity.contains("Riding", Tag.TAG_COMPOUND)) {
            prepareEntity(entity.getCompound("Riding"), ids, fixer, from);
        }
        int targetVersion = SharedConstants.getCurrentVersion().getDataVersion().getVersion();
        var forgeData = entity.getCompound("ForgeData");
        if (forgeData.contains(AnimalMerchant.STOCK_TAG, Tag.TAG_COMPOUND)) {
            ids.upgradeVanillaItems(forgeData.getCompound(AnimalMerchant.STOCK_TAG), fixer, from, targetVersion);
        }

        String oldType = entity.getString("id");
        if (!oldType.startsWith(LEGACY_PREFIX) && !oldType.startsWith(NAMESPACE)) return;
        ResourceLocation name = ResourceLocation.tryParse(NAMESPACE + oldType.substring(NAMESPACE.length()).toLowerCase(Locale.ROOT));
        if (name == null) return;
        // The vanilla Witch schema does not visit these trade and brew stacks.
        switch (name.getPath()) {
            case "goblin", "wolfman" -> ids.upgradeVanillaItems(entity.getCompound("Offers"), fixer, from, targetVersion);
            case "demon" -> ids.upgradeVanillaItems(entity.getCompound("Bargains"), fixer, from, targetVersion);
            case "droplet" -> ids.upgradeVanillaItems(entity.getCompound("Brew"), fixer, from, targetVersion);
            case "brew", "brew2" -> name = prepareBrewProjectile(entity, name, ids, fixer, from, targetVersion);
        }
        if (!ForgeRegistries.ENTITY_TYPES.containsKey(name)) return;
        if (name.getPath().equals("bolt") && entity.contains("inTile", Tag.TAG_BYTE)) {
            entity.putInt("inTile", Byte.toUnsignedInt(entity.getByte("inTile")));
        }

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

    private static ResourceLocation prepareBrewProjectile(CompoundTag entity, ResourceLocation legacyType,
                                                          LegacyWorldIds ids, DataFixer fixer, int from, int to) {
        boolean fixedRecipe = legacyType.getPath().equals("brew");
        if (!fixedRecipe) ids.upgradeVanillaItems(entity.getCompound("Brew"), fixer, from, to);
        ItemStack stack = ThrownBrew.readLegacyPayload(entity, fixedRecipe);
        if (stack.isEmpty()) return legacyType;
        if (!fixedRecipe && entity.getBoolean("Spell")) entity.putBoolean("NoGravity", true);
        entity.put("Item", stack.save(new CompoundTag()));
        entity.remove("Brew");
        entity.remove("Spell");
        entity.remove("damageValue");
        return WitcheryEntities.THROWN_BREW.getId();
    }

    static void restore(CompoundTag chunk) {
        for (Tag tag : chunk.getList("entities", Tag.TAG_COMPOUND)) restoreEntity((CompoundTag) tag);
    }

    private static void restoreEntity(CompoundTag entity) {
        if (entity.contains(ORIGINAL_TYPE, Tag.TAG_STRING)) {
            String type = entity.getString(ORIGINAL_TYPE);
            entity.putString("id", type);
            entity.remove(ORIGINAL_TYPE);
            restoreEquipmentSlots(entity);
            if (type.equals(NAMESPACE + "cat") && !entity.contains("variant")) {
                entity.putString("variant", "minecraft:tabby");
            } else if (type.equals(NAMESPACE + "werevillager") && entity.getInt("Xp") == 0
                    && !entity.getCompound("Offers").getList("Recipes", Tag.TAG_COMPOUND).isEmpty()) {
                // Legacy villagers kept their profession even without a modern job site.
                entity.putInt("Xp", 1);
            }
        }
        for (Tag tag : entity.getList("Passengers", Tag.TAG_COMPOUND)) restoreEntity((CompoundTag) tag);
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
}
