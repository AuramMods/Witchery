package art.arcane.witchery.block;

import art.arcane.witchery.brewing.BrewTerrain;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WaterlilyBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraftforge.common.util.FakePlayerFactory;

import java.util.List;

public final class WitchLilyPadBlock extends WaterlilyBlock {
    public static final IntegerProperty GROWTH = IntegerProperty.create("growth", 0, 3);
    private static final int SPREAD_DELAY = 20;
    private static final List<BlockPos> NEIGHBORS = List.of(
            new BlockPos(-1, 0, -1), new BlockPos(-1, 0, -1), new BlockPos(-1, 0, 0),
            new BlockPos(-1, 0, 1), new BlockPos(-1, 0, 1), new BlockPos(0, 0, -1),
            new BlockPos(0, 0, 1), new BlockPos(1, 0, -1), new BlockPos(1, 0, -1),
            new BlockPos(1, 0, 0), new BlockPos(1, 0, 1), new BlockPos(1, 0, 1));

    public WitchLilyPadBlock() {
        super(Properties.copy(Blocks.LILY_PAD));
        registerDefaultState(stateDefinition.any().setValue(GROWTH, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(GROWTH);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.WATER) && state.getFluidState().isSource();
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos support = pos.below();
        return mayPlaceOn(level.getBlockState(support), level, support);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return new ItemStack(Items.LILY_PAD);
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState previous, boolean moved) {
        if (!level.isClientSide && !previous.is(this)) level.scheduleTick(pos, this, SPREAD_DELAY);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.canSurvive(level, pos)) {
            level.destroyBlock(pos, true);
            return;
        }
        int growth = state.getValue(GROWTH);
        if (growth == 0) return;
        BlockPos target = pos.offset(NEIGHBORS.get(random.nextInt(NEIGHBORS.size())));
        BlockState offspring = defaultBlockState().setValue(GROWTH, growth - 1);
        if (level.hasChunkAt(target) && level.isEmptyBlock(target) && offspring.canSurvive(level, target)
                && BrewTerrain.setBlock(FakePlayerFactory.getMinecraft(level), level, target, offspring, Direction.UP)
                && random.nextInt(growth) == 0) {
            state = state.setValue(GROWTH, growth - 1);
            level.setBlockAndUpdate(pos, state);
        }
        if (state.getValue(GROWTH) > 0) level.scheduleTick(pos, this, SPREAD_DELAY);
    }
}
