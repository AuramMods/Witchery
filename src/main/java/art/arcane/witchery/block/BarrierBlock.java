package art.arcane.witchery.block;

import art.arcane.witchery.block.entity.BarrierBlockEntity;
import art.arcane.witchery.registry.WitcheryBlockEntities;
import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.EntityCollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public final class BarrierBlock extends BaseEntityBlock {
    private static final VoxelShape BOUNDS = box(1, 1, 1, 15, 15, 15);

    public BarrierBlock() {
        super(Properties.copy(Blocks.BARRIER).strength(-1, 600).dynamicShape().noOcclusion().noLootTable());
    }

    public static boolean place(ServerLevel level, BlockPos pos, @Nullable Player owner, boolean blocksPlayers, int duration) {
        Block barrier = WitcheryBlocks.getRequired("barrier").get();
        BlockState previous = level.getBlockState(pos);
        if (!previous.is(barrier)) {
            if (previous.blocksMotion() || previous.hasBlockEntity()
                    || !level.setBlockAndUpdate(pos, barrier.defaultBlockState())) return false;
        }
        if (!(level.getBlockEntity(pos) instanceof BarrierBlockEntity placed)) return false;
        placed.configure(owner, blocksPlayers, duration);
        return true;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState adjacent, Direction direction) {
        return adjacent.is(this) || super.skipRendering(state, adjacent, direction);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return ItemStack.EMPTY;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return BOUNDS;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (context instanceof EntityCollisionContext collision && collision.getEntity() instanceof Player player
                && level.getBlockEntity(pos) instanceof BarrierBlockEntity barrier && barrier.allows(player)) {
            return Shapes.empty();
        }
        return BOUNDS;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BarrierBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, WitcheryBlockEntities.BARRIER.get(), BarrierBlockEntity::tick);
    }
}
