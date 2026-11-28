package art.arcane.witchery.entity;

import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.Vec3;

final class LegacyArrowLaunch {
    private LegacyArrowLaunch() {}

    static void shoot(AbstractArrow arrow, LivingEntity shooter, float yaw, float muzzleBelowEye,
                      float speed, RandomSource random) {
        float yawRadians = yaw / 180F * Mth.PI;
        float pitchRadians = shooter.getXRot() / 180F * Mth.PI;
        float sinYaw = Mth.sin(yawRadians);
        float cosYaw = Mth.cos(yawRadians);
        arrow.setPos(shooter.getX() - cosYaw * 0.16F, shooter.getEyeY() - muzzleBelowEye,
                shooter.getZ() - sinYaw * 0.16F);

        float cosPitch = Mth.cos(pitchRadians);
        double x = -sinYaw * cosPitch;
        double y = -Mth.sin(pitchRadians);
        double z = cosYaw * cosPitch;
        float length = (float) Math.sqrt(x * x + y * y + z * z);
        Vec3 motion = new Vec3((x / length + nextSpread(random)) * speed, (y / length + nextSpread(random)) * speed,
                (z / length + nextSpread(random)) * speed);
        arrow.setDeltaMovement(motion);
        arrow.setYRot((float) Math.toDegrees(Math.atan2(motion.x, motion.z)));
        arrow.setXRot((float) Math.toDegrees(Math.atan2(motion.y, motion.horizontalDistance())));
        arrow.yRotO = arrow.getYRot();
        arrow.xRotO = arrow.getXRot();
    }

    private static double nextSpread(RandomSource random) {
        return random.nextGaussian() * (random.nextBoolean() ? -1 : 1) * 0.0075F;
    }
}
