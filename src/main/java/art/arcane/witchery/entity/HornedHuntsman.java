package art.arcane.witchery.entity;

import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.registry.WitcheryDamageTypes;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcheryItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
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
    private int lastTeleportTick;
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
        BlockPos origin = new BlockPos((int) caller.getX(), (int) caller.getY(), (int) caller.getZ());
        var at = origin.offset(huntOffset(world.random), 0, huntOffset(world.random)).mutable();
        if (!world.hasChunkAt(at) || !world.getWorldBorder().isWithinBounds(at)) return false;
        while (!world.isEmptyBlock(at) && at.getY() < Math.min(origin.getY() + 8, world.getMaxBuildHeight() - 4)) {
            at.move(Direction.UP);
        }
        while (at.getY() > world.getMinBuildHeight() && world.isEmptyBlock(at)) at.move(Direction.DOWN);
        at.move(Direction.UP);
        var floor = at.below();
        if (!world.getBlockState(floor).isFaceSturdy(world, floor, Direction.UP)) return false;
        var huntsman = WitcheryEntities.HORNED_HUNTSMAN.get().create(world);
        if (huntsman == null) return false;
        huntsman.moveTo(at.getX() + 0.5, at.getY() + 0.05, at.getZ() + 0.5, 0, 0);
        if (!world.noCollision(huntsman) || world.containsAnyLiquid(huntsman.getBoundingBox())) return false;
        huntsman.beginEntrance(true);
        if (!world.addFreshEntity(huntsman)) return false;
        WitcheryNetwork.sendHuntsmanArrival(world, huntsman.position(), huntsman.getBbHeight());
        return true;
    }

    private static int huntOffset(RandomSource random) {
        int offset = random.nextInt(13) - 8;
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
            if (--entranceTicks == 0) {
                setNoAi(false);
                if (explosiveEntrance) world.explode(this, getX(), getEyeY(), getZ(), 6, false, Level.ExplosionInteraction.MOB);
                world.globalLevelEvent(1023, blockPosition(), 0);
            }
            if (tickCount % 10 == 0) heal(20);
            return;
        }
        if (isNoAi()) return;
        if (tickCount % 20 == 0) heal(1);
        LivingEntity target = getTarget();
        if (tickCount % 20 == 0 && world.random.nextInt(5) == 0 && target != null && getSensing().hasLineOfSight(target)) {
            getLookControl().setLookAt(target, 30, 30);
            performRangedAttack(target, distanceTo(target) / 30.0F);
        }
        if (tickCount % (200 + world.random.nextInt(4) * 100) == 0 && target != null
                && distanceToSqr(target) <= 256 && getSensing().hasLineOfSight(target)) {
            summonWolf(world, target);
        }
        if (target != null && getNavigation().isDone() && tickCount - lastTeleportTick > 200) {
            lastTeleportTick = tickCount;
            Vec3 toward = new Vec3(target.getX() - getX(), target.getY() - target.getEyeHeight() - getY(0.5),
                    target.getZ() - getZ()).normalize().scale(8);
            Vec3 origin = position();
            Vec3 destination = position().add(toward).add(random.nextDouble() * 8 - 4, random.nextInt(16) - 8,
                    random.nextDouble() * 8 - 4);
            if (randomTeleport(destination.x, destination.y, destination.z, true)) {
                world.playSound(null, origin.x, origin.y, origin.z, SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 1, 1);
                playSound(SoundEvents.ENDERMAN_TELEPORT, 1, 1);
            }
        }
    }

    private void summonWolf(ServerLevel world, LivingEntity target) {
        double x = getX() - 0.5 + world.random.nextDouble();
        double z = getZ() - 0.5 + world.random.nextDouble();
        Wolf wolf = EntityType.WOLF.create(world);
        if (wolf == null) return;
        wolf.moveTo(x, getY(), z, yBodyRot, getXRot());
        wolf.setTarget(target);
        wolf.startPersistentAngerTimer();
        wolf.setPersistentAngerTarget(target.getUUID());
        wolf.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20_000, 1));
        if (world.addFreshEntity(wolf)) {
            WitcheryNetwork.sendHuntsmanWolf(world, wolf.position());
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
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        double launchY = getEyeY() - 0.1;
        if (horizontal >= 1.0E-7) {
            arrow.setPos(getX() + dx / horizontal, launchY, getZ() + dz / horizontal);
        }
        double dy = target.getY(1.0 / 3) - arrow.getY();
        int difficulty = level().getDifficulty().getId();
        arrow.shoot(dx, dy + horizontal * 0.2, dz, 1.6F, 14 - difficulty * 4);
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

    public boolean hasActiveEntrance() { return entranceTicks > 0; }

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
