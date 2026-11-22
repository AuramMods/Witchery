package art.arcane.witchery.block;

import art.arcane.witchery.block.entity.WitchesOvenBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class FumeFunnelBlock extends HorizontalDirectionalBlock {
    private static final VoxelShape SHAPE = box(2, 0, 2, 14, 16, 14);

    public FumeFunnelBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
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
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return Shapes.empty();
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            Direction firstSide = state.getValue(FACING).getAxis() == Direction.Axis.Z ? Direction.EAST : Direction.SOUTH;
            BlockPos ovenPos = pos.relative(firstSide);
            if (!(level.getBlockState(ovenPos).getBlock() instanceof WitchesOvenBlock)) {
                ovenPos = pos.relative(firstSide.getOpposite());
                if (!(level.getBlockState(ovenPos).getBlock() instanceof WitchesOvenBlock)) {
                    ovenPos = pos.below();
                }
            }
            if (level.getBlockEntity(ovenPos) instanceof WitchesOvenBlockEntity oven) {
                player.openMenu(oven);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
