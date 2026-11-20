package art.arcane.witchery.infusion;

import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class InfusedBrews {
    private static final String TYPE = "WITCInfusedBrewType";
    private static final String START = "WITCInfusedBrewStart";
    private static final int SOARING = 1;
    private static final long SOARING_DURATION = 144_000;

    private InfusedBrews() {}

    public static void applySoaring(LivingEntity drinker) {
        if (!(drinker instanceof ServerPlayer player)) return;
        var data = player.getPersistentData();
        var persisted = data.getCompound(Player.PERSISTED_NBT_TAG);
        persisted.putInt(TYPE, SOARING);
        persisted.putLong(START, timeInTicks());
        data.put(Player.PERSISTED_NBT_TAG, persisted);
    }

    public static boolean hasSoaring(ServerPlayer player) {
        var persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        return persisted.getInt(TYPE) == SOARING && persisted.contains(START, Tag.TAG_ANY_NUMERIC)
                && timeInTicks() - persisted.getLong(START) <= SOARING_DURATION;
    }

    private static long timeInTicks() {
        return System.currentTimeMillis() / 50L;
    }
}
