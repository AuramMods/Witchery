package art.arcane.witchery.item;

import art.arcane.witchery.entity.AnimalMerchant;
import art.arcane.witchery.infusion.OtherwhereInfusion;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public final class PolynesiaCharmItem extends Item {
    public PolynesiaCharmItem() {
        super(new Properties().durability(50).rarity(Rarity.UNCOMMON));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (player instanceof ServerPlayer server) {
            var hit = OtherwhereInfusion.rayTrace(server, 5, target -> true);
            if (!(hit instanceof EntityHitResult entity && entity.getEntity() instanceof Mob animal
                    && greet(player, hand, animal).consumesAction())) decline(server);
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide);
    }

    public InteractionResult greet(Player player, InteractionHand hand, Mob animal) {
        if (!AnimalMerchant.eligible(player, animal)) return InteractionResult.PASS;
        if (player instanceof ServerPlayer server) {
            if (AnimalMerchant.open(server, animal)) {
                ItemStack charm = player.getItemInHand(hand);
                player.awardStat(Stats.ITEM_USED.get(this));
                charm.hurtAndBreak(1, player, owner -> owner.broadcastBreakEvent(hand));
            } else {
                decline(server);
            }
        }
        return InteractionResult.sidedSuccess(player.level().isClientSide);
    }

    private static void decline(ServerPlayer player) {
        player.playNotifySound(SoundEvents.NOTE_BLOCK_SNARE.value(), player.getSoundSource(), 1, 1);
    }
}
