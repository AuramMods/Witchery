package art.arcane.witchery.mixin;

import art.arcane.witchery.brewing.ResizingEffect;
import art.arcane.witchery.registry.WitcheryEffects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class ResizingLivingEntityMixin implements ResizingEffect.Scaled {
    @Unique private float witchery$resizeScale = 1;
    @Unique @Nullable private LivingEntity witchery$resizedShape;
    @Unique private boolean witchery$hasShapeScale;

    @Override
    public float witchery$scale() {
        return witchery$resizeScale;
    }

    @Override
    public void witchery$setScale(float scale) {
        witchery$resizeScale = scale;
    }

    @Override
    @Nullable
    public LivingEntity witchery$replaceResizedShape(@Nullable LivingEntity shape) {
        LivingEntity previous = witchery$resizedShape;
        witchery$resizedShape = shape;
        return previous;
    }

    @Override
    public boolean witchery$hasShapeScale() {
        return witchery$hasShapeScale;
    }

    @Override
    public void witchery$setHasShapeScale(boolean hasShapeScale) {
        witchery$hasShapeScale = hasShapeScale;
    }

    @Inject(method = "tickEffects", at = @At("TAIL"))
    private void witchery$tickResize(CallbackInfo callback) {
        ResizingEffect.tick((LivingEntity) (Object) this);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void witchery$saveResize(CompoundTag tag, CallbackInfo callback) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (witchery$resizeScale != 1 && entity.hasEffect(WitcheryEffects.RESIZING.get())) {
            tag.putFloat("WITCResizeScale", witchery$resizeScale);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void witchery$readResize(CompoundTag tag, CallbackInfo callback) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (entity.hasEffect(WitcheryEffects.RESIZING.get())) {
            ResizingEffect.setScale(entity, tag.getFloat("WITCResizeScale"));
        }
    }
}
