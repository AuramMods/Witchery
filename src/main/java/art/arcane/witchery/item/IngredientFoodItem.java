package art.arcane.witchery.item;

import art.arcane.witchery.registry.WitcherySounds;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;

public class IngredientFoodItem extends Item {
    public IngredientFoodItem(FoodProperties food) {
        super(new Properties().food(food));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity eater) {
        if (!(eater instanceof Player player)) return super.finishUsingItem(stack, level, eater);

        var food = player.getFoodData();
        int previousHunger = food.getFoodLevel();
        food.eat(this, stack, player);
        player.awardStat(Stats.ITEM_USED.get(this));
        if (player instanceof ServerPlayer serverPlayer) CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, stack);
        if (!player.getAbilities().instabuild) stack.shrink(1);

        if (!level.isClientSide()) {
            afterEating(player, food.getFoodLevel() - previousHunger);
            player.gameEvent(GameEvent.EAT);
            WitcherySounds.playIngredientBurp(level, player);
        }
        return stack;
    }

    protected void afterEating(Player player, int foodRestored) {}
}
