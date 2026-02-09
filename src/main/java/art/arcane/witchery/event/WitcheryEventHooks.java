package art.arcane.witchery.event;

import art.arcane.witchery.capability.WitcheryPlayerDataProvider;
import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.Witchery;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

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
                data.setInitialized(true);
                data.bumpSyncRevision();
                WitcheryNetwork.sendTo(serverPlayer, "extended_player_sync");
            });
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        event.getOriginal().reviveCaps();
        WitcheryPlayerDataProvider.get(event.getOriginal()).ifPresent(oldData ->
                WitcheryPlayerDataProvider.get(event.getEntity()).ifPresent(newData -> newData.copyFrom(oldData)));
        event.getOriginal().invalidateCaps();
    }

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        // Placeholder hook for world/runtime registry bootstrap migration.
    }
}
