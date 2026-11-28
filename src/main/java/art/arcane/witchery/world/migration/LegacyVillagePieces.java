package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.world.WitcheryDimensions;
import art.arcane.witchery.world.WitcheryVillagePools;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.chunk.ImposterProtoChunk;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.JigsawReplacementProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.LevelResource;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

public final class LegacyVillagePieces {
    private static final String SAVE_FILE = "data/Village.dat";
    private static final int ORIGINAL_PLAN_Y = 74;
    private static final Plan EMPTY = new Plan(Map.of(), Set.of());
    private static final Map<ServerLevel, Plan> LOADED = Collections.synchronizedMap(new WeakHashMap<>());

    private LegacyVillagePieces() {}

    public static void place(WorldGenRegion region, ChunkAccess chunk, ChunkGenerator generator) {
        if (!(chunk instanceof ProtoChunk) || chunk instanceof ImposterProtoChunk) return;
        var level = region.getLevel();
        if (!level.dimension().equals(Level.OVERWORLD) && !level.dimension().equals(WitcheryDimensions.DREAM)) return;
        var pos = chunk.getPos();
        var plan = LOADED.computeIfAbsent(level, LegacyVillagePieces::read);
        var pieces = plan.chunks().get(pos.toLong());
        if (pieces == null) return;
        var clips = unpopulatedArea(pos, region.getMinBuildHeight(), region.getMaxBuildHeight() - 1, plan.populated());
        for (var piece : pieces) piece.place(region, generator, clips);
    }

    static List<BoundingBox> unpopulatedArea(ChunkPos chunk, int bottom, int top, Set<Long> populated) {
        int minX = chunk.getMinBlockX();
        int minZ = chunk.getMinBlockZ();
        var full = new BoundingBox(minX, bottom, minZ, chunk.getMaxBlockX(), top, chunk.getMaxBlockZ());
        if (populated.isEmpty()) return List.of(full);
        var clips = new ArrayList<BoundingBox>(4);
        for (int x = minX; x < minX + 16; x += 8) {
            for (int z = minZ; z < minZ + 16; z += 8) {
                if (!populated.contains(LegacyVillagePopulation.owner(x, z).toLong())) {
                    clips.add(new BoundingBox(x, bottom, z, x + 7, top, z + 7));
                }
            }
        }
        return clips.size() == 4 ? List.of(full) : clips;
    }

    static void capturePopulation(Path root) throws IOException {
        for (var key : List.of(Level.OVERWORLD, WitcheryDimensions.DREAM)) {
            var dimension = DimensionType.getStorageFolder(key, root);
            var file = dimension.resolve(SAVE_FILE);
            if (Files.isRegularFile(file) && !LegacyVillagePopulation.hasSnapshot(dimension)) {
                var features = NbtIo.readCompressed(file.toFile()).getCompound("data").getCompound("Features");
                LegacyVillagePopulation.capture(dimension, load(features).keySet());
            }
        }
    }

    private static Plan read(ServerLevel level) {
        var root = level.getServer().getWorldPath(LevelResource.ROOT);
        var dimension = DimensionType.getStorageFolder(level.dimension(), root);
        var file = dimension.resolve(SAVE_FILE);
        if (!Files.isRegularFile(file) || LegacyWorldIds.find(root) == null) return EMPTY;
        try {
            var features = NbtIo.readCompressed(file.toFile()).getCompound("data").getCompound("Features");
            return new Plan(load(features), LegacyVillagePopulation.read(dimension));
        } catch (IOException exception) {
            Witchery.LOGGER.warn("Could not read pending legacy village pieces from {}", file, exception);
            return EMPTY;
        }
    }

    private record Plan(Map<Long, List<Piece>> chunks, Set<Long> populated) {}

