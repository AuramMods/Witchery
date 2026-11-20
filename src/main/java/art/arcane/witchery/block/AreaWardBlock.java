package art.arcane.witchery.block;

import art.arcane.witchery.block.entity.AreaWardBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class AreaWardBlock extends HorizontalDirectionalBlock implements EntityBlock {
    private static final VoxelShape SHAPE = box(2.4, 0, 2.4, 13.6, 8, 13.6);
    private final AreaWardBlockEntity.Kind kind;

    public AreaWardBlock(AreaWardBlockEntity.Kind kind) {
        super(Properties.copy(Blocks.STONE).strength(2.5F, 9999).noOcclusion());
        this.kind = kind;
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
        return SHAPE;
    }

    @Override
    public AreaWardBlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AreaWardBlockEntity(pos, state, kind);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity actor, ItemStack stack) {
        if (!level.isClientSide && actor instanceof Player player
                && level.getBlockEntity(pos) instanceof AreaWardBlockEntity ward) ward.placedBy(player);
    }

    @Override
    public void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (!level.isClientSide && player.isShiftKeyDown()
                && level.getBlockEntity(pos) instanceof AreaWardBlockEntity ward && ward.ownedBy(player)) {
            removeColumn(level, pos);
        }
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && player.isCreative()) {
            popResource(level, pos, new ItemStack(this));
            removeColumn(level, pos.above());
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    private void removeColumn(Level level, BlockPos pos) {
        for (BlockPos next = pos; level.getBlockState(next).is(this); next = next.above()) {
            if (level.removeBlock(next, false)) popResource(level, next, new ItemStack(this));
        }
    }
}
