package art.arcane.witchery.mixin;

import art.arcane.witchery.entity.GrenadeProjectile;
import art.arcane.witchery.entity.Hellhound;
import art.arcane.witchery.entity.ThrownBrew;
import art.arcane.witchery.entity.WingedMonkey;
import art.arcane.witchery.entity.WitcheryFollower;
import art.arcane.witchery.entity.WitchOwl;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "toni.doesittick.DoesItTick", remap = false)
abstract class DoesItTickActiveEntityMixin {
    @Inject(method = "isTickable(Lnet/minecraft/world/entity/Entity;)Z",
            at = @At("HEAD"), cancellable = true, require = 0)
    private static void witchery$keepActiveEntitiesTicking(Entity entity, CallbackInfoReturnable<Boolean> result) {
        if (entity.level().isClientSide()) return;
        if ((entity instanceof WitchOwl owl && owl.hasActiveDelivery())
                || (entity instanceof WingedMonkey monkey && monkey.hasActiveFlight())
                || (entity instanceof Hellhound hound && hound.isConverting())
                || entity instanceof GrenadeProjectile
                || entity instanceof ThrownBrew
                || (entity instanceof WitcheryFollower follower && follower.isTemporaryDuplicate())) {
            result.setReturnValue(true);
        }
    }
}
