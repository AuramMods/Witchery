package art.arcane.witchery.entity;

import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryDamageTypes;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcheryItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class HornedHuntsman extends Monster implements RangedAttackMob {
    private static final int ENTRANCE_TICKS = 150;
    private final ServerBossEvent bossBar = new ServerBossEvent(getDisplayName(), BossEvent.BossBarColor.GREEN,
            BossEvent.BossBarOverlay.PROGRESS);
    private int entranceTicks;
    private int attackTicks;
    private int teleportCooldown;
    private boolean explosiveEntrance;
    private boolean playerCreated;

    public HornedHuntsman(EntityType<? extends HornedHuntsman> type, Level level) {
        super(type, level);
        xpReward = 70;
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder attributes() {
        return createMonsterAttributes().add(Attributes.MAX_HEALTH, 400).add(Attributes.MOVEMENT_SPEED, 0.35)
                .add(Attributes.FOLLOW_RANGE, 50).add(Attributes.KNOCKBACK_RESISTANCE, 1).add(Attributes.ARMOR, 4);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1, true));
        goalSelector.addGoal(3, new MoveTowardsTargetGoal(this, 1, 48));
        goalSelector.addGoal(4, new RangedAttackGoal(this, 1, 20, 60, 30));
        goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6));
        goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 0, true, false, null));
    }

    public static boolean summon(ServerPlayer caller) {
        ServerLevel world = caller.serverLevel();
        var huntsman = WitcheryEntities.HORNED_HUNTSMAN.get().create(world);
        if (huntsman == null) return false;
        BlockPos origin = caller.blockPosition();
        for (int i = 0; i < 8; i++) {
            var at = origin.offset(huntOffset(caller), 0, huntOffset(caller)).mutable();
            if (!world.hasChunkAt(at) || !world.getWorldBorder().isWithinBounds(at)) continue;
            while (!world.isEmptyBlock(at) && at.getY() < Math.min(origin.getY() + 8, world.getMaxBuildHeight() - 4)) {
                at.move(Direction.UP);
            }
            while (at.getY() > world.getMinBuildHeight() && world.isEmptyBlock(at.below())) at.move(Direction.DOWN);
            var floor = at.below();
            if (!world.getBlockState(floor).isFaceSturdy(world, floor, Direction.UP)) continue;
            huntsman.moveTo(at.getX() + 0.5, at.getY() + 0.05, at.getZ() + 0.5, 0, 0);
            if (!world.noCollision(huntsman) || world.containsAnyLiquid(huntsman.getBoundingBox())) continue;
            huntsman.beginEntrance(true);
            if (!world.addFreshEntity(huntsman)) return false;
            world.sendParticles(ParticleTypes.POOF, huntsman.getX(), huntsman.getY(0.5), huntsman.getZ(),
                    16, 0.5, 1, 0.5, 0.05);
            world.playSound(null, at, SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 1, 1);
            return true;
        }
        return false;
    }

    private static int huntOffset(Player caller) {
        int offset = caller.getRandom().nextInt(13) - 8;
        return offset > -2 ? offset + 4 : offset;
    }

    public static boolean summonFromWicker(ServerLevel level, double x, double y, double z, float yaw) {
        HornedHuntsman huntsman = WitcheryEntities.HORNED_HUNTSMAN.get().create(level);
        if (huntsman == null) return false;
        huntsman.moveTo(x, y, z, yaw, 0);
        huntsman.setYHeadRot(yaw);
        huntsman.setYBodyRot(yaw);
        huntsman.beginEntrance(false);
        return level.addFreshEntity(huntsman);
    }

    private void beginEntrance(boolean explosive) {
        entranceTicks = ENTRANCE_TICKS;
        explosiveEntrance = explosive;
        setHealth(getMaxHealth() / 4);
        setNoAi(true);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (attackTicks > 0) attackTicks--;
        if (!(level() instanceof ServerLevel world) || !isAlive()) return;
        bossBar.setName(getDisplayName());
        bossBar.setProgress(getHealth() / getMaxHealth());
        if (entranceTicks > 0) {
            if (entranceTicks % 10 == 0) heal(20);
            if (--entranceTicks == 0) {
                setNoAi(false);
                if (explosiveEntrance) world.explode(this, getX(), getEyeY(), getZ(), 6, false, Level.ExplosionInteraction.MOB);
                world.levelEvent(1023, blockPosition(), 0);
            }
            return;
        }
        if (isNoAi()) return;
        if (tickCount % 20 == 0) heal(1);
        if (teleportCooldown > 0) teleportCooldown--;
        LivingEntity target = getTarget();
        if (target == null) return;
        boolean visible = getSensing().hasLineOfSight(target);
        if (visible && tickCount % 20 == 0 && random.nextInt(5) == 0) {
            getLookControl().setLookAt(target, 30, 30);
            performRangedAttack(target, Mth.clamp((float) distanceToSqr(target) / 30, 0.1F, 1));
        }
        if (visible && distanceToSqr(target) <= 256 && tickCount % (200 + random.nextInt(4) * 100) == 0) {
            summonWolf(world, target);
        }
        if (getNavigation().isDone() && teleportCooldown == 0) {
            teleportCooldown = 200;
            Vec3 toward = target.getEyePosition().subtract(getEyePosition()).normalize().scale(8);
            Vec3 destination = position().add(toward).add(random.nextDouble() * 8 - 4, random.nextInt(16) - 8,
                    random.nextDouble() * 8 - 4);
            if (randomTeleport(destination.x, destination.y, destination.z, true)) {
                playSound(SoundEvents.ENDERMAN_TELEPORT, 1, 1);
            }
        }
    }

    private void summonWolf(ServerLevel world, LivingEntity target) {
        Wolf wolf = EntityType.WOLF.create(world);
        if (wolf == null) return;
        wolf.moveTo(getX() + random.nextDouble() - 0.5, getY(), getZ() + random.nextDouble() - 0.5, getYRot(), 0);
        wolf.setTarget(target);
        wolf.startPersistentAngerTimer();
        wolf.setPersistentAngerTarget(target.getUUID());
        wolf.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20_000, 1));
        if (world.addFreshEntity(wolf)) {
            world.sendParticles(ParticleTypes.INSTANT_EFFECT, wolf.getX(), wolf.getY(0.5), wolf.getZ(),
                    10, 0.5, 0.5, 0.5, 0.02);
            wolf.playSound(SoundEvents.FIRE_EXTINGUISH, 1, 1);
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        attackTicks = 10;
        level().broadcastEntityEvent(this, (byte) 4);
        playSound(SoundEvents.IRON_GOLEM_ATTACK, 1, 1);
        boolean hit = target.hurt(damageSources().mobAttack(this), 7 + random.nextInt(15));
        if (hit) target.push(0, 0.4, 0);
        return hit;
    }

    @Override
    public void performRangedAttack(LivingEntity target, float strength) {
        Arrow arrow = new Arrow(level(), this);
        double dx = target.getX() - getX();
        double dz = target.getZ() - getZ();
        double dy = target.getY(1.0 / 3) - arrow.getY();
        int difficulty = level().getDifficulty().getId();
        arrow.shoot(dx, dy + Math.sqrt(dx * dx + dz * dz) * 0.2, dz, 1.6F, 14 - difficulty * 4);
        arrow.setBaseDamage(strength * 8 + random.nextGaussian() * 0.25 + difficulty * 0.11);
        arrow.setKnockback(2);
        playSound(SoundEvents.ARROW_SHOOT, 1, 1 / (random.nextFloat() * 0.4F + 0.8F));
        level().addFreshEntity(arrow);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return super.hurt(source, WitcheryDamageTypes.capBossDamage(source, amount, 15));
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        spawnAtLocation(new ItemStack(Items.WITHER_SKELETON_SKULL, random.nextInt(3) == 0 ? 3 : 2));
        BuiltInRegistries.ENCHANTMENT.getRandom(random).ifPresent(holder -> {
            var enchantment = holder.value();
            int highest = enchantment.getMaxLevel();
            int level = random.nextIntBetweenInclusive(Math.min(enchantment.getMinLevel() + 2, highest), highest);
            spawnAtLocation(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantment, level)));
        });
        spawnAtLocation(new ItemStack(WitcheryIngredient.INFERNAL_BLOOD.item()));
        if (random.nextInt(4) == 0) spawnAtLocation(WitcheryItems.getRequired("huntsmanspear").get());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Invul", entranceTicks);
        tag.putBoolean("explosiveEntrance", explosiveEntrance);
        tag.putBoolean("PlayerCreated", playerCreated);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entranceTicks = Mth.clamp(tag.getInt("Invul"), 0, ENTRANCE_TICKS);
        explosiveEntrance = tag.getBoolean("explosiveEntrance");
        playerCreated = tag.getBoolean("PlayerCreated");
        if (entranceTicks > 0) setNoAi(true);
    }

    @Override
    public void handleEntityEvent(byte event) {
        if (event == 4) attackTicks = 10;
        else super.handleEntityEvent(event);
    }

    public int attackTicks() { return attackTicks; }

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
    protected SoundEvent getAmbientSound() { return SoundEvents.ENDER_DRAGON_GROWL; }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) { return SoundEvents.ZOMBIE_HORSE_HURT; }

    @Override
    protected SoundEvent getDeathSound() { return SoundEvents.WITHER_DEATH; }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) { playSound(SoundEvents.IRON_GOLEM_STEP, 1, 1); }
}
