package art.arcane.witchery.item;

import art.arcane.witchery.world.DreamTravel;
import art.arcane.witchery.world.WitcheryDimensions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class IcyNeedleItem extends Item {
    public IcyNeedleItem() {
        super(new Properties());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer serverPlayer) {
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            if (level.dimension().equals(WitcheryDimensions.DREAM)) {
                DreamTravel.requestWake(serverPlayer);
            } else if (DreamTravel.isGhost(serverPlayer)) {
                DreamTravel.requestReturnFromGhost(serverPlayer);
            } else {
                player.hurt(level.damageSources().playerAttack(player), 1);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
