package art.arcane.witchery.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public final class PitDirtBlock extends Block {
    public static final BooleanProperty PODZOL = BooleanProperty.create("podzol");
    public static final EnumProperty<Cover> COVER = EnumProperty.create("cover", Cover.class);

    public PitDirtBlock() {
        super(Properties.copy(Blocks.DIRT).noOcclusion().noCollission());
        registerDefaultState(defaultBlockState().setValue(PODZOL, false).setValue(COVER, Cover.NONE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PODZOL, COVER);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(COVER, coverAbove(context.getLevel().getBlockState(context.getClickedPos().above())));
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level,
                                  BlockPos pos, BlockPos neighborPos) {
        return direction == Direction.UP ? state.setValue(COVER, coverAbove(neighbor)) : state;
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        ItemStack stack = new ItemStack(this);
        if (state.getValue(PODZOL)) setPodzol(stack);
        return stack;
    }

    private static Cover coverAbove(BlockState above) {
        if (above.is(BlockTags.SNOW)) return Cover.SNOW;
        if (above.is(Blocks.DIRT) || above.is(Blocks.GRASS_BLOCK)
                || above.is(Blocks.COARSE_DIRT) || above.is(Blocks.PODZOL)) return Cover.SOIL;
        return Cover.NONE;
    }

    public static boolean isPodzol(ItemStack stack) {
        var data = stack.getTagElement(BlockItem.BLOCK_STATE_TAG);
        return data != null && "true".equals(data.getString(PODZOL.getName()));
    }

    public static void setPodzol(ItemStack stack) {
        stack.getOrCreateTagElement(BlockItem.BLOCK_STATE_TAG).putString(PODZOL.getName(), "true");
    }

    public enum Cover implements StringRepresentable {
        NONE("none"), SOIL("soil"), SNOW("snow");

        private final String name;

        Cover(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
