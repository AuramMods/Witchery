package art.arcane.witchery.entity;

import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.registry.WitcheryDamageTypes;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcheryItems;
import art.arcane.witchery.registry.WitcherySounds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.UUID;
import java.util.function.Predicate;

public final class HobgoblinGod extends Monster implements RangedAttackMob {
    private static final int ENTRANCE_TICKS = 150;
    private final ServerBossEvent bossBar = new ServerBossEvent(getDisplayName(), BossEvent.BossBarColor.PURPLE,
            BossEvent.BossBarOverlay.PROGRESS);
    private int entranceTicks;
    private int nextTeleportTick;
    private int attackTicks;
    @Nullable
    private UUID patronId;

    public HobgoblinGod(EntityType<? extends HobgoblinGod> type, Level level) {
        super(type, level);
        xpReward = 35;
        setPersistenceRequired();
        nextTeleportTick = teleportInterval();
    }

    public static AttributeSupplier.Builder attributes(boolean mog) {
        return createMonsterAttributes().add(Attributes.MAX_HEALTH, 400).add(Attributes.MOVEMENT_SPEED, 0.35)
                .add(Attributes.FOLLOW_RANGE, 50).add(Attributes.KNOCKBACK_RESISTANCE, mog ? 0 : 1)
                .add(Attributes.ARMOR, mog ? 5 : 8);
    }

