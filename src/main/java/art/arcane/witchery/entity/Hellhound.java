package art.arcane.witchery.entity;

import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.ForgeEventFactory;

public final class Hellhound extends Monster {
    private static final int INNATE_ARMOR = 2;
    private static final int CONVERSION_COMPLETE_EVENT = 1027;
    private static final EntityDataAccessor<Boolean> CONVERTING = SynchedEntityData.defineId(Hellhound.class, EntityDataSerializers.BOOLEAN);
    private int conversionTime = -1;
    private boolean wetFur;
    private boolean shaking;
    private float shakeAge;
    private float previousShakeAge;

    public Hellhound(EntityType<? extends Hellhound> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return createMonsterAttributes().add(Attributes.MAX_HEALTH, 20).add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.ATTACK_DAMAGE, 4);
    }

    @Override
    public boolean checkSpawnRules(LevelAccessor level, MobSpawnType reason) {
        // The original spawn rule uses difficulty and clearance at every light level.
        return level.getDifficulty() != Difficulty.PEACEFUL;
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new LeapAtTargetGoal(this, 0.4F));
        goalSelector.addGoal(3, new MeleeAttackGoal(this, 1, true));
        goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1));
        goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8));
        goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this).setAlertOthers());
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Sheep.class, 0, true, true, null));
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, 0, true, true,
                target -> distanceToSqr(target) < 25));
        targetSelector.addGoal(4, new NearestAttackableTargetGoal<>(this, WitcheryFollower.class, 0, true, true,
                target -> distanceToSqr(target) < 25));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(CONVERTING, false);
    }

    public boolean isConverting() {
        return entityData.get(CONVERTING);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        var stack = player.getItemInHand(hand);
        if (isConverting() || !stack.is(Items.GOLDEN_APPLE) || !hasEffect(MobEffects.WEAKNESS)) {
            return super.mobInteract(player, hand);
        }
        if (!level().isClientSide) {
            if (!player.getAbilities().instabuild) stack.shrink(1);
            startConversion(3600 + random.nextInt(1000));
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    private void startConversion(int ticks) {
        conversionTime = ticks;
        entityData.set(CONVERTING, true);
        removeEffect(MobEffects.WEAKNESS);
        addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, ticks));
    }

    @Override
    public void tick() {
        if (isAlive() && level() instanceof ServerLevel level && isConverting()) {
            conversionTime -= conversionProgress();
            if (conversionTime <= 0 && ForgeEventFactory.canLivingConvert(this, EntityType.WOLF, ticks -> conversionTime = ticks)) {
                var wolf = convertTo(EntityType.WOLF, false);
                if (wolf != null) {
                    wolf.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 200));
                    ForgeEventFactory.onLivingConvert(this, wolf);
                    if (!isSilent()) level.levelEvent(CONVERSION_COMPLETE_EVENT, legacyPosition(), 0);
                    return;
                }
            }
        }
        super.tick();
        if (!isAlive()) return;
        tickWetFur();
        if (level().isClientSide && tickCount % 2 == 0) {
            var effectsRandom = level().getRandom();
            double width = getBbWidth() * 0.7;
            double x = getX() + (effectsRandom.nextDouble() - 0.5) * width;
            double y = getY() + 0.5 + effectsRandom.nextDouble() * (getBbHeight() - 0.1);
            double z = getZ() + (effectsRandom.nextDouble() - 0.5) * width;
            level().addParticle(ParticleTypes.FLAME, x, y, z, 0, 0, 0);
        }
    }

    private void tickWetFur() {
        if (isInWaterOrRain()) {
            wetFur = true;
            shaking = false;
            shakeAge = previousShakeAge = 0;
            return;
        }
        if (!level().isClientSide && wetFur && !shaking && onGround() && !isPathFinding()) {
            shaking = true;
            level().broadcastEntityEvent(this, (byte) 8);
            playSound(SoundEvents.WOLF_SHAKE, getSoundVolume(), (random.nextFloat() - random.nextFloat()) * 0.2F + 1);
        }
        if (!shaking) return;
        previousShakeAge = shakeAge;
        shakeAge += 0.05F;
        if (previousShakeAge >= 2) {
            wetFur = shaking = false;
            shakeAge = previousShakeAge = 0;
        }
        if (level().isClientSide && shakeAge > 0.4F) {
            int count = (int) (Mth.sin((shakeAge - 0.4F) * Mth.PI) * 7);
            var motion = getDeltaMovement();
            for (int particle = 0; particle < count; particle++) {
                double x = getX() + (random.nextFloat() * 2 - 1) * getBbWidth() * 0.5;
                double z = getZ() + (random.nextFloat() * 2 - 1) * getBbWidth() * 0.5;
                level().addParticle(ParticleTypes.SPLASH, x, getY() + 0.8, z, motion.x, motion.y, motion.z);
            }
        }
    }

    public float shakeAngle(float partialTick, float offset) {
        float phase = Mth.clamp((Mth.lerp(partialTick, previousShakeAge, shakeAge) + offset) / 1.8F, 0, 1) * Mth.PI;
        return Mth.sin(phase) * Mth.sin(phase * 11) * 0.15F * Mth.PI;
    }

    @Override
    public void handleEntityEvent(byte event) {
        if (event == 8) {
            shaking = true;
            shakeAge = previousShakeAge = 0;
        } else super.handleEntityEvent(event);
    }

    private int conversionProgress() {
        if (random.nextFloat() >= 0.01F) return 1;
        BlockPos origin = legacyPosition();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int progress = 1;
        int accelerators = 0;
        for (int x = -4; x < 4; x++) {
            for (int y = -4; y < 4; y++) {
                for (int z = -4; z < 4; z++) {
                    BlockState state = level().getBlockState(pos.setWithOffset(origin, x, y, z));
                    if (state.is(Blocks.IRON_BARS) || state.is(BlockTags.BEDS)) {
                        if (random.nextFloat() < 0.3F) progress++;
                        if (++accelerators == 14) return progress;
                    }
                }
            }
        }
        return progress;
    }

    private BlockPos legacyPosition() {
        return new BlockPos((int) getX(), (int) getY(), (int) getZ());
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return !isConverting() && super.removeWhenFarAway(distance);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        Entity attacker = source.getEntity();
        if (attacker != null && !(attacker instanceof Player) && !(attacker instanceof AbstractArrow)) amount = (amount + 1) / 2;
        return super.hurt(source, amount);
    }

    @Override
    protected float getDamageAfterArmorAbsorb(DamageSource source, float damage) {
        float remaining = super.getDamageAfterArmorAbsorb(source, damage);
        return source.is(DamageTypeTags.BYPASSES_ARMOR) ? remaining : remaining * (25 - INNATE_ARMOR) / 25;
    }

    @Override
    public boolean doHurtTarget(Entity victim) {
        if (!super.doHurtTarget(victim)) return false;
        int difficulty = level().getDifficulty().getId();
        if (random.nextFloat() < difficulty * 0.1F) victim.setSecondsOnFire(2 * difficulty);
        return true;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean playerKill) {
        super.dropCustomDeathLoot(source, looting, playerKill);
        spawnAtLocation(WitcheryIngredient.DOG_TONGUE.item());
        if (level().getRandom().nextInt(12) <= Math.min(looting, 3)) spawnAtLocation(WitcheryItems.getRequired("hellhound_head").get());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("InnateArmor", true);
        tag.putInt("ConversionTime", isConverting() ? Math.max(0, conversionTime) : -1);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        var armor = getAttribute(Attributes.ARMOR);
        if (!tag.getBoolean("InnateArmor") && armor.getBaseValue() == INNATE_ARMOR) armor.setBaseValue(0);
        int remaining = tag.contains("ConversionTime", Tag.TAG_ANY_NUMERIC) ? tag.getInt("ConversionTime") : -1;
        if (remaining >= 0) {
            startConversion(remaining);
        } else {
            conversionTime = -1;
            entityData.set(CONVERTING, false);
        }
    }

    @Override
    protected SoundEvent getAmbientSound() { return SoundEvents.WOLF_GROWL; }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.WOLF_HURT; }

    @Override
    protected SoundEvent getDeathSound() { return SoundEvents.WOLF_DEATH; }

    @Override
    protected float getSoundVolume() { return 0.4F; }

    @Override
    protected float getStandingEyeHeight(Pose pose, EntityDimensions dimensions) { return dimensions.height * 0.8F; }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) { playSound(SoundEvents.WOLF_STEP, 0.15F, 1); }
}
