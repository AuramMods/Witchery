package art.arcane.witchery.block;

import art.arcane.witchery.block.entity.CrystalBallBlockEntity;
import art.arcane.witchery.item.WaystoneItem;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.power.AltarPower;
import art.arcane.witchery.prediction.Predictions;
import art.arcane.witchery.world.ScryingView;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Comparator;

public final class CrystalBallBlock extends BaseEntityBlock {
    private static final VoxelShape SHAPE = box(4.8, 0, 4.8, 11.2, 9.6, 11.2);

    public CrystalBallBlock() {
        super(Properties.copy(Blocks.GLASS).strength(2).noOcclusion());
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
        if (held.is(WitcheryIngredient.BOUND_WAYSTONE.item())) {
            if (level.isClientSide) {
                player.startUsingItem(hand);
            } else if (level instanceof ServerLevel server && player instanceof ServerPlayer reader
                    && consumePower(server, pos, reader) && !ScryingView.start(reader, hand, WaystoneItem.location(held))) {
                failure(reader, "waystone_out_of_range");
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (level instanceof ServerLevel server && player instanceof ServerPlayer reader
                && server.getBlockEntity(pos) instanceof CrystalBallBlockEntity ball) {
            if (!ball.ready(server.getGameTime())) {
                failure(reader, "recharging");
            } else if (consumePower(server, pos, reader)) {
                Vec3 center = Vec3.atCenterOf(pos);
                ServerPlayer subject = server.getEntitiesOfClass(ServerPlayer.class, new AABB(center, center).inflate(5, 2, 5),
                        nearby -> nearby != reader && nearby.isAlive() && !nearby.isSpectator()).stream()
                        .min(Comparator.comparingDouble(nearby -> nearby.distanceToSqr(center))).orElse(reader);
                Predictions.read(subject, reader);
                ball.readAt(server.getGameTime());
                server.sendParticles(ParticleTypes.POOF, center.x, pos.getY() + 0.28, center.z,
                        16, 0.2, 0.2, 0.2, 0.03);
                server.playSound(null, pos, SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 0.7F, 1.15F);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    private static boolean consumePower(ServerLevel level, BlockPos pos, ServerPlayer reader) {
        var altar = AltarPower.nearest(level, pos);
        if (altar != null && altar.getBlockPos().distSqr(pos) <= 16 * 16 && altar.consumePower(500)) return true;
        failure(reader, "nopower");
        return false;
    }

    private static void failure(ServerPlayer player, String reason) {
        player.displayClientMessage(Component.translatable("witchery.prediction." + reason).withStyle(ChatFormatting.GRAY), true);
        player.playNotifySound(SoundEvents.NOTE_BLOCK_SNARE.value(), SoundSource.PLAYERS, 0.7F, 1);
    }
}
