package art.arcane.witchery.client.event;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.client.WitcheryClientCameraState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Witchery.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class WitcheryClientCameraHooks {
    private static LocalPlayer heldPlayer;
    private static boolean previousNoGravity;
    private static Entity activeCamera;

    private WitcheryClientCameraHooks() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        ClientLevel level = minecraft.level;
        if (player == null || level == null) return;

        if (!WitcheryClientCameraState.isActive() || level.getGameTime() - WitcheryClientCameraState.getActiveTick() > 60) {
            WitcheryClientCameraState.markInactive();
            restoreMovement();
            if (activeCamera != null && minecraft.getCameraEntity() == activeCamera) minecraft.setCameraEntity(player);
            activeCamera = null;
            return;
        }

        if (heldPlayer != player) {
            restoreMovement();
            heldPlayer = player;
            previousNoGravity = player.isNoGravity();
        }
        player.setNoGravity(true);
        player.setDeltaMovement(Vec3.ZERO);

        int targetId = WitcheryClientCameraState.getTargetEntityId();
        Entity target = targetId > 0 ? level.getEntity(targetId) : null;
        if (target != null && !target.isRemoved()) {
            activeCamera = target;
            if (minecraft.getCameraEntity() != target) minecraft.setCameraEntity(target);
        }
    }

    public static boolean isScryingFrom(Entity camera) {
        return camera != null && camera == activeCamera && WitcheryClientCameraState.isActive();
    }

    @SubscribeEvent
    public static void onMovementInput(MovementInputUpdateEvent event) {
        if (!WitcheryClientCameraState.isActive()) return;
        var input = event.getInput();
        input.leftImpulse = 0;
        input.forwardImpulse = 0;
        input.jumping = false;
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        WitcheryClientCameraState.markInactive();
        restoreMovement();
        activeCamera = null;
    }

    private static void restoreMovement() {
        if (heldPlayer == null) return;
        heldPlayer.setNoGravity(previousNoGravity);
        heldPlayer = null;
    }
}
