package art.arcane.witchery.mixin;

import art.arcane.witchery.world.migration.LegacyWorldEffects;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LegacyActiveEffectsMixin {
    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void witchery$restoreImportedEffectAttributes(CompoundTag saved, CallbackInfo callback) {
        LivingEntity entity = (LivingEntity) (Object) this;
        if (!entity.level().isClientSide) LegacyWorldEffects.restoreActiveAttributes(entity, saved);
    }
}
