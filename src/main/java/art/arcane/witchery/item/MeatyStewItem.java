package art.arcane.witchery.item;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public final class MeatyStewItem extends Item {
    public MeatyStewItem(int nutrition, float saturation) {
        super(new Properties().food(new FoodProperties.Builder()
                .nutrition(nutrition).saturationMod(saturation).build()));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity eater) {
        if (!(eater instanceof Player player)) return super.finishUsingItem(stack, level, eater);
        if (!level.isClientSide) {
            // The original inventory kept the eating slot occupied while returning the bowl.
            ItemStack bowl = new ItemStack(Items.BOWL);
            if (player.getInventory().add(bowl)) {
                player.containerMenu.broadcastChanges();
            } else {
                level.addFreshEntity(new ItemEntity(level, player.getX() + 0.5, player.getY() + 1.5,
                        player.getZ() + 0.5, bowl));
            }
        }
        ItemStack remaining = super.finishUsingItem(stack, level, eater);
        if (player.getAbilities().instabuild) remaining.shrink(1);
        return remaining;
    }
}
