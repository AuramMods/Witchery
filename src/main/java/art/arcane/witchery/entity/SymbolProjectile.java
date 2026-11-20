package art.arcane.witchery.entity;

import art.arcane.witchery.infusion.SpellKnowledge;
import art.arcane.witchery.infusion.SymbolSpell;
import art.arcane.witchery.item.MysticBranchItem;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.world.TormentTravel;
import art.arcane.witchery.world.WitcheryDimensions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

public final class SymbolProjectile extends ThrowableProjectile {
    private static final EntityDataAccessor<Integer> SPELL = SynchedEntityData.defineId(SymbolProjectile.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Vector3f> ACCELERATION = SynchedEntityData.defineId(SymbolProjectile.class, EntityDataSerializers.VECTOR3);
    private int age;

    public SymbolProjectile(EntityType<? extends SymbolProjectile> type, Level level) {
        super(type, level);
        setNoGravity(true);
    }

    public SymbolProjectile(ServerLevel level, ServerPlayer owner, SymbolSpell spell) {
        this(WitcheryEntities.SYMBOL_PROJECTILE.get(), level);
        setOwner(owner);
        Vec3 look = owner.getLookAngle();
        Vec3 launch = owner.getEyePosition().add(0, -0.1, 0).add(look.scale(1.5));
        moveTo(launch.x, launch.y, launch.z, owner.getYRot(), owner.getXRot());
        entityData.set(SPELL, spell.id());
        entityData.set(ACCELERATION, look.scale(0.3).toVector3f());
    }

    @Override
    protected void defineSynchedData() {
        entityData.define(SPELL, 0);
        entityData.define(ACCELERATION, new Vector3f());
    }

    @Nullable
    public SymbolSpell spell() {
        return SymbolSpell.byId(entityData.get(SPELL));
    }

    @Override
    public void tick() {
        SymbolSpell spell = spell();
        if (spell == null) {
            if (!level().isClientSide) discard();
            return;
        }
        age++;
        Vec3 velocity = getDeltaMovement();
        boolean inWater = isInWater();
        super.tick();
        if (isRemoved()) return;

        Vec3 acceleration = new Vec3(entityData.get(ACCELERATION));
        if (spell.falls()) {
            setDeltaMovement(velocity.add(acceleration.scale(Math.pow(0.8, age))).add(0, -0.05, 0));
        } else {
            setDeltaMovement(velocity.add(acceleration).scale(inWater ? 0.8 : 0.95));
        }
        if (level() instanceof ServerLevel server) {
            if (age >= spell.lifetime()) {
                if (spell == SymbolSpell.MORSMORDRE) impact(server, spell, position());
                discard();
            }
        } else {
            level().addParticle(ParticleTypes.WITCH, getX(), getY(0.5), getZ(), 0, 0, 0);
            level().addParticle(ParticleTypes.FLAME, getX(), getY(0.5), getZ(), 0, 0, 0);
        }
    }

    @Override
    protected void onHit(HitResult hit) {
        SymbolSpell spell = spell();
        if (level() instanceof ServerLevel server && spell != null) {
            impact(server, spell, hit.getLocation());
            discard();
        }
    }

    private void impact(ServerLevel level, SymbolSpell spell, Vec3 location) {
        level.sendParticles(ParticleTypes.WITCH, location.x, location.y, location.z, 16, 0.6, 0.6, 0.6, 0);
        level.playSound(null, location.x, location.y, location.z, SoundEvents.ENDER_DRAGON_HURT,
                SoundSource.PLAYERS, 0.8F, 1);
        switch (spell) {
            case IGNIANIMA -> Soulfire.ignite(level, this, getOwner(), location);
            case MORSMORDRE -> DarkMark.summon(level, location);
            case TORMENTUM -> sendToTorment(level, location);
            case CARNOSA_DIEM -> { }
        }
    }

    private void sendToTorment(ServerLevel level, Vec3 location) {
        if (level.dimension().equals(WitcheryDimensions.TORMENT)) return;
        boolean affected = false;
        AABB bounds = new AABB(location, location).inflate(2);
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, bounds, LivingEntity::isAlive)) {
            if (victim instanceof ServerPlayer player) {
                affected |= TormentTravel.requestSpellEncounter(player);
            } else if (victim instanceof Mob && !victim.getType().is(Tags.EntityTypes.BOSSES)) {
                victim.discard();
                affected = true;
            }
        }
        if (affected && getOwner() instanceof ServerPlayer caster) {
            SpellKnowledge.TORMENTUM.startCooldown(caster);
        }
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public float getPickRadius() {
        return 1;
    }

    @Override
    public boolean hurt(DamageSource source, float damage) {
        if (isInvulnerableTo(source) || !(source.getEntity() instanceof ServerPlayer player)
                || !(player.getMainHandItem().getItem() instanceof MysticBranchItem)) return false;
        Vec3 look = player.getLookAngle();
        setOwner(player);
        setDeltaMovement(look);
        entityData.set(ACCELERATION, look.scale(0.1).toVector3f());
        markHurt();
        return true;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("EffectID", entityData.get(SPELL));
        tag.putInt("Age", age);
        Vector3f acceleration = entityData.get(ACCELERATION);
        tag.putFloat("AccelerationX", acceleration.x);
        tag.putFloat("AccelerationY", acceleration.y);
        tag.putFloat("AccelerationZ", acceleration.z);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        entityData.set(SPELL, tag.getInt("EffectID"));
        age = tag.getInt("Age");
        entityData.set(ACCELERATION, new Vector3f(tag.getFloat("AccelerationX"),
                tag.getFloat("AccelerationY"), tag.getFloat("AccelerationZ")));
        ListTag direction = tag.getList("direction", Tag.TAG_DOUBLE);
        if (direction.size() == 3) {
            setDeltaMovement(direction.getDouble(0), direction.getDouble(1), direction.getDouble(2));
            if (spell() == SymbolSpell.MORSMORDRE && tag.contains("lifetime", Tag.TAG_ANY_NUMERIC)) {
                age = Math.max(0, SymbolSpell.MORSMORDRE.lifetime() - tag.getInt("lifetime"));
            }
        }
    }
}
