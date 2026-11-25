package art.arcane.witchery.entity;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;

final class BrewProjectileLaunch {
    private BrewProjectileLaunch() {}

    static void fromPlayer(Projectile projectile, LivingEntity thrower, float pitchOffset, float speed) {
        float yaw = thrower.getYRot() * Mth.DEG_TO_RAD;
        float pitch = thrower.getXRot();
        float sinYaw = Mth.sin(yaw);
        float cosYaw = Mth.cos(yaw);
        projectile.setPos(projectile.position().add(-cosYaw * 0.16F, 0, -sinYaw * 0.16F));

        float horizontal = Mth.cos(pitch * Mth.DEG_TO_RAD);
        projectile.shoot(-sinYaw * horizontal, -Mth.sin((pitch + pitchOffset) * Mth.DEG_TO_RAD),
                cosYaw * horizontal, speed, 1);
    }

    static void shoot(Projectile projectile, RandomSource random, Vec3 aim, float speed, float inaccuracy) {
        double spread = (double) 0.0075F * inaccuracy;
        Vec3 motion = aim.normalize().add(random.nextGaussian() * spread, random.nextGaussian() * spread,
                random.nextGaussian() * spread).scale(speed);
        projectile.setDeltaMovement(motion);
        projectile.setYRot((float) (Mth.atan2(motion.x, motion.z) * Mth.RAD_TO_DEG));
        projectile.setXRot((float) (Mth.atan2(motion.y, motion.horizontalDistance()) * Mth.RAD_TO_DEG));
        projectile.yRotO = projectile.getYRot();
        projectile.xRotO = projectile.getXRot();
    }
}
