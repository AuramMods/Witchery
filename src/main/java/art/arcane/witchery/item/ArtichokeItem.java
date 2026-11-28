package art.arcane.witchery.item;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;

public final class ArtichokeItem extends IngredientFoodItem {
    public ArtichokeItem() {
        super(new FoodProperties.Builder().nutrition(20).saturationMod(0F).build());
    }

    @Override
    protected void afterEating(Player player, int foodRestored) {
        if (foodRestored > 0) player.addEffect(new MobEffectInstance(MobEffects.HUNGER, foodRestored * 60, 2));
    }
}
