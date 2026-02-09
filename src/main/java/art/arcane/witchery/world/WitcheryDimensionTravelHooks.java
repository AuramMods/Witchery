package art.arcane.witchery.world;

import art.arcane.witchery.Witchery;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Locale;
import java.util.Optional;

public final class WitcheryDimensionTravelHooks {
    private WitcheryDimensionTravelHooks() {
    }

    public static boolean isWitcheryDimension(ResourceKey<Level> levelKey) {
        return WitcheryDimensions.levels().contains(levelKey);
    }

    public static Optional<ResourceKey<Level>> resolveTarget(ResourceKey<Level> currentLevel, TravelTrigger trigger) {
        return switch (trigger) {
            case DREAM_PORTAL -> Optional.of(routeToOrReturn(currentLevel, WitcheryDimensions.DREAM));
            case TORMENT_PORTAL -> Optional.of(routeToOrReturn(currentLevel, WitcheryDimensions.TORMENT));
            case MIRROR_RITE -> Optional.of(routeToOrReturn(currentLevel, WitcheryDimensions.MIRROR));
            case RETURN_TO_OVERWORLD -> isWitcheryDimension(currentLevel)
                    ? Optional.of(Level.OVERWORLD)
                    : Optional.empty();
        };
    }

    public static boolean routePlayer(ServerPlayer player, TravelTrigger trigger) {
        ResourceKey<Level> current = player.serverLevel().dimension();
        Optional<ResourceKey<Level>> targetKey = resolveTarget(current, trigger);
        if (targetKey.isEmpty()) {
            Witchery.LOGGER.debug(
                    "No dimension travel route resolved for trigger={} current={} player={}",
                    trigger.key(), dimensionId(current), player.getGameProfile().getName()
            );
            return false;
        }

        ServerLevel targetLevel = player.server.getLevel(targetKey.get());
        if (targetLevel == null) {
            Witchery.LOGGER.warn(
                    "Dimension travel route '{}' resolved to unavailable level {}",
                    trigger.key(), dimensionId(targetKey.get())
            );
            return false;
        }

        Vec3 spawn = Vec3.atCenterOf(targetLevel.getSharedSpawnPos());
        player.teleportTo(targetLevel, spawn.x, spawn.y, spawn.z, player.getYRot(), player.getXRot());
        Witchery.LOGGER.info(
                "Scaffold dimension route '{}' moved player={} {} -> {}",
                trigger.key(), player.getGameProfile().getName(), dimensionId(current), dimensionId(targetLevel.dimension())
        );
        return true;
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

    private static ResourceKey<Level> routeToOrReturn(ResourceKey<Level> currentLevel, ResourceKey<Level> routeLevel) {
        if (currentLevel.equals(routeLevel)) {
            return Level.OVERWORLD;
        }
        return routeLevel;
    }

    private static String dimensionId(ResourceKey<Level> levelKey) {
        return levelKey.location().toString();
    }

    public enum TravelTrigger {
        DREAM_PORTAL,
        TORMENT_PORTAL,
        MIRROR_RITE,
        RETURN_TO_OVERWORLD;

        public String key() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}
