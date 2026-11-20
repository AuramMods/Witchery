package art.arcane.witchery.item;

import art.arcane.witchery.entity.WitcheryBolt;
import art.arcane.witchery.registry.WitcheryItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.event.entity.living.LivingDamageEvent;

public final class DeathProtectionPoppetItem extends BoundPoppetItem {
    public DeathProtectionPoppetItem() {
        super(Rarity.RARE);
    }

    public static void protect(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !player.isAlive()
                || event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
        DamageSource source = event.getSource();
        float remaining = player.getHealth() - event.getAmount();
        if (remaining > 0) {
            if (remaining <= 2 && source.is(DamageTypes.STARVE)) {
                protectWithEffect(player, event, "hunger_protection_poppet", MobEffects.SATURATION);
            }
            return;
        }
        boolean fall = source.is(DamageTypeTags.IS_FALL) || source.is(DamageTypes.FALLING_BLOCK);
        boolean fire = source.is(DamageTypeTags.IS_FIRE) || source.is(DamageTypeTags.IS_EXPLOSION);
        if (fall && protectWith(player, event, "earth_protection_poppet", false)) return;
        if (fire && protectWithEffect(player, event, "fire_protection_poppet", MobEffects.FIRE_RESISTANCE)) return;
        if (source.is(DamageTypes.DROWN) && protectWithEffect(player, event, "water_protection_poppet", MobEffects.WATER_BREATHING)) return;
        if (source.is(DamageTypes.STARVE) && protectWithEffect(player, event, "hunger_protection_poppet", MobEffects.SATURATION)) return;
        if (!WitcheryBolt.poweredAntiMagic(source) && protectWith(player, event, "death_protection_poppet", true)) {
            if (fire || player.isOnFire()) effect(player, MobEffects.FIRE_RESISTANCE, 120);
            else if (source.is(DamageTypes.DROWN)) effect(player, MobEffects.WATER_BREATHING, 120);
            else if (source.is(DamageTypes.STARVE)) effect(player, MobEffects.SATURATION, 120);
        }
    }

    private static boolean protectWithEffect(ServerPlayer player, LivingDamageEvent event, String item, MobEffect effect) {
        if (!protectWith(player, event, item, true)) return false;
        effect(player, effect, 60);
        return true;
    }

    private static boolean protectWith(ServerPlayer player, LivingDamageEvent event, String item, boolean heal) {
        if (!useWard(player, WitcheryItems.getRequired(item).get(), USE_LIMIT)) return false;
        event.setCanceled(true);
        if (heal && player.getHealth() < 10) player.setHealth(10);
        player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1, 1);
        return true;
    }

    private static void effect(ServerPlayer player, MobEffect effect, int duration) {
        player.addEffect(new MobEffectInstance(effect, duration));
    }

    public static void repairHeldTool(ServerPlayer player) {
        repair(player, player.getMainHandItem(), "tool_protection_poppet");
    }

    public static void repairArmor(ServerPlayer player) {
        for (ItemStack armor : player.getArmorSlots()) repair(player, armor, "armor_protection_poppet");
    }

    private static void repair(ServerPlayer player, ItemStack stack, String ward) {
        if (!stack.isDamageableItem() || stack.getDamageValue() < (int) (stack.getMaxDamage() * 0.9F)) return;
        if (useWard(player, WitcheryItems.getRequired(ward).get(), USE_LIMIT)) {
            stack.setDamageValue(0);
            player.getInventory().setChanged();
            player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 0.5F, 1);
        }
    }
}