    static Map<Long, List<Piece>> load(CompoundTag features) {
        Map<Long, List<Piece>> chunks = new HashMap<>();
        for (String key : features.getAllKeys().stream().sorted().toList()) {
            var start = features.getCompound(key);
            if (!start.getBoolean("Valid")) continue;
            for (Tag child : start.getList("Children", Tag.TAG_COMPOUND)) {
                var piece = Piece.read((CompoundTag) child);
                if (piece == null) continue;
                var box = piece.bounds();
                for (int x = (box.minX() - 1) >> 4; x <= (box.maxX() + 1) >> 4; x++) {
                    for (int z = (box.minZ() - 1) >> 4; z <= (box.maxZ() + 1) >> 4; z++) {
                        chunks.computeIfAbsent(ChunkPos.asLong(x, z), ignored -> new ArrayList<>()).add(piece);
                    }
                }
            }
        }
        chunks.replaceAll((pos, pieces) -> List.copyOf(pieces));
        return Map.copyOf(chunks);
    }

    enum Building {
        APOTHECARY(WitcheryVillagePools.APOTHECARY, 1),
        WITCH_HUT(WitcheryVillagePools.WITCH_HUT, 1),
        BOOKSHOP(WitcheryVillagePools.BOOKSHOP, 0),
        KEEP(WitcheryVillagePools.KEEP, 0),
        WATCHTOWER(WitcheryVillagePools.WATCHTOWER, 0);

        private final ResourceLocation template;
        private final int approach;

        Building(ResourceLocation template, int approach) {
            this.template = template;
            this.approach = approach;
        }

        @Nullable
        static Building fromId(String id) {
            return switch (id.toLowerCase(Locale.ROOT)) {
                case "witchery:apothecary" -> APOTHECARY;
                case "witchery:witchhut" -> WITCH_HUT;
                case "witchery:bookshop" -> BOOKSHOP;
                case "witchery:villagekeep" -> KEEP;
                case "witchery:villagewatchtower" -> WATCHTOWER;
                default -> null;
            };
        }
    }

    record Piece(Building building, BoundingBox bounds, int orientation, boolean desert, boolean chestPlaced,
                 int residentsPlaced, boolean grounded, boolean tallHut, boolean furnishedHut) {
        @Nullable
        static Piece read(CompoundTag saved) {
            var building = Building.fromId(saved.getString("id"));
            int[] box = saved.getIntArray("BB");
            int direction = saved.getInt("O");
            if (building == null || box.length != 6 || direction < 0 || direction > 3
                    || (long) box[3] - box[0] < 0 || (long) box[3] - box[0] >= 16
                    || (long) box[5] - box[2] < 0 || (long) box[5] - box[2] >= 16 || box[4] < box[1]) return null;
            boolean chest = saved.getBoolean(building == Building.APOTHECARY ? "WITCApocChest" : "Chest");
            int residents = switch (building) {
                case APOTHECARY -> saved.getInt("VCount");
                case WITCH_HUT -> saved.getInt("WITCWCount");
                case KEEP, WATCHTOWER -> saved.getInt("Guards");
                case BOOKSHOP -> 0;
            };
            boolean grounded = saved.contains("HPos", Tag.TAG_ANY_NUMERIC) && saved.getInt("HPos") >= 0;
            // The Apothecary kept its sampled height in a field that its old writer omitted.
            if (building == Building.APOTHECARY) grounded |= chest || residents > 0 || box[1] != ORIGINAL_PLAN_Y;
            return new Piece(building, new BoundingBox(box[0], box[1], box[2], box[3], box[4], box[5]),
                    direction, saved.getBoolean("Desert"), chest, residents, grounded, saved.getBoolean("C"), saved.getInt("T") > 0);
        }

        StructurePlaceSettings settings() {
            var place = new StructurePlaceSettings().setKnownShape(true).setFinalizeEntities(true)
                    .addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK).addProcessor(JigsawReplacementProcessor.INSTANCE);
            // The original east and north pieces reflected one axis in addition to turning.
            return switch (orientation) {
                case 1 -> place.setRotation(Rotation.CLOCKWISE_90);
                case 2 -> place.setMirror(Mirror.LEFT_RIGHT);
                case 3 -> place.setMirror(Mirror.FRONT_BACK).setRotation(Rotation.COUNTERCLOCKWISE_90);
                default -> place;
            };
        }

