package art.arcane.witchery.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class DeathProtectionPoppetItem extends Item {
    public DeathProtectionPoppetItem() {
        super(new Properties().stacksTo(1));
    }

    public static void protect(LivingDamageEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !player.isAlive() || event.getAmount() < player.getHealth()
                || event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack poppet = inventory.getItem(slot);
            if (poppet.getItem() instanceof DeathProtectionPoppetItem && TaglockItem.isBoundTo(poppet, player)) {
                inventory.removeItem(slot, 1);
                event.setCanceled(true);
                if (player.getHealth() < 10) player.setHealth(10);
                player.playSound(SoundEvents.EXPERIENCE_ORB_PICKUP, 1, 1);
                break;
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flags) {
        String name = TaglockItem.boundName(stack);
        Component status = name.isEmpty() ? Component.translatable("item.witchery.death_protection_poppet.unbound")
                : Component.translatable("item.witchery.death_protection_poppet.bound", name);
        tooltip.add(status.copy().withStyle(ChatFormatting.GRAY));
    }
}
