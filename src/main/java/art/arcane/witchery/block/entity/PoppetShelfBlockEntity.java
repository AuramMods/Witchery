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
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
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
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.world.ForgeChunkManager;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public final class PoppetShelfBlockEntity extends RandomizableContainerBlockEntity {
    private static final int SLOTS = 9;
    private static final List<ResourceKey<Level>> ORIGINAL_WORLD_ORDER = List.of(Level.OVERWORLD, Level.NETHER, Level.END,
            WitcheryDimensions.DREAM, WitcheryDimensions.TORMENT, WitcheryDimensions.MIRROR);
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

    @Override
    public void onChunkUnloaded() {
        if (level instanceof ServerLevel server) removeLoadedShelf(server, worldPosition);
        super.onChunkUnloaded();
    }

    public void keepChunkLoaded() {
        if (level instanceof ServerLevel server) {
            SHELVES.computeIfAbsent(server, ignored -> new LinkedHashSet<>()).add(worldPosition);
            ChunkPos chunk = new ChunkPos(worldPosition);
            ForgeChunkManager.forceChunk(server, Witchery.MODID, worldPosition, chunk.x, chunk.z, true, true);
        }
    }

    public static void unforce(ServerLevel level, BlockPos pos) {
        removeLoadedShelf(level, pos);
        ChunkPos chunk = new ChunkPos(pos);
        ForgeChunkManager.forceChunk(level, Witchery.MODID, pos, chunk.x, chunk.z, false, true);
    }

    private static void removeLoadedShelf(ServerLevel level, BlockPos pos) {
        Set<BlockPos> positions = SHELVES.get(level);
        if (positions != null) positions.remove(pos);
    }

    public static List<PoppetShelfBlockEntity> activeShelves(MinecraftServer server) {
        List<PoppetShelfBlockEntity> shelves = new ArrayList<>();
        boolean restricted = WitcheryServerConfig.RESTRICT_POPPET_SHELVES.get();
        // Forge 1.7.10 put vanilla worlds first, then the original Witchery dimensions in their default order.
        var worlds = restricted ? ORIGINAL_WORLD_ORDER.subList(0, 4) : ORIGINAL_WORLD_ORDER;
        for (ResourceKey<Level> world : worlds) addLoadedShelves(server.getLevel(world), shelves);
        if (!restricted) {
            for (ServerLevel level : server.getAllLevels()) {
                if (!ORIGINAL_WORLD_ORDER.contains(level.dimension())) addLoadedShelves(level, shelves);
            }
        }
        return shelves;
    }

    private static void addLoadedShelves(@Nullable ServerLevel level, List<PoppetShelfBlockEntity> shelves) {
        if (level == null) return;
        Set<BlockPos> positions = SHELVES.get(level);
        if (positions == null) return;
        for (BlockPos pos : List.copyOf(positions)) {
            LevelChunk chunk = level.getChunkSource().getChunkNow(SectionPos.blockToSectionCoord(pos.getX()),
                    SectionPos.blockToSectionCoord(pos.getZ()));
            if (chunk != null && chunk.getBlockEntity(pos, LevelChunk.EntityCreationType.CHECK) instanceof PoppetShelfBlockEntity shelf
                    && !shelf.isRemoved()) shelves.add(shelf);
        }
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