    public boolean isMog() {
        return getType() == WitcheryEntities.MOG.get();
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        if (isMog()) {
            goalSelector.addGoal(4, new RangedAttackGoal(this, 1, 40, 80, 30));
        } else {
            goalSelector.addGoal(2, new MeleeAttackGoal(this, 1, true));
            goalSelector.addGoal(3, new FollowMogGoal());
            goalSelector.addGoal(4, new MoveTowardsTargetGoal(this, 1, 48));
        }
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this, HobgoblinGod.class, Hobgoblin.class));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 0, true, false, null));
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
                                        @Nullable SpawnGroupData group, @Nullable CompoundTag tag) {
        if (isMog() && getMainHandItem().isEmpty()) setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
        return super.finalizeSpawn(level, difficulty, reason, group, tag);
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return !(target instanceof Hobgoblin || target instanceof HobgoblinGod) && super.canAttack(target);
    }

    @Override
    public void setTarget(@Nullable LivingEntity target) {
        if (target instanceof Hobgoblin || target instanceof HobgoblinGod) return;
        super.setTarget(target);
    }

    public void assignPatron(ServerPlayer patron) {
        patronId = patron.getUUID();
        setTarget(patron);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (attackTicks > 0) attackTicks--;
        if (!(level() instanceof ServerLevel level) || !isAlive()) return;
        bossBar.setName(getDisplayName());
        bossBar.setProgress(getHealth() / getMaxHealth());
        if (hasActiveEntrance()) {
            if (--entranceTicks == 0) {
                setNoAi(false);
                level.globalLevelEvent(1023, blockPosition(), 0);
            }
            if (tickCount % 10 == 0) heal(20);
            return;
        }
        if (isNoAi()) return;
        if (tickCount % 20 == 0) heal(isMog() ? 1 : 2);
        double followRange = getAttributeValue(Attributes.FOLLOW_RANGE);
        if (getTarget() == null && patronId != null && level.getPlayerByUUID(patronId) instanceof ServerPlayer target
                && distanceToSqr(target) <= followRange * followRange && canAttack(target)) {
            setTarget(target);
        }
        LivingEntity target = getTarget();
        if (target != null && tickCount >= nextTeleportTick && getNavigation().isDone()) {
            nextTeleportTick = tickCount + teleportInterval();
            BossTeleport.toward(level, this, target, 16);
        }
        if (isMog() && getMainHandItem().isEmpty() && random.nextInt(100) == 0) {
            setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
            WitcheryNetwork.sendMogNewBow(level, position());
        }
    }

    private int teleportInterval() {
        return isMog() ? 301 : 201;
    }

    @Nullable
    private HobgoblinGod nearestPartner(double radius, Predicate<HobgoblinGod> eligible) {
        HobgoblinGod nearest = null;
        double best = radius * radius;
        for (HobgoblinGod candidate : level().getEntitiesOfClass(HobgoblinGod.class, getBoundingBox().inflate(radius),
                god -> god.getType() != getType() && eligible.test(god))) {
            double distance = distanceToSqr(candidate);
            if (distance <= best) {
                nearest = candidate;
                best = distance;
            }
        }
        return nearest;
    }

    private double partnerDistanceSquared() {
        // The partner's death animation still contributes to the encounter.
        HobgoblinGod partner = nearestPartner(16, god -> !god.isRemoved());
        return partner == null ? Double.POSITIVE_INFINITY : distanceToSqr(partner);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(WitcheryDamageTypes.SPECTRAL_TOUCH)) {
            return super.hurt(source, WitcheryDamageTypes.capBossDamage(source, amount, 15));
        }
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return super.hurt(source, amount);
        double distance = partnerDistanceSquared();
        if (distance <= 9) return false;
        float scale = distance <= 36 ? 0.2F : distance <= 81 ? 0.5F : distance <= 256 ? 0.8F : 1;
        return super.hurt(source, Math.min(amount * scale, 15));
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        double distance = isMog() ? Double.POSITIVE_INFINITY : partnerDistanceSquared();
        int force = isMog() ? 15 : distance <= 9 ? 20 : distance <= 36 ? 15 : distance <= 81 ? 10 : distance <= 256 ? 6 : 4;
        double lift = isMog() ? 0.4 : 0.5 + (distance <= 9 ? 1 : distance <= 36 ? 0.8 : distance <= 81 ? 0.5 : distance <= 256 ? 0.2 : 0);
        attackTicks = 10;
        level().broadcastEntityEvent(this, (byte) 4);
        playSound(SoundEvents.IRON_GOLEM_ATTACK, 1, 1);
        boolean hit = target.hurt(damageSources().mobAttack(this), (isMog() ? 7 : 6) + random.nextInt(force));
        if (hit) {
            target.push(0, lift, 0);
            target.hurtMarked = true;
        }
        return hit;
    }

    @Override
    public void performRangedAttack(LivingEntity target, float strength) {
        if (!isMog() || getMainHandItem().isEmpty()) return;
        double dx = target.getX() - getX();
        double dz = target.getZ() - getZ();
        double distance = Math.hypot(dx, dz);
        if (distance < 1.0E-7) return;

        Arrow arrow = new Arrow(level(), this);
        arrow.setPos(getX() + dx / distance, arrow.getY(), getZ() + dz / distance);
        double dy = target.getBoundingBox().minY + target.getBbHeight() / 3.0F - arrow.getY();
        int difficulty = level().getDifficulty().getId();
        arrow.shoot(dx, dy + (float) distance * 0.2F, dz, 1.6F, 14 - difficulty * 4);
        // The original arrow checks the target's jump or knockback impulse.
        arrow.setDeltaMovement(arrow.getDeltaMovement().scale(target.hasImpulse ? 2.5 : 1.5));
        arrow.setBaseDamage(strength * 8 + random.nextGaussian() * 0.25 + difficulty * 0.11F);
        playSound(SoundEvents.ARROW_SHOOT, 1, 1 / (random.nextFloat() * 0.4F + 0.8F));
        level().addFreshEntity(arrow);
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean playerKill) {
        super.dropCustomDeathLoot(source, looting, playerKill);
        int nuggets = random.nextInt(3) + 1;
        ItemStack armor = new ItemStack(switch (random.nextInt(4)) {
            case 0 -> Items.CHAINMAIL_BOOTS;
            case 1 -> Items.CHAINMAIL_LEGGINGS;
            case 2 -> Items.CHAINMAIL_CHESTPLATE;
            default -> Items.CHAINMAIL_HELMET;
        });
        var worldRandom = level().getRandom();
        ItemStack enchantedArmor = EnchantmentHelper.enchantItem(worldRandom, armor, 30, false);
        // Vanilla's item entities draw from the world random source for their initial motion.
        boolean uniqueReward = worldRandom.nextInt(2) == 0;
        spawnAtLocation(new ItemStack(WitcheryIngredient.KOBOLDITE_NUGGET.item(), nuggets));
        spawnAtLocation(enchantedArmor);
        if (uniqueReward) {
            spawnAtLocation(WitcheryItems.getRequired(isMog() ? "quiverofmog" : "gurdleofgulg").get());
        }
    }

    @Override
    @Nullable
    public ItemEntity spawnAtLocation(ItemStack stack, float height) {
        ItemEntity drop = super.spawnAtLocation(stack, height);
        if (drop != null && isMog() && stack.is(Items.BOW)) {
            drop.setPickUpDelay(100);
            drop.lifespan = 100;
        }
        return drop;
    }

    public int attackTicks() {
        return attackTicks;
    }

    public boolean hasActiveEntrance() { return entranceTicks > 0; }

    @Override
    public void handleEntityEvent(byte event) {
        if (event == 4) attackTicks = 10;
        else super.handleEntityEvent(event);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return isMog() ? WitcherySounds.MOG_IDLE.get() : WitcherySounds.GULG_IDLE.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ZOMBIE_HORSE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WITHER_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        playSound(SoundEvents.IRON_GOLEM_STEP, 1, 1);
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        bossBar.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossBar.removePlayer(player);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Invul", entranceTicks);
        tag.putInt("TeleportDelay", Math.max(0, nextTeleportTick - tickCount));
        if (patronId != null) tag.putUUID("WitcheryTarget", patronId);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entranceTicks = Mth.clamp(tag.getInt("Invul"), 0, ENTRANCE_TICKS);
        if (hasActiveEntrance()) setNoAi(true);
        int remaining = tag.contains("TeleportDelay") ? Mth.clamp(tag.getInt("TeleportDelay"), 0, teleportInterval()) : teleportInterval();
        nextTeleportTick = tickCount + remaining;
        patronId = tag.hasUUID("WitcheryTarget") ? tag.getUUID("WitcheryTarget") : null;
    }

    private final class FollowMogGoal extends Goal {
        private static final int MAX_DISTANCE = 64;
        private static final int MIN_DISTANCE = 6;
        @Nullable
        private HobgoblinGod partner;
        @Nullable
        private Vec3 destination;

        private FollowMogGoal() {
            setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (level().getRandom().nextInt(20) != 0) return false;
            partner = nearestPartner(MAX_DISTANCE, god -> !god.isRemoved());
            if (partner == null || distanceToSqr(partner) < MIN_DISTANCE * MIN_DISTANCE) return false;
            destination = DefaultRandomPos.getPosTowards(HobgoblinGod.this, 16, 7, partner.position(), Math.PI / 2);
            return destination != null;
        }

        @Override
        public boolean canContinueToUse() {
            return !getNavigation().isDone() && partner != null && partner.isAlive()
                    && distanceToSqr(partner) < MAX_DISTANCE * MAX_DISTANCE;
        }

        @Override
        public void start() {
            if (destination != null) getNavigation().moveTo(destination.x, destination.y, destination.z, 1);
        }

        @Override
        public void stop() {
            partner = null;
            destination = null;
        }
    }
}
