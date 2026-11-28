package art.arcane.witchery.entity;

import art.arcane.witchery.infusion.SymbolSpell;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.registry.WitcheryDamageTypes;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcherySounds;
import art.arcane.witchery.world.TormentTravel;
import art.arcane.witchery.world.WitcheryDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class LordOfTorment extends Monster implements RangedAttackMob {
    private final ServerBossEvent bar = new ServerBossEvent(getDisplayName(), BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.PROGRESS);
    private final Set<String> attackers = new HashSet<>();
    private int attackTicks;

    public LordOfTorment(EntityType<? extends LordOfTorment> type, Level level) {
        super(type, level);
        setNoGravity(true);
        xpReward = 80;
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 500)
                .add(Attributes.MOVEMENT_SPEED, 0.2).add(Attributes.FLYING_SPEED, 0.2)
                .add(Attributes.ATTACK_DAMAGE, 8).add(Attributes.FOLLOW_RANGE, 50);
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        var navigation = new GroundPathNavigation(this, level);
        navigation.setCanFloat(true);
        navigation.setCanOpenDoors(true);
        return navigation;
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new TormentFlightGoals.RangedAttack(this));
        goalSelector.addGoal(5, new TormentFlightGoals.Land(this));
        goalSelector.addGoal(6, new TormentFlightGoals.Wander(this));
        goalSelector.addGoal(7, new LookAtPlayerGoal(this, Player.class, 8));
        goalSelector.addGoal(8, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 0, true, false, null));
    }

    @Override
    public void travel(Vec3 input) {
        if (isControlledByLocalInstance()) {
            boolean inWater = isInWater();
            if (inWater || isInLava()) {
                moveRelative(0.02F, input);
                move(MoverType.SELF, getDeltaMovement());
                setDeltaMovement(getDeltaMovement().scale(inWater ? 0.8F : 0.5D));
            } else {
                BlockPos ground = getBlockPosBelowThatAffectsMyMovement();
                float friction = onGround() ? 0.91F * level().getBlockState(ground).getFriction(level(), ground, this) : 0.91F;
                moveRelative(onGround() ? 0.1F * 0.16277136F / (friction * friction * friction) : 0.02F, input);
                move(MoverType.SELF, getDeltaMovement());
                setDeltaMovement(getDeltaMovement().scale(friction));
            }
        }
        calculateEntityAnimation(false);
    }

    @Override
    protected void checkFallDamage(double movement, boolean onGround, BlockState state, BlockPos pos) {}

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public boolean onClimbable() {
        return false;
    }

    @Override
    protected int decreaseAirSupply(int air) {
        return air;
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            var random = level().getRandom();
            level().addParticle(ParticleTypes.FLAME, getX() + (random.nextDouble() - 0.5) * getBbWidth(),
                    getY() + 0.1 + random.nextDouble() * 2, getZ() + (random.nextDouble() - 0.5) * getBbWidth(), 0, 0, 0);
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (attackTicks > 0) attackTicks--;
        if (!level().isClientSide()) {
            bar.setProgress(getHealth() / getMaxHealth());
            if (level().getRandom().nextDouble() < 0.05 && getTarget() instanceof ServerPlayer player && player.getAbilities().flying
                    && !player.hasEffect(MobEffects.MOVEMENT_SLOWDOWN)) {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 5));
            }
        }
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        if (!source.is(WitcheryDamageTypes.DIRECT_FIRE)) return super.isInvulnerableTo(source);
        // The old fire immunity applies to contact with the environment. Witchery attacks may deal fire damage directly.
        return isRemoved() || isInvulnerable() && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY) && !source.isCreativePlayer();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.IS_EXPLOSION) || !isAlive()) return false;
        boolean demonic = source.is(WitcheryDamageTypes.DEMONIC);
        Entity attacker = demonic ? source.getEntity() : source.getDirectEntity();
        if (attacker instanceof ServerPlayer player) attackers.add(player.getGameProfile().getName());
        boolean damaged = super.hurt(source, Math.min(amount, demonic ? 8 : 5));
        if (!isRemoved() && level() instanceof ServerLevel server
                && !server.dimension().equals(WitcheryDimensions.TORMENT) && getHealth() <= getMaxHealth() / 2) {
            int floor = TormentTravel.randomFloor(server.getRandom());
            AABB nearby = AABB.ofSize(position(), 32, 64, 32);
            for (ServerPlayer player : server.players()) {
                if (nearby.intersects(player.getBoundingBox())
                        || attackers.contains(player.getGameProfile().getName())) {
                    TormentTravel.requestBossEncounter(player, floor);
                }
            }
            WitcheryNetwork.sendTormentPortalCue(server, position());
            discard();
        }
        return damaged;
    }

    @Override
    public void performRangedAttack(LivingEntity target, float strength) {
        if (!(level() instanceof ServerLevel server)) return;
        beginAttack();
        MobProjectile.playLaunchCue(server, this);
        int bursts = random.nextInt(10) == 0 ? 9 : 3;
        server.addFreshEntity(SymbolProjectile.mobShot(server, this, target, SymbolSpell.IGNIANIMA, strength));
        for (int i = 0; i < bursts; i++) {
            Vec3 aim = MobProjectile.aim(this, target, strength);
            Soulfire shot = new Soulfire(WitcheryEntities.SOULFIRE.get(), this, aim);
            shot.moveTo(getX(), getY(0.5) + 0.5, getZ(), getYRot(), getXRot());
            server.addFreshEntity(shot);
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        beginAttack();
        playSound(SoundEvents.IRON_GOLEM_ATTACK, 1, 1);
        boolean hit = target.hurt(damageSources().mobAttack(this), 7 + random.nextInt(15));
        if (hit) target.push(0, 0.4, 0);
        return hit;
    }

    private void beginAttack() {
        attackTicks = 10;
        level().broadcastEntityEvent(this, (byte)4);
    }

    @Override
    public void handleEntityEvent(byte event) {
        if (event == 4) {
            attackTicks = 10;
            playSound(SoundEvents.IRON_GOLEM_ATTACK, 1, 1);
        } else {
            super.handleEntityEvent(event);
        }
    }

    public int attackTicks() {
        return attackTicks;
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        bar.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bar.removePlayer(player);
    }

    @Override
    public void remove(RemovalReason reason) {
        bar.removeAllPlayers();
        super.remove(reason);
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        List<Enchantment> choices = BuiltInRegistries.ENCHANTMENT.stream().toList();
        if (!choices.isEmpty()) {
            Enchantment enchantment = choices.get(random.nextInt(choices.size()));
            int high = enchantment.getMaxLevel();
            int low = Math.min(high, enchantment.getMinLevel() + 3);
            int level = low == high ? high : low + random.nextInt(high - low + 1);
            var book = EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantment, level));
            spawnAtLocation(book);
            spawnAtLocation(book.copy());
        }
        spawnAtLocation(WitcheryIngredient.DEMON_HEART.item());
        spawnAtLocation(WitcheryIngredient.BREW_SOUL_TORMENT.item());
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return WitcherySounds.TORMENT_LAUGH.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 200;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return WitcherySounds.TORMENT_HIT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return WitcherySounds.TORMENT_DEATH.get();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag root) {
        super.addAdditionalSaveData(root);
        ListTag names = new ListTag();
        for (String name : attackers) names.add(StringTag.valueOf(name));
        root.put("WITCAttackers", names);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag root) {
        super.readAdditionalSaveData(root);
        attackers.clear();
        ListTag names = root.getList("WITCAttackers", Tag.TAG_STRING);
        for (int index = 0; index < names.size(); index++) attackers.add(names.getString(index));
        bar.setName(getDisplayName());
    }
}
