package art.arcane.witchery.entity;

import art.arcane.witchery.infusion.OtherwhereInfusion;
import art.arcane.witchery.item.GlassGobletItem;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import org.jetbrains.annotations.Nullable;

public final class AttackBat extends Bat {
    private static final int LIFETIME = 300;
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
            world.sendParticles(ParticleTypes.SMOKE, getX(), getY(), getZ(), 10, 0.3, 0.3, 0.3, 0.01);
            discard();
            return;
        }
        ServerPlayer caster = caster(world);
        for (LivingEntity victim : world.getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(0.2, 0, 0.2),
                entity -> mayBite(caster, entity) && hasLineOfSight(entity))) {
            victim.hurt(damageSources().indirectMagic(this, caster), 4);
            GlassGobletItem.bloodParticles(world, victim);
            discard();
            return;
        }
    }

    @Override
    protected void customServerAiStep() {
        ServerPlayer caster = level() instanceof ServerLevel world ? caster(world) : null;
        if (caster != null
                && OtherwhereInfusion.rayTrace(caster, 32,
                        entity -> entity instanceof LivingEntity living && mayBite(caster, living)) instanceof EntityHitResult aim
                && hasLineOfSight(aim.getEntity())) {
            setResting(false);
            var velocity = getDeltaMovement().scale(0.82).add(aim.getEntity().getBoundingBox().getCenter()
                    .subtract(getBoundingBox().getCenter()).normalize().scale(0.1));
            setDeltaMovement(velocity);
            float yaw = (float) (Mth.atan2(velocity.z, velocity.x) * Mth.RAD_TO_DEG) - 90;
            setYRot(yaw);
            yBodyRot = yaw;
        } else {
            super.customServerAiStep();
        }
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
        owner = tag.contains("Owner", Tag.TAG_COMPOUND) ? NbtUtils.readGameProfile(tag.getCompound("Owner")) : null;
        remainingTicks = tag.contains("SwarmTicks", Tag.TAG_ANY_NUMERIC) ? Mth.clamp(tag.getInt("SwarmTicks"), 1, LIFETIME) : LIFETIME;
    }
}
