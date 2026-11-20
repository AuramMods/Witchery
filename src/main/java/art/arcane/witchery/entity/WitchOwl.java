package art.arcane.witchery.entity;

import art.arcane.witchery.item.WaystoneItem;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcheryItems;
import art.arcane.witchery.registry.WitcherySounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.FollowOwnerGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.SitWhenOrderedToGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomFlyingGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

public final class WitchOwl extends WitchPet {
    private static final Ingredient FOOD = Ingredient.of(Items.PORKCHOP, Items.BEEF);
    private static final EntityDataAccessor<Boolean> DELIVERING = SynchedEntityData.defineId(WitchOwl.class, EntityDataSerializers.BOOLEAN);
    private final OwlDeliveryGoal delivery = new OwlDeliveryGoal(this);

    public WitchOwl(EntityType<? extends WitchOwl> type, Level level) {
        super(type, level);
        moveControl = new FlyingMoveControl(this, 20, false);
        goalSelector.addGoal(2, delivery);
    }

    public static AttributeSupplier.Builder attributes() {
        return createMobAttributes().add(Attributes.MAX_HEALTH, 10).add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FLYING_SPEED, 0.4).add(Attributes.ATTACK_DAMAGE, 4).add(Attributes.ARMOR, 1);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(DELIVERING, false);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new SitWhenOrderedToGoal(this) {
            @Override
            public boolean canUse() {
                return isTame() && isOrderedToSit();
            }
        });
        goalSelector.addGoal(3, new MeleeAttackGoal(this, 1, true));
        goalSelector.addGoal(4, new TemptGoal(this, 0.6, FOOD, true));
        goalSelector.addGoal(5, new FollowOwnerGoal(this, 1, 14, 5, true));
        goalSelector.addGoal(8, new BreedGoal(this, 0.8));
        goalSelector.addGoal(10, new WaterAvoidingRandomFlyingGoal(this, 0.8));
        goalSelector.addGoal(11, new LookAtPlayerGoal(this, Player.class, 10, 0.2F));
        targetSelector.addGoal(1, new OwnerHurtByTargetGoal(this));
        targetSelector.addGoal(2, new OwnerHurtTargetGoal(this));
        targetSelector.addGoal(3, new HurtByTargetGoal(this).setAlertOthers());
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        var flying = new FlyingPathNavigation(this, level);
        flying.setCanOpenDoors(false);
        flying.setCanFloat(true);
        flying.setCanPassDoors(true);
        return flying;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        return target.hurt(damageSources().mobAttack(this), 2);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return FOOD.test(stack);
    }

    @Override
    public boolean canMate(net.minecraft.world.entity.animal.Animal other) {
        return other instanceof WitchOwl owl && isTame() && owl.isTame() && super.canMate(other);
    }

    @Nullable
    @Override
    public WitchOwl getBreedOffspring(ServerLevel level, AgeableMob other) {
        WitchOwl fledgling = WitcheryEntities.OWL.get().create(level);
        if (fledgling != null) {
            fledgling.setColor(color());
            fledgling.setPersistenceRequired();
        }
        return fledgling;
    }

    @Override
    protected boolean canTameFrom(Player player) {
        return distanceToSqr(player) < 9;
    }

    public void beginRescue(LivingEntity attacker) {
        timeToLive = 300;
        setTarget(attacker);
        setLastHurtByMob(attacker);
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (isFamiliar() && isOrderedToSit() && player.isShiftKeyDown()) return super.mobInteract(player, hand);
        if (!isTemporary() && isOwnedBy(player) && !isFood(held) && !(held.getItem() instanceof DyeItem)
                && !held.is(Items.NAME_TAG) && !held.is(WitcheryItems.getRequired("polynesiacharm").get())
                && !held.is(WitcheryItems.getRequired("devilstonguecharm").get())) {
            ItemStack carried = getMainHandItem();
            if (!carried.isEmpty() && WaystoneItem.isBoundWaystone(held)) {
                if (!level().isClientSide && !delivery.assign(held)) {
                    player.displayClientMessage(Component.translatable("message.witchery.owl.no_waypoint"), true);
                }
                return InteractionResult.sidedSuccess(level().isClientSide);
            }
            if (entityData.get(DELIVERING)) return super.mobInteract(player, hand);
            if (!held.isEmpty() && carried.isEmpty()) {
                if (!level().isClientSide) {
                    ItemStack parcel = player.getAbilities().instabuild ? held.copyWithCount(1) : held.split(1);
                    setItemSlot(EquipmentSlot.MAINHAND, parcel);
                    setDropChance(EquipmentSlot.MAINHAND, 1);
                }
                return InteractionResult.sidedSuccess(level().isClientSide);
            }
            if (!carried.isEmpty() && held.isEmpty()) {
                if (!level().isClientSide) {
                    spawnAtLocation(carried);
                    setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                }
                return InteractionResult.sidedSuccess(level().isClientSide);
            }
        }
        return super.mobInteract(player, hand);
    }

    void setDelivering(boolean active) {
        entityData.set(DELIVERING, active);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        delivery.save(tag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        delivery.load(tag);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType cause,
                                        @Nullable SpawnGroupData group, @Nullable CompoundTag data) {
        setColor(random.nextInt(100) == 0 ? 0 : random.nextInt(15) + 1);
        return super.finalizeSpawn(level, difficulty, cause, group, data);
    }

    @Override
    protected String colorKey() {
        return "FeatherColor";
    }

    @Override
    protected Item naturalDrop() {
        return Items.FEATHER;
    }

    @Override
    protected double naturalArmor() {
        return 1;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return WitcherySounds.OWL_HOOT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return WitcherySounds.OWL_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return WitcherySounds.OWL_HURT.get();
    }

    @Override
    protected boolean shouldExpireEarly() {
        return getTarget() == null || !getTarget().isAlive();
    }

    @Override
    protected void expire(ServerLevel server) {
        server.sendParticles(ParticleTypes.POOF, getX(), getY(0.5), getZ(), 16, 0.25, 0.25, 0.25, 0.04);
    }
}
