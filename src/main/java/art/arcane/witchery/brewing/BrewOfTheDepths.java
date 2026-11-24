package art.arcane.witchery.brewing;

import art.arcane.witchery.registry.WitcheryEffects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class BrewOfTheDepths {
    private static final String REMAINING_SECONDS = "witcheryDepths";
    private static final int DURATION_SECONDS = 300;
    private static final int INFINITE = -1;

    private BrewOfTheDepths() {}

    public static void drink(LivingEntity drinker) {
        if (drinker instanceof ServerPlayer player) {
            setRemainingSeconds(player, DURATION_SECONDS);
            player.removeEffect(WitcheryEffects.DEPTHS.get());
        }
    }

    public static void restorePortEffect(ServerPlayer player) {
        MobEffectInstance previous = player.getEffect(WitcheryEffects.DEPTHS.get());
        if (previous == null) return;
        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        if (!persisted.contains(REMAINING_SECONDS, Tag.TAG_ANY_NUMERIC)) {
            int duration = previous.getDuration();
            if (previous.isInfiniteDuration() || duration > 0) {
                setRemainingSeconds(player, previous.isInfiniteDuration() ? INFINITE : 1 + (duration - 1) / 20);
            }
        }
        player.removeEffect(WitcheryEffects.DEPTHS.get());
    }

    public static void tickSecond(ServerPlayer player) {
        if (!player.isAlive()) return;
        restorePortEffect(player);
        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        if (!persisted.contains(REMAINING_SECONDS, Tag.TAG_ANY_NUMERIC)) return;
        int seconds = persisted.getInt(REMAINING_SECONDS);
        if (seconds > 0 || seconds == INFINITE) {
            if (!player.hasEffect(MobEffects.WATER_BREATHING)) {
                player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 6_000));
            }
            if (player.isEyeInFluid(FluidTags.WATER)) {
                player.removeEffect(MobEffects.WITHER);
            } else if (!player.hasEffect(MobEffects.WITHER)) {
                player.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1));
            }
        }

        if (seconds > 1) {
            persisted.putInt(REMAINING_SECONDS, seconds - 1);
        } else if (seconds != INFINITE) {
            persisted.remove(REMAINING_SECONDS);
            player.removeEffect(MobEffects.WATER_BREATHING);
            player.removeEffect(MobEffects.POISON);
        }
    }

    public static void clearOnDeath(ServerPlayer player) {
        player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).remove(REMAINING_SECONDS);
        player.removeEffect(WitcheryEffects.DEPTHS.get());
    }

    private static void setRemainingSeconds(ServerPlayer player, int seconds) {
        CompoundTag persisted = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        persisted.putInt(REMAINING_SECONDS, seconds);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, persisted);
    }
}
