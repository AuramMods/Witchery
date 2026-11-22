package art.arcane.witchery.block;

import art.arcane.witchery.entity.HornedHuntsman;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public final class WickerBundleBlock extends RotatedPillarBlock {
    public static final EnumProperty<Type> BUNDLE_TYPE = EnumProperty.create("bundle_type", Type.class);
    private static final String[] MAN_ROWS = {
            "?.##.?", "?.#?.?", ".#?##.", ".####.", ".####.", "?.##.?", "?.##.?", "??..??"
    };
    private static final String[] BURN_ROWS = {
            "..##..", "..##..", "..##..", "..##..", ".#..#.", "......", "..##.."
    };
    private static final int[] BURN_ORDER = {6, 3, 2, 1, 0, 4};

    public WickerBundleBlock() {
        super(Properties.copy(Blocks.OAK_PLANKS).strength(0.5F).sound(SoundType.GRASS));
        registerDefaultState(defaultBlockState().setValue(BUNDLE_TYPE, Type.PLAIN));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
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

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player,
                                 InteractionHand hand, BlockHitResult hit) {
        if (!player.getItemInHand(hand).is(Items.FLINT_AND_STEEL)) return InteractionResult.PASS;
        if (level instanceof ServerLevel server) return igniteEffigy(server, pos, player.getYRot());
        return locateBase(level, pos) == null ? InteractionResult.PASS : InteractionResult.SUCCESS;
    }

    public static InteractionResult igniteEffigy(ServerLevel level, BlockPos pos, float yaw) {
        Effigy effigy = locateBase(level, pos);
        if (effigy == null) return InteractionResult.PASS;
        // An aligned group still handles the original click when the rest of the figure is incomplete.
        if (!matches(level, effigy.base(), effigy.direction())) return InteractionResult.CONSUME;
        double x = effigy.base().getX() + (effigy.direction() == Direction.EAST ? 1 : 0.5);
        double z = effigy.base().getZ() + (effigy.direction() == Direction.SOUTH ? 1 : 0.5);
        if (!HornedHuntsman.summonFromWicker(level, x, effigy.base().getY() + 0.1, z, yaw + 180)) {
            return InteractionResult.FAIL;
        }
        for (int y : BURN_ORDER) {
            for (int i = 0; i < BURN_ROWS[y].length(); i++) {
                if (BURN_ROWS[y].charAt(i) != '#') continue;
                BlockPos burn = effigy.base().above(y).relative(effigy.direction(), i - 2);
                level.setBlockAndUpdate(burn, BaseFireBlock.getState(level, burn));
            }
        }
        level.sendParticles(ParticleTypes.POOF, x, effigy.base().getY() + 1.5, z, 120, 0.5, 1.25, 0.5, 0.05);
        return InteractionResult.CONSUME;
    }

    private record Effigy(BlockPos base, Direction direction) {}

    @Nullable
    private static Effigy locateBase(BlockGetter level, BlockPos clicked) {
        boolean west = wickerAt(level, clicked.west());
        boolean east = wickerAt(level, clicked.east());
        boolean north = wickerAt(level, clicked.north());
        boolean south = wickerAt(level, clicked.south());
        boolean alongX = west || east;
        if (alongX == (north || south)) return null;

        Direction forwards = alongX ? Direction.EAST : Direction.SOUTH;
        Direction backwards = forwards.getOpposite();
        boolean negative = alongX ? west : north;
        boolean positive = alongX ? east : south;
        BlockPos base = clicked;
        if (!negative) base = base.relative(forwards);
        else if (!positive) base = base.relative(backwards);

        while (wickerAt(level, base.below())) base = base.below();
        while (wickerAt(level, base.relative(backwards))) base = base.relative(backwards);
        return new Effigy(base, forwards);
    }

    private static boolean matches(BlockGetter level, BlockPos base, Direction direction) {
        for (int row = 0; row < MAN_ROWS.length; row++) {
            for (int i = 0; i < MAN_ROWS[row].length(); i++) {
                char slot = MAN_ROWS[row].charAt(i);
                if (slot == '?') continue;
                if (bloodiedAt(level, base.above(row).relative(direction, i - 2)) != (slot == '#')) return false;
            }
        }
        return true;
    }

    private static boolean bloodiedAt(BlockGetter level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.getBlock() instanceof WickerBundleBlock && state.getValue(BUNDLE_TYPE) == Type.BLOODIED;
    }

    private static boolean wickerAt(BlockGetter level, BlockPos pos) {
        return level.getBlockState(pos).getBlock() instanceof WickerBundleBlock;
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
