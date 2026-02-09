package art.arcane.witchery.event;

import art.arcane.witchery.capability.WitcheryPlayerDataProvider;
import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.Witchery;
import art.arcane.witchery.world.WitcheryDimensionTravelHooks;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.event.TickEvent;

@Mod.EventBusSubscriber(modid = Witchery.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class WitcheryEventHooks {
    private WitcheryEventHooks() {
    }

    @SubscribeEvent
    public static void onAttachEntityCapabilities(AttachCapabilitiesEvent<Entity> event) {
        WitcheryPlayerDataProvider.attach(event);
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            WitcheryPlayerDataProvider.get(serverPlayer).ifPresent(data -> {
                if (data.getOtherPlayerSkin().isEmpty()) {
                    data.applyPlayerStyle(
                            serverPlayer.getGameProfile().getName(),
                            data.getGrotesqueTicks(),
                            data.getNightmareLevel(),
                            data.isGhost(),
                            data.getCreatureTypeOrdinal(),
                            data.getHumanBlood()
                    );
                }
                data.setInitialized(true);
                data.bumpSyncRevision();
                WitcheryNetwork.sendExtendedPlayerSync(serverPlayer, data);
                WitcheryNetwork.sendPlayerSync(serverPlayer, data);
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        event.getOriginal().reviveCaps();
        WitcheryPlayerDataProvider.get(event.getOriginal()).ifPresent(oldData ->
                WitcheryPlayerDataProvider.get(event.getEntity()).ifPresent(newData -> newData.copyFrom(oldData)));
        event.getOriginal().invalidateCaps();

        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            WitcheryPlayerDataProvider.get(serverPlayer).ifPresent(data ->
                    WitcheryNetwork.sendPartialExtendedPlayerSync(serverPlayer, data));
        }
    }

    @SubscribeEvent
    public static void onEntityTravelToDimension(EntityTravelToDimensionEvent event) {
        WitcheryDimensionTravelHooks.onEntityTravelToDimension(event.getEntity(), event.getDimension());
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
            WitcheryDimensionTravelHooks.onPlayerChangedDimension(serverPlayer, event.getFrom(), event.getTo());
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        if (event.player.level().isClientSide()) {
            return;
        }

        if (!(event.player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        if (serverPlayer.isPassenger() || serverPlayer.isVehicle()) {
            return;
        }

        WitcheryDimensionTravelHooks.resolvePortalCollisionTrigger(serverPlayer)
                .ifPresent(trigger -> WitcheryDimensionTravelHooks.routePlayerFromPortalCollision(serverPlayer, trigger));
    }

    @SubscribeEvent
    public static void onPlayerRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide()) {
            return;
        }

        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }

        if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
            return;
        }

        // Scaffold activation gesture for portal/rite trigger testing.
        if (!serverPlayer.isShiftKeyDown()) {
            return;
        }

        WitcheryDimensionTravelHooks.resolveRiteTriggerFromBlock(event.getLevel().getBlockState(event.getPos()))
                .ifPresent(trigger -> {
                    if (WitcheryDimensionTravelHooks.routePlayer(serverPlayer, trigger)) {
                        event.setCanceled(true);
                        event.setCancellationResult(InteractionResult.SUCCESS);
                    }
                });
    }

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        // Placeholder hook for world/runtime registry bootstrap migration.
    }
}
