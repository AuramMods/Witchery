package art.arcane.witchery.mixin;

import art.arcane.witchery.item.HunterClothingItem;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public abstract class HunterArmorProtectionMixin {
    @Redirect(method = "getDamageAfterArmorAbsorb", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/damagesource/CombatRules;getDamageAfterAbsorb(FFF)F"))
    private float witchery$reinforceHunterArmor(float incoming, float armor, float toughness, DamageSource source, float damage) {
        LivingEntity wearer = (LivingEntity) (Object) this;
        int reinforcedDefense = 0;
        for (ItemStack stack : wearer.getArmorSlots()) {
            if (stack.getItem() instanceof HunterClothingItem clothing && clothing.reinforcedAgainst(source)) {
                reinforcedDefense += clothing.getDefense();
            }
        }
        return CombatRules.getDamageAfterAbsorb(incoming, armor + reinforcedDefense * 1.5F, toughness);
    }
}
