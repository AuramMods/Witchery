package art.arcane.witchery.item;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import java.util.List;

final class ItemInventory {
    private ItemInventory() {}

    static NonNullList<ItemStack> load(ItemStack container, int size) {
        var contents = NonNullList.withSize(size, ItemStack.EMPTY);
        var inventory = container.getTagElement("Inventory");
        if (inventory == null) return contents;
        for (Tag tag : inventory.getList("Items", Tag.TAG_COMPOUND)) {
            var entry = (CompoundTag) tag;
            int slot = entry.getByte("Slot") & 255;
            if (slot < size) contents.set(slot, WitcheryIngredient.loadInventoryStack(entry));
        }
        return contents;
    }

    static void save(ItemStack container, List<ItemStack> contents) {
        var items = new ListTag();
        for (int slot = 0; slot < contents.size(); slot++) {
            ItemStack stack = contents.get(slot);
            if (stack.isEmpty()) continue;
            var entry = new CompoundTag();
            entry.putByte("Slot", (byte) slot);
            items.add(stack.save(entry));
        }
        container.getOrCreateTagElement("Inventory").put("Items", items);
    }
}
