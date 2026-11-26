package art.arcane.witchery.entity;

import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.world.migration.LegacyGameProfiles;
import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class AttackBat extends Bat {
    private static final int LIFETIME = 301;
    private static final double AIM_RANGE = 32;
    private GameProfile owner;
    private int remainingTicks = LIFETIME;

    public AttackBat(EntityType<? extends AttackBat> type, Level level) {
        super(type, level);
    }

    public void joinSwarm(ServerPlayer caster) {
        owner = caster.getGameProfile();
        setResting(false);
        getPersistentData().putBoolean("WITCNoDrops", true);
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel world) || !isAlive()) return;
        if (--remainingTicks == 0) {
            WitcheryNetwork.sendAttackBatExpiry(world, this);
            discard();
            return;
        }
        ServerPlayer caster = caster(world);
        for (LivingEntity victim : world.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(0.2, 0, 0.2),
                entity -> mayBite(caster, entity) && hasLineOfSight(entity))) {
            victim.hurt(damageSources().indirectMagic(this, caster), 4);
            WitcheryNetwork.sendAttackBatBite(world, victim);
            discard();
            return;
        }
    }

    @Override
    protected void customServerAiStep() {
        ServerPlayer caster = level() instanceof ServerLevel world ? caster(world) : null;
        if (caster == null || !pursueAim(caster)) super.customServerAiStep();
    }

    private boolean pursueAim(ServerPlayer caster) {
        Vec3 eye = caster.getEyePosition();
        Vec3 look = caster.getLookAngle().scale(AIM_RANGE);
        // The original aim considers entities along the whole ray. The bat checks its own flight path.
        var aim = ProjectileUtil.getEntityHitResult(caster, eye, eye.add(look),
                caster.getBoundingBox().expandTowards(look).inflate(1),
                entity -> entity.isPickable() && !entity.isSpectator(), AIM_RANGE * AIM_RANGE);
        if (aim == null || !(aim.getEntity() instanceof LivingEntity target) || target instanceof Bat) return false;

        Vec3 displacement = target.position().subtract(position());
        double distance = displacement.length();
        if (distance < 1e-5) return false;
        Vec3 direction = displacement.scale(1 / distance);
        if (!canFlyTo(direction, distance)) return false;

        setResting(false);
        Vec3 velocity = getDeltaMovement().add(direction.scale(0.1));
        setDeltaMovement(velocity);
        float yaw = (float) (-Mth.atan2(velocity.x, velocity.z) * Mth.RAD_TO_DEG);
        setYRot(yaw);
        yBodyRot = yaw;
        return true;
    }

    private boolean canFlyTo(Vec3 direction, double distance) {
        var bounds = getBoundingBox();
        for (int step = 1; step < distance; step++) {
            if (!level().noCollision(this, bounds.move(direction.scale(step)))) return false;
        }
        return true;
    }

    @Nullable
    private ServerPlayer caster(ServerLevel level) {
        if (owner == null || owner.getId() == null) return null;
        return level.getPlayerByUUID(owner.getId()) instanceof ServerPlayer player && player.isAlive() ? player : null;
    }

    private boolean mayBite(@Nullable ServerPlayer caster, LivingEntity target) {
        if (target instanceof Bat || !target.isAlive() || target.isSpectator()) return false;
        return !(target instanceof Player player) || owner == null || caster != null && caster.canHarmPlayer(player) && player != caster;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (owner != null) tag.put("Owner", NbtUtils.writeGameProfile(new CompoundTag(), owner));
        tag.putInt("SwarmTicks", remainingTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        owner = tag.contains("Owner", Tag.TAG_COMPOUND) ? LegacyGameProfiles.read(tag.getCompound("Owner")) : null;
        remainingTicks = tag.contains("SwarmTicks", Tag.TAG_ANY_NUMERIC) ? Mth.clamp(tag.getInt("SwarmTicks"), 1, LIFETIME) : LIFETIME;
    }
}
