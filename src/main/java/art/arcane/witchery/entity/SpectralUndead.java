package art.arcane.witchery.entity;

import art.arcane.witchery.infusion.InfernalInfusion;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryDamageTypes;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcheryItems;
import art.arcane.witchery.registry.WitcherySounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.Container;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
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
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import java.util.Collections;
import java.util.List;

public final class SpectralUndead extends Monster {
    private static final EntityDataAccessor<Boolean> SCREAMING =
            SynchedEntityData.defineId(SpectralUndead.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> OBSCURED =
            SynchedEntityData.defineId(SpectralUndead.class, EntityDataSerializers.BOOLEAN);
    private static final int HAUNT_RADIUS = 16;
    private static final int[] SPAWN_HEIGHTS = {0, 1, -1, 2, -2, 3, -3, 4, -4, 5, -5, 6, -6, 7, -7, 8, -8};

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

    private final Kind kind;
    private String summoner = "";
    private int lifeTicks = -1;
    private int throwAnimation;

    public SpectralUndead(EntityType<? extends SpectralUndead> type, Level level, Kind kind) {
        super(type, level);
        this.kind = kind;
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, kind == Kind.BANSHEE ? 0.3 : 1, kind == Kind.SPECTRE));
        if (kind == Kind.SPECTRE) goalSelector.addGoal(3, new MoveTowardsTargetGoal(this, 1, 32));
        if (kind == Kind.POLTERGEIST && getNavigation() instanceof GroundPathNavigation navigation) {
            navigation.setCanOpenDoors(true);
            goalSelector.addGoal(3, new OpenDoorGoal(this, true));
        }
        goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 1));
        goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 8));
        goalSelector.addGoal(6, new RandomLookAroundGoal(this));

        HurtByTargetGoal retaliation = new HurtByTargetGoal(this);
        targetSelector.addGoal(2, kind == Kind.SPECTRE ? retaliation : retaliation.setAlertOthers());
        if (kind != Kind.POLTERGEIST) {
            targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, Player.class, 0, true, false,
                    target -> !InfernalInfusion.owns(this, target)));
        }
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
    public static SpectralUndead summonNear(ServerLevel level, BlockPos origin, Kind kind, int minDistance, int maxDistance) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) return null;
        SpectralUndead mob = kind.type().create(level);
        if (mob == null) return null;

        List<BlockPos> columns = new ArrayList<>();
        for (int x = -maxDistance; x <= maxDistance; x++) {
            for (int z = -maxDistance; z <= maxDistance; z++) {
                int distance = Math.max(Math.abs(x), Math.abs(z));
                if (distance >= minDistance) columns.add(origin.offset(x, 0, z));
            }
        }
        // Shuffle with the world random source so valid columns retain equal odds.
        for (int index = columns.size() - 1; index > 0; index--) {
            Collections.swap(columns, index, level.random.nextInt(index + 1));
        }
        for (BlockPos column : columns) {
            for (int y : SPAWN_HEIGHTS) {
                if (mob.trySpawnAt(level, column.above(y))) return mob;
            }
        }
        return null;
    }

    private boolean trySpawnAt(ServerLevel level, BlockPos pos) {
        if (!level.isLoaded(pos) || !level.getWorldBorder().isWithinBounds(pos)
                || !level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP)) return false;
        moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, random.nextFloat() * 360, 0);
        if (!level.noCollision(this) || level.containsAnyLiquid(getBoundingBox())) return false;
        initializeAppearance();
        setPersistenceRequired();
        if (!level.addFreshEntity(this)) return false;
        level.sendParticles(ParticleTypes.INSTANT_EFFECT, getX(), getY(0.5), getZ(), 16, 0.5, 0.5, 0.5, 0.03);
        return true;
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
                                        @Nullable SpawnGroupData group, @Nullable CompoundTag tag) {
        var result = super.finalizeSpawn(level, difficulty, reason, group, tag);
        initializeAppearance();
        return result;
    }

    private void initializeAppearance() {
        if (kind == Kind.SPECTRE) entityData.set(OBSCURED, true);
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
    public boolean canAttack(LivingEntity target) {
        return !InfernalInfusion.owns(this, target) && super.canAttack(target);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return super.hurt(source, Math.min(amount, 15));
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (kind == Kind.BANSHEE) return false;
        if (kind == Kind.SPECTRE && target instanceof LivingEntity living) {
            return canAttack(living) && spectralTouch(living, 0.15F);
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
        if (temporary() && (--lifeTicks <= 0 || getTarget() == null || !getTarget().isAlive())) {
            level.sendParticles(ParticleTypes.POOF, getX(), getY(0.5), getZ(), 16, 0.4, 0.5, 0.4, 0.05);
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
            for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(6),
                    candidate -> candidate.isAlive() && distanceToSqr(candidate) <= 36
                            && (candidate instanceof Player || candidate == getTarget() || candidate == getLastHurtByMob()))) {
                nearby = true;
                if (!target.getItemBySlot(EquipmentSlot.HEAD).is(WitcheryItems.getRequired("earmuffs").get())) {
                    spectralTouch(target, 0.1F);
                }
            }
            entityData.set(SCREAMING, nearby);
        }
        if (screaming() && (!wasScreaming || tickCount % 60 == 0)) {
            playSound(WitcherySounds.BANSHEE_SCREAM.get(), 1, 0.7F + random.nextFloat() * 0.3F);
        }
    }

    private void haunt(ServerLevel level) {
        AABB area = getBoundingBox().inflate(HAUNT_RADIUS);
        boolean grief = ForgeEventFactory.getMobGriefingEvent(level, this);
        if (grief) {
            for (HangingEntity hanging : level.getEntitiesOfClass(HangingEntity.class, area, target -> distanceToSqr(target) <= 256)) {
                if (approach(hanging.position())) {
                    hanging.hurt(damageSources().mobAttack(this), 3);
                    level.broadcastEntityEvent(this, (byte) 4);
                }
                return;
            }

            Player owner = summoner.isEmpty() ? null : level.getServer().getPlayerList().getPlayerByName(summoner);
            if (owner != null && owner.level() == level && distanceToSqr(owner) <= 64 && hauntContainer(level)) return;
        }

        for (ItemEntity dropped : level.getEntitiesOfClass(ItemEntity.class, area, target -> distanceToSqr(target) <= 256)) {
            if (approach(dropped.position())) {
                dropped.setDeltaMovement(throwVelocity());
                dropped.hurtMarked = true;
                level.broadcastEntityEvent(this, (byte) 4);
            }
            return;
        }
    }

    private boolean hauntContainer(ServerLevel level) {
        BlockEntity closest = null;
        double nearest = HAUNT_RADIUS * HAUNT_RADIUS;
        for (int cx = SectionPos.blockToSectionCoord(getBlockX() - HAUNT_RADIUS);
             cx <= SectionPos.blockToSectionCoord(getBlockX() + HAUNT_RADIUS); cx++) {
            for (int cz = SectionPos.blockToSectionCoord(getBlockZ() - HAUNT_RADIUS);
                 cz <= SectionPos.blockToSectionCoord(getBlockZ() + HAUNT_RADIUS); cz++) {
                LevelChunk chunk = level.getChunkSource().getChunkNow(cx, cz);
                if (chunk == null) continue;
                for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                    if (!(blockEntity instanceof Container inventory) || inventory.isEmpty() || blockEntity.isRemoved()) continue;
                    double distance = distanceToSqr(Vec3.atCenterOf(blockEntity.getBlockPos()));
                    if (distance <= nearest) {
                        nearest = distance;
                        closest = blockEntity;
                    }
                }
            }
        }
        if (!(closest instanceof Container inventory)) return false;
        Vec3 position = Vec3.atCenterOf(closest.getBlockPos());
        if (!approach(position)) return true;

        List<Integer> slots = new ArrayList<>();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (!inventory.getItem(slot).isEmpty()) slots.add(slot);
        }
        if (slots.isEmpty()) return false;
        int slot = slots.get(random.nextInt(slots.size()));
        ItemStack original = inventory.getItem(slot).copy();
        ItemEntity dropped = new ItemEntity(level, position.x, position.y, position.z, original.copyWithCount(1));
        dropped.setDeltaMovement(throwVelocity());
        dropped.lifespan = 18000;
        if (!level.addFreshEntity(dropped)) return true;
        if (closest.isRemoved() || !ItemStack.matches(inventory.getItem(slot), original)) {
            dropped.discard();
            return true;
        }
        inventory.removeItem(slot, 1);
        inventory.setChanged();
        level.broadcastEntityEvent(this, (byte) 4);
        return true;
    }

    private boolean approach(Vec3 target) {
        if (distanceToSqr(target) <= 9) return true;
        getNavigation().moveTo(target.x, target.y, target.z, 1);
        return false;
    }

    private Vec3 throwVelocity() {
        return new Vec3(random.nextDouble() * 0.6 - 0.3, random.nextDouble() * 0.2 + 0.1,
                random.nextDouble() * 0.6 - 0.3);
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
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        summoner = tag.getString("Summoner");
        if (!summoner.isEmpty()) setPersistenceRequired();
        entityData.set(OBSCURED, tag.getBoolean("Obscured"));
        lifeTicks = tag.contains("SuicideIn", Tag.TAG_ANY_NUMERIC) ? tag.getInt("SuicideIn") : -1;
    }
}
