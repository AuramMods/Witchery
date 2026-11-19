package art.arcane.witchery.block.entity;

import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class PlacedItemBlockEntity extends BlockEntity {
    private static final String ITEM = "WITCPlacedItem";
    private ItemStack stack = ItemStack.EMPTY;

    public PlacedItemBlockEntity(BlockPos pos, BlockState state) {
        super(WitcheryBlockEntities.PLACED_ITEM.get(), pos, state);
    }

    public ItemStack stack() {
        return stack;
    }

    public void place(ItemStack item) {
        stack = item.copyWithCount(1);
        setChanged();
        if (level != null && !level.isClientSide) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        stack = WitcheryIngredient.loadInventoryStack(tag.getCompound(ITEM));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (!stack.isEmpty()) {
            tag.put(ITEM, stack.save(new CompoundTag()));
        }
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
