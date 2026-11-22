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
        if (flyer.tickCount % 10 == 0) flyer.getNavigation().moveTo(target.x, target.y, target.z, speed);
        if (!flyer.getNavigation().isDone()) return;
        Vec3 direction = target.subtract(flyer.position());
        Vec3 ahead = flyer.position().add(direction.normalize().scale(Math.min(direction.length(), 2)));
        var hit = level.clip(new ClipContext(flyer.getEyePosition(), ahead.add(0, flyer.getEyeHeight(), 0),
                ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, flyer));
        if (hit.getType() != HitResult.Type.MISS) {
            flyer.getMoveControl().setWantedPosition(flyer.getX(), flyer.getY() + 2, flyer.getZ(), speed);
        } else {
            flyer.getMoveControl().setWantedPosition(target.x, target.y, target.z, speed);
        }
    }
}
