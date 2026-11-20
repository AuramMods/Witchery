package art.arcane.witchery.item;

import art.arcane.witchery.client.model.VampireClothingModel;
import art.arcane.witchery.registry.WitcheryItems;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public final class VampireClothingItem extends ArmorItem implements DyeableLeatherItem {
    public enum Style {
        HAT("vampirehat", Type.HELMET, false, false),
        HELMET("vampirehelmet", Type.HELMET, false, true),
        COAT("vampirecoat", Type.CHESTPLATE, false, false),
        FITTED_COAT("vampirecoat_female", Type.CHESTPLATE, true, false),
        CHAIN_COAT("vampirechaincoat", Type.CHESTPLATE, false, true),
        FITTED_CHAIN_COAT("vampirechaincoat_female", Type.CHESTPLATE, true, true),
        TROUSERS("vampirelegs", Type.LEGGINGS, false, false),
        KILT("vampirelegs_kilt", Type.LEGGINGS, true, false),
        BOOTS("vampireboots", Type.BOOTS, false, false);

        private final String path;
        private final Type type;
        private final boolean fitted;
        private final boolean reinforced;

        Style(String path, Type type, boolean fitted, boolean reinforced) {
            this.path = path;
            this.type = type;
            this.fitted = fitted;
            this.reinforced = reinforced;
        }

        public String path() { return path; }
        public Type armorType() { return type; }
        public boolean fitted() { return fitted; }
    }

    private enum Material implements ArmorMaterial {
        CLOTH(ArmorMaterials.LEATHER),
        REINFORCED_CLOTH(ArmorMaterials.IRON);

        private final ArmorMaterials defense;

        Material(ArmorMaterials defense) {
            this.defense = defense;
        }

        @Override
        public int getDurabilityForType(Type type) { return ArmorMaterials.IRON.getDurabilityForType(type); }

        @Override
        public int getDefenseForType(Type type) { return defense.getDefenseForType(type); }

        @Override
        public int getEnchantmentValue() { return ArmorMaterials.GOLD.getEnchantmentValue(); }

        @Override
        public SoundEvent getEquipSound() { return ArmorMaterials.LEATHER.getEquipSound(); }

        @Override
        public Ingredient getRepairIngredient() { return ArmorMaterials.LEATHER.getRepairIngredient(); }

        @Override
        public String getName() { return "witchery:vampire_clothing"; }

        @Override
        public float getToughness() { return 0; }

        @Override
        public float getKnockbackResistance() { return 0; }
    }

    private final Style style;

    public VampireClothingItem(Style style) {
        super(style.reinforced ? Material.REINFORCED_CLOTH : Material.CLOTH, style.type,
                new Properties().rarity(Rarity.UNCOMMON));
        this.style = style;
    }

    public Style style() {
        return style;
    }

    public static ItemStack make(Style style) {
        return new ItemStack(WitcheryItems.getRequired(style.path()).get());
    }

    @Override
    public int getColor(ItemStack stack) {
        return hasCustomColor(stack) ? DyeableLeatherItem.super.getColor(stack) : 0xCC0000;
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot, @Nullable String type) {
        String base = "witchery:textures/entities/vampirearmor";
        if (slot == EquipmentSlot.LEGS) return base + ".png";
        if (type == null) return base + "_over_first.png";
        return base + (style.reinforced && slot == EquipmentSlot.CHEST ? "_reinforced_overlay.png" : "_over.png");
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(VampireClothingModel.extension(this));
    }

    public static int lightPieces(LivingEntity wearer) {
        int pieces = 0;
        for (ItemStack stack : wearer.getArmorSlots()) {
            if (stack.getItem() instanceof VampireClothingItem clothing && !clothing.style.reinforced) pieces++;
        }
        return pieces;
    }
}
