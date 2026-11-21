package art.arcane.witchery.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class GlowGlobeBlock extends Block {
    private static final VoxelShape SHAPE = box(6.4, 6.4, 6.4, 9.6, 9.6, 9.6);

    public GlowGlobeBlock() {
        super(Properties.copy(Blocks.GLASS).instabreak().noCollission().noOcclusion().noLootTable().lightLevel(state -> 14));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
        return ItemStack.EMPTY;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(3) == 0) return;
        level.addParticle(ParticleTypes.FLAME, pos.getX() + 0.45 + random.nextInt(3) * 0.05,
                pos.getY() + 0.4 + random.nextInt(4) * 0.1, pos.getZ() + 0.45 + random.nextInt(3) * 0.05, 0, 0, 0);
    }
}
