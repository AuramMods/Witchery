package art.arcane.witchery.mixin;

import art.arcane.witchery.brewing.DeathBrewEffects;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Player.class)
public abstract class KeepInventoryBrewMixin {
    @Redirect(method = "dropEquipment", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/player/Inventory;dropAll()V"))
    private void witchery$retainBrewInventory(Inventory inventory) {
        if (!DeathBrewEffects.retainInventory(inventory.player)) inventory.dropAll();
    }
}
