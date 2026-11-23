package art.arcane.witchery.world.migration;

import com.mojang.authlib.GameProfile;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public final class LegacyGameProfiles {
    private LegacyGameProfiles() {
    }

    @Nullable
    public static GameProfile read(CompoundTag profile) {
        if (profile.contains("Id", Tag.TAG_STRING)) {
            try {
                var converted = profile.copy();
                converted.putUUID("Id", UUID.fromString(profile.getString("Id")));
                return NbtUtils.readGameProfile(converted);
            } catch (IllegalArgumentException ignored) {
                // A legacy profile can still identify its owner by name.
            }
        }
        return NbtUtils.readGameProfile(profile);
    }
}
