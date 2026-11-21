package art.arcane.witchery.entity;

import art.arcane.witchery.block.entity.MirrorBlockEntity;
import art.arcane.witchery.infusion.SymbolSpell;
import art.arcane.witchery.item.HandBowItem;
import art.arcane.witchery.item.MysticBranchItem;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.lycanthropy.Lycanthropy;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcherySounds;
import art.arcane.witchery.vampirism.Vampirism;
import art.arcane.witchery.world.MirrorDisguise;
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
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RangedAttackGoal;
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
import net.minecraft.world.level.ServerLevelAccessor;
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
    private static final EntityDataAccessor<String> OWNER_SKIN =
            SynchedEntityData.defineId(Reflection.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Boolean> WOLFMAN_MODEL =
            SynchedEntityData.defineId(Reflection.class, EntityDataSerializers.BOOLEAN);
    private static final EquipmentSlot[] ARMOR = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private final ServerBossEvent boss = new ServerBossEvent(Component.translatable("entity.witchery.reflection"),
            BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);
    @Nullable private BlockPos roomExit;
    private boolean freeSpawn;
    private int lifetime = -1;
    private boolean vampire;
    private int attackTicks;

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
                || !level.getEntitiesOfClass(Reflection.class, roomBounds(door),
                        reflection -> !reflection.freeSpawn && reflection.isAlive()).isEmpty()) return;
        Reflection reflection = WitcheryEntities.REFLECTION.get().create(level);
        if (reflection == null) return;
        reflection.roomExit = door;
        reflection.setPersistenceRequired();
        reflection.moveTo(door.getX() + 4.5, door.getY() - 1.9, door.getZ() + 0.5, 90, 0);
        reflection.copyOwner(player, true);
        reflection.setTarget(player);
        level.addFreshEntity(reflection);
    }

    public void allowOutsideMirror() {
        freeSpawn = true;
        setPersistenceRequired();
    }

    @Override
    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason,
                                        @Nullable SpawnGroupData data, @Nullable CompoundTag tag) {
        allowOutsideMirror();
        return super.finalizeSpawn(level, difficulty, reason, data, tag);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(OWNER_ID, Optional.empty());
        entityData.define(OWNER_NAME, "");
        entityData.define(OWNER_SKIN, "");
        entityData.define(WOLFMAN_MODEL, false);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new RangedBowAttackGoal<Reflection>(this, 1, 22, 20) {
            @Override
            protected boolean isHoldingBow() {
                return getMainHandItem().getItem() instanceof BowItem && !holdsModRangedWeapon() && super.isHoldingBow();
            }
        });
        goalSelector.addGoal(2, new RangedAttackGoal(this, 1, 20, 60, 15) {
            @Override
            public boolean canUse() {
                return holdsModRangedWeapon() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return holdsModRangedWeapon() && super.canContinueToUse();
            }
        });
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1, true) {
            @Override
            public boolean canUse() {
                return !holdsRangedWeapon() && super.canUse();
            }

            @Override
            public boolean canContinueToUse() {
                return !holdsRangedWeapon() && super.canContinueToUse();
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

    public String ownerSkin() { return entityData.get(OWNER_SKIN); }
    public boolean hasWolfmanModel() { return entityData.get(WOLFMAN_MODEL); }
    public boolean isVampireCopy() { return vampire; }
    public int attackTicks() { return attackTicks; }

    private boolean holdsModRangedWeapon() {
        return getMainHandItem().getItem() instanceof MysticBranchItem || getMainHandItem().getItem() instanceof HandBowItem;
    }

    private boolean holdsRangedWeapon() {
        return getMainHandItem().getItem() instanceof BowItem || holdsModRangedWeapon();
    }

    @Override
    protected Component getTypeName() {
        String name = entityData.get(OWNER_NAME);
        return name.isEmpty() ? super.getTypeName() : Component.translatable("entity.witchery.reflection.owner", name);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (attackTicks > 0) attackTicks--;
        if (!(level() instanceof ServerLevel level) || !isAlive()) return;
        if (tickCount % 30 == 1) {
            if (!freeSpawn && !level.dimension().equals(WitcheryDimensions.MIRROR) || lifetime > 0 && --lifetime == 0) {
                discard();
                return;
            }
            AABB bounds = roomExit == null ? getBoundingBox().inflate(10, 8, 10) : roomBounds(roomExit);
            List<ServerPlayer> players = level.getEntitiesOfClass(ServerPlayer.class, bounds,
                    player -> player.isAlive() && !player.isSpectator());
            ServerPlayer owner = players.stream().filter(this::matchesOwner)
                    .findFirst().orElseGet(() -> players.stream().min(Comparator.comparingDouble(this::distanceToSqr)).orElse(null));
            copyOwner(owner, tickCount % 60 == 1);
            boss.setName(getDisplayName());
            for (ServerPlayer viewer : List.copyOf(boss.getPlayers())) {
                if (!players.contains(viewer)) boss.removePlayer(viewer);
            }
            players.forEach(boss::addPlayer);
            LivingEntity enemy = getTarget();
            if (enemy != null && getNavigation().isDone() && hasLineOfSight(enemy)) {
                castAt(level, enemy, 1, SymbolSpell.ATTRAHO);
            }
        }
        LivingEntity enemy = getTarget();
        if (enemy != null && (!enemy.onGround() || enemy instanceof Player player && player.getAbilities().flying)
                && !enemy.hasEffect(MobEffects.MOVEMENT_SLOWDOWN) && random.nextFloat() < 0.05F) {
            enemy.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 5));
        }
        boss.setProgress(getHealth() / getMaxHealth());
    }

    private boolean matchesOwner(ServerPlayer player) {
        return ownerId().map(player.getUUID()::equals)
                .orElseGet(() -> entityData.get(OWNER_NAME).equalsIgnoreCase(player.getGameProfile().getName()));
    }

    private void copyOwner(@Nullable ServerPlayer owner, boolean copyEffects) {
        entityData.set(OWNER_ID, owner == null ? Optional.empty() : Optional.of(owner.getUUID()));
        entityData.set(OWNER_NAME, owner == null ? "" : owner.getGameProfile().getName());
        String disguise = owner == null ? "" : MirrorDisguise.alias(owner);
        entityData.set(OWNER_SKIN, disguise.isEmpty() ? entityData.get(OWNER_NAME) : disguise);
        entityData.set(WOLFMAN_MODEL, owner != null && Lycanthropy.isWolfman(owner));
        vampire = owner != null && Vampirism.isVampire(owner);
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(hasWolfmanModel() ? 6 : 2);
        for (EquipmentSlot slot : ARMOR) {
            setItemSlot(slot, owner == null ? ItemStack.EMPTY : owner.getItemBySlot(slot).copyWithCount(1));
        }
        ItemStack weapon = ItemStack.EMPTY;
        if (owner != null) {
            if (!hasWolfmanModel()) weapon = copiedWeapon(owner);
            if (copyEffects) {
                removeAllEffects();
                owner.getActiveEffects().forEach(effect -> addEffect(new MobEffectInstance(effect)));
            }
        }
        setItemSlot(EquipmentSlot.MAINHAND, weapon.copyWithCount(1));
    }

    private static ItemStack copiedWeapon(ServerPlayer owner) {
        ItemStack weapon = owner.getMainHandItem();
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
        return weapon;
    }

    @Override
    public boolean hurt(DamageSource source, float damage) {
        return super.hurt(source, Math.min(6, damage));
    }

    @Override
    public void performRangedAttack(LivingEntity target, float strength) {
        if (!(level() instanceof ServerLevel level) || !holdsRangedWeapon()) return;
        startAttackAnimation();
        if (getMainHandItem().getItem() instanceof MysticBranchItem) {
            swing(InteractionHand.MAIN_HAND);
            if (random.nextBoolean()) castAt(level, target, strength, switch (random.nextInt(21)) {
                case 14, 15 -> SymbolSpell.EXPELLIARMUS;
                case 16, 17 -> SymbolSpell.FLIPENDO;
                case 18, 19 -> SymbolSpell.IMPEDIMENTA;
                case 20 -> SymbolSpell.CONFUNDUS;
                default -> SymbolSpell.IGNIANIMA;
            });
            return;
        }
        boolean handBow = getMainHandItem().getItem() instanceof HandBowItem;
        AbstractArrow arrow = handBow ? copiedBolt(target, strength)
                : ProjectileUtil.getMobArrow(this, new ItemStack(Items.ARROW), strength);
        double x = target.getX() - getX();
        double z = target.getZ() - getZ();
        double y = target.getY(0.33) - arrow.getY() + Math.sqrt(x * x + z * z) * 0.2;
        arrow.pickup = AbstractArrow.Pickup.DISALLOWED;
        arrow.shoot(x, y, z, 1.6F, 14 - level.getDifficulty().getId() * (handBow ? 4 : 3));
        playSound(SoundEvents.ARROW_SHOOT, 1, 1 / (random.nextFloat() * 0.4F + 0.8F));
        level.addFreshEntity(arrow);
    }

    private WitcheryBolt copiedBolt(LivingEntity target, float strength) {
        WitcheryBolt.Kind kind = WitcheryBolt.Kind.STAKE;
        if (isShiftedWerewolf(target)) kind = WitcheryBolt.Kind.SILVER;
        else if (CreatureAlignment.undead(target)) kind = WitcheryBolt.Kind.HOLY;
        else if (random.nextInt(4) == 0) kind = WitcheryBolt.Kind.POWERED_ANTI_MAGIC;
        WitcheryBolt bolt = new WitcheryBolt(this, kind);
        bolt.setBaseDamage(Math.max(0, strength * 2 + random.nextGaussian() * 0.25 + level().getDifficulty().getId() * 0.11));
        bolt.applyBowEnchantments(getMainHandItem());
        if (Vampirism.isVampire(target) && random.nextInt(3) == 0) bolt.setSecondsOnFire(100);
        return bolt;
    }

    private static boolean isShiftedWerewolf(LivingEntity entity) {
        return entity instanceof Wolfman || entity instanceof Reflection reflection && reflection.hasWolfmanModel()
                || entity instanceof Player player && Lycanthropy.isShifted(player);
    }

    private void castAt(ServerLevel level, LivingEntity target, float strength, SymbolSpell spell) {
        Vec3 origin = position().add(0, getBbHeight() / 2, 0);
        float spread = (float) Math.sqrt(strength) * 0.5F;
        Vec3 aim = target.position().add(random.nextGaussian() * spread, target.getBbHeight() / 2,
                random.nextGaussian() * spread).subtract(origin);
        if (level.addFreshEntity(new SymbolProjectile(level, this, spell, origin, aim))) {
            playSound(SoundEvents.GHAST_SHOOT, 1, 1);
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        startAttackAnimation();
        return super.doHurtTarget(target);
    }

    private void startAttackAnimation() {
        attackTicks = 10;
        level().broadcastEntityEvent(this, (byte) 4);
    }

    @Override
    public void handleEntityEvent(byte event) {
        if (event == 4) attackTicks = 10;
        else super.handleEntityEvent(event);
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        spawnAtLocation(WitcheryIngredient.DEMON_HEART.item());
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        boss.removeAllPlayers();
        if (!freeSpawn && level() instanceof ServerLevel level && level.dimension().equals(WitcheryDimensions.MIRROR)) {
            BlockPos exit = roomExit == null ? legacyRoomExit(blockPosition()) : roomExit;
            if (level.getEntitiesOfClass(Reflection.class, roomBounds(exit), other -> other != this && !other.freeSpawn && other.isAlive()).isEmpty()) {
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
    protected SoundEvent getAmbientSound() {
        return WitcherySounds.REFLECTION_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return WitcherySounds.REFLECTION_HIT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return WitcherySounds.REFLECTION_DEATH.get();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("Owner", entityData.get(OWNER_NAME));
        tag.putString("OwnerSkin", ownerSkin());
        ownerId().ifPresent(id -> tag.putUUID("OwnerUUID", id));
        if (roomExit != null) tag.putLong("RoomExit", roomExit.asLong());
        tag.putBoolean("FreeSpawn", freeSpawn);
        tag.putInt("LivingTicks", lifetime);
        tag.putInt("Model", hasWolfmanModel() ? 1 : 0);
        tag.putBoolean("Vampire", vampire);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(OWNER_NAME, tag.getString("Owner"));
        entityData.set(OWNER_SKIN, tag.contains("OwnerSkin") ? tag.getString("OwnerSkin") : tag.getString("Owner"));
        entityData.set(OWNER_ID, tag.hasUUID("OwnerUUID") ? Optional.of(tag.getUUID("OwnerUUID")) : Optional.empty());
        roomExit = tag.contains("RoomExit") ? BlockPos.of(tag.getLong("RoomExit")) : null;
        freeSpawn = tag.getBoolean("FreeSpawn");
        lifetime = tag.contains("LivingTicks") ? tag.getInt("LivingTicks") : -1;
        entityData.set(WOLFMAN_MODEL, tag.getInt("Model") == 1);
        vampire = tag.getBoolean("Vampire");
        getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(hasWolfmanModel() ? 6 : 2);
    }
}
