package art.arcane.witchery.block;

import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryFluids;
import art.arcane.witchery.world.DreamTravel;
import art.arcane.witchery.world.WitcheryDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

public final class SomnianCottonBlock extends BushBlock {
    private static final VoxelShape SHAPE = box(3, 0, 3, 13, 13, 13);

    public SomnianCottonBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!level.dimension().equals(WitcheryDimensions.DREAM) || random.nextInt(6) != 0
                || !level.hasChunksAt(pos.offset(-4, -1, -4), pos.offset(4, 1, 4)) || !hasSpiritPool(level, pos)) {
            return;
        }
        int remaining = 5;
        for (BlockPos nearby : BlockPos.betweenClosed(pos.offset(-4, -1, -4), pos.offset(4, 1, 4))) {
            if (level.getBlockState(nearby).is(this) && --remaining == 0) {
                return;
            }
        }
        BlockPos origin = pos;
        for (int step = 0; step < 4; step++) {
            BlockPos candidate = step(origin, random);
            if (canSpreadTo(state, level, candidate)) {
                origin = candidate;
            }
        }
        BlockPos target = step(origin, random);
        if (canSpreadTo(state, level, target)) {
            level.setBlock(target, state, Block.UPDATE_ALL);
        }
    }

    private static BlockPos step(BlockPos origin, RandomSource random) {
        return origin.offset(random.nextInt(3) - 1, random.nextInt(2) - random.nextInt(2), random.nextInt(3) - 1);
    }

    private static boolean canSpreadTo(BlockState state, ServerLevel level, BlockPos pos) {
        if (!level.hasChunkAt(pos) || !level.isEmptyBlock(pos)) {
            return false;
        }
        BlockState soil = level.getBlockState(pos.below());
        return (soil.is(Blocks.DIRT) || soil.is(Blocks.GRASS_BLOCK)) && state.canSurvive(level, pos);
    }

    private static boolean hasSpiritPool(ServerLevel level, BlockPos pos) {
        var spirit = WitcheryFluids.getRequiredSource("fluidspirit").get();
        for (Direction side : Direction.Plane.HORIZONTAL) {
            var fluid = level.getFluidState(pos.below().relative(side));
            if (fluid.isSource() && fluid.getType() == spirit) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        List<ItemStack> drops = super.getDrops(state, builder);
        if (!DreamTravel.isNightmare(builder.getLevel())) {
            return drops;
        }
        return drops.stream().map(stack -> stack.is(asItem())
                ? new ItemStack(WitcheryIngredient.DISTURBED_COTTON.item(), stack.getCount()) : stack).toList();
    }
}
