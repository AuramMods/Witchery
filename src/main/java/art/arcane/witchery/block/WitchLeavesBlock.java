package art.arcane.witchery.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import java.util.ArrayDeque;
import java.util.HashSet;

public final class WitchLeavesBlock extends LeavesBlock {
    public static final BooleanProperty CHECK_DECAY = BooleanProperty.create("check_decay");
    private static final int SUPPORT_DISTANCE = 4;

    public WitchLeavesBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(WitchWoodType.PROPERTY, WitchWoodType.ROWAN)
                .setValue(CHECK_DECAY, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(WitchWoodType.PROPERTY, CHECK_DECAY);
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return state.getValue(CHECK_DECAY) && !state.getValue(PERSISTENT);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!isRandomlyTicking(state) || !level.isAreaLoaded(pos, SUPPORT_DISTANCE + 1)) return;
        if (hasSupportingLog(level, pos)) {
            level.setBlock(pos, state.setValue(CHECK_DECAY, false), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
        } else {
            dropResources(state, level, pos);
            level.removeBlock(pos, false);
        }
    }

    private static boolean hasSupportingLog(BlockGetter level, BlockPos start) {
        var open = new ArrayDeque<BlockPos>();
        var visited = new HashSet<BlockPos>();
        var directions = Direction.values();
        open.add(start);
        visited.add(start);
        // The original check traversed four faces through any leaves supported by a log.
        for (int distance = 1; distance <= SUPPORT_DISTANCE && !open.isEmpty(); ++distance) {
            for (int left = open.size(); left > 0; --left) {
                BlockPos leaf = open.removeFirst();
                for (Direction direction : directions) {
                    BlockPos next = leaf.relative(direction);
                    if (!visited.add(next)) continue;
                    BlockState neighbor = level.getBlockState(next);
                    if (neighbor.is(BlockTags.LOGS)) return true;
                    if (distance < SUPPORT_DISTANCE && neighbor.is(BlockTags.LEAVES)) open.addLast(next);
                }
            }
        }
        return false;
    }

    public static void checkAfterRemoval(ServerLevel level, BlockPos pos, BlockState previous, BlockState replacement) {
        if (previous.is(replacement.getBlock())) return;
        int radius = previous.is(BlockTags.LOGS) ? SUPPORT_DISTANCE : previous.is(BlockTags.LEAVES) ? 1 : 0;
        if (radius == 0 || !level.isAreaLoaded(pos, radius + 1) || level.getBlockState(pos).is(previous.getBlock())) return;
        for (BlockPos neighbor : BlockPos.betweenClosed(pos.offset(-radius, -radius, -radius), pos.offset(radius, radius, radius))) {
            BlockState candidate = level.getBlockState(neighbor);
            if (candidate.getBlock() instanceof WitchLeavesBlock && !candidate.getValue(CHECK_DECAY)) {
                level.setBlock(neighbor, candidate.setValue(CHECK_DECAY, true), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
            }
        }
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return state.getValue(WitchWoodType.PROPERTY).stack(this);
    }
}
