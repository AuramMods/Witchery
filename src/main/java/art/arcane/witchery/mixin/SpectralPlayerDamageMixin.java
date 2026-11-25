package art.arcane.witchery.mixin;

import art.arcane.witchery.registry.WitcheryDamageTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Player.class)
public abstract class SpectralPlayerDamageMixin {
    @Redirect(method = "actuallyHurt", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;getAbsorptionAmount()F"))
    private float witchery$spectralAbsorption(Player player, DamageSource source, float amount) {
        return source.is(WitcheryDamageTypes.SPECTRAL_TOUCH) ? 0 : player.getAbsorptionAmount();
    }

    @Redirect(method = "actuallyHurt", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Player;setAbsorptionAmount(F)V"))
    private void witchery$preserveSpectralAbsorption(Player player, float absorption, DamageSource source, float amount) {
        if (!source.is(WitcheryDamageTypes.SPECTRAL_TOUCH)) player.setAbsorptionAmount(absorption);
    }
}
