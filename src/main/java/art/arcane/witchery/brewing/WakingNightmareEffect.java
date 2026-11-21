package art.arcane.witchery.brewing;

import art.arcane.witchery.registry.WitcheryEffects;
import art.arcane.witchery.ritual.DirectedCurse;
import art.arcane.witchery.world.DreamTravel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class WakingNightmareEffect extends MobEffect {
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
                || target instanceof ServerPlayer && DirectedCurse.WAKING_NIGHTMARE.strength(target) > 0;
    }
}
