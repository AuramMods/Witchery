package art.arcane.witchery.block;

import art.arcane.witchery.block.entity.BrazierBlockEntity;
import art.arcane.witchery.registry.WitcheryBlockEntities;
import art.arcane.witchery.registry.WitcherySounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.concurrent.ThreadLocalRandom;

public final class BrazierBlock extends BaseEntityBlock {
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    private static final VoxelShape SHAPE = box(3.2, 0, 3.2, 12.8, 15.2, 12.8);

    public BrazierBlock() {
        super(Properties.copy(Blocks.IRON_BLOCK).noOcclusion().strength(3.5F).sound(SoundType.METAL)
                .pushReaction(PushReaction.BLOCK));
        registerDefaultState(defaultBlockState().setValue(LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BrazierBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null
                : createTickerHelper(type, WitcheryBlockEntities.BRAZIER.get(), BrazierBlockEntity::serverTick);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack held = player.getItemInHand(hand);
        if (held.isEmpty()) return InteractionResult.PASS;
        if (!(level.getBlockEntity(pos) instanceof BrazierBlockEntity brazier)) return InteractionResult.PASS;
        if (held.is(Items.FLINT_AND_STEEL)) return strikeFlint(level, brazier, player, hand, hit, held);
        if (level.isClientSide) return InteractionResult.SUCCESS;

        boolean waterBottle = held.is(Items.POTION) && PotionUtils.getPotion(held) == Potions.WATER;
        if (waterBottle || held.is(Items.WATER_BUCKET)) {
            if (!brazier.isEmpty()) {
                brazier.clearBatch();
                ItemStack empty = new ItemStack(waterBottle ? Items.GLASS_BOTTLE : Items.BUCKET);
                player.setItemInHand(hand, ItemUtils.createFilledResult(held, player, empty, false));
                if (player instanceof ServerPlayer server) {
                    WitcherySounds.playLegacyPlayerCue(server, SoundEvents.PLAYER_SPLASH, 0.5F);
                }
            }
        } else if (brazier.insert(held) && !player.getAbilities().instabuild) {
            held.shrink(1);
        }
        return InteractionResult.CONSUME;
    }

    private static InteractionResult strikeFlint(Level level, BrazierBlockEntity brazier, Player player,
                                                 InteractionHand hand, BlockHitResult hit, ItemStack tool) {
        if (!level.isClientSide) brazier.ignite();
        BlockPos target = hit.getBlockPos().relative(hit.getDirection());
        // Ordinary fire placement continues through vanilla useOn and Forge's placement event.
        if (BaseFireBlock.canBePlacedAt(level, target, player.getDirection())) return InteractionResult.PASS;
        if (!player.mayUseItemAt(target, hit.getDirection(), tool)) return InteractionResult.PASS;

        if (!level.isClientSide) {
            // The original flint wore even against an obstruction and sounded only when it struck air.
            if (level.isEmptyBlock(target)) {
                float pitch = 0.8F + ThreadLocalRandom.current().nextFloat() * 0.4F;
                level.playSound(null, target, SoundEvents.FLINTANDSTEEL_USE, SoundSource.BLOCKS, 1, pitch);
            }
            tool.hurtAndBreak(1, player, user -> user.broadcastBreakEvent(hand));
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(LIT)) {
            level.addParticle(ParticleTypes.FLAME, pos.getX() + 0.4 + random.nextInt(3) * 0.1,
                    pos.getY() + 1.1 + random.nextInt(2) * 0.1,
                    pos.getZ() + 0.4 + random.nextInt(3) * 0.1, 0, 0, 0);
        }
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighbor, BlockPos fromPos, boolean moving) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof BrazierBlockEntity brazier) {
            brazier.updateSignal(level.hasNeighborSignal(pos));
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moving) {
        if (!level.isClientSide && !state.is(replacement.getBlock()) && level.getBlockEntity(pos) instanceof BrazierBlockEntity brazier) {
            brazier.dropContents(level);
            level.updateNeighbourForOutputSignal(pos, this);
        }
        super.onRemove(state, level, pos, replacement, moving);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof BrazierBlockEntity brazier ? brazier.comparatorSignal() : 0;
    }
}
