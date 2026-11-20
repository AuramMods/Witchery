package art.arcane.witchery.world;

import art.arcane.witchery.Witchery;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public final class WitcheryDimensionTravelHooks {
    private WitcheryDimensionTravelHooks() {
    }

    private static boolean isWitcheryDimension(ResourceKey<Level> levelKey) {
        return WitcheryDimensions.levels().contains(levelKey);
    }

    public static void returnToOverworld(ServerPlayer player) {
        ServerLevel overworld = player.server.overworld();
        Vec3 spawn = Vec3.atCenterOf(overworld.getSharedSpawnPos());
        player.teleportTo(overworld, spawn.x, spawn.y, spawn.z, player.getYRot(), player.getXRot());
        player.connection.resetPosition();
    }

    public static void onEntityTravelToDimension(Entity entity, ResourceKey<Level> destination) {
        if (!(entity instanceof ServerPlayer player)) {
            return;
        }

        ResourceKey<Level> from = player.serverLevel().dimension();
        if (!isWitcheryDimension(from) && !isWitcheryDimension(destination)) {
            return;
        }

        Witchery.LOGGER.debug(
                "Observed dimension travel attempt player={} from={} to={}",
                player.getGameProfile().getName(), dimensionId(from), dimensionId(destination)
        );
    }

    public static void onPlayerChangedDimension(ServerPlayer player, ResourceKey<Level> from, ResourceKey<Level> to) {
        if (!isWitcheryDimension(from) && !isWitcheryDimension(to)) {
            return;
        }

        Witchery.LOGGER.info(
                "Observed dimension transition player={} from={} to={}",
                player.getGameProfile().getName(), dimensionId(from), dimensionId(to)
        );
    }

    private static String dimensionId(ResourceKey<Level> levelKey) {
        return levelKey.location().toString();
    }
}
