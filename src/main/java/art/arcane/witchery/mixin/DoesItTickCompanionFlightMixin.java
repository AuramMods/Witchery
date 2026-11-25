package art.arcane.witchery.mixin;

import art.arcane.witchery.entity.WingedMonkey;
import art.arcane.witchery.entity.WitchOwl;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "toni.doesittick.DoesItTick", remap = false)
abstract class DoesItTickCompanionFlightMixin {
    @Inject(method = "isTickable(Lnet/minecraft/world/entity/Entity;)Z",
            at = @At("HEAD"), cancellable = true, require = 0)
    private static void witchery$keepCompanionFlightsTicking(Entity entity, CallbackInfoReturnable<Boolean> result) {
        if (entity.level().isClientSide()) return;
        // Active companion flights keep their own chunks ticking through their flight tickets.
        if ((entity instanceof WitchOwl owl && owl.hasActiveDelivery())
                || (entity instanceof WingedMonkey monkey && monkey.hasActiveFlight())) {
            result.setReturnValue(true);
        }
    }
}
