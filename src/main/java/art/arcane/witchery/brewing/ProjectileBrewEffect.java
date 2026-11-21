package art.arcane.witchery.brewing;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.phys.Vec3;

public final class ProjectileBrewEffect extends MobEffect {
    private final boolean attracts;

    public ProjectileBrewEffect(boolean attracts, int color) {
        super(attracts ? MobEffectCategory.HARMFUL : MobEffectCategory.BENEFICIAL, color);
        this.attracts = attracts;
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity target, int amplifier) {
        if (!(target.level() instanceof ServerLevel level)) return;
        double radius = attracts ? 3 * (amplifier + 1) : 2;
        for (Projectile projectile : level.getEntitiesOfClass(Projectile.class, target.getBoundingBox().inflate(radius),
                shot -> shot.isAlive() && (shot instanceof AbstractArrow || shot instanceof ThrowableProjectile))) {
            if (attracts) attract(projectile, target);
            else if (projectile.getOwner() != target) reflect(projectile, amplifier);
        }
    }

    private static void attract(Projectile projectile, LivingEntity target) {
        int minimumAge = projectile.getDeltaMovement().lengthSqr() > 0.25 ? 1 : 10;
        if (projectile.tickCount < minimumAge) return;
        Vec3 direction = target.position().add(0, target.getBbHeight() * 0.75, 0).subtract(projectile.position());
        if (direction.horizontalDistanceSqr() < 1.0E-14) return;
        projectile.shoot(direction.x, direction.y, direction.z, 1, 1);
        projectile.hasImpulse = true;
    }

    private static void reflect(Projectile projectile, int amplifier) {
        Vec3 movement = projectile.getDeltaMovement();
        if (movement.equals(Vec3.ZERO)) return;
        double multiplier = -0.25 * (amplifier + 1);
        double x = movement.x * multiplier;
        double y = x > 0 || movement.z > 0 ? movement.y * multiplier : movement.y;
        projectile.setDeltaMovement(x, y, movement.z * multiplier);
        projectile.hasImpulse = true;
    }
}
