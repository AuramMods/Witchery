package art.arcane.witchery.mixin;

import art.arcane.witchery.client.ClientPlayerAppearance;
import art.arcane.witchery.world.MirrorDisguise;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public abstract class MirrorSkinMixin {
    @Inject(method = "getSkinTextureLocation", at = @At("HEAD"), cancellable = true)
    private void witchery$mirrorSkin(CallbackInfoReturnable<ResourceLocation> callback) {
        String alias = MirrorDisguise.alias((AbstractClientPlayer) (Object) this);
        if (!alias.isEmpty()) callback.setReturnValue(ClientPlayerAppearance.of(alias).texture());
    }

    @Inject(method = "getModelName", at = @At("HEAD"), cancellable = true)
    private void witchery$mirrorModel(CallbackInfoReturnable<String> callback) {
        String alias = MirrorDisguise.alias((AbstractClientPlayer) (Object) this);
        if (!alias.isEmpty()) callback.setReturnValue(ClientPlayerAppearance.of(alias).model());
    }
}
