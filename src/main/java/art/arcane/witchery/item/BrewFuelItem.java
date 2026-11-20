package art.arcane.witchery.item;

import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class BrewFuelItem extends Item {
    private static final String TIER = "FuelTier";
    private static final int[] BURN_TICKS = {2400, 5000, 10000, 50000};
    private static final int[] COLORS = {0xFFA65E, 0xFF8E38, 0xFF710C, 0xE05900};

    public BrewFuelItem() {
        super(new Properties());
    }

    public static void setTier(ItemStack stack, int tier) {
        stack.getOrCreateTag().putInt(TIER, Mth.clamp(tier, 0, BURN_TICKS.length - 1));
    }

    public static int tier(ItemStack stack) {
        return stack.getTag() == null ? 0 : Mth.clamp(stack.getTag().getInt(TIER), 0, BURN_TICKS.length - 1);
    }

    public static int color(ItemStack stack) {
        return COLORS[tier(stack)];
    }

    @Override
    public int getBurnTime(ItemStack stack, @Nullable RecipeType<?> recipeType) {
        return BURN_TICKS[tier(stack)];
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flags) {
        tooltip.add(Component.translatable("tooltip.witchery.brew.fuel", BURN_TICKS[tier(stack)] / 20));
    }
}
