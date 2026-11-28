package art.arcane.witchery.world;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

public final class DreamAtmosphere {
    private static final Map<ServerLevel, Integer> SERVER_MODES = new WeakHashMap<>();
    private static WeakReference<Level> clientLevel = new WeakReference<>(null);
    private static int clientMode;

    private DreamAtmosphere() {}

    public static int mode(@Nullable Level level) {
        if (level instanceof ServerLevel server) {
            return SERVER_MODES.getOrDefault(server, 0);
        }
        return level != null && clientLevel.get() == level ? clientMode : 0;
    }

    public static boolean tickServer(ServerLevel level) {
        return sample(level, level.getRandom());
    }

    static boolean sample(ServerLevel level, RandomSource random) {
        if (random.nextInt(20) != 0) return false;

        int mode = 0;
        for (ServerPlayer player : level.players()) {
            mode = DreamTravel.nightmareMode(player);
            if (mode != 0) break;
        }
        if (mode == mode(level)) return false;

        if (mode == 0) SERVER_MODES.remove(level);
        else SERVER_MODES.put(level, mode);
        level.updateSkyBrightness();
        return true;
    }

    public static void updateClient(Level level, int mode) {
        if (clientLevel.get() != level) clientLevel = new WeakReference<>(level);
        clientMode = Mth.clamp(mode, 0, 2);
        level.updateSkyBrightness();
    }
}
