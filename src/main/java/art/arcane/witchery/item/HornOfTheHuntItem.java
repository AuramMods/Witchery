package art.arcane.witchery.item;

import art.arcane.witchery.entity.HornedHuntsman;
import art.arcane.witchery.registry.WitcherySounds;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public final class HornOfTheHuntItem extends Item {
    public HornOfTheHuntItem() {
        super(new Properties().durability(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) { return UseAnim.BOW; }

    @Override
    public int getUseDuration(ItemStack stack) { return 40; }

    @Override
    public void onUseTick(Level level, LivingEntity user, ItemStack stack, int remaining) {
        if (remaining != 1 || !(user instanceof ServerPlayer caller)) return;
        level.playSound(null, caller, WitcherySounds.HUNT_HORN.get(), SoundSource.PLAYERS, 1, 1);
        if (level.getDifficulty() != Difficulty.PEACEFUL && HornedHuntsman.summon(caller)) {
            stack.hurtAndBreak(2, caller, player -> player.broadcastBreakEvent(player.getUsedItemHand()));
        } else {
            caller.displayClientMessage(Component.translatable("message.witchery.horn.no_huntsman"), true);
        }
    }
}
