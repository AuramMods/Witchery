package art.arcane.witchery.entity;

import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcherySounds;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class WitchToad extends WitchPet {
    private static final Ingredient FOOD = Ingredient.of(Items.ROTTEN_FLESH);
    private boolean poisonous;

    public WitchToad(EntityType<? extends WitchToad> type, Level level) {
        super(type, level);
    }

    public void prepareRainToad() {
        preparePoisonousToad(30);
    }

    public void preparePoisonousToad(int ticks) {
        timeToLive = ticks;
        poisonous = true;
        // The original constructor draws dye metadata and converts it to the wool color order.
        setColor(15 - level().random.nextInt(16));
    }

    public static AttributeSupplier.Builder attributes() {
        return createMobAttributes().add(Attributes.MAX_HEALTH, 10).add(Attributes.MOVEMENT_SPEED, 0.2);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new GroundFamiliarGoals.Sit(this));
        goalSelector.addGoal(3, new GroundFamiliarGoals.Follow(this, 2));
        goalSelector.addGoal(4, new TemptGoal(this, 1.25, FOOD, false));
        goalSelector.addGoal(6, new BreedGoal(this, 1));
        goalSelector.addGoal(7, new RandomStrollGoal(this, 1));
        goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 8));
        goalSelector.addGoal(9, new RandomLookAroundGoal(this));
    }

    @Override
    protected PathNavigation createNavigation(Level level) {
        GroundPathNavigation navigation = new GroundPathNavigation(this, level);
        navigation.setCanFloat(true);
        return navigation;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!level().isClientSide && !isOrderedToSit() && onGround() && !isInWater()
                && getDeltaMovement().horizontalDistanceSqr() > 0.0001) {
            getJumpControl().jump();
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        Entity attacker = source.getEntity();
        if (attacker != null && !(attacker instanceof Player) && !(attacker instanceof AbstractArrow)) {
            amount = (amount + 1) / 2;
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return FOOD.test(stack);
    }

    @Override
    protected boolean canToggleSit(ItemStack held) {
        return !bypassesSitCommand(held);
    }

    @Override
    protected boolean mayHandleFoodAndDye(Player player) {
        return true;
    }

    @Override
    public boolean canMate(Animal other) {
        return other instanceof WitchToad toad && isTame() && toad.isTame()
                && !toad.isOrderedToSit() && super.canMate(other);
    }

    @Nullable
    @Override
    public WitchToad getBreedOffspring(ServerLevel level, AgeableMob other) {
        WitchToad baby = WitcheryEntities.TOAD.get().create(level);
        if (baby != null) {
            baby.setColor(color());
            baby.setPersistenceRequired();
        }
        return baby;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType cause,
                                        @Nullable SpawnGroupData group, @Nullable CompoundTag data) {
        setColor(random.nextInt(16));
        return super.finalizeSpawn(level, difficulty, cause, group, data);
    }

    @Override
    protected String colorKey() {
        return "SkinColor";
    }

    @Override
    protected Item naturalDrop() {
        return Items.SLIME_BALL;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag data) {
        super.addAdditionalSaveData(data);
        data.putBoolean("Poisonous", poisonous);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag data) {
        super.readAdditionalSaveData(data);
        poisonous = data.getBoolean("Poisonous");
    }

    @Override
    protected void expire(ServerLevel server) {
        if (poisonous) {
            for (LivingEntity target : server.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(3, 2, 3),
                    entity -> entity != this && distanceToSqr(entity) < 9)) {
                target.addEffect(new MobEffectInstance(MobEffects.POISON, 60));
            }
        }
        WitcheryNetwork.sendToadExpiration(server, position(), poisonous);
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        playSound(SoundEvents.SLIME_SQUISH_SMALL, 0.15F, 1);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return WitcherySounds.TOAD_CROAK.get();
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return WitcherySounds.TOAD_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return WitcherySounds.TOAD_HURT.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return super.getAmbientSoundInterval() * 2;
    }

    @Override
    protected float getSoundVolume() {
        return 0.4F;
    }
}
