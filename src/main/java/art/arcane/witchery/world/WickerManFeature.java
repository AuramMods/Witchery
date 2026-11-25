package art.arcane.witchery.world;

import art.arcane.witchery.config.WitcheryServerConfig;
import art.arcane.witchery.registry.WitcheryEntities;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.Locale;
import java.util.regex.Pattern;

public final class WickerManFeature extends Feature<NoneFeatureConfiguration> {
    private static final String[] EFFIGY = {"..SS..", "..SS..", ".FSSF.", ".SYYS.", ".YYYY.", "..FF..", "..FF.."};
    private static final Pattern WORD_BOUNDARY = Pattern.compile("(?<=[a-z0-9])(?=[A-Z])");

    public WickerManFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        return SurfaceStructuresFeature.place(context, true);
    }

    static void build(SurfaceStructureSite site, RandomSource random) {
        for (int z = 0; z < site.depth(); z++) {
            for (int x = 0; x < site.width(); x++) {
                boolean platform = z == 2 || (x > 0 && x < site.width() - 1);
                if (platform) site.put(x, 0, z, site.surface());
                if (platform || !site.level().isEmptyBlock(site.at(x, 0, z))) site.support(x, z);
                site.clearAbove(x, z, platform ? 1 : 8, 27);
            }
        }

        Block flower = site.sandy() ? Blocks.DEAD_BUSH : Blocks.POPPY;
        for (int x : new int[]{1, 4}) {
            site.put(x, 1, 0, flower);
            site.put(x, 1, 4, flower);
        }
        site.put(0, 1, 2, flower);
        site.put(5, 1, 2, flower);

        BlockState hay = Blocks.HAY_BLOCK.defaultBlockState();
        Direction.Axis shoulderAxis = site.direction(Direction.EAST).getAxis();
        Direction.Axis depthAxis = site.facing().getAxis();
        for (int y = 0; y < EFFIGY.length; y++) {
            for (int x = 0; x < site.width(); x++) {
                Direction.Axis axis = switch (EFFIGY[y].charAt(x)) {
                    case 'S' -> shoulderAxis;
                    case 'F' -> depthAxis;
                    case 'Y' -> Direction.Axis.Y;
                    default -> null;
                };
                if (axis != null) site.put(x, y + 1, 2, hay.setValue(RotatedPillarBlock.AXIS, axis));
            }
        }
        site.spawner(2, 0, 2, spawnerMob(random), random);
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
        ResourceLocation id = WitcheryEntities.legacyEntityId(configured);
        if (id == null) id = ResourceLocation.tryParse(name);
        return id == null ? EntityType.ZOMBIE : BuiltInRegistries.ENTITY_TYPE.getOptional(id).orElse(EntityType.ZOMBIE);
    }
}
