package art.arcane.witchery.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.IForgeShearable;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class SpreadingHerbBlock extends BushBlock {
    private final VoxelShape outline;

    protected SpreadingHerbBlock(int light, VoxelShape outline) {
        super(Properties.of().mapColor(MapColor.PLANT).noCollission().instabreak().randomTicks()
                .sound(SoundType.GRASS).lightLevel(state -> light).pushReaction(PushReaction.DESTROY));
        this.outline = outline;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return outline;
    }

    @Override
    protected boolean mayPlaceOn(BlockState support, BlockGetter level, BlockPos pos) {
        return support.blocksMotion();
    }

    @Override
    public final void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(6) != 0 || !level.isAreaLoaded(pos, 5)) return;
        int nearby = 0;
        for (BlockPos candidate : BlockPos.betweenClosed(pos.offset(-4, -1, -4), pos.offset(4, 1, 4))) {
            if (level.getBlockState(candidate).is(this) && ++nearby == 5) return;
        }
        BlockPos center = pos;
        for (int step = 0; step < 4; step++) {
            BlockPos candidate = nearbyPosition(center, random);
            if (canSpreadTo(level, candidate)) center = candidate;
        }
        BlockPos destination = nearbyPosition(center, random);
        if (canSpreadTo(level, destination)) level.setBlock(destination, defaultBlockState(), Block.UPDATE_CLIENTS);
    }

    private static BlockPos nearbyPosition(BlockPos center, RandomSource random) {
        return center.offset(random.nextInt(3) - 1, random.nextInt(2) - random.nextInt(2), random.nextInt(3) - 1);
    }

    private static boolean canSpreadTo(ServerLevel level, BlockPos pos) {
        if (!level.isEmptyBlock(pos)) return false;
        BlockState ground = level.getBlockState(pos.below());
        return ground.is(Blocks.DIRT) || ground.is(Blocks.COARSE_DIRT) || ground.is(Blocks.PODZOL)
                || ground.is(Blocks.GRASS_BLOCK) || ground.is(Blocks.MYCELIUM) || ground.is(Blocks.FARMLAND)
                || ground.is(Blocks.SAND) || ground.is(Blocks.RED_SAND);
    }

    public static final class GlintWeed extends SpreadingHerbBlock {
        public GlintWeed() {
            super(14, box(0.8, 0, 0.8, 15.2, 16, 15.2));
        }

        @Override
        public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
            return super.canSurvive(state, level, pos) || level.getBlockState(pos.above()).blocksMotion();
        }

        @Override
        public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
            level.addParticle(ParticleTypes.FLAME, pos.getX() + 0.4 + random.nextInt(3) * 0.1,
                    pos.getY() + 0.4 + random.nextInt(3) * 0.1, pos.getZ() + 0.4 + random.nextInt(3) * 0.1, 0, 0, 0);
        }
    }

    public static final class EmberMoss extends SpreadingHerbBlock implements IForgeShearable {
        public EmberMoss() {
            super(6, box(1.6, 0, 1.6, 14.4, 6.4, 14.4));
        }

        @Override
        public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
            if (!(level instanceof ServerLevel server) || !(entity instanceof LivingEntity living) || !living.isAlive()
                    || living.isOnFire() || living.fireImmune() || living instanceof Player player && player.isCreative()) return;
            living.setSecondsOnFire(3);
            server.sendParticles(ParticleTypes.FLAME, pos.getX() + 0.5, pos.getY() + 0.05, pos.getZ() + 0.5,
                    16, 0.5, 1, 0.5, 0.04);
            server.playSound(null, pos, SoundEvents.GHAST_SHOOT, SoundSource.BLOCKS, 1, 1);
        }

        @Override
        public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
            if (random.nextInt(100) == 0) {
                level.addParticle(ParticleTypes.FLAME, pos.getX() + 0.2 + random.nextFloat() * 0.8,
                        pos.getY() + 0.15 + random.nextFloat() * 0.3, pos.getZ() + 0.2 + random.nextFloat() * 0.8, 0, 0, 0);
            }
        }

        @Override
        public List<ItemStack> onSheared(@Nullable Player player, ItemStack item, Level level, BlockPos pos, int fortune) {
            return List.of(new ItemStack(this));
        }
    }
}
