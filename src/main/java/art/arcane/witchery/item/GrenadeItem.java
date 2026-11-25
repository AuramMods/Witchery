package art.arcane.witchery.item;

import art.arcane.witchery.entity.GrenadeProjectile;
import art.arcane.witchery.registry.WitcheryItems;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class GrenadeItem extends Item {
    private final boolean duplicate;

    public GrenadeItem(boolean duplicate) {
        super(new Properties().stacksTo(16).rarity(Rarity.UNCOMMON));
        this.duplicate = duplicate;
    }

    public static ItemStack duplicate(String owner) {
        ItemStack item = new ItemStack(WitcheryItems.getRequired("dupgrenade").get());
        item.getOrCreateTag().putString("Owner", owner);
        return item;
    }

    public static boolean isDuplicate(ItemStack stack) {
        return stack.getItem() instanceof GrenadeItem grenade && grenade.duplicate;
    }

    public static String ownerName(ItemStack grenade) {
        return grenade.hasTag() ? grenade.getTag().getString("Owner") : "";
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flags) {
        if (duplicate) {
            String owner = ownerName(stack);
            tooltip.add(Component.translatable("item.witchery.dupgrenade.owner", owner.isEmpty() ? "Steve" : owner));
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            GrenadeProjectile grenade = new GrenadeProjectile(level, player);
            grenade.setItem(stack);
            if (level.addFreshEntity(grenade)) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT,
                        SoundSource.PLAYERS, 0.5F, 0.4F / (level.random.nextFloat() * 0.4F + 0.8F));
                if (!player.getAbilities().instabuild) stack.shrink(1);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

}
