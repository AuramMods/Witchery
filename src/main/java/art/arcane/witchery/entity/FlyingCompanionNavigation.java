package art.arcane.witchery.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

final class FlyingCompanionNavigation {
    private FlyingCompanionNavigation() {
    }

    static void flyToward(PathfinderMob flyer, ServerLevel level, Vec3 target, double speed) {
        Vec3 direction = target.subtract(flyer.position());
        var navigation = flyer.getNavigation();
        if (direction.lengthSqr() <= 32 * 32 && clear(flyer, level, target)) {
            // A short clear approach can start from a surface, where a partial path may stay on its first node.
            navigation.stop();
            flyer.getMoveControl().setWantedPosition(target.x, target.y, target.z, speed);
            return;
        }
        if (flyer.tickCount % 10 == 0) navigation.moveTo(target.x, target.y, target.z, speed);
        if (!navigation.isDone()) return;
        Vec3 ahead = flyer.position().add(direction.normalize().scale(Math.min(direction.length(), 2)));
        if (!clear(flyer, level, ahead)) {
            flyer.getMoveControl().setWantedPosition(flyer.getX(), flyer.getY() + 2, flyer.getZ(), speed);
        } else {
            flyer.getMoveControl().setWantedPosition(target.x, target.y, target.z, speed);
        }
    }

    private static boolean clear(PathfinderMob flyer, ServerLevel level, Vec3 point) {
        return level.clip(new ClipContext(flyer.getEyePosition(), point.add(0, flyer.getEyeHeight(), 0),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, flyer)).getType() == HitResult.Type.MISS;
    }
}
