package art.arcane.witchery.item;

import art.arcane.witchery.world.DreamTravel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class SleepingAppleItem extends Item {
    public SleepingAppleItem() {
        super(new Properties().food(new FoodProperties.Builder().nutrition(3).saturationMod(3.0F).alwaysEat().build()));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity eater) {
        ItemStack remaining = super.finishUsingItem(stack, level, eater);
        DreamTravel.requestNightmare(eater);
        return remaining;
    }
}
