package art.arcane.witchery.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.fluids.ForgeFlowingFluid;

import java.util.EnumSet;
import java.util.List;

public abstract class SpiritFluid extends ForgeFlowingFluid {
    public static final int QUANTA = 5;
    private static final int SLOPE_DISTANCE = 4;
    private static final int NO_DROP = 1000;
    private static final List<Direction> HORIZONTAL = List.of(Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH);

    private SpiritFluid(Properties properties) {
        super(properties);
    }

    public static int legacyBlockLevel(int metadata, boolean falling) {
        if (metadata == 0) return 0;
        return falling ? 8 : Math.round(metadata * (8.0F / QUANTA));
    }

    @Override
    public final void tick(Level level, BlockPos pos, FluidState state) {
        var above = pos.above();
        boolean fedFromAbove = isSame(level.getFluidState(above).getType());
        int quanta = QUANTA;
        if (!state.isSource()) {
            // Classic fluid also feeds a cell from a column one block above any of its sides.
            quanta = fedFromAbove || HORIZONTAL.stream().anyMatch(side -> isSame(level.getFluidState(above.relative(side)).getType()))
                    ? QUANTA - 1 : supportedQuanta(level, pos);
            if (quanta <= 0) {
                level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            } else {
                FluidState updated = flow(quanta, fedFromAbove);
                if (updated != state) {
                    level.setBlock(pos, updated.createLegacyBlock(), Block.UPDATE_ALL);
                    level.scheduleTick(pos, updated.getType(), getTickDelay(level));
                }
            }
        }

        if (displace(level, pos, Direction.DOWN, flow(QUANTA - 1, true))) return;
        if (quanta <= 1 || !state.isSource() && isSame(level.getFluidState(pos.below()).getType())) return;
        FluidState runoff = flow(fedFromAbove ? QUANTA - 1 : quanta - 1, false);
        var outlets = outlets(level, pos);
        for (Direction side : HORIZONTAL) {
            if (outlets.contains(side)) displace(level, pos, side, runoff);
        }
    }

    private int supportedQuanta(Level level, BlockPos pos) {
        int strongest = 0;
        for (Direction side : HORIZONTAL) {
            FluidState neighbor = level.getFluidState(pos.relative(side));
            if (isSame(neighbor.getType())) {
                int quanta = neighbor.isSource() ? QUANTA : neighbor.getValue(FALLING) ? QUANTA - 1
                        : Mth.clamp(Math.round(neighbor.getAmount() * (QUANTA / 8.0F)), 1, QUANTA - 1);
                strongest = Math.max(strongest, quanta);
            }
        }
        return strongest - 1;
    }

    private FluidState flow(int quanta, boolean falling) {
        int blockLevel = legacyBlockLevel(QUANTA - quanta, false);
        return getFlowing(falling ? 8 : 8 - blockLevel, falling);
    }

    private EnumSet<Direction> outlets(Level level, BlockPos pos) {
        EnumSet<Direction> outlets = EnumSet.noneOf(Direction.class);
        int best = NO_DROP;
        for (Direction side : HORIZONTAL) {
            var next = pos.relative(side);
            if (!pathThrough(level, pos, side, next)) continue;
            int cost = hasOutletBelow(level, next) ? 0 : distanceToDrop(level, next, side.getOpposite(), 1);
            if (cost < best) {
                outlets.clear();
                best = cost;
            }
            if (cost == best) outlets.add(side);
        }
        return outlets;
    }

    private int distanceToDrop(Level level, BlockPos pos, Direction backwards, int distance) {
        int best = NO_DROP;
        for (Direction side : HORIZONTAL) {
            if (side == backwards) continue;
            var next = pos.relative(side);
            if (!pathThrough(level, pos, side, next)) continue;
            if (hasOutletBelow(level, next)) return distance;
            if (distance < SLOPE_DISTANCE) best = Math.min(best, distanceToDrop(level, next, side.getOpposite(), distance + 1));
        }
        return best;
    }

    private boolean hasOutletBelow(Level level, BlockPos from) {
        var below = from.below();
        return isSame(level.getFluidState(below).getType()) || canDisplace(level, from, Direction.DOWN, below);
    }

    private boolean pathThrough(Level level, BlockPos from, Direction side, BlockPos next) {
        FluidState nextState = level.getFluidState(next);
        return isSame(nextState.getType()) ? !nextState.isSource() : canDisplace(level, from, side, next);
    }

    private boolean canDisplace(Level level, BlockPos from, Direction side, BlockPos target) {
        if (!level.isInWorldBounds(target)) return false;
        var existing = level.getBlockState(target);
        return existing.getFluidState().isEmpty()
                && canSpreadTo(level, from, level.getBlockState(from), side, target, existing, existing.getFluidState(), getFlowing());
    }

    private boolean displace(Level level, BlockPos from, Direction side, FluidState incoming) {
        var target = from.relative(side);
        if (!canDisplace(level, from, side, target)) return false;
        spreadTo(level, target, level.getBlockState(target), side, incoming);
        return true;
    }

    public static final class Source extends SpiritFluid {
        public Source(Properties properties) { super(properties); }

        @Override
        public int getAmount(FluidState state) { return 8; }

        @Override
        public boolean isSource(FluidState state) { return true; }
    }

    public static final class Flowing extends SpiritFluid {
        public Flowing(Properties properties) {
            super(properties);
            registerDefaultState(getStateDefinition().any().setValue(LEVEL, 8 - legacyBlockLevel(1, false)));
        }

        @Override
        protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
            super.createFluidStateDefinition(builder);
            builder.add(LEVEL);
        }

        @Override
        public int getAmount(FluidState state) { return state.getValue(LEVEL); }

        @Override
        public boolean isSource(FluidState state) { return false; }
    }
}
