package art.arcane.witchery.block;

import art.arcane.witchery.block.entity.MirrorBlockEntity;
import art.arcane.witchery.entity.MirrorFace;
import art.arcane.witchery.registry.WitcheryBlockEntities;
import art.arcane.witchery.registry.WitcheryItems;
import art.arcane.witchery.world.MirrorTravel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class MirrorBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    private final boolean unbreakable;

    public MirrorBlock(Properties properties, boolean unbreakable) {
        super(properties);
        this.unbreakable = unbreakable;
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH).setValue(HALF, DoubleBlockHalf.UPPER));
    }

    public boolean isRoomExit() {
        return unbreakable;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, HALF);
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return rotate(state, mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MirrorBlockEntity(unbreakable ? WitcheryBlockEntities.ROOM_MIRROR.get() : WitcheryBlockEntities.MIRROR.get(), pos, state);
    }

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        BlockEntityType<MirrorBlockEntity> mirrorType = unbreakable
                ? WitcheryBlockEntities.ROOM_MIRROR.get() : WitcheryBlockEntities.MIRROR.get();
        if (level.isClientSide()) return createTickerHelper(type, mirrorType, MirrorBlockEntity::tickGlass);
        if (state.getValue(HALF) != DoubleBlockHalf.UPPER) return null;
        return createTickerHelper(type, mirrorType, unbreakable ? MirrorTravel::tickRoom : MirrorBlockEntity::tickMirror);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hit) {
        if (!unbreakable && level instanceof ServerLevel server && player instanceof ServerPlayer speaker) {
            MirrorBlockEntity mirror = upperEntity(server, pos, state);
            if (mirror != null) mirror.interact(server, speaker, hand);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    public boolean placePair(Level level, BlockPos upper, Direction facing) {
        BlockPos lower = upper.below();
        if (!level.isInWorldBounds(upper) || !level.isInWorldBounds(lower)
                || !level.isEmptyBlock(upper) || !level.isEmptyBlock(lower)) {
            return false;
        }
        BlockState frame = defaultBlockState().setValue(FACING, facing);
        level.setBlock(lower, frame.setValue(HALF, DoubleBlockHalf.LOWER), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        level.setBlock(upper, frame, Block.UPDATE_ALL);
        return level.getBlockState(lower).is(this) && level.getBlockState(upper).is(this);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return panel(state.getValue(FACING), 5.12);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return panel(state.getValue(FACING), 2.4);
    }

    private static VoxelShape panel(Direction facing, double thickness) {
        return switch (facing) {
            case NORTH -> box(0, 0, 16 - thickness, 16, 16, 16);
            case SOUTH -> box(0, 0, 0, 16, 16, thickness);
            case WEST -> box(16 - thickness, 0, 0, 16, 16, 16);
            default -> box(0, 0, 0, thickness, 16, 16);
        };
    }

    @Override
    public BlockState updateShape(BlockState state, Direction side, BlockState neighbor, LevelAccessor level,
                                  BlockPos pos, BlockPos neighborPos) {
        boolean upper = state.getValue(HALF) == DoubleBlockHalf.UPPER;
        if (side == (upper ? Direction.DOWN : Direction.UP)
                && (!neighbor.is(this) || neighbor.getValue(HALF) == state.getValue(HALF)
                || neighbor.getValue(FACING) != state.getValue(FACING))) {
            return net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        }
        return state;
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (!level.isClientSide() && player.isCreative() && state.getValue(HALF) == DoubleBlockHalf.LOWER) {
            BlockPos upper = pos.above();
            if (level.getBlockState(upper).is(this)) {
                level.setBlock(upper, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),
                        Block.UPDATE_ALL | Block.UPDATE_SUPPRESS_DROPS);
            }
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        if (unbreakable || state.getValue(HALF) != DoubleBlockHalf.UPPER) return List.of();
        BlockEntity data = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        return List.of(data instanceof MirrorBlockEntity mirror ? mirror.toItem() : new ItemStack(WitcheryItems.getRequired("mirror").get()));
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        MirrorBlockEntity upper = upperEntity(level, pos, state);
        return upper == null || unbreakable ? new ItemStack(WitcheryItems.getRequired("mirror").get()) : upper.toItem();
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean piston) {
        if (!state.is(replacement.getBlock()) && state.getValue(HALF) == DoubleBlockHalf.UPPER
                && level instanceof ServerLevel server && level.getBlockEntity(pos) instanceof MirrorBlockEntity mirror) {
            MirrorTravel.unlink(server, mirror);
        }
        super.onRemove(state, level, pos, replacement, piston);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!(level instanceof ServerLevel server) || entity instanceof MirrorFace
                || entity instanceof LivingEntity && entity.getDirection() != state.getValue(FACING).getOpposite()
                || !panel(state.getValue(FACING), 5.12).bounds().move(pos).intersects(entity.getBoundingBox())) {
            return;
        }
        MirrorBlockEntity upper = upperEntity(level, pos, state);
        if (upper != null) MirrorTravel.walkThrough(server, upper, entity);
    }

    @Nullable
    private static MirrorBlockEntity upperEntity(BlockGetter level, BlockPos pos, BlockState state) {
        BlockPos upper = state.getValue(HALF) == DoubleBlockHalf.UPPER ? pos : pos.above();
        return level.getBlockEntity(upper) instanceof MirrorBlockEntity mirror ? mirror : null;
    }
}
