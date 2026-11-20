package art.arcane.witchery.block.entity;

import art.arcane.witchery.registry.WitcheryBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class CrystalBallBlockEntity extends BlockEntity {
    private long lastRead;

    public CrystalBallBlockEntity(BlockPos pos, BlockState state) {
        super(WitcheryBlockEntities.CRYSTAL_BALL.get(), pos, state);
    }

    public boolean ready(long gameTime) {
        return gameTime - lastRead > 100;
    }

    public void readAt(long gameTime) {
        lastRead = gameTime;
        setChanged();
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        lastRead = tag.getLong("LastUsedTime");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putLong("LastUsedTime", lastRead);
    }
}
