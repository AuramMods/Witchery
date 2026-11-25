package art.arcane.witchery.block;

import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

public final class PerpetualIceFenceBlock extends FenceBlock {
    public PerpetualIceFenceBlock() {
        super(Properties.copy(Blocks.OAK_FENCE).strength(2, 3).sound(SoundType.GLASS));
    }

    @Override
    public boolean connectsTo(BlockState other, boolean sturdy, Direction direction) {
        return other.is(this) || other.is(WitcheryBlocks.getRequired("icefencegate").get())
                || super.connectsTo(other, sturdy, direction);
    }
}
