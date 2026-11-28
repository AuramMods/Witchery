package art.arcane.witchery.entity;

import art.arcane.witchery.world.migration.LegacyGameProfiles;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.OwnerHurtTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

public final class Mindrake extends TamableAnimal {
    public Mindrake(EntityType<? extends Mindrake> type, Level level) {
        super(type, level);
        xpReward = 0;
    }

    public static AttributeSupplier.Builder attributes() {
        return createMobAttributes().add(Attributes.MAX_HEALTH, 4).add(Attributes.MOVEMENT_SPEED, 0.4);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(LegacyGameProfiles.withTameOwner(tag));
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new FloatGoal(this));
        goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, false));
        goalSelector.addGoal(3, new RandomStrollGoal(this, 0.8));
        goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 8.0F));
        goalSelector.addGoal(5, new RandomLookAroundGoal(this));
        targetSelector.addGoal(1, new OwnerHurtTargetGoal(this));
        targetSelector.addGoal(2, new HurtByTargetGoal(this));
        targetSelector.addGoal(3, new ReachableTargetGoal<>(this, Player.class, false, player -> !isTame()));
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        if (level() instanceof ServerLevel server) {
            server.explode(this, getX(), getY(), getZ(), 1.5F, Level.ExplosionInteraction.NONE);
            BlockPos pos = blockPosition();
            var soil = server.getBlockState(pos.below());
            var flower = (random.nextBoolean() ? Blocks.POPPY : Blocks.DANDELION).defaultBlockState();
            if ((soil.is(BlockTags.DIRT) || soil.is(Blocks.FARMLAND)) && server.getBlockState(pos).canBeReplaced()
                    && flower.canSurvive(server, pos)) server.setBlockAndUpdate(pos, flower);
            discard();
        }
        return true;
    }

    @Override
    protected void dropAllDeathLoot(DamageSource source) {
        level().explode(this, getX(), getY(), getZ(), 1, Level.ExplosionInteraction.NONE);
        super.dropAllDeathLoot(source);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.GHAST_SCREAM;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.GHAST_DEATH;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return null;
    }
}
