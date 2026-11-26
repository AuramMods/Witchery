package art.arcane.witchery.mixin;

import art.arcane.witchery.brewing.BrewMixture;
import art.arcane.witchery.entity.ThrownBrew;
import art.arcane.witchery.item.BrewBottleItem;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ProjectileUtil.class)
abstract class TriggeredBrewHitShapeMixin {
    @ModifyArg(method = "getHitResult", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/ClipContext;<init>(Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/phys/Vec3;Lnet/minecraft/world/level/ClipContext$Block;Lnet/minecraft/world/level/ClipContext$Fluid;Lnet/minecraft/world/entity/Entity;)V"), index = 2)
    private static ClipContext.Block witchery$traceTriggeredBrewOutlines(Vec3 start, Vec3 end, ClipContext.Block shape,
                                                                        ClipContext.Fluid fluid, Entity projectile) {
        if (projectile instanceof ThrownBrew brew && brew.getItem().getItem() instanceof BrewBottleItem) {
            BrewMixture mixture = BrewMixture.from(brew.getItem().getTag());
            if (mixture != null && mixture.impactDelivery() == BrewMixture.Delivery.TRIGGERED) {
                return ClipContext.Block.OUTLINE;
            }
        }
        return shape;
    }
}
