package art.arcane.witchery.menu;

import art.arcane.witchery.item.BrewBagItem;
import art.arcane.witchery.registry.WitcheryMenus;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;

public final class BrewBagMenu extends AbstractContainerMenu {
    private static final int BAG_SLOTS = BrewBagItem.SLOT_COUNT;
    private static final int PLAYER_HOTBAR = BAG_SLOTS + 27;
    @Nullable private final ItemStack bag;
    private final int selected;

    public BrewBagMenu(int id, Inventory player, FriendlyByteBuf buffer) {
        this(id, player, null, buffer.readVarInt());
    }

    public BrewBagMenu(int id, Inventory player, @Nullable ItemStack bag, int selected) {
        super(WitcheryMenus.BREW_BAG.get(), id);
        this.bag = bag;
        this.selected = selected;

        SimpleContainer bottles = new SimpleContainer(BAG_SLOTS);
        if (bag != null) {
            var contents = BrewBagItem.contents(bag);
            for (int slot = 0; slot < BAG_SLOTS; slot++) bottles.setItem(slot, contents.get(slot));
            bottles.addListener(container -> {
                var stored = new ArrayList<ItemStack>(BAG_SLOTS);
                for (int slot = 0; slot < BAG_SLOTS; slot++) stored.add(container.getItem(slot));
                BrewBagItem.saveContents(bag, stored);
                player.setChanged();
            });
        }

        for (int slot = 0; slot < BAG_SLOTS; slot++) {
            addSlot(new Slot(bottles, slot, 16 + slot * 18, 18) {
                @Override public boolean mayPlace(ItemStack stack) { return BrewBagItem.accepts(stack); }
            });
        }
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) addSlot(new Slot(player, 9 + row * 9 + col, 8 + col * 18, 49 + row * 18));
        }
        for (int col = 0; col < 9; col++) {
            int slot = col;
            addSlot(new Slot(player, slot, 8 + slot * 18, 107) {
                @Override public boolean mayPickup(Player player) { return slot != selected; }
                @Override public boolean mayPlace(ItemStack stack) { return slot != selected; }
            });
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return bag == null || player.getInventory().selected == selected && player.getMainHandItem() == bag;
    }

    @Override
    public void clicked(int slot, int button, ClickType type, Player player) {
        if (!stillValid(player) || slot == PLAYER_HOTBAR + selected || type == ClickType.SWAP && button == selected) return;
        super.clicked(slot, button, type, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        Slot source = slots.get(slotIndex);
        if (!stillValid(player) || !source.hasItem() || !source.mayPickup(player)) return ItemStack.EMPTY;
        ItemStack content = source.getItem();
        ItemStack original = content.copy();
        boolean moved = slotIndex < BAG_SLOTS ? moveItemStackTo(content, BAG_SLOTS, slots.size(), true)
                : moveItemStackTo(content, 0, BAG_SLOTS, false);
        if (!moved) return ItemStack.EMPTY;
        if (content.isEmpty()) source.set(ItemStack.EMPTY);
        else source.setChanged();
        source.onTake(player, content);
        return original;
    }
}
