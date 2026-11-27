package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.AlluringSkullBlock;
import art.arcane.witchery.block.AltarBlock;
import art.arcane.witchery.block.BloodRoseBlock;
import art.arcane.witchery.block.BrambleBlock;
import art.arcane.witchery.block.ChaliceBlock;
import art.arcane.witchery.block.CoffinBlock;
import art.arcane.witchery.block.CritterSnareBlock;
import art.arcane.witchery.block.CursedControls;
import art.arcane.witchery.block.DaylightCollectorBlock;
import art.arcane.witchery.block.DistilleryBlock;
import art.arcane.witchery.block.DreamCatcherBlock;
import art.arcane.witchery.block.DreamWeave;
import art.arcane.witchery.block.LegacyPairingChestBlock;
import art.arcane.witchery.block.MirrorBlock;
import art.arcane.witchery.block.MountedSkullBlock;
import art.arcane.witchery.block.PitDirtBlock;
import art.arcane.witchery.block.PoppetShelfBlock;
import art.arcane.witchery.block.PlantMineBlock;
import art.arcane.witchery.block.RiteHeartBlock;
import art.arcane.witchery.block.RitualGlyphBlock;
import art.arcane.witchery.block.ShadedGlassBlock;
import art.arcane.witchery.block.SpanishMossBlock;
import art.arcane.witchery.block.SpiritFluid;
import art.arcane.witchery.block.SpiritPortalBlock;
import art.arcane.witchery.block.StockadeBlock;
import art.arcane.witchery.block.SunlightBlock;
import art.arcane.witchery.block.TormentPortalBlock;
import art.arcane.witchery.block.WickerBundleBlock;
import art.arcane.witchery.block.WitchCropBlock;
import art.arcane.witchery.block.WitchLeavesBlock;
import art.arcane.witchery.block.WitchLilyPadBlock;
import art.arcane.witchery.block.WitchLogBlock;
import art.arcane.witchery.block.WitchWoodType;
import art.arcane.witchery.block.WitchesOvenBlock;
import art.arcane.witchery.block.WolfHeadBlock;
import art.arcane.witchery.block.entity.DreamCatcherBlockEntity;
import art.arcane.witchery.world.WitcheryDimensions;
import com.mojang.datafixers.DataFixer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.ShortTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.datafix.PackedBitStorage;
import net.minecraft.util.datafix.fixes.BlockStateData;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.FenceGateBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoorHingeSide;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public final class LegacyChunkUpgrade {
    private static final int SECTION_SIZE = 16 * 16 * 16;
    private static final int LEGACY_SECTIONS = 16;
    private static final Set<String> MIRRORS = Set.of("mirrorblock", "mirrorblock2");
    private static final Set<String> FETISHES = Set.of("scarecrow", "trent", "witchsladder");
    private static final Set<String> CURSED_CONTROLS = Set.of("clever", "cwoodendoor", "cwoodpressureplate", "cstonepressureplate",
            "csnowpressureplate", "cbuttonwood", "cbuttonstone");
    private static final Set<String> BREW_DISPERSALS = Set.of("brewgas", "brewliquid");
    private static final List<Set<String>> SHARED_TILE_TYPES = List.of(MIRRORS, FETISHES, BREW_DISPERSALS, CURSED_CONTROLS);
    private static final Set<String> LEGACY_SCHEDULED_BLOCKS = Set.of("lilypad", "infinityegg", "icepressureplate", "snowpressureplate");
    private static final String LEGACY_BLOCK_TICKS = "witchery:legacy_block_ticks";
    private static final String LEGACY_FLUID_TICKS = "witchery:legacy_fluid_ticks";
    private static final int LEGACY_LEVER_ID = 69;
    private static final int LEGACY_STONE_BUTTON_ID = 77;
    private static final int SKULL_TYPE_BIT = 16;
    private static final Set<String> SKULLS = Set.of("wolfhead", "alluringskull");
    private static final Set<String> TILE_STATE_BLOCKS = Set.of("coffinblock", "wolfhead", "alluringskull", "dreamcatcher");
    private static final Set<String> DOORS = Set.of("rowanwooddoor", "alderwooddoor", "icedoor", "cwoodendoor");
    private static final Map<String, Integer> FLUID_QUANTA = Map.of(
            "spiritflowing", SpiritFluid.QUANTA, "hollowtears", SpiritFluid.QUANTA, "brew", 2, "disease", 1);
    private static final Map<String, String> CLASSIC_FLUID_IDS = Map.of(
            "spiritflowing", "fluidspirit", "hollowtears", "hollowtears", "brew", "brew", "disease", "fluiddisease");
    private static final Set<String> POST_PROCESSING_BLOCKS = Set.of("stockade", "icestockade", "icefence", "icefencegate", "coffinblock",
            "tormentportal", "spiritportal", "leechchest", "refillingchest", "pitdirt", "pitgrass", "witchleaves",
            "stairswoodrowan", "stairswoodalder", "stairswoodhawthorn", "icestairs", "snowstairs");

    private LegacyChunkUpgrade() {
    }

    public static CompoundTag upgrade(DataFixTypes type, DataFixer fixer, CompoundTag original, int from, int to,
                                      LegacyWorldIds ids) {
        CompoundTag input = original.copy();
        CompoundTag inputLevel = input.getCompound("Level");
        restoreLegacyTileTypes(inputLevel, ids);
        ids.upgradeChunkItems(inputLevel, fixer, from, to);
        ListTag fluidTicks = new ListTag();
        ListTag savedTicks = restoreLegacyTicks(inputLevel, ids, fluidTicks);
        LegacyChunkEntities.prepare(inputLevel, ids, fixer, from);
        CompoundTag updated = type.update(fixer, input, from, to);

        // At 1493 vanilla has flattened the block arrays, but still uses the original section Ys.
        CompoundTag legacyLevel = original.getCompound("Level");
        ListTag legacy = legacyLevel.getList("Sections", Tag.TAG_COMPOUND);
        CompoundTag fixedLevel = updated.getCompound("Level");
        ListTag sections = fixedLevel.getList("Sections", Tag.TAG_COMPOUND);
        ListTag postProcessing = fixedLevel.getList("PostProcessing", Tag.TAG_LIST);
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
        Map<Integer, Integer> tileStates = savedBlockStates(legacyLevel);
        Map<Integer, Optional<CompoundTag>> states = new HashMap<>();
        List<BlockPos> sunlight = new ArrayList<>();
        legacyByHeight.forEach((height, section) -> {
            CompoundTag flattened = byHeight.get(height);
            if (flattened != null) restoreSection(ids, section, flattened, states, legacyByHeight, postProcessing, tileStates, sunlight);
        });
        var chunk = new ChunkPos(legacyLevel.getInt("xPos"), legacyLevel.getInt("zPos"));
        for (BlockPos relative : sunlight) {
            savedTicks.add(blockTick(Witchery.MODID + ":light", chunk.getBlockAt(relative.getX(), relative.getY(), relative.getZ()), 10));
        }
        if (!postProcessing.isEmpty()) fixedLevel.put("PostProcessing", postProcessing);
        if (!savedTicks.isEmpty()) fixedLevel.put(LEGACY_BLOCK_TICKS, savedTicks);
        if (!fluidTicks.isEmpty()) fixedLevel.put(LEGACY_FLUID_TICKS, fluidTicks);
        return updated;
    }

    public static void finish(CompoundTag upgraded, ResourceKey<Level> dimension) {
        LegacyChunkEntities.restore(upgraded.getList("entities", Tag.TAG_COMPOUND));
        restoreTicks(upgraded, LEGACY_BLOCK_TICKS, "block_ticks");
        restoreTicks(upgraded, LEGACY_FLUID_TICKS, "fluid_ticks");
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

    private static void restoreTicks(CompoundTag upgraded, String savedKey, String currentKey) {
        if (upgraded.get(savedKey) instanceof ListTag savedTicks) {
            var ticks = upgraded.getList(currentKey, Tag.TAG_COMPOUND);
            ticks.addAll(savedTicks);
            upgraded.put(currentKey, ticks);
            upgraded.remove(savedKey);
        }
    }

    public static void restorePendingLeafChecks(CompoundTag chunk) {
        for (Tag entry : chunk.getList("sections", Tag.TAG_COMPOUND)) {
            var section = (CompoundTag) entry;
            for (Tag value : section.getCompound("block_states").getList("palette", Tag.TAG_COMPOUND)) {
                var state = (CompoundTag) value;
                if (!state.getString("Name").equals("witchery:witchleaves")) continue;
                var properties = state.getCompound("Properties");
                String check = WitchLeavesBlock.CHECK_DECAY.getName();
                String distance = properties.getString("distance");
                // Earlier 1.20.1 builds ran vanilla decay for nonpersistent leaves at distance seven.
                if (!properties.contains(check) && !properties.getString("persistent").equals("true")
                        && (distance.isEmpty() || distance.equals("7"))) {
                    properties.putString(check, "true");
                    state.put("Properties", properties);
                }
            }
        }
    }

    private static void restoreLegacyTileTypes(CompoundTag level, LegacyWorldIds ids) {
        var sections = level.getList("Sections", Tag.TAG_COMPOUND);
        var tiles = level.getList("TileEntities", Tag.TAG_COMPOUND);
        updateBlockEntityTypes(tiles);
        for (Tag entry : tiles) {
            CompoundTag tile = (CompoundTag) entry;
            String tileId = tile.getString("id");
            if ((tileId.equals("witchery:leechchest") || tileId.equals("witchery:refillingchest"))
                    && tile.contains("CustomName", Tag.TAG_STRING)) {
                tile.putString("CustomName", Component.Serializer.toJson(Component.literal(tile.getString("CustomName"))));
            }
            Set<String> family = sharedTileFamily(tileId);
            if (family.isEmpty()) continue;

            // Each family registered one tile class for several different blocks.
            int y = tile.getInt("y");
            int index = (y & 15) * 256 + (tile.getInt("z") & 15) * 16 + (tile.getInt("x") & 15);
            for (Tag value : sections) {
                CompoundTag section = (CompoundTag) value;
                if (section.getInt("Y") != (y >> 4)) continue;
                byte[] blocks = section.getByteArray("Blocks");
                if (blocks.length != SECTION_SIZE) break;
                var block = ids.block(blockNumber(blocks, section.getByteArray("Add"), index));
                if (belongsTo(block, family)) {
                    tile.putString("id", block.toString());
                }
                break;
            }
        }
    }

    public static String restoreSharedTileType(String savedType, ResourceLocation block) {
        return belongsTo(block, sharedTileFamily(savedType)) ? block.toString() : savedType;
    }

    private static Set<String> sharedTileFamily(String savedType) {
        if (!savedType.startsWith(Witchery.MODID + ":")) return Set.of();
        String path = savedType.substring(Witchery.MODID.length() + 1);
        for (Set<String> family : SHARED_TILE_TYPES) {
            if (family.contains(path)) return family;
        }
        return Set.of();
    }

    public static void updateBlockEntityTypes(ListTag tiles) {
        // The decorative tiles were empty, and village wall plans were never saved in the marker.
        tiles.removeIf(entry -> switch (((CompoundTag) entry).getString("id")) {
            case "witchery:candelabra", "witchery:fumefunnel", "witchery:filteredfumefunnel",
                    "witchery:garlicgarland", "witchery:wallgen" -> true;
            default -> false;
        });
        for (Tag entry : tiles) {
            var tile = (CompoundTag) entry;
            switch (tile.getString("id")) {
                case "witchery:wolftrap" -> tile.putString("id", "witchery:beartrap");
                case "minecraft:mob_spawner" -> LegacyChunkEntities.restoreSpawner(tile);
            }
        }
    }

    private static boolean belongsTo(@Nullable ResourceLocation id, Set<String> paths) {
        return id != null && id.getNamespace().equals(Witchery.MODID) && paths.contains(id.getPath());
    }

    private static ListTag restoreLegacyTicks(CompoundTag level, LegacyWorldIds ids, ListTag fluidTicks) {
        var dormantBrews = new HashMap<BlockPos, String>();
        for (Tag entry : level.getList("TileEntities", Tag.TAG_COMPOUND)) {
            var tile = (CompoundTag) entry;
            var id = tile.getString("id");
            if (belongsTo(ResourceLocation.tryParse(id), BREW_DISPERSALS)) {
                dormantBrews.put(savedPosition(tile), id);
            }
        }
        var saved = new ListTag();
        // Vanilla's protochunk conversion retains tick locations and discards their block IDs, delays and priorities.
        level.getList("TileTicks", Tag.TAG_COMPOUND).removeIf(entry -> {
            var tick = (CompoundTag) entry;
            var name = tick.contains("i", Tag.TAG_ANY_NUMERIC) ? ids.block(tick.getInt("i"))
                    : ResourceLocation.tryParse(tick.getString("i"));
            if (belongsTo(name, CLASSIC_FLUID_IDS.keySet())) {
                int meta = fluidMetadata(level.getList("Sections", Tag.TAG_COMPOUND), savedPosition(tick), ids, name);
                boolean disease = name.getPath().equals("disease");
                if (meta >= 0 && (meta < FLUID_QUANTA.get(name.getPath()) || disease && meta == 1)) {
                    String fluidPath = CLASSIC_FLUID_IDS.get(name.getPath());
                    var fluidTick = tick.copy();
                    fluidTick.putString("i", Witchery.MODID + ":" + (meta == 0 ? "" : "flowing_") + fluidPath);
                    fluidTicks.add(fluidTick);
                    if (disease) {
                        tick.putString("i", name.toString());
                        saved.add(tick);
                    }
                }
                return true;
            }
            if (!belongsTo(name, CURSED_CONTROLS) && !belongsTo(name, BREW_DISPERSALS)
                    && !belongsTo(name, LEGACY_SCHEDULED_BLOCKS)) return false;
            tick.putString("i", name.toString());
            saved.add(tick);
            dormantBrews.remove(savedPosition(tick), name.toString());
            return true;
        });
        restoreUnstartedEggFalls(level.getList("Entities", Tag.TAG_COMPOUND), ids, saved);
        // Legacy runoff could stop scheduling when its level stabilized. Restart the modern dispersal tick on load.
        dormantBrews.forEach((pos, id) -> saved.add(blockTick(id, pos, 5)));
        return saved;
    }

    private static void restoreUnstartedEggFalls(ListTag entities, LegacyWorldIds ids, ListTag ticks) {
        String egg = LegacyChunkEntities.INFINITY_EGG.toString();
        entities.removeIf(entry -> {
            var entity = (CompoundTag) entry;
            if (entity.getInt("Time") != 0 || !LegacyChunkEntities.isFallingInfinityEgg(entity, ids)) return false;
            var position = entity.getList("Pos", Tag.TAG_DOUBLE);
            if (position.size() != 3) return false;
            var origin = BlockPos.containing(position.getDouble(0), position.getDouble(1), position.getDouble(2));
            // The old entity had not removed its source block yet. Schedule that block to fall once.
            var scheduled = ticks.stream().map(CompoundTag.class::cast)
                    .filter(tick -> tick.getString("i").equals(egg) && savedPosition(tick).equals(origin)).findFirst();
            if (scheduled.isPresent()) {
                var tick = scheduled.get();
                tick.putInt("t", Math.min(tick.getInt("t"), 1));
            } else {
                ticks.add(blockTick(egg, origin, 1));
            }
            return true;
        });
    }

    private static CompoundTag blockTick(String id, BlockPos pos, int delay) {
        var tick = new CompoundTag();
        tick.putString("i", id);
        tick.putInt("x", pos.getX());
        tick.putInt("y", pos.getY());
        tick.putInt("z", pos.getZ());
        tick.putInt("t", delay);
        tick.putInt("p", 0);
        return tick;
    }

    private static int fluidMetadata(ListTag sections, BlockPos pos, LegacyWorldIds ids, ResourceLocation name) {
        for (Tag value : sections) {
            CompoundTag section = (CompoundTag) value;
            if (section.getInt("Y") != pos.getY() >> 4) continue;
            int index = ((pos.getY() & 15) << 8) | ((pos.getZ() & 15) << 4) | (pos.getX() & 15);
            byte[] blocks = section.getByteArray("Blocks");
            if (blocks.length == SECTION_SIZE
                    && name.equals(ids.block(blockNumber(blocks, section.getByteArray("Add"), index)))) {
                return nibble(section.getByteArray("Data"), index);
            }
            break;
        }
        return -1;
    }

    private static BlockPos savedPosition(CompoundTag tag) {
        return new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
    }

    private static Map<Integer, Integer> savedBlockStates(CompoundTag level) {
        Map<Integer, Integer> states = new HashMap<>();
        for (Tag value : level.getList("TileEntities", Tag.TAG_COMPOUND)) {
            CompoundTag tile = (CompoundTag) value;
            int index = (tile.getInt("y") << 8) | ((tile.getInt("z") & 15) << 4) | (tile.getInt("x") & 15);
            ResourceLocation id = ResourceLocation.tryParse(tile.getString("id"));
            if (belongsTo(id, SKULLS)) {
                int skullType = tile.getInt("SkullType") == 1 ? SKULL_TYPE_BIT : 0;
                states.put(index, skullType | (tile.getInt("Rot") & 15));
            } else if (tile.getString("id").equals("witchery:coffinblock") && tile.getBoolean("Opened")) {
                // The lid must already be open when chunk post-processing loads the block entity.
                states.put(index, 1);
            } else if (tile.getString("id").equals("witchery:dreamcatcher")) {
                states.put(index, DreamCatcherBlockEntity.readWeave(tile, DreamWeave.MOVEMENT).ordinal());
            }
        }
        return states;
    }

    private static void restoreSection(LegacyWorldIds ids, CompoundTag old, CompoundTag section,
                                       Map<Integer, Optional<CompoundTag>> states, Map<Integer, CompoundTag> legacyByHeight,
                                       ListTag postProcessing, Map<Integer, Integer> tileStates, List<BlockPos> sunlight) {
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
        int sectionY = old.getInt("Y");
        for (int i = 0; i < SECTION_SIZE; i++) {
            int number = blockNumber(blocks, highBits, i);
            ResourceLocation name = ids.block(number);
            if (name == null) continue;
            int meta = nibble(metadata, i);
            String path = name.getPath();
            int saved;
            if (DOORS.contains(path)) {
                saved = doorMateMetadata(legacyByHeight, sectionY, i, number, meta);
            } else if (meta > 0 && FLUID_QUANTA.containsKey(path)) {
                int quanta = FLUID_QUANTA.get(path);
                int above = blockMetadata(legacyByHeight, sectionY, i + 256, number);
                saved = above >= 0 && above < quanta ? 1 : 0;
            } else {
                saved = TILE_STATE_BLOCKS.contains(path)
                        ? tileStates.getOrDefault((sectionY << 12) | i, 0) : 0;
            }
            Optional<CompoundTag> state = states.computeIfAbsent((number << 9) | (meta << 5) | saved,
                    ignored -> Optional.ofNullable(restoreState(name, meta, saved)));
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
            // Vanilla executes fluids in this list immediately, bypassing their saved delays.
            if (POST_PROCESSING_BLOCKS.contains(path)) queueShapeUpdate(postProcessing, sectionY, i);
            if (path.equals("light")) sunlight.add(new BlockPos(i & 15, (sectionY << 4) + (i >> 8), (i >> 4) & 15));
            changed = true;
        }
        if (changed) {
            section.putLongArray("BlockStates", packed.getRaw());
            section.remove("BlockLight");
            section.remove("SkyLight");
        }
    }

    private static void queueShapeUpdate(ListTag postProcessing, int sectionY, int index) {
        if (sectionY < 0 || sectionY >= LEGACY_SECTIONS) return;
        // Vanilla pads this list for the larger height and updates shapes after neighboring chunks are loaded.
        while (postProcessing.size() < LEGACY_SECTIONS) postProcessing.add(new ListTag());
        BlockPos local = new BlockPos(index & 15, (index >> 8) & 15, (index >> 4) & 15);
        postProcessing.getList(sectionY).add(ShortTag.valueOf(ProtoChunk.packOffsetCoordinates(local)));
    }

    private static int blockNumber(byte[] blocks, byte[] highBits, int index) {
        return (blocks[index] & 0xFF) | (nibble(highBits, index) << 8);
    }

    static int savedBlockIdAndMetadata(CompoundTag level, BlockPos pos) {
        if (!level.contains("xPos", Tag.TAG_ANY_NUMERIC) || !level.contains("zPos", Tag.TAG_ANY_NUMERIC)
                || (pos.getX() >> 4) != level.getInt("xPos") || (pos.getZ() >> 4) != level.getInt("zPos")) return -1;
        for (Tag value : level.getList("Sections", Tag.TAG_COMPOUND)) {
            CompoundTag section = (CompoundTag) value;
            if (section.getInt("Y") != pos.getY() >> 4) continue;
            byte[] blocks = section.getByteArray("Blocks");
            if (blocks.length != SECTION_SIZE) return -1;
            int index = ((pos.getY() & 15) << 8) | ((pos.getZ() & 15) << 4) | (pos.getX() & 15);
            return (blockNumber(blocks, section.getByteArray("Add"), index) << 4)
                    | nibble(section.getByteArray("Data"), index);
        }
        return -1;
    }

    private static int nibble(byte[] bytes, int index) {
        int offset = index >> 1;
        return offset < bytes.length ? bytes[offset] >> ((index & 1) << 2) & 15 : 0;
    }

    private static int doorMateMetadata(Map<Integer, CompoundTag> sections, int height, int index, int id, int meta) {
        boolean upper = (meta & 8) != 0;
        int otherMeta = blockMetadata(sections, height, index + (upper ? -256 : 256), id);
        return otherMeta >= 0 && ((otherMeta ^ meta) & 8) != 0 ? otherMeta : 0;
    }

    private static int blockMetadata(Map<Integer, CompoundTag> sections, int height, int index, int id) {
        CompoundTag section = sections.get(height + Math.floorDiv(index, SECTION_SIZE));
        if (section != null) {
            int otherIndex = Math.floorMod(index, SECTION_SIZE);
            byte[] blocks = section.getByteArray("Blocks");
            if (blocks.length == SECTION_SIZE && blockNumber(blocks, section.getByteArray("Add"), otherIndex) == id) {
                return nibble(section.getByteArray("Data"), otherIndex);
            }
        }
        return -1;
    }

    @Nullable
    static CompoundTag restoreState(ResourceLocation name, int metadata, int saved) {
        Block block = ForgeRegistries.BLOCKS.getValue(name);
        if (block == null) return null;
        if (block instanceof WitchCropBlock crop) return NbtUtils.writeBlockState(crop.getStateForAge(Math.min(metadata, crop.getMaxAge())));
        if (block instanceof SpanishMossBlock moss) return NbtUtils.writeBlockState(moss.stateFromLegacy(metadata));
        if (block instanceof CursedControls.Lever) return restoreMountedControl(name, LEGACY_LEVER_ID, metadata);
        if (block instanceof CursedControls.Button) return restoreMountedControl(name, LEGACY_STONE_BUTTON_ID, metadata);
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
            case "kettle", "spinningwheel", "fumefunnel", "filteredfumefunnel", "wolfaltar", "beartrap", "wolftrap",
                    "garlicgarland", "decursedirected", "decurseteleport", "demonheart",
                    "statuegoddess" ->
                    state.setValue(BlockStateProperties.HORIZONTAL_FACING, horizontalFacing(metadata));
            case "dreamcatcher" -> state.setValue(DreamCatcherBlock.FACING, horizontalFacing(metadata))
                    .setValue(DreamCatcherBlock.WEAVE, DreamWeave.fromLegacyId(saved));
            // Both original chest renderers drew default metadata zero facing south.
            case "leechchest", "refillingchest" -> state.setValue(LegacyPairingChestBlock.FACING,
                            metadata == 0 ? Direction.SOUTH : horizontalFacing(metadata))
                    .setValue(LegacyPairingChestBlock.LEGACY_PAIRING, true);
            case "statueofworship" -> state.setValue(BlockStateProperties.HORIZONTAL_FACING, horizontalFacing(metadata & 7));
            case "vine" -> state.setValue(BlockStateProperties.HORIZONTAL_FACING, horizontalFacing(metadata));
            case "bramble" -> state.setValue(BrambleBlock.WILD, metadata == 1);
            case "plantmine" -> state.setValue(PlantMineBlock.VARIANT, PlantMineBlock.Variant.fromLegacyMetadata(metadata));
            case "crittersnare" -> {
                var critter = CritterSnareBlock.Critter.fromLegacyMetadata(metadata);
                yield state.setValue(CritterSnareBlock.CRITTER, critter)
                        .setValue(CritterSnareBlock.STOCKED, critter == CritterSnareBlock.Critter.BAT && (metadata & 8) != 0);
            }
            case "lilypad" -> state.setValue(WitchLilyPadBlock.GROWTH, (metadata >> 2) & 3);
            case "grassper", "placeditem", "scarecrow", "trent", "witchsladder" -> state.setValue(
                    BlockStateProperties.HORIZONTAL_FACING, horizontalFacing(metadata & 7));
            case "altar" -> state.setValue(AltarBlock.FORMED, metadata == 1);
            case "chalice" -> state.setValue(ChaliceBlock.FILLED, metadata == 1);
            case "bloodrose" -> state.setValue(BloodRoseBlock.FULL, metadata == 1);
            case "daylightcollector" -> state.setValue(DaylightCollectorBlock.CHARGE, metadata);
            case "circle" -> state.setValue(RiteHeartBlock.ACTIVE, metadata == 1);
            case "circleglyphritual", "circleglyphotherwhere", "circleglyphinfernal" -> state.setValue(
                    RitualGlyphBlock.PATTERN, Math.min(metadata, RitualGlyphBlock.PATTERN_COUNT - 1));
            case "wickerbundle" -> state.setValue(WickerBundleBlock.BUNDLE_TYPE,
                    (metadata & 1) == 0 ? WickerBundleBlock.Type.PLAIN : WickerBundleBlock.Type.BLOODIED)
                    .setValue(BlockStateProperties.AXIS, pillarAxis(metadata));
            case "rowanwooddoor", "alderwooddoor", "icedoor", "cwoodendoor" -> restoreDoor(state, metadata, saved);
            case "coffinblock" -> state.setValue(CoffinBlock.FACING, Direction.from2DDataValue(variant))
                    .setValue(CoffinBlock.PART, (metadata & 8) == 0 ? BedPart.FOOT : BedPart.HEAD)
                    .setValue(CoffinBlock.OCCUPIED, (metadata & 4) != 0)
                    .setValue(CoffinBlock.OPEN, saved != 0);
            case "wolfhead" -> restoreMountedSkull(state, metadata, saved)
                    .setValue(WolfHeadBlock.HELLHOUND, (saved & SKULL_TYPE_BIT) != 0);
            case "alluringskull" -> restoreMountedSkull(state, metadata, saved)
                    .setValue(AlluringSkullBlock.ACTIVE, (saved & SKULL_TYPE_BIT) != 0);
            case "stockade" -> state.setValue(StockadeBlock.WOOD, StockadeBlock.Wood.fromLegacyMetadata(metadata));
            case "icefencegate" -> state.setValue(FenceGateBlock.FACING, Direction.from2DDataValue(variant))
                    .setValue(FenceGateBlock.OPEN, (metadata & 4) != 0)
                    .setValue(FenceGateBlock.POWERED, (metadata & 8) != 0);
            case "tormentportal" -> state.setValue(TormentPortalBlock.LEGACY_ORIENTATION, true);
            case "spiritportal" -> state.setValue(SpiritPortalBlock.LEGACY_ORIENTATION, true);
            case "spiritflowing", "hollowtears" -> metadata >= SpiritFluid.QUANTA ? Blocks.AIR.defaultBlockState()
                    : state.setValue(LiquidBlock.LEVEL, SpiritFluid.legacyBlockLevel(metadata, saved != 0));
            case "brew", "disease" ->
                    restoreClassicFluid(state, metadata, FLUID_QUANTA.get(name.getPath()), saved != 0);
            case "brewgas", "brewliquid" -> state.setValue(LiquidBlock.LEVEL, metadata);
            case "light" -> state.setValue(SunlightBlock.TEMPORARY, true);
            case "wallgen" -> Blocks.AIR.defaultBlockState();
            case "shadedglass", "shadedglass_active" -> state.setValue(ShadedGlassBlock.COLOR,
                    ShadedGlassBlock.Color.fromLegacyMetadata(metadata));
            case "mirrorblock", "mirrorblock2" -> state.setValue(MirrorBlock.FACING, switch (variant) {
                case 0 -> Direction.NORTH;
                case 1 -> Direction.SOUTH;
                case 2 -> Direction.WEST;
                default -> Direction.EAST;
            }).setValue(MirrorBlock.HALF, (metadata & 4) == 0 ? DoubleBlockHalf.LOWER : DoubleBlockHalf.UPPER);
            case "pitdirt" -> state.setValue(PitDirtBlock.PODZOL, metadata == 2);
            case "poppetshelf" -> state.setValue(PoppetShelfBlock.FACING,
                            (metadata & 7) < 6 ? Direction.from3DDataValue(metadata & 7) : Direction.SOUTH)
                    .setValue(PoppetShelfBlock.POWERED, (metadata & 8) != 0);
            case "bloodedwool", "pitgrass", "mirrorwall",
                    "tormentstone", "embermoss", "glintweed", "infinityegg", "icedoubleslab", "snowdoubleslab",
                    "cauldron", "brazier", "candelabra", "perpetualice", "icestockade", "icefence", "bloodcrucible", "silvervat",
                    "force", "barrier", "slurp", "somniancotton", "web", "cactus", "glowglobe", "leapinglily", "crystalball",
                    "voidbramble" -> state;
            case "iceslab", "snowslab" -> state.setValue(SlabBlock.TYPE, (metadata & 8) == 0 ? SlabType.BOTTOM : SlabType.TOP);
            case "witchwoodslab" -> state.setValue(WitchWoodType.PROPERTY, wood)
                    .setValue(SlabBlock.TYPE, (metadata & 8) == 0 ? SlabType.BOTTOM : SlabType.TOP);
            case "witchwooddoubleslab" -> state.setValue(WitchWoodType.PROPERTY, wood);
            case "icepressureplate", "snowpressureplate" -> state.setValue(BlockStateProperties.POWERED, metadata != 0);
            case "cwoodpressureplate", "cstonepressureplate", "csnowpressureplate" ->
                    state.setValue(BlockStateProperties.POWERED, metadata == 1);
            case "witchwood" -> state.setValue(WitchWoodType.PROPERTY, wood);
            case "witchlog" -> state.setValue(WitchWoodType.PROPERTY, wood)
                    .setValue(WitchLogBlock.AXIS, pillarAxis(metadata))
                    .setValue(WitchLogBlock.BARK, (metadata & 12) == 12);
            case "witchleaves" -> state.setValue(WitchWoodType.PROPERTY, wood)
                    // Retain vanilla distance updates for mixed canopies.
                    .setValue(BlockStateProperties.DISTANCE, 1)
                    .setValue(BlockStateProperties.PERSISTENT, (metadata & 4) != 0)
                    .setValue(WitchLeavesBlock.CHECK_DECAY, (metadata & 8) != 0);
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

    private static CompoundTag restoreMountedControl(ResourceLocation name, int vanillaId, int metadata) {
        // The cursed blocks copied the original lever and button placement metadata.
        var vanilla = (CompoundTag) BlockStateData.getTag((vanillaId << 4) | metadata).convert(NbtOps.INSTANCE).getValue();
        var restored = vanilla.copy();
        restored.putString("Name", name.toString());
        return restored;
    }

    private static BlockState restoreMountedSkull(BlockState state, int metadata, int saved) {
        int side = metadata & 7;
        Direction facing = side >= 2 && side <= 5 ? Direction.from3DDataValue(side) : Direction.UP;
        return state.setValue(MountedSkullBlock.FACING, facing)
                .setValue(MountedSkullBlock.ROTATION, facing == Direction.UP ? saved & 15 : 0);
    }

    private static BlockState restoreClassicFluid(BlockState state, int metadata, int quanta, boolean falling) {
        if (metadata >= quanta && (quanta != 1 || metadata != 1)) return Blocks.AIR.defaultBlockState();
        int level = metadata != 0 && (quanta == 1 || falling) ? 8 : Math.round(metadata * (8.0F / quanta));
        return state.setValue(LiquidBlock.LEVEL, level);
    }

    static BlockState restoreDoor(BlockState state, int metadata, int mate) {
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

    private static Direction.Axis pillarAxis(int metadata) {
        return switch (metadata & 12) {
            case 4 -> Direction.Axis.X;
            case 8 -> Direction.Axis.Z;
            default -> Direction.Axis.Y;
        };
    }
}
