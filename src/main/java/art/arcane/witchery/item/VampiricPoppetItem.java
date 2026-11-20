package art.arcane.witchery.item;

import art.arcane.witchery.entity.WitchHunterPursuit;
import art.arcane.witchery.registry.WitcheryItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public final class VampiricPoppetItem extends BoundPoppetItem {
    private static final int TRANSFER_WEAR = 66;
    private static final ThreadLocal<Set<UUID>> TRANSFERRING = ThreadLocal.withInitial(HashSet::new);

    public VampiricPoppetItem() {
        super(Rarity.RARE);
    }

    public static void transferDamage(ServerPlayer player, LivingHurtEvent event) {
        if (event.getSource().getEntity() == null) return;
        Set<UUID> chain = TRANSFERRING.get();
        if (!chain.add(player.getUUID())) return;
        try {
            useWard(player, WitcheryItems.getRequired("vampiric_poppet").get(), TRANSFER_WEAR,
                    stack -> TaglockItem.isBound(stack, 2), stack -> transfer(player, event, stack, chain));
        } finally {
            chain.remove(player.getUUID());
            if (chain.isEmpty()) TRANSFERRING.remove();
        }
    }

    private static void transfer(ServerPlayer player, LivingHurtEvent event, ItemStack stack, Set<UUID> chain) {
        WitchHunterPursuit.noticeBlackMagic(player);
        LivingEntity target = TaglockItem.resolveTarget(player.serverLevel(), stack, 2);
        if (target == null || !target.isAlive() || chain.contains(target.getUUID())
                || HunterClothingItem.fullSet(target, HunterClothingItem.Upgrade.PLAIN) || VoodooPoppetItem.warded(player, target, stack)) return;
        float amount = target instanceof ServerPlayer ? event.getAmount() : Math.min(event.getAmount(), 15);
        target.hurt(event.getSource(), amount);
        if (!(target instanceof ServerPlayer) && !target.isAlive()) TaglockItem.clearBinding(stack, 2);
        event.setCanceled(true);
    }

    @Override
    public Component getName(ItemStack stack) {
        String first = TaglockItem.boundName(stack);
        String second = TaglockItem.boundName(stack, 2);
        return first.isEmpty() && second.isEmpty() ? super.getName(stack)
                : Component.translatable("item.witchery.vampiric_poppet.named", super.getName(stack), name(first), name(second));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flags) {
        String first = TaglockItem.boundName(stack);
        String second = TaglockItem.boundName(stack, 2);
        if (first.isEmpty() && second.isEmpty()) super.appendHoverText(stack, level, tooltip, flags);
        else tooltip.add(Component.translatable("item.witchery.vampiric_poppet.bound", name(first), name(second))
                .withStyle(ChatFormatting.GRAY));
    }

    private static String name(String name) {
        return name.isEmpty() ? "??" : name;
    }
}
