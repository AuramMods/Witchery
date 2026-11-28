package art.arcane.witchery.block;

import art.arcane.witchery.block.entity.CrystalBallBlockEntity;
import art.arcane.witchery.item.TaglockItem;
import art.arcane.witchery.item.WaystoneItem;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.power.AltarPower;
import art.arcane.witchery.prediction.Predictions;
import art.arcane.witchery.registry.WitcherySounds;
import art.arcane.witchery.world.ScryingView;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Comparator;

public final class CrystalBallBlock extends BaseEntityBlock {
    private static final VoxelShape SHAPE = box(4.8, 0, 4.8, 11.2, 9.6, 11.2);

    public CrystalBallBlock() {
        super(Properties.copy(Blocks.GLASS).strength(2).requiresCorrectToolForDrops().noOcclusion()
                .pushReaction(PushReaction.BLOCK));
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return RitualGlyphBlock.supportsGlyph(level, pos);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level,
                                  BlockPos pos, BlockPos neighborPos) {
        if (direction == Direction.DOWN && !canSurvive(state, level, pos)) return Blocks.AIR.defaultBlockState();
        return super.updateShape(state, direction, neighbor, level, pos, neighborPos);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CrystalBallBlockEntity(pos, state);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        var held = player.getItemInHand(hand);
        boolean taglock = held.getItem() instanceof TaglockItem;
        if (taglock || held.is(WitcheryIngredient.BOUND_WAYSTONE.item())) {
            boolean hasBinding = !taglock || TaglockItem.isBound(held);
            if (level.isClientSide && hasBinding) {
                player.startUsingItem(hand);
            } else if (level instanceof ServerLevel server && player instanceof ServerPlayer reader) {
                if (!hasBinding) {
                    failure(reader, "taglock_unbound", true);
                } else if (consumePower(server, pos, reader)) {
                    boolean opened = taglock ? ScryingView.startTaglock(reader, hand, TaglockItem.resolveTarget(server, held))
                            : ScryingView.start(reader, hand, WaystoneItem.location(held));
                    if (!opened) failure(reader, taglock ? "taglock_unavailable" : "waystone_out_of_range", true);
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (level instanceof ServerLevel server && player instanceof ServerPlayer reader
                && server.getBlockEntity(pos) instanceof CrystalBallBlockEntity ball) {
            if (!ball.ready(server.getGameTime())) {
                failure(reader, "recharging", false);
            } else if (consumePower(server, pos, reader)) {
                Vec3 center = Vec3.atCenterOf(pos);
                Predictions.read(predictionSubject(server, reader, center), reader);
                ball.readAt(server.getGameTime());
                WitcheryNetwork.sendCrystalBallReading(server, Vec3.atBottomCenterOf(pos).add(0, 0.2, 0));
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static ServerPlayer predictionSubject(ServerLevel level, ServerPlayer reader, Vec3 center) {
        // The original compared the reader's distance on every iteration, so the first visitor won.
        return level.getEntitiesOfClass(ServerPlayer.class, new AABB(center, center).inflate(5, 2, 5),
                        visitor -> visitor != reader && visitor.isAlive() && !visitor.isSpectator()).stream()
                .min(Comparator.comparingInt((ServerPlayer visitor) -> visitor.chunkPosition().x)
                        .thenComparingInt(visitor -> visitor.chunkPosition().z)
                        .thenComparingInt(visitor -> SectionPos.blockToSectionCoord(visitor.getBlockY())))
                .orElse(reader);
    }

    private static boolean consumePower(ServerLevel level, BlockPos pos, ServerPlayer reader) {
        var altar = AltarPower.nearest(level, pos);
        if (altar != null && altar.consumePower(500)) return true;
        failure(reader, "nopower", false);
        return false;
    }

    private static void failure(ServerPlayer player, String reason, boolean overlay) {
        player.displayClientMessage(Component.translatable("witchery.prediction." + reason).withStyle(ChatFormatting.GRAY), overlay);
        WitcherySounds.playLegacyPlayerCue(player, SoundEvents.NOTE_BLOCK_SNARE.value(), SoundSource.RECORDS, 0.5F);
    }
}
