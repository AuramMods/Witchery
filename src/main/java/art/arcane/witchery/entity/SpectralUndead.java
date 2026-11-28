package art.arcane.witchery.entity;

import art.arcane.witchery.block.entity.HauntingContainerOrder;
import art.arcane.witchery.infusion.InfernalInfusion;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.registry.WitcheryDamageTypes;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcheryItems;
import art.arcane.witchery.registry.WitcherySounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public final class SpectralUndead extends Monster {
    private static final EntityDataAccessor<Boolean> SCREAMING =
            SynchedEntityData.defineId(SpectralUndead.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> OBSCURED =
            SynchedEntityData.defineId(SpectralUndead.class, EntityDataSerializers.BOOLEAN);
    private static final int HAUNT_RADIUS = 16;

    public enum Kind {
        SPECTRE(1), BANSHEE(2), POLTERGEIST(3);

        private final int legacyId;

        Kind(int legacyId) {
            this.legacyId = legacyId;
        }

        public int legacyId() {
            return legacyId;
        }

        public EntityType<SpectralUndead> type() {
            return switch (this) {
                case SPECTRE -> WitcheryEntities.SPECTRE.get();
                case BANSHEE -> WitcheryEntities.BANSHEE.get();
                case POLTERGEIST -> WitcheryEntities.POLTERGEIST.get();
            };
        }
    }

    public enum Summoning {
        BRAZIER(1, 2), STRAY(6, 10), STONE(0, 1);

        private final int min;
        private final int max;

        Summoning(int min, int max) {
            this.min = min;
            this.max = max;
        }

        private int offset(ServerLevel level) {
            int span = max - min;
            int draw = level.getRandom().nextInt(span * 2 + 1);
            return draw - max + (draw > span ? min * 2 : 0);
        }
    }

    private final Kind kind;
    private String summoner = "";
    private int lifeTicks = -1;
    @Nullable
    private UUID sentinelTarget;
    @Nullable
    private NearestAttackableTargetGoal<?> sentinelTargeting;
    private int throwAnimation;

    public SpectralUndead(EntityType<? extends SpectralUndead> type, Level level, Kind kind) {
        super(type, level);
        this.kind = kind;
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, kind == Kind.POLTERGEIST ? new PoltergeistMeleeGoal(this)
                : new MeleeAttackGoal(this, kind == Kind.BANSHEE ? 0.3 : 1, kind == Kind.SPECTRE));
        if (kind == Kind.SPECTRE) goalSelector.addGoal(3, new MoveTowardsTargetGoal(this, 1, 32));
        if (getNavigation() instanceof GroundPathNavigation navigation) {
            navigation.setCanOpenDoors(kind != Kind.SPECTRE);
            navigation.setAvoidSun(kind == Kind.BANSHEE);
            if (kind == Kind.POLTERGEIST) goalSelector.addGoal(3, new OpenDoorGoal(this, true));
        }
        int wanderPriority = kind == Kind.BANSHEE ? 3 : 4;
        goalSelector.addGoal(wanderPriority, new RandomStrollGoal(this, 1));
        goalSelector.addGoal(wanderPriority + 1, new LookAtPlayerGoal(this, Player.class, 8));
        goalSelector.addGoal(wanderPriority + 2, new RandomLookAroundGoal(this));

        HurtByTargetGoal retaliation = new HurtByTargetGoal(this) {
            @Override
            protected void alertOther(Mob mob, LivingEntity attacker) {
                // The original species had separate classes. Forge sees all three as SpectralUndead.
                if (mob instanceof SpectralUndead ally && ally.kind == kind && ally.canAttack(attacker)) {
                    super.alertOther(ally, attacker);
                }
            }
        };
        targetSelector.addGoal(1, kind == Kind.SPECTRE ? retaliation : retaliation.setAlertOthers());
        if (kind != Kind.POLTERGEIST) {
            targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, 0, true, false,
                    target -> !InfernalInfusion.owns(this, target)));
        }
    }

    private static final class PoltergeistMeleeGoal extends MeleeAttackGoal {
        private PoltergeistMeleeGoal(SpectralUndead mob) {
            super(mob, 1, false);
        }

        @Override
        public boolean canUse() {
            return mob.getTarget() instanceof Player && super.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return mob.getTarget() instanceof Player && super.canContinueToUse();
        }
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new GroundPathNavigation(this, level) {
            @Override
            protected void trimPath() {
                int length = path.getNodeCount();
                super.trimPath();
                if (kind == Kind.BANSHEE && path.getNodeCount() < length) {
                    // The original sun restriction also removes the shaded node before the first sunny node.
                    path.truncateNodes(Math.max(0, path.getNodeCount() - 1));
                }
            }
        };
    }

    public static AttributeSupplier.Builder attributes(Kind kind) {
        return createMonsterAttributes().add(Attributes.MAX_HEALTH, 20)
                .add(Attributes.FOLLOW_RANGE, kind == Kind.POLTERGEIST ? 20 : 40)
                .add(Attributes.MOVEMENT_SPEED, switch (kind) {
                    case SPECTRE -> 0.4;
                    case BANSHEE -> 0.2;
                    case POLTERGEIST -> 0.3;
                })
                .add(Attributes.ATTACK_DAMAGE, switch (kind) {
                    case SPECTRE -> 4;
                    case BANSHEE -> 0;
                    case POLTERGEIST -> 3;
                })
                .add(Attributes.ARMOR, kind == Kind.SPECTRE ? 2 : 0);
    }

    @Nullable
    public static SpectralUndead summonNear(ServerLevel level, BlockPos origin, Kind kind, Summoning source) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) return null;
        BlockPos column = origin.offset(source.offset(level), 0, source.offset(level));
        BlockPos feet = landing(level, column);
        if (feet == null) return null;

        SpectralUndead mob = kind.type().create(level);
        if (mob == null) return null;
        mob.moveTo(feet.getX() + 0.5, feet.getY() + 0.05, feet.getZ() + 0.5, 0, 0);
        if (level.getBlockCollisions(mob, mob.getBoundingBox()).iterator().hasNext()) return null;
        // Preserve the brazier's next world roll. Vanilla's spawn finalization also draws from that random source.
        mob.initializeSpectralSpawn();
        mob.setPersistenceRequired();
        if (!level.addFreshEntity(mob)) return null;
        WitcheryNetwork.sendSpectralSummon(level, mob.position(), mob.getBbHeight(), source);
        return mob;
    }

    public static void summonSentinel(ServerLevel level, LivingEntity intruder) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) return;
        BlockPos column = intruder.blockPosition().north().west();
        BlockPos feet = landing(level, column);
        if (feet == null) return;

        SpectralUndead guard = Kind.SPECTRE.type().create(level);
        if (guard == null) return;
        guard.moveTo(feet.getX() + 0.5, feet.getY() + 0.05, feet.getZ() + 0.5, 0, 0);
        guard.initializeSpectralSpawn();
        guard.setPersistenceRequired();
        guard.lifeTicks = 600;
        guard.sentinelTarget = intruder.getUUID();
        // Both guards can arrive in the original column on the same tick.
        if (!level.addFreshEntity(guard)) return;
        guard.followSentinelTarget(intruder);
        WitcheryNetwork.sendSentinelAppearance(level, guard.position(), guard.getBbHeight());
    }

    @Nullable
    private static BlockPos landing(ServerLevel level, BlockPos column) {
        if (!level.isLoaded(column) || !level.getWorldBorder().isWithinBounds(column)) return null;
        int bottom = level.getMinBuildHeight();
        int top = level.getMaxBuildHeight();
        int floor = Math.min(column.getY(), top - 1);
        int searchTop = Math.min(floor + 8, top - 1);
        while (floor < searchTop && !level.isEmptyBlock(column.atY(floor))) floor++;
        while (floor >= bottom && level.isEmptyBlock(column.atY(floor))) floor--;
        if (floor < bottom || floor + 2 >= top || !level.isEmptyBlock(column.atY(floor + 1))
                || !level.isEmptyBlock(column.atY(floor + 2))) return null;
        return column.atY(floor + 1);
    }

    private void followSentinelTarget(LivingEntity intruder) {
        if (sentinelTargeting == null) {
            // The original Sentinel gives creatures of the intruder's class priority over the usual player target.
            sentinelTargeting = new NearestAttackableTargetGoal<>(this, intruder.getClass(), 0, true, false, null);
            targetSelector.addGoal(1, sentinelTargeting);
        }
        setTarget(intruder);
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
                                        @Nullable SpawnGroupData group, @Nullable CompoundTag tag) {
        var result = super.finalizeSpawn(level, difficulty, reason, group, tag);
        initializeSpectralSpawn();
        return result;
    }

    private void initializeSpectralSpawn() {
        if (kind == Kind.SPECTRE) {
            SpectreSpawnEquipment.initialize(this);
            entityData.set(OBSCURED, true);
        }
        if (kind == Kind.POLTERGEIST) addEffect(new MobEffectInstance(MobEffects.INVISIBILITY,
                MobEffectInstance.INFINITE_DURATION, 0, false, false));
    }

    public Kind kind() {
        return kind;
    }

    public boolean screaming() {
        return entityData.get(SCREAMING);
    }

    public boolean obscured() {
        return entityData.get(OBSCURED);
    }

    public void reveal() {
        entityData.set(OBSCURED, false);
    }

    public int throwAnimation() {
        return throwAnimation;
    }

    public void setSummoner(ServerPlayer player) {
        summoner = player.getGameProfile().getName();
        setPersistenceRequired();
        InfernalInfusion.bind(this, player);
    }

    public boolean temporary() {
        return lifeTicks >= 0;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(SCREAMING, false);
        entityData.define(OBSCURED, false);
    }

    @Override
    public MobType getMobType() {
        return MobType.UNDEAD;
    }

    @Override
    protected int decreaseAirSupply(int air) {
        return air;
    }

    @Override
    public boolean canAttack(LivingEntity target) {
        return !InfernalInfusion.owns(this, target) && super.canAttack(target);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return super.hurt(source, Math.min(amount, 15));
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (kind == Kind.SPECTRE && target instanceof LivingEntity living) {
            if (!canAttack(living)) return false;
            float damage = Math.max(1, living.getMaxHealth() * 0.15F)
                    + EnchantmentHelper.getDamageBonus(getMainHandItem(), living.getMobType());
            if (!living.hurt(WitcheryDamageTypes.spectralTouch(this), damage)) return false;
            int knockback = EnchantmentHelper.getKnockbackBonus(this);
            if (knockback > 0) {
                float yaw = getYRot() * Mth.DEG_TO_RAD;
                living.push(-Mth.sin(yaw) * knockback * 0.5F, 0.1, Mth.cos(yaw) * knockback * 0.5F);
                setDeltaMovement(getDeltaMovement().multiply(0.6, 1, 0.6));
            }
            int fire = EnchantmentHelper.getFireAspect(this);
            if (fire > 0) living.setSecondsOnFire(fire * 4);
            return true;
        }
        return super.doHurtTarget(target);
    }

    private boolean spectralTouch(LivingEntity target, float fraction) {
        return target.hurt(WitcheryDamageTypes.spectralTouch(this), Math.max(1, target.getMaxHealth() * fraction));
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (throwAnimation > 0) throwAnimation--;
        if (!(level() instanceof ServerLevel level) || !isAlive()) return;
        if (sentinelTarget != null && sentinelTargeting == null
                && level.getEntity(sentinelTarget) instanceof LivingEntity target && target.isAlive()) {
            followSentinelTarget(target);
        }
        if (temporary() && (--lifeTicks <= 0 || getTarget() == null || !getTarget().isAlive())) {
            WitcheryNetwork.sendSpiritDeparture(level, position());
            discard();
            return;
        }
        if (kind == Kind.BANSHEE) scream(level);
        if (kind == Kind.POLTERGEIST && tickCount % 100 == 0) haunt(level);
    }

    private void scream(ServerLevel level) {
        boolean wasScreaming = screaming();
        if (tickCount % (wasScreaming ? 20 : 100) == 0) {
            boolean nearby = false;
            Vec3 center = position();
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, new AABB(center, center).inflate(6),
                    candidate -> candidate.isAlive() && distanceToSqr(candidate) <= 36
                            && (candidate instanceof Player || candidate == getTarget()))) {
                nearby = true;
                if (!(target instanceof Player) || !target.getItemBySlot(EquipmentSlot.HEAD).is(WitcheryItems.getRequired("earmuffs").get())) {
                    spectralTouch(target, 0.1F);
                }
            }
            entityData.set(SCREAMING, nearby);
        }
        if (screaming() && (!wasScreaming || tickCount % 60 == 0)) {
            playSound(WitcherySounds.BANSHEE_SCREAM.get(), 1, 0.7F + level.getRandom().nextFloat() * 0.3F);
        }
    }

    private void haunt(ServerLevel level) {
        AABB area = AABB.ofSize(position(), 2 * HAUNT_RADIUS, 2 * HAUNT_RADIUS, 2 * HAUNT_RADIUS);
        boolean grief = ForgeEventFactory.getMobGriefingEvent(level, this);
        if (grief) {
            for (HangingEntity hanging : level.getEntitiesOfClass(HangingEntity.class, area, target -> distanceToSqr(target) <= 256)) {
                if (approach(hanging.position())) {
                    hanging.hurt(damageSources().mobAttack(this), 3);
                    beginThrow(level);
                }
                return;
            }

            if (!summoner.isEmpty() && level.players().stream().anyMatch(player ->
                    player.getGameProfile().getName().equals(summoner) && distanceToSqr(player) <= 64)
                    && hauntContainer(level)) return;
        }

        for (ItemEntity dropped : level.getEntitiesOfClass(ItemEntity.class, area, target -> distanceToSqr(target) <= 256)) {
            if (approach(dropped.position())) {
                dropped.setDeltaMovement(throwVelocity(level));
                dropped.hurtMarked = true;
                beginThrow(level);
            }
            return;
        }
    }

    private boolean hauntContainer(ServerLevel level) {
        List<BlockEntity> candidates = new ArrayList<>();
        for (int cx = SectionPos.blockToSectionCoord(getBlockX() - HAUNT_RADIUS);
             cx <= SectionPos.blockToSectionCoord(getBlockX() + HAUNT_RADIUS); cx++) {
            for (int cz = SectionPos.blockToSectionCoord(getBlockZ() - HAUNT_RADIUS);
                 cz <= SectionPos.blockToSectionCoord(getBlockZ() + HAUNT_RADIUS); cz++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(cx, cz);
                if (chunk == null) continue;
                for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                    if (blockEntity instanceof Container && !blockEntity.isRemoved()
                            && distanceToSqr(Vec3.atCenterOf(blockEntity.getBlockPos())) <= HAUNT_RADIUS * HAUNT_RADIUS) {
                        candidates.add(blockEntity);
                    }
                }
            }
        }
        // Reading a modern loot container can generate its contents. Only inspect candidates in pursuit order.
        candidates.sort(Comparator.<BlockEntity>comparingDouble(candidate -> distanceToSqr(Vec3.atCenterOf(candidate.getBlockPos())))
                .thenComparingLong(candidate -> ((HauntingContainerOrder) candidate).witchery$hauntingLoadOrder()));
        BlockEntity closest = candidates.stream()
                .filter(candidate -> !candidate.isRemoved() && !((Container) candidate).isEmpty())
                .findFirst().orElse(null);
        if (!(closest instanceof Container inventory)) return false;
        Vec3 position = Vec3.atCenterOf(closest.getBlockPos());
        if (!approach(position)) return true;

        List<Integer> slots = new ArrayList<>();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (!inventory.getItem(slot).isEmpty()) slots.add(slot);
        }
        if (slots.isEmpty()) return false;
        int slot = slots.get(level.getRandom().nextInt(slots.size()));
        ItemStack original = inventory.getItem(slot).copy();
        Vec3 motion = throwVelocity(level);
        // An explicit velocity avoids the item constructor's two world-random throws.
        ItemEntity dropped = new ItemEntity(level, position.x, position.y, position.z, original.copyWithCount(1),
                motion.x, motion.y, motion.z);
        dropped.lifespan = 18000;
        if (!level.addFreshEntity(dropped)) return true;
        if (closest.isRemoved() || !ItemStack.matches(inventory.getItem(slot), original)) {
            dropped.discard();
            return true;
        }
        inventory.removeItem(slot, 1);
        inventory.setChanged();
        beginThrow(level);
        return true;
    }

    private boolean approach(Vec3 target) {
        if (distanceToSqr(target) <= 9) return true;
        getNavigation().moveTo(target.x, target.y, target.z, 1);
        return false;
    }

    private static Vec3 throwVelocity(ServerLevel level) {
        var random = level.getRandom();
        return new Vec3(random.nextDouble() * 0.6 - 0.3, random.nextDouble() * 0.2 + 0.1,
                random.nextDouble() * 0.6 - 0.3);
    }

    private void beginThrow(ServerLevel level) {
        throwAnimation = 15;
        level.broadcastEntityEvent(this, (byte) 4);
    }

    @Override
    public void handleEntityEvent(byte event) {
        if (event == 4 && kind == Kind.POLTERGEIST) {
            throwAnimation = 15;
        } else {
            super.handleEntityEvent(event);
        }
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        if (!temporary() && random.nextInt(Math.max(4 - looting, 2)) == 0) {
            spawnAtLocation(new ItemStack(WitcheryIngredient.SPECTRAL_DUST.item()));
        }
    }

    @Override
    @Nullable
    protected SoundEvent getAmbientSound() {
        return kind == Kind.SPECTRE ? WitcherySounds.SPECTRE_AMBIENT.get() : null;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return kind == Kind.BANSHEE ? WitcherySounds.SPECTRE_HIT.get() : WitcherySounds.SPECTRE_DEATH.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return kind == Kind.BANSHEE ? WitcherySounds.SPECTRE_HIT.get() : WitcherySounds.SPECTRE_DEATH.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return super.getAmbientSoundInterval() * 3;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("Summoner", summoner);
        tag.putBoolean("Obscured", obscured());
        tag.putInt("SuicideIn", lifeTicks);
        if (sentinelTarget != null) tag.putUUID("SentinelTarget", sentinelTarget);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        summoner = tag.getString("Summoner");
        if (!summoner.isEmpty()) setPersistenceRequired();
        entityData.set(OBSCURED, tag.getBoolean("Obscured"));
        lifeTicks = tag.contains("SuicideIn", Tag.TAG_ANY_NUMERIC) ? tag.getInt("SuicideIn") : -1;
        sentinelTarget = tag.hasUUID("SentinelTarget") ? tag.getUUID("SentinelTarget") : null;
        if (sentinelTargeting != null) {
            targetSelector.removeGoal(sentinelTargeting);
            sentinelTargeting = null;
        }
    }
}
