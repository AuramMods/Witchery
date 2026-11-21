package art.arcane.witchery.entity;

import art.arcane.witchery.brewing.LousePotion;
import art.arcane.witchery.registry.WitcheryItems;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public final class ParasyticLouse extends Monster {
    private static final EntityDataAccessor<Integer> POTION = SynchedEntityData.defineId(
            ParasyticLouse.class, EntityDataSerializers.INT);

    public ParasyticLouse(EntityType<? extends ParasyticLouse> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return createMonsterAttributes().add(Attributes.MAX_HEALTH, 4).add(Attributes.MOVEMENT_SPEED, 0.6)
                .add(Attributes.ATTACK_DAMAGE, 0).add(Attributes.FOLLOW_RANGE, 8);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(2, new LouseBiteGoal(this));
        goalSelector.addGoal(5, new RandomStrollGoal(this, 0.8));
        goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 8));
        targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, true));
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (!(level() instanceof ServerLevel) || !(target instanceof LivingEntity living) || !living.isAlive()) return false;
        LousePotion potion = LousePotion.byId(bitePotion());
        if (potion == null) return false;
        setBitePotion(0);
        potion.apply(living, this);
        return true;
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (level() instanceof ServerLevel server) {
            var stack = new ItemStack(WitcheryItems.getRequired("louse").get());
            var drop = new ItemEntity(server, getX(), getY() + 0.4, getZ(), stack);
            if (!server.addFreshEntity(drop)) return InteractionResult.FAIL;
            discard();
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(POTION, 0);
    }

    public int bitePotion() {
        return entityData.get(POTION);
    }

    public void setBitePotion(int id) {
        entityData.set(POTION, LousePotion.byId(id) == null ? 0 : id);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt(LousePotion.TAG, bitePotion());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setBitePotion(tag.getInt(LousePotion.TAG));
    }

    @Override
    public MobType getMobType() {
        return MobType.ARTHROPOD;
    }

    @Override
    public float getWalkTargetValue(BlockPos pos, LevelReader level) {
        return level.getBlockState(pos.below()).is(Blocks.STONE) ? 10 : super.getWalkTargetValue(pos, level);
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return SoundEvents.SILVERFISH_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.SILVERFISH_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.SILVERFISH_DEATH;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        playSound(SoundEvents.SILVERFISH_STEP, 0.15F, 1);
    }

    private static final class LouseBiteGoal extends MeleeAttackGoal {
        LouseBiteGoal(ParasyticLouse louse) {
            super(louse, 1, false);
        }

        @Override
        protected double getAttackReachSqr(LivingEntity target) {
            return 1.44;
        }

        @Override
        protected void checkAndPerformAttack(LivingEntity target, double distanceSquared) {
            if (target.getBoundingBox().maxY > mob.getBoundingBox().minY
                    && target.getBoundingBox().minY < mob.getBoundingBox().maxY) {
                super.checkAndPerformAttack(target, distanceSquared);
            }
        }
    }
}
