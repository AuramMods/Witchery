package art.arcane.witchery.block.entity;

import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class SilverVatBlockEntity extends BlockEntity implements Container {
    private final NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);
    private final int[] observedGold = new int[4];

    public SilverVatBlockEntity(BlockPos pos, BlockState state) {
        super(WitcheryBlockEntities.SILVER_VAT.get(), pos, state);
    }

    public void observeGold(ServerLevel world, Direction side) {
        int index = side.get2DDataValue();
        int current = furnaceOutput(world, side);
        if (current == 0) return;

        // The original vat remembers only nonempty observations during this load.
        int previous = observedGold[index];
        observedGold[index] = current;
        if (current > previous && world.random.nextInt(5) == 0) {
            ItemStack stored = getItem(0);
            if (stored.isEmpty()) {
                setItem(0, new ItemStack(WitcheryIngredient.SILVER_DUST.item()));
            } else if (stored.getCount() < stored.getMaxStackSize()) {
                stored.grow(1);
                setChanged();
            }
        }
    }

    private int furnaceOutput(ServerLevel level, Direction side) {
        if (!(level.getBlockEntity(worldPosition.relative(side)) instanceof WorldlyContainer inventory)) return 0;
        Direction face = side.getOpposite();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.is(Items.GOLD_INGOT) && inventory.canTakeItemThroughFace(slot, stack, face)
                    && !inventory.canPlaceItemThroughFace(slot, stack, face)) return stack.getCount();
        }
        return 0;
    }

    @Override
    public int getContainerSize() {
        return items.size();
    }

    @Override
    public boolean isEmpty() {
        return getItem(0).isEmpty();
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        ItemStack removed = ContainerHelper.removeItem(items, slot, count);
        if (!removed.isEmpty()) setChanged();
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack.copyWithCount(Math.min(getMaxStackSize(), stack.getCount())));
        setChanged();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return false;
    }

    @Override
    public void clearContent() {
        items.clear();
        setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        items.clear();
        for (Tag entry : tag.getList("Items", Tag.TAG_COMPOUND)) {
            CompoundTag saved = (CompoundTag) entry;
            if (saved.getByte("Slot") == 0) {
                ItemStack stack = WitcheryIngredient.loadInventoryStack(saved);
                if (stack.is(WitcheryIngredient.SILVER_DUST.item())) {
                    items.set(0, stack.copyWithCount(Math.min(getMaxStackSize(), stack.getCount())));
                }
                break;
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
