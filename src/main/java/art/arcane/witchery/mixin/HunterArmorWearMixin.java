package art.arcane.witchery.mixin;

import art.arcane.witchery.item.HunterClothingItem;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.function.Consumer;

@Mixin(Inventory.class)
public abstract class HunterArmorWearMixin {
    @Redirect(method = "hurtArmor", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;hurtAndBreak(ILnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V"))
    private <T extends LivingEntity> void witchery$preventHunterArmorWear(ItemStack stack, int wear, T wearer,
                                                                        Consumer<T> onBreak, DamageSource source, float damage, int[] slots) {
        if (!(stack.getItem() instanceof HunterClothingItem clothing) || !clothing.reinforcedAgainst(source)) {
            stack.hurtAndBreak(wear, wearer, onBreak);
        }
    }
}
