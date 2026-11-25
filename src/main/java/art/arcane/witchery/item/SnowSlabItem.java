package art.arcane.witchery.item;

import art.arcane.witchery.block.SnowSlabBlock;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.SlabType;

public final class SnowSlabItem extends BlockItem {
    public SnowSlabItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext use) {
        var level = use.getLevel();
        var block = getBlock();
        boolean singleSlab = block instanceof SnowSlabBlock;
        var clicked = level.getBlockState(use.getClickedPos());
        var context = new BlockPlaceContext(use);
        if (singleSlab && clicked.is(block) && SnowSlabBlock.canStackOnFace(clicked, use.getClickedFace())) {
            var result = place(context);
            return result.consumesAction() ? result : InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (singleSlab) {
            var adjacent = use.getClickedPos().relative(use.getClickedFace());
            var neighbor = level.getBlockState(adjacent);
            if (neighbor.is(block) && neighbor.getValue(SnowSlabBlock.TYPE) != SlabType.DOUBLE) {
                var face = neighbor.getValue(SnowSlabBlock.TYPE) == SlabType.BOTTOM ? Direction.UP : Direction.DOWN;
                var result = place(BlockPlaceContext.at(context, adjacent, face));
                return result.consumesAction() ? result : InteractionResult.sidedSuccess(level.isClientSide);
            }
        }

        var target = level.getBlockState(context.getClickedPos());
        if (target.is(block) && target == block.getStateForPlacement(context)) {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return place(context);
    }
}
