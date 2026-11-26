package art.arcane.witchery.block;

import art.arcane.witchery.block.entity.DreamCatcherBlockEntity;
import art.arcane.witchery.config.WitcheryServerConfig;
import art.arcane.witchery.item.DreamWeaveItem;
import art.arcane.witchery.registry.WitcheryBlockEntities;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.vampirism.Vampirism;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

public final class DreamCatcherBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<DreamWeave> WEAVE = EnumProperty.create("weave", DreamWeave.class);
    private static final int SLEEP_RANGE = 5;
    private static final Map<ServerLevel, Boolean> SLEEP_PHASES = new WeakHashMap<>();
    private static final VoxelShape NORTH = box(4, 0, 14.65, 12, 14, 16);
    private static final VoxelShape SOUTH = box(4, 0, 0, 12, 14, 1.35);
    private static final VoxelShape WEST = box(14.65, 0, 4, 16, 14, 12);
    private static final VoxelShape EAST = box(0, 0, 4, 1.35, 14, 12);

    public DreamCatcherBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH).setValue(WEAVE, DreamWeave.MOVEMENT));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, WEAVE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction face = context.getClickedFace();
        if (!face.getAxis().isHorizontal()) {
            return null;
        }
        DreamWeave weave = context.getItemInHand().getItem() instanceof DreamWeaveItem item ? item.weave() : DreamWeave.MOVEMENT;
        BlockState state = defaultBlockState().setValue(FACING, face).setValue(WEAVE, weave);
        return state.canSurvive(context.getLevel(), context.getClickedPos()) ? state : null;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos wall = pos.relative(state.getValue(FACING).getOpposite());
        return level.getBlockState(wall).isSolid();
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return !(context.getItemInHand().getItem() instanceof DreamWeaveItem);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level,
                                  BlockPos pos, BlockPos neighborPos) {
        return direction == state.getValue(FACING).getOpposite() && !state.canSurvive(level, pos)
                ? Blocks.AIR.defaultBlockState() : state;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case SOUTH -> SOUTH;
            case WEST -> WEST;
            case EAST -> EAST;
            default -> NORTH;
        };
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return new ItemStack(weaveAt(level, pos, state).item());
    }

    public static DreamWeave weaveAt(BlockGetter level, BlockPos pos, BlockState state) {
        return weave(state, level.getBlockEntity(pos));
    }

    public static DreamWeave weave(BlockState state, @Nullable BlockEntity tile) {
        return tile instanceof DreamCatcherBlockEntity catcher ? catcher.weave() : state.getValue(WEAVE);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder loot) {
        // Earlier saves may carry the fitted weave on the tile until its first tick.
        return super.getDrops(state.setValue(WEAVE, weave(state, loot.getOptionalParameter(LootContextParams.BLOCK_ENTITY))), loot);
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
        return new DreamCatcherBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null
                : createTickerHelper(type, WitcheryBlockEntities.DREAM_CATCHER.get(), DreamCatcherBlockEntity::applyLegacyState);
    }

    public static void tickSleep(ServerLevel level) {
        boolean day = level.isDay();
        Boolean sleepingDuringDay = SLEEP_PHASES.remove(level);
        if (sleepingDuringDay != null && sleepingDuringDay != day) {
            applySleepEffects(level, day);
        }

        int players = 0;
        int sleepers = 0;
        for (ServerPlayer player : level.players()) {
            if (player.isSpectator()) continue;
            ++players;
            if (player.isSleeping()) ++sleepers;
        }
        if (sleepers > 0 && sleepers * 100 >= players * WitcheryServerConfig.PERCENTAGE_OF_PLAYERS_SLEEPING_FOR_BUFF.get()) {
            SLEEP_PHASES.put(level, day);
        }
    }

    private static void applySleepEffects(ServerLevel level, boolean day) {
        for (ServerPlayer player : level.players()) {
            if (player.isSpectator() || Vampirism.isVampire(player) == day) continue;
            BlockPos center = player.blockPosition();
            for (BlockPos pos : BlockPos.betweenClosed(center.offset(-SLEEP_RANGE, -SLEEP_RANGE, -SLEEP_RANGE),
                    center.offset(SLEEP_RANGE, SLEEP_RANGE, SLEEP_RANGE))) {
                if (!level.hasChunkAt(pos)) continue;
                BlockState state = level.getBlockState(pos);
                if (state.getBlock() instanceof DreamCatcherBlock) {
                    applyNearbyWeave(level, pos, weaveAt(level, pos, state), player);
                }
            }
        }
    }

    private static void applyNearbyWeave(ServerLevel level, BlockPos source, DreamWeave weave, ServerPlayer player) {
        boolean rested = true;
        boolean intense = false;
        Block block = WitcheryBlocks.getRequired("dreamcatcher").get();
        for (BlockPos pos : BlockPos.betweenClosed(source.offset(-SLEEP_RANGE, -SLEEP_RANGE, -SLEEP_RANGE),
                source.offset(SLEEP_RANGE, SLEEP_RANGE, SLEEP_RANGE))) {
            if (pos.equals(source) || !level.hasChunkAt(pos)) continue;
            BlockState neighbor = level.getBlockState(pos);
            if (neighbor.is(block)) {
                DreamWeave nearby = weaveAt(level, pos, neighbor);
                rested &= nearby != DreamWeave.NIGHTMARES;
                intense |= nearby == DreamWeave.INTENSITY;
                if (!rested && intense) break;
            }
        }
        weave.apply(player, rested, intense);
    }
}
