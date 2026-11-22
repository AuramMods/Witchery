package art.arcane.witchery.world.migration;

import art.arcane.witchery.block.AltarBlock;
import art.arcane.witchery.block.ChaliceBlock;
import art.arcane.witchery.block.DistilleryBlock;
import art.arcane.witchery.block.MirrorBlock;
import art.arcane.witchery.block.RiteHeartBlock;
import art.arcane.witchery.block.RitualGlyphBlock;
import art.arcane.witchery.block.WickerBundleBlock;
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
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class LegacyChunkUpgrade {
    private static final int SECTION_SIZE = 16 * 16 * 16;
    private static final Set<String> MIRRORS = Set.of("mirrorblock", "mirrorblock2");
    private static final Set<String> FETISHES = Set.of("scarecrow", "trent", "witchsladder");
    private static final Set<String> DOORS = Set.of("rowanwooddoor", "alderwooddoor", "icedoor");

    private LegacyChunkUpgrade() {
    }

    public static CompoundTag upgrade(DataFixTypes type, DataFixer fixer, CompoundTag original, int from, int to,
                                      LegacyWorldIds ids) {
        CompoundTag input = original.copy();
        CompoundTag inputLevel = input.getCompound("Level");
        ids.upgradeItems(inputLevel);
        restoreSharedTileTypes(inputLevel, ids);
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
        Map<Integer, CompoundTag> legacyByHeight = new HashMap<>();
        for (Tag value : legacy) {
            CompoundTag section = (CompoundTag) value;
            legacyByHeight.put(section.getInt("Y"), section);
        }
        Map<Integer, Optional<CompoundTag>> states = new HashMap<>();
        legacyByHeight.forEach((height, section) -> {
            CompoundTag flattened = byHeight.get(height);
            if (flattened != null) restoreSection(ids, section, flattened, states, legacyByHeight);
        });
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

    private static void restoreSharedTileTypes(CompoundTag level, LegacyWorldIds ids) {
        var sections = level.getList("Sections", Tag.TAG_COMPOUND);
        for (Tag entry : level.getList("TileEntities", Tag.TAG_COMPOUND)) {
            CompoundTag tile = (CompoundTag) entry;
            Set<String> family = switch (tile.getString("id")) {
                case "witchery:mirrorblock", "witchery:mirrorblock2" -> MIRRORS;
                case "witchery:scarecrow", "witchery:trent", "witchery:witchsladder" -> FETISHES;
                default -> Set.of();
            };
            if (family.isEmpty()) continue;

            // Legacy mirrors and fetishes each used one tile class for several different blocks.
            int y = tile.getInt("y");
            int index = (y & 15) * 256 + (tile.getInt("z") & 15) * 16 + (tile.getInt("x") & 15);
            for (Tag value : sections) {
                CompoundTag section = (CompoundTag) value;
                if (section.getInt("Y") != (y >> 4)) continue;
                byte[] blocks = section.getByteArray("Blocks");
                if (blocks.length != SECTION_SIZE) break;
                var block = ids.block(blockNumber(blocks, section.getByteArray("Add"), index));
                if (block != null && family.contains(block.getPath())) {
                    tile.putString("id", block.toString());
                }
                break;
            }
        }
    }

    private static void restoreSection(LegacyWorldIds ids, CompoundTag old, CompoundTag section,
                                       Map<Integer, Optional<CompoundTag>> states, Map<Integer, CompoundTag> legacyByHeight) {
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
            int mate = DOORS.contains(name.getPath()) ? doorMateMetadata(legacyByHeight, old.getInt("Y"), i, number, meta) : 0;
            Optional<CompoundTag> state = states.computeIfAbsent((number << 8) | (meta << 4) | mate,
                    ignored -> Optional.ofNullable(restoreState(name, meta, mate)));
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

    private static int doorMateMetadata(Map<Integer, CompoundTag> sections, int height, int index, int id, int meta) {
        boolean upper = (meta & 8) != 0;
        int across = index + (upper ? -256 : 256);
        CompoundTag section = sections.get(height + Math.floorDiv(across, SECTION_SIZE));
        if (section != null) {
            int otherIndex = Math.floorMod(across, SECTION_SIZE);
            byte[] blocks = section.getByteArray("Blocks");
            if (blocks.length == SECTION_SIZE && blockNumber(blocks, section.getByteArray("Add"), otherIndex) == id) {
                int otherMeta = nibble(section.getByteArray("Data"), otherIndex);
                if (((otherMeta ^ meta) & 8) != 0) return otherMeta;
            }
        }
        return 0;
    }

    @Nullable
    private static CompoundTag restoreState(ResourceLocation name, int metadata, int mate) {
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
            case "grassper", "placeditem", "scarecrow", "trent", "witchsladder" -> state.setValue(
                    BlockStateProperties.HORIZONTAL_FACING, horizontalFacing(metadata & 7));
            case "altar" -> state.setValue(AltarBlock.FORMED, metadata == 1);
            case "chalice" -> state.setValue(ChaliceBlock.FILLED, metadata == 1);
            case "circle" -> state.setValue(RiteHeartBlock.ACTIVE, metadata == 1);
            case "circleglyphritual", "circleglyphotherwhere", "circleglyphinfernal" -> state.setValue(
                    RitualGlyphBlock.PATTERN, Math.min(metadata, RitualGlyphBlock.PATTERN_COUNT - 1));
            case "wickerbundle" -> state.setValue(WickerBundleBlock.BUNDLE_TYPE,
                    (metadata & 1) == 0 ? WickerBundleBlock.Type.PLAIN : WickerBundleBlock.Type.BLOODIED);
            case "rowanwooddoor", "alderwooddoor", "icedoor" -> restoreDoor(state, metadata, mate);
            case "mirrorblock", "mirrorblock2" -> state.setValue(MirrorBlock.FACING, switch (variant) {
                case 0 -> Direction.NORTH;
                case 1 -> Direction.SOUTH;
                case 2 -> Direction.WEST;
                default -> Direction.EAST;
            }).setValue(MirrorBlock.HALF, (metadata & 4) == 0 ? DoubleBlockHalf.LOWER : DoubleBlockHalf.UPPER);
            case "poppetshelf", "bloodedwool", "pitdirt", "pitgrass", "mirrorwall", "shadedglass", "shadedglass_active",
                    "tormentstone", "embermoss", "glintweed", "spanishmoss", "infinityegg", "icedoubleslab", "snowdoubleslab",
                    "cauldron", "brazier", "candelabra", "perpetualice" -> state;
            case "iceslab", "snowslab" -> state.setValue(SlabBlock.TYPE, (metadata & 8) == 0 ? SlabType.BOTTOM : SlabType.TOP);
            case "witchwoodslab" -> state.setValue(WitchWoodType.PROPERTY, wood)
                    .setValue(SlabBlock.TYPE, (metadata & 8) == 0 ? SlabType.BOTTOM : SlabType.TOP);
            case "witchwooddoubleslab" -> state.setValue(WitchWoodType.PROPERTY, wood);
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

    private static BlockState restoreDoor(BlockState state, int metadata, int mate) {
        boolean isUpper = (metadata & 8) != 0;
        int lower = isUpper ? mate : metadata;
        int upper = isUpper ? metadata : mate;
        return state.setValue(DoorBlock.FACING, Direction.from2DDataValue((lower + 3) & 3))
                .setValue(DoorBlock.HALF, isUpper ? DoubleBlockHalf.UPPER : DoubleBlockHalf.LOWER)
                .setValue(DoorBlock.OPEN, (lower & 4) != 0)
                .setValue(DoorBlock.HINGE, (upper & 1) != 0 ? DoorHingeSide.RIGHT : DoorHingeSide.LEFT)
                .setValue(DoorBlock.POWERED, (upper & 2) != 0);
    }

    private static Direction horizontalFacing(int metadata) {
        Direction facing = Direction.from3DDataValue(metadata);
        return facing.getAxis().isHorizontal() ? facing : Direction.NORTH;
    }
}
