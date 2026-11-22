package art.arcane.witchery.item;

import net.minecraft.world.entity.LivingEntity;
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
        ItemStack remaining = super.finishUsingItem(stack, level, eater);
        if (!(eater instanceof Player player) || player.getAbilities().instabuild) return remaining;

        ItemStack bowl = new ItemStack(Items.BOWL);
        if (remaining.isEmpty()) return bowl;
        if (!level.isClientSide) player.getInventory().placeItemBackInInventory(bowl);
        return remaining;
    }
}
