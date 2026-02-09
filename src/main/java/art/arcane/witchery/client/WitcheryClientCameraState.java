package art.arcane.witchery.client;

public final class WitcheryClientCameraState {
    private static final int NO_TARGET_ENTITY_ID = -1;

    private static boolean active;
    private static long activeTick = Long.MIN_VALUE;
    private static int targetEntityId = NO_TARGET_ENTITY_ID;

    private WitcheryClientCameraState() {
    }

    public static boolean isActive() {
        return active;
    }

    public static long getActiveTick() {
        return activeTick;
    }

    public static int getTargetEntityId() {
        return targetEntityId;
    }

    public static void markActive(long gameTime) {
        active = true;
        activeTick = gameTime;
    }

    public static void markInactive() {
        active = false;
        activeTick = Long.MIN_VALUE;
        targetEntityId = NO_TARGET_ENTITY_ID;
    }

    public static void setTargetEntityId(int entityId) {
        targetEntityId = entityId;
    }
}
