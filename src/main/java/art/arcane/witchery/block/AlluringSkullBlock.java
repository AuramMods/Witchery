package art.arcane.witchery.block;

import art.arcane.witchery.block.entity.AlluringSkullBlockEntity;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.registry.WitcheryBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public final class AlluringSkullBlock extends MountedSkullBlock {
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    public AlluringSkullBlock() {
        super(Properties.copy(Blocks.SKELETON_SKULL).strength(-1, 600).lightLevel(state -> 7).noOcclusion());
        registerDefaultState(defaultBlockState().setValue(ACTIVE, false));
    }

    public static void importLegacyItemState(ItemStack stack, int metadata) {
        var state = stack.getTagElement(BlockItem.BLOCK_STATE_TAG);
        if (metadata == 1 && (state == null || !state.contains(ACTIVE.getName(), Tag.TAG_STRING))) {
            stack.getOrCreateTagElement(BlockItem.BLOCK_STATE_TAG).putString(ACTIVE.getName(), "true");
        }
    }

    public static boolean isActiveItem(ItemStack stack) {
        var state = stack.getTagElement(BlockItem.BLOCK_STATE_TAG);
        return state != null && ACTIVE.getValue(state.getString(ACTIVE.getName())).orElse(false);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(ACTIVE);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!WitcheryIngredient.NECROMANTIC_STONE.matches(player.getItemInHand(hand))) return InteractionResult.PASS;
        if (level instanceof ServerLevel server) {
            boolean active = state.getValue(ACTIVE);
            if (active) {
                var drop = new ItemEntity(server, pos.getX() + 0.5, pos.getY() + 0.8, pos.getZ() + 0.5,
                        new ItemStack(this));
                if (!server.addFreshEntity(drop)) return InteractionResult.FAIL;
                if (server.getBlockState(pos) != state || !server.removeBlock(pos, false)) {
                    drop.discard();
                    return InteractionResult.FAIL;
                }
            } else {
                server.setBlockAndUpdate(pos, state.setValue(ACTIVE, true));
            }
            WitcheryNetwork.sendAlluringSkullCue(server, pos, active);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AlluringSkullBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return !level.isClientSide
                ? createTickerHelper(type, WitcheryBlockEntities.ALLURING_SKULL.get(), AlluringSkullBlockEntity::tick) : null;
    }
}
