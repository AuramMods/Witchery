package art.arcane.witchery.mixin;

import art.arcane.witchery.item.MutatingSprigItem;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = "io.github.jamalam360.rightclickharvest.RightClickHarvest", remap = false)
abstract class RightClickHarvestMixin {
    @Inject(method = "onBlockUse", at = @At("HEAD"), cancellable = true, require = 0)
    private static void witchery$allowWormwoodMutation(Player player, Level level, InteractionHand hand,
                                                      BlockHitResult hit, boolean simulate,
                                                      CallbackInfoReturnable<InteractionResult> result) {
        if (player.getItemInHand(hand).getItem() instanceof MutatingSprigItem
                && MutatingSprigItem.canMutateWormwood(level, hit.getBlockPos())) {
            result.setReturnValue(InteractionResult.PASS);
        }
    }
}
