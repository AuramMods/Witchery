package art.arcane.witchery.mixin;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Pseudo
@Mixin(targets = "com.leclowndu93150.thaumaturge.content.entity.trait.MobTraitEngine", remap = false)
abstract class ThaumaturgeRemovedMobTraitsMixin {
    @Inject(method = "traits(Lnet/minecraft/world/entity/LivingEntity;)Ljava/util/List;",
            at = @At("HEAD"), cancellable = true, require = 0)
    private static void witchery$skipRemovedEntityTraits(LivingEntity entity, CallbackInfoReturnable<List<?>> cir) {
        // An open inventory can render the player briefly after Forge invalidates their capabilities.
        if (entity.isRemoved()) cir.setReturnValue(List.of());
    }
}
