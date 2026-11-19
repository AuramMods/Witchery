package art.arcane.witchery.menu;

import art.arcane.witchery.block.entity.DistilleryBlockEntity;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryMenus;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import static art.arcane.witchery.block.entity.DistilleryBlockEntity.COOK_TICKS;
import static art.arcane.witchery.block.entity.DistilleryBlockEntity.DATA_COUNT;
import static art.arcane.witchery.block.entity.DistilleryBlockEntity.FIRST_INPUT;
import static art.arcane.witchery.block.entity.DistilleryBlockEntity.FIRST_RESULT;
import static art.arcane.witchery.block.entity.DistilleryBlockEntity.JARS;
import static art.arcane.witchery.block.entity.DistilleryBlockEntity.POWERED;
import static art.arcane.witchery.block.entity.DistilleryBlockEntity.PROGRESS;
import static art.arcane.witchery.block.entity.DistilleryBlockEntity.SECOND_INPUT;
import static art.arcane.witchery.block.entity.DistilleryBlockEntity.SLOT_COUNT;

public final class DistilleryMenu extends AbstractContainerMenu {
    private static final int HOTBAR_START = SLOT_COUNT + 27;
    private static final int INVENTORY_END = HOTBAR_START + 9;
    private final Container distillery;
    private final ContainerData progress;

    public DistilleryMenu(int id, Inventory inventory) {
        this(id, inventory, new SimpleContainer(SLOT_COUNT), new SimpleContainerData(DATA_COUNT));
    }

    public DistilleryMenu(int id, Inventory inventory, Container distillery, ContainerData progress) {
        super(WitcheryMenus.DISTILLERY.get(), id);
        checkContainerSize(distillery, SLOT_COUNT);
        checkContainerDataCount(progress, DATA_COUNT);
        this.distillery = distillery;
        this.progress = progress;

        addMachineSlot(FIRST_INPUT, 48, 16);
        addMachineSlot(SECOND_INPUT, 48, 34);
        addMachineSlot(JARS, 48, 54);
        for (int i = 0; i < 4; i++) {
            addMachineSlot(FIRST_RESULT + i, 110 + (i % 2) * 18, 16 + (i / 2) * 18);
        }
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

    private void addMachineSlot(int index, int x, int y) {
        addSlot(new Slot(distillery, index, x, y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return DistilleryBlockEntity.acceptsItem(index, stack);
            }
        });
    }

    public int cookTime() {
        return progress.get(PROGRESS);
    }

    public int progressWidth(int width) {
        return cookTime() * width / COOK_TICKS;
    }

    public boolean powered() {
        return progress.get(POWERED) != 0;
    }

    @Override
    public boolean stillValid(Player player) {
        return distillery.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        boolean moved;
        if (index < SLOT_COUNT) {
            moved = moveItemStackTo(stack, SLOT_COUNT, INVENTORY_END, index >= FIRST_RESULT);
        } else if (stack.is(WitcheryIngredient.CLAY_JAR.item())) {
            moved = moveItemStackTo(stack, JARS, JARS + 1, false);
        } else {
            moved = moveItemStackTo(stack, FIRST_INPUT, SECOND_INPUT + 1, false)
                    || (index < HOTBAR_START ? moveItemStackTo(stack, HOTBAR_START, INVENTORY_END, false)
                    : moveItemStackTo(stack, SLOT_COUNT, HOTBAR_START, false));
        }
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
}
