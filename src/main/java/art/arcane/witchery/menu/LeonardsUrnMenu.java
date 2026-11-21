package art.arcane.witchery.menu;

import art.arcane.witchery.item.LeonardsUrnItem;
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

public final class LeonardsUrnMenu extends AbstractContainerMenu {
    private final int urnSlots;
    private final int lockedHotbar;
    @Nullable
    private final ItemStack urn;

    public LeonardsUrnMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
        this(id, inventory, null, buffer.readVarInt(), buffer.readVarInt());
    }

    public LeonardsUrnMenu(int id, Inventory inventory, ItemStack urn, int selected) {
        this(id, inventory, urn, LeonardsUrnItem.tier(urn) + 1, selected);
    }

    private LeonardsUrnMenu(int id, Inventory inventory, @Nullable ItemStack urn, int capacity, int selected) {
        super(WitcheryMenus.LEONARDS_URN.get(), id);
        this.urn = urn;
        urnSlots = capacity;
        lockedHotbar = selected;
        SimpleContainer bottles = new SimpleContainer(capacity);
        if (urn != null) {
            var saved = LeonardsUrnItem.contents(urn);
            for (int i = 0; i < capacity; i++) bottles.setItem(i, saved.get(i));
            bottles.addListener(container -> {
                var stacks = new ArrayList<ItemStack>(capacity);
                for (int i = 0; i < capacity; i++) stacks.add(container.getItem(i));
                LeonardsUrnItem.saveContents(urn, stacks);
                inventory.setChanged();
            });
        }

        addBrewSlot(bottles, 0, 80, 22);
        if (capacity > 1) addBrewSlot(bottles, 1, 80, 70);
        if (capacity > 2) addBrewSlot(bottles, 2, 103, 46);
        if (capacity > 3) addBrewSlot(bottles, 3, 56, 46);
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) addSlot(new Slot(inventory, 9 + col + row * 9, 8 + col * 18, 103 + row * 18));
        }
        for (int col = 0; col < 9; col++) {
            int index = col;
            addSlot(new Slot(inventory, col, 8 + col * 18, 161) {
                @Override public boolean mayPickup(Player player) { return index != lockedHotbar; }
                @Override public boolean mayPlace(ItemStack item) { return index != lockedHotbar; }
            });
        }
    }

    private void addBrewSlot(SimpleContainer container, int index, int x, int y) {
        addSlot(new Slot(container, index, x, y) {
            @Override public boolean mayPlace(ItemStack item) { return LeonardsUrnItem.splash(item) != null; }
            @Override public int getMaxStackSize() { return 1; }
        });
    }

    public int urnSlots() { return urnSlots; }

    @Override
    public boolean stillValid(Player player) {
        return urn == null || player.getInventory().selected == lockedHotbar && player.getMainHandItem() == urn;
    }

    @Override
    public void clicked(int slot, int button, ClickType type, Player player) {
        if (!stillValid(player) || slot == urnSlots + 27 + lockedHotbar || type == ClickType.SWAP && button == lockedHotbar) return;
        super.clicked(slot, button, type, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotIndex) {
        Slot source = slots.get(slotIndex);
        if (!source.hasItem() || !source.mayPickup(player)) return ItemStack.EMPTY;
        ItemStack content = source.getItem();
        ItemStack original = content.copy();
        boolean moved = slotIndex < urnSlots ? moveItemStackTo(content, urnSlots, slots.size(), true)
                : moveItemStackTo(content, 0, urnSlots, false);
        if (!moved) return ItemStack.EMPTY;
        if (content.isEmpty()) source.set(ItemStack.EMPTY);
        else source.setChanged();
        source.onTake(player, content);
        return original;
    }
}
