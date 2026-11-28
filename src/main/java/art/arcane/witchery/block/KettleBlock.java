package art.arcane.witchery.block;

import art.arcane.witchery.block.entity.KettleBlockEntity;
import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.registry.WitcheryBlockEntities;
import art.arcane.witchery.registry.WitcherySounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.fluids.FluidUtil;

public final class KettleBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final VoxelShape COLLISION = box(1, 0, 1, 15, 15, 15);

    public KettleBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return rotate(state, mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return COLLISION;
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new KettleBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, WitcheryBlockEntities.KETTLE.get(), KettleBlockEntity::serverTick);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof KettleBlockEntity kettle)) {
            return InteractionResult.PASS;
        }
        ItemStack held = player.getItemInHand(hand);
        if (held.is(Items.GLASS_BOTTLE)) {
            if (level.isClientSide) return kettle.isReady() || kettle.isFull() ? InteractionResult.SUCCESS : InteractionResult.PASS;
            if (!(player instanceof ServerPlayer server)) return InteractionResult.PASS;
            if (!kettle.isReady()) {
                if (!BrewingContainers.fillWaterBottle(player, hand, kettle, hit.getDirection())) return InteractionResult.PASS;
                kettle.rinse();
            } else if (!kettle.bottleBrew(server, hand)) {
                return InteractionResult.CONSUME;
            }
            WitcherySounds.playLegacyPlayerCue(server, SoundEvents.GENERIC_SWIM, 0.5F);
            return InteractionResult.CONSUME;
        }
        if (held.is(Items.BUCKET)) {
            if (level.isClientSide) return kettle.isFull() ? InteractionResult.SUCCESS : InteractionResult.PASS;
            if (!(player instanceof ServerPlayer server)
                    || !BrewingContainers.fillBucket(player, hand, kettle, hit.getDirection())) return InteractionResult.PASS;
            kettle.rinse();
            WitcherySounds.playLegacyPlayerCue(server, SoundEvents.GENERIC_SWIM, 0.5F);
            return InteractionResult.CONSUME;
        }
        boolean waterBottle = BrewingContainers.isWaterBottle(held);
        if (waterBottle || held.is(Items.WATER_BUCKET)) {
            if (player instanceof ServerPlayer server) {
                if (waterBottle) {
                    BrewingContainers.pourWaterBottle(player, hand, kettle, hit.getDirection());
                } else {
                    BrewingContainers.pourBucket(player, hand, kettle, hit.getDirection());
                }
                kettle.rinse();
                WitcherySounds.playLegacyPlayerCue(server, SoundEvents.GENERIC_SWIM, 0.5F);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (FluidUtil.getFluidHandler(held).isPresent()) {
            if (!level.isClientSide && FluidUtil.interactWithFluidHandler(player, hand, level, pos, hit.getDirection())) {
                kettle.rinse();
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!(level instanceof ServerLevel server) || !(entity instanceof ItemEntity item) || !item.isAlive()
                || !(level.getBlockEntity(pos) instanceof KettleBlockEntity kettle)) {
            return;
        }
        ItemStack stack = item.getItem();
        if (kettle.addDroppedStack(stack).isEmpty()) {
            item.discard();
            if (!stack.isEmpty() && !stack.is(Items.GLASS_BOTTLE)) {
                WitcheryNetwork.sendWaterSplash(server, pos.getX() + 0.5, pos.getY() + 0.2, pos.getZ() + 0.5);
            }
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (level.getBlockEntity(pos) instanceof KettleBlockEntity kettle) {
            double x = pos.getX() + 0.45;
            double y = pos.getY() + 0.4;
            double z = pos.getZ() + 0.5;
            if (kettle.isRuined()) {
                level.addParticle(ParticleTypes.LARGE_SMOKE, x, y, z, 0, 0, 0);
            } else if (kettle.isReady()) {
                level.addParticle(ParticleTypes.ITEM_SLIME, x, y, z, 0, 0, 0);
                level.addParticle(kettle.isPowered() ? ParticleTypes.EFFECT : ParticleTypes.ENTITY_EFFECT, x, y, z, 0, 0, 0);
            } else if (kettle.isFull() && kettle.hasIngredients()) {
                level.addParticle(ParticleTypes.ENTITY_EFFECT, x, y, z, 0, 0, 0);
            }
        }
    }
}
