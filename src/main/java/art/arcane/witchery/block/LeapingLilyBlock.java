package art.arcane.witchery.block;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WaterlilyBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
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
        public InteractionResult useOn(UseOnContext context) {
            var level = context.getLevel();
            var player = context.getPlayer();
            if (player != null) {
                // The ordinary block hit can target the bottom of shallow water.
                var source = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
                if (hitsSourceWater(level, source)) return placeOnWater(level, player, context.getHand(), source);
            }
            var placement = new BlockPlaceContext(context);
            var target = level.getBlockState(placement.getClickedPos());
            if (target.liquid() && !target.getFluidState().isEmpty()) return InteractionResult.PASS;
            return super.useOn(context);
        }

        @Override
        public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
            ItemStack stack = player.getItemInHand(hand);
            var hit = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
            return hitsSourceWater(level, hit) ? new InteractionResultHolder<>(placeOnWater(level, player, hand, hit), stack)
                    : InteractionResultHolder.pass(stack);
        }

        private static boolean hitsSourceWater(Level level, BlockHitResult hit) {
            if (hit.getType() != HitResult.Type.BLOCK) return false;
            var fluid = level.getFluidState(hit.getBlockPos());
            return fluid.isSource() && fluid.is(FluidTags.WATER);
        }

        private InteractionResult placeOnWater(Level level, Player player, InteractionHand hand, BlockHitResult hit) {
            var water = hit.getBlockPos();
            var above = water.above();
            if (!level.isEmptyBlock(above) || !level.mayInteract(player, water)
                    || !player.mayUseItemAt(water, hit.getDirection(), player.getItemInHand(hand))) {
                return InteractionResult.PASS;
            }
            return super.useOn(new UseOnContext(player, hand, hit.withPosition(above)));
        }
    }
}
