package art.arcane.witchery.menu;

import art.arcane.witchery.block.entity.WitchesOvenBlockEntity;
import art.arcane.witchery.registry.WitcheryMenus;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.FurnaceResultSlot;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import static art.arcane.witchery.block.entity.WitchesOvenBlockEntity.BURN_PROGRESS;
import static art.arcane.witchery.block.entity.WitchesOvenBlockEntity.COOK_PROGRESS;
import static art.arcane.witchery.block.entity.WitchesOvenBlockEntity.FUEL_SLOT;
import static art.arcane.witchery.block.entity.WitchesOvenBlockEntity.FUME_SLOT;
import static art.arcane.witchery.block.entity.WitchesOvenBlockEntity.INPUT_SLOT;
import static art.arcane.witchery.block.entity.WitchesOvenBlockEntity.JAR_SLOT;
import static art.arcane.witchery.block.entity.WitchesOvenBlockEntity.PROGRESS_COUNT;
import static art.arcane.witchery.block.entity.WitchesOvenBlockEntity.PROGRESS_SCALE;
import static art.arcane.witchery.block.entity.WitchesOvenBlockEntity.RESULT_SLOT;
import static art.arcane.witchery.block.entity.WitchesOvenBlockEntity.SLOT_COUNT;

public final class WitchesOvenMenu extends AbstractContainerMenu {
    private static final int HOTBAR_START = SLOT_COUNT + 27;
    private static final int INVENTORY_END = HOTBAR_START + 9;
    private final Container oven;
    private final ContainerData progress;

    public WitchesOvenMenu(int id, Inventory inventory) {
        this(id, inventory, new SimpleContainer(SLOT_COUNT), new SimpleContainerData(PROGRESS_COUNT));
    }

    public WitchesOvenMenu(int id, Inventory inventory, Container oven, ContainerData progress) {
        super(WitcheryMenus.WITCHES_OVEN.get(), id);
        checkContainerSize(oven, SLOT_COUNT);
        checkContainerDataCount(progress, PROGRESS_COUNT);
        this.oven = oven;
        this.progress = progress;

        addOvenSlot(INPUT_SLOT, 56, 17);
        addOvenSlot(FUEL_SLOT, 56, 53);
        addSlot(new FurnaceResultSlot(inventory.player, oven, RESULT_SLOT, 118, 21) {
            @Override
            public void onTake(Player player, ItemStack stack) {
                super.onTake(player, stack);
                if (player instanceof ServerPlayer serverPlayer && oven instanceof WitchesOvenBlockEntity blockEntity) {
                    blockEntity.awardSmeltingExperience(serverPlayer);
                }
            }
        });
        addSlot(new FurnaceResultSlot(inventory.player, oven, FUME_SLOT, 118, 53));
        addOvenSlot(JAR_SLOT, 83, 53);

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, 9 + row * 9 + column, 8 + 18 * column, 84 + 18 * row));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + 18 * column, 142));
        }
        addDataSlots(progress);
    }

    private void addOvenSlot(int index, int x, int y) {
        addSlot(new Slot(oven, index, x, y) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return WitchesOvenBlockEntity.acceptsItem(index, stack);
            }
        });
    }

    public int flameHeight(int height) {
        int burn = progress.get(BURN_PROGRESS);
        return burn > 0 ? Math.max(1, burn * height / PROGRESS_SCALE) : 0;
    }

    public int arrowWidth(int width) {
        return progress.get(COOK_PROGRESS) * width / PROGRESS_SCALE;
    }

    @Override
    public boolean stillValid(Player player) {
        return oven.stillValid(player);
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
            moved = moveItemStackTo(stack, SLOT_COUNT, INVENTORY_END, index == RESULT_SLOT || index == FUME_SLOT);
        } else if (WitchesOvenBlockEntity.hasOvenRecipe(player.level(), stack)) {
            moved = moveItemStackTo(stack, INPUT_SLOT, INPUT_SLOT + 1, false);
        } else if (WitchesOvenBlockEntity.acceptsItem(FUEL_SLOT, stack)) {
            moved = moveItemStackTo(stack, FUEL_SLOT, FUEL_SLOT + 1, false);
        } else if (WitchesOvenBlockEntity.acceptsItem(JAR_SLOT, stack)) {
            moved = moveItemStackTo(stack, JAR_SLOT, JAR_SLOT + 1, false);
        } else if (index < HOTBAR_START) {
            moved = moveItemStackTo(stack, HOTBAR_START, INVENTORY_END, false);
        } else {
            moved = moveItemStackTo(stack, SLOT_COUNT, HOTBAR_START, false);
        }
        if (!moved) {
            return ItemStack.EMPTY;
        }
        if (index == RESULT_SLOT || index == FUME_SLOT) {
            slot.onQuickCraft(stack, original);
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
