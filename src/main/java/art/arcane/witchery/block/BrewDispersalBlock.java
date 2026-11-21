package art.arcane.witchery.block;

import art.arcane.witchery.block.entity.BrewFluidBlockEntity;
import art.arcane.witchery.brewing.BrewMixture;
import art.arcane.witchery.registry.WitcheryBlockEntities;
import art.arcane.witchery.registry.WitcheryFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public final class BrewDispersalBlock extends LiquidBlock implements EntityBlock {
    private static final int TICK_DELAY = 5;
    private static final int MAX_LIQUID_LEVEL = 7;
    private static final int NO_DROP_FOUND = 1000;
    private static final Direction[] HORIZONTAL = {Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH};
    private static final Direction[] GAS_DIRECTIONS = {
            Direction.UP, Direction.DOWN, Direction.EAST, Direction.WEST, Direction.SOUTH, Direction.NORTH
    };
    private final boolean gas;

    public BrewDispersalBlock(boolean gas) {
        super(WitcheryFluids.getRequiredSource(gas ? "brewgas" : "brewliquid"),
                Properties.copy(Blocks.WATER).noCollission().noOcclusion().noLootTable());
        this.gas = gas;
    }

    public boolean isGas() { return gas; }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BrewFluidBlockEntity((gas ? WitcheryBlockEntities.BREW_GAS : WitcheryBlockEntities.BREW_LIQUID).get(),
                pos, state);
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return gas ? Fluids.EMPTY.defaultFluidState() : super.getFluidState(state);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return gas;
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState toward, Direction side) {
        return toward.is(this);
    }

    @Override
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return true;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    public ItemStack pickupBlock(LevelAccessor level, BlockPos pos, BlockState state) {
        return ItemStack.EMPTY;
    }

    @Override
    public Optional<SoundEvent> getPickupSound() {
        return Optional.empty();
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState previous, boolean moved) {
        if (!level.isClientSide) level.scheduleTick(pos, this, TICK_DELAY);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction side, BlockState neighbor, LevelAccessor level,
                                  BlockPos pos, BlockPos neighborPos) {
        level.scheduleTick(pos, this, TICK_DELAY);
        return state;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighbor, BlockPos neighborPos, boolean moved) {
        if (!level.isClientSide) level.scheduleTick(pos, this, TICK_DELAY);
    }

    public void release(ServerLevel level, BlockPos pos, Direction face, CompoundTag tag, BrewMixture mixture, int color,
                        @Nullable Entity caster) {
        if (!canReplace(level, pos)) return;
        if (caster != null && ForgeEventFactory.onBlockPlace(caster, BlockSnapshot.create(level.dimension(), level, pos), face)) {
            return;
        }
        if (level.setBlockAndUpdate(pos, defaultBlockState()) && level.getBlockEntity(pos) instanceof BrewFluidBlockEntity fluid) {
            fluid.initialize(tag, mixture, color, caster);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!(level.getBlockEntity(pos) instanceof BrewFluidBlockEntity fluid) || !fluid.hasContents()) {
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            return;
        }
        int depth = state.getValue(LEVEL);
        boolean remains = gas ? tickGas(level, pos, state, fluid, depth, random)
                : tickLiquid(level, pos, state, fluid, depth, random);
        if (remains) {
            fluid.sampleStationaryContacts(level, gas);
            if (!gas) fluid.sampleTerrainContacts(level);
            level.scheduleTick(pos, this, TICK_DELAY);
        }
    }

    private boolean tickGas(ServerLevel level, BlockPos pos, BlockState state, BrewFluidBlockEntity fluid, int depth,
                            RandomSource random) {
        int age = fluid.tickAge();
        if (age > 120 || depth >= fluid.expansion() && evaporates(fluid, random)) {
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            return false;
        }
        if (depth >= fluid.expansion()) return true;
        boolean spread = false;
        for (Direction direction : GAS_DIRECTIONS) {
            double probability = switch (direction) {
                case UP -> 0.2;
                case DOWN -> 0.4;
                default -> 0.8;
            };
            if (random.nextDouble() < probability && flowInto(level, pos.relative(direction), depth + 1, fluid)) {
                spread = true;
            }
        }
        if (spread) level.setBlockAndUpdate(pos, state.setValue(LEVEL, depth + 1));
        return true;
    }

    private boolean tickLiquid(ServerLevel level, BlockPos pos, BlockState state, BrewFluidBlockEntity fluid, int depth,
                               RandomSource random) {
        if (depth == 0) {
            int age = fluid.tickAge();
            if (age > 3 && evaporates(fluid, random)) {
                level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                return false;
            }
        } else {
            int suppliedDepth = suppliedDepth(level, pos);
            if (suppliedDepth > MAX_LIQUID_LEVEL) {
                level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                return false;
            }
            if (depth != suppliedDepth) {
                depth = suppliedDepth;
                level.setBlockAndUpdate(pos, state.setValue(LEVEL, depth));
            }
        }
        BlockPos below = pos.below();
        if (flowInto(level, below, 1, fluid)) return true;
        if (depth >= MAX_LIQUID_LEVEL || depth > 0 && level.getBlockState(below).is(this)) return true;
        int nextDepth = level.getBlockState(pos.above()).is(this) ? 1 : depth + 1;
        int[] costs = new int[HORIZONTAL.length];
        int best = NO_DROP_FOUND;
        for (int i = 0; i < HORIZONTAL.length; i++) {
            Direction direction = HORIZONTAL[i];
            BlockPos next = pos.relative(direction);
            costs[i] = canReplace(level, next) ? distanceToDrop(level, next, direction.getOpposite(), 0) : NO_DROP_FOUND;
            best = Math.min(best, costs[i]);
        }
        for (int i = 0; i < HORIZONTAL.length; i++) {
            if (costs[i] == best) flowInto(level, pos.relative(HORIZONTAL[i]), nextDepth, fluid);
        }
        return true;
    }

    private int suppliedDepth(ServerLevel level, BlockPos pos) {
        BlockPos above = pos.above();
        if (level.getBlockState(above).is(this)) return 1;
        int supplied = MAX_LIQUID_LEVEL + 1;
        for (Direction direction : HORIZONTAL) {
            BlockPos adjacent = pos.relative(direction);
            if (!level.hasChunkAt(adjacent)) continue;
            if (level.getBlockState(adjacent.above()).is(this)) return 1;
            BlockState neighbor = level.getBlockState(adjacent);
            if (neighbor.is(this)) supplied = Math.min(supplied, neighbor.getValue(LEVEL) + 1);
        }
        return supplied;
    }

    private int distanceToDrop(ServerLevel level, BlockPos pos, Direction cameFrom, int steps) {
        if (canFlowThrough(level, pos.below())) return steps;
        if (steps == 4) return NO_DROP_FOUND;
        int best = NO_DROP_FOUND;
        for (Direction direction : HORIZONTAL) {
            if (direction == cameFrom) continue;
            BlockPos next = pos.relative(direction);
            if (canFlowThrough(level, next)) {
                best = Math.min(best, distanceToDrop(level, next, direction.getOpposite(), steps + 1));
            }
        }
        return best;
    }

    private boolean canFlowThrough(ServerLevel level, BlockPos pos) {
        if (!level.hasChunkAt(pos)) return false;
        BlockState state = level.getBlockState(pos);
        return state.is(this) ? state.getValue(LEVEL) > 0 : canReplace(level, pos);
    }

    private boolean flowInto(ServerLevel level, BlockPos pos, int depth, BrewFluidBlockEntity source) {
        if (!canReplace(level, pos) || !level.setBlockAndUpdate(pos, defaultBlockState().setValue(LEVEL, depth))) {
            return false;
        }
        if (level.getBlockEntity(pos) instanceof BrewFluidBlockEntity fluid) {
            fluid.spreadFrom(source);
            return true;
        }
        return false;
    }

    private boolean canReplace(ServerLevel level, BlockPos pos) {
        if (level.isOutsideBuildHeight(pos) || !level.hasChunkAt(pos)) return false;
        BlockState state = level.getBlockState(pos);
        if (gas) return state.isAir() || state.is(Blocks.SNOW);
        return !state.is(this) && state.canBeReplaced() && state.getFluidState().isEmpty();
    }

    private static boolean evaporates(BrewFluidBlockEntity fluid, RandomSource random) {
        return fluid.duration() == 0 || random.nextInt(fluid.duration()) == 0;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level instanceof ServerLevel server && entity instanceof LivingEntity target
                && level.getBlockEntity(pos) instanceof BrewFluidBlockEntity fluid) {
            fluid.contact(server, target, gas);
        }
    }
}
