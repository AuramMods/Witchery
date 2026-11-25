package art.arcane.witchery.block;

import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.world.DreamTravel;
import art.arcane.witchery.world.WitcheryDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class SpiritPortalBlock extends Block {
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
    public static final BooleanProperty LEGACY_ORIENTATION = BooleanProperty.create("legacy_orientation");
    private static final VoxelShape PORTAL_X = box(0, 0, 6, 16, 16, 10);
    private static final VoxelShape PORTAL_Z = box(6, 0, 0, 10, 16, 16);
    private static final int SPAN = 2;

    public SpiritPortalBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(AXIS, Direction.Axis.X).setValue(LEGACY_ORIENTATION, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS, LEGACY_ORIENTATION);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(AXIS) == Direction.Axis.X ? PORTAL_X : PORTAL_Z;
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return ItemStack.EMPTY;
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        Direction horizontal = horizontal(state.getValue(AXIS));
        return state.setValue(AXIS, rotation.rotate(horizontal).getAxis());
    }

    public static boolean tryIgnite(Level level, BlockPos source) {
        if (level.isClientSide() || !level.dimension().equals(WitcheryDimensions.DREAM)
                || !level.getBlockState(source.below()).is(Blocks.SNOW_BLOCK)) {
            return false;
        }
        Block portal = WitcheryBlocks.getRequired("spiritportal").get();
        Block fluid = WitcheryBlocks.getRequired("spiritflowing").get();
        for (Direction direction : new Direction[]{Direction.EAST, Direction.SOUTH}) {
            for (int shift = 0; shift < SPAN; shift++) {
                BlockPos bottom = source.relative(direction, -shift);
                if (!matchesFrame(level, bottom, direction,
                        interior -> interior.isAir() || interior.is(fluid))) {
                    continue;
                }
                BlockState state = portal.defaultBlockState().setValue(AXIS, direction.getAxis());
                for (int along = 0; along < SPAN; along++) {
                    for (int vertical = 0; vertical < SPAN; vertical++) {
                        level.setBlock(bottom.relative(direction, along).above(vertical), state, UPDATE_CLIENTS | UPDATE_KNOWN_SHAPE);
                    }
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level,
                                  BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(LEGACY_ORIENTATION)) {
            for (Direction horizontal : new Direction[]{Direction.EAST, Direction.SOUTH}) {
                if (hasFrame(level, pos, horizontal)) {
                    return state.setValue(AXIS, horizontal.getAxis()).setValue(LEGACY_ORIENTATION, false);
                }
            }
            return Blocks.AIR.defaultBlockState();
        }
        Direction.Axis axis = state.getValue(AXIS);
        if (direction.getAxis() != axis && direction.getAxis() != Direction.Axis.Y) {
            return state;
        }
        return hasFrame(level, pos, horizontal(axis)) ? state : Blocks.AIR.defaultBlockState();
    }

    private boolean hasFrame(BlockGetter level, BlockPos pos, Direction horizontal) {
        for (int shift = 0; shift < SPAN; shift++) {
            for (int depth = 0; depth < SPAN; depth++) {
                BlockPos bottom = pos.relative(horizontal, -shift).below(depth);
                if (matchesFrame(level, bottom, horizontal, interior -> interior.is(this)
                        && (interior.getValue(LEGACY_ORIENTATION) || interior.getValue(AXIS) == horizontal.getAxis()))) return true;
            }
        }
        return false;
    }

    private static boolean matchesFrame(BlockGetter level, BlockPos bottom, Direction horizontal,
                                        java.util.function.Predicate<BlockState> interiorRule) {
        for (int along = -1; along <= SPAN; along++) {
            for (int vertical = -1; vertical <= SPAN; vertical++) {
                boolean side = along == -1 || along == SPAN;
                boolean edge = vertical == -1 || vertical == SPAN;
                if (side && edge) continue;
                BlockState state = level.getBlockState(bottom.relative(horizontal, along).above(vertical));
                if (side || edge ? !state.is(Blocks.SNOW_BLOCK) : !interiorRule.test(state)) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity instanceof ServerPlayer player && !player.isPassenger() && !player.isVehicle()) {
            DreamTravel.requestManifest(player);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        boolean runsAlongX = state.getValue(AXIS) == Direction.Axis.X;
        for (int particle = 0; particle < 2; particle++) {
            double plane = 0.5 + (random.nextBoolean() ? 0.23 : -0.23);
            double spread = random.nextDouble();
            level.addParticle(ParticleTypes.DRIPPING_WATER, pos.getX() + (runsAlongX ? spread : plane),
                    pos.getY() + random.nextDouble(), pos.getZ() + (runsAlongX ? plane : spread), 0, 0, 0);
        }
    }

    private static Direction horizontal(Direction.Axis axis) {
        return axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
    }
}
