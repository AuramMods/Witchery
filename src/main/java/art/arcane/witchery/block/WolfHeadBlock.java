package art.arcane.witchery.block;

import art.arcane.witchery.block.entity.WolfHeadBlockEntity;
import art.arcane.witchery.registry.WitcheryItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public final class WolfHeadBlock extends MountedSkullBlock {
    public static final BooleanProperty HELLHOUND = BooleanProperty.create("hellhound");

    public WolfHeadBlock() {
        super(Properties.copy(Blocks.SKELETON_SKULL).noOcclusion().instabreak());
        registerDefaultState(defaultBlockState().setValue(HELLHOUND, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HELLHOUND);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = super.getStateForPlacement(context);
        return state == null ? null : state.setValue(HELLHOUND, context.getItemInHand().is(WitcheryItems.getRequired("hellhound_head").get()));
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return new ItemStack(state.getValue(HELLHOUND) ? WitcheryItems.getRequired("hellhound_head").get() : asItem());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WolfHeadBlockEntity(pos, state);
    }
}
