package art.arcane.witchery.entity;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.vampirism.BloodFeeding;
import art.arcane.witchery.vampirism.ParalysisEffect;
import art.arcane.witchery.vampirism.VampireVillages;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.EnumSet;

public final class VillageGuard extends PathfinderMob implements RangedAttackMob {
    private static final EntityDataAccessor<Byte> GUARD_TYPE = SynchedEntityData.defineId(VillageGuard.class, EntityDataSerializers.BYTE);
    private static final EntityDimensions TALL_SIZE = EntityDimensions.scalable(0.72F, 2.34F);
    private static final ResourceLocation WITCH_HUNTER = ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "witchhunter");
    private static final int TRUSTED_REPUTATION = 10;
    private int blood = BloodFeeding.MAX_MORTAL_BLOOD;
    private int patrolCheck;
    @Nullable private VampireVillages.Territory village;

    public VillageGuard(EntityType<? extends VillageGuard> type, Level level) {
        super(type, level);
        xpReward = 5;
        if (getNavigation() instanceof GroundPathNavigation ground) ground.setCanOpenDoors(true);
    }

    public static AttributeSupplier.Builder attributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 40).add(Attributes.FOLLOW_RANGE, 40)
                .add(Attributes.MOVEMENT_SPEED, 0.3).add(Attributes.ATTACK_DAMAGE, 4);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FollowVampireGoal());
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(4, new RangedAttackGoal(this, 1, 20, 60, 15) {
            @Override public boolean canUse() { return hasBow() && canFight() && super.canUse(); }
            @Override public boolean canContinueToUse() { return hasBow() && canFight() && super.canContinueToUse(); }
            @Override public void start() { super.start(); setAggressive(true); }
            @Override public void stop() { super.stop(); setAggressive(false); }
        });
        goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.2, false) {
            @Override public boolean canUse() { return !hasBow() && canFight() && super.canUse(); }
            @Override public boolean canContinueToUse() { return !hasBow() && canFight() && super.canContinueToUse(); }
        });
        goalSelector.addGoal(6, new MoveThroughVillageGoal(this, 0.6, true, 4, () -> true));
        goalSelector.addGoal(7, new MoveTowardsRestrictionGoal(this, 1));
        goalSelector.addGoal(9, new OpenDoorGoal(this, true));
        goalSelector.addGoal(10, new WaterAvoidingRandomStrollGoal(this, 1));
        goalSelector.addGoal(11, new LookAtPlayerGoal(this, Player.class, 8));
        goalSelector.addGoal(12, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new DefendVillageGoal());
        targetSelector.addGoal(2, new HurtByTargetGoal(this, VillageGuard.class).setAlertOthers());
        targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 10, true, true,
                entity -> entity instanceof Enemy));
    }

    private boolean hasBow() {
        return getMainHandItem().getItem() instanceof BowItem;
    }

    private boolean canFight() {
        return !BloodFeeding.paralysis(this, 4);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType type,
                                        @Nullable SpawnGroupData group, @Nullable CompoundTag tag) {
        SpawnGroupData data = super.finalizeSpawn(level, difficulty, type, group, tag);
        setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
        setItemSlot(EquipmentSlot.FEET, new ItemStack(Items.LEATHER_BOOTS));
        setItemSlot(EquipmentSlot.LEGS, new ItemStack(Items.LEATHER_LEGGINGS));
        setItemSlot(EquipmentSlot.CHEST, new ItemStack(random.nextInt(5) == 0 ? Items.IRON_CHESTPLATE : Items.LEATHER_CHESTPLATE));
        setItemSlot(EquipmentSlot.HEAD, new ItemStack(random.nextInt(5) == 0 ? Items.IRON_HELMET : Items.LEATHER_HELMET));
        setPersistenceRequired();
        return data;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (--patrolCheck > 0 || !(level() instanceof ServerLevel world)) return;
        patrolCheck = 70 + random.nextInt(50);
        village = VampireVillages.territory(world, blockPosition()).orElse(null);
        if (village == null) {
            clearRestriction();
        } else {
            restrictTo(village.center(), village.radius() * 3 / 2);
            if (getTarget() == null) {
                heal(1);
                if (random.nextInt(4) == 0) setBlood(blood + 1);
            }
        }
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return !(target instanceof Creeper || target instanceof Villager) && !isVillageDefender(target) && super.canAttack(target);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return !isVillageDefender(source.getEntity()) && super.hurt(source, amount);
    }

    public static boolean isVillageDefender(@Nullable Entity entity) {
        return entity instanceof VillageGuard || entity != null && WITCH_HUNTER.equals(BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()));
    }

    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        if (!canFight()) return;
        var arrow = ProjectileUtil.getMobArrow(this, new ItemStack(Items.ARROW), power);
        if (getGuardType() == 1) arrow.setSecondsOnFire(5);
        double dx = target.getX() - getX();
        double dz = target.getZ() - getZ();
        double dy = target.getY(1 / 3.0) - arrow.getY() + Math.sqrt(dx * dx + dz * dz) * 0.2;
        arrow.shoot(dx, dy, dz, 1.6F, 16 - level().getDifficulty().getId() * 4);
        playSound(SoundEvents.ARROW_SHOOT, 1, 1 / (random.nextFloat() * 0.4F + 0.8F));
        level().addFreshEntity(arrow);
    }

    public int getBlood() {
        return blood;
    }

    public void setBlood(int amount) {
        blood = Mth.clamp(amount, 0, BloodFeeding.MAX_MORTAL_BLOOD);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(GUARD_TYPE, (byte) 0);
    }

    public int getGuardType() {
        return entityData.get(GUARD_TYPE);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (GUARD_TYPE.equals(key)) refreshDimensions();
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return getGuardType() == 1 ? TALL_SIZE : super.getDimensions(pose);
    }

    @Override
    public boolean fireImmune() {
        return getGuardType() == 1 || super.fireImmune();
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(GUARD_TYPE, (byte) Mth.clamp(tag.getByte("GuardType"), 0, 1));
        if (tag.contains("BloodLevel", Tag.TAG_ANY_NUMERIC)) setBlood(tag.getInt("BloodLevel"));
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putByte("GuardType", (byte) getGuardType());
        tag.putInt("BloodLevel", blood);
    }

    @Override protected SoundEvent getAmbientSound() { return SoundEvents.VILLAGER_AMBIENT; }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.VILLAGER_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.VILLAGER_DEATH; }

    private final class DefendVillageGoal extends TargetGoal {
        private final TargetingConditions threats = TargetingConditions.forCombat().ignoreLineOfSight();
        private long nextScan;

        private DefendVillageGoal() {
            super(VillageGuard.this, false, true);
            setFlags(EnumSet.of(Flag.TARGET));
        }

        @Override
        public boolean canUse() {
            if (village == null || !canFight() || !(level() instanceof ServerLevel world)) return false;
            long now = world.getGameTime();
            if (now < nextScan) return false;
            nextScan = now + 5;
            threats.range(getFollowDistance());
            targetMob = VampireVillages.recentAggressors(world, village)
                    .filter(target -> threats.test(VillageGuard.this, target))
                    .min(Comparator.comparingDouble(VillageGuard.this::distanceToSqr)).orElse(null);
            if (canAttack(targetMob, threats)) return true;
            if (getRandom().nextInt(20) != 0) return false;

            var residents = world.getEntitiesOfClass(Villager.class, village.residents(), Villager::isAlive);
            targetMob = world.getPlayers(player -> threats.test(VillageGuard.this, player)).stream()
                    .filter(player -> residents.stream().anyMatch(resident -> VampireVillages.isHostile(world, resident, player)
                            || player.getLastHurtByMob() instanceof Player ally
                            && VampireVillages.reputation(world, resident, ally) >= TRUSTED_REPUTATION))
                    .min(Comparator.comparingDouble(VillageGuard.this::distanceToSqr)).orElse(null);
            return canAttack(targetMob, threats);
        }

        @Override
        public boolean canContinueToUse() {
            return village != null && canFight() && super.canContinueToUse();
        }

        @Override
        public void start() {
            setTarget(targetMob);
            super.start();
        }
    }

    private final class FollowVampireGoal extends Goal {
        private FollowVampireGoal() {
            setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return BloodFeeding.paralysis(VillageGuard.this, 5);
        }

        @Override
        public void start() {
            setTarget(null);
            getNavigation().stop();
        }

        @Override
        public void stop() {
            getNavigation().stop();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            if (tickCount % 10 != 0 || !(level() instanceof ServerLevel world)) return;
            var vampire = ParalysisEffect.lureTarget(VillageGuard.this, world).orElse(null);
            if (vampire == null || distanceToSqr(vampire) <= 1) {
                getNavigation().stop();
            } else {
                getNavigation().moveTo(vampire, 0.8);
                getLookControl().setLookAt(vampire);
            }
        }
    }
}
