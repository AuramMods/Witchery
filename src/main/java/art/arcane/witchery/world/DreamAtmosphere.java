package art.arcane.witchery.world;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;

public final class DreamAtmosphere {
    private static WeakReference<Level> clientLevel = new WeakReference<>(null);
    private static int clientMode;

    private DreamAtmosphere() {}

    public static int mode(@Nullable Level level) {
        if (level instanceof ServerLevel server) {
            return DreamTravel.isDemonicNightmare(server) ? 2 : DreamTravel.isNightmare(server) ? 1 : 0;
        }
        return level != null && clientLevel.get() == level ? clientMode : 0;
    }

    public static void updateClient(Level level, int mode) {
        if (clientLevel.get() != level) clientLevel = new WeakReference<>(level);
        clientMode = Mth.clamp(mode, 0, 2);
        level.updateSkyBrightness();
    }
}
