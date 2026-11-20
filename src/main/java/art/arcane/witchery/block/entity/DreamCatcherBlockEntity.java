package art.arcane.witchery.block.entity;

import art.arcane.witchery.block.DreamCatcherBlock;
import art.arcane.witchery.block.DreamWeave;
import art.arcane.witchery.registry.WitcheryBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class DreamCatcherBlockEntity extends BlockEntity {
    private static final String WEAVE_ID = "WITCWeaveID";
    private DreamWeave importedWeave;

    public DreamCatcherBlockEntity(BlockPos pos, BlockState state) {
        super(WitcheryBlockEntities.DREAM_CATCHER.get(), pos, state);
    }

    public static void applyLegacyState(Level level, BlockPos pos, BlockState state, DreamCatcherBlockEntity catcher) {
        if (catcher.importedWeave != null) {
            DreamWeave weave = catcher.importedWeave;
            catcher.importedWeave = null;
            level.setBlock(pos, state.setValue(DreamCatcherBlock.WEAVE, weave), Block.UPDATE_ALL);
            catcher.setChanged();
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains(WEAVE_ID, Tag.TAG_ANY_NUMERIC)) {
            DreamWeave weave = DreamWeave.fromLegacyId(tag.getInt(WEAVE_ID));
            importedWeave = weave == getBlockState().getValue(DreamCatcherBlock.WEAVE) ? null : weave;
        } else {
            importedWeave = null;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        DreamWeave weave = importedWeave != null ? importedWeave : getBlockState().getValue(DreamCatcherBlock.WEAVE);
        tag.putInt(WEAVE_ID, weave.ordinal());
    }
}
