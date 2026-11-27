package art.arcane.witchery.mixin;

import art.arcane.witchery.entity.WitcheryBolt;
import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractArrow.class)
abstract class LegacyBoltAttachmentMixin {
    @Inject(method = "shouldFall", at = @At("HEAD"), cancellable = true)
    private void witchery$consultSavedBoltAnchor(CallbackInfoReturnable<Boolean> result) {
        if ((Object) this instanceof WitcheryBolt bolt) {
            Boolean falling = bolt.shouldFallFromSavedAnchor();
            if (falling != null) result.setReturnValue(falling);
        }
    }
}
