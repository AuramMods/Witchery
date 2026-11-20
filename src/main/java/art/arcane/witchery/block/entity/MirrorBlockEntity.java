package art.arcane.witchery.block.entity;

import art.arcane.witchery.block.MirrorBlock;
import art.arcane.witchery.item.MirrorItem;
import art.arcane.witchery.registry.WitcheryItems;
import art.arcane.witchery.world.WitcheryDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public final class MirrorBlockEntity extends BlockEntity {
    private static final List<String> LEGACY_DETAILS = List.of("Favorite", "Fairest", "PlayersSeen", "LastFairestSpawnTime");
    private final CompoundTag legacyDetails = new CompoundTag();
    @Nullable private GlobalPos counterpart;
    @Nullable private UUID connectionId;
    private boolean connected;
    private boolean demonSlain;

    public MirrorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public Direction facing() {
        return getBlockState().getValue(MirrorBlock.FACING);
    }

    public boolean isRoomExit() {
        return ((MirrorBlock) getBlockState().getBlock()).isRoomExit();
    }

    @Nullable
    public GlobalPos counterpart() {
        return counterpart;
    }

    @Nullable
    public UUID connectionId() {
        return connectionId;
    }

    public boolean isConnected() {
        return connected;
    }

    public boolean demonSlain() {
        return demonSlain;
    }

    public void bind(GlobalPos destination, UUID connection) {
        counterpart = destination;
        connectionId = connection;
        connected = true;
        sync();
    }

    public void setDisconnected() {
        if (connected) {
            connected = false;
            sync();
        }
    }

    public void markDemonSlain() {
        if (!demonSlain) {
            demonSlain = true;
            sync();
        }
    }

    private void sync() {
        setChanged();
        if (level != null && !level.isClientSide()) {
            BlockState state = getBlockState();
            level.sendBlockUpdated(worldPosition, state, state, Block.UPDATE_CLIENTS);
        }
    }

    public ItemStack toItem() {
        ItemStack item = new ItemStack(WitcheryItems.getRequired("mirror").get());
        CompoundTag data = new CompoundTag();
        writeMirrorData(data);
        item.setTag(data);
        return item;
    }

    public void applyItem(ItemStack stack) {
        if (stack.hasTag()) {
            readMirrorData(stack.getTag());
            sync();
        }
    }

    public static boolean carriesLink(ItemStack stack, GlobalPos room, @Nullable UUID connection) {
        return !stack.isEmpty() && stack.getItem() instanceof MirrorItem && stack.hasTag()
                && (connection == null || stack.getTag().hasUUID("Connection") && connection.equals(stack.getTag().getUUID("Connection")))
                && room.equals(readPosition(stack.getTag(), WitcheryDimensions.MIRROR));
    }

    private static @Nullable GlobalPos readPosition(CompoundTag root, ResourceKey<Level> fallback) {
        CompoundTag coords = root.getCompound("DimCoords");
        if (!coords.contains("posX", Tag.TAG_ANY_NUMERIC) || !coords.contains("posY", Tag.TAG_ANY_NUMERIC)
                || !coords.contains("posZ", Tag.TAG_ANY_NUMERIC)) {
            return null;
        }
        String rawDimension = coords.getString("DimensionId");
        ResourceLocation key = rawDimension.isEmpty() ? null : ResourceLocation.tryParse(rawDimension);
        ResourceKey<Level> dimension = key == null ? fallback : ResourceKey.create(Registries.DIMENSION, key);
        return GlobalPos.of(dimension, new BlockPos(coords.getInt("posX"), coords.getInt("posY"), coords.getInt("posZ")));
    }

    private static ResourceKey<Level> legacyDimension(int id) {
        return switch (id) {
            case -39 -> WitcheryDimensions.MIRROR;
            case -38 -> WitcheryDimensions.TORMENT;
            case -37 -> WitcheryDimensions.DREAM;
            case -1 -> Level.NETHER;
            case 1 -> Level.END;
            default -> Level.OVERWORLD;
        };
    }

    private void readMirrorData(CompoundTag root) {
        ResourceKey<Level> fallback = isRoomExit()
                ? legacyDimension(root.getCompound("DimCoords").getInt("Dimension")) : WitcheryDimensions.MIRROR;
        counterpart = readPosition(root, fallback);
        connectionId = root.hasUUID("Connection") ? root.getUUID("Connection") : null;
        connected = counterpart != null && (!root.contains("Connected") || root.getBoolean("Connected"));
        demonSlain = root.getBoolean("DemonSlain");
        for (String field : LEGACY_DETAILS) {
            Tag value = root.get(field);
            if (value != null) legacyDetails.put(field, value.copy());
            else legacyDetails.remove(field);
        }
    }

    private void writeMirrorData(CompoundTag root) {
        if (counterpart != null) {
            CompoundTag coords = new CompoundTag();
            coords.putInt("posX", counterpart.pos().getX());
            coords.putInt("posY", counterpart.pos().getY());
            coords.putInt("posZ", counterpart.pos().getZ());
            coords.putString("DimensionId", counterpart.dimension().location().toString());
            root.put("DimCoords", coords);
        }
        if (connectionId != null) root.putUUID("Connection", connectionId);
        root.putBoolean("DemonSlain", demonSlain);
        root.putBoolean("Connected", connected);
        root.merge(legacyDetails.copy());
    }

    @Override
    public void load(CompoundTag root) {
        super.load(root);
        readMirrorData(root);
    }

    @Override
    protected void saveAdditional(CompoundTag root) {
        super.saveAdditional(root);
        writeMirrorData(root);
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
