package art.arcane.witchery.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class CandelabraBlock extends Block {
    private static final VoxelShape SHAPE = box(1.5, 0, 1.5, 14.5, 16, 14.5);

    public CandelabraBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return canSupportCenter(level, pos.below(), Direction.UP);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return direction == Direction.DOWN && !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState() : state;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.9;
        double z = pos.getZ() + 0.5;
        flame(level, random, x, y + 0.15, z);
        flame(level, random, x - 0.3, y, z);
        flame(level, random, x + 0.3, y, z);
        flame(level, random, x, y, z - 0.3);
        flame(level, random, x, y, z + 0.3);
    }

    private static void flame(Level level, RandomSource random, double x, double y, double z) {
        if (random.nextInt(4) != 0) {
            level.addParticle(ParticleTypes.FLAME, x, y, z, 0, 0, 0);
            level.addParticle(ParticleTypes.SMOKE, x, y, z, 0, 0, 0);
        }
    }
}
