package art.arcane.witchery.mixin;

import art.arcane.witchery.block.WitchCrop;
import art.arcane.witchery.item.MindrakeBulbs;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemEntity.class)
abstract class MindrakeBulbMergeMixin {
    @Inject(method = "tryToMerge", at = @At("HEAD"), cancellable = true)
    private void witchery$keepBulbOwners(ItemEntity other, CallbackInfo info) {
        ItemEntity drop = (ItemEntity) (Object) this;
        if (drop.getItem().is(WitchCrop.MINDRAKE.seed()) && !MindrakeBulbs.sameThrower(drop, other)) info.cancel();
    }
}
