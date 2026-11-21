package art.arcane.witchery.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.IceBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class PerpetualIceBlock extends IceBlock {
    public PerpetualIceBlock() {
        super(Properties.copy(Blocks.ICE).friction(0.98F).strength(2, 5));
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {}

    @Override
    public int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return 3;
    }
}
