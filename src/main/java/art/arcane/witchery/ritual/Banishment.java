package art.arcane.witchery.ritual;

import art.arcane.witchery.entity.DeathBoss;
import art.arcane.witchery.entity.Demon;
import art.arcane.witchery.entity.Imp;
import art.arcane.witchery.entity.LordOfTorment;
import art.arcane.witchery.entity.Reflection;
import art.arcane.witchery.network.WitcheryNetwork;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public final class Banishment {
    private static final int RADIUS = 9;

    private Banishment() {}

    public static void perform(ServerLevel level, BlockPos heart, boolean legacy) {
        Vec3 origin = Vec3.atLowerCornerOf(heart);
        var bounds = new AABB(origin, origin).inflate(RADIUS);
        for (Mob target : level.getEntitiesOfClass(Mob.class, bounds, mob -> mob.isAlive()
                && mob.distanceToSqr(origin) < RADIUS * RADIUS && canBanish(mob))) {
            target.discard();
            if (legacy) {
                WitcheryNetwork.sendBanishment(level, target.position());
            } else {
                level.sendParticles(ParticleTypes.EXPLOSION, target.getX(), target.getY(0.5), target.getZ(),
                        16, 0.5, 1, 0.5, 0.04);
            }
        }
    }

    private static boolean canBanish(Mob mob) {
        return mob instanceof Demon || mob instanceof DeathBoss || mob instanceof LordOfTorment
                || mob instanceof Imp || mob instanceof Reflection;
    }
}
