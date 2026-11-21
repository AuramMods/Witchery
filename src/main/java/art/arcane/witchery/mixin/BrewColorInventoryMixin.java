package art.arcane.witchery.mixin;

import art.arcane.witchery.brewing.BrewDye;
import art.arcane.witchery.registry.WitcheryEffects;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EffectRenderingInventoryScreen.class)
public abstract class BrewColorInventoryMixin {
    @Inject(method = "getEffectName", at = @At("HEAD"), cancellable = true)
    private void witchery$showBrewColor(MobEffectInstance effect, CallbackInfoReturnable<Component> callback) {
        if (effect.getEffect() == WitcheryEffects.COLORFUL.get()) {
            callback.setReturnValue(BrewDye.byLegacyId(effect.getAmplifier()).label());
        }
    }
}
