package art.arcane.witchery.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

public final class WitchLeavesBlock extends LeavesBlock {
    public WitchLeavesBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(WitchWoodType.PROPERTY, WitchWoodType.ROWAN));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(WitchWoodType.PROPERTY);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return state.getValue(WitchWoodType.PROPERTY).stack(this);
    }
}
