package art.arcane.witchery.entity;

import art.arcane.witchery.item.TaglockItem;
import art.arcane.witchery.item.WaystoneItem;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcheryItems;
import art.arcane.witchery.registry.WitcherySounds;
import art.arcane.witchery.world.migration.LegacyGameProfiles;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.TamableAnimal;
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
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.Nullable;

public final class WingedMonkey extends TamableAnimal {
    private static final Ingredient FOOD = Ingredient.of(Items.PORKCHOP, Items.BEEF);
    private static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(WingedMonkey.class, EntityDataSerializers.INT);
    private final MonkeyFlightGoal flight = new MonkeyFlightGoal(this);
    private int attackTicks;
    private int timeToLive = -1;

    public WingedMonkey(EntityType<? extends WingedMonkey> type, Level level) {
        super(type, level);
        moveControl = new FlyingMoveControl(this, 20, false);
        goalSelector.addGoal(2, flight);
    }

    public static AttributeSupplier.Builder attributes() {
        return createMobAttributes().add(Attributes.MAX_HEALTH, 20).add(Attributes.MOVEMENT_SPEED, 0.3)
                .add(Attributes.FLYING_SPEED, 0.4).add(Attributes.ATTACK_DAMAGE, 4).add(Attributes.ARMOR, 5);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(COLOR, 0);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new SitWhenOrderedToGoal(this) {
            @Override
            public boolean canUse() {
                return (!flight.active() || isOrderedToSit()) && super.canUse();
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
        var navigation = new FlyingPathNavigation(this, level);
        navigation.setCanFloat(true);
        navigation.setCanPassDoors(true);
        return navigation;
    }

    public int color() {
        return entityData.get(COLOR);
    }

    private void setColor(int color) {
        entityData.set(COLOR, color & 15);
    }

    public int attackTicks() {
        return attackTicks;
    }

    boolean isTemporary() {
        return timeToLive >= 0;
    }

    @Override
    public boolean isOwnedBy(LivingEntity owner) {
        return owner.getUUID().equals(getOwnerUUID());
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        boolean client = level().isClientSide;
        if (isTemporary()) return InteractionResult.sidedSuccess(client);
        if (!isTame() && isFood(held) && distanceToSqr(player) < 9) {
            if (!client) {
                consume(player, held);
                boolean tamed = random.nextInt(3) == 0 && !ForgeEventFactory.onAnimalTame(this, player);
                if (tamed) {
                    tame(player);
                    setTarget(null);
                    setOrderedToSit(true);
                    getNavigation().stop();
                }
                level().broadcastEntityEvent(this, (byte) (tamed ? 7 : 6));
            }
            return InteractionResult.sidedSuccess(client);
        }
        if (!isOwnedBy(player)) return super.mobInteract(player, hand);
        if (isFood(held)) {
            if (getHealth() >= getMaxHealth()) return super.mobInteract(player, hand);
            if (!client) {
                heal(10);
                consume(player, held);
            }
            return InteractionResult.sidedSuccess(client);
        }
        if (held.getItem() instanceof DyeItem dye) {
            if (!client) {
                setColor(dye.getDyeColor().getId());
                consume(player, held);
            }
            return InteractionResult.sidedSuccess(client);
        }
        if (held.is(Items.NAME_TAG) || held.is(WitcheryItems.getRequired("polynesiacharm").get())
                || held.is(WitcheryItems.getRequired("devilstonguecharm").get())) return InteractionResult.PASS;

        boolean taglock = held.getItem() instanceof TaglockItem && !TaglockItem.boundName(held).isEmpty();
        if (taglock || WaystoneItem.isBoundWaystone(held)) {
            if (!client) {
                if (!flight.assign(player, held, taglock)) {
                    player.displayClientMessage(Component.translatable("message.witchery.wingedmonkey.no_waypoint"), true);
                } else if (taglock) consume(player, held);
            }
            return InteractionResult.sidedSuccess(client);
        }
        if (distanceToSqr(player) >= 9) return InteractionResult.PASS;
        if (!client) {
            if (flight.active() && !isOrderedToSit()) {
                flight.finish();
            } else {
                setOrderedToSit(!isOrderedToSit());
                setTarget(null);
                getNavigation().stop();
            }
        }
        return InteractionResult.sidedSuccess(client);
    }

    private static void consume(Player player, ItemStack item) {
        if (!player.getAbilities().instabuild) item.shrink(1);
    }

    public boolean hasActiveFlight() {
        return isAlive() && !isNoAi() && flight.canUse();
    }

    @Override
    public boolean isFood(ItemStack item) {
        return FOOD.test(item);
    }

    @Override
    public boolean canMate(Animal other) {
        return other instanceof WingedMonkey monkey && isTame() && monkey.isTame() && super.canMate(other);
    }

    @Nullable
    @Override
    public WingedMonkey getBreedOffspring(ServerLevel level, AgeableMob other) {
        var baby = WitcheryEntities.WINGED_MONKEY.get().create(level);
        if (baby != null) baby.setColor(color());
        return baby;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType cause,
                                        @Nullable SpawnGroupData group, @Nullable CompoundTag data) {
        setColor(random.nextInt(100) == 0 ? 0 : random.nextInt(15) + 1);
        return super.finalizeSpawn(level, difficulty, cause, group, data);
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        level().broadcastEntityEvent(this, (byte) 4);
        return target.hurt(damageSources().mobAttack(this), 2);
    }

    @Override
    public boolean hurt(DamageSource source, float damage) {
        if (isInvulnerableTo(source)) return false;
        setOrderedToSit(false);
        return super.hurt(source, damage);
    }

    @Override
    public void handleEntityEvent(byte event) {
        if (event == 4) attackTicks = 10;
        else super.handleEntityEvent(event);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (attackTicks > 0) attackTicks--;
        if (isTemporary() && level() instanceof ServerLevel server && (--timeToLive <= 0 || getTarget() == null || !getTarget().isAlive())) {
            server.sendParticles(ParticleTypes.POOF, getX(), getY(0.5), getZ(), 16, 0.25, 0.25, 0.25, 0.02);
            discard();
        }
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public double getPassengersRidingOffset() {
        Entity passenger = getFirstPassenger();
        return passenger == null ? 0 : -passenger.getBbHeight() * 0.6;
    }

    @Override
    @Nullable
    public LivingEntity getControllingPassenger() {
        // A captured creature rides beneath the monkey without taking over its movement goals.
        return null;
    }

    @Override
    public boolean shouldRiderSit() {
        return false;
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        if (!isTemporary()) {
            int feathers = random.nextInt(3) + random.nextInt(1 + looting);
            if (feathers > 0) spawnAtLocation(new ItemStack(Items.FEATHER, feathers));
        }
    }

    @Override
    public int getExperienceReward() {
        return 2 * super.getExperienceReward();
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return WitcherySounds.MONKEY_AMBIENT.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return WitcherySounds.MONKEY_HIT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return WitcherySounds.MONKEY_DEATH.get();
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putByte("FeatherColor", (byte) color());
        tag.putInt("SuicideIn", timeToLive);
        flight.save(tag);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(LegacyGameProfiles.withTameOwner(tag));
        setColor(tag.getByte("FeatherColor"));
        timeToLive = tag.contains("SuicideIn", Tag.TAG_ANY_NUMERIC) ? tag.getInt("SuicideIn") : -1;
        flight.load(tag);
    }
}
