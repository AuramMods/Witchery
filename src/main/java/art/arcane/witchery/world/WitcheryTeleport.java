package art.arcane.witchery.world;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.portal.PortalInfo;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.ITeleporter;

import java.util.function.Function;

public final class WitcheryTeleport {
    private static final TicketType<Integer> ARRIVAL = TicketType.create("witchery_travel_arrival", Integer::compareTo, 40);

    private WitcheryTeleport() {}

    public static boolean canArrive(Entity actor, ServerLevel level, Vec3 position) {
        if (!Double.isFinite(position.x) || !Double.isFinite(position.y) || !Double.isFinite(position.z)) return false;
        BlockPos pos = BlockPos.containing(position);
        return !level.isOutsideBuildHeight(pos) && level.getWorldBorder().isWithinBounds(pos)
                && !level.getBlockCollisions(actor, actor.getBoundingBox().move(position.subtract(actor.position())))
                .iterator().hasNext();
    }

    public static boolean move(Entity actor, ServerLevel destination, Vec3 target) {
        ServerLevel origin = (ServerLevel) actor.level();
        Vec3 start = actor.position();
        Entity moved = actor;
        if (actor instanceof ServerPlayer player) {
            player.teleportTo(destination, target.x, target.y, target.z, player.getYRot(), player.getXRot());
            if (player.connection != null) player.connection.resetPosition();
        } else if (origin == destination) {
            actor.teleportTo(target.x, target.y, target.z);
        } else {
            moved = actor.changeDimension(destination, new ITeleporter() {
                @Override
                public PortalInfo getPortalInfo(Entity entity, ServerLevel level, Function<ServerLevel, PortalInfo> defaultInfo) {
                    return new PortalInfo(target, Vec3.ZERO, entity.getYRot(), entity.getXRot());
                }

                @Override
                public Entity placeEntity(Entity entity, ServerLevel source, ServerLevel level, float yaw,
                                          Function<Boolean, Entity> reposition) {
                    return reposition.apply(false);
                }
            });
        }
        if (moved == null || moved.level() != destination || moved.distanceToSqr(target) > 0.01) return false;
        if (origin != destination && !(moved instanceof ServerPlayer)) {
            // Make the destination entity-ticking long enough for its entity storage to load.
            destination.getChunkSource().addRegionTicket(ARRIVAL, moved.chunkPosition(), 2, moved.getId());
        }
        moved.resetFallDistance();
        TormentTravel.transitionEffect(origin, start);
        TormentTravel.transitionEffect(destination, target);
        return true;
    }
}
