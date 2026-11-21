package art.arcane.witchery.brewing;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.infusion.InfusionEnergy;
import art.arcane.witchery.registry.WitcheryEffects;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Witchery.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class CombatBrewEffects {
    private static final TagKey<DamageType> ABSORBABLE_MAGIC = TagKey.create(Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "absorbable_magic"));

    private CombatBrewEffects() {}

    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event) {
        LivingEntity target = event.getEntity();
        if (!(target.level() instanceof ServerLevel) || event.getAmount() <= 0) return;
        DamageSource source = event.getSource();
        MobEffectInstance absorb = target.getEffect(WitcheryEffects.ABSORB_MAGIC.get());
        if (absorb != null && source.is(ABSORBABLE_MAGIC)) {
            float absorbed = Math.min(event.getAmount(), event.getAmount() * 0.2F * (absorb.getAmplifier() + 1));
            event.setAmount(event.getAmount() - absorbed);
            if (absorbed > 1 && target instanceof ServerPlayer player) InfusionEnergy.recharge(player, (int) absorbed);
        }
        if (!(source.getEntity() instanceof LivingEntity attacker)) return;
        MobEffectInstance poison = attacker.getEffect(WitcheryEffects.POISON_WEAPONS.get());
        if (poison != null && isMelee(source)) applyWeaponPoison(target, attacker, poison.getAmplifier());
        if (attacker != target) {
            boolean projectile = source.is(DamageTypeTags.IS_PROJECTILE);
            MobEffectInstance reflect = target.getEffect(WitcheryEffects.REFLECT_DAMAGE.get());
            if (reflect != null && (!projectile || reflect.getAmplifier() >= 2) && event.getAmount() > 0) {
                float reflected = (float) Math.ceil(event.getAmount() * 0.1F * (reflect.getAmplifier() + (projectile ? 0 : 1)));
                // Preserve the original damage source. Its attacker is the target of the return hit.
                attacker.hurt(source, reflected);
                event.setAmount(Math.max(0, event.getAmount() - reflected));
            }
            MobEffectInstance repel = target.getEffect(WitcheryEffects.REPEL_ATTACKER.get());
            if (repel != null && !projectile && attacker.distanceToSqr(target) < 9) repelAttacker(attacker, target, repel.getAmplifier());
        }
        MobEffectInstance moonshine = target.getEffect(WitcheryEffects.FEEL_NO_PAIN.get());
        if (moonshine != null && target instanceof ServerPlayer player) spendHunger(player, event, moonshine.getAmplifier());
    }

    static boolean isMelee(DamageSource source) {
        return source.getDirectEntity() == source.getEntity()
                && (source.is(DamageTypes.PLAYER_ATTACK) || source.is(DamageTypes.MOB_ATTACK)
                || source.is(DamageTypes.MOB_ATTACK_NO_AGGRO));
    }

    private static void applyWeaponPoison(LivingEntity target, LivingEntity attacker, int amplifier) {
        MobEffectInstance effect = switch (amplifier) {
            case 0 -> new MobEffectInstance(MobEffects.POISON, 100);
            case 1 -> new MobEffectInstance(MobEffects.POISON, 100, 1);
            case 2 -> new MobEffectInstance(MobEffects.POISON, 300, 1);
            default -> new MobEffectInstance(MobEffects.WITHER, 400);
        };
        target.addEffect(effect, attacker);
    }

    private static void repelAttacker(LivingEntity attacker, LivingEntity target, int amplifier) {
        Vec3 away = attacker.position().subtract(target.position()).normalize();
        double speed = 1 + 0.75 * amplifier;
        attacker.setDeltaMovement(away.x * speed, Math.max(away.y, 0.5 + 0.2 * amplifier), away.z * speed);
        attacker.hurtMarked = true;
    }

    private static void spendHunger(ServerPlayer player, LivingHurtEvent event, int amplifier) {
        var food = player.getFoodData();
        if (food.getFoodLevel() <= 0) return;
        float damage = damageAfterProtection(player, event.getSource(), event.getAmount());
        float penalty = amplifier > 0 ? Math.max(damage / amplifier, amplifier == 1 ? 2 : 1) : Math.max(damage * 2, 3);
        food.eat(-Math.min(food.getFoodLevel(), (int) Math.ceil(penalty)), 2);
        food.setSaturation(Math.max(0, food.getSaturationLevel()));
        event.setCanceled(true);
    }

    private static float damageAfterProtection(ServerPlayer player, DamageSource source, float damage) {
        if (!source.is(DamageTypeTags.BYPASSES_ARMOR)) {
            damage = CombatRules.getDamageAfterAbsorb(damage, player.getArmorValue(),
                    (float) player.getAttributeValue(Attributes.ARMOR_TOUGHNESS));
        }
        if (source.is(DamageTypeTags.BYPASSES_EFFECTS)) return damage;
        MobEffectInstance resistance = player.getEffect(MobEffects.DAMAGE_RESISTANCE);
        if (resistance != null && !source.is(DamageTypeTags.BYPASSES_RESISTANCE)) {
            damage = Math.max(damage * (1 - 0.2F * (resistance.getAmplifier() + 1)), 0);
        }
        if (source.is(DamageTypeTags.BYPASSES_ENCHANTMENTS)) return damage;
        return CombatRules.getDamageAfterMagicAbsorb(damage, EnchantmentHelper.getDamageProtection(player.getArmorSlots(), source));
    }
}
