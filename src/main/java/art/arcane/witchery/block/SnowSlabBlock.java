package art.arcane.witchery.block;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.PushReaction;

public final class SnowSlabBlock extends SlabBlock {
    public SnowSlabBlock() {
        super(Properties.copy(Blocks.SNOW_BLOCK).pushReaction(PushReaction.DESTROY));
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return true;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        var pos = context.getClickedPos();
        var existing = context.getLevel().getBlockState(pos);
        var face = context.getClickedFace();
        if (!existing.is(this) || canStackOnFace(existing, face)) return super.getStateForPlacement(context);

        // The original falls back to ordinary half-slab placement when the clicked face cannot merge.
        boolean top = face == Direction.DOWN || face.getAxis().isHorizontal()
                && context.getClickLocation().y - pos.getY() > 0.5;
        return existing.setValue(TYPE, top ? SlabType.TOP : SlabType.BOTTOM);
    }

    public static boolean canStackOnFace(BlockState state, Direction face) {
        return state.getValue(TYPE) == SlabType.BOTTOM && face == Direction.UP
                || state.getValue(TYPE) == SlabType.TOP && face == Direction.DOWN;
    }

    public static final class DoubleSlab extends Block {
        public DoubleSlab() {
            super(Properties.copy(Blocks.SNOW_BLOCK).pushReaction(PushReaction.DESTROY));
        }

        @Override
        public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
            return true;
        }
    }
}
