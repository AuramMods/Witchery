package art.arcane.witchery.item;

import art.arcane.witchery.block.PlacedItemBlock;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.context.UseOnContext;

public final class ArthanaItem extends SwordItem {
    public ArthanaItem() {
        super(Tiers.GOLD, 3, -2.4F, new Properties().durability(Tiers.IRON.getUses()).rarity(Rarity.UNCOMMON));
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        return PlacedItemBlock.placeOnAltar(context, stack);
    }
}
