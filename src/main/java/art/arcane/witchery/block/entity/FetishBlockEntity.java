package art.arcane.witchery.block.entity;

import art.arcane.witchery.registry.WitcheryBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

public final class FetishBlockEntity extends BlockEntity {
    private static final int ENHANCED_POPPETS = 1;
    private static final int RANGE = 16;
    private static final List<String> LEGACY_DATA = List.of("EffectTypeID", "BlockColor", "AlarmMode", "KnownPlayers",
            "KnownCreatureTypes", "KnownCreatures");
    private CompoundTag data = new CompoundTag();
    private boolean spectral;
    private long lastActivation;

    public FetishBlockEntity(BlockPos pos, BlockState state, String path) {
        super(WitcheryBlockEntities.fetish(path).get(), pos, state);
    }

    public static boolean enhancesPoppets(ServerPlayer player) {
        var chunks = player.serverLevel().getChunkSource();
        BlockPos pos = player.blockPosition();
        int maxX = SectionPos.blockToSectionCoord(pos.getX() + RANGE);
        int maxZ = SectionPos.blockToSectionCoord(pos.getZ() + RANGE);
        for (int x = SectionPos.blockToSectionCoord(pos.getX() - RANGE); x <= maxX; x++) {
            for (int z = SectionPos.blockToSectionCoord(pos.getZ() - RANGE); z <= maxZ; z++) {
                var chunk = chunks.getChunkNow(x, z);
                if (chunk == null) continue;
                for (BlockEntity entity : chunk.getBlockEntities().values()) {
                    if (!(entity instanceof FetishBlockEntity fetish) || fetish.isRemoved()
                            || fetish.data.getInt("EffectTypeID") != ENHANCED_POPPETS) continue;
                    BlockPos center = fetish.getBlockPos();
                    if (player.distanceToSqr(center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5) <= RANGE * RANGE) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void loadItem(ItemStack item) {
        CompoundTag tag = item.getTag();
        if (tag == null || !(tag.contains("TileData", Tag.TAG_COMPOUND) || tag.contains("BlockColor")
                || tag.contains("WITCSpiritEffect"))) return;
        data = copySubData(tag.getCompound("TileData"));
        if (!data.contains("BlockColor") && tag.contains("BlockColor")) data.putByte("BlockColor", tag.getByte("BlockColor"));
        if (data.getInt("EffectTypeID") == 0 && tag.contains("WITCSpiritEffect")) {
            data.putInt("EffectTypeID", tag.getInt("WITCSpiritEffect"));
        }
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }

    public void saveItem(ItemStack item) {
        if (data.isEmpty()) return;
        CompoundTag tag = item.getOrCreateTag();
        if (data.contains("BlockColor")) tag.putByte("BlockColor", data.getByte("BlockColor"));
        tag.putInt("WITCSpiritEffect", data.getInt("EffectTypeID"));
        tag.put("TileData", data.copy());
    }

    public boolean isSpectral() {
        return spectral;
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        data = copySubData(tag);
        spectral = tag.getBoolean("Spectral");
        lastActivation = tag.getLong("LastActivation");
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.merge(data);
        if (spectral) tag.putBoolean("Spectral", true);
        if (lastActivation != 0) tag.putLong("LastActivation", lastActivation);
    }

    private static CompoundTag copySubData(CompoundTag source) {
        CompoundTag result = new CompoundTag();
        for (String key : LEGACY_DATA) {
            Tag value = source.get(key);
            if (value != null) result.put(key, value.copy());
        }
        return result;
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("Spectral", spectral);
        return tag;
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
