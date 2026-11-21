package art.arcane.witchery.ritual;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.function.Consumer;

final class ExpandingWave {
    private ExpandingWave() {}

    static boolean contains(Entity entity, Vec3 center, int radius) {
        double distance = entity.distanceToSqr(center);
        return distance > (radius - 1) * (radius - 1) && distance <= radius * radius;
    }

    static void visitSurface(ServerLevel level, BlockPos center, int radius, boolean curse, Consumer<BlockPos> action) {
        int error = 1 - radius;
        for (int x = radius, z = 0; x >= z; z++) {
            visitColumn(level, center.offset(x, 0, z), curse, action);
            visitColumn(level, center.offset(z, 0, x), curse, action);
            visitColumn(level, center.offset(-x, 0, z), curse, action);
            visitColumn(level, center.offset(-z, 0, x), curse, action);
            visitColumn(level, center.offset(-x, 0, -z), curse, action);
            visitColumn(level, center.offset(-z, 0, -x), curse, action);
            visitColumn(level, center.offset(x, 0, -z), curse, action);
            visitColumn(level, center.offset(z, 0, -x), curse, action);
            if (error < 0) error += 2 * (z + 1) + 1;
            else error += 2 * (z - --x + 2);
        }
    }

    private static void visitColumn(ServerLevel level, BlockPos column, boolean curse, Consumer<BlockPos> action) {
        if (!level.hasChunkAt(column) || !level.getWorldBorder().isWithinBounds(column)) return;
        for (int height = 0; height < 15; height++) {
            if (apply(level, column.above(height), curse, 16, action)) return;
            if (height > 0 && apply(level, column.below(height), curse, 32, action)) return;
        }
    }

    private static boolean apply(ServerLevel level, BlockPos pos, boolean curse, int particles, Consumer<BlockPos> action) {
        if (level.isEmptyBlock(pos) || !level.isEmptyBlock(pos.above())) return false;
        level.sendParticles(curse ? ParticleTypes.ENTITY_EFFECT : ParticleTypes.EFFECT,
                pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, particles, 0.5, 0.5, 0.5, 0);
        action.accept(pos);
        return true;
    }
}
