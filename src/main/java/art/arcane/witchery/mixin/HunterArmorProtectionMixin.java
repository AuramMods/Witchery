package art.arcane.witchery.mixin;

import art.arcane.witchery.item.HunterClothingItem;
import art.arcane.witchery.registry.WitcheryDamageTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Adjust the input before Apothic Attributes redirects the armor calculation.
@Mixin(value = LivingEntity.class, priority = 1100)
public abstract class HunterArmorProtectionMixin {
    @Unique private float witchery$hunterArmorBonus;

    @Inject(method = "getDamageAfterArmorAbsorb", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/LivingEntity;getArmorValue()I"))
    private void witchery$checkHunterArmor(DamageSource source, float damage, CallbackInfoReturnable<Float> info) {
        LivingEntity wearer = (LivingEntity) (Object) this;
        int reinforcedDefense = 0;
        boolean silverRetaliation = false;
        for (ItemStack stack : wearer.getArmorSlots()) {
            if (stack.getItem() instanceof HunterClothingItem clothing && clothing.reinforcedAgainst(source)) {
                reinforcedDefense += clothing.getDefense();
                silverRetaliation |= clothing.silverProtection(source);
            }
        }
        if (silverRetaliation && !wearer.level().isClientSide && source.getEntity() instanceof LivingEntity attacker && attacker != wearer) {
            attacker.hurt(WitcheryDamageTypes.directFire(attacker.level()), 1);
        }
        witchery$hunterArmorBonus = reinforcedDefense * 1.5F;
    }

    @ModifyArg(method = "getDamageAfterArmorAbsorb", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/damagesource/CombatRules;getDamageAfterAbsorb(FFF)F"), index = 1)
    private float witchery$reinforceHunterArmor(float armor) {
        float reinforcedArmor = armor + witchery$hunterArmorBonus;
        witchery$hunterArmorBonus = 0;
        return reinforcedArmor;
    }
}
