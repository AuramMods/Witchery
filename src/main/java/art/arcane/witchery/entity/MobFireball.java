package art.arcane.witchery.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.phys.Vec3;

final class MobFireball extends LargeFireball {
    MobFireball(ServerLevel level, LivingEntity shooter, LivingEntity target, float strength) {
        super(EntityType.FIREBALL, level);
        setOwner(shooter);

        float spread = Mth.sqrt(strength) * 0.5F;
        var aimRandom = shooter.getRandom();
        // The shooter aims from its midpoint. The projectile applies a separate spread before launch.
        Vec3 aim = new Vec3(target.getX() - shooter.getX() + aimRandom.nextGaussian() * spread,
                target.getY(0.5) - shooter.getY(0.5),
                target.getZ() - shooter.getZ() + aimRandom.nextGaussian() * spread);
        aim = aim.add(random.nextGaussian() * 0.4, random.nextGaussian() * 0.4, random.nextGaussian() * 0.4);
        double length = (float) aim.length();
        if (length > 0) {
            Vec3 acceleration = aim.scale(0.1 / length);
            xPower = acceleration.x;
            yPower = acceleration.y;
            zPower = acceleration.z;
        }

        Vec3 view = shooter.getLookAngle();
        moveTo(shooter.getX() + view.x, shooter.getY(0.5) + 0.5, shooter.getZ() + view.z,
                shooter.getYRot(), shooter.getXRot());
    }

    static void playLaunchCue(ServerLevel level, LivingEntity shooter) {
        level.levelEvent(null, 1016, new BlockPos((int) shooter.getX(), (int) shooter.getY(), (int) shooter.getZ()), 0);
    }
}
