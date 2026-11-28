package art.arcane.witchery.world.migration;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

final class LegacyEmbeddedBlocks {
    private static final String SAVED_STATE = "WitcheryLegacyEmbeddedBlockState";

    private LegacyEmbeddedBlocks() {}

    static void prepare(CompoundTag carrier, LegacyWorldIds ids) {
        Payload payload = Payload.of(carrier);
        if (payload == null || hasCurrentState(carrier, payload)) return;
        CompoundTag state = decode(carrier, ids, payload);
        // Vanilla's fixer resolves numeric payloads against vanilla's block IDs.
        if (state != null) carrier.put(SAVED_STATE, state);
    }

    static void restoreAfterDataFix(CompoundTag carrier) {
        if (!(carrier.get(SAVED_STATE) instanceof CompoundTag state)) return;
        Payload payload = Payload.of(carrier);
        if (payload != null) carrier.put(payload.stateKey, state);
        carrier.remove(SAVED_STATE);
    }

    static void repairConverted(CompoundTag carrier, LegacyWorldIds ids) {
        Payload payload = Payload.of(carrier);
        if (payload == null || hasCurrentState(carrier, payload)) return;
        CompoundTag state = decode(carrier, ids, payload);
        if (state != null) carrier.put(payload.stateKey, state);
    }

    private static boolean hasCurrentState(CompoundTag carrier, Payload payload) {
        return carrier.getCompound(payload.stateKey).contains("Name", Tag.TAG_STRING);
    }

    @Nullable
    private static CompoundTag decode(CompoundTag carrier, LegacyWorldIds ids, Payload payload) {
        ResourceLocation name = switch (payload) {
            case FALLING -> fallingBlock(carrier, ids);
            case PISTON -> registeredBlock(carrier, ids, "blockId");
        };
        if (name == null) return null;
        var state = LegacySchematicBlocks.read(name.toString(), carrier.getInt(payload.metadataKey) & 15, 0);
        return state == null ? null : NbtUtils.writeBlockState(state);
    }

    @Nullable
    static ResourceLocation fallingBlock(CompoundTag carrier, LegacyWorldIds ids) {
        if (carrier.contains("TileID", Tag.TAG_ANY_NUMERIC)) return registeredBlock(carrier, ids, "TileID");
        if (carrier.contains("Tile", Tag.TAG_BYTE)) {
            return ids.block(Byte.toUnsignedInt(carrier.getByte("Tile")));
        }
        return null;
    }

    @Nullable
    private static ResourceLocation registeredBlock(CompoundTag carrier, LegacyWorldIds ids, String key) {
        return carrier.contains(key, Tag.TAG_ANY_NUMERIC) ? ids.block(carrier.getInt(key)) : null;
    }

    private enum Payload {
        FALLING("BlockState", "Data"),
        PISTON("blockState", "blockData");

        private final String stateKey;
        private final String metadataKey;

        Payload(String stateKey, String metadataKey) {
            this.stateKey = stateKey;
            this.metadataKey = metadataKey;
        }

        @Nullable
        private static Payload of(CompoundTag carrier) {
            return switch (carrier.getString("id")) {
                case "FallingSand", "minecraft:falling_block" -> FALLING;
                case "Piston", "minecraft:piston" -> PISTON;
                default -> null;
            };
        }
    }
}
