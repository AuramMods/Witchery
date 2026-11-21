package art.arcane.witchery.world;

import art.arcane.witchery.config.WitcheryServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.common.Tags;

import java.util.Locale;
import java.util.regex.Pattern;

public final class WickerManFeature extends Feature<NoneFeatureConfiguration> {
    private static final ResourceLocation TWILIGHT_FOREST = ResourceLocation.parse("twilightforest:twilight_forest");
    private static final int SPACING_MARGIN = 8;
    private static final int REGION_SALT = 10387312;
    private static final int WIDTH = 6;
    private static final int DEPTH = 5;
    private static final String[] EFFIGY = {"..SS..", "..SS..", ".FSSF.", ".SYYS.", ".YYYY.", "..FF..", "..FF.."};
    private static final Pattern WORD_BOUNDARY = Pattern.compile("(?<=[a-z0-9])(?=[A-Z])");

    public WickerManFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        if (!WitcheryServerConfig.GENERATE_WICKER_MEN.get() || !supportsDimension(level)) return false;

        ChunkPos chunk = new ChunkPos(context.origin());
        int spread = WitcheryServerConfig.WORLD_GEN_FREQUENCY.get();
        int spacing = SPACING_MARGIN + spread;
        int regionX = Math.floorDiv(chunk.x, spacing);
        int regionZ = Math.floorDiv(chunk.z, spacing);
        WorldgenRandom random = new WorldgenRandom(new LegacyRandomSource(0));
        random.setLargeFeatureWithSalt(level.getSeed(), regionX, regionZ, REGION_SALT);
        int candidateX = regionX * spacing + random.nextInt(spread);
        int candidateZ = regionZ * spacing + random.nextInt(spread);
        // The original generator chose among four equally weighted surface structures.
        if (chunk.x != candidateX || chunk.z != candidateZ || random.nextInt(4) != 0) return false;

