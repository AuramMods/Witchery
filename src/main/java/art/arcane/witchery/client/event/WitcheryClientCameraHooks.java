package art.arcane.witchery.client.event;

import art.arcane.witchery.Witchery;
import net.minecraft.Util;
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
    private static final long HEARTBEAT_TIMEOUT_MS = 3_000;

    private record View(LocalPlayer viewer, ClientLevel level, int targetId) {}

    private static View view;
    private static long lastHeartbeat;
    private static boolean previousNoGravity;
    private static Entity activeCamera;

    private WitcheryClientCameraHooks() {
    }

    public static void updateView(boolean active, boolean updatePosition, int targetId) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!active) {
            closeView(minecraft);
            return;
        }

        LocalPlayer viewer = minecraft.player;
        ClientLevel level = minecraft.level;
        if (viewer == null || level == null || !viewer.isAlive()) return;
        if (updatePosition) {
            closeView(minecraft);
            view = new View(viewer, level, targetId);
        } else if (view == null || view.viewer() != viewer || view.level() != level || view.targetId() != targetId) {
            return;
        }
        lastHeartbeat = Util.getMillis();
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || view == null) return;

        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer viewer = view.viewer();
        if (minecraft.player != viewer || minecraft.level != view.level() || !viewer.isAlive()) {
            closeView(minecraft);
            return;
        }

        Entity target = view.level().getEntity(view.targetId());
        if (target == null || !target.isAlive() || Util.getMillis() - lastHeartbeat > HEARTBEAT_TIMEOUT_MS) {
            restoreCamera(minecraft);
            return;
        }

        if (activeCamera == null) previousNoGravity = viewer.isNoGravity();
        activeCamera = target;
        viewer.setNoGravity(true);
        viewer.setDeltaMovement(Vec3.ZERO);
        if (minecraft.getCameraEntity() != target) minecraft.setCameraEntity(target);
    }

    public static boolean isScryingFrom(Entity camera) {
        return camera != null && camera == activeCamera && camera.isAlive();
    }

    @SubscribeEvent
    public static void onMovementInput(MovementInputUpdateEvent event) {
        if (view == null || event.getEntity() != view.viewer() || !isScryingFrom(activeCamera)) return;
        var input = event.getInput();
        input.leftImpulse = 0;
        input.forwardImpulse = 0;
        input.jumping = false;
    }

    @SubscribeEvent
    public static void onPlayerClone(ClientPlayerNetworkEvent.Clone event) {
        if (activeCamera != null && view.viewer() == event.getOldPlayer() && event.getPlayer().isNoGravity()) {
            // Vanilla can copy the temporary scrying flag to the new player during dimension travel.
            event.getPlayer().setNoGravity(previousNoGravity);
        }
        closeView(Minecraft.getInstance());
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        closeView(Minecraft.getInstance());
    }

    private static void closeView(Minecraft minecraft) {
        restoreCamera(minecraft);
        view = null;
    }

    private static void restoreCamera(Minecraft minecraft) {
        if (activeCamera == null) return;
        view.viewer().setNoGravity(previousNoGravity);
        if (minecraft.player != null && minecraft.getCameraEntity() == activeCamera) {
            minecraft.setCameraEntity(minecraft.player);
        }
        activeCamera = null;
    }
}
