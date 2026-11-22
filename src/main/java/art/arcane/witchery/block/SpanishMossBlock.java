package art.arcane.witchery.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;

public final class SpanishMossBlock extends VineBlock {
    public SpanishMossBlock() {
        super(Properties.copy(Blocks.VINE).strength(0.2F));
    }

    public BlockState stateFromLegacy(int metadata) {
        return defaultBlockState().setValue(UP, metadata == 0)
                .setValue(SOUTH, (metadata & 1) != 0).setValue(WEST, (metadata & 2) != 0)
                .setValue(NORTH, (metadata & 4) != 0).setValue(EAST, (metadata & 8) != 0);
    }

    public BlockState mutatedState(BlockGetter level, BlockPos pos) {
        BlockState attached = inferAttachments(level, pos);
        // The original mutation places a south-facing strand even when it has no nearby support.
        return hasFaces(attached) ? attached : defaultBlockState().setValue(SOUTH, true);
    }

    private BlockState inferAttachments(BlockGetter level, BlockPos pos) {
        BlockPos above = pos.above();
        BlockState overhang = level.getBlockState(above);
        BlockState attached = defaultBlockState().setValue(UP, isAcceptableNeighbour(level, above, Direction.UP));
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            var face = getPropertyForFace(direction);
            if (isAcceptableNeighbour(level, pos.relative(direction), direction)
                    || overhang.is(this) && overhang.getValue(face)) attached = attached.setValue(face, true);
        }
        return attached;
    }

    private static boolean hasFaces(BlockState state) {
        return state.getValue(UP) || state.getValue(NORTH) || state.getValue(SOUTH) || state.getValue(EAST) || state.getValue(WEST);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction side, BlockState neighbor, LevelAccessor level,
                                  BlockPos pos, BlockPos neighborPos) {
        // Early 1.20.1 saves had a decorative plant without attachment properties.
        return hasFaces(state) ? super.updateShape(state, side, neighbor, level, pos, neighborPos) : inferAttachments(level, pos);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!level.isAreaLoaded(pos, 5)) return;
        if (!hasFaces(state)) {
            BlockState attached = inferAttachments(level, pos);
            if (hasFaces(attached)) level.setBlock(pos, attached, Block.UPDATE_CLIENTS);
            return;
        }
        super.randomTick(state, level, pos, random);
        super.randomTick(level.getBlockState(pos), level, pos, random);
    }

    @Override
    public boolean isLadder(BlockState state, LevelReader level, BlockPos pos, LivingEntity entity) {
        return false;
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 100;
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 15;
    }
}
