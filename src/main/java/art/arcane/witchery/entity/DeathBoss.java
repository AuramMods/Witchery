package art.arcane.witchery.entity;

import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryDamageTypes;
import art.arcane.witchery.registry.WitcheryItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.UUID;

public final class DeathBoss extends Monster {
    private static final AttributeModifier PURSUIT_SPEED = new AttributeModifier(
            UUID.fromString("e942c510-c256-11e3-8a33-0800200c9a66"), "Death pursuit speed",
            0.15, AttributeModifier.Operation.ADDITION);
    private static final int GAZE_RANGE = 64;

    private final ServerBossEvent bossBar = new ServerBossEvent(getDisplayName(),
            BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);
    private Player staringPlayer;
    private int stareTicks;
    private int teleportDelay;

    public DeathBoss(EntityType<? extends DeathBoss> type, Level level) {
        super(type, level);
        bossBar.setDarkenScreen(true);
        xpReward = 80;
        setMaxUpStep(1);
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 1000)
                .add(Attributes.MOVEMENT_SPEED, 0.3).add(Attributes.ATTACK_DAMAGE, 7)
                .add(Attributes.FOLLOW_RANGE, GAZE_RANGE);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new MeleeAttackGoal(this, 1, true));
        goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1));
        goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 8));
        goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        LivingEntity target = getTarget();
        if (target == null) {
            teleportDelay = 0;
            seekStare();
        } else {
            if (target instanceof Player player && meetsGaze(player)) {
                if (distanceToSqr(target) < 16) teleportRandomly();
                teleportDelay = 0;
            } else if (distanceToSqr(target) > 256 && ++teleportDelay >= 30 && teleportToward(target)) {
                teleportDelay = 0;
            }
            if ((!target.onGround() || target instanceof Player player && player.getAbilities().flying)
                    && !target.hasEffect(MobEffects.MOVEMENT_SLOWDOWN) && random.nextFloat() < 0.05F) {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 5), this);
            }
        }

        var speed = getAttribute(Attributes.MOVEMENT_SPEED);
        if (speed != null) {
            if (getTarget() == null) speed.removeModifier(PURSUIT_SPEED);
            else if (!speed.hasModifier(PURSUIT_SPEED)) speed.addTransientModifier(PURSUIT_SPEED);
        }
    }

    private void seekStare() {
        Player nearest = level().getNearestPlayer(this, GAZE_RANGE);
        if (nearest == null || !canAttack(nearest) || !meetsGaze(nearest)) {
            staringPlayer = null;
            stareTicks = 0;
            return;
        }
        if (nearest != staringPlayer) {
            staringPlayer = nearest;
            stareTicks = 0;
            if (nearest instanceof ServerPlayer player) {
                player.playNotifySound(SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 1, 1);
            }
        }
        if (++stareTicks >= 6) {
            setTarget(nearest);
            staringPlayer = null;
            stareTicks = 0;
        }
    }

    private boolean meetsGaze(Player player) {
        Vec3 direction = position().add(0, getBbHeight() / 2, 0).subtract(player.getEyePosition());
        double distance = direction.length();
        return distance < 1.0E-6 || player.getLookAngle().dot(direction.scale(1 / distance)) > 1 - 0.025 / distance;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide) {
            for (int i = 0; i < 2; i++) {
                level().addParticle(ParticleTypes.PORTAL, getRandomX(0.5), getRandomY() - 0.25, getRandomZ(0.5),
                        random.nextDouble() - 0.5, -random.nextDouble(), random.nextDouble() - 0.5);
            }
        } else if (isAlive()) {
            if (tickCount % 20 == 0) heal(1);
            bossBar.setProgress(getHealth() / getMaxHealth());
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (!(target instanceof LivingEntity living)) return super.doHurtTarget(target);
        return canAttack(living) && living.hurt(WitcheryDamageTypes.spectralTouch(this),
                Math.max(living.getMaxHealth() * 0.15F, 1));
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (isInvulnerableTo(source)) return false;
        if (!level().isClientSide && !isNoAi() && isAlive()
                && (source.is(DamageTypeTags.IS_PROJECTILE)
                    || source.getDirectEntity() != null && source.getDirectEntity() != source.getEntity())) {
            for (int attempt = 0; attempt < 64; attempt++) {
                if (teleportRandomly()) return true;
            }
        }
        if (source.is(DamageTypes.GENERIC_KILL)) return super.hurt(source, amount);
        float cap = source.is(WitcheryDamageTypes.SPECTRAL_TOUCH) && source.getEntity() instanceof Mob ? 6 : 15;
        return super.hurt(source, Math.min(amount, cap));
    }

    private boolean teleportRandomly() {
        return teleportSafely(getX() + (random.nextDouble() - 0.5) * 32,
                getY() + random.nextInt(64) - 32, getZ() + (random.nextDouble() - 0.5) * 32);
    }

    private boolean teleportToward(LivingEntity target) {
        Vec3 direction = getEyePosition().subtract(target.getEyePosition()).normalize().scale(16);
        return teleportSafely(getX() - direction.x + (random.nextDouble() - 0.5) * 8,
                getY() - direction.y + random.nextInt(16) - 8,
                getZ() - direction.z + (random.nextDouble() - 0.5) * 8);
    }

    private boolean teleportSafely(double x, double y, double z) {
        BlockPos.MutableBlockPos ground = new BlockPos.MutableBlockPos(x, y, z);
        if (!level().isLoaded(ground)) return false;
        while (ground.getY() > level().getMinBuildHeight() && !level().getBlockState(ground).blocksMotion()) {
            ground.move(0, -1, 0);
        }
        if (!level().getBlockState(ground).blocksMotion() || !level().getFluidState(ground.above()).isEmpty()) return false;

        var event = ForgeEventFactory.onEnderTeleport(this, x, ground.getY() + 1, z);
        if (event.isCanceled()) return false;
        Vec3 origin = position();
        if (!randomTeleport(event.getTargetX(), event.getTargetY(), event.getTargetZ(), true)) return false;
        level().playSound(null, origin.x, origin.y, origin.z, SoundEvents.ENDERMAN_TELEPORT, getSoundSource(), 1, 1);
        playSound(SoundEvents.ENDERMAN_TELEPORT, 1, 1);
        return true;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        spawnAtLocation(Items.SKELETON_SKULL);
        spawnAtLocation(new ItemStack(Items.BONE, 5));
        var enchantments = BuiltInRegistries.ENCHANTMENT.stream().toList();
        var enchantment = enchantments.get(random.nextInt(enchantments.size()));
        int low = Math.min(enchantment.getMaxLevel(), enchantment.getMinLevel() + 2);
        int strength = low + random.nextInt(enchantment.getMaxLevel() - low + 1);
        spawnAtLocation(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantment, strength)));

        if (random.nextInt(4) == 0) {
            ItemStack sword = EnchantmentHelper.enchantItem(random, new ItemStack(Items.DIAMOND_SWORD), 40, false);
            sword.setHoverName(Component.translatable("item.witchery.swordofdeath.customname"));
            spawnAtLocation(sword);
        }
        switch (random.nextInt(5)) {
            case 0 -> {
                spawnAtLocation(Items.SADDLE);
                spawnAtLocation(WitcheryIngredient.BINKY_HEAD.item());
            }
            case 1 -> spawnAtLocation(WitcheryItems.getRequired("deathscowl").get());
            case 2 -> spawnAtLocation(WitcheryItems.getRequired("deathsrobe").get());
            case 3 -> spawnAtLocation(WitcheryItems.getRequired("deathsfeet").get());
            case 4 -> spawnAtLocation(WitcheryItems.getRequired("deathshand").get());
        }
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (isDeadOrDying() && level() instanceof ServerLevel level) {
            bossBar.setVisible(false);
            level.sendParticles(ParticleTypes.PORTAL, getX(), getY(0.5), getZ(), 16, 0.5, 1, 0.5, 0.1);
            playSound(SoundEvents.ENDERMAN_TELEPORT, 1, 1);
        }
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
    public void remove(RemovalReason reason) {
        bossBar.removeAllPlayers();
        super.remove(reason);
    }

    @Override
    public void setCustomName(Component name) {
        super.setCustomName(name);
        bossBar.setName(getDisplayName());
    }

    @Override
    protected int decreaseAirSupply(int air) {
        return air;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.SKELETON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SKELETON_DEATH;
    }
}
