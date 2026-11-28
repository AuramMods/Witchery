package art.arcane.witchery.entity;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public abstract class FlyingSpirit extends PathfinderMob {
    private static final EntityDataAccessor<Integer> COLOR =
            SynchedEntityData.defineId(FlyingSpirit.class, EntityDataSerializers.INT);

    FlyingSpirit(EntityType<? extends FlyingSpirit> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(COLOR, 0);
    }

    public final int color() {
        return entityData.get(COLOR);
    }

    protected final void setColor(int color) {
        entityData.set(COLOR, color);
    }

    @Override
    public void travel(Vec3 input) {
        FlyingCompanionGoals.travel(this, input);
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (level().isClientSide && isAlive()) {
            int tint = color() > 0 ? color() : 0xFFCC00;
            var dust = new DustParticleOptions(Vec3.fromRGB24(tint).toVector3f(), 0.6F);
            level().addParticle(dust, getRandomX(1), getY() + 0.1 + random.nextDouble() * 0.2,
                    getRandomZ(1), 0, 0, 0);
        }
    }
}
