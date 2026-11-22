package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.AnimalMerchant;
import com.mojang.datafixers.DataFixer;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Locale;

final class LegacyChunkEntities {
    private static final String LEGACY_PREFIX = Witchery.MODID + ".";
    private static final String NAMESPACE = Witchery.MODID + ":";
    private static final String ORIGINAL_TYPE = "WitcheryLegacyEntityType";

    private LegacyChunkEntities() {
    }

    static void prepare(CompoundTag level, LegacyWorldIds ids, DataFixer fixer, int from) {
        for (Tag tag : level.getList("Entities", Tag.TAG_COMPOUND)) prepareEntity((CompoundTag) tag, ids, fixer, from);
    }

    private static void prepareEntity(CompoundTag entity, LegacyWorldIds ids, DataFixer fixer, int from) {
        if (entity.contains("Riding", Tag.TAG_COMPOUND)) {
            prepareEntity(entity.getCompound("Riding"), ids, fixer, from);
        }
        var forgeData = entity.getCompound("ForgeData");
        if (forgeData.contains(AnimalMerchant.STOCK_TAG, Tag.TAG_COMPOUND)) {
            ids.upgradeVanillaItems(forgeData.getCompound(AnimalMerchant.STOCK_TAG), fixer, from,
                    SharedConstants.getCurrentVersion().getDataVersion().getVersion());
        }

        String oldType = entity.getString("id");
        if (!oldType.startsWith(LEGACY_PREFIX) && !oldType.startsWith(NAMESPACE)) return;
        ResourceLocation name = ResourceLocation.tryParse(NAMESPACE + oldType.substring(NAMESPACE.length()).toLowerCase(Locale.ROOT));
        if (name == null || !ForgeRegistries.ENTITY_TYPES.containsKey(name)) return;
        if (name.getPath().equals("goblin") && entity.contains("Offers", Tag.TAG_COMPOUND)) {
            // Keep the Goblin's profession and trade list. The vanilla Witch schema does not type its offers.
            ids.upgradeVanillaItems(entity.getCompound("Offers"), fixer, from,
                    SharedConstants.getCurrentVersion().getDataVersion().getVersion());
        }

        // Vanilla's old entity schema needs a known type to fix the entity tree and its typed fields.
        // Keep the Witchery type in the remainder, which survives both passes of the chunk fixer.
        String schemaType = switch (name.getPath()) {
            case "owl", "toad", "covenwitch", "mindrake", "wingedmonkey" -> "Wolf";
            case "cat" -> "Ozelot";
            case "deathhorse" -> "EntityHorse";
            case "item" -> "Item";
            default -> "Witch";
        };
        entity.putString(ORIGINAL_TYPE, name.toString());
        entity.putString("id", schemaType);
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
