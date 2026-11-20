package art.arcane.witchery.item;

import art.arcane.witchery.lycanthropy.Lycanthropy;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public final class MoonCharmItem extends Item {
    public MoonCharmItem() {
        super(new Properties().durability(49).rarity(Rarity.RARE));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.BOW; }

    @Override
    public int getUseDuration(ItemStack stack) { return 60; }

    @Override
    public void onUseTick(Level level, LivingEntity user, ItemStack stack, int remaining) {
        if (!(user instanceof ServerPlayer player)) return;
        int rank = Lycanthropy.rank(player);
        if (remaining != Math.max((rank - 1) * 4, 1)) return;
        if (!Lycanthropy.useMoonCharm(player)) {
            player.playNotifySound(SoundEvents.NOTE_BLOCK_PLING.value(), SoundSource.PLAYERS, 1, 1);
        }
        stack.hurtAndBreak(1, player, wearer -> wearer.broadcastBreakEvent(player.getUsedItemHand()));
    }

    @Override
    public boolean isValidRepairItem(ItemStack charm, ItemStack repair) { return repair.is(Items.GOLD_INGOT); }
}
