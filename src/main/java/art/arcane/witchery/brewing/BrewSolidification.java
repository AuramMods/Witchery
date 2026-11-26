package art.arcane.witchery.brewing;

import art.arcane.witchery.entity.ThrownBrew;
import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
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
            BrewImpacts.recoverBrew(brew, hit);
            return;
        }
        Block tears = WitcheryBlocks.getRequired("hollowtears").get();
        BlockPos pos = block.getBlockPos();
        if (!level.getBlockState(pos).is(tears)) pos = pos.relative(block.getDirection());
        if (!replaceTears(level, brew.getOwner(), pos, block.getDirection(), tears, result.defaultBlockState())) {
            BrewImpacts.recoverBrew(brew, hit);
        }
    }

    private record Visit(BlockPos pos, Direction side) {}

    private static boolean replaceTears(ServerLevel level, @Nullable Entity owner, BlockPos origin, Direction face,
                                        Block tears, BlockState result) {
        if (!level.hasChunkAt(origin) || !level.getBlockState(origin).is(tears)) return false;
        var frontier = new ArrayDeque<Visit>();
        var visited = new HashSet<BlockPos>();
        boolean changed = false;
        boolean rejected = false;
        // The original visits neighbors first and only reaches the origin again through another Tears cell.
        visitNeighbors(frontier, origin);
        while (!frontier.isEmpty()) {
            Visit visit = frontier.removeFirst();
            BlockPos pos = visit.pos();
            if (!visited.add(pos) || !level.hasChunkAt(pos) || !level.getBlockState(pos).is(tears)) continue;
            if (!BrewTerrain.setBlock(owner, level, pos, result, pos.equals(origin) ? face : visit.side())) {
                rejected = true;
                continue;
            }
            changed = true;
            if (result.isAir()) erodeSupport(level, owner, pos.below(), result);
            WitcheryNetwork.sendSolidificationCue(level, pos);
            if (Math.abs(pos.getX() - origin.getX()) < RANGE && Math.abs(pos.getY() - origin.getY()) < RANGE
                    && Math.abs(pos.getZ() - origin.getZ()) < RANGE) {
                visitNeighbors(frontier, pos);
            }
        }
        return changed || !rejected;
    }

    private static void visitNeighbors(ArrayDeque<Visit> frontier, BlockPos pos) {
        for (int index = NEIGHBORS.size() - 1; index >= 0; index--) {
            Direction side = NEIGHBORS.get(index);
            frontier.addFirst(new Visit(pos.relative(side), side));
        }
    }

    private static void erodeSupport(ServerLevel level, @Nullable Entity owner, BlockPos pos, BlockState air) {
        BlockState support = level.getBlockState(pos);
        if (!support.isAir() && !BrewTerrain.isProtectedTerrain(support)) {
            BrewTerrain.setBlock(owner, level, pos, air, Direction.UP);
        }
    }
}
