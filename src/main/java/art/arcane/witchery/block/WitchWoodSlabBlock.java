package art.arcane.witchery.block;

import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.SlabType;

public final class WitchWoodSlabBlock extends SlabBlock {
    private final boolean fullBlock;

    public WitchWoodSlabBlock(boolean fullBlock) {
        super(Properties.copy(Blocks.OAK_SLAB));
        this.fullBlock = fullBlock;
        registerDefaultState(defaultBlockState().setValue(WitchWoodType.PROPERTY, WitchWoodType.ROWAN)
                .setValue(TYPE, fullBlock ? SlabType.DOUBLE : SlabType.BOTTOM));
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return level.getMaxLightLevel();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(WitchWoodType.PROPERTY);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = fullBlock ? defaultBlockState() : super.getStateForPlacement(context);
        return state.setValue(WitchWoodType.PROPERTY, WitchWoodType.fromStack(context.getItemInHand()));
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return state.getValue(WitchWoodType.PROPERTY) == WitchWoodType.fromStack(context.getItemInHand())
                && super.canBeReplaced(state, context);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        Block item = state.getValue(TYPE) == SlabType.DOUBLE ? WitcheryBlocks.getRequired("witchwooddoubleslab").get() : this;
        return state.getValue(WitchWoodType.PROPERTY).stack(item);
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction face) {
        return state.getValue(WATERLOGGED) ? 0 : 20;
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction face) {
        return state.getValue(WATERLOGGED) ? 0 : 5;
    }
}
