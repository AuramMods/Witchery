package art.arcane.witchery.brewing;

import art.arcane.witchery.registry.WitcheryEffects;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public final class ChilledEffect extends MobEffect {
    public ChilledEffect() {
        super(MobEffectCategory.NEUTRAL, "witchery:potion.chilled".hashCode() & 0xFFFFFF);
        addAttributeModifier(Attributes.MOVEMENT_SPEED, "7A20B8CD-7A97-4800-A7DC-5B464E31A11A", -0.1,
                AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        int interval = 25 >> Math.min(amplifier, 31);
        return interval == 0 || duration % interval == 0;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide && (entity instanceof Blaze || amplifier >= 2)) {
            entity.hurt(entity.damageSources().magic(), 1);
        }
    }

    public static void reduceFireDamage(LivingHurtEvent event) {
        if (!event.getSource().is(DamageTypeTags.IS_FIRE)) return;
        var chilled = event.getEntity().getEffect(WitcheryEffects.CHILLED.get());
        if (chilled == null) return;
        int amplifier = chilled.getAmplifier();
        float damage = event.getAmount();
        float minimum = amplifier >= 2 ? 0 : Math.min(damage, 1);
        event.setAmount(Math.max(damage - (1 + amplifier), minimum));
    }
}
