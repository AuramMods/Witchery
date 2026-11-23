package art.arcane.witchery.mixin;

import art.arcane.witchery.item.MutatingSprigItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "org.violetmoon.quark.content.tweaks.module.SimpleHarvestModule", remap = false)
abstract class QuarkSimpleHarvestMixin {
    @Inject(method = "tryHarvestOrClickCrop", at = @At("HEAD"), cancellable = true, require = 0)
    private static void witchery$allowWormwoodMutation(Level level, BlockPos pos, LivingEntity entity,
                                                      InteractionHand hand, boolean allowRightClick,
                                                      CallbackInfoReturnable<Boolean> result) {
        if (entity instanceof Player player && hand != null
                && player.getItemInHand(hand).getItem() instanceof MutatingSprigItem
                && MutatingSprigItem.canMutateWormwood(level, pos)) {
            result.setReturnValue(false);
        }
    }
}
