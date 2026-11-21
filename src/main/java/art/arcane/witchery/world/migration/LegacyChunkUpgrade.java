package art.arcane.witchery.world.migration;

import art.arcane.witchery.block.WitchWoodType;
import art.arcane.witchery.block.WitchesOvenBlock;
import com.mojang.datafixers.DataFixer;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.datafix.PackedBitStorage;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class LegacyChunkUpgrade {
    private static final int SECTION_SIZE = 16 * 16 * 16;

    private LegacyChunkUpgrade() {
    }

    public static CompoundTag upgrade(DataFixTypes type, DataFixer fixer, CompoundTag original, int from, int to,
                                      LegacyWorldIds ids) {
        CompoundTag input = original.copy();
        ids.upgradeItems(input.getCompound("Level"));
        CompoundTag updated = type.update(fixer, input, from, to);

        // At 1493 vanilla has flattened the block arrays, but still uses the original section Ys.
        ListTag legacy = original.getCompound("Level").getList("Sections", Tag.TAG_COMPOUND);
        ListTag sections = updated.getCompound("Level").getList("Sections", Tag.TAG_COMPOUND);
        Map<Integer, CompoundTag> byHeight = new HashMap<>();
        for (Tag value : sections) {
            CompoundTag section = (CompoundTag) value;
            byHeight.put(section.getInt("Y"), section);
        }
        Map<Integer, Optional<CompoundTag>> states = new HashMap<>();
        for (Tag value : legacy) {
            CompoundTag section = (CompoundTag) value;
            CompoundTag flattened = byHeight.get(section.getInt("Y"));
            if (flattened != null) restoreSection(ids, section, flattened, states);
        }
        return updated;
    }

    private static void restoreSection(LegacyWorldIds ids, CompoundTag old, CompoundTag section,
                                       Map<Integer, Optional<CompoundTag>> states) {
        byte[] blocks = old.getByteArray("Blocks");
        if (blocks.length != SECTION_SIZE) return;
        byte[] highBits = old.getByteArray("Add");
        byte[] metadata = old.getByteArray("Data");
        ListTag palette = section.getList("Palette", Tag.TAG_COMPOUND);
        long[] entries = section.getLongArray("BlockStates");
        if (palette.isEmpty()) return;

        int oldBits = Math.max(4, Mth.ceillog2(palette.size()));
        PackedBitStorage packed = entries.length == 0 && palette.size() == 1
                ? new PackedBitStorage(oldBits, SECTION_SIZE) : new PackedBitStorage(oldBits, SECTION_SIZE, entries);
        boolean changed = false;
        for (int i = 0; i < SECTION_SIZE; i++) {
            int number = (blocks[i] & 0xFF) | (nibble(highBits, i) << 8);
            ResourceLocation name = ids.block(number);
            if (name == null) continue;
            int meta = nibble(metadata, i);
            Optional<CompoundTag> state = states.computeIfAbsent((number << 4) | meta,
                    ignored -> Optional.ofNullable(restoreState(name, meta)));
            if (state.isEmpty()) {
                ids.reportUnmappedBlock(name);
                continue;
            }
            int index = palette.indexOf(state.get());
            if (index < 0) {
                index = palette.size();
                palette.add(state.get());
                int newBits = Math.max(4, Mth.ceillog2(palette.size()));
                if (newBits != packed.getBits()) {
                    PackedBitStorage expanded = new PackedBitStorage(newBits, SECTION_SIZE);
                    for (int n = 0; n < SECTION_SIZE; n++) expanded.set(n, packed.get(n));
                    packed = expanded;
                }
            }
            packed.set(i, index);
            changed = true;
        }
        if (changed) {
            section.putLongArray("BlockStates", packed.getRaw());
            section.remove("BlockLight");
            section.remove("SkyLight");
        }
    }

    private static int nibble(byte[] bytes, int index) {
        int offset = index >> 1;
        return offset < bytes.length ? bytes[offset] >> ((index & 1) << 2) & 15 : 0;
    }

    @Nullable
    private static CompoundTag restoreState(ResourceLocation name, int metadata) {
        Block block = ForgeRegistries.BLOCKS.getValue(name);
        if (block == null) return null;
        BlockState state = block.defaultBlockState();
        int variant = metadata & 3;
        WitchWoodType wood = switch (variant) {
            case 1 -> WitchWoodType.ALDER;
            case 2 -> WitchWoodType.HAWTHORN;
            default -> WitchWoodType.ROWAN;
        };
        BlockState restored = switch (name.getPath()) {
            case "witchesovenidle", "witchesovenburning" -> {
                Direction facing = Direction.from3DDataValue(metadata);
                yield state.setValue(WitchesOvenBlock.FACING, facing.getAxis().isHorizontal() ? facing : Direction.NORTH)
                        .setValue(WitchesOvenBlock.LIT, name.getPath().equals("witchesovenburning"));
            }
            case "witchwood" -> state.setValue(WitchWoodType.PROPERTY, wood);
            case "witchlog" -> state.setValue(WitchWoodType.PROPERTY, wood).setValue(BlockStateProperties.AXIS,
                    switch (metadata & 12) {
                        case 4 -> Direction.Axis.X;
                        case 8 -> Direction.Axis.Z;
                        default -> Direction.Axis.Y;
                    });
            case "witchleaves" -> state.setValue(WitchWoodType.PROPERTY, wood)
                    .setValue(BlockStateProperties.PERSISTENT, (metadata & 4) != 0);
            case "witchsapling" -> state.setValue(WitchWoodType.PROPERTY, wood)
                    .setValue(BlockStateProperties.STAGE, (metadata & 8) == 0 ? 0 : 1);
            case "stairswoodrowan", "stairswoodalder", "stairswoodhawthorn" -> state.setValue(StairBlock.FACING,
                    switch (variant) {
                        case 0 -> Direction.EAST;
                        case 1 -> Direction.WEST;
                        case 2 -> Direction.SOUTH;
                        default -> Direction.NORTH;
                    }).setValue(StairBlock.HALF, (metadata & 4) == 0 ? Half.BOTTOM : Half.TOP);
            default -> null;
        };
        return restored == null ? null : NbtUtils.writeBlockState(restored);
    }
}
