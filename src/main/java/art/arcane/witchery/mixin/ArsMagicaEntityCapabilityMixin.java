package art.arcane.witchery.mixin;

import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = {
        "com.github.minecraftschurlimods.arsmagicalegacy.common.magic.ManaHelper",
        "com.github.minecraftschurlimods.arsmagicalegacy.common.magic.BurnoutHelper"
}, remap = false)
abstract class ArsMagicaEntityCapabilityMixin {
    @Redirect(method = {"getManaHolder", "getBurnoutHolder"},
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;invalidateCaps()V"),
            require = 0)
    private void witchery$restoreInvalidationForRemovedEntity(LivingEntity entity) {
        if (entity.isRemoved()) entity.invalidateCaps();
    }
}
