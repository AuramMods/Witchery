package art.arcane.witchery.block.entity;

import art.arcane.witchery.registry.WitcheryBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class BloodCrucibleBlockEntity extends BlockEntity {
    private static final int CAPACITY = 20;
    private static final int DEPOSIT = 5;
    private int bloodLevel;

    public BloodCrucibleBlockEntity(BlockPos pos, BlockState state) {
        super(WitcheryBlockEntities.BLOOD_CRUCIBLE.get(), pos, state);
    }

    public boolean isFull() {
        return bloodLevel == CAPACITY;
    }

    public float fullness() {
        return (float) bloodLevel / CAPACITY;
    }

    public void depositBlood() {
        setBloodLevel(Math.min(CAPACITY, bloodLevel + DEPOSIT));
    }

    public void drain() {
        setBloodLevel(0);
    }

    private void setBloodLevel(int amount) {
        if (amount == bloodLevel) return;
        bloodLevel = amount;
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        bloodLevel = Mth.clamp(tag.getInt("BloodLevel"), 0, CAPACITY);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("BloodLevel", bloodLevel);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
