package art.arcane.witchery.mixin;

import art.arcane.witchery.entity.ScryEye;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

// Allow optional redirects to inspect the method when a default-priority renderer replaces it.
@Mixin(value = LevelRenderer.class, priority = 1100)
public abstract class ScryingWorldRendererMixin {
    @Shadow @Final private Minecraft minecraft;

    // Alternate terrain renderers may replace this method and already use the camera origin.
    @Redirect(method = "setupRender", require = 0,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getX()D"))
    private double witchery$scryingRenderX(LocalPlayer player) {
        return witchery$renderOrigin(player).getX();
    }

    @Redirect(method = "setupRender", require = 0,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getY()D"))
    private double witchery$scryingRenderY(LocalPlayer player) {
        return witchery$renderOrigin(player).getY();
    }

    @Redirect(method = "setupRender", require = 0,
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;getZ()D"))
    private double witchery$scryingRenderZ(LocalPlayer player) {
        return witchery$renderOrigin(player).getZ();
    }

    @Unique
    private Entity witchery$renderOrigin(LocalPlayer player) {
        return minecraft.getCameraEntity() instanceof ScryEye eye ? eye : player;
    }
}
