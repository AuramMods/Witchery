package art.arcane.witchery.brewing;

import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public final class ColorfulEffect extends TrackedBrewEffect {
    public static final int DURATION = 1800;

    public ColorfulEffect(int color) {
        super(MobEffectCategory.HARMFUL, color);
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        return List.of();
    }

}
