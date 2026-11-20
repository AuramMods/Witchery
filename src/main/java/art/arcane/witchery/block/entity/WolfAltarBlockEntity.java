package art.arcane.witchery.block.entity;

import art.arcane.witchery.registry.WitcheryBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

public final class WolfAltarBlockEntity extends BlockEntity {
    public WolfAltarBlockEntity(BlockPos pos, BlockState state) {
        super(WitcheryBlockEntities.WOLF_ALTAR.get(), pos, state);
    }

    @Override
    public AABB getRenderBoundingBox() {
        BlockPos pos = getBlockPos();
        return new AABB(pos.getX() - 0.8, pos.getY(), pos.getZ() - 0.8,
                pos.getX() + 1.8, pos.getY() + 2, pos.getZ() + 1.8);
    }
}
