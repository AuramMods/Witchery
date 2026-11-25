package art.arcane.witchery.block;

import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryItems;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DaylightDetectorBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class DaylightCollectorBlock extends Block {
    public static final IntegerProperty CHARGE = IntegerProperty.create("charge", 0, 15);
    private static final VoxelShape SHAPE = box(3.2, 0, 3.2, 12.8, 12.8, 12.8);

    public DaylightCollectorBlock() {
        super(BlockBehaviour.Properties.copy(Blocks.DAYLIGHT_DETECTOR).strength(3.5F).sound(SoundType.METAL)
                .requiresCorrectToolForDrops().noOcclusion());
        registerDefaultState(stateDefinition.any().setValue(CHARGE, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CHARGE);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighbor, BlockPos neighborPos, boolean moving) {
        int charge = state.getValue(CHARGE);
        if (level.isClientSide || charge == 0 || charge == 15 || pos.getY() != neighborPos.getY()
                || pos.distManhattan(neighborPos) != 1) return;
        BlockState detector = level.getBlockState(neighborPos);
        if (detector.is(Blocks.DAYLIGHT_DETECTOR) && detector.getValue(DaylightDetectorBlock.POWER) == charge + 1) {
            level.setBlockAndUpdate(pos, state.setValue(CHARGE, charge + 1));
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            int charge = state.getValue(CHARGE);
            ItemStack held = player.getItemInHand(hand);
            if (charge == 0 && held.is(WitcheryIngredient.QUARTZ_SPHERE.item())) {
                if (!player.getAbilities().instabuild) held.shrink(1);
                level.setBlockAndUpdate(pos, state.setValue(CHARGE, 1));
            } else if (charge > 0) {
                level.setBlockAndUpdate(pos, state.setValue(CHARGE, 0));
                ItemStack output = new ItemStack(charge == 15 ? WitcheryItems.getRequired("sungrenade").get()
                        : WitcheryIngredient.QUARTZ_SPHERE.item());
                var item = new ItemEntity(level, player.getX(), player.getY() + 1, player.getZ(), output);
                item.setDefaultPickUpDelay();
                level.addFreshEntity(item);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
