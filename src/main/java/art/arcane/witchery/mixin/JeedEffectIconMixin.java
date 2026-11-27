package art.arcane.witchery.mixin;

import art.arcane.witchery.client.WitcheryEffectIcons;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "net.mehvahdjukaar.jeed.common.EffectRenderer", remap = false)
abstract class JeedEffectIconMixin {
    @Inject(method = "render(Lnet/minecraft/client/gui/GuiGraphics;Lnet/minecraft/world/effect/MobEffectInstance;IIII)V",
            at = @At("HEAD"), cancellable = true, require = 0)
    private void witchery$skipMissingEffectSprite(GuiGraphics graphics, MobEffectInstance effect,
                                                  int x, int y, int width, int height, CallbackInfo callback) {
        var type = effect.getEffect();
        if (WitcheryEffectIcons.owns(type) && WitcheryEffectIcons.hasMissingSprite(type)) callback.cancel();
    }
}
