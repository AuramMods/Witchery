package art.arcane.witchery.client;

import art.arcane.witchery.brewing.BrewDye;
import art.arcane.witchery.registry.WitcheryEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import org.jetbrains.annotations.Nullable;

public final class BrewEffectLabels {
    private BrewEffectLabels() {
    }

    @Nullable
    public static Component inventoryLabel(MobEffectInstance effect) {
        if (effect.getEffect() == WitcheryEffects.COLORFUL.get()) {
            return BrewDye.byLegacyId(effect.getAmplifier()).label();
        }
        if (effect.getEffect() == WitcheryEffects.INSANITY.get()) {
            int index = Math.floorMod(effect.getDuration() / 60, 7);
            return Component.translatable("effect.witchery.potion.insane." + index);
        }
        return null;
    }
}
