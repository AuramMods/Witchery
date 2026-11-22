package art.arcane.witchery.infusion;

import art.arcane.witchery.integration.BloodMagicCompat;
import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.ritual.DirectedCurse;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;

public final class InfusionEnergy {
    private static final String TYPE = "witcheryInfusionID";
    private static final String CHARGE = "witcheryInfusionCharges";
    private static final String CAPACITY = "witcheryInfusionChargesMax";

    private InfusionEnergy() {
    }

    public static int type(ServerPlayer player) {
        return data(player).getInt(TYPE);
    }

    public static void grant(ServerPlayer player, int type, int capacity) {
        CompoundTag data = player.getPersistentData();
        CompoundTag infusion = data.getCompound(Player.PERSISTED_NBT_TAG);
        infusion.putInt(TYPE, type);
        infusion.putInt(CHARGE, capacity);
        infusion.putInt(CAPACITY, capacity);
        infusion.remove("WITCBeastPower");
        infusion.remove("WITCBeastPowerCharges");
        data.put(Player.PERSISTED_NBT_TAG, infusion);
        sync(player, infusion);
    }

    public static boolean recharge(ServerPlayer player, int amount) {
        CompoundTag infusion = data(player);
        int current = infusion.getInt(CHARGE);
        int capacity = infusion.getInt(CAPACITY);
        if (current >= capacity) return false;
        infusion.putInt(CHARGE, Math.min(current + amount, capacity));
        sync(player, infusion);
        return true;
    }

    public static void drain(ServerPlayer player, float fraction) {
        CompoundTag infusion = data(player);
        int current = infusion.getInt(CHARGE);
        int capacity = infusion.getInt(CAPACITY);
        if (current > 0 && capacity > 0) {
            infusion.putInt(CHARGE, Math.max(0, current - Math.max(1, (int) (capacity * fraction))));
            sync(player, infusion);
        }
        BloodMagicCompat.drain(player, fraction);
    }

    public static boolean spend(ServerPlayer player, int cost) {
        if (!canSpend(player, cost)) return false;
        if (!player.getAbilities().instabuild) {
            CompoundTag infusion = data(player);
            infusion.putInt(CHARGE, infusion.getInt(CHARGE) - cost);
            sync(player, infusion);
        }
        return true;
    }

    public static boolean canSpend(ServerPlayer player, int cost) {
        CompoundTag infusion = data(player);
        if (player.getAbilities().instabuild) return true;
        if (infusion.getInt(TYPE) <= 0 || !infusion.contains(CHARGE, Tag.TAG_ANY_NUMERIC)) {
            explain(player, "required");
            return false;
        }
        if (infusion.getInt(CHARGE) < cost) {
            explain(player, "empty");
            return false;
        }
        return true;
    }

    public static void sync(ServerPlayer player) {
        sync(player, data(player));
    }

    private static void sync(ServerPlayer player, CompoundTag infusion) {
        InfusedBrews.Status brew = InfusedBrews.status(player);
        WitcheryNetwork.sendInfusionStatus(player, infusion.getInt(TYPE), infusion.getInt(CHARGE), infusion.getInt(CAPACITY),
                CreaturePower.activeId(player), CreaturePower.charges(player), brew.type(), brew.remainingTicks(),
                DirectedCurse.SINKING.strength(player));
    }

    private static CompoundTag data(ServerPlayer player) {
        return player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
    }

    private static void explain(ServerPlayer player, String key) {
        player.displayClientMessage(Component.translatable("message.witchery.infusion." + key)
                .withStyle(ChatFormatting.RED), true);
        player.playSound(SoundEvents.NOTE_BLOCK_SNARE.value(), 0.9F, 1.0F);
    }
}
