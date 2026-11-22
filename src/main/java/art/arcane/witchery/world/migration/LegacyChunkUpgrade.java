package art.arcane.witchery.world.migration;

import art.arcane.witchery.block.AltarBlock;
import art.arcane.witchery.block.DistilleryBlock;
import art.arcane.witchery.block.MirrorBlock;
import art.arcane.witchery.block.WitchCropBlock;
import art.arcane.witchery.block.WitchWoodType;
import art.arcane.witchery.block.WitchesOvenBlock;
import art.arcane.witchery.world.WitcheryDimensions;
import com.mojang.datafixers.DataFixer;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.datafix.PackedBitStorage;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
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
        CompoundTag inputLevel = input.getCompound("Level");
        ids.upgradeItems(inputLevel);
        restoreMirrorEntityTypes(inputLevel, ids);
        ids.upgradeTileInventories(inputLevel, fixer, from, to);
        LegacyChunkEntities.prepare(inputLevel, ids, fixer, from);
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

    public static void finish(CompoundTag upgraded, ResourceKey<Level> dimension) {
        LegacyChunkEntities.restore(upgraded);
        if (dimension.equals(WitcheryDimensions.TORMENT) || dimension.equals(WitcheryDimensions.MIRROR)) {
            // Both old generators wrote the vanilla Nether biome. Restore their dedicated modern biomes.
            var biome = new CompoundTag();
            var palette = new ListTag();
            palette.add(StringTag.valueOf(dimension.location().toString()));
            biome.put("palette", palette);
            for (Tag entry : upgraded.getList("sections", Tag.TAG_COMPOUND)) {
                ((CompoundTag) entry).put("biomes", biome.copy());
            }
        }
    }

    private static void restoreMirrorEntityTypes(CompoundTag level, LegacyWorldIds ids) {
        var sections = level.getList("Sections", Tag.TAG_COMPOUND);
        for (Tag entry : level.getList("TileEntities", Tag.TAG_COMPOUND)) {
            CompoundTag tile = (CompoundTag) entry;
            String id = tile.getString("id");
            if (!id.equals("witchery:mirrorblock") && !id.equals("witchery:mirrorblock2")) continue;

            // Both legacy mirror blocks use the same tile class and can share the same saved tile ID.
            int y = tile.getInt("y");
            int index = (y & 15) * 256 + (tile.getInt("z") & 15) * 16 + (tile.getInt("x") & 15);
            for (Tag value : sections) {
                CompoundTag section = (CompoundTag) value;
                if (section.getInt("Y") != (y >> 4)) continue;
                byte[] blocks = section.getByteArray("Blocks");
                if (blocks.length != SECTION_SIZE) break;
                var block = ids.block(blockNumber(blocks, section.getByteArray("Add"), index));
                if (block != null && (block.getPath().equals("mirrorblock") || block.getPath().equals("mirrorblock2"))) {
                    tile.putString("id", block.toString());
                }
                break;
            }
        }
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
            int number = blockNumber(blocks, highBits, i);
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

    private static int blockNumber(byte[] blocks, byte[] highBits, int index) {
        return (blocks[index] & 0xFF) | (nibble(highBits, index) << 8);
    }

    private static int nibble(byte[] bytes, int index) {
        int offset = index >> 1;
        return offset < bytes.length ? bytes[offset] >> ((index & 1) << 2) & 15 : 0;
    }

    @Nullable
    private static CompoundTag restoreState(ResourceLocation name, int metadata) {
        Block block = ForgeRegistries.BLOCKS.getValue(name);
        if (block == null) return null;
        if (block instanceof WitchCropBlock crop) return NbtUtils.writeBlockState(crop.getStateForAge(Math.min(metadata, crop.getMaxAge())));
        BlockState state = block.defaultBlockState();
        int variant = metadata & 3;
        WitchWoodType wood = switch (variant) {
            case 1 -> WitchWoodType.ALDER;
            case 2 -> WitchWoodType.HAWTHORN;
            default -> WitchWoodType.ROWAN;
        };
        BlockState restored = switch (name.getPath()) {
            case "witchesovenidle", "witchesovenburning" -> state.setValue(WitchesOvenBlock.FACING, horizontalFacing(metadata))
                    .setValue(WitchesOvenBlock.LIT, name.getPath().equals("witchesovenburning"));
            case "distilleryidle", "distilleryburning" -> state.setValue(BlockStateProperties.HORIZONTAL_FACING, horizontalFacing(metadata))
                    .setValue(DistilleryBlock.LIT, name.getPath().equals("distilleryburning"));
            case "kettle", "spinningwheel", "fumefunnel", "filteredfumefunnel" -> state.setValue(
                    BlockStateProperties.HORIZONTAL_FACING, horizontalFacing(metadata));
            case "altar" -> state.setValue(AltarBlock.FORMED, metadata == 1);
            case "mirrorblock", "mirrorblock2" -> state.setValue(MirrorBlock.FACING, switch (variant) {
                case 0 -> Direction.NORTH;
                case 1 -> Direction.SOUTH;
                case 2 -> Direction.WEST;
                default -> Direction.EAST;
            }).setValue(MirrorBlock.HALF, (metadata & 4) == 0 ? DoubleBlockHalf.LOWER : DoubleBlockHalf.UPPER);
            case "poppetshelf", "bloodedwool", "pitdirt", "pitgrass", "mirrorwall", "shadedglass", "shadedglass_active",
                    "tormentstone", "embermoss", "glintweed", "spanishmoss", "infinityegg", "icedoubleslab", "snowdoubleslab" -> state;
            case "iceslab", "snowslab" -> state.setValue(SlabBlock.TYPE, (metadata & 8) == 0 ? SlabType.BOTTOM : SlabType.TOP);
            case "icepressureplate", "snowpressureplate" -> state.setValue(BlockStateProperties.POWERED, metadata != 0);
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
            case "stairswoodrowan", "stairswoodalder", "stairswoodhawthorn", "icestairs", "snowstairs" -> state.setValue(StairBlock.FACING,
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

    private static Direction horizontalFacing(int metadata) {
        Direction facing = Direction.from3DDataValue(metadata);
        return facing.getAxis().isHorizontal() ? facing : Direction.NORTH;
    }
}
