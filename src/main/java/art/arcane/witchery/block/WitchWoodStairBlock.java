package art.arcane.witchery.block;

import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class WitchWoodStairBlock extends StairBlock {
    private final WitchWoodType wood;

    public WitchWoodStairBlock(WitchWoodType wood) {
        super(() -> WitcheryBlocks.getRequired("witchwood").get().defaultBlockState().setValue(WitchWoodType.PROPERTY, wood),
                Properties.copy(Blocks.OAK_STAIRS));
        this.wood = wood;
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction face) {
        if (state.getValue(WATERLOGGED)) return 0;
        return wood == WitchWoodType.HAWTHORN ? 1 : 20;
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction face) {
        if (state.getValue(WATERLOGGED)) return 0;
        return wood == WitchWoodType.HAWTHORN ? 1 : 5;
    }
}
