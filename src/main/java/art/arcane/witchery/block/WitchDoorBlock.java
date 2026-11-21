package art.arcane.witchery.block;

import art.arcane.witchery.item.DoorKeyItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class WitchDoorBlock extends DoorBlock {
    private final boolean alder;

    public WitchDoorBlock(boolean alder) {
        super(properties(alder), BlockSetType.OAK);
        this.alder = alder;
    }

    private static Properties properties(boolean alder) {
        var properties = Properties.copy(Blocks.OAK_DOOR).noOcclusion().strength(alder ? 3 : 5);
        return alder ? properties : properties.noLootTable();
    }

    public static BlockPos lowerHalf(BlockPos pos, BlockState state) {
        return state.getValue(HALF) == DoubleBlockHalf.LOWER ? pos : pos.below();
    }

    public static boolean replacePair(ServerLevel level, BlockPos base, BlockState lower, BlockState upper,
                                      @Nullable Entity caster, Direction face) {
        base = base.immutable();
        BlockPos top = base.above();
        var bottomBefore = level.getBlockState(base);
        var topBefore = level.getBlockState(top);
        var snapshots = List.of(BlockSnapshot.create(level.dimension(), level, base),
                BlockSnapshot.create(level.dimension(), level, top));

        // Keep the two halves from updating each other before both writes and the placement event complete.
        int isolated = Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_SUPPRESS_DROPS;
        level.setBlock(base, lower, isolated);
        level.setBlock(top, upper, isolated);
        if (!level.getBlockState(base).equals(lower) || !level.getBlockState(top).equals(upper)
                || caster != null && ForgeEventFactory.onMultiBlockPlace(caster, snapshots, face)) {
            level.setBlock(base, bottomBefore, isolated);
            level.setBlock(top, topBefore, isolated);
            for (var snapshot : snapshots) snapshot.restore(true, false);
            return false;
        }
        level.sendBlockUpdated(base, bottomBefore, lower, Block.UPDATE_CLIENTS);
        level.sendBlockUpdated(top, topBefore, upper, Block.UPDATE_CLIENTS);
        level.updateNeighborsAt(base, lower.getBlock());
        level.updateNeighborsAt(top, upper.getBlock());
        return true;
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        if (context.getClickedFace() != Direction.UP) return null;
        var state = super.getStateForPlacement(context);
        return state == null ? null : state.setValue(OPEN, false).setValue(POWERED, false);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (alder || !(level instanceof ServerLevel server) || !(placer instanceof Player)) return;
        var key = new ItemEntity(server, placer.getX(), placer.getY() + 0.5, placer.getZ(), DoorKeyItem.forDoor(server, pos));
        var minecraft = server.getServer();
        // Forge can still cancel the two-block item placement after setPlacedBy returns.
        minecraft.tell(new TickTask(minecraft.getTickCount(), () -> {
            if (server.getBlockState(pos).is(this) && server.getBlockState(pos.above()).is(this)) server.addFreshEntity(key);
        }));
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!alder && !DoorKeyItem.canOpen(player, level, lowerHalf(pos, state))) return InteractionResult.PASS;
        setOpen(player, level, state, pos, !isOpen(state));
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void setOpen(@Nullable Entity actor, Level level, BlockState state, BlockPos pos, boolean open) {
        if (!state.is(this) || isOpen(state) == open) return;
        super.setOpen(actor, level, state, pos, open);
        if (alder) {
            BlockPos base = lowerHalf(pos, state);
            level.updateNeighborsAt(base, this);
            level.updateNeighborsAt(base.above(), this);
            level.updateNeighborsAt(base.below(), this);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighbor, BlockPos source, boolean moved) {
        if (alder || neighbor == this || !isOpen(state) || !neighbor.defaultBlockState().isSignalSource()) return;
        BlockPos base = lowerHalf(pos, state);
        if (!level.hasNeighborSignal(base) && !level.hasNeighborSignal(base.above())) setOpen(null, level, state, pos, false);
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return alder;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return alder && isOpen(state) ? 15 : 0;
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return direction == Direction.UP ? getSignal(state, level, pos, direction) : 0;
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, ItemStack tool) {
        super.playerDestroy(level, player, pos, state, blockEntity, tool);
        if (!alder && !player.isCreative()) {
            BlockPos base = lowerHalf(pos, state);
            ItemStack drop = DoorKeyItem.canOpen(player, level, base) ? new ItemStack(this) : new ItemStack(Items.STICK, 24);
            popResource(level, pos, drop);
        }
    }
}
