package art.arcane.witchery.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.ChestType;

import java.util.function.Supplier;

public abstract class LegacyPairingChestBlock extends ChestBlock {
    public static final BooleanProperty LEGACY_PAIRING = BooleanProperty.create("legacy_pairing");

    protected LegacyPairingChestBlock(Properties properties, Supplier<BlockEntityType<? extends ChestBlockEntity>> type) {
        super(properties, type);
        registerDefaultState(defaultBlockState().setValue(LEGACY_PAIRING, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LEGACY_PAIRING);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level,
                                  BlockPos pos, BlockPos neighborPos) {
        if (!state.getValue(LEGACY_PAIRING)) return super.updateShape(state, direction, neighbor, level, pos, neighborPos);
        Direction facing = state.getValue(FACING);
        boolean joinsClockwise = canPair(level.getBlockState(pos.relative(facing.getClockWise())), facing, ChestType.RIGHT);
        boolean joinsCounterClockwise = canPair(level.getBlockState(pos.relative(facing.getCounterClockWise())), facing, ChestType.LEFT);
        ChestType type = ChestType.SINGLE;
        if (joinsClockwise != joinsCounterClockwise) type = joinsClockwise ? ChestType.LEFT : ChestType.RIGHT;
        return state.setValue(TYPE, type).setValue(LEGACY_PAIRING, false);
    }

    private boolean canPair(BlockState neighbor, Direction facing, ChestType otherHalf) {
        return neighbor.is(this) && neighbor.getValue(FACING) == facing
                && (neighbor.getValue(LEGACY_PAIRING) || neighbor.getValue(TYPE) == otherHalf);
    }
}
