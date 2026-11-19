package art.arcane.witchery.block;

import art.arcane.witchery.block.entity.WitchesCauldronBlockEntity;
import art.arcane.witchery.registry.WitcheryBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.fluids.FluidUtil;

public final class WitchesCauldronBlock extends BaseEntityBlock {
    public static final IntegerProperty WATER = IntegerProperty.create("water", 0, 3);
    public static final BooleanProperty BOILING = BooleanProperty.create("boiling");
    private static final VoxelShape BASIN = Shapes.or(box(1, 0, 1, 15, 3, 15),
            box(1, 3, 1, 3, 13, 15), box(13, 3, 1, 15, 13, 15),
            box(3, 3, 1, 13, 13, 3), box(3, 3, 13, 13, 13, 15));

    public WitchesCauldronBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(WATER, 0).setValue(BOILING, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(WATER, BOILING);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return BASIN;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WitchesCauldronBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null
                : createTickerHelper(type, WitcheryBlockEntities.CAULDRON.get(), WitchesCauldronBlockEntity::serverTick);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!FluidUtil.getFluidHandler(player.getItemInHand(hand)).isPresent()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            FluidUtil.interactWithFluidHandler(player, hand, level, pos, hit.getDirection());
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide || !state.getValue(BOILING) || !(entity instanceof ItemEntity item) || !item.isAlive()
                || !(level.getBlockEntity(pos) instanceof WitchesCauldronBlockEntity cauldron)) {
            return;
        }
        double x = item.getX() - pos.getX();
        double z = item.getZ() - pos.getZ();
        if (x >= 0.125 && x <= 0.875 && z >= 0.125 && z <= 0.875
                && item.getY() < pos.getY() + 1 && cauldron.addIngredient(item.getItem())) {
            var remainder = item.getItem().getCraftingRemainingItem();
            item.getItem().shrink(1);
            if (item.getItem().isEmpty()) {
                item.discard();
            }
            if (!remainder.isEmpty()) {
                popResource(level, pos.above(), remainder);
            }
        }
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof WitchesCauldronBlockEntity cauldron ? cauldron.comparatorSignal() : 0;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(BOILING)) {
            level.addParticle(ParticleTypes.BUBBLE_POP, pos.getX() + 0.2 + random.nextDouble() * 0.6,
                    pos.getY() + (3.0 + 3.0 * state.getValue(WATER)) / 16.0,
                    pos.getZ() + 0.2 + random.nextDouble() * 0.6, 0, 0.07, 0);
        }
    }
}
