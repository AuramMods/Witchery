package art.arcane.witchery.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingHurtEvent;

public class GuardingSwordItem extends SwordItem {
    public GuardingSwordItem(Tier tier, int damage, float speed, Properties properties) {
        super(tier, damage, speed, properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (hand != InteractionHand.MAIN_HAND) return InteractionResultHolder.pass(stack);
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BLOCK;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 72_000;
    }

    public static void defend(ServerPlayer wearer, LivingHurtEvent event) {
        if (!wearer.isUsingItem() || !(wearer.getUseItem().getItem() instanceof GuardingSwordItem sword)) return;
        DamageSource source = event.getSource();
        if (event.getAmount() > 0 && !source.is(DamageTypeTags.BYPASSES_ARMOR)) {
            event.setAmount((1 + event.getAmount()) * 0.5F);
        }
        sword.onGuardHit(wearer, source);
    }

    protected void onGuardHit(ServerPlayer wearer, DamageSource source) {
    }
}
