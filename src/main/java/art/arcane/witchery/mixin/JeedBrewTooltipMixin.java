package art.arcane.witchery.mixin;

import art.arcane.witchery.client.BrewEffectLabels;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import java.util.ArrayList;
import java.util.List;

@Pseudo
@Mixin(targets = "net.mehvahdjukaar.jeed.common.ScreenExtensionsHandler", remap = false)
abstract class JeedBrewTooltipMixin {
    @ModifyVariable(method = "renderEffectTooltip", at = @At("STORE"), ordinal = 0, require = 0)
    private static List<Component> witchery$showBrewLabel(List<Component> tooltip, MobEffectInstance effect, Screen screen) {
        if (!(screen instanceof EffectRenderingInventoryScreen<?>) || tooltip.isEmpty()) return tooltip;
        var label = BrewEffectLabels.inventoryLabel(effect);
        if (label == null) return tooltip;
        var updated = new ArrayList<>(tooltip);
        updated.set(0, label.copy().setStyle(tooltip.get(0).getStyle()));
        return updated;
    }
}
