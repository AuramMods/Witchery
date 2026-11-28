package art.arcane.witchery.block;

import art.arcane.witchery.registry.WitcheryFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class BrewBlock extends LiquidBlock {
    public BrewBlock() {
        super(WitcheryFluids.getRequiredSource("brew"),
                Properties.copy(Blocks.WATER).instabreak().noCollission().noLootTable());
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }
}
