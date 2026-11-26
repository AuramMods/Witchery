package art.arcane.witchery.mixin;

import art.arcane.witchery.world.migration.StoredMerchantOffers;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.trading.MerchantOffers;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Villager.class)
abstract class LegacyVillagerTradesMixin {
    @Redirect(method = "readAdditionalSaveData", at = @At(value = "NEW",
            target = "(Lnet/minecraft/nbt/CompoundTag;)Lnet/minecraft/world/item/trading/MerchantOffers;"))
    private MerchantOffers witchery$readTrades(CompoundTag saved) {
        var recovered = StoredMerchantOffers.read(saved);
        return recovered.hasPending() ? recovered : new MerchantOffers(saved);
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void witchery$savePendingTrades(CompoundTag tag, CallbackInfo info) {
        // Vanilla only writes Offers when there are active recipes.
        var villager = (Villager) (Object) this;
        if (villager.getOffers() instanceof StoredMerchantOffers saved && saved.isEmpty() && saved.hasPending()) {
            tag.put("Offers", saved.createTag());
        }
    }
}
