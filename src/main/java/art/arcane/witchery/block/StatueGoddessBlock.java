package art.arcane.witchery.block;

import art.arcane.witchery.block.entity.PlacedOwnerBlockEntity;
import art.arcane.witchery.registry.WitcheryBlockEntities;
import art.arcane.witchery.ritual.DirectedCurse;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class StatueGoddessBlock extends OwnedColumnBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final VoxelShape NORTH_SOUTH = box(0, 0, 1.6, 16, 32, 14.4);
    private static final VoxelShape EAST_WEST = box(1.6, 0, 0, 14.4, 32, 16);

    public StatueGoddessBlock() {
        super(Properties.copy(Blocks.STONE).strength(-1, 1000).noOcclusion(), true);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return rotate(state, mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(FACING).getAxis() == Direction.Axis.X ? EAST_WEST : NORTH_SOUTH;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public PlacedOwnerBlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PlacedOwnerBlockEntity(WitcheryBlockEntities.STATUE_GODDESS.get(), pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level instanceof ServerLevel server) {
            if (DirectedCurse.clearAtGoddess(player)) {
                server.sendParticles(ParticleTypes.INSTANT_EFFECT, player.getX(), player.getY() + 0.8, player.getZ(),
                        8, 0.5, 0.8, 0.5, 0.03);
                level.playSound(null, player, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1, 1);
            } else {
                level.playSound(null, player, SoundEvents.NOTE_BLOCK_SNARE.value(), SoundSource.BLOCKS, 1, 1);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
