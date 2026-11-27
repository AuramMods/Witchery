package art.arcane.witchery.entity;

import art.arcane.witchery.infusion.InfusionEnergy;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

import java.util.List;

public final class WitcheryBolt extends AbstractArrow {
    private static final EntityDataAccessor<Byte> BOLT_TYPE = SynchedEntityData.defineId(WitcheryBolt.class, EntityDataSerializers.BYTE);

    public enum Kind {
        STAKE(WitcheryIngredient.STAKE_BOLT), ANTI_MAGIC(WitcheryIngredient.ANTI_MAGIC_BOLT),
        POWERED_ANTI_MAGIC(WitcheryIngredient.ANTI_MAGIC_BOLT), HOLY(WitcheryIngredient.HOLY_BOLT),
        SILVER(WitcheryIngredient.SILVER_BOLT);

        private static final Kind[] BY_ID = values();
        private final WitcheryIngredient ingredient;

        Kind(WitcheryIngredient ingredient) {
            this.ingredient = ingredient;
        }

        private static Kind fromId(int id) {
            return id >= 0 && id < BY_ID.length ? BY_ID[id] : STAKE;
        }
    }

    public WitcheryBolt(EntityType<? extends WitcheryBolt> type, Level level) {
        super(type, level);
    }

    public WitcheryBolt(LivingEntity shooter, Kind kind) {
        super(WitcheryEntities.BOLT.get(), shooter, shooter.level());
        setKind(kind);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(BOLT_TYPE, (byte) 0);
    }

    public Kind kind() {
        return Kind.fromId(entityData.get(BOLT_TYPE));
    }

    public boolean isWoodenDamage() {
        return switch (kind()) {
            case STAKE, ANTI_MAGIC, POWERED_ANTI_MAGIC -> true;
            case HOLY, SILVER -> false;
        };
    }

    public static boolean poweredAntiMagic(DamageSource source) {
        return source.getDirectEntity() instanceof WitcheryBolt bolt && bolt.kind() == Kind.POWERED_ANTI_MAGIC;
    }

    private void setKind(Kind kind) {
        entityData.set(BOLT_TYPE, (byte) kind.ordinal());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (!tag.contains("pickup") && tag.contains("player", Tag.TAG_ANY_NUMERIC)) {
            pickup = tag.getBoolean("player") ? Pickup.ALLOWED : Pickup.DISALLOWED;
        }
        setKind(Kind.fromId(tag.getInt("boltType")));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("boltType", kind().ordinal());
    }

    @Override
    protected ItemStack getPickupItem() {
        return kind().ingredient.item().getDefaultInstance();
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        if (!level().isClientSide && kind() == Kind.POWERED_ANTI_MAGIC && hit.getEntity() instanceof LivingEntity target) {
            for (MobEffectInstance effect : List.copyOf(target.getActiveEffects())) {
                if (effect.getEffect() != MobEffects.POISON && effect.getEffect() != MobEffects.WITHER
                        && effect.getEffect() != MobEffects.CONFUSION) target.removeEffect(effect.getEffect());
            }
            InfusionEnergy.drain(target, 0.5F);
        }
        super.onHitEntity(hit);
    }

    public float modifyDamage(LivingEntity target, float amount) {
        if (kind() != Kind.HOLY) return amount;
        return CreatureAlignment.undead(target) || CreatureAlignment.demonic(target) ? Mth.floor(amount * 1.5F) : amount;
    }

    public void applyBowEnchantments(ItemStack bow) {
        int power = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, bow);
        if (power > 0) setBaseDamage(getBaseDamage() + 0.5 * power + 0.5);
        setKnockback(EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, bow));
        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, bow) > 0) setSecondsOnFire(100);
    }
}
