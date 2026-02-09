package art.arcane.witchery.capability;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import org.jetbrains.annotations.Nullable;

public final class WitcheryPlayerDataProvider implements ICapabilitySerializable<CompoundTag> {
    private final WitcheryPlayerData data = new WitcheryPlayerData();
    private final LazyOptional<WitcheryPlayerData> optional = LazyOptional.of(() -> data);

    private WitcheryPlayerDataProvider() {
    }

    public static void attach(AttachCapabilitiesEvent<Entity> event) {
        if (!(event.getObject() instanceof Player)) {
            return;
        }

        WitcheryPlayerDataProvider provider = new WitcheryPlayerDataProvider();
        event.addCapability(WitcheryCapabilities.playerDataId(), provider);
        event.addListener(provider::invalidate);
    }

    public static LazyOptional<WitcheryPlayerData> get(Player player) {
        return player.getCapability(WitcheryCapabilities.PLAYER_DATA);
    }

    private void invalidate() {
        optional.invalidate();
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, @Nullable Direction side) {
        if (cap == WitcheryCapabilities.PLAYER_DATA) {
            return optional.cast();
        }
        return LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        return data.serializeNBT();
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        data.deserializeNBT(nbt);
    }
}
