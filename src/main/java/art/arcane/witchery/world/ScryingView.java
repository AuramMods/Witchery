package art.arcane.witchery.world;

import art.arcane.witchery.entity.ScryEye;
import art.arcane.witchery.item.WaystoneItem;
import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.registry.WitcheryEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.UUID;

public final class ScryingView {
    private static final TicketType<UUID> BODY_TICKET = TicketType.create("witchery_scrying_body", UUID::compareTo);
    private static final double MAX_DISTANCE_SQUARED = 150 * 150;
    private static final Map<ServerPlayer, View> ACTIVE = new IdentityHashMap<>();

    private record View(ServerLevel level, Entity camera, Item tool, Vec3 origin, ChunkPos bodyChunk) {}

    private ScryingView() {}

    public static boolean start(ServerPlayer player, InteractionHand hand, @Nullable WaystoneItem.Location destination) {
        if (destination == null || !destination.dimension().equals(player.level().dimension())) return false;
        ServerLevel level = player.serverLevel();
        BlockPos pos = destination.position();
        if (level.isOutsideBuildHeight(pos) || !level.getWorldBorder().isWithinBounds(pos)
                || player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) > MAX_DISTANCE_SQUARED) return false;
        return startEye(player, hand, Vec3.atLowerCornerOf(pos));
    }

    public static boolean startTaglock(ServerPlayer player, InteractionHand hand, @Nullable LivingEntity target) {
        if (target == null || !target.isAlive() || target.level() != player.level()) return false;
        return target == player ? startEye(player, hand, player.position()) : open(player, hand, target);
    }

    private static boolean startEye(ServerPlayer player, InteractionHand hand, Vec3 position) {
        ServerLevel level = player.serverLevel();
        ScryEye eye = WitcheryEntities.SCRY_EYE.get().create(level);
        if (eye == null) return false;
        eye.moveTo(position.x, position.y, position.z, player.getYRot(), 90);
        level.getChunkAt(eye.blockPosition());
        if (!level.addFreshEntity(eye)) return false;
        return open(player, hand, eye);
    }

    private static boolean open(ServerPlayer player, InteractionHand hand, Entity camera) {
        close(player);
        ServerLevel level = player.serverLevel();
        ChunkPos body = player.chunkPosition();
        level.getChunkSource().addRegionTicket(BODY_TICKET, body, 2, player.getUUID());
        ACTIVE.put(player, new View(level, camera, player.getItemInHand(hand).getItem(), player.position(), body));
        player.startUsingItem(hand);
        level.getChunkSource().move(player);
        WitcheryNetwork.sendCamPos(player, true, true, camera.getId());
        return true;
    }

    public static void tick(ServerPlayer player) {
        View view = ACTIVE.get(player);
        if (view == null) return;
        Entity camera = view.camera();
        if (!player.isAlive() || player.level() != view.level() || player.position().distanceToSqr(view.origin()) > 0.5625
                || !player.isUsingItem() || !player.getUseItem().is(view.tool())
                || !camera.isAlive() || camera.level() != view.level()
                || camera instanceof ScryEye && camera.tickCount >= ScryEye.LIFETIME) {
            close(player);
            return;
        }
        if (!camera.chunkPosition().equals(player.getLastSectionPos().chunk())) view.level().getChunkSource().move(player);
        if (player.tickCount % 20 == 0) WitcheryNetwork.sendCamPos(player, true, false, camera.getId());
    }

    public static void close(ServerPlayer player) {
        View view = ACTIVE.remove(player);
        if (view == null) return;
        // The item remains in use until release, even when the camera expires first.
        if (view.camera() instanceof ScryEye eye) eye.discard();
        view.level().getChunkSource().removeRegionTicket(BODY_TICKET, view.bodyChunk(), 2, player.getUUID());
        if (player.connection != null && player.connection.connection.isConnected()) {
            WitcheryNetwork.sendCamPos(player, false, false, 0);
            if (player.level() == view.level() && !player.isRemoved()) view.level().getChunkSource().move(player);
        }
    }

    @Nullable
    public static Entity camera(ServerPlayer player) {
        View view = ACTIVE.get(player);
        return view == null || player.level() != view.level() ? null : view.camera();
    }
}
