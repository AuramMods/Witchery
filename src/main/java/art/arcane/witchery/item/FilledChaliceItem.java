package art.arcane.witchery.item;

import art.arcane.witchery.block.ChaliceBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public final class FilledChaliceItem extends BlockItem {
    public FilledChaliceItem(Block block) {
        super(block, new Properties());
    }

    @Override
    public String getDescriptionId() {
        return "item.witchery.filled_chalice";
    }

    @Override
    @Nullable
    protected BlockState getPlacementState(BlockPlaceContext context) {
        BlockState state = super.getPlacementState(context);
        return state == null ? null : state.setValue(ChaliceBlock.FILLED, true);
    }

    @Override
    public void registerBlocks(Map<Block, Item> blockToItemMap, Item item) {
        // The empty chalice remains the block's default item.
    }
}
