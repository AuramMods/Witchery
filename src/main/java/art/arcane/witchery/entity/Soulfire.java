package art.arcane.witchery.entity;

import art.arcane.witchery.registry.WitcheryDamageTypes;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class Soulfire extends SmallFireball {
    private static final EntityDataAccessor<Boolean> IGNITION = SynchedEntityData.defineId(Soulfire.class, EntityDataSerializers.BOOLEAN);

    public Soulfire(EntityType<? extends Soulfire> type, Level level) {
        super(type, level);
    }

    public Soulfire(EntityType<? extends Soulfire> type, LivingEntity owner, double x, double y, double z, boolean ignition) {
        this(type, owner.level());
        setOwner(owner);
        Vec3 acceleration = new Vec3(x, y, z).normalize().scale(0.1);
        xPower = acceleration.x;
        yPower = acceleration.y;
        zPower = acceleration.z;
        entityData.set(IGNITION, ignition);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(IGNITION, false);
    }

    @Override
    protected ParticleOptions getTrailParticle() {
        return entityData.get(IGNITION) ? ParticleTypes.FLAME : ParticleTypes.SOUL_FIRE_FLAME;
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        if (!entityData.get(IGNITION)) {
            super.onHitEntity(hit);
            if (level() instanceof ServerLevel server) hit.getEntity().hurt(WitcheryDamageTypes.demonic(server, this, getOwner()), 6);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult hit) {
        if (!entityData.get(IGNITION)) super.onHitBlock(hit);
    }

    @Override
    protected void onHit(HitResult hit) {
        if (entityData.get(IGNITION) && level() instanceof ServerLevel server) {
            Vec3 position = hit.getLocation();
            float health = getOwner() instanceof LivingEntity caster ? 20 * caster.getHealth() / caster.getMaxHealth() : 16;
            float damage = health > 19 ? 2 : health > 15 ? 3 : health > 10 ? 5 : 6 + (12 - health) / 2;
            for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class, new AABB(position, position).inflate(1.5),
                    entity -> entity.distanceToSqr(position) <= 2.25)) {
                float scale = victim instanceof Player ? victim.getMaxHealth() / 20 : 1;
                victim.hurt(WitcheryDamageTypes.demonic(server, this, getOwner()), damage * scale);
            }
            server.sendParticles(ParticleTypes.FLAME, position.x, position.y, position.z, 16, 0.5, 0.5, 0.5, 0.04);
        }
        super.onHit(hit);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Ignianima", entityData.get(IGNITION));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(IGNITION, tag.getBoolean("Ignianima"));
    }
}
