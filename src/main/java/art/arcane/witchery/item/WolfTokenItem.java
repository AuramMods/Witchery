package art.arcane.witchery.item;

import art.arcane.witchery.lycanthropy.Lycanthropy;
import art.arcane.witchery.vampirism.Vampirism;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class WolfTokenItem extends Item {
    public WolfTokenItem() {
        super(new Properties().stacksTo(1).rarity(Rarity.EPIC));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 20;
    }

    @Override
    public void onUseTick(Level level, LivingEntity user, ItemStack stack, int remaining) {
        if (remaining != 1 || !(user instanceof ServerPlayer player)) return;
        MutableComponent message;
        if (player.isShiftKeyDown()) {
            int rank = Vampirism.cycleRank(player);
            message = Component.translatable("message.witchery.vampire.set_level", rank);
        } else {
            Lycanthropy.setRank(player, (Lycanthropy.rank(player) + 1) % 11);
            message = Component.translatable("message.witchery.werewolf.set_level", Lycanthropy.rank(player));
        }
        player.displayClientMessage(message.withStyle(ChatFormatting.GREEN), false);
        var server = player.serverLevel();
        server.sendParticles(ParticleTypes.POOF, player.getX(), player.getY() + 0.75, player.getZ(),
                30, 0.75, 0.75, 0.75, 0.02);
        server.playSound(null, player, SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1, 1);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flags) {
        tooltip.add(Component.translatable("item.witchery.wolftoken.creative").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.witchery.wolftoken.werewolf").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("item.witchery.wolftoken.vampire").withStyle(ChatFormatting.GRAY));
    }
}
