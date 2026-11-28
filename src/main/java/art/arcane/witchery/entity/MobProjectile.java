package art.arcane.witchery.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

final class MobProjectile {
    private MobProjectile() {}

    static Vec3 aim(LivingEntity shooter, LivingEntity target, float strength) {
        float spread = Mth.sqrt(strength) * 0.5F;
        var random = shooter.getRandom();
        return new Vec3(target.getX() - shooter.getX() + random.nextGaussian() * spread,
                target.getY(0.5) - shooter.getY(0.5),
                target.getZ() - shooter.getZ() + random.nextGaussian() * spread);
    }

    static Vec3 acceleration(Vec3 aim, RandomSource projectileRandom) {
        Vec3 spread = aim.add(projectileRandom.nextGaussian() * 0.4,
                projectileRandom.nextGaussian() * 0.4, projectileRandom.nextGaussian() * 0.4);
        double length = (float) spread.length();
        return length > 0 ? spread.scale(0.1 / length) : Vec3.ZERO;
    }

    static void playLaunchCue(ServerLevel level, LivingEntity shooter) {
        level.levelEvent(null, 1016, new BlockPos((int) shooter.getX(), (int) shooter.getY(), (int) shooter.getZ()), 0);
    }
}
