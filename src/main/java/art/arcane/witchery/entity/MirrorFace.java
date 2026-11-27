package art.arcane.witchery.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class MirrorFace extends Mob {
    private long ticksAlive;

    public MirrorFace(EntityType<? extends MirrorFace> type, Level level) {
        super(type, level);
        noPhysics = true;
        setNoGravity(true);
        setPersistenceRequired();
        float yaw = random.nextFloat() * Mth.TWO_PI;
        setYRot(yaw);
        setYHeadRot(yaw);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 16, 0.4F));
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 movement = getDeltaMovement();
        setDeltaMovement(movement.x, 0, movement.z);
        ticksAlive = Math.max(tickCount, ++ticksAlive);
        if (!level().isClientSide && ticksAlive > 200) discard();
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    @Override
    protected int decreaseAirSupply(int air) {
        return air;
    }

    @Override
    protected void doPush(Entity other) {
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putLong("WITCTicksAlive", ticksAlive);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setNoGravity(true);
        ticksAlive = tag.getLong("WITCTicksAlive");
    }
}
