package art.arcane.witchery.entity;

import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcherySounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class DarkMark extends PathfinderMob {
    private static final int LIFETIME = 5 * 60 * 20;
    private static final double RADIUS = 6;
    private long age;

    public DarkMark(EntityType<? extends DarkMark> type, Level level) {
        super(type, level);
        setNoGravity(true);
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder attributes() {
        return Mob.createMobAttributes();
    }

    public static void summon(ServerLevel level, Vec3 location) {
        DarkMark mark = new DarkMark(WitcheryEntities.DARK_MARK.get(), level);
        mark.moveTo(location.x, location.y, location.z, 0, 0);
        level.addFreshEntity(mark);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(1, new LookAtPlayerGoal(this, Player.class, 16));
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide) {
            for (int i = 0; i < 5; i++) {
                level().addParticle(ParticleTypes.LARGE_SMOKE, getRandomX(1.4), getRandomY(), getRandomZ(1.4), 0, 0, 0);
            }
            return;
        }
        if (++age > LIFETIME) {
            discard();
        } else if (age % 4 == 0) {
            AABB bounds = new AABB(getX() - RADIUS, level().getMinBuildHeight(), getZ() - RADIUS,
                    getX() + RADIUS, level().getMaxBuildHeight(), getZ() + RADIUS);
            for (PathfinderMob mob : level().getEntitiesOfClass(PathfinderMob.class, bounds, mob -> mob != this && mob.isPushable())) {
                double x = mob.getX() - getX();
                double z = mob.getZ() - getZ();
                if (x * x + z * z <= RADIUS * RADIUS) {
                    mob.push(Math.signum(x) * 0.22, 0, Math.signum(z) * 0.22);
                }
            }
        }
    }

    @Override
    public void travel(Vec3 input) {
        setDeltaMovement(Vec3.ZERO);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void doPush(Entity entity) { }

    @Override
    public boolean hurt(DamageSource source, float damage) {
        return false;
    }

    @Override
    protected SoundEvent getAmbientSound() {
        return WitcherySounds.TORMENT_LAUGH.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 80;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putLong("WITCTicksAlive", age);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        age = tag.getLong("WITCTicksAlive");
    }
}
