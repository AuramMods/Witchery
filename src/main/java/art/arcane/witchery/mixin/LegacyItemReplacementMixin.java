package art.arcane.witchery.mixin;

import art.arcane.witchery.event.WitcheryBlockPlacement;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.BlockSnapshot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = ForgeHooks.class, remap = false)
abstract class LegacyItemReplacementMixin {
    @Redirect(method = "onPlaceItemIntoWorld", at = @At(value = "INVOKE",
            target = "Lnet/minecraftforge/common/util/BlockSnapshot;getFlag()I"))
    private static int witchery$finishApprovedReplacement(BlockSnapshot snapshot) {
        // Forge reaches the notification loop only after approving the captured placement.
        WitcheryBlockPlacement.finishItemReplacement(snapshot);
        return snapshot.getFlag();
    }
}
