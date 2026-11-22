package art.arcane.witchery.block;

import art.arcane.witchery.block.entity.VoidBrambleBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class VoidBrambleBlock extends OwnedColumnBlock {
    private static final VoxelShape OUTLINE = box(0.8, 0, 0.8, 15.2, 16, 15.2);

    public VoidBrambleBlock() {
        super(Properties.copy(Blocks.SWEET_BERRY_BUSH).strength(-1, 1000).noCollission().noOcclusion()
                .sound(SoundType.GRASS).lightLevel(state -> 1), false);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return OUTLINE;
    }

    @Override
    public VoidBrambleBlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new VoidBrambleBlockEntity(pos, state);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level instanceof ServerLevel server && entity instanceof LivingEntity living && living.isAlive()) {
            server.scheduleTick(pos, this, 1);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        BrambleBlock.teleportContacts(level, pos);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextBoolean()) {
            level.addParticle(ParticleTypes.PORTAL, pos.getX() + random.nextDouble(),
                    pos.getY() + 0.65 + random.nextDouble() * 0.3, pos.getZ() + random.nextDouble(), 0, -1.2, 0);
        }
    }
}
