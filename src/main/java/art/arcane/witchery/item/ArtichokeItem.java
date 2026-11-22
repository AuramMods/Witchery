package art.arcane.witchery.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class ArtichokeItem extends Item {
    public ArtichokeItem() {
        super(new Properties().food(new FoodProperties.Builder().nutrition(20).saturationMod(0F).build()));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity eater) {
        if (level.isClientSide() || !(eater instanceof Player player)) {
            return super.finishUsingItem(stack, level, eater);
        }
        var food = player.getFoodData();
        int before = food.getFoodLevel();
        ItemStack remaining = super.finishUsingItem(stack, level, eater);
        int restored = food.getFoodLevel() - before;
        if (restored > 0) player.addEffect(new MobEffectInstance(MobEffects.HUNGER, restored * 60, 2));
        return remaining;
    }
}
