package art.arcane.witchery.capability;

import net.minecraft.nbt.CompoundTag;

public final class WitcheryPlayerData {
    private static final String KEY_INITIALIZED = "Initialized";
    private static final String KEY_SYNC_REVISION = "SyncRevision";

    private boolean initialized;
    private int syncRevision;

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    public int getSyncRevision() {
        return syncRevision;
    }

    public void bumpSyncRevision() {
        syncRevision++;
    }

    public void copyFrom(WitcheryPlayerData source) {
        initialized = source.initialized;
        syncRevision = source.syncRevision;
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(KEY_INITIALIZED, initialized);
        tag.putInt(KEY_SYNC_REVISION, syncRevision);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        initialized = tag.getBoolean(KEY_INITIALIZED);
        syncRevision = tag.getInt(KEY_SYNC_REVISION);
    }
}
