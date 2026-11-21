package art.arcane.witchery.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.storage.PrimaryLevelData;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(PrimaryLevelData.class)
public interface LegacyPrimaryLevelDataAccessor {
    @Accessor("loadedPlayerTag")
    @Nullable CompoundTag witchery$getRawPlayerTag();

    @Accessor("playerDataVersion")
    int witchery$getPlayerDataVersion();
}
