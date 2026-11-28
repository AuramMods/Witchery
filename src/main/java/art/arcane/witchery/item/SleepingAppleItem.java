package art.arcane.witchery.item;

import art.arcane.witchery.world.DreamTravel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;

public final class SleepingAppleItem extends IngredientFoodItem {
    public SleepingAppleItem() {
        super(new FoodProperties.Builder().nutrition(3).saturationMod(3.0F).alwaysEat().build());
    }

    @Override
    protected void afterEating(Player player, int foodRestored) {
        DreamTravel.requestNightmare(player);
    }
}
