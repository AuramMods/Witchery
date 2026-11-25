package art.arcane.witchery.mixin;

import art.arcane.witchery.menu.AltarMenu;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "art.arcane.adapt.mod.event.inventory.ContainerHooks", remap = false)
abstract class AdaptAltarMenuMixin {
    @Inject(method = "opening(Lnet/minecraft/server/level/ServerPlayer;Lnet/minecraft/world/inventory/AbstractContainerMenu;)Z",
            at = @At("HEAD"), cancellable = true, require = 0)
    private static void witchery$allowAltarDisplay(ServerPlayer player, AbstractContainerMenu menu,
                                                  CallbackInfoReturnable<Boolean> result) {
        // The Altar displays power and has no inventory to supply to Adapt's event.
        if (menu instanceof AltarMenu) result.setReturnValue(true);
    }
}
