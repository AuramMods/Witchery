package art.arcane.witchery.entity;

import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.registry.WitcheryEffects;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcheryItems;
import art.arcane.witchery.registry.WitcherySounds;
import art.arcane.witchery.world.WitcheryTeleport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
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
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class Leonard extends Monster implements RangedAttackMob {
    private static final int ENTRANCE_TICKS = 150;
    private static final int SOUL_RADIUS = 15;
    private static final RandomSource SPELL_RANDOM = RandomSource.create();

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
        if (hasActiveEntrance()) {
            if (--entranceTicks == 0) level.globalLevelEvent(1023, blockPosition(), 0);
            if (tickCount % 10 == 0) heal(getMaxHealth() * 0.05F);
        } else {
            lifetime++;
            if (tickCount % 20 == 0) {
                heal(1);
                if (!isNoAi() && level.random.nextInt(5) == 0 && engaged()) hexPlayers(level);
            }
            if (tickCount % 20 == 2 && !isNoAi()) {
                if (level.random.nextInt(5) == 0) disperseBrews(level);
                updateSoulPhase(level);
            }
        }
        bossBar.setProgress(getHealth() / getMaxHealth());
    }

    private boolean engaged() {
        return getTarget() != null || getLastHurtByMob() != null;
    }

    private void hexPlayers(ServerLevel level) {
        var players = level.getEntitiesOfClass(ServerPlayer.class, AABB.ofSize(position(), 80, 80, 80));
        boolean applied = false;
        for (ServerPlayer player : players) {
            if (!canHex(player) || player.hasEffect(WitcheryEffects.MORTAL_COIL.get())) continue;
            WitcheryNetwork.sendLeonardHex(level, player.position());
            player.addEffect(new MobEffectInstance(WitcheryEffects.MORTAL_COIL.get(), 1800), this);
            applied = true;
        }
        if (applied) {
            WitcheryNetwork.sendLeonardHexCast(level, position(), 39168);
        } else if (level.random.nextInt(5) == 1 && !players.isEmpty()) {
            ServerPlayer target = players.get(level.random.nextInt(players.size()));
            if (!canHex(target)) return;
            WitcheryNetwork.sendLeonardHex(level, target.position());
            switch (level.random.nextInt(10)) {
                case 0, 1, 2 -> target.getActiveEffects().stream().filter(effect -> effect.getEffect().getCategory()
                        != MobEffectCategory.HARMFUL && !effect.getCurativeItems().isEmpty())
                        .map(MobEffectInstance::getEffect).toList().forEach(target::removeEffect);
                case 3, 4, 5 -> {
                    target.addEffect(new MobEffectInstance(WitcheryEffects.SINKING.get(), 1200, 3), this);
                    WitcheryNetwork.sendLeonardHexCast(level, position(), 10027008);
                }
                case 6, 7, 8 -> {
                    target.addEffect(new MobEffectInstance(WitcheryEffects.INSANITY.get(), 1200, 3), this);
                    WitcheryNetwork.sendLeonardHexCast(level, position(), 153);
                }
                case 9 -> {
                    target.addEffect(new MobEffectInstance(WitcheryEffects.OVERHEATING.get(), 1200, 3), this);
                    WitcheryNetwork.sendLeonardHexCast(level, position(), 39321);
                }
            }
        }
    }

    private boolean canHex(ServerPlayer player) {
        return player.isAlive() && !player.isSpectator() && distanceToSqr(player.getX(), getY(), player.getZ()) <= 1600;
    }

    private void updateSoulPhase(ServerLevel level) {
        if (getHealth() >= getMaxHealth() * 0.5F) {
            shielded = false;
            return;
        }
        if (!engaged()) return;
        if (getHealth() < getMaxHealth() * 0.25F && level.random.nextInt(3) == 1 && !hasEffect(WitcheryEffects.RESIZING.get())) {
            addEffect(new MobEffectInstance(WitcheryEffects.RESIZING.get(), 1200, 3), this);
        }
        shielded = !level.getEntitiesOfClass(LostSoul.class,
                AABB.ofSize(position(), SOUL_RADIUS * 2, 10, SOUL_RADIUS * 2)).isEmpty();
        if (shielded || --soulDelay > 0) return;

        removeCoilEffects(level, SOUL_RADIUS, 5);
        soulDelay = 10;
        int spawned = 0;
        for (int i = 0; i < 4 + level.random.nextInt(2); i++) {
            if (spawnSoul(level, 1, 4)) spawned++;
        }
        for (int i = spawned; i < 3; i++) spawnSoul(level, 0, 0);
    }

    private boolean spawnSoul(ServerLevel level, int minimum, int maximum) {
        int span = maximum - minimum;
        int offsetX = level.random.nextInt(span * 2 + 1);
        int offsetZ = level.random.nextInt(span * 2 + 1);
        var start = new BlockPos(
                (int) getX() - maximum + offsetX + (offsetX > span ? minimum * 2 : 0), (int) getY() + 1,
                (int) getZ() - maximum + offsetZ + (offsetZ > span ? minimum * 2 : 0));
        if (!level.hasChunkAt(start) || !level.getWorldBorder().isWithinBounds(start)) return false;
        BlockPos floor = WitcheryTeleport.findEncounterGround(level, start);
        BlockPos landing = floor.above();
        if (level.isOutsideBuildHeight(landing.above()) || !level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP)
                || !level.isEmptyBlock(landing) || !level.isEmptyBlock(landing.above())) return false;
        LostSoul soul = WitcheryEntities.LOST_SOUL.get().create(level);
        if (soul == null) return false;
        soul.setPos(Vec3.atBottomCenterOf(landing).add(0, 0.05, 0));
        if (level.getBlockCollisions(soul, soul.getBoundingBox()).iterator().hasNext()) return false;
        soul.configure(level.random.nextInt(3), -1);
        soul.setPersistenceRequired();
        if (!level.addFreshEntity(soul)) return false;
        WitcheryNetwork.sendLostSoulArrival(level, soul.position(), soul.getBbHeight());
        soul.setLifetime((60 + level.random.nextInt(30)) * 20);
        return true;
    }

    private void disperseBrews(ServerLevel level) {
        if (!net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(level, this)) return;
        BlockPos center = blockPosition().above(2);
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-4, -4, -4), center.offset(4, 4, 4))) {
            if (pos.distSqr(center) >= 15) continue;
            if (level.getBlockState(pos).getBlock() instanceof art.arcane.witchery.block.BrewDispersalBlock) {
                level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            }
        }
    }

    private void removeCoilEffects(ServerLevel level, int radius, int height) {
        for (ServerPlayer player : level.getEntitiesOfClass(ServerPlayer.class, AABB.ofSize(position(), radius * 2, height * 2, radius * 2),
                LivingEntity::isAlive)) {
            player.removeEffect(WitcheryEffects.MORTAL_COIL.get());
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypes.GENERIC_KILL) || source.is(DamageTypes.FELL_OUT_OF_WORLD)) return super.hurt(source, amount);
        if (shielded) return false;
        if (!source.is(DamageTypes.PLAYER_ATTACK)) return false;
        float cap = getHealth() < getMaxHealth() * 0.25F ? (enlarged() ? 1 : 4) : 12;
        return super.hurt(source, Math.min(amount, cap));
    }

    public void harmedByIce(@Nullable LivingEntity caster, int amount) {
        if (getHealth() < getMaxHealth() * 0.4F) {
            super.hurt(damageSources().indirectMagic(caster, caster), Math.min(amount, enlarged() ? 8 : 15));
        }
    }

    @Override
    public void performRangedAttack(LivingEntity target, float strength) {
        if (!(level() instanceof ServerLevel level) || entranceTicks > 0 || !level.random.nextBoolean()) return;
        beginAttack();
        SymbolProjectile.castMobSymbol(level, this, target, SymbolProjectile.commonMobSpell(SPELL_RANDOM), strength);
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
    public boolean hasActiveEntrance() { return entranceTicks > 0; }
    public boolean enlarged() {
        var effect = getEffect(WitcheryEffects.RESIZING.get());
        return effect != null && effect.getAmplifier() >= 2;
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
