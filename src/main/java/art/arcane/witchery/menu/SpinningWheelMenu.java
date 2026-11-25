package art.arcane.witchery.menu;

import art.arcane.witchery.crafting.SpinningRecipe;
import art.arcane.witchery.registry.WitcheryMenus;
import art.arcane.witchery.registry.WitcheryRecipes;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import java.util.List;

import static art.arcane.witchery.block.entity.SpinningWheelBlockEntity.COOK_TICKS;
import static art.arcane.witchery.block.entity.SpinningWheelBlockEntity.DATA_COUNT;
import static art.arcane.witchery.block.entity.SpinningWheelBlockEntity.POWERED;
import static art.arcane.witchery.block.entity.SpinningWheelBlockEntity.PROGRESS;
import static art.arcane.witchery.crafting.SpinningRecipe.FIBRE_SLOT;
import static art.arcane.witchery.crafting.SpinningRecipe.OUTPUT_SLOT;
import static art.arcane.witchery.crafting.SpinningRecipe.SLOT_COUNT;

public final class SpinningWheelMenu extends AbstractContainerMenu {
    private static final int HOTBAR_START = SLOT_COUNT + 27;
    private static final int INVENTORY_END = HOTBAR_START + 9;
    private final Container wheel;
    private final ContainerData progress;

    public SpinningWheelMenu(int id, Inventory inventory) {
        this(id, inventory, new SimpleContainer(SLOT_COUNT), new SimpleContainerData(DATA_COUNT));
    }

    public SpinningWheelMenu(int id, Inventory inventory, Container wheel, ContainerData progress) {
        super(WitcheryMenus.SPINNING_WHEEL.get(), id);
        checkContainerSize(wheel, SLOT_COUNT);
        checkContainerDataCount(progress, DATA_COUNT);
        this.wheel = wheel;
        this.progress = progress;

        addSlot(new Slot(wheel, FIBRE_SLOT, 56, 20));
        addSlot(new Slot(wheel, 1, 56, 53));
        addSlot(new Slot(wheel, OUTPUT_SLOT, 118, 21) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false;
            }
        });
        addSlot(new Slot(wheel, 3, 74, 53));
        addSlot(new Slot(wheel, 4, 92, 53));
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, 9 + row * 9 + column, 8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 142));
        }
        addDataSlots(progress);
    }

    public int progressWidth(int width) {
        return progress.get(PROGRESS) * width / COOK_TICKS;
    }

    public boolean powered() {
        return progress.get(POWERED) != 0;
    }

    @Override
    public boolean stillValid(Player player) {
        return wheel.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        boolean moved = index < SLOT_COUNT
                ? moveItemStackTo(stack, SLOT_COUNT, INVENTORY_END, index == OUTPUT_SLOT)
                : moveToMachineOrInventory(player, index, stack);
        if (!moved) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        slot.onTake(player, stack);
        return original;
    }

    private boolean moveToMachineOrInventory(Player player, int index, ItemStack stack) {
        List<SpinningRecipe> recipes = player.level().getRecipeManager().getAllRecipesFor(WitcheryRecipes.SPINNING.get());
        ItemStack fibre = wheel.getItem(FIBRE_SLOT);
        boolean matchesFibre = recipes.stream().anyMatch(recipe -> recipe.fibre().test(stack));
        if (matchesFibre && (fibre.isEmpty()
                || ItemStack.isSameItem(stack, fibre) && stack.getDamageValue() == fibre.getDamageValue())) {
            return moveItemStackTo(stack, FIBRE_SLOT, FIBRE_SLOT + 1, false);
        }
        if (matchesFibre || recipes.stream().anyMatch(recipe -> recipe.modifiers().stream().anyMatch(modifier -> modifier.test(stack)))) {
            return moveItemStackTo(stack, 1, 2, false) || moveItemStackTo(stack, 3, 4, false)
                    || moveItemStackTo(stack, 4, 5, false);
        }
        return index < HOTBAR_START ? moveItemStackTo(stack, HOTBAR_START, INVENTORY_END, false)
                : moveItemStackTo(stack, SLOT_COUNT, HOTBAR_START, false);
    }
}
