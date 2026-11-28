package art.arcane.witchery.entity;

import art.arcane.witchery.block.WitchWoodType;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.FakePlayerFactory;
import org.jetbrains.annotations.Nullable;

public final class Ent extends Monster {
    private static final EntityDataAccessor<Boolean> SCREAMING = SynchedEntityData.defineId(Ent.class, EntityDataSerializers.BOOLEAN);
    private String ownerName = "";
    private int attackTicks;

    public Ent(EntityType<? extends Ent> type, Level level) {
        super(type, level);
        xpReward = 25;
    }

    public static AttributeSupplier.Builder attributes() {
        return createMonsterAttributes().add(Attributes.MAX_HEALTH, 200).add(Attributes.MOVEMENT_SPEED, 0.4)
                .add(Attributes.ATTACK_DAMAGE, 4).add(Attributes.KNOCKBACK_RESISTANCE, 1).add(Attributes.FOLLOW_RANGE, 32);
    }

    public static void tryAwaken(ServerLevel level, BlockPos harvested, Block witchLog) {
        double chance = 0.01;
        for (Direction direction : Direction.values()) {
            if (level.getBlockState(harvested.relative(direction)).is(witchLog)) chance += 0.01;
        }
        if (level.random.nextDouble() >= Math.min(chance, 0.05)) return;

        int offsetX = awakeningOffset(level);
        int offsetZ = awakeningOffset(level);
        BlockPos.MutableBlockPos ground = harvested.offset(offsetX, 0, offsetZ).mutable();
        if (!level.hasChunkAt(ground)) return;
        int ceiling = Math.min(harvested.getY() + 8, level.getMaxBuildHeight() - 3);
        while (!level.isEmptyBlock(ground) && ground.getY() < ceiling) {
            ground.move(Direction.UP);
        }
        while (level.isEmptyBlock(ground) && ground.getY() > level.getMinBuildHeight()) {
            ground.move(Direction.DOWN);
        }
        if (!level.getBlockState(ground).isFaceSturdy(level, ground, Direction.UP)) return;
        for (int height = 1; height <= 3; height++) {
            if (!level.isEmptyBlock(ground.above(height))) return;
        }

        Ent ent = WitcheryEntities.ENT.get().create(level);
        if (ent == null) return;
        ent.moveTo(ground.getX() + 0.5, ground.getY() + 1.05, ground.getZ() + 0.5, 0, 0);
        if (!level.noCollision(ent) || !level.addFreshEntity(ent)) return;
        WitcheryNetwork.sendEntAwakening(level, harvested, ent.position());
    }

    private static int awakeningOffset(ServerLevel level) {
        int roll = level.random.nextInt(17);
        return roll > 8 ? roll : roll - 16;
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1, true));
        goalSelector.addGoal(3, new MoveTowardsTargetGoal(this, 0.9, 32));
        goalSelector.addGoal(4, new RandomStrollGoal(this, 0.6));
        goalSelector.addGoal(5, new LookAtPlayerGoal(this, Player.class, 6));
        goalSelector.addGoal(6, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new HurtByTargetGoal(this));
        targetSelector.addGoal(2, new ReachableTargetGoal<>(this, Player.class, false, null));
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(SCREAMING, false);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (attackTicks > 0) attackTicks--;
        if (level() instanceof ServerLevel server && isAlive()) {
            entityData.set(SCREAMING, getTarget() != null);
            if (server.getRandom().nextInt(300) == 0) {
                BoneMealItem.applyBonemeal(new ItemStack(Items.BONE_MEAL), server, blockPosition().below(),
                        FakePlayerFactory.getMinecraft(server));
            }
        } else if (level().isClientSide && getDeltaMovement().horizontalDistanceSqr() > 2.5E-7 && random.nextInt(5) == 0) {
            BlockState ground = level().getBlockState(getOnPos());
            if (!ground.isAir()) {
                level().addParticle(new BlockParticleOption(ParticleTypes.BLOCK, ground).setPos(getOnPos()),
                        getRandomX(1), getY() + 0.1, getRandomZ(1),
                        (random.nextFloat() - 0.5) * 4, 0.5, (random.nextFloat() - 0.5) * 4);
            }
        }
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        attackTicks = 10;
        level().broadcastEntityEvent(this, (byte) 4);
        boolean hit = super.doHurtTarget(target);
        if (hit) {
            target.push(0, 0.4, 0);
        }
        playSound(SoundEvents.IRON_GOLEM_ATTACK, 1, 1);
        return hit;
    }

    @Override
    public void handleEntityEvent(byte event) {
        if (event == 4) attackTicks = 10;
        else super.handleEntityEvent(event);
    }

    public boolean isScreaming() {
        return entityData.get(SCREAMING);
    }

    public int attackTicks() {
        return attackTicks;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        float capped = Math.min(amount, 15);
        if ((source.is(DamageTypes.MOB_ATTACK) || source.is(DamageTypes.PLAYER_ATTACK))
                && source.getEntity() instanceof LivingEntity attacker && attacker.getMainHandItem().getItem() instanceof AxeItem) {
            capped *= 3;
        }
        return super.hurt(source, capped);
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean recentlyHit) {
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        spawnAtLocation(new ItemStack(WitcheryIngredient.ENT_BRANCH.item()));
        WitchWoodType[] woods = WitchWoodType.values();
        spawnAtLocation(woods[level().getRandom().nextInt(woods.length)].stack(WitcheryBlocks.getRequired("witchsapling").get()));
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.ZOMBIE_HORSE_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.ZOMBIE_HORSE_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        playSound(SoundEvents.IRON_GOLEM_STEP, 1, 1);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("Owner", ownerName);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        ownerName = tag.getString("Owner");
        if (!ownerName.isEmpty()) setPersistenceRequired();
    }
}
