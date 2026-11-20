package art.arcane.witchery.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.Map;

public final class DemonHeartItem extends BlockItem {
    private final boolean blockItem;

    public DemonHeartItem(Block block, boolean blockItem) {
        super(block, new Properties().rarity(Rarity.RARE));
        this.blockItem = blockItem;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        return context.getPlayer() != null && context.getPlayer().isShiftKeyDown()
                ? super.useOn(context) : InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return player.isShiftKeyDown() ? InteractionResultHolder.pass(player.getItemInHand(hand))
                : ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 32;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.EAT;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity user) {
        if (user.isShiftKeyDown()) {
            return stack;
        }
        if (user instanceof ServerPlayer player) {
            CriteriaTriggers.CONSUME_ITEM.trigger(player, stack);
            player.awardStat(Stats.ITEM_USED.get(this));
        }
        if (!level.isClientSide()) {
            user.addEffect(new MobEffectInstance(MobEffects.HEALTH_BOOST, 2400, 4));
            user.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 2400, 1));
            user.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 2400, 2));
            user.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 2400, 2));
            user.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 2400, 2));
            user.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 2400));
            user.addEffect(new MobEffectInstance(MobEffects.HUNGER, 3600, 1));
            user.setSecondsOnFire(2640);
            user.gameEvent(GameEvent.EAT);
        }
        if (!(user instanceof Player player) || !player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return stack;
    }

    @Override
    public String getDescriptionId() {
        return getOrCreateDescriptionId();
    }

    @Override
    public void registerBlocks(Map<Block, Item> blockToItem, Item item) {
        if (blockItem) {
            super.registerBlocks(blockToItem, item);
        }
    }
}
