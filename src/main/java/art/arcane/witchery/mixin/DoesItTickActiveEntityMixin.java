package art.arcane.witchery.mixin;

import art.arcane.witchery.entity.BrewDroplet;
import art.arcane.witchery.entity.DarkMark;
import art.arcane.witchery.entity.DreamSpirit;
import art.arcane.witchery.entity.GrenadeProjectile;
import art.arcane.witchery.entity.Hellhound;
import art.arcane.witchery.entity.HobgoblinGod;
import art.arcane.witchery.entity.HornedHuntsman;
import art.arcane.witchery.entity.Illusion;
import art.arcane.witchery.entity.InfernoSplatter;
import art.arcane.witchery.entity.Leonard;
import art.arcane.witchery.entity.LostSoul;
import art.arcane.witchery.entity.MirrorFace;
import art.arcane.witchery.entity.SymbolProjectile;
import art.arcane.witchery.entity.ThrownBrew;
import art.arcane.witchery.entity.WingedMonkey;
import art.arcane.witchery.entity.WitcheryBolt;
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
                || (entity instanceof HornedHuntsman huntsman && huntsman.hasActiveEntrance())
                || (entity instanceof HobgoblinGod god && god.hasActiveEntrance())
                || (entity instanceof Leonard leonard && leonard.hasActiveEntrance())
                || (entity instanceof DreamSpirit spirit && spirit.hasTimedLife())
                || (entity instanceof LostSoul soul && soul.hasTimedLife())
                || entity instanceof DarkMark
                || entity instanceof Illusion
                || entity instanceof MirrorFace
                || (entity instanceof WitcheryBolt bolt && bolt.canResolveLegacyBoundaryAnchor())
                || entity instanceof GrenadeProjectile
                || entity instanceof SymbolProjectile
                || entity instanceof ThrownBrew
                || entity instanceof BrewDroplet
                || entity instanceof InfernoSplatter
                || (entity instanceof WitcheryFollower follower && follower.isTemporaryDuplicate())) {
            result.setReturnValue(true);
        }
    }
}
