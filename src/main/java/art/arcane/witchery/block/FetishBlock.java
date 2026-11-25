package art.arcane.witchery.block;

import art.arcane.witchery.block.entity.FetishBlockEntity;
import art.arcane.witchery.item.TaglockItem;
import art.arcane.witchery.registry.WitcheryBlockEntities;
import art.arcane.witchery.registry.WitcheryItems;
import art.arcane.witchery.world.WitcheryDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.List;

public final class FetishBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    private static final VoxelShape SHAPE = box(3.2, 0, 3.2, 12.8, 16, 12.8);
    private final String path;

    public FetishBlock(String path) {
        super(properties(path));
        this.path = path;
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH).setValue(POWERED, false));
    }

    private static Properties properties(String path) {
        var properties = Properties.copy(Blocks.OAK_PLANKS).strength(3.5F, 60_000.0F).noOcclusion()
                .pushReaction(PushReaction.BLOCK);
        return path.equals("witchsladder") ? properties.noCollission() : properties.dynamicShape();
    }

    public boolean isLadderFetish() {
        return path.equals("witchsladder");
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
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
        return SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (level.getBlockEntity(pos) instanceof FetishBlockEntity fetish && fetish.isSpectral()) {
            return Shapes.empty();
        }
        return super.getCollisionShape(state, level, pos, context);
    }

    @Override
    public float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof FetishBlockEntity fetish && fetish.isSpectral()) return 0;
        return super.getDestroyProgress(state, player, level, pos);
    }

    @Override
    public FetishBlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FetishBlockEntity(pos, state, path);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type, WitcheryBlockEntities.fetish(path).get(), FetishBlockEntity::tick);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity actor, ItemStack stack) {
        if (level instanceof ServerLevel server && level.getBlockEntity(pos) instanceof FetishBlockEntity fetish) {
            fetish.loadItem(stack);
            fetish.projectFromDream(server);
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moving) {
        if (!state.is(replacement.getBlock()) && level instanceof ServerLevel server) {
            if (server.dimension().equals(WitcheryDimensions.DREAM)) {
                ServerLevel overworld = server.getServer().overworld();
                if (overworld.getBlockEntity(pos) instanceof FetishBlockEntity copy && copy.isSpectral() && copy.getBlockState().is(this)) {
                    overworld.removeBlock(pos, false);
                }
            }
            if (state.getValue(POWERED)) level.updateNeighborsAt(pos.below(), this);
        }
        super.onRemove(state, level, pos, replacement, moving);
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        var entity = params.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        if (!(entity instanceof FetishBlockEntity fetish)) return super.getDrops(state, params);
        if (fetish.isSpectral()) return List.of();
        List<ItemStack> drops = super.getDrops(state, params);
        for (ItemStack drop : drops) {
            if (drop.is(asItem())) fetish.saveItem(drop);
        }
        return drops;
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof FetishBlockEntity fetish)) return InteractionResult.PASS;
        ItemStack held = player.getItemInHand(hand);
        if (held.getItem() instanceof TaglockItem) {
            if (level instanceof ServerLevel server && fetish.bindSubject(server, held, player) && !player.getAbilities().instabuild) {
                held.shrink(1);
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (fetish.isSpectral()) return InteractionResult.PASS;
        if (held.getItem() instanceof DyeItem dye) {
            if (!level.isClientSide) {
                fetish.dye(dye.getDyeColor());
                if (!player.getAbilities().instabuild) held.shrink(1);
            }
        } else if (held.is(Items.WATER_BUCKET)) {
            if (!level.isClientSide) {
                fetish.clearSubjects(player);
                player.setItemInHand(hand, ItemUtils.createFilledResult(held, player, new ItemStack(Items.BUCKET), false));
                level.playSound(null, pos, SoundEvents.GENERIC_SWIM, SoundSource.BLOCKS, 0.8F, 1);
            }
        } else if (held.is(WitcheryItems.getRequired("boline").get())) {
            if (!level.isClientSide) fetish.cycleAlarm(player);
        } else {
            return InteractionResult.PASS;
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(POWERED) ? 15 : 0;
    }

    @Override
    public int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return direction == Direction.UP ? getSignal(state, level, pos, direction) : 0;
    }
}
