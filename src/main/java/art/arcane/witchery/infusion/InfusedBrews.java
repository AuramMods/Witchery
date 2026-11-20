package art.arcane.witchery.infusion;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.player.Player;

public final class InfusedBrews {
    private static final String TYPE = "WITCInfusedBrewType";
    private static final String START = "WITCInfusedBrewStart";
    private static final String LAST_GRAVE_USE = "WITCBrewGraveTime";
    public static final int SOARING = 1;
    public static final int GRAVE = 2;
    private static final int DURATION = 144_000;
    private static final int GRAVE_COOLDOWN = 200;

    public record Status(int type, int remainingTicks) {}

    private InfusedBrews() {}

    public static void applySoaring(LivingEntity drinker) {
        apply(drinker, SOARING);
    }

    public static void applyGrave(LivingEntity drinker) {
        apply(drinker, GRAVE);
    }

    private static void apply(LivingEntity drinker, int type) {
        if (!(drinker instanceof ServerPlayer player)) return;
        var data = player.getPersistentData();
        var persisted = data.getCompound(Player.PERSISTED_NBT_TAG);
        persisted.putInt(TYPE, type);
        persisted.putLong(START, timeInTicks());
        data.put(Player.PERSISTED_NBT_TAG, persisted);
        InfusionEnergy.sync(player);
    }

    public static boolean hasSoaring(ServerPlayer player) {
        var persisted = data(player);
        return persisted.getInt(TYPE) == SOARING && remaining(persisted, timeInTicks()) > 0;
    }

    public static boolean useGrave(ServerPlayer player, Mob target) {
        CompoundTag persisted = data(player);
        long now = timeInTicks();
        if (persisted.getInt(TYPE) != GRAVE || remaining(persisted, now) == 0
                || now - persisted.getLong(LAST_GRAVE_USE) <= GRAVE_COOLDOWN || !target.isAlive()
                || target.getMobType() != MobType.UNDEAD || InfernalInfusion.owns(target, player)) return false;
        InfernalInfusion.bind(target, player);
        persisted.putLong(LAST_GRAVE_USE, now);
        return true;
    }

    public static Status status(ServerPlayer player) {
        CompoundTag persisted = data(player);
        int type = persisted.getInt(TYPE);
        int ticks = type == GRAVE || type == SOARING ? remaining(persisted, timeInTicks()) : 0;
        return new Status(ticks > 0 ? type : 0, ticks);
    }

    public static void tick(ServerPlayer player) {
        if (player.tickCount % 20 != 0) return;
        CompoundTag persisted = data(player);
        int type = persisted.getInt(TYPE);
        if ((type == GRAVE || type == SOARING) && remaining(persisted, timeInTicks()) == 0) {
            persisted.remove(TYPE);
            persisted.remove(START);
            InfusionEnergy.sync(player);
        }
    }

    private static int remaining(CompoundTag persisted, long now) {
        if (!persisted.contains(START, Tag.TAG_ANY_NUMERIC)) return 0;
        long start = persisted.getLong(START);
        if (start < now - DURATION) return 0;
        return (int) (DURATION - Math.max(0, now - start));
    }

    private static CompoundTag data(ServerPlayer player) {
        return player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
    }

    private static long timeInTicks() {
        return System.currentTimeMillis() / 50L;
    }
}
