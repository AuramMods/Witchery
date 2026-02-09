package art.arcane.witchery.world;

import art.arcane.witchery.Witchery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public final class WitcheryDimensionTravelHooks {
    private static final Map<String, TravelTrigger> PORTAL_TRIGGER_MAP = new LinkedHashMap<>();
    private static final Map<String, TravelTrigger> RITE_TRIGGER_MAP = new LinkedHashMap<>();
    private static final Set<TravelTrigger> PORTAL_COLLISION_TRIGGERS = Set.of(
            TravelTrigger.DREAM_PORTAL,
            TravelTrigger.TORMENT_PORTAL
    );
    private static final String KEY_LAST_PORTAL_COLLISION_TICK = "WITCLastPortalCollisionTick";
    private static final String KEY_LAST_PORTAL_COLLISION_TRIGGER = "WITCLastPortalCollisionTrigger";
    private static final long PORTAL_COLLISION_COOLDOWN_TICKS = 40L;

    static {
        PORTAL_TRIGGER_MAP.put("spiritportal", TravelTrigger.DREAM_PORTAL);
        PORTAL_TRIGGER_MAP.put("tormentportal", TravelTrigger.TORMENT_PORTAL);

        RITE_TRIGGER_MAP.put("mirrorblock", TravelTrigger.MIRROR_RITE);
        RITE_TRIGGER_MAP.put("mirrorblock2", TravelTrigger.MIRROR_RITE);
        RITE_TRIGGER_MAP.put("mirrorwall", TravelTrigger.MIRROR_RITE);
        RITE_TRIGGER_MAP.put("circleglyphotherwhere", TravelTrigger.MIRROR_RITE);
    }

    private WitcheryDimensionTravelHooks() {
    }

    public static boolean isWitcheryDimension(ResourceKey<Level> levelKey) {
        return WitcheryDimensions.levels().contains(levelKey);
    }

    public static Optional<TravelTrigger> resolveTriggerFromBlock(BlockState state) {
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (!Witchery.MODID.equals(blockId.getNamespace())) {
            return Optional.empty();
        }
        TravelTrigger trigger = PORTAL_TRIGGER_MAP.get(blockId.getPath());
        if (trigger != null) {
            return Optional.of(trigger);
        }
        return Optional.ofNullable(RITE_TRIGGER_MAP.get(blockId.getPath()));
    }

    public static Optional<TravelTrigger> resolveRiteTriggerFromBlock(BlockState state) {
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(state.getBlock());
        if (!Witchery.MODID.equals(blockId.getNamespace())) {
            return Optional.empty();
        }
        return Optional.ofNullable(RITE_TRIGGER_MAP.get(blockId.getPath()));
    }

    public static Optional<TravelTrigger> resolvePortalCollisionTrigger(ServerPlayer player) {
        BlockPos feetPos = player.blockPosition();
        Optional<TravelTrigger> feetTrigger = resolvePortalCollisionTriggerFromPos(player.serverLevel(), feetPos);
        if (feetTrigger.isPresent()) {
            return feetTrigger;
        }

        BlockPos eyePos = BlockPos.containing(player.getX(), player.getEyeY(), player.getZ());
        if (!eyePos.equals(feetPos)) {
            return resolvePortalCollisionTriggerFromPos(player.serverLevel(), eyePos);
        }

        return Optional.empty();
    }

    public static boolean routePlayerFromPortalCollision(ServerPlayer player, TravelTrigger trigger) {
        if (!PORTAL_COLLISION_TRIGGERS.contains(trigger)) {
            return false;
        }

        CompoundTag persistent = player.getPersistentData();
        long gameTime = player.serverLevel().getGameTime();
        long lastPortalTick = persistent.getLong(KEY_LAST_PORTAL_COLLISION_TICK);
        if (gameTime - lastPortalTick < PORTAL_COLLISION_COOLDOWN_TICKS) {
            return false;
        }

        if (!routePlayer(player, trigger)) {
            return false;
        }

        persistent.putLong(KEY_LAST_PORTAL_COLLISION_TICK, gameTime);
        persistent.putString(KEY_LAST_PORTAL_COLLISION_TRIGGER, trigger.key());
        return true;
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

    private static Optional<TravelTrigger> resolvePortalCollisionTriggerFromPos(Level level, BlockPos pos) {
        return resolveTriggerFromBlock(level.getBlockState(pos))
                .filter(PORTAL_COLLISION_TRIGGERS::contains);
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
