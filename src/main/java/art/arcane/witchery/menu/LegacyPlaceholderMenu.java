package art.arcane.witchery.menu;

import art.arcane.witchery.registry.WitcheryMenus;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class LegacyPlaceholderMenu extends AbstractContainerMenu {
    private final String legacyMenuKey;

    public LegacyPlaceholderMenu(int containerId, Inventory playerInventory, String legacyMenuKey) {
        super(WitcheryMenus.getRequired(legacyMenuKey).get(), containerId);
        this.legacyMenuKey = legacyMenuKey;
    }

    public String legacyMenuKey() {
        return legacyMenuKey;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
}