        BlockPos origin(StructurePlaceSettings settings, int y) {
            int x = orientation == 1 ? bounds.maxX() : bounds.minX();
            int z = orientation == 2 ? bounds.maxZ() : bounds.minZ();
            var approach = StructureTemplate.calculateRelativePosition(settings, new BlockPos(0, 0, building.approach));
            return new BlockPos(x, y, z).subtract(approach);
        }

        void place(WorldGenRegion region, ChunkGenerator generator, List<BoundingBox> clips) {
            if (clips.isEmpty()) return;
            var level = region.getLevel();
            var settings = settings();
            int y = grounded ? bounds.minY() : generator.getBaseHeight(
                    (bounds.minX() + bounds.maxX()) >> 1, (bounds.minZ() + bounds.maxZ()) >> 1,
                    Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, region, level.getChunkSource().randomState());
            var origin = origin(settings, y);
            var template = level.getStructureManager().getOrCreate(building.template);
            var bounds = template.getBoundingBox(settings, origin);
            var processor = desert && (building == Building.KEEP || building == Building.WATCHTOWER)
                    ? building.template.withSuffix("_desert") : building.template;
            var processors = region.registryAccess().registryOrThrow(Registries.PROCESSOR_LIST);
            processors.getHolderOrThrow(ResourceKey.create(Registries.PROCESSOR_LIST, processor))
                    .value().list().forEach(settings::addProcessor);
            for (var clip : clips) {
                if (!bounds.intersects(clip)) continue;
                var placement = settings.copy().setBoundingBox(clip).addProcessor(new SavedPlacement(this));
                var random = RandomSource.create(region.getSeed() ^ origin.asLong());
                template.placeInWorld(region, origin, origin, placement, random, 18);
            }
        }
    }

    private static final class SavedPlacement extends StructureProcessor {
        private final Piece piece;
        private int guardsSeen;

        private SavedPlacement(Piece piece) {
            this.piece = piece;
        }

        @Override
        @Nullable
        public StructureTemplate.StructureBlockInfo process(LevelReader level, BlockPos origin, BlockPos pivot,
                StructureTemplate.StructureBlockInfo raw, StructureTemplate.StructureBlockInfo placed,
                StructurePlaceSettings settings, @Nullable StructureTemplate template) {
            if (piece.chestPlaced() && raw.state().is(Blocks.CHEST)) return null;
            if (piece.building() != Building.WITCH_HUT) return placed;
            var local = raw.pos();
            if (!piece.furnishedHut() && (local.equals(new BlockPos(1, 1, 4))
                    || local.equals(new BlockPos(2, 1, 4)) || local.equals(new BlockPos(2, 2, 4)))) {
                return new StructureTemplate.StructureBlockInfo(placed.pos(), Blocks.AIR.defaultBlockState(), null);
            }
            if (!piece.tallHut() && local.getX() >= 1 && local.getX() <= 2
                    && local.getZ() >= 2 && local.getZ() <= 4) {
                if (local.getY() == 4) return new StructureTemplate.StructureBlockInfo(placed.pos(), Blocks.SPRUCE_PLANKS.defaultBlockState(), null);
                if (local.getY() == 5) return new StructureTemplate.StructureBlockInfo(placed.pos(), Blocks.AIR.defaultBlockState(), null);
            }
            return placed;
        }

        @Override
        @Nullable
        public StructureTemplate.StructureEntityInfo processEntity(LevelReader level, BlockPos origin,
                StructureTemplate.StructureEntityInfo raw, StructureTemplate.StructureEntityInfo placed,
                StructurePlaceSettings settings, StructureTemplate template) {
            boolean alreadyPlaced = switch (piece.building()) {
                case APOTHECARY, WITCH_HUT -> piece.residentsPlaced() > 0;
                case BOOKSHOP -> piece.chestPlaced();
                case WATCHTOWER -> guardsSeen++ < piece.residentsPlaced();
                case KEEP -> piece.residentsPlaced() >= 5 || guardsSeen++ < piece.residentsPlaced();
            };
            return alreadyPlaced ? null : placed;
        }

        @Override
        protected StructureProcessorType<?> getType() {
            return StructureProcessorType.NOP;
        }
    }
}
