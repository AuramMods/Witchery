package art.arcane.witchery.entity;

import art.arcane.witchery.lycanthropy.Lycanthropy;

import art.arcane.witchery.infusion.InfernalInfusion;
import art.arcane.witchery.infusion.InfusionEnergy;
import art.arcane.witchery.item.HandBowItem;
import art.arcane.witchery.item.VampiricPoppetItem;
import art.arcane.witchery.item.VoodooPoppetItem;
import art.arcane.witchery.registry.WitcheryItems;
import art.arcane.witchery.vampirism.BloodFeeding;
import art.arcane.witchery.vampirism.Vampirism;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class WitchHunter extends Monster implements RangedAttackMob {
    private static final EntityDataAccessor<Byte> TYPE = SynchedEntityData.defineId(WitchHunter.class, EntityDataSerializers.BYTE);
    private String targetName = "";
    @Nullable private UUID targetUuid;

    public WitchHunter(EntityType<? extends WitchHunter> type, Level level) {
        super(type, level);
        xpReward = 5;
    }

    public static AttributeSupplier.Builder attributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 30).add(Attributes.MOVEMENT_SPEED, 0.25)
                .add(Attributes.ATTACK_DAMAGE, 2);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(4, new RangedAttackGoal(this, 1, 20, 60, 15) {
            @Override public boolean canUse() { return hasCrossbow() && canFight() && super.canUse(); }
            @Override public boolean canContinueToUse() { return hasCrossbow() && canFight() && super.canContinueToUse(); }
            @Override public void start() { super.start(); setAggressive(true); }
            @Override public void stop() { super.stop(); setAggressive(false); }
        });
        goalSelector.addGoal(4, new MeleeAttackGoal(this, 1.2, false) {
            @Override public boolean canUse() { return !hasCrossbow() && canFight() && super.canUse(); }
            @Override public boolean canContinueToUse() { return !hasCrossbow() && canFight() && super.canContinueToUse(); }
        });
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8));
        goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this, VillageGuard.class, WitchHunter.class));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, LivingEntity.class, 0, false, true, this::hunts));
    }

    private boolean hunts(LivingEntity entity) {
        if (entity instanceof Player player && (targetUuid == null ? targetName.equals(player.getGameProfile().getName())
                : targetUuid.equals(player.getUUID()))) return true;
        if (Lycanthropy.isShifted(entity) || CreatureAlignment.undead(entity) || CreatureAlignment.demonic(entity)
                || entity instanceof Witch || entity instanceof BabaYaga) return true;
        return entity instanceof ServerPlayer player && (InfusionEnergy.type(player) == InfernalInfusion.ID
                || player.getInventory().items.stream().anyMatch(WitchHunter::hasDarkPoppet) || hasDarkPoppet(player.getOffhandItem()));
    }

    private static boolean hasDarkPoppet(ItemStack stack) {
        return stack.getItem() instanceof VoodooPoppetItem || stack.getItem() instanceof VampiricPoppetItem;
    }

    private boolean canFight() {
        return !BloodFeeding.paralysis(this, 4);
    }

    public boolean hasCrossbow() {
        return getMainHandItem().getItem() instanceof HandBowItem;
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return !(target instanceof Villager) && !VillageGuard.isVillageDefender(target) && super.canAttack(target);
    }

    public void pursue(ServerPlayer player) {
        targetName = player.getGameProfile().getName();
        targetUuid = player.getUUID();
        setTarget(player);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
                                        @Nullable SpawnGroupData data, @Nullable CompoundTag tag) {
        SpawnGroupData group = super.finalizeSpawn(level, difficulty, reason, data, tag);
        entityData.set(TYPE, (byte) random.nextInt(3));
        setItemSlot(EquipmentSlot.MAINHAND, WitcheryItems.getRequired("handbow").get().getDefaultInstance());
        populateDefaultEquipmentEnchantments(level.getRandom(), difficulty);
        return group;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        if (tickCount % 20 == 2) removeEffect(MobEffects.POISON);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (VillageGuard.isVillageDefender(source.getEntity())) return false;
        return super.hurt(source, source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) ? amount : Math.min(amount, 9));
    }

    @Override
    public void performRangedAttack(LivingEntity target, float power) {
        if (!canFight() || !hasCrossbow()) return;
        WitcheryBolt.Kind kind = Lycanthropy.isShifted(target) ? WitcheryBolt.Kind.SILVER
                : CreatureAlignment.undead(target) ? WitcheryBolt.Kind.HOLY
                : random.nextInt(4) == 0 ? WitcheryBolt.Kind.POWERED_ANTI_MAGIC : WitcheryBolt.Kind.STAKE;
        var bolt = new WitcheryBolt(this, kind);
        int difficulty = level().getDifficulty().getId();
        bolt.setBaseDamage(Math.max(0, power * 2 + random.nextGaussian() * 0.25 + difficulty * 0.11));
        bolt.applyBowEnchantments(getMainHandItem());
        if (Vampirism.isVampire(target) && random.nextInt(3) == 0) bolt.setSecondsOnFire(100);
        double dx = target.getX() - getX();
        double dz = target.getZ() - getZ();
        double dy = target.getY(1 / 3.0) - bolt.getY() + Math.sqrt(dx * dx + dz * dz) * 0.2;
        bolt.shoot(dx, dy, dz, 1.6F, 14 - difficulty * 4);
        playSound(SoundEvents.ARROW_SHOOT, 1, 1 / (random.nextFloat() * 0.4F + 0.8F));
        level().addFreshEntity(bolt);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(TYPE, (byte) 0);
    }

    public int hunterType() {
        return entityData.get(TYPE);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(TYPE, (byte) Mth.clamp(tag.getByte("HunterType"), 0, 2));
        targetName = tag.getString("HunterTarget");
        targetUuid = tag.hasUUID("HunterTargetUUID") ? tag.getUUID("HunterTargetUUID") : null;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putByte("HunterType", (byte) hunterType());
        tag.putString("HunterTarget", targetName);
        if (targetUuid != null) tag.putUUID("HunterTargetUUID", targetUuid);
    }

    @Override protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.VILLAGER_HURT; }
    @Override protected SoundEvent getDeathSound() { return SoundEvents.VILLAGER_DEATH; }
}
