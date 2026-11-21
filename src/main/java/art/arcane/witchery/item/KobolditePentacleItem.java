package art.arcane.witchery.item;

import art.arcane.witchery.block.PlacedItemBlock;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

public final class KobolditePentacleItem extends Item {
    public KobolditePentacleItem() {
        super(new Properties());
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        return PlacedItemBlock.placeOnAltar(context, stack);
    }
}
