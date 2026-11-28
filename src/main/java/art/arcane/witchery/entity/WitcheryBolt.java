package art.arcane.witchery.entity;

import art.arcane.witchery.infusion.InfusionEnergy;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.world.migration.LegacyBoltAnchors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class WitcheryBolt extends AbstractArrow {
    private static final EntityDataAccessor<Byte> BOLT_TYPE = SynchedEntityData.defineId(WitcheryBolt.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Boolean> LEGACY_ATTACHMENT = SynchedEntityData.defineId(WitcheryBolt.class, EntityDataSerializers.BOOLEAN);
    @Nullable private CompoundTag pendingAnchor;
    @Nullable private BlockPos savedAnchorPosition;
    @Nullable private BlockState savedAnchorState;

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

    public void shootFromCrossbow(LivingEntity shooter, float yaw, float power) {
        LegacyArrowLaunch.shoot(this, shooter, yaw, 0.3F, power * 3F, random);
    }

    @Override
    protected float getWaterInertia() {
        return 0.8F;
    }

    @Override
    public boolean updateFluidHeightAndDoFluidPushing(TagKey<Fluid> tag, double motionScale) {
        if (tag != FluidTags.WATER) return super.updateFluidHeightAndDoFluidPushing(tag, motionScale);
        var water = ForgeMod.WATER_TYPE.get();
        super.updateFluidHeightAndDoFluidPushing(state -> state.getFluidType() != water);

        // 1.7.10 shrank the vertical bounds before rounding to cells. An inverted interval can still sample one row.
        var bounds = getBoundingBox();
        int minX = Mth.floor(bounds.minX + 0.001D);
        int maxX = Mth.floor(bounds.maxX - 0.001D + 1);
        int minY = Mth.floor(bounds.minY + (double) 0.4F + 0.001D);
        int maxY = Mth.floor(bounds.maxY - (double) 0.4F - 0.001D + 1);
        int minZ = Mth.floor(bounds.minZ + 0.001D);
        int maxZ = Mth.floor(bounds.maxZ - 0.001D + 1);
        if (minY >= maxY || !level().hasChunksAt(minX, minZ, maxX, maxZ)) return false;

        boolean touchingWater = false;
        double depth = 0;
        Vec3 flow = Vec3.ZERO;
        for (BlockPos pos : BlockPos.betweenClosed(minX, minY, minZ, maxX - 1, maxY - 1, maxZ - 1)) {
            var fluid = level().getFluidState(pos);
            if (fluid.getFluidType() != water) continue;
            touchingWater = true;
            depth = Math.max(depth, pos.getY() + fluid.getHeight(level(), pos) - bounds.minY);
            flow = flow.add(fluid.getFlow(level(), pos));
        }
        if (touchingWater) {
            setFluidTypeHeight(water, depth);
            if (isPushedByFluid(water)) setDeltaMovement(getDeltaMovement().add(flow.normalize().scale(motionScale)));
        }
        return touchingWater;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(BOLT_TYPE, (byte) 0);
        entityData.define(LEGACY_ATTACHMENT, false);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if (level().isClientSide && LEGACY_ATTACHMENT.equals(accessor)) {
            inGround = entityData.get(LEGACY_ATTACHMENT);
        }
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
        pendingAnchor = tag.get(LegacyBoltAnchors.PENDING) instanceof CompoundTag pending ? pending.copy() : null;
        savedAnchorPosition = tag.contains(LegacyBoltAnchors.POSITION, Tag.TAG_LONG)
                ? BlockPos.of(tag.getLong(LegacyBoltAnchors.POSITION)) : null;
        savedAnchorState = savedAnchorPosition != null && tag.get("inBlockState") instanceof CompoundTag state
                ? NbtUtils.readBlockState(level().holderLookup(Registries.BLOCK), state) : null;
        entityData.set(LEGACY_ATTACHMENT, inGround && savedAnchorPosition != null
                && (savedAnchorState != null || hasPendingLegacyBoundaryAnchor()));
        if (!tag.contains("pickup") && tag.contains("player", Tag.TAG_ANY_NUMERIC)) {
            pickup = tag.getBoolean("player") ? Pickup.ALLOWED : Pickup.DISALLOWED;
        }
        setKind(Kind.fromId(tag.getInt("boltType")));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (inGround && pendingAnchor != null) tag.put(LegacyBoltAnchors.PENDING, pendingAnchor.copy());
        if (inGround && savedAnchorPosition != null) {
            tag.putLong(LegacyBoltAnchors.POSITION, savedAnchorPosition.asLong());
            if (savedAnchorState != null) tag.put("inBlockState", NbtUtils.writeBlockState(savedAnchorState));
        }
        tag.putInt("boltType", kind().ordinal());
    }

    private boolean hasPendingLegacyBoundaryAnchor() {
        return inGround && pendingAnchor != null && pendingAnchor.contains(LegacyBoltAnchors.BOUNDARY_STATES, Tag.TAG_LIST);
    }

    public boolean canResolveLegacyBoundaryAnchor() {
        return hasPendingLegacyBoundaryAnchor() && savedAnchorPosition != null && level().hasChunkAt(savedAnchorPosition);
    }

    @Nullable
    public Boolean shouldFallFromSavedAnchor() {
        if (!inGround || !entityData.get(LEGACY_ATTACHMENT)) return null;
        if (level().isClientSide) return false;
        if (savedAnchorPosition == null) return null;
        if (!level().hasChunkAt(savedAnchorPosition)) return false;
        BlockState current = level().getBlockState(savedAnchorPosition);
        if (savedAnchorState == null && pendingAnchor != null && pendingAnchor
                .getList(LegacyBoltAnchors.BOUNDARY_STATES, Tag.TAG_COMPOUND).contains(NbtUtils.writeBlockState(current))) {
            savedAnchorState = current;
            pendingAnchor = null;
        }
        if (current.equals(savedAnchorState)) return false;
        clearSavedAnchor();
        hasImpulse = true;
        return true;
    }

    @Override
    protected void onHitBlock(BlockHitResult hit) {
        clearSavedAnchor();
        super.onHitBlock(hit);
    }

    private void clearSavedAnchor() {
        pendingAnchor = null;
        savedAnchorPosition = null;
        savedAnchorState = null;
        entityData.set(LEGACY_ATTACHMENT, false);
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

    public float hitDamage(LivingEntity target, float amount) {
        if (kind() != Kind.HOLY) return amount;
        return CreatureAlignment.undead(target) || CreatureAlignment.demonic(target) ? (int) (amount * 1.5D) : amount;
    }

    @Override
    protected void doPostHurtEffects(LivingEntity target) {
        target.invulnerableTime = 0;
    }

    public void applyLegacyPunch(LivingEntity target) {
        int strength = getKnockback();
        if (strength <= 0) return;
        Vec3 motion = getDeltaMovement();
        float horizontalSpeed = (float) motion.horizontalDistance();
        if (horizontalSpeed > 0) {
            double force = strength * (double) 0.6F;
            target.push(motion.x * force / horizontalSpeed, 0.1D, motion.z * force / horizontalSpeed);
        }
    }

    public void completeAcceptedEndermanHit(LivingEntity target) {
        if (!level().isClientSide && getPierceLevel() <= 0) target.setArrowCount(target.getArrowCount() + 1);
        applyLegacyPunch(target);
        if (!level().isClientSide && getOwner() instanceof LivingEntity shooter) {
            EnchantmentHelper.doPostHurtEffects(target, shooter);
            EnchantmentHelper.doPostDamageEffects(shooter, target);
        }
        doPostHurtEffects(target);
        playSound(getHitGroundSoundEvent(), 1, 1.2F / (random.nextFloat() * 0.2F + 0.9F));
    }

    public void applyBowEnchantments(ItemStack bow) {
        int power = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.POWER_ARROWS, bow);
        if (power > 0) setBaseDamage(getBaseDamage() + 0.5 * power + 0.5);
        setKnockback(EnchantmentHelper.getItemEnchantmentLevel(Enchantments.PUNCH_ARROWS, bow));
        if (EnchantmentHelper.getItemEnchantmentLevel(Enchantments.FLAMING_ARROWS, bow) > 0) setSecondsOnFire(100);
    }
}
