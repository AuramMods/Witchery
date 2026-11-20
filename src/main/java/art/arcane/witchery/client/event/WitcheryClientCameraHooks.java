package art.arcane.witchery.client.event;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.client.WitcheryClientCameraState;
import art.arcane.witchery.entity.ScryEye;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
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
            if (minecraft.getCameraEntity() instanceof ScryEye) minecraft.setCameraEntity(player);
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
        if (targetId > 0 && level.getEntity(targetId) instanceof ScryEye eye && !eye.isRemoved()
                && minecraft.getCameraEntity() != eye) {
            minecraft.setCameraEntity(eye);
        }
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
    }

    private static void restoreMovement() {
        if (heldPlayer == null) return;
        heldPlayer.setNoGravity(previousNoGravity);
        heldPlayer = null;
    }
}
