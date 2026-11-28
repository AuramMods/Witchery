package art.arcane.witchery.entity;

import art.arcane.witchery.brewing.WakingNightmareEffect;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryItems;
import art.arcane.witchery.registry.WitcherySounds;
import art.arcane.witchery.world.DreamTravel;
import art.arcane.witchery.world.WitcheryDimensions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreakDoorGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;

import java.util.UUID;

public final class Nightmare extends Monster {
    private static final EntityDataAccessor<Boolean> DEFENDED =
            SynchedEntityData.defineId(Nightmare.class, EntityDataSerializers.BOOLEAN);
    private static final EquipmentSlot[] ARMOR_SLOTS = {
            EquipmentSlot.FEET, EquipmentSlot.LEGS, EquipmentSlot.CHEST, EquipmentSlot.HEAD
    };
    private static final byte SWIPE_EVENT = 4;
    public static final int SWIPE_TICKS = 15;
    private String victimName = "";
    private UUID victimId;
    private boolean foundVictim;
    private int defenseTicks;
    private int swipeTicks;

    public Nightmare(EntityType<? extends Nightmare> type, Level level) {
        super(type, level);
        xpReward = 25;
        ((GroundPathNavigation) getNavigation()).setCanOpenDoors(true);
    }

    public static AttributeSupplier.Builder attributes() {
        return createMonsterAttributes().add(Attributes.MAX_HEALTH, 100)
                .add(Attributes.MOVEMENT_SPEED, 0.35).add(Attributes.ATTACK_DAMAGE, 4)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(DEFENDED, false);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new BreakDoorGoal(this, difficulty -> difficulty == Difficulty.HARD));
        goalSelector.addGoal(3, new MeleeAttackGoal(this, 1, true));
        goalSelector.addGoal(4, new MoveTowardsTargetGoal(this, 0.9, 32));
        goalSelector.addGoal(5, new MoveTowardsRestrictionGoal(this, 1));
        goalSelector.addGoal(6, new MoveThroughVillageGoal(this, 1, false, 4, () -> true));
        goalSelector.addGoal(7, new RandomStrollGoal(this, 1));
        goalSelector.addGoal(8, new LookAtPlayerGoal(this, Player.class, 8));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new ReachableTargetGoal<>(this, Player.class, false,
                target -> !hasVictim() || target instanceof Player player && targets(player)));
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return super.canAttack(target) && (!hasVictim() || targets(target));
    }

    public void assignVictim(Player victim) {
        victimId = victim.getUUID();
        victimName = victim.getGameProfile().getName();
        foundVictim = true;
        setTarget(victim);
    }

    public boolean targets(LivingEntity victim) {
        return victimId != null ? victimId.equals(victim.getUUID())
                : victim instanceof Player player && victimName.equalsIgnoreCase(player.getGameProfile().getName());
    }

    private boolean hasVictim() {
        return victimId != null || !victimName.isEmpty();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (swipeTicks > 0) {
            --swipeTicks;
        }
        if (!(level() instanceof ServerLevel level) || !isAlive()) {
            return;
        }
        if (defenseTicks > 0 && --defenseTicks == 0) {
            entityData.set(DEFENDED, false);
        }
        if (hasVictim()) {
            Entity target = victimId == null ? level.players().stream().filter(this::targets).findFirst().orElse(null)
                    : level.getEntity(victimId);
            if (target instanceof Player victim) {
                foundVictim = true;
                if (!victim.isAlive() || distanceToSqr(victim) > 256 || !canAttack(victim)) {
                    disappear(level);
                    return;
                }
                if (getTarget() != victim) setTarget(victim);
            } else if (target instanceof LivingEntity) {
                // Earlier Forge saves could bind the Nightmare to the creature that received the brew.
                victimId = null;
                victimName = "";
                if (getTarget() == target) setTarget(null);
            } else {
                // Give an unloaded victim time to reconnect when the Nightmare is restored from disk.
                if (foundVictim || tickCount > 30) disappear(level);
                return;
            }
        }
        if (level.random.nextInt(5) == 0 && getTarget() instanceof ServerPlayer player
                && !DreamTravel.hasNightmare(player) && !WakingNightmareEffect.isHaunted(player)) {
            disappear(level);
        }
    }

    private void disappear(ServerLevel level) {
        WitcheryNetwork.sendBanishment(level, position());
        discard();
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        swipeTicks = SWIPE_TICKS;
        level().broadcastEntityEvent(this, SWIPE_EVENT);
        if (target instanceof ServerPlayer player) knockArmorOff(player);

        float damage = level().dimension().equals(WitcheryDimensions.DREAM)
                ? (float) getAttributeValue(Attributes.ATTACK_DAMAGE) : 0.5F;
        if (target instanceof LivingEntity living) {
            damage += EnchantmentHelper.getDamageBonus(getMainHandItem(), living.getMobType());
        }
        if (!target.hurt(damageSources().mobAttack(this), damage)) return false;
        int knockback = EnchantmentHelper.getKnockbackBonus(this);
        if (target instanceof LivingEntity && knockback > 0) {
            float heading = getYRot() * Mth.DEG_TO_RAD;
            target.push(-Mth.sin(heading) * knockback * 0.5F, 0.1, Mth.cos(heading) * knockback * 0.5F);
            setDeltaMovement(getDeltaMovement().multiply(0.6, 1, 0.6));
        }
        int fire = EnchantmentHelper.getFireAspect(this);
        if (fire > 0) target.setSecondsOnFire(fire * 4);
        setLastHurtMob(target);
        return true;
    }

    private void knockArmorOff(ServerPlayer player) {
        if (player.getInventory().items.stream().anyMatch(stack -> stack.is(WitcheryIngredient.CHARM_OF_DISRUPTED_DREAMS.item()))) {
            return;
        }
        ServerLevel level = player.serverLevel();
        EquipmentSlot slot = ARMOR_SLOTS[level.random.nextInt(ARMOR_SLOTS.length)];
        ItemStack armor = player.getItemBySlot(slot);
        if (armor.isEmpty()) return;
        float speed = level.random.nextFloat() * 0.5F;
        float angle = level.random.nextFloat() * Mth.TWO_PI;
        var drop = new ItemEntity(level, player.getX(), player.getEyeY() - 0.3F, player.getZ(), armor.copy(),
                -Mth.sin(angle) * speed, 0.2F, Mth.cos(angle) * speed);
        drop.setPickUpDelay(40);
        if (level.addFreshEntity(drop)) player.setItemSlot(slot, ItemStack.EMPTY);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isDefended()) {
            return false;
        }
        if (!level().isClientSide() && !level().getBlockState(blockPosition()).is(WitcheryBlocks.getRequired("spiritflowing").get())) {
            boolean spear = source.getEntity() instanceof LivingEntity attacker
                    && attacker.getMainHandItem().is(WitcheryItems.getRequired("huntsmanspear").get());
            defenseTicks = level().dimension().equals(WitcheryDimensions.DREAM) ? (spear ? 40 : 80) : (spear ? 30 : 40);
            entityData.set(DEFENDED, true);
        }
        return super.hurt(source, Math.min(amount, 15));
    }

    public boolean isDefended() {
        return entityData.get(DEFENDED);
    }

    public int swipeTicks() {
        return swipeTicks;
    }

    @Override
    public void handleEntityEvent(byte event) {
        if (event == SWIPE_EVENT) {
            swipeTicks = SWIPE_TICKS;
        } else {
            super.handleEntityEvent(event);
        }
    }

    @Override
    public void die(DamageSource source) {
        if (level().dimension().equals(WitcheryDimensions.DREAM)
                && source.getEntity() instanceof ServerPlayer player && targets(player)) {
            DreamTravel.markNightmareKilled(player);
        }
        super.die(source);
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        if (level().dimension().equals(WitcheryDimensions.DREAM)) {
            int roll = random.nextInt(Math.max(10 - looting, 5));
            int count = looting > 0 && roll == 0 ? 2 : 1;
            spawnAtLocation(new ItemStack(WitcheryIngredient.MELLIFLUOUS_HUNGER.item(), count));
        }
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return WitcherySounds.NIGHTMARE_LIVE.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return super.getAmbientSoundInterval() * 2;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return WitcherySounds.NIGHTMARE_DEAD.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return WitcherySounds.NIGHTMARE_HIT.get();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("Victim", victimName);
        if (victimId != null) {
            tag.putUUID("VictimUUID", victimId);
        }
        tag.putInt("DefenseTicks", defenseTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        victimName = tag.getString("Victim");
        victimId = tag.hasUUID("VictimUUID") ? tag.getUUID("VictimUUID") : null;
        defenseTicks = tag.getInt("DefenseTicks");
        entityData.set(DEFENDED, defenseTicks > 0);
    }
}
