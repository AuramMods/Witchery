package art.arcane.witchery.item;

import art.arcane.witchery.registry.WitcheryItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraftforge.event.entity.living.LivingDamageEvent;

public final class DeathProtectionPoppetItem extends BoundPoppetItem {
    public static void protect(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !player.isAlive() || event.getAmount() < player.getHealth()
                || event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
        DamageSource source = event.getSource();
        if ((source.is(DamageTypeTags.IS_FIRE) || source.is(DamageTypeTags.IS_EXPLOSION))
                && protectWith(player, event, "fire_protection_poppet")) {
            player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 60));
            return;
        }
        protectWith(player, event, "death_protection_poppet");
    }

    private static boolean protectWith(ServerPlayer player, LivingDamageEvent event, String item) {
        if (!useWard(player, WitcheryItems.getRequired(item).get(), USE_LIMIT)) return false;
        event.setCanceled(true);
        if (player.getHealth() < 10) player.setHealth(10);
        player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1, 1);
        return true;
    }
}
