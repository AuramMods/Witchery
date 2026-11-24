package art.arcane.witchery.block.entity;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.config.WitcheryServerConfig;
import art.arcane.witchery.item.BoundPoppetItem;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryBlockEntities;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.world.WitcheryDimensions;
import com.google.gson.JsonParseException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.world.ForgeChunkManager;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public final class PoppetShelfBlockEntity extends RandomizableContainerBlockEntity {
    private static final int SLOTS = 9;
    private static final Map<ServerLevel, Set<BlockPos>> SHELVES = new WeakHashMap<>();
    private NonNullList<ItemStack> items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);

    public PoppetShelfBlockEntity(BlockPos pos, BlockState state) {
        super(WitcheryBlockEntities.POPPET_SHELF.get(), pos, state);
    }

    public static void restoreTickets(ServerLevel level, ForgeChunkManager.TicketHelper tickets) {
        for (var entry : tickets.getBlockTickets().entrySet()) {
            BlockPos pos = entry.getKey();
            long chunk = new ChunkPos(pos).toLong();
            if (!entry.getValue().getSecond().contains(chunk)) continue;
            if (level.getBlockState(pos).is(WitcheryBlocks.getRequired("poppetshelf").get())) {
                SHELVES.computeIfAbsent(level, ignored -> new LinkedHashSet<>()).add(pos.immutable());
            } else {
                tickets.removeTicket(pos, chunk, true);
            }
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        keepChunkLoaded();
    }

    public void keepChunkLoaded() {
        if (level instanceof ServerLevel server) {
            SHELVES.computeIfAbsent(server, ignored -> new LinkedHashSet<>()).add(worldPosition);
            ChunkPos chunk = new ChunkPos(worldPosition);
            ForgeChunkManager.forceChunk(server, Witchery.MODID, worldPosition, chunk.x, chunk.z, true, true);
        }
    }

    public static void unforce(ServerLevel level, BlockPos pos) {
        Set<BlockPos> positions = SHELVES.get(level);
        if (positions != null) positions.remove(pos);
        ChunkPos chunk = new ChunkPos(pos);
        ForgeChunkManager.forceChunk(level, Witchery.MODID, pos, chunk.x, chunk.z, false, true);
    }

    public static List<PoppetShelfBlockEntity> activeShelves(MinecraftServer server) {
        List<PoppetShelfBlockEntity> shelves = new ArrayList<>();
        for (ServerLevel level : server.getAllLevels()) {
            if (!canProtect(level)) continue;
            Set<BlockPos> positions = SHELVES.get(level);
            if (positions == null) continue;
            for (BlockPos pos : List.copyOf(positions)) {
                if (level.getBlockEntity(pos) instanceof PoppetShelfBlockEntity shelf) shelves.add(shelf);
            }
        }
        return shelves;
    }

    private static boolean canProtect(ServerLevel level) {
        if (!WitcheryServerConfig.RESTRICT_POPPET_SHELVES.get()) return true;
        var dimension = level.dimension();
        return dimension.equals(Level.OVERWORLD) || dimension.equals(Level.NETHER) || dimension.equals(Level.END)
                || dimension.equals(WitcheryDimensions.DREAM);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.witchery.poppetshelf");
    }

    @Override
    public int getContainerSize() {
        return SLOTS;
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return BoundPoppetItem.isPoppet(stack);
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory player) {
        return new ChestMenu(MenuType.GENERIC_9x1, id, player, this, 1);
    }

    @Override
    public void load(CompoundTag tag) {
        try {
            super.load(tag);
        } catch (JsonParseException oldName) {
            CompoundTag fixed = tag.copy();
            fixed.putString("CustomName", Component.Serializer.toJson(Component.literal(tag.getString("CustomName"))));
            super.load(fixed);
        }
        items = NonNullList.withSize(SLOTS, ItemStack.EMPTY);
        for (Tag entry : tag.getList("Items", Tag.TAG_COMPOUND)) {
            CompoundTag saved = (CompoundTag) entry;
            int slot = saved.getByte("Slot") & 255;
            if (slot < SLOTS) items.set(slot, WitcheryIngredient.loadInventoryStack(saved));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, items);
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level instanceof ServerLevel) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
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
