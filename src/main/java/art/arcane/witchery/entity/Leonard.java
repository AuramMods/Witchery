package art.arcane.witchery.entity;

import art.arcane.witchery.infusion.SymbolSpell;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryDamageTypes;
import art.arcane.witchery.registry.WitcheryEffects;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcheryItems;
import art.arcane.witchery.registry.WitcherySounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class Leonard extends Monster implements RangedAttackMob {
    private static final EntityDataAccessor<Boolean> ENLARGED =
            SynchedEntityData.defineId(Leonard.class, EntityDataSerializers.BOOLEAN);
    private static final int ENTRANCE_TICKS = 150;
    private static final int SOUL_RADIUS = 15;

    private final ServerBossEvent bossBar = new ServerBossEvent(getDisplayName(),
            BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);
    private int entranceTicks;
    private int lifetime;
    private int soulDelay;
    private int attackTicks;
    private boolean playerCreated;
    private boolean shielded;

    public Leonard(EntityType<? extends Leonard> type, Level level) {
        super(type, level);
        xpReward = 100;
        bossBar.setDarkenScreen(true);
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 600).add(Attributes.MOVEMENT_SPEED, 0.35)
                .add(Attributes.FOLLOW_RANGE, 50).add(Attributes.KNOCKBACK_RESISTANCE, 1);
    }

    public void beginSummoning() {
        entranceTicks = ENTRANCE_TICKS;
        setHealth(getMaxHealth() * 0.25F);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(ENLARGED, false);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new RangedAttackGoal(this, 1, 20, 60, 30));
        goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1) {
            @Override public boolean canUse() { return entranceTicks == 0 && super.canUse(); }
        });
        goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 6));
        goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (attackTicks > 0) attackTicks--;
        if (!(level() instanceof ServerLevel level) || !isAlive()) return;
        if (entranceTicks > 0) {
            if (--entranceTicks % 10 == 0) heal(getMaxHealth() * 0.05F);
            if (entranceTicks == 0) level.globalLevelEvent(1023, blockPosition(), 0);
        } else {
            lifetime++;
            if (tickCount % 20 == 0) {
                heal(1);
                if (!isNoAi() && engaged() && random.nextInt(5) == 0) hexPlayers(level);
            }
            if (tickCount % 20 == 2 && !isNoAi()) {
                if (random.nextInt(5) == 0) disperseBrews(level);
                updateSoulPhase(level);
            }
        }
        boolean large = hasEffect(WitcheryEffects.RESIZING.get());
        if (entityData.get(ENLARGED) != large) entityData.set(ENLARGED, large);
        bossBar.setProgress(getHealth() / getMaxHealth());
    }

    private boolean engaged() {
        return getTarget() != null || getLastHurtByMob() != null;
    }

    private void hexPlayers(ServerLevel level) {
        var players = level.getEntitiesOfClass(ServerPlayer.class, getBoundingBox().inflate(40),
                player -> player.isAlive() && !player.isSpectator() && !player.isCreative()
                        && distanceToSqr(player.getX(), getY(), player.getZ()) <= 1600);
        boolean applied = false;
        for (ServerPlayer player : players) {
            if (player.hasEffect(WitcheryEffects.MORTAL_COIL.get())) continue;
            player.addEffect(new MobEffectInstance(WitcheryEffects.MORTAL_COIL.get(), 1800), this);
            level.sendParticles(ParticleTypes.ENTITY_EFFECT, player.getX(), player.getY(0.5), player.getZ(),
                    24, 0.4, 0.7, 0.4, 0);
            applied = true;
        }
        if (applied) {
            playSound(SoundEvents.NOTE_BLOCK_HARP.value(), 1, 1);
        } else if (!players.isEmpty() && random.nextInt(5) == 1) {
            ServerPlayer target = players.get(random.nextInt(players.size()));
            switch (random.nextInt(10)) {
                case 0, 1, 2 -> target.getActiveEffects().stream().filter(effect -> effect.getEffect().getCategory()
                        != MobEffectCategory.HARMFUL && !effect.getCurativeItems().isEmpty())
                        .map(MobEffectInstance::getEffect).toList().forEach(target::removeEffect);
                case 3, 4, 5 -> target.addEffect(new MobEffectInstance(WitcheryEffects.SINKING.get(), 1200, 3), this);
                case 6, 7, 8 -> target.addEffect(new MobEffectInstance(WitcheryEffects.INSANITY.get(), 1200, 3), this);
                case 9 -> target.addEffect(new MobEffectInstance(WitcheryEffects.OVERHEATING.get(), 1200, 3), this);
            }
        }
    }

    private void updateSoulPhase(ServerLevel level) {
        if (getHealth() >= getMaxHealth() * 0.5F) {
            shielded = false;
            return;
        }
        shielded = level.getEntitiesOfClass(LostSoul.class, getBoundingBox().inflate(SOUL_RADIUS, 5, SOUL_RADIUS),
                LivingEntity::isAlive).stream().anyMatch(soul -> distanceToSqr(soul.getX(), getY(), soul.getZ()) <= 225);
        if (!engaged()) return;
        if (getHealth() < getMaxHealth() * 0.25F && random.nextInt(3) == 1 && !enlarged()) {
            addEffect(new MobEffectInstance(WitcheryEffects.RESIZING.get(), 1200, 3), this);
        }
        if (!shielded && --soulDelay <= 0) {
            removeCoilEffects(level, SOUL_RADIUS, 5);
            soulDelay = 10;
            int wanted = 4 + random.nextInt(2);
            int spawned = 0;
            for (int i = 0; i < wanted; i++) {
                Vec3 point = position().add(random.nextInt(9) - 4, 1, random.nextInt(9) - 4);
                if (spawnSoul(level, point)) spawned++;
            }
            int fallbacks = 3 - spawned;
            for (int i = 0; i < fallbacks; i++) {
                if (spawnSoul(level, position().add(0, 1, 0))) spawned++;
            }
            shielded = spawned > 0;
        }
    }

    private boolean spawnSoul(ServerLevel level, Vec3 position) {
        LostSoul soul = WitcheryEntities.LOST_SOUL.get().create(level);
        if (soul == null || !level.isLoaded(BlockPos.containing(position))) return false;
        soul.setPos(position);
        if (level.getBlockCollisions(soul, soul.getBoundingBox()).iterator().hasNext()) return false;
        soul.configure(random.nextInt(3), 1200 + random.nextInt(600));
        soul.setPersistenceRequired();
        soul.setTarget(getTarget());
        if (!level.addFreshEntity(soul)) return false;
        level.sendParticles(ParticleTypes.SMOKE, position.x, position.y, position.z, 8, 0.2, 0.3, 0.2, 0.02);
        return true;
    }

    private void disperseBrews(ServerLevel level) {
        if (!net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(level, this)) return;
        BlockPos center = blockPosition().above(2);
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-4, -4, -4), center.offset(4, 4, 4))) {
            if (!pos.closerThan(center, 4.1)) continue;
            if (level.getBlockState(pos).getBlock() instanceof art.arcane.witchery.block.BrewDispersalBlock) {
                level.removeBlock(pos, false);
            }
        }
    }

    private void removeCoilEffects(ServerLevel level, int radius, int height) {
        for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, getBoundingBox().inflate(radius, height, radius),
                LivingEntity::isAlive)) {
            player.removeEffect(WitcheryEffects.MORTAL_COIL.get());
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypes.GENERIC_KILL) || source.is(DamageTypes.FELL_OUT_OF_WORLD)) return super.hurt(source, amount);
        if (entranceTicks > 0 || shielded) return false;
        if (source.is(WitcheryDamageTypes.SPECTRAL_TOUCH) && source.getEntity() instanceof Player) {
            return super.hurt(source, Math.min(amount, 2));
        }
        if (!source.is(DamageTypes.PLAYER_ATTACK)) return false;
        float cap = getHealth() < getMaxHealth() * 0.25F ? (enlarged() ? 1 : 4) : 12;
        return super.hurt(source, Math.min(amount, cap));
    }

    public void harmedByIce(@Nullable LivingEntity caster, int amount) {
        if (entranceTicks == 0 && getHealth() < getMaxHealth() * 0.4F) {
            super.hurt(damageSources().indirectMagic(caster, caster), Math.min(amount, enlarged() ? 8 : 15));
        }
    }

    @Override
    public void performRangedAttack(LivingEntity target, float distance) {
        if (!(level() instanceof ServerLevel level) || entranceTicks > 0 || !random.nextBoolean()) return;
        beginAttack();
        Vec3 origin = position().add(0, getBbHeight() / 2, 0);
        double spread = Math.sqrt(distance) * 0.5;
        Vec3 aim = target.position().add(0, target.getBbHeight() / 2, 0).subtract(origin)
                .add(random.nextGaussian() * spread, 0, random.nextGaussian() * spread);
        SymbolSpell spell = switch (random.nextInt(21)) {
            case 14, 15 -> SymbolSpell.EXPELLIARMUS;
            case 16, 17 -> SymbolSpell.FLIPENDO;
            case 18, 19 -> SymbolSpell.IMPEDIMENTA;
            case 20 -> SymbolSpell.CONFUNDUS;
            default -> SymbolSpell.IGNIANIMA;
        };
        level.addFreshEntity(new SymbolProjectile(level, this, spell, origin, aim));
        playSound(SoundEvents.GHAST_SHOOT, 1, 1);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (entranceTicks > 0) return false;
        beginAttack();
        boolean hit = target.hurt(damageSources().mobAttack(this), 7 + random.nextInt(15));
        if (hit) target.push(0, 0.4, 0);
        playSound(SoundEvents.IRON_GOLEM_ATTACK, 1, 1);
        return hit;
    }

    private void beginAttack() {
        attackTicks = 10;
        level().broadcastEntityEvent(this, (byte) 4);
    }

    @Override
    public void handleEntityEvent(byte event) {
        if (event == 4) attackTicks = 10;
        else super.handleEntityEvent(event);
    }

    public int attackTicks() { return attackTicks; }
    public boolean enlarged() { return entityData.get(ENLARGED); }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return super.getDimensions(pose).scale(enlarged() ? 3 : 1);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (ENLARGED.equals(key)) refreshDimensions();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("PlayerCreated", playerCreated);
        tag.putInt("Invul", entranceTicks);
        tag.putLong("Lifetime", lifetime);
        tag.putInt("SoulDelay", soulDelay);
        tag.putBoolean("Shielded", shielded);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        playerCreated = tag.getBoolean("PlayerCreated");
        entranceTicks = Math.max(0, tag.getInt("Invul"));
        lifetime = Math.max(0, tag.getInt("Lifetime"));
        soulDelay = tag.getInt("SoulDelay");
        shielded = tag.getBoolean("Shielded");
        entityData.set(ENLARGED, hasEffect(WitcheryEffects.RESIZING.get()));
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        var choices = BuiltInRegistries.ENCHANTMENT.stream().toList();
        var enchantment = choices.get(random.nextInt(choices.size()));
        int minimum = Math.min(enchantment.getMaxLevel(), enchantment.getMinLevel() + 2);
        int level = minimum + random.nextInt(enchantment.getMaxLevel() - minimum + 1);
        spawnAtLocation(EnchantedBookItem.createForEnchantment(new EnchantmentInstance(enchantment, level)));
        spawnAtLocation(WitcheryIngredient.DEMON_HEART.item());
        spawnAtLocation(WitcheryItems.getRequired("leonardsurn").get());
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (isDeadOrDying() && level() instanceof ServerLevel level) {
            removeCoilEffects(level, 40, 40);
            bossBar.setVisible(false);
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
    public void setCustomName(@Nullable Component name) {
        super.setCustomName(name);
        bossBar.setName(getDisplayName());
    }

    @Override protected int decreaseAirSupply(int air) { return air; }
    @Override public boolean isPushable() { return false; }
    @Override protected SoundEvent getAmbientSound() { return WitcherySounds.LEONARD_AMBIENT.get(); }
    @Override protected SoundEvent getHurtSound(DamageSource source) { return WitcherySounds.LEONARD_HIT.get(); }
    @Override protected SoundEvent getDeathSound() { return WitcherySounds.LEONARD_DEATH.get(); }
}
