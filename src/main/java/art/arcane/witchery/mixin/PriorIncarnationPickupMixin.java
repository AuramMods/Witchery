package art.arcane.witchery.mixin;

import art.arcane.witchery.ritual.PriorIncarnation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ItemEntity.class)
public abstract class PriorIncarnationPickupMixin {
    @Redirect(method = "playerTouch", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Inventory;add(Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean witchery$pickUpDeathDrop(Inventory inventory, ItemStack stack) {
        return PriorIncarnation.pickUp(inventory, stack);
    }
}
