package art.arcane.witchery.block.entity;

import art.arcane.witchery.registry.WitcheryBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class WolfHeadBlockEntity extends BlockEntity {
    public WolfHeadBlockEntity(BlockPos pos, BlockState state) {
        super(WitcheryBlockEntities.WOLF_HEAD.get(), pos, state);
    }
}
