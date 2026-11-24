package art.arcane.witchery.mixin;

import art.arcane.witchery.config.WitcheryCommonConfig;
import art.arcane.witchery.integration.ThaumaturgeAspectVariants;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "com.leclowndu93150.thaumaturge.content.aspect.AspectIndex", remap = false)
abstract class ThaumaturgeStackAspectsMixin {
    @Inject(method = "of(Lnet/minecraft/world/item/ItemStack;)Lcom/leclowndu93150/thaumaturge/api/aspect/AspectList;",
            at = @At("RETURN"), cancellable = true)
    private void witchery$aspectVariant(ItemStack stack, CallbackInfoReturnable<Object> callback) {
        if (!WitcheryCommonConfig.ALLOW_MOD_INTEGRATION.get()) return;
        var aspects = ThaumaturgeAspectVariants.lookup(stack);
        if (aspects != null) callback.setReturnValue(aspects);
    }
}
