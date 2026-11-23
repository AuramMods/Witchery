package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.AnimalMerchant;
import art.arcane.witchery.item.BrewBottleItem;
import art.arcane.witchery.item.ThrowableBrewItem;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryEntities;
import com.mojang.datafixers.DataFixer;
import net.minecraft.SharedConstants;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
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
        ItemStack stack;
        if (legacyType.getPath().equals("brew")) {
            if (!entity.contains("damageValue", Tag.TAG_ANY_NUMERIC)) return legacyType;
            var ingredient = new CompoundTag();
            ingredient.putString("id", "witchery:ingredient");
            ingredient.putByte("Count", (byte) 1);
            ingredient.putInt("Damage", entity.getInt("damageValue"));
            stack = WitcheryIngredient.loadInventoryStack(ingredient);
            if (!(stack.getItem() instanceof ThrowableBrewItem) && !stack.is(WitcheryIngredient.QUICKLIME.item())
                    && !stack.is(WitcheryIngredient.ROCK.item()) && !stack.is(WitcheryIngredient.WEB.item())) {
                return legacyType;
            }
        } else {
            var brew = entity.getCompound("Brew");
            ids.upgradeVanillaItems(brew, fixer, from, to);
            stack = ItemStack.of(brew);
            if (!(stack.getItem() instanceof BrewBottleItem)) return legacyType;
            if (entity.getBoolean("Spell")) entity.putBoolean("NoGravity", true);
        }
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
