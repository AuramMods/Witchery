package art.arcane.witchery.mixin;

import art.arcane.witchery.brewing.ResizingEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class ResizingPoseEyeHeightMixin {
    @Inject(method = "getEyeHeight(Lnet/minecraft/world/entity/Pose;)F",
            at = @At("RETURN"), cancellable = true)
    private void witchery$scaleShapeEyeHeight(Pose pose, CallbackInfoReturnable<Float> result) {
        // Woodwalkers uses the pose-specific query for crouching humanoids. The cached
        // eye height used for standing shapes already goes through Forge's size event.
        if (this instanceof ResizingEffect.Scaled shape && shape.witchery$hasShapeScale()) {
            result.setReturnValue(result.getReturnValueF() * shape.witchery$scale());
        }
    }
}
