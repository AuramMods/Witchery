package art.arcane.witchery.block;

import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

public class StockadeBlock extends FenceBlock {
    public static final BooleanProperty COVERED = BooleanProperty.create("covered");
    public static final EnumProperty<Wood> WOOD = EnumProperty.create("wood_type", Wood.class);

    public enum Wood implements StringRepresentable {
        OAK("oak", () -> Blocks.OAK_LOG),
        SPRUCE("spruce", () -> Blocks.SPRUCE_LOG),
        BIRCH("birch", () -> Blocks.BIRCH_LOG),
        JUNGLE("jungle", () -> Blocks.JUNGLE_LOG),
        ROWAN(WitchWoodType.ROWAN),
        ALDER(WitchWoodType.ALDER),
        HAWTHORN(WitchWoodType.HAWTHORN),
        ACACIA("acacia", () -> Blocks.ACACIA_LOG),
        DARK_OAK("dark_oak", () -> Blocks.DARK_OAK_LOG);

        private static final Wood[] BY_META = values();
        private final String name;
        private final Supplier<Block> log;
        @Nullable private final WitchWoodType witchWood;

        Wood(String name, Supplier<Block> log) {
            this(name, log, null);
        }

        Wood(WitchWoodType wood) {
            this(wood.getSerializedName(), () -> WitcheryBlocks.getRequired("witchlog").get(), wood);
        }

        Wood(String name, Supplier<Block> log, @Nullable WitchWoodType witchWood) {
            this.name = name;
            this.log = log;
            this.witchWood = witchWood;
        }

        @Override
        public String getSerializedName() {
            return name;
        }

        public Block log() {
            return log.get();
        }

        @Nullable
        public WitchWoodType witchWood() {
            return witchWood;
        }

        public ItemStack stack(Block block) {
            var stack = new ItemStack(block);
            write(stack);
            return stack;
        }

        private void write(ItemStack stack) {
            if (this != OAK) stack.getOrCreateTagElement(BlockItem.BLOCK_STATE_TAG).putString(WOOD.getName(), name);
        }

        public static Wood fromStack(ItemStack stack) {
            var tag = stack.getTagElement(BlockItem.BLOCK_STATE_TAG);
            return tag == null ? OAK : WOOD.getValue(tag.getString(WOOD.getName())).orElse(OAK);
        }

        public static void importLegacyMetadata(ItemStack stack, int metadata) {
            fromLegacyMetadata(metadata).write(stack);
        }

        public static Wood fromLegacyMetadata(int metadata) {
            return metadata >= 0 && metadata < BY_META.length ? BY_META[metadata] : OAK;
        }
    }

    private static final VoxelShape POST = box(4.8, 0, 4.8, 11.2, 14.4, 11.2);
    private static final VoxelShape LINE_NS = box(4.8, 0, 0.8, 11.2, 14.4, 15.2);
    private static final VoxelShape LINE_EW = box(0.8, 0, 4.8, 15.2, 14.4, 11.2);
    private static final VoxelShape LINE_EW_COLLISION = box(0.8, 0, 4.8, 8.8, 14.4, 11.2);
    private static final VoxelShape CROSS_OUTLINE = box(0.8, 0, 0.8, 15.2, 16, 15.2);
    private static final VoxelShape CROSS_COLLISION = Shapes.or(
            box(4.8, 0, 0.8, 11.2, 16, 15.2), box(0.8, 0, 4.8, 8.8, 16, 11.2));

    public StockadeBlock(boolean frozen) {
        super(Properties.copy(Blocks.OAK_FENCE).strength(25, 12)
                .sound(frozen ? SoundType.GLASS : SoundType.WOOD));
        registerDefaultState(defaultBlockState().setValue(COVERED, false));
    }

    @Override
    public boolean connectsTo(BlockState other, boolean sturdy, Direction direction) {
        return joins(other);
    }

    private boolean joins(BlockState other) {
        return other.is(this) || other.is(Blocks.OAK_FENCE_GATE)
                || other.is(WitcheryBlocks.getRequired("icefencegate").get());
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context)
                .setValue(COVERED, joins(context.getLevel().getBlockState(context.getClickedPos().above())));
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState other, LevelAccessor level,
                                  BlockPos pos, BlockPos otherPos) {
        state = super.updateShape(state, direction, other, level, pos, otherPos);
        return direction == Direction.UP ? state.setValue(COVERED, joins(other)) : state;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (alongNorthSouth(state)) return alongEastWest(state) ? CROSS_OUTLINE : LINE_NS;
        return alongEastWest(state) ? LINE_EW : POST;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (alongNorthSouth(state)) return alongEastWest(state) ? CROSS_COLLISION : LINE_NS;
        return alongEastWest(state) ? LINE_EW_COLLISION : POST;
    }

    public static boolean alongNorthSouth(BlockState state) {
        return state.getValue(NORTH) || state.getValue(SOUTH);
    }

    public static boolean alongEastWest(BlockState state) {
        return state.getValue(EAST) || state.getValue(WEST);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide && entity instanceof LivingEntity living) {
            living.hurt(level.damageSources().cactus(), 3);
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        return InteractionResult.PASS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(COVERED);
    }

    public static final class Wooden extends StockadeBlock {
        public Wooden() {
            super(false);
            registerDefaultState(defaultBlockState().setValue(WOOD, Wood.OAK));
        }

        @Override
        public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
            return state.getValue(WOOD).stack(this);
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            super.createBlockStateDefinition(builder);
            builder.add(WOOD);
        }
    }

    public static final class Item extends BlockItem {
        public Item(Block block) {
            super(block, new Properties());
        }

        @Override
        public String getDescriptionId(ItemStack stack) {
            return getDescriptionId() + "." + Wood.fromStack(stack).getSerializedName();
        }
    }
}
