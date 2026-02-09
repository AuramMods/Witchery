package art.arcane.witchery.event;

import art.arcane.witchery.Witchery;
import net.minecraft.world.entity.Entity;
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
        // Placeholder hook for capability migration (e.g. ExtendedPlayer replacement).
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        // Placeholder hook for player sync/bootstrap migration.
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        // Placeholder hook for persistent player-data migration.
    }

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        // Placeholder hook for world/runtime registry bootstrap migration.
    }
}
