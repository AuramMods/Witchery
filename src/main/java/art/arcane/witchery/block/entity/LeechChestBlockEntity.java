package art.arcane.witchery.block.entity;

import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class LeechChestBlockEntity extends ChestBlockEntity {
    private final LeechSamples samples = new LeechSamples(3);
    private int renderedCount;

    public LeechChestBlockEntity(BlockPos pos, BlockState state) {
        super(WitcheryBlockEntities.LEECH_CHEST.get(), pos, state);
    }

    public void record(ServerPlayer player) {
        if (samples.record(player)) syncSamples();
    }

    public void loadSamples(CompoundTag itemTag) {
        samples.load(itemTag);
        syncSamples();
    }

    @Nullable
    public ServerPlayer collect(ServerPlayer collector) {
        ServerPlayer subject = samples.take(collector, true);
        if (subject != null) syncSamples();
        return subject;
    }

    private void syncSamples() {
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        samples.load(tag);
        ListTag items = tag.getList("Items", Tag.TAG_COMPOUND);
        for (int i = 0; i < items.size(); i++) {
            CompoundTag entry = items.getCompound(i);
            int slot = entry.getByte("Slot") & 255;
            if (slot < getContainerSize()) getItems().set(slot, WitcheryIngredient.loadInventoryStack(entry));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        samples.save(tag);
    }

    public int renderedCount() {
        return renderedCount;
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        tag.putByte("Samples", (byte) samples.size());
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        renderedCount = tag.getByte("Samples");
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet) {
        CompoundTag tag = packet.getTag();
        if (tag != null) handleUpdateTag(tag);
    }
}
