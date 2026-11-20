package art.arcane.witchery.item;

import art.arcane.witchery.registry.WitcheryItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDamageEvent;

public final class DeathProtectionPoppetItem extends BoundPoppetItem {
    public static void protect(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !player.isAlive() || event.getAmount() < player.getHealth()
                || event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
        ItemStack poppet = inInventory(player, WitcheryItems.getRequired("death_protection_poppet").get());
        if (poppet.isEmpty()) return;
        wear(poppet, USE_LIMIT);
        player.getInventory().setChanged();
        event.setCanceled(true);
        if (player.getHealth() < 10) player.setHealth(10);
        player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1, 1);
    }
}
