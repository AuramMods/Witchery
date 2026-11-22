package art.arcane.witchery.block;

import art.arcane.witchery.block.entity.CoffinBlockEntity;
import art.arcane.witchery.registry.WitcheryBlockEntities;
import art.arcane.witchery.registry.WitcheryItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public final class CoffinBlock extends BedBlock {
    public static final BooleanProperty OPEN = BlockStateProperties.OPEN;
    private static final BooleanProperty PAIRED = BooleanProperty.create("paired");
    private static final VoxelShape[][] OPEN_SHAPES = new VoxelShape[BedPart.values().length][4];

    static {
        for (BedPart part : BedPart.values()) {
            for (Direction facing : Direction.Plane.HORIZONTAL) {
                Direction inside = part == BedPart.FOOT ? facing : facing.getOpposite();
                Direction lid = facing.getCounterClockWise();
                VoxelShape shape = box(0, 0, 0, 16, 7, 16);
                for (Direction edge : Direction.Plane.HORIZONTAL) {
                    if (edge == inside) continue;
                    int height = edge == lid ? 32 : 16;
                    VoxelShape wall = switch (edge) {
                        case NORTH -> box(0, 7, 0, 16, height, 0.8);
                        case SOUTH -> box(0, 7, 15.2, 16, height, 16);
                        case WEST -> box(0, 7, 0, 0.8, height, 16);
                        case EAST -> box(15.2, 7, 0, 16, height, 16);
                        default -> throw new IllegalStateException("Invalid coffin wall direction: " + edge);
                    };
                    shape = Shapes.or(shape, wall);
                }
                OPEN_SHAPES[part.ordinal()][facing.get2DDataValue()] = shape;
            }
        }
    }

    public CoffinBlock() {
        super(DyeColor.BLACK, Properties.copy(Blocks.BLACK_BED).strength(1));
        registerDefaultState(defaultBlockState().setValue(OPEN, false).setValue(PAIRED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(OPEN, PAIRED);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        if (context.getClickedFace() != Direction.UP) return null;
        BlockState state = super.getStateForPlacement(context);
        if (state == null) return null;
        Level level = context.getLevel();
        BlockPos foot = context.getClickedPos();
        BlockPos head = foot.relative(state.getValue(FACING));
        return supported(level, foot) && supported(level, head) ? state.setValue(PAIRED, true) : null;
    }

    private static boolean supported(Level level, BlockPos pos) {
        BlockPos below = pos.below();
        return level.getBlockState(below).isFaceSturdy(level, below, Direction.UP);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level,
                                  BlockPos pos, BlockPos neighborPos) {
        if (direction == getConnectedDirection(state)) {
            if (matches(state, neighbor)) {
                BlockState head = state.getValue(PART) == BedPart.HEAD ? state : neighbor;
                return state.setValue(PAIRED, true).setValue(OCCUPIED, head.getValue(OCCUPIED));
            }
            // Older 1.20.1 versions placed a single decorative block under this ID.
            return state.getValue(PAIRED) ? Blocks.AIR.defaultBlockState() : state;
        }
        return super.updateShape(state, direction, neighbor, level, pos, neighborPos);
    }

    private boolean matches(BlockState state, BlockState other) {
        return other.is(this) && state.getValue(FACING) == other.getValue(FACING) && state.getValue(PART) != other.getValue(PART);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level instanceof ServerLevel server)) return InteractionResult.SUCCESS;
        Direction facing = state.getValue(FACING);
        BlockPos head = state.getValue(PART) == BedPart.HEAD ? pos : pos.relative(facing);
        BlockPos foot = head.relative(facing.getOpposite());
        BlockState headState = level.getBlockState(head);
        BlockState footState = level.getBlockState(foot);
        if (!headState.is(this) || headState.getValue(PART) != BedPart.HEAD || !matches(headState, footState)) {
            return InteractionResult.CONSUME;
        }

        boolean occupied = server.players().stream().anyMatch(sleeper -> sleeper.getSleepingPos().filter(head::equals).isPresent());
        if (player.isShiftKeyDown()) {
            if (occupied || blocksLid(level, head) || blocksLid(level, foot)) return InteractionResult.CONSUME;
            boolean open = !headState.getValue(OPEN);
            level.setBlockAndUpdate(head, headState.setValue(OPEN, open).setValue(OCCUPIED, false));
            BlockState updatedFoot = level.getBlockState(foot);
            level.setBlockAndUpdate(foot, updatedFoot.setValue(OPEN, open).setValue(OCCUPIED, false));
            level.playSound(null, foot, open ? SoundEvents.CHEST_OPEN : SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS, 0.5F, 1);
            return InteractionResult.CONSUME;
        }
        if (!headState.getValue(OPEN)) {
            player.displayClientMessage(Component.translatable("message.witchery.coffin.closed"), true);
            return InteractionResult.CONSUME;
        }
        if (headState.getValue(OCCUPIED) && !occupied) {
            headState = headState.setValue(OCCUPIED, false);
            level.setBlockAndUpdate(head, headState);
        }
        return super.use(headState, level, head, player, hand, hit);
    }

    private static boolean blocksLid(Level level, BlockPos pos) {
        BlockPos above = pos.above();
        return level.getBlockState(above).isFaceSturdy(level, above, Direction.DOWN);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (!state.getValue(OPEN)) return Shapes.block();
        return OPEN_SHAPES[state.getValue(PART).ordinal()][state.getValue(FACING).get2DDataValue()];
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float distance) {
        entity.causeFallDamage(distance, 1, level.damageSources().fall());
    }

    @Override
    public void updateEntityAfterFallOn(BlockGetter level, Entity entity) {
        Vec3 movement = entity.getDeltaMovement();
        entity.setDeltaMovement(movement.x, 0, movement.z);
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return new ItemStack(WitcheryItems.getRequired("coffin").get());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CoffinBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (!level.isClientSide || type != WitcheryBlockEntities.COFFIN.get()) return null;
        return (world, pos, current, entity) -> CoffinBlockEntity.animate(world, pos, current, (CoffinBlockEntity) entity);
    }
}
