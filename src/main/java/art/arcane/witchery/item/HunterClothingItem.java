package art.arcane.witchery.item;

import art.arcane.witchery.client.model.HunterClothingModel;
import art.arcane.witchery.lycanthropy.Lycanthropy;
import art.arcane.witchery.registry.WitcheryItems;
import art.arcane.witchery.vampirism.Vampirism;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
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

public final class HunterClothingItem extends ArmorItem implements DyeableLeatherItem {
    public enum Piece {
        HAT("hunterhat", Type.HELMET, 4139550),
        COAT("huntercoat", Type.CHESTPLATE, 4139550),
        LEGS("hunterlegs", Type.LEGGINGS, 4798251),
        BOOTS("hunterboots", Type.BOOTS, 1642763);

        private final String baseId;
        private final Type type;
        private final int color;

        Piece(String baseId, Type type, int color) {
            this.baseId = baseId;
            this.type = type;
            this.color = color;
        }

        public String path(Upgrade upgrade) {
            return baseId + upgrade.suffix;
        }

        public Type armorType() {
            return type;
        }
    }

    public enum Upgrade {
        PLAIN(""), SILVERED("silvered"), GARLICKED("garlicked");

        private final String suffix;

        Upgrade(String suffix) {
            this.suffix = suffix;
        }
    }

    private enum Material implements ArmorMaterial {
        NULLIFIED_LEATHER;

        @Override public int getDurabilityForType(Type type) { return ArmorMaterials.IRON.getDurabilityForType(type); }
        @Override public int getDefenseForType(Type type) { return ArmorMaterials.LEATHER.getDefenseForType(type); }
        @Override public int getEnchantmentValue() { return ArmorMaterials.LEATHER.getEnchantmentValue(); }
        @Override public SoundEvent getEquipSound() { return ArmorMaterials.LEATHER.getEquipSound(); }
        @Override public Ingredient getRepairIngredient() { return ArmorMaterials.LEATHER.getRepairIngredient(); }
        @Override public String getName() { return "witchery:hunter_clothing"; }
        @Override public float getToughness() { return 0; }
        @Override public float getKnockbackResistance() { return 0; }
    }

    private final Piece piece;
    private final Upgrade upgrade;

    public HunterClothingItem(Piece piece, Upgrade upgrade) {
        super(Material.NULLIFIED_LEATHER, piece.type, new Properties().rarity(Rarity.UNCOMMON));
        this.piece = piece;
        this.upgrade = upgrade;
    }

    public Piece piece() { return piece; }
    public Upgrade upgrade() { return upgrade; }

    public static ItemStack make(Piece piece, Upgrade upgrade) {
        return WitcheryItems.getRequired(piece.path(upgrade)).get().getDefaultInstance();
    }

    public static boolean fullSet(LivingEntity wearer, Upgrade required) {
        for (Piece piece : Piece.values()) {
            ItemStack armor = wearer.getItemBySlot(piece.type.getSlot());
            if (!(armor.getItem() instanceof HunterClothingItem clothing) || clothing.piece != piece
                    || clothing.upgrade.ordinal() < required.ordinal()) return false;
        }
        return true;
    }

    public static boolean resistsMagic(LivingEntity wearer) {
        return fullSet(wearer, Upgrade.PLAIN) && wearer.getRandom().nextFloat() < 0.25F;
    }

    public static boolean resistsCurse(LivingEntity wearer) {
        return fullSet(wearer, Upgrade.PLAIN) && wearer.getRandom().nextFloat() < 0.9F;
    }

    public static void tick(ServerPlayer player) {
        if (player.tickCount % 20 != 2) return;
        boolean werewolf = Lycanthropy.rank(player) > 0;
        boolean vampire = Vampirism.isVampire(player);
        if (!werewolf && !vampire) return;
        for (ItemStack stack : player.getArmorSlots()) {
            if (stack.getItem() instanceof HunterClothingItem item
                    && (werewolf && item.upgrade != Upgrade.PLAIN || vampire && item.upgrade == Upgrade.GARLICKED)) {
                player.hurt(player.damageSources().inFire(), 1.0F);
                return;
            }
        }
    }

    public boolean silverProtection(DamageSource source) {
        return upgrade != Upgrade.PLAIN && source.getEntity() instanceof LivingEntity attacker && Lycanthropy.isShifted(attacker);
    }

    public boolean reinforcedAgainst(DamageSource source) {
        return silverProtection(source) || upgrade == Upgrade.GARLICKED && Vampirism.isVampire(source.getEntity());
    }

    @Override
    public int getColor(ItemStack stack) {
        return hasCustomColor(stack) ? DyeableLeatherItem.super.getColor(stack) : piece.color;
    }

    @Override
    public String getArmorTexture(ItemStack stack, Entity wearer, EquipmentSlot slot, @Nullable String type) {
        String layer = slot == EquipmentSlot.LEGS ? "2" : "";
        return "witchery:textures/entities/hunterclothes" + layer + (type == null ? ".png" : "_overlay.png");
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(HunterClothingModel.extension(this));
    }
}
