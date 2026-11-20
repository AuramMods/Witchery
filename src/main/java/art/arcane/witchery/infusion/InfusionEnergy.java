package art.arcane.witchery.infusion;

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

    public static void grant(ServerPlayer player, int type, int capacity) {
        CompoundTag data = player.getPersistentData();
        CompoundTag infusion = data.getCompound(Player.PERSISTED_NBT_TAG);
        infusion.putInt(TYPE, type);
        infusion.putInt(CHARGE, capacity);
        infusion.putInt(CAPACITY, capacity);
        infusion.remove("WITCBeastPower");
        infusion.remove("WITCBeastPowerCharges");
        data.put(Player.PERSISTED_NBT_TAG, infusion);
    }

    public static boolean recharge(ServerPlayer player, int amount) {
        CompoundTag infusion = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        int current = infusion.getInt(CHARGE);
        int capacity = infusion.getInt(CAPACITY);
        if (current >= capacity) return false;
        infusion.putInt(CHARGE, Math.min(current + amount, capacity));
        return true;
    }

    public static boolean spend(ServerPlayer player, int cost) {
        CompoundTag infusion = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        if (!infusion.contains(TYPE, Tag.TAG_ANY_NUMERIC) || !infusion.contains(CHARGE, Tag.TAG_ANY_NUMERIC)) {
            explain(player, "required");
            return false;
        }
        if (player.getAbilities().instabuild) return true;
        int available = infusion.getInt(CHARGE);
        if (available < cost) {
            explain(player, "empty");
            return false;
        }
        infusion.putInt(CHARGE, available - cost);
        return true;
    }

    private static void explain(ServerPlayer player, String key) {
        player.displayClientMessage(Component.translatable("message.witchery.infusion." + key)
                .withStyle(ChatFormatting.RED), true);
        player.playSound(SoundEvents.NOTE_BLOCK_SNARE.value(), 0.9F, 1.0F);
    }
}
