package art.arcane.witchery.brewing;

import art.arcane.witchery.registry.WitcheryEffects;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

import java.util.List;

public final class PlantBrewEffect extends TrackedBrewEffect {
    private final boolean wrapped;

    public PlantBrewEffect(boolean wrapped, int color) {
        super(wrapped ? MobEffectCategory.HARMFUL : MobEffectCategory.BENEFICIAL, color);
        this.wrapped = wrapped;
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        return wrapped ? List.of() : super.getCurativeItems();
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return !wrapped;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!(entity.level() instanceof ServerLevel level) || level.getGameTime() % 5 != 3) return;
        double reach = 0.2 + 0.1 * amplifier;
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(reach, 0, reach),
                target -> target != entity && target.isAlive())) {
            target.hurt(level.damageSources().cactus(), 1 + amplifier);
        }
    }

    public static void amplifyFireDamage(LivingHurtEvent event) {
        if (!event.getSource().is(DamageTypeTags.IS_FIRE)) return;
        var effect = event.getEntity().getEffect(WitcheryEffects.WRAPPED_IN_VINE.get());
        if (effect != null) event.setAmount(event.getAmount() * Math.min(effect.getAmplifier() + 1, 4));
    }
}
