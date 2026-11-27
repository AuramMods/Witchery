package art.arcane.witchery.world.migration;

import art.arcane.witchery.registry.WitcheryEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.ByteTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.NumericTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public final class LegacyBoltAnchors {
    public static final String PENDING = "WITCBoltAnchor";
    public static final String POSITION = "WITCBoltPosition";
    private static final String SAVED_STATE = "WitcheryLegacyBoltState";

    private LegacyBoltAnchors() {}

    static void prepare(CompoundTag bolt, LegacyWorldIds ids, @Nullable CompoundTag originalChunk) {
        if (bolt.get("inTile") instanceof ByteTag number) {
            // The arrow data fixer expects an integer for the original unsigned block byte.
            int id = Byte.toUnsignedInt(number.getAsByte());
            var pos = savedPosition(bolt);
            if (originalChunk != null && bolt.getBoolean("inGround") && pos != null) {
                int saved = LegacyChunkUpgrade.savedBlockIdAndMetadata(originalChunk, pos);
                if (saved >= 0 && ((saved >> 4) & 255) == id && (saved & 15) == (bolt.getInt("inData") & 15)) {
                    id = saved >> 4;
                }
            }
            bolt.putInt("inTile", id);
        }
        repairAnchor(bolt, ids);
        // Preserve the resolved state across vanilla's flattening of the temporary Arrow schema.
        if (bolt.get("inBlockState") instanceof CompoundTag state) bolt.put(SAVED_STATE, state.copy());
    }

    static void finishImport(CompoundTag bolt) {
        if (bolt.get(SAVED_STATE) instanceof CompoundTag state) {
            bolt.put("inBlockState", state);
            bolt.remove(SAVED_STATE);
        } else if (bolt.contains(PENDING, Tag.TAG_COMPOUND)) {
            // Vanilla can synthesize an air state from a block ID its registry doesn't know.
            bolt.remove("inBlockState");
        }
    }

    static void repairStored(CompoundTag entity, @Nullable LegacyWorldIds ids) {
        if ((entity.contains("inTile") || entity.contains(PENDING, Tag.TAG_COMPOUND))
                && WitcheryEntities.BOLT.getId().equals(WitcheryEntities.legacyEntityId(entity.getString("id")))) {
            repairAnchor(entity, ids);
        }
    }

    private static void repairAnchor(CompoundTag bolt, @Nullable LegacyWorldIds ids) {
        if (!bolt.getBoolean("inGround")) return;
        if (bolt.contains("inBlockState", Tag.TAG_COMPOUND)) {
            bolt.remove(PENDING);
            return;
        }

        CompoundTag saved = bolt.getCompound(PENDING);
        Tag block = saved.get("Block");
        if (block == null) block = bolt.get("inTile");
        if (block == null) return;
        int metadata = (saved.contains("Metadata") ? saved.getInt("Metadata") : bolt.getInt("inData")) & 15;
        var pos = savedPosition(bolt);
        if (pos != null && !bolt.contains(POSITION, Tag.TAG_LONG)) bolt.putLong(POSITION, pos.asLong());
        BlockState state = read(block, metadata, ids);
        if (state != null) {
            bolt.put("inBlockState", NbtUtils.writeBlockState(state));
            bolt.remove(PENDING);
        } else {
            saved.put("Block", block.copy());
            saved.putInt("Metadata", metadata);
            bolt.put(PENDING, saved);
        }
    }

    @Nullable
    private static BlockPos savedPosition(CompoundTag bolt) {
        return bolt.contains("xTile", Tag.TAG_ANY_NUMERIC) && bolt.contains("yTile", Tag.TAG_ANY_NUMERIC)
                && bolt.contains("zTile", Tag.TAG_ANY_NUMERIC)
                ? new BlockPos(bolt.getInt("xTile"), bolt.getInt("yTile"), bolt.getInt("zTile")) : null;
    }

    @Nullable
    private static BlockState read(Tag block, int metadata, @Nullable LegacyWorldIds ids) {
        if (block.getId() == Tag.TAG_STRING) return LegacySchematicBlocks.read(block.getAsString(), metadata, 0);
        if (!(block instanceof NumericTag numeric)) return null;
        int id = block instanceof ByteTag ? Byte.toUnsignedInt(numeric.getAsByte()) : numeric.getAsInt();
        ResourceLocation name = ids == null ? null : ids.block(id);
        if (name != null) return LegacySchematicBlocks.read(name.toString(), metadata, 0);
        if (id < 0 || id > 255) return null;
        BlockState state = LegacySchematicBlocks.readVanilla((id << 4) | metadata);
        return state != null && (!state.isAir() || id == 0) ? state : null;
    }
}
