package art.arcane.witchery.entity;

import art.arcane.witchery.block.entity.MirrorBlockEntity;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.world.MirrorTravel;
import art.arcane.witchery.world.WitcheryDimensions;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedBowAttackGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class Reflection extends Monster implements RangedAttackMob {
    private static final EntityDataAccessor<Optional<UUID>> OWNER_ID =
            SynchedEntityData.defineId(Reflection.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<String> OWNER_NAME =
            SynchedEntityData.defineId(Reflection.class, EntityDataSerializers.STRING);
    private static final EquipmentSlot[] ARMOR = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private final ServerBossEvent boss = new ServerBossEvent(Component.translatable("entity.witchery.reflection"),
            BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);
    @Nullable private BlockPos roomExit;
    private boolean freeSpawn;
    private int lifetime = -1;
    private int legacyModel;
    private boolean legacyVampire;

    public Reflection(EntityType<? extends Reflection> type, Level level) {
        super(type, level);
        xpReward = 50;
        ((GroundPathNavigation) getNavigation()).setCanOpenDoors(true);
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 100)
                .add(Attributes.MOVEMENT_SPEED, 0.35).add(Attributes.FOLLOW_RANGE, 50)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1).add(Attributes.ATTACK_DAMAGE, 2);
    }

    public static void spawnForRoom(ServerLevel level, MirrorBlockEntity exit, ServerPlayer player) {
        BlockPos door = exit.getBlockPos();
        if (exit.demonSlain() || level.getDifficulty() == Difficulty.PEACEFUL
                || !level.getEntitiesOfClass(Reflection.class, roomBounds(door), Reflection::isAlive).isEmpty()) return;
        Reflection reflection = WitcheryEntities.REFLECTION.get().create(level);
        if (reflection == null) return;
        reflection.roomExit = door;
        reflection.setPersistenceRequired();
        reflection.moveTo(door.getX() + 4.5, door.getY() - 1.9, door.getZ() + 0.5, 90, 0);
        reflection.copyOwner(player, true);
        reflection.setTarget(player);
        level.addFreshEntity(reflection);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(OWNER_ID, Optional.empty());
        entityData.define(OWNER_NAME, "");
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new RangedBowAttackGoal<>(this, 1, 22, 20));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1, true) {
            @Override
            public boolean canUse() {
                return !(getMainHandItem().getItem() instanceof BowItem) && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !(getMainHandItem().getItem() instanceof BowItem) && super.canContinueToUse();
            }
        });
        goalSelector.addGoal(3, new WaterAvoidingRandomStrollGoal(this, 1));
        goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 6));
        goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
        targetSelector.addGoal(2, new HurtByTargetGoal(this));
    }

    public Optional<UUID> ownerId() {
        return entityData.get(OWNER_ID);
    }

    @Override
    protected Component getTypeName() {
        String name = entityData.get(OWNER_NAME);
        return name.isEmpty() ? super.getTypeName() : Component.translatable("entity.witchery.reflection.owner", name);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(level() instanceof ServerLevel level) || !isAlive()) return;
        if (tickCount % 30 == 1) {
            if (!freeSpawn && !level.dimension().equals(WitcheryDimensions.MIRROR) || lifetime > 0 && --lifetime == 0) {
                discard();
                return;
            }
            AABB bounds = roomExit == null ? getBoundingBox().inflate(10, 8, 10) : roomBounds(roomExit);
            List<ServerPlayer> players = level.getEntitiesOfClass(ServerPlayer.class, bounds,
                    player -> player.isAlive() && !player.isSpectator());
            ServerPlayer owner = players.stream().filter(player -> ownerId().filter(player.getUUID()::equals).isPresent())
                    .findFirst().orElseGet(() -> players.stream().min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null));
            copyOwner(owner, tickCount % 60 == 1);
            boss.setName(getDisplayName());
            for (ServerPlayer viewer : List.copyOf(boss.getPlayers())) {
                if (!players.contains(viewer)) boss.removePlayer(viewer);
            }
            players.forEach(boss::addPlayer);
            LivingEntity enemy = getTarget();
            if (enemy != null && getNavigation().isDone() && hasLineOfSight(enemy) && distanceToSqr(enemy) > 16) {
                Vec3 pull = position().subtract(enemy.position()).normalize().scale(0.3);
                enemy.push(pull.x, 0.05, pull.z);
                enemy.hurtMarked = true;
            }
        }
        if (getTarget() instanceof Player player && player.getAbilities().flying
                && !player.hasEffect(MobEffects.MOVEMENT_SLOWDOWN) && random.nextFloat() < 0.05F) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 5));
        }
        boss.setProgress(getHealth() / getMaxHealth());
    }

    private void copyOwner(@Nullable ServerPlayer owner, boolean copyEffects) {
        entityData.set(OWNER_ID, owner == null ? Optional.empty() : Optional.of(owner.getUUID()));
        entityData.set(OWNER_NAME, owner == null ? "" : owner.getGameProfile().getName());
        for (EquipmentSlot slot : ARMOR) {
            setItemSlot(slot, owner == null ? ItemStack.EMPTY : owner.getItemBySlot(slot).copyWithCount(1));
        }
        ItemStack weapon = ItemStack.EMPTY;
        if (owner != null) {
            weapon = owner.getMainHandItem();
            double bestDamage = 0;
            for (int slot = 0; slot < 9; slot++) {
                ItemStack candidate = owner.getInventory().getItem(slot);
                double attack = 0;
                for (AttributeModifier mod : candidate.getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_DAMAGE)) {
                    if (mod.getOperation() == AttributeModifier.Operation.ADDITION) attack += mod.getAmount();
                }
                if (attack > bestDamage) {
                    bestDamage = attack;
                    weapon = candidate;
                }
            }
            if (copyEffects) {
                removeAllEffects();
                owner.getActiveEffects().forEach(effect -> addEffect(new MobEffectInstance(effect)));
            }
        }
        setItemSlot(EquipmentSlot.MAINHAND, weapon.copyWithCount(1));
    }

    @Override
    public boolean hurt(DamageSource source, float damage) {
        return super.hurt(source, Math.min(6, damage));
    }

    @Override
    public void performRangedAttack(LivingEntity target, float strength) {
        AbstractArrow arrow = ProjectileUtil.getMobArrow(this, new ItemStack(Items.ARROW), strength);
        double x = target.getX() - getX();
        double z = target.getZ() - getZ();
        double y = target.getY(0.33) - arrow.getY() + Math.sqrt(x * x + z * z) * 0.2;
        arrow.shoot(x, y, z, 1.6F, 14 - level().getDifficulty().getId() * 3);
        playSound(SoundEvents.SKELETON_SHOOT, 1, 1);
        level().addFreshEntity(arrow);
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        spawnAtLocation(WitcheryIngredient.DEMON_HEART.item());
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        boss.removeAllPlayers();
        if (level() instanceof ServerLevel level && level.dimension().equals(WitcheryDimensions.MIRROR)) {
            BlockPos exit = roomExit == null ? legacyRoomExit(blockPosition()) : roomExit;
            if (level.getEntitiesOfClass(Reflection.class, roomBounds(exit), other -> other != this && other.isAlive()).isEmpty()) {
                MirrorTravel.reflectionSlain(level, exit);
            }
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        boss.removeAllPlayers();
        super.remove(reason);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        boss.removePlayer(player);
    }

    private static BlockPos legacyRoomExit(BlockPos pos) {
        return new BlockPos((pos.getX() & ~15) + 4, (pos.getY() & ~15) + 8, (pos.getZ() & ~15) + 8);
    }

    public static AABB roomBounds(BlockPos exit) {
        return new AABB(exit.offset(0, -2, -4), exit.offset(9, 7, 5));
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.PLAYER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.PLAYER_DEATH;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("Owner", entityData.get(OWNER_NAME));
        tag.putString("OwnerSkin", entityData.get(OWNER_NAME));
        ownerId().ifPresent(id -> tag.putUUID("OwnerUUID", id));
        if (roomExit != null) tag.putLong("RoomExit", roomExit.asLong());
        tag.putBoolean("FreeSpawn", freeSpawn);
        tag.putInt("LivingTicks", lifetime);
        tag.putInt("Model", legacyModel);
        tag.putBoolean("Vampire", legacyVampire);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(OWNER_NAME, tag.getString("Owner"));
        entityData.set(OWNER_ID, tag.hasUUID("OwnerUUID") ? Optional.of(tag.getUUID("OwnerUUID")) : Optional.empty());
        roomExit = tag.contains("RoomExit") ? BlockPos.of(tag.getLong("RoomExit")) : null;
        freeSpawn = tag.getBoolean("FreeSpawn");
        lifetime = tag.contains("LivingTicks") ? tag.getInt("LivingTicks") : -1;
        legacyModel = tag.getInt("Model");
        legacyVampire = tag.getBoolean("Vampire");
    }
}
