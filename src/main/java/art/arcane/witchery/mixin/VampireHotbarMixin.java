package art.arcane.witchery.mixin;

import art.arcane.witchery.vampirism.VampireAbilities;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Gui.class)
public abstract class VampireHotbarMixin {
    // The second widgets blit draws the selected inventory slot's outline.
    @Redirect(method = "renderHotbar", at = @At(value = "INVOKE", ordinal = 1,
            target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIII)V"))
    private void witchery$renderInventorySelection(GuiGraphics graphics, ResourceLocation texture,
                                                  int x, int y, int u, int v, int width, int height) {
        Minecraft client = Minecraft.getInstance();
        var player = client.player;
        if (player == null || client.getCameraEntity() != player || VampireAbilities.selected(player) == 0) {
            graphics.blit(texture, x, y, u, v, width, height);
        }
    }
}
