package art.arcane.witchery.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

public final class WickerBundleBlock extends Block {
    public static final EnumProperty<Type> BUNDLE_TYPE = EnumProperty.create("bundle_type", Type.class);

    public WickerBundleBlock() {
        super(Properties.copy(Blocks.OAK_PLANKS).strength(0.5F).sound(SoundType.GRASS));
        registerDefaultState(defaultBlockState().setValue(BUNDLE_TYPE, Type.PLAIN));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BUNDLE_TYPE);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        ItemStack stack = new ItemStack(this);
        if (state.getValue(BUNDLE_TYPE) == Type.BLOODIED) markBloodied(stack);
        return stack;
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction face) {
        return 20;
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction face) {
        return 20;
    }

    public static void markBloodied(ItemStack stack) {
        stack.getOrCreateTagElement(BlockItem.BLOCK_STATE_TAG).putString(BUNDLE_TYPE.getName(), Type.BLOODIED.getSerializedName());
    }

    public static boolean isBloodied(ItemStack stack) {
        var state = stack.getTagElement(BlockItem.BLOCK_STATE_TAG);
        return state != null && state.getString(BUNDLE_TYPE.getName()).equals(Type.BLOODIED.getSerializedName());
    }

    public enum Type implements StringRepresentable {
        PLAIN("plain"),
        BLOODIED("bloodied");

        private final String name;

        Type(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public static final class Item extends BlockItem {
        public Item(Block block) {
            super(block, new Properties());
        }

        @Override
        public String getDescriptionId(ItemStack stack) {
            return isBloodied(stack) ? getDescriptionId() + ".bloodied" : getDescriptionId();
        }
    }
}
