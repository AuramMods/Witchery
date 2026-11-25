package art.arcane.witchery.block;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import org.jetbrains.annotations.Nullable;

public final class PerpetualIceDoorBlock extends DoorBlock {
    public PerpetualIceDoorBlock() {
        super(Properties.copy(Blocks.OAK_DOOR).strength(2, 3), BlockSetType.OAK);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return context.getClickedFace() == Direction.UP ? super.getStateForPlacement(context) : null;
    }
}
