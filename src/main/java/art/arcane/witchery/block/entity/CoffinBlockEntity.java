package art.arcane.witchery.block.entity;

import art.arcane.witchery.block.CoffinBlock;
import art.arcane.witchery.registry.WitcheryBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class CoffinBlockEntity extends BlockEntity {
    @Nullable private Boolean savedOpen;
    private float openness;
    private float previousOpenness;

    public CoffinBlockEntity(BlockPos pos, BlockState state) {
        super(WitcheryBlockEntities.COFFIN.get(), pos, state);
        openness = previousOpenness = state.getValue(CoffinBlock.OPEN) ? 1 : 0;
    }

    public static void animate(Level level, BlockPos pos, BlockState state, CoffinBlockEntity coffin) {
        coffin.previousOpenness = coffin.openness;
        coffin.openness = Mth.clamp(coffin.openness + (state.getValue(CoffinBlock.OPEN) ? 0.1F : -0.1F), 0, 1);
    }

    public float openness(float partialTick) {
        return Mth.lerp(partialTick, previousOpenness, openness);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (savedOpen != null && level != null && !level.isClientSide && savedOpen != getBlockState().getValue(CoffinBlock.OPEN)) {
            level.setBlock(worldPosition, getBlockState().setValue(CoffinBlock.OPEN, savedOpen), Block.UPDATE_CLIENTS);
        }
        savedOpen = null;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        savedOpen = tag.contains("Opened", Tag.TAG_ANY_NUMERIC) ? tag.getBoolean("Opened") : null;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putBoolean("Opened", getBlockState().getValue(CoffinBlock.OPEN));
    }
}
