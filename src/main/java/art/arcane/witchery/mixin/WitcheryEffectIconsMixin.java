package art.arcane.witchery.mixin;

import art.arcane.witchery.client.WitcheryEffectIcons;
import net.minecraft.world.effect.MobEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MobEffect.class)
abstract class WitcheryEffectIconsMixin {
    @Inject(method = "getEffectRendererInternal", at = @At("RETURN"), cancellable = true, remap = false)
    private void witchery$defaultEffectIcons(CallbackInfoReturnable<Object> callback) {
        if (callback.getReturnValue() == null && WitcheryEffectIcons.owns((MobEffect) (Object) this)) {
            callback.setReturnValue(WitcheryEffectIcons.INSTANCE);
        }
    }
}
