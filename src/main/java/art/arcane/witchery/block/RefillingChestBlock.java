package art.arcane.witchery.block;

import art.arcane.witchery.block.entity.RefillingChestBlockEntity;
import art.arcane.witchery.registry.WitcheryBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class RefillingChestBlock extends LegacyPairingChestBlock {
    public RefillingChestBlock() {
        super(Properties.copy(Blocks.CHEST).strength(-1, 9_999).noLootTable(), WitcheryBlockEntities.REFILLING_CHEST::get);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RefillingChestBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide() ? super.getTicker(level, state, type)
                : createTickerHelper(type, WitcheryBlockEntities.REFILLING_CHEST.get(), RefillingChestBlockEntity::tick);
    }
}
