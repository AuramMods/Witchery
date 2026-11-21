package art.arcane.witchery.brewing;

import art.arcane.witchery.entity.ThrownBrew;
import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;

public final class BrewSolidification {
    private static final int RANGE = 64;
    private static final List<Direction> NEIGHBORS = List.of(Direction.EAST, Direction.WEST, Direction.SOUTH,
            Direction.NORTH, Direction.UP, Direction.DOWN);

    private BrewSolidification() {}

    public static void impact(ThrownBrew brew, HitResult hit, Block result) {
        if (!(brew.level() instanceof ServerLevel level)) return;
        if (!(hit instanceof BlockHitResult block)) {
            BrewImpacts.recoverBrew(brew, level, hit);
            return;
        }
        Block tears = WitcheryBlocks.getRequired("hollowtears").get();
        BlockPos pos = block.getBlockPos();
        if (!level.getBlockState(pos).is(tears)) pos = pos.relative(block.getDirection());
        if (!replaceTears(level, brew.getOwner(), pos, block.getDirection(), tears, result.defaultBlockState())) {
            BrewImpacts.recoverBrew(brew, level, hit);
        }
    }

    private static boolean replaceTears(ServerLevel level, @Nullable Entity owner, BlockPos origin, Direction face,
                                        Block tears, BlockState result) {
        if (!level.hasChunkAt(origin) || !level.getBlockState(origin).is(tears)
                || !BrewTerrain.setBlock(owner, level, origin, result, face)) return false;
        var frontier = new ArrayDeque<BlockPos>();
        var visited = new HashSet<BlockPos>();
        frontier.add(origin);
        visited.add(origin);
        while (!frontier.isEmpty()) {
            BlockPos pos = frontier.removeFirst();
            if (result.isAir()) erodeSupport(level, owner, pos.below(), tears, result);
            Vec3 center = Vec3.atCenterOf(pos);
            level.sendParticles(ParticleTypes.INSTANT_EFFECT, center.x, center.y, center.z, 4, 0.3, 0.3, 0.3, 0);
            if (Math.abs(pos.getX() - origin.getX()) >= RANGE || Math.abs(pos.getY() - origin.getY()) >= RANGE
                    || Math.abs(pos.getZ() - origin.getZ()) >= RANGE) continue;
            for (Direction direction : NEIGHBORS) {
                BlockPos next = pos.relative(direction);
                if (visited.add(next) && level.hasChunkAt(next) && level.getBlockState(next).is(tears)
                        && BrewTerrain.setBlock(owner, level, next, result, direction)) {
                    frontier.addLast(next);
                }
            }
        }
        return true;
    }

    private static void erodeSupport(ServerLevel level, @Nullable Entity owner, BlockPos pos, Block tears, BlockState air) {
        BlockState support = level.getBlockState(pos);
        if (!support.isAir() && !support.is(tears) && !BrewTerrain.isProtectedTerrain(support)) {
            BrewTerrain.setBlock(owner, level, pos, air, Direction.UP);
        }
    }
}
