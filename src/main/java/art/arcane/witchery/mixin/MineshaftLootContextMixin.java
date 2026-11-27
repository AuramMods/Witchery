package art.arcane.witchery.mixin;

import art.arcane.witchery.data.LegacyMineshaftLoot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LootContext.class)
abstract class MineshaftLootContextMixin implements LegacyMineshaftLoot.Context {
    @Unique private ItemStack witchery$mineshaftBook = ItemStack.EMPTY;

    @Override
    public ItemStack witchery$getMineshaftBook() {
        return witchery$mineshaftBook;
    }

    @Override
    public void witchery$setMineshaftBook(ItemStack book) {
        witchery$mineshaftBook = book;
    }
}
