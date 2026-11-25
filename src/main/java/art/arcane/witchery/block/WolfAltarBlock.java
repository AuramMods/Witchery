package art.arcane.witchery.block;

import art.arcane.witchery.block.entity.WolfAltarBlockEntity;
import art.arcane.witchery.capability.WitcheryPlayerData.WolfmanQuestState;
import art.arcane.witchery.capability.WitcheryPlayerDataProvider;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.lycanthropy.Lycanthropy;
import art.arcane.witchery.lycanthropy.WolfTrials.Trial;
import art.arcane.witchery.registry.WitcheryItems;
import art.arcane.witchery.registry.WitcherySounds;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class WolfAltarBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final VoxelShape SHAPE = box(0, 0, 0, 16, 32, 16);
    private static final int GOLD_REQUIRED = 3;

    public WolfAltarBlock() {
        super(Properties.copy(Blocks.STONE).strength(2.5F, 600).noOcclusion().dynamicShape()
                .pushReaction(PushReaction.BLOCK));
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
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
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WolfAltarBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level instanceof ServerLevel server && player instanceof ServerPlayer worshipper) {
            int rank = Lycanthropy.rank(worshipper);
            var stack = worshipper.getItemInHand(hand);
            Direction facing = state.getValue(FACING);
            worshipper.playNotifySound(WitcherySounds.WOLFMAN_LORD.get(), SoundSource.BLOCKS, 1, 1);
            if (rank == 0) {
                worshipper.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 1200));
                tell(worshipper, "unworthy");
            } else if (stack.is(Items.GOLD_INGOT) && stack.getCount() >= GOLD_REQUIRED) {
                if (grant(server, pos, facing, WitcheryItems.getRequired("mooncharm").get(), SoundEvents.PLAYER_LEVELUP)) {
                    stack.shrink(GOLD_REQUIRED);
                    if (rank == 1) Lycanthropy.setRank(worshipper, 2);
                    tell(worshipper, rank == 1 ? "gold_offered" : "charm_replaced");
                }
            } else if (rank == 1) {
                showOffering(worshipper, stack, Items.GOLD_INGOT, GOLD_REQUIRED, "gold");
            } else if (rank == 2) {
                offer(server, pos, facing, worshipper, stack, Items.MUTTON, 30, "mutton", 3);
            } else if (rank == 3) {
                offer(server, pos, facing, worshipper, stack, WitcheryIngredient.DOG_TONGUE.item(), 10, "tongues", 4);
            } else if (rank == 4) {
                huntsmanTrial(server, pos, facing, worshipper);
            } else if (rank == 10) {
                tell(worshipper, "mastered");
            } else {
                Trial trial = Trial.atRank(rank);
                if (trial != null) countedTrial(server, pos, facing, worshipper, rank, trial);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private static void countedTrial(ServerLevel level, BlockPos pos, Direction facing, ServerPlayer player, int rank, Trial trial) {
        WitcheryPlayerDataProvider.get(player).ifPresent(data -> {
            int remaining = trial.required() - data.getWolfmanQuestCounter();
            if (data.getWolfmanQuestState() == WolfmanQuestState.COMPLETE || remaining <= 0) {
                Lycanthropy.setRank(player, rank + 1);
                rewardEffect(level, pos, facing, SoundEvents.PLAYER_LEVELUP);
                tell(player, trial.message() + "_finished");
            } else if (data.getWolfmanQuestState() == WolfmanQuestState.NOT_STARTED) {
                data.setWolfmanQuestState(WolfmanQuestState.STARTED);
                tell(player, trial.message() + "_started", trial.required());
            } else {
                tell(player, trial.message() + "_remaining", remaining);
            }
        });
    }

    private static void huntsmanTrial(ServerLevel level, BlockPos pos, Direction facing, ServerPlayer player) {
        WitcheryPlayerDataProvider.get(player).ifPresent(data -> {
            switch (data.getWolfmanQuestState()) {
                case NOT_STARTED -> {
                    if (grant(level, pos, facing, WitcheryItems.getRequired("hornofthehunt").get(), SoundEvents.FIRE_EXTINGUISH)) {
                        data.setWolfmanQuestState(WolfmanQuestState.STARTED);
                        tell(player, "hunt_started");
                    }
                }
                case STARTED -> tell(player, "hunt_pending");
                case COMPLETE -> {
                    Lycanthropy.setRank(player, 5);
                    rewardEffect(level, pos, facing, SoundEvents.PLAYER_LEVELUP);
                    tell(player, "hunt_finished");
                }
            }
        });
    }

    private static boolean grant(ServerLevel level, BlockPos pos, Direction facing, Item item, SoundEvent sound) {
        var reward = new ItemEntity(level, pos.getX() + 0.5 + facing.getStepX(), pos.getY() + 1.1,
                pos.getZ() + 0.5 + facing.getStepZ(), new ItemStack(item), 0, 0, 0);
        reward.setDefaultPickUpDelay();
        if (!level.addFreshEntity(reward)) return false;
        rewardEffect(level, pos, facing, sound);
        return true;
    }

    private static void offer(ServerLevel level, BlockPos pos, Direction facing, ServerPlayer player,
                              ItemStack stack, Item ingredient, int required, String trial, int nextRank) {
        if (stack.is(ingredient) && stack.getCount() >= required) {
            stack.shrink(required);
            Lycanthropy.setRank(player, nextRank);
            rewardEffect(level, pos, facing, SoundEvents.PLAYER_LEVELUP);
            tell(player, trial + "_offered");
        } else {
            showOffering(player, stack, ingredient, required, trial);
        }
    }

    private static void showOffering(ServerPlayer player, ItemStack stack, Item ingredient, int required, String trial) {
        if (!stack.is(ingredient)) tell(player, trial + "_requested", required);
        else tell(player, trial + "_remaining", required - stack.getCount());
    }

    private static void rewardEffect(ServerLevel level, BlockPos pos, Direction facing, SoundEvent sound) {
        level.sendParticles(DustParticleOptions.REDSTONE, pos.getX() + 0.5 + facing.getStepX(), pos.getY() + 1.1,
                pos.getZ() + 0.5 + facing.getStepZ(), 16, 0.2, 0.2, 0.2, 0.02);
        level.playSound(null, pos, sound, SoundSource.BLOCKS, 1, 1);
    }

    private static void tell(ServerPlayer player, String key, Object... values) {
        player.displayClientMessage(Component.translatable("message.witchery.wolfaltar." + key, values)
                .withStyle(ChatFormatting.GOLD), true);
    }
}
