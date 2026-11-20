package art.arcane.witchery.brewing;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class MortalCoilEffect extends MobEffect {
    public MortalCoilEffect() {
        super(MobEffectCategory.HARMFUL, 0x000000);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) { return duration == 1; }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide) entity.kill();
    }

    @Override
    public List<ItemStack> getCurativeItems() { return List.of(); }
}
