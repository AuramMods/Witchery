package art.arcane.witchery.entity;

import art.arcane.witchery.registry.WitcheryDamageTypes;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
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
import org.jetbrains.annotations.Nullable;

public final class Soulfire extends SmallFireball {
    private static final EntityDataAccessor<Boolean> IGNITION = SynchedEntityData.defineId(Soulfire.class, EntityDataSerializers.BOOLEAN);

    public Soulfire(EntityType<? extends Soulfire> type, Level level) {
        super(type, level);
    }

    public Soulfire(EntityType<? extends Soulfire> type, LivingEntity owner, Vec3 direction) {
        this(type, owner.level());
        setOwner(owner);
        Vec3 acceleration = direction.normalize().scale(0.1);
        xPower = acceleration.x;
        yPower = acceleration.y;
        zPower = acceleration.z;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(IGNITION, false);
    }

    @Override
    protected ParticleOptions getTrailParticle() {
        return entityData.get(IGNITION) ? ParticleTypes.FLAME : super.getTrailParticle();
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
            ignite(server, this, getOwner(), hit.getLocation());
        }
        super.onHit(hit);
    }

    public static void ignite(ServerLevel server, Entity projectile, @Nullable Entity owner, Vec3 position) {
        float damage = 4;
        if (owner instanceof LivingEntity caster) {
            float health = 20 * caster.getHealth() / caster.getMaxHealth();
            if (health > 19) damage = 2;
            else if (health > 15) damage = 3;
            else if (health > 10) damage = 5;
            else damage = 6 + (12 - health) / 2;
        }
        for (LivingEntity victim : server.getEntitiesOfClass(LivingEntity.class, new AABB(position, position).inflate(1.5),
                entity -> entity.isAlive() && entity.distanceToSqr(position) <= 2.25)) {
            float scale = victim instanceof Player ? victim.getMaxHealth() / 20 : 1;
            victim.hurt(WitcheryDamageTypes.demonic(server, projectile, owner), damage * scale);
            server.sendParticles(ParticleTypes.FLAME, victim.getX(), victim.getY(0.5), victim.getZ(),
                    16, 0.5, 0.5, 0.5, 0.04);
            server.playSound(null, victim.blockPosition(), SoundEvents.FLINTANDSTEEL_USE, SoundSource.PLAYERS, 0.8F, 1);
        }
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
