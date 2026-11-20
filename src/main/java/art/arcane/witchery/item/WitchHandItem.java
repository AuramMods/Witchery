package art.arcane.witchery.item;

import art.arcane.witchery.infusion.InfusionEnergy;
import art.arcane.witchery.infusion.OtherwhereInfusion;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public final class WitchHandItem extends Item {
    private static final String COOLDOWN = "WITCCooldown";
    private static final int USE_TICKS = 400;
    private static final int TELEPORT_COOLDOWN_MILLIS = 1500;

    public WitchHandItem() {
        super(new Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return USE_TICKS;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BLOCK;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public void onUseTick(Level level, LivingEntity living, ItemStack stack, int remaining) {
        if (living instanceof ServerPlayer player && InfusionEnergy.type(player) == OtherwhereInfusion.ID) {
            OtherwhereInfusion.channel(player, USE_TICKS - remaining);
        }
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity living, int remaining) {
        if (!(living instanceof ServerPlayer player) || InfusionEnergy.type(player) != OtherwhereInfusion.ID) return;
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.getLong(COOLDOWN) > System.currentTimeMillis()) return;
        if (OtherwhereInfusion.release(player, USE_TICKS - remaining)) {
            stack.getOrCreateTag().putLong(COOLDOWN, System.currentTimeMillis() + TELEPORT_COOLDOWN_MILLIS);
        }
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity target) {
        if (player instanceof ServerPlayer caster && target instanceof LivingEntity living
                && InfusionEnergy.type(caster) == OtherwhereInfusion.ID) {
            OtherwhereInfusion.attack(caster, living);
        }
        return true;
    }
}
