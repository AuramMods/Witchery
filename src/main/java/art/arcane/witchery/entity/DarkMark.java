package art.arcane.witchery.entity;

import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcherySounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class DarkMark extends Mob {
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
        Vec3 movement = getDeltaMovement();
        setDeltaMovement(movement.x, 0, movement.z);
        age = Math.max(tickCount, ++age);
        if (level().isClientSide()) {
            if (age <= LIFETIME && age % 4 == 0) repelCreatures();
            emitSmoke();
        } else if (age > LIFETIME) {
            discard();
        }
    }

    private void repelCreatures() {
        AABB bounds = new AABB(getX() - RADIUS, level().getMinBuildHeight(), getZ() - RADIUS,
                getX() + RADIUS, level().getMaxBuildHeight(), getZ() + RADIUS);
        for (PathfinderMob creature : level().getEntitiesOfClass(PathfinderMob.class, bounds)) {
            double x = creature.getX() - getX();
            double z = creature.getZ() - getZ();
            double distance = x * x + z * z;
            if (distance > 0 && distance <= RADIUS * RADIUS) {
                creature.setDeltaMovement(creature.getDeltaMovement().add(Math.signum(x) * 0.22, 0, Math.signum(z) * 0.22));
            }
        }
    }

    private void emitSmoke() {
        var random = level().getRandom();
        for (int i = 0; i < 5; i++) {
            level().addParticle(ParticleTypes.LARGE_SMOKE, getX() - 1.4 + random.nextDouble() * 2.8,
                    getY() + random.nextDouble() * 2, getZ() - 1.4 + random.nextDouble() * 2.8, 0, 0, 0);
        }
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void pushEntities() { }

    @Override
    protected int decreaseAirSupply(int air) {
        return air;
    }

    @Override
    public boolean onClimbable() {
        return false;
    }

    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

    @Override
    protected void checkFallDamage(double movement, boolean onGround, BlockState state, BlockPos pos) { }

    @Override
    public boolean causeFallDamage(float distance, float multiplier, DamageSource source) {
        return false;
    }

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
    protected float getSoundVolume() {
        return 0.8F;
    }

    @Override
    public float getVoicePitch() {
        return 1;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putLong("WITCTicksAlive", age);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        setNoGravity(true);
        age = tag.getLong("WITCTicksAlive");
    }
}
