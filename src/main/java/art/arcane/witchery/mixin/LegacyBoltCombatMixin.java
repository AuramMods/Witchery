package art.arcane.witchery.mixin;

import art.arcane.witchery.entity.WitcheryBolt;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.EntityHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractArrow.class)
abstract class LegacyBoltCombatMixin {
    @Redirect(method = "onHitEntity", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private boolean witchery$applyHolyBonusBeforeDamage(Entity entity, DamageSource source, float amount) {
        if ((Object) this instanceof WitcheryBolt bolt && entity instanceof LivingEntity target) {
            amount = bolt.hitDamage(target, amount);
        }
        return entity.hurt(source, amount);
    }

    @Redirect(method = "onHitEntity", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/Entity;setRemainingFireTicks(I)V"))
    private void witchery$keepIgnitionOnRejectedHits(Entity entity, int previousFire) {
        if (!((Object) this instanceof WitcheryBolt)) entity.setRemainingFireTicks(previousFire);
    }

    @ModifyConstant(method = "onHitEntity", constant = @Constant(doubleValue = 1.0E-7D))
    private double witchery$keepSlowRicochetsInFlight(double threshold) {
        return (Object) this instanceof WitcheryBolt ? 0 : threshold;
    }

    @Inject(method = "onHitEntity", at = @At("RETURN"), slice = @Slice(
            from = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"),
            to = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setArrowCount(I)V")))
    private void witchery$completeAcceptedEndermanHit(EntityHitResult hit, CallbackInfo callback) {
        if ((Object) this instanceof WitcheryBolt bolt && hit.getEntity() instanceof LivingEntity target) {
            bolt.completeAcceptedEndermanHit(target);
        }
    }
}
