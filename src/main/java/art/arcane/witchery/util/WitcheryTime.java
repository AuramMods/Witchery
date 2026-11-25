package art.arcane.witchery.util;

public final class WitcheryTime {
    // Epoch ticks from 2001 onward predate Witchery and exceed ordinary saved world ages.
    private static final long MIN_EPOCH_TICK = 1_000_000_000_000L / 50;

    private WitcheryTime() {}

    public static long epochTicks() {
        return System.currentTimeMillis() / 50;
    }

    public static boolean isWorldTick(long saved) {
        return saved > 0 && saved < MIN_EPOCH_TICK;
    }

    public static long toEpochTick(long saved, long worldTime, long epochTime) {
        return isWorldTick(saved) ? Math.max(1, epochTime + saved - worldTime) : saved;
    }
}
