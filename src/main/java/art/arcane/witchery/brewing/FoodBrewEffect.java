package art.arcane.witchery.brewing;

import art.arcane.witchery.registry.WitcheryEffects;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class FoodBrewEffect extends MobEffect {
    public enum Kind { MOONSHINE, STOUT_BELLY }

    private final Kind kind;

    public FoodBrewEffect(Kind kind, int color) {
        super(MobEffectCategory.BENEFICIAL, color);
        this.kind = kind;
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return amplifier > 0;
    }

    @Override
    public void applyEffectTick(LivingEntity target, int amplifier) {
        if (!(target.level() instanceof ServerLevel level) || amplifier < 1) return;
        long phase = level.getDayTime() % 20;
        if (kind == Kind.STOUT_BELLY) {
            if (phase == 3) target.removeEffect(MobEffects.CONFUSION);
        } else if (phase == 2 && !target.hasEffect(MobEffects.CONFUSION) && !target.hasEffect(WitcheryEffects.STOUT_BELLY.get())
                && level.random.nextInt(5 - Math.min(amplifier, 3)) == 0) {
            target.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 20 * (6 + amplifier * 2)));
        }
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        return List.of();
    }
}
