package art.arcane.witchery.block;

import art.arcane.witchery.entity.Ent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.Nullable;

public final class WitchLogBlock extends RotatedPillarBlock {
    public static final BooleanProperty BARK = BooleanProperty.create("bark");

    public WitchLogBlock() {
        super(Properties.copy(Blocks.OAK_LOG));
        registerDefaultState(defaultBlockState().setValue(WitchWoodType.PROPERTY, WitchWoodType.ROWAN).setValue(BARK, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(WitchWoodType.PROPERTY, BARK);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.getValue(BARK) ? state : super.rotate(state, rotation);
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction face) {
        return fireValue(state);
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction face) {
        return fireValue(state);
    }

    private static int fireValue(BlockState state) {
        // Only the original metadata value 2 used the lower fire values.
        return state.getValue(WitchWoodType.PROPERTY) == WitchWoodType.HAWTHORN
                && state.getValue(AXIS) == Direction.Axis.Y && !state.getValue(BARK) ? 1 : 5;
    }

    @Override
    public void playerDestroy(Level level, Player player, BlockPos pos, BlockState state,
                              @Nullable BlockEntity entity, ItemStack tool) {
        if (level instanceof ServerLevel server) Ent.tryAwaken(server, pos, this);
        super.playerDestroy(level, player, pos, state, entity, tool);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return state.getValue(WitchWoodType.PROPERTY).stack(this);
    }
}
