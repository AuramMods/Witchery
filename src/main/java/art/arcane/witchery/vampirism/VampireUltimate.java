package art.arcane.witchery.vampirism;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

public enum VampireUltimate {
    NONE,
    STORM,
    SWARM,
    FARM;

    public static final int MAX_CHARGES = 5;
    private static final VampireUltimate[] BY_ID = values();

    public static VampireUltimate fromId(int id) {
        return id >= 0 && id < BY_ID.length ? BY_ID[id] : NONE;
    }

    boolean activate(ServerPlayer player) {
        return this == STORM && startStorm(player);
    }

    private static boolean startStorm(ServerPlayer player) {
        var world = player.serverLevel();
        if (!world.dimension().equals(Level.OVERWORLD) || world.getLevelData().isRaining()) return false;
        int duration = 20 * (300 + world.random.nextInt(600));
        world.setWeatherParameters(0, duration, true, true);
        return true;
    }
}
