package art.arcane.witchery.world;

import art.arcane.witchery.entity.ScryEye;
import art.arcane.witchery.item.WaystoneItem;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.registry.WitcheryEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.InteractionHand;
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

    private record View(ServerLevel level, ScryEye eye, Vec3 origin, ChunkPos bodyChunk) {}

    private ScryingView() {}

    public static boolean start(ServerPlayer player, InteractionHand hand, @Nullable WaystoneItem.Location destination) {
        if (destination == null || !destination.dimension().equals(player.level().dimension())) return false;
        ServerLevel level = player.serverLevel();
        BlockPos pos = destination.position();
        if (level.isOutsideBuildHeight(pos) || !level.getWorldBorder().isWithinBounds(pos)
                || player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ()) > MAX_DISTANCE_SQUARED) return false;

        ScryEye eye = WitcheryEntities.SCRY_EYE.get().create(level);
        if (eye == null) return false;
        close(player);
        eye.moveTo(pos.getX(), pos.getY(), pos.getZ(), player.getYRot(), 90);
        level.getChunkAt(pos);
        if (!level.addFreshEntity(eye)) return false;
        ChunkPos body = player.chunkPosition();
        level.getChunkSource().addRegionTicket(BODY_TICKET, body, 2, player.getUUID());
        ACTIVE.put(player, new View(level, eye, player.position(), body));
        player.startUsingItem(hand);
        level.getChunkSource().move(player);
        WitcheryNetwork.sendCamPos(player, true, true, eye.getId());
        return true;
    }

    public static void tick(ServerPlayer player) {
        View view = ACTIVE.get(player);
        if (view == null) return;
        if (!player.isAlive() || player.level() != view.level() || player.position().distanceToSqr(view.origin()) > 0.5625
                || !player.isUsingItem() || !player.getUseItem().is(WitcheryIngredient.BOUND_WAYSTONE.item())
                || view.eye().tickCount >= ScryEye.LIFETIME || view.eye().isRemoved()) {
            close(player);
        } else if (player.tickCount % 20 == 0) {
            WitcheryNetwork.sendCamPos(player, true, false, view.eye().getId());
        }
    }

    public static void close(ServerPlayer player) {
        View view = ACTIVE.remove(player);
        if (view == null) return;
        // The Waystone remains in use until release, even after the Eye's shorter lifetime ends.
        view.eye().discard();
        view.level().getChunkSource().removeRegionTicket(BODY_TICKET, view.bodyChunk(), 2, player.getUUID());
        if (player.connection != null && player.connection.connection.isConnected()) {
            WitcheryNetwork.sendCamPos(player, false, false, 0);
            if (player.level() == view.level() && !player.isRemoved()) view.level().getChunkSource().move(player);
        }
    }

    @Nullable
    public static ScryEye eye(ServerPlayer player) {
        View view = ACTIVE.get(player);
        return view == null || player.level() != view.level() ? null : view.eye();
    }
}
