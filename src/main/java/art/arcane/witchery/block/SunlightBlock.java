package art.arcane.witchery.block;

import art.arcane.witchery.entity.GrenadeProjectile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class SunlightBlock extends Block {
    public static final BooleanProperty TEMPORARY = BooleanProperty.create("temporary");
    private static final int RECHECK_TICKS = 10;

    public SunlightBlock() {
        super(BlockBehaviour.Properties.of().noCollission().replaceable().noOcclusion().noLootTable().lightLevel(state -> 15));
        registerDefaultState(stateDefinition.any().setValue(TEMPORARY, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TEMPORARY);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    public void illuminate(ServerLevel level, BlockPos pos) {
        if (level.isEmptyBlock(pos) && level.setBlock(pos, defaultBlockState().setValue(TEMPORARY, true), UPDATE_CLIENTS)) {
            level.scheduleTick(pos, this, RECHECK_TICKS);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(TEMPORARY) && !clearUnusedLight(level, pos)) level.scheduleTick(pos, this, RECHECK_TICKS);
    }

    public boolean clearUnusedLight(ServerLevel level, BlockPos pos) {
        if (!level.hasChunkAt(pos)) return false;
        BlockState state = level.getBlockState(pos);
        if (!state.is(this) || !state.getValue(TEMPORARY)) return true;
        var sources = level.getEntitiesOfClass(GrenadeProjectile.class, new AABB(pos).inflate(2),
                grenade -> grenade.isLighting(pos));
        return sources.isEmpty() && level.removeBlock(pos, false);
    }
}
