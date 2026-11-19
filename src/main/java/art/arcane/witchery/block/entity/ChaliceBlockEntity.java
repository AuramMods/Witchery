package art.arcane.witchery.block.entity;

import art.arcane.witchery.block.ChaliceBlock;
import art.arcane.witchery.registry.WitcheryBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class ChaliceBlockEntity extends BlockEntity {
    private boolean legacyFilled;

    public ChaliceBlockEntity(BlockPos pos, BlockState state) {
        super(WitcheryBlockEntities.CHALICE.get(), pos, state);
    }

    public static void applyLegacyState(Level level, BlockPos pos, BlockState state, ChaliceBlockEntity chalice) {
        if (chalice.legacyFilled) {
            chalice.legacyFilled = false;
            level.setBlock(pos, state.setValue(ChaliceBlock.FILLED, true), Block.UPDATE_ALL);
            chalice.setChanged();
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        legacyFilled = tag.getBoolean("WITCFilled") && !getBlockState().getValue(ChaliceBlock.FILLED);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("WITCFilled", legacyFilled || getBlockState().getValue(ChaliceBlock.FILLED));
    }
}
