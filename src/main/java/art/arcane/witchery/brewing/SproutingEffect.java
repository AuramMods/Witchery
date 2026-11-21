package art.arcane.witchery.brewing;

import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public final class SproutingEffect extends MobEffect {
    public SproutingEffect(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity target, int amplifier) {
        if (target.level() instanceof ServerLevel level && level.getGameTime() % 20 == 9 && level.random.nextInt(4) == 0) {
            BrewTerrain.sprout(level, target.blockPosition().below(), Direction.UP,
                    1 + amplifier, target.getBoundingBox().inflate(0.5), null);
        }
    }
}
