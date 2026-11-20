package art.arcane.witchery.block;

import art.arcane.witchery.block.entity.WolfHeadBlockEntity;
import art.arcane.witchery.registry.WitcheryItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class WolfHeadBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = DirectionProperty.create("facing", direction -> direction != Direction.DOWN);
    public static final IntegerProperty ROTATION = BlockStateProperties.ROTATION_16;
    public static final BooleanProperty HELLHOUND = BooleanProperty.create("hellhound");

    public WolfHeadBlock() {
        super(Properties.copy(Blocks.SKELETON_SKULL).noOcclusion());
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP).setValue(ROTATION, 0).setValue(HELLHOUND, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ROTATION, HELLHOUND);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getClickedFace();
        if (facing == Direction.DOWN) return null;
        BlockPos support = context.getClickedPos().relative(facing.getOpposite());
        if (!context.getLevel().getBlockState(support).isFaceSturdy(context.getLevel(), support, facing)) return null;
        return defaultBlockState().setValue(FACING, facing)
                .setValue(ROTATION, facing == Direction.UP ? RotationSegment.convertToSegment(context.getRotation()) : 0)
                .setValue(HELLHOUND, context.getItemInHand().is(WitcheryItems.getRequired("hellhound_head").get()));
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)))
                .setValue(ROTATION, rotation.rotate(state.getValue(ROTATION), 16));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.setValue(FACING, mirror.mirror(state.getValue(FACING)))
                .setValue(ROTATION, mirror.mirror(state.getValue(ROTATION), 16));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case NORTH -> box(4, 4, 8, 12, 12, 16);
            case SOUTH -> box(4, 4, 0, 12, 12, 8);
            case WEST -> box(8, 4, 4, 16, 12, 12);
            case EAST -> box(0, 4, 4, 8, 12, 12);
            default -> box(4, 0, 4, 12, 8, 12);
        };
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return new ItemStack(state.getValue(HELLHOUND) ? WitcheryItems.getRequired("hellhound_head").get() : asItem());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WolfHeadBlockEntity(pos, state);
    }
}
