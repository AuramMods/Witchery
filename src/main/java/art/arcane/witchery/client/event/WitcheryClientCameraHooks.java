package art.arcane.witchery.client.event;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.client.WitcheryClientCameraState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Witchery.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class WitcheryClientCameraHooks {
    private WitcheryClientCameraHooks() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        ClientLevel level = minecraft.level;
        if (player == null || level == null) {
            return;
        }

        if (!WitcheryClientCameraState.isActive()) {
            if (minecraft.getCameraEntity() != player) {
                minecraft.setCameraEntity(player);
            }
            return;
        }

        int targetEntityId = WitcheryClientCameraState.getTargetEntityId();
        if (targetEntityId <= 0) {
            return;
        }

        Entity target = level.getEntity(targetEntityId);
        if (target == null || target.isRemoved()) {
            if (minecraft.getCameraEntity() != player) {
                minecraft.setCameraEntity(player);
            }
            return;
        }

        if (minecraft.getCameraEntity() != target) {
            minecraft.setCameraEntity(target);
        }
    }
}