        int x = chunk.getMiddleBlockX();
        int z = chunk.getMiddleBlockZ();
        var biome = level.getBiome(new BlockPos(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) - 1, z));
        if (biome.is(BiomeTags.IS_OCEAN) || biome.is(BiomeTags.IS_RIVER) || biome.is(BiomeTags.IS_BEACH)
                || biome.is(BiomeTags.IS_JUNGLE) || biome.is(BiomeTags.IS_MOUNTAIN) || biome.is(Tags.Biomes.IS_MOUNTAIN)
                || biome.is(Tags.Biomes.IS_SWAMP)) return false;

        boolean sand = biome.is(Biomes.DESERT);
        Direction front = Direction.Plane.HORIZONTAL.getRandomDirection(random);
        Footprint footprint = new Footprint(new BlockPos(x, 0, z), front);
        int ground = groundHeight(level, footprint, context.chunkGenerator().getSeaLevel() + 1);
        if (level.isOutsideBuildHeight(ground) || level.isOutsideBuildHeight(ground + EFFIGY.length)) return false;
        build(level, new Footprint(footprint.center().atY(ground), front), sand, random);
        return true;
    }

    private static boolean supportsDimension(WorldGenLevel level) {
        var dimension = level.getLevel().dimension();
        return dimension.equals(Level.OVERWORLD) || (WitcheryServerConfig.WORLD_GEN_TWILIGHT_FOREST.get()
                && dimension.location().equals(TWILIGHT_FOREST));
    }

    private static int groundHeight(WorldGenLevel level, Footprint footprint, int minimumSurface) {
        int total = 0;
        for (int z = 0; z < DEPTH; z++) {
            for (int x = 0; x < WIDTH; x++) {
                BlockPos pos = footprint.at(x, 0, z);
                int surface = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos.getX(), pos.getZ());
                total += Math.max(minimumSurface, surface);
            }
        }
        return total / (WIDTH * DEPTH) - 1;
    }

    private static void build(WorldGenLevel level, Footprint footprint, boolean sand, RandomSource random) {
        BlockState surface = (sand ? Blocks.SAND : Blocks.GRASS_BLOCK).defaultBlockState();
        BlockState foundation = (sand ? Blocks.SAND : Blocks.DIRT).defaultBlockState();
        for (int z = 0; z < DEPTH; z++) {
            for (int x = 0; x < WIDTH; x++) {
                boolean platform = z == 2 || (x > 0 && x < WIDTH - 1);
                BlockPos base = footprint.at(x, 0, z);
                if (platform) level.setBlock(base, surface, Block.UPDATE_CLIENTS);
                if (platform || !level.isEmptyBlock(base)) fillFoundation(level, base.below(), foundation);
                clearAbove(level, base, platform ? 1 : 8);
            }
        }

        BlockState flower = (sand ? Blocks.DEAD_BUSH : Blocks.POPPY).defaultBlockState();
        for (int x : new int[]{1, 4}) {
            level.setBlock(footprint.at(x, 1, 0), flower, Block.UPDATE_CLIENTS);
            level.setBlock(footprint.at(x, 1, 4), flower, Block.UPDATE_CLIENTS);
        }
        level.setBlock(footprint.at(0, 1, 2), flower, Block.UPDATE_CLIENTS);
        level.setBlock(footprint.at(5, 1, 2), flower, Block.UPDATE_CLIENTS);

        BlockState hay = Blocks.HAY_BLOCK.defaultBlockState();
        Direction.Axis shoulderAxis = footprint.front().getClockWise().getAxis();
        Direction.Axis depthAxis = footprint.front().getAxis();
        for (int y = 0; y < EFFIGY.length; y++) {
            for (int x = 0; x < WIDTH; x++) {
                Direction.Axis axis = switch (EFFIGY[y].charAt(x)) {
                    case 'S' -> shoulderAxis;
                    case 'F' -> depthAxis;
                    case 'Y' -> Direction.Axis.Y;
                    default -> null;
                };
                if (axis != null) {
                    level.setBlock(footprint.at(x, y + 1, 2), hay.setValue(RotatedPillarBlock.AXIS, axis), Block.UPDATE_CLIENTS);
                }
            }
        }

        BlockPos spawnerPos = footprint.at(2, 0, 2);
        level.setBlock(spawnerPos, Blocks.SPAWNER.defaultBlockState(), Block.UPDATE_CLIENTS);
        if (level.getBlockEntity(spawnerPos) instanceof SpawnerBlockEntity spawner) {
            spawner.setEntityId(spawnerMob(random), random);
            spawner.setChanged();
        }
    }

    private static void fillFoundation(WorldGenLevel level, BlockPos top, BlockState foundation) {
        BlockPos.MutableBlockPos cursor = top.mutable();
        while (!level.isOutsideBuildHeight(cursor)) {
            BlockState old = level.getBlockState(cursor);
            if (old.blocksMotion() && old.getFluidState().isEmpty() && !old.is(Blocks.ICE)) break;
            level.setBlock(cursor, foundation, Block.UPDATE_CLIENTS);
            cursor.move(Direction.DOWN);
        }
    }

    private static void clearAbove(WorldGenLevel level, BlockPos base, int start) {
        BlockPos.MutableBlockPos cursor = base.above(start).mutable();
        int clearance = base.getY() + 27;
        while (!level.isOutsideBuildHeight(cursor)) {
            if (level.isEmptyBlock(cursor)) {
                if (cursor.getY() >= clearance) break;
            } else {
                level.setBlock(cursor, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
            cursor.move(Direction.UP);
        }
    }

    private static EntityType<?> spawnerMob(RandomSource random) {
        var mobs = WitcheryServerConfig.STRAWMAN_SPAWNER_RULES.get();
        if (mobs.isEmpty()) return EntityType.ZOMBIE;
        String configured = mobs.get(random.nextInt(mobs.size())).trim();
        if (configured.isEmpty()) return EntityType.ZOMBIE;
        String name = switch (configured) {
            case "PigZombie" -> "zombified_piglin";
            case "LavaSlime" -> "magma_cube";
            case "VillagerGolem" -> "iron_golem";
            case "SnowMan" -> "snow_golem";
            case "MushroomCow" -> "mooshroom";
            case "Ozelot" -> "ocelot";
            case "EntityHorse" -> "horse";
            case "WitherBoss" -> "wither";
            default -> configured.contains(":") ? configured
                    : WORD_BOUNDARY.matcher(configured).replaceAll("_").toLowerCase(Locale.ROOT);
        };
        ResourceLocation id = ResourceLocation.tryParse(name);
        return id == null ? EntityType.ZOMBIE : BuiltInRegistries.ENTITY_TYPE.getOptional(id).orElse(EntityType.ZOMBIE);
    }

    private record Footprint(BlockPos center, Direction front) {
        BlockPos at(int x, int y, int z) {
            return center.relative(front.getClockWise(), x - 2).relative(front, z - 2).above(y);
        }
    }
}
