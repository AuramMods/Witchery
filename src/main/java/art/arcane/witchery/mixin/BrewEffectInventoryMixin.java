package art.arcane.witchery.mixin;

import art.arcane.witchery.client.BrewEffectLabels;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EffectRenderingInventoryScreen.class)
public abstract class BrewEffectInventoryMixin {
    @Inject(method = "getEffectName", at = @At("HEAD"), cancellable = true)
    private void witchery$showBrewLabel(MobEffectInstance effect, CallbackInfoReturnable<Component> callback) {
        var label = BrewEffectLabels.inventoryLabel(effect);
        if (label != null) callback.setReturnValue(label);
    }
}
