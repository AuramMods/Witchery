package art.arcane.witchery.block;

import art.arcane.witchery.block.entity.PlacedOwnerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;

public abstract class OwnedColumnBlock extends Block implements EntityBlock {
    private final boolean requiresSneak;

    protected OwnedColumnBlock(Properties properties, boolean requiresSneak) {
        super(properties);
        this.requiresSneak = requiresSneak;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (!level.isClientSide && placer instanceof Player player
                && level.getBlockEntity(pos) instanceof PlacedOwnerBlockEntity owned) owned.placedBy(player);
    }

    @Override
    public void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (!level.isClientSide && !player.isCreative() && (!requiresSneak || player.isShiftKeyDown())
                && level.getBlockEntity(pos) instanceof PlacedOwnerBlockEntity owned && owned.canRecover(player)) {
            recoverColumn(level, pos);
        }
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide && player.isCreative()) {
            popResource(level, pos, new ItemStack(this));
            recoverColumn(level, pos.above());
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    private void recoverColumn(Level level, BlockPos start) {
        for (BlockPos pos = start; level.getBlockState(pos).is(this); pos = pos.above()) {
            if (level.removeBlock(pos, false)) popResource(level, pos, new ItemStack(this));
        }
    }
}
