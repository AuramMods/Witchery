package art.arcane.witchery.block;

import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.GlassBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import org.jetbrains.annotations.Nullable;

public final class ShadedGlassBlock extends GlassBlock {
    public static final EnumProperty<Color> COLOR = EnumProperty.create("color", Color.class);
    private final boolean darkened;

    public ShadedGlassBlock(boolean darkened) {
        super(darkened ? Properties.copy(Blocks.TINTED_GLASS).requiresCorrectToolForDrops() : Properties.copy(Blocks.GLASS));
        this.darkened = darkened;
        registerDefaultState(defaultBlockState().setValue(COLOR, Color.WHITE));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(COLOR);
    }

    @Override
    public int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return darkened ? level.getMaxLightLevel() : super.getLightBlock(state, level, pos);
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return !darkened;
    }

    @Override
    public boolean skipRendering(BlockState state, BlockState adjacent, Direction face) {
        return adjacent.is(this) ? state.getValue(COLOR) == adjacent.getValue(COLOR) : super.skipRendering(state, adjacent, face);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return poweredGlass(context.getLevel(), context.getClickedPos()).defaultBlockState()
                .setValue(COLOR, Color.fromStack(context.getItemInHand()));
    }

    @Override
    public void onPlace(BlockState state, Level level, BlockPos pos, BlockState previous, boolean moved) {
        if (!level.isClientSide && !previous.is(this) && poweredGlass(level, pos) != this) {
            level.scheduleTick(pos, this, 1);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        updatePower(state, level, pos);
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighbor, BlockPos neighborPos, boolean moved) {
        if (!level.isClientSide) updatePower(state, level, pos);
    }

    private void updatePower(BlockState state, Level level, BlockPos pos) {
        Block next = poweredGlass(level, pos);
        if (next != this) level.setBlockAndUpdate(pos, next.defaultBlockState().setValue(COLOR, state.getValue(COLOR)));
    }

    private static Block poweredGlass(Level level, BlockPos pos) {
        return WitcheryBlocks.getRequired(level.hasNeighborSignal(pos) ? "shadedglass_active" : "shadedglass").get();
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return state.getValue(COLOR).stack(asItem());
    }

    public enum Color implements StringRepresentable {
        WHITE, ORANGE, MAGENTA, LIGHT_BLUE, YELLOW, LIME, PINK, GRAY, SILVER, CYAN, PURPLE, BLUE, BROWN, GREEN, RED, BLACK;

        private static final Color[] BY_METADATA = values();

        public DyeColor dye() {
            return DyeColor.byId(ordinal());
        }

        @Override
        public String getSerializedName() {
            return this == SILVER ? "silver" : dye().getName();
        }

        public static Color fromLegacyMetadata(int metadata) {
            return metadata >= 0 && metadata < BY_METADATA.length ? BY_METADATA[metadata] : WHITE;
        }

        public static Color fromStack(ItemStack stack) {
            var state = stack.getTagElement(BlockItem.BLOCK_STATE_TAG);
            return state == null ? WHITE : COLOR.getValue(state.getString(COLOR.getName())).orElse(WHITE);
        }

        public static void importLegacyMetadata(ItemStack stack, int metadata) {
            var tag = stack.getTagElement(BlockItem.BLOCK_STATE_TAG);
            Color color = fromLegacyMetadata(metadata);
            if (color != WHITE && (tag == null || COLOR.getValue(tag.getString(COLOR.getName())).isEmpty())) color.write(stack);
        }

        public ItemStack stack(ItemLike item) {
            ItemStack stack = new ItemStack(item);
            if (this != WHITE) write(stack);
            return stack;
        }

        private void write(ItemStack stack) {
            stack.getOrCreateTagElement(BlockItem.BLOCK_STATE_TAG).putString(COLOR.getName(), getSerializedName());
        }
    }

    public static final class Item extends BlockItem {
        public Item(Block block) {
            super(block, new Properties());
        }

        public boolean isDarkened() {
            return ((ShadedGlassBlock) getBlock()).darkened;
        }

        @Override
        public Component getName(ItemStack stack) {
            var color = Component.translatable("color.minecraft." + Color.fromStack(stack).dye().getName());
            return Component.translatable("item.witchery.shadedglass_colored", color, super.getName(stack));
        }
    }
}
