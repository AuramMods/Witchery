package art.arcane.witchery.block.entity;

import art.arcane.witchery.block.BloodRoseBlock;
import art.arcane.witchery.registry.WitcheryBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class BloodRoseBlockEntity extends BlockEntity {
    private final LeechSamples samples = new LeechSamples(1);

    public BloodRoseBlockEntity(BlockPos pos, BlockState state) {
        super(WitcheryBlockEntities.BLOOD_ROSE.get(), pos, state);
    }

    public void sample(ServerPlayer player) {
        if (samples.record(player)) updateFlower();
    }

    @Nullable
    public ServerPlayer collect(ServerPlayer collector) {
        ServerPlayer subject = samples.take(collector, false);
        if (subject != null) updateFlower();
        return subject;
    }

    private void updateFlower() {
        setChanged();
        if (level != null) {
            level.setBlock(worldPosition, getBlockState().setValue(BloodRoseBlock.FULL, samples.size() > 0), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        samples.load(tag);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide && getBlockState().getValue(BloodRoseBlock.FULL) != (samples.size() > 0)) {
            updateFlower();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        samples.save(tag);
    }
}
