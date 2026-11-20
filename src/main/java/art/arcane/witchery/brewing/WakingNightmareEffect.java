package art.arcane.witchery.brewing;

import art.arcane.witchery.registry.WitcheryEffects;
import art.arcane.witchery.world.DreamTravel;
import art.arcane.witchery.world.WitcheryDimensions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class WakingNightmareEffect extends MobEffect {
    private static final String CURSE = "witcheryWakingNightmare";

    public WakingNightmareEffect() {
        super(MobEffectCategory.HARMFUL, 0x342836);
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        return List.of();
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity target, int amplifier) {
        if (!target.level().isClientSide() && target.level().getGameTime() % 20 == 3) {
            DreamTravel.spawnWakingNightmare(target, amplifier + 1);
        }
    }

    public static boolean isHaunted(LivingEntity target) {
        return target.hasEffect(WitcheryEffects.WAKING_NIGHTMARE.get())
                || target instanceof ServerPlayer && curseData(target).getInt(CURSE) > 0;
    }

    public static void tickCurse(ServerPlayer target) {
        if (target.level().getGameTime() % 20 != 0) return;
        CompoundTag data = curseData(target);
        int level = data.getInt(CURSE);
        if (level > 0 && !target.level().dimension().equals(WitcheryDimensions.DREAM)) {
            DreamTravel.spawnWakingNightmare(target, level);
        } else {
            data.remove(CURSE);
        }
    }

    public static void changeCurse(LivingEntity target, boolean apply) {
        CompoundTag data = curseData(target);
        int level = data.getInt(CURSE);
        if (apply) {
            data.putInt(CURSE, Math.max(1, level));
        } else if (level > 0 && target.getRandom().nextInt(4) < (level == 1 ? 1 : 3)) {
            data.putInt(CURSE, level + 1);
        } else {
            data.remove(CURSE);
            target.removeEffect(MobEffects.POISON);
            target.removeEffect(MobEffects.WEAKNESS);
            target.removeEffect(MobEffects.BLINDNESS);
            target.removeEffect(MobEffects.DIG_SLOWDOWN);
            target.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
        }
        if (target instanceof ServerPlayer) target.getPersistentData().put(Player.PERSISTED_NBT_TAG, data);
    }

    private static CompoundTag curseData(LivingEntity target) {
        return target instanceof ServerPlayer ? target.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG) : target.getPersistentData();
    }
}
