package art.arcane.witchery.block;

import art.arcane.witchery.block.entity.KettleBlockEntity;
import art.arcane.witchery.registry.WitcheryBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.fluids.FluidUtil;

public final class KettleBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final VoxelShape BASIN = Shapes.or(box(3, 0, 3, 13, 1, 13),
            box(3, 1, 3, 4, 6, 13), box(12, 1, 3, 13, 6, 13),
            box(4, 1, 3, 12, 6, 4), box(4, 1, 12, 12, 6, 13));
    private static final VoxelShape OUTLINE = box(1, 0, 1, 15, 15, 15);

    public KettleBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
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
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return OUTLINE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return BASIN;
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
        if (held.is(Items.GLASS_BOTTLE) && kettle.isReady()) {
            if (!level.isClientSide) {
                ItemStack brew = kettle.bottleBrew(player);
                if (!brew.isEmpty()) {
                    held.shrink(1);
                    if (held.isEmpty()) {
                        player.setItemInHand(hand, brew);
                    } else if (!player.addItem(brew)) {
                        player.drop(brew, false);
                    }
                    level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (held.is(Items.WATER_BUCKET)) {
            if (!level.isClientSide) {
                if (!BrewingBuckets.pour(player, hand, kettle, hit.getDirection())) kettle.rinse();
                level.playSound(null, pos, SoundEvents.GENERIC_SWIM, SoundSource.BLOCKS, 1, 1);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (FluidUtil.getFluidHandler(held).isPresent()) {
            if (!level.isClientSide) {
                FluidUtil.interactWithFluidHandler(player, hand, level, pos, hit.getDirection());
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide || !(entity instanceof ItemEntity item) || !item.isAlive()
                || !(level.getBlockEntity(pos) instanceof KettleBlockEntity kettle)) {
            return;
        }
        if (kettle.addDroppedStack(item.getItem()).isEmpty()) {
            item.discard();
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
