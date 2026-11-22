package art.arcane.witchery.block;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WaterlilyBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class LeapingLilyBlock extends WaterlilyBlock {
    private static final VoxelShape PAD = box(0, 0, 0, 16, 0.5, 16);

    public LeapingLilyBlock() {
        super(Properties.copy(Blocks.LILY_PAD).lightLevel(state -> 6));
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        var below = level.getBlockState(pos.below());
        return level.isEmptyBlock(pos.above()) && (below.blocksMotion() || !below.getFluidState().isEmpty());
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return PAD;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        super.entityInside(state, level, pos, entity);
        if (!level.isClientSide && entity instanceof LivingEntity living) {
            if (!living.hasEffect(MobEffects.MOVEMENT_SPEED)) living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 10, 0));
            if (!living.hasEffect(MobEffects.JUMP)) living.addEffect(new MobEffectInstance(MobEffects.JUMP, 10, 4));
        }
    }

    public static final class Item extends BlockItem {
        public Item(Block block) {
            super(block, new Properties());
        }

        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            var hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
            if (hit.getType() != HitResult.Type.BLOCK) return InteractionResultHolder.pass(stack);
            var fluid = level.getFluidState(hit.getBlockPos());
            if (!fluid.isSource() || !fluid.is(FluidTags.WATER) || !level.isEmptyBlock(hit.getBlockPos().above())) {
                return InteractionResultHolder.pass(stack);
            }
            var target = hit.withPosition(hit.getBlockPos().above());
            return new InteractionResultHolder<>(super.useOn(new UseOnContext(player, hand, target)), stack);
        }
    }
}
