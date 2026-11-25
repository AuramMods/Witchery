package art.arcane.witchery.block;

import art.arcane.witchery.world.TormentTravel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.context.BlockPlaceContext;
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

public final class TormentPortalBlock extends Block {
    public static final BooleanProperty LEGACY_ORIENTATION = BooleanProperty.create("legacy_orientation");
    private static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
    private static final VoxelShape PORTAL_X = box(0, 0, 6, 16, 16, 10);
    private static final VoxelShape PORTAL_Z = box(6, 0, 0, 10, 16, 16);

    public TormentPortalBlock() {
        super(Properties.copy(Blocks.NETHER_PORTAL).strength(-1, 5_999.4F).noLootTable().lightLevel(state -> 11));
        registerDefaultState(defaultBlockState().setValue(AXIS, Direction.Axis.X).setValue(LEGACY_ORIENTATION, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AXIS, LEGACY_ORIENTATION);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(AXIS, context.getHorizontalDirection().getClockWise().getAxis());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        Direction normal = state.getValue(AXIS) == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
        return state.setValue(AXIS, rotation.rotate(normal).getAxis());
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level,
                                  BlockPos pos, BlockPos neighborPos) {
        if (!state.getValue(LEGACY_ORIENTATION)) return state;
        // The old portal chose its axis from the two neighboring blocks, even across chunk boundaries.
        Direction.Axis axis = level.isEmptyBlock(pos.west()) && level.isEmptyBlock(pos.east()) ? Direction.Axis.Z : Direction.Axis.X;
        return state.setValue(AXIS, axis).setValue(LEGACY_ORIENTATION, false);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(AXIS) == Direction.Axis.X ? PORTAL_X : PORTAL_Z;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity instanceof ServerPlayer player && !player.isSpectator() && !player.isPassenger() && !player.isVehicle()) {
            TormentTravel.touchPortal(player);
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(100) == 0) {
            level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                    SoundEvents.PORTAL_AMBIENT, SoundSource.BLOCKS, 0.5F, random.nextFloat() * 0.4F + 0.8F, false);
        }
        for (int i = 0; i < 2; i++) {
            level.addParticle(ParticleTypes.FLAME, pos.getX() + random.nextDouble(), pos.getY() + random.nextDouble(),
                    pos.getZ() + random.nextDouble(), 0, 0, 0);
        }
    }
}
