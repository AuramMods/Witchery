package art.arcane.witchery.ritual;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ServerLevelData;

public final class RainOfToads {
    public static final ResourceLocation RECIPE = ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "rite_of_rain_of_toads");
    public static final int LAST_LIGHTNING_STAGE = 4;
    static final int LAST_STAGE = 200;
    private static final int INNER_RADIUS = 5;
    private static final int OUTER_RADIUS = 16;

    private RainOfToads() {}

    public static void lightningPulse(ServerLevel level, BlockPos heart, int stage) {
        if (stage == LAST_LIGHTNING_STAGE) {
            ServerLevel overworld = level.getServer().overworld();
            if (!overworld.isRaining() && overworld.getLevelData() instanceof ServerLevelData weather) {
                weather.setClearWeatherTime(0);
                weather.setRainTime((300 + level.random.nextInt(600)) * 20);
                weather.setRaining(true);
            }
        }
        WeatherRites.strike(level, sample(level, heart));
    }

    static void shower(ServerLevel level, BlockPos heart) {
        for (int count = 0; count < level.random.nextInt(10) + 8; count++) {
            BlockPos column = sample(level, heart);
            if (!level.getWorldBorder().isWithinBounds(column)) continue;
            WeatherRites.keepWeatherSiteLoaded(level, column);
            BlockPos surface = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, column);
            if (!level.isEmptyBlock(surface)) continue;
            var toad = WitcheryEntities.TOAD.get().create(level);
            if (toad != null) {
                toad.setPos(surface.getX(), surface.getY() + 8 + level.random.nextInt(7), surface.getZ());
                toad.prepareRainToad();
                level.addFreshEntity(toad);
            }
        }
    }

    private static BlockPos sample(ServerLevel level, BlockPos origin) {
        return origin.offset(WeatherRites.offsetOutsideCenter(level.random, INNER_RADIUS, OUTER_RADIUS), 0,
                WeatherRites.offsetOutsideCenter(level.random, INNER_RADIUS, OUTER_RADIUS));
    }
}
