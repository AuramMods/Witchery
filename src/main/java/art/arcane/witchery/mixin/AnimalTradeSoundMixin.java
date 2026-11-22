package art.arcane.witchery.mixin;

import art.arcane.witchery.entity.AnimalMerchant;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.Merchant;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MerchantMenu.class)
abstract class AnimalTradeSoundMixin {
    @Shadow @Final private Merchant trader;

    @Inject(method = "playTradeSound", at = @At("HEAD"), cancellable = true)
    private void witchery$useAnimalVoice(CallbackInfo callback) {
        // The animal merchant plays its voice from notifyTrade. Vanilla expects its trader to be an Entity here.
        if (trader instanceof AnimalMerchant) callback.cancel();
    }
}
