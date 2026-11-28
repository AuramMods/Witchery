package art.arcane.witchery.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.phys.Vec3;

final class MobFireball extends LargeFireball {
    MobFireball(ServerLevel level, LivingEntity shooter, LivingEntity target, float strength) {
        super(EntityType.FIREBALL, level);
        setOwner(shooter);

        Vec3 acceleration = MobProjectile.acceleration(MobProjectile.aim(shooter, target, strength), random);
        xPower = acceleration.x;
        yPower = acceleration.y;
        zPower = acceleration.z;

        Vec3 view = shooter.getLookAngle();
        moveTo(shooter.getX() + view.x, shooter.getY(0.5) + 0.5, shooter.getZ() + view.z,
                shooter.getYRot(), shooter.getXRot());
    }
}
