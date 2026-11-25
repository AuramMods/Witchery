package art.arcane.witchery.mixin;

import art.arcane.witchery.registry.WitcheryDamageTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public abstract class SpectralLivingDamageMixin {
    // The original takes health directly and leaves absorption hearts intact.
    @Redirect(method = "actuallyHurt", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;getAbsorptionAmount()F"))
    private float witchery$spectralAbsorption(LivingEntity entity, DamageSource source, float amount) {
        return source.is(WitcheryDamageTypes.SPECTRAL_TOUCH) ? 0 : entity.getAbsorptionAmount();
    }

    @Redirect(method = "actuallyHurt", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;setAbsorptionAmount(F)V"))
    private void witchery$preserveSpectralAbsorption(LivingEntity entity, float absorption, DamageSource source, float amount) {
        if (!source.is(WitcheryDamageTypes.SPECTRAL_TOUCH)) entity.setAbsorptionAmount(absorption);
    }
}
