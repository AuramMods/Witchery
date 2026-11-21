package art.arcane.witchery.brewing;

import art.arcane.witchery.world.BiomePainting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.level.BlockEvent;
import org.jetbrains.annotations.Nullable;

final class BrewTerrainEffects {
    private BrewTerrainEffects() {}

    static void summonRain(ServerLevel level, int strength) {
        if (!(level.getLevelData() instanceof ServerLevelData weather)) return;
        int duration = (300 + level.random.nextInt(600)) * 20;
        if (!weather.isRaining()) {
            weather.setClearWeatherTime(0);
            weather.setRainTime(duration);
            weather.setRaining(true);
        }
        if (strength > 0 && !weather.isThundering()) {
            weather.setClearWeatherTime(0);
            weather.setThunderTime(duration);
            weather.setThundering(true);
        }
    }

    static void extinguish(ServerLevel level, BlockPos center, int radius, boolean ritual, int strength,
                           @Nullable Player caster) {
        if (strength <= 1 && level.dimensionType().ultraWarm()) return;
        Player actor = caster != null ? caster : FakePlayerFactory.getMinecraft(level);
        int reach = radius + (ritual ? 5 : 0) - 1;
        int bottom = Math.max(level.getMinBuildHeight(), center.getY() - radius);
        int top = Math.min(level.getMaxBuildHeight() - 1, center.getY() + radius);
        var cursor = new BlockPos.MutableBlockPos();
        int[] rowWidth = circleWidths(reach);
        for (int z = -reach; z <= reach; z++) {
            int width = rowWidth[Math.abs(z)];
            for (int x = -width; x <= width; x++) {
                for (int y = bottom; y <= top; y++) {
                    cursor.set(center.getX() + x, y, center.getZ() + z);
                    if (!level.getWorldBorder().isWithinBounds(cursor)) continue;
                    var state = level.getBlockState(cursor);
                    if (state.is(Blocks.FIRE) && !MinecraftForge.EVENT_BUS.post(
                            new BlockEvent.BreakEvent(level, cursor.immutable(), state, actor))) {
                        level.removeBlock(cursor, false);
                        level.playSound(null, cursor, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1, 2);
                    }
                }
            }
        }
    }

    static void changeSeasons(ServerLevel level, BlockPos center, ResourceLocation biomeId, int strength,
                              boolean ritual, @Nullable Player caster) {
        var biome = level.registryAccess().registryOrThrow(Registries.BIOME)
                .getHolder(ResourceKey.create(Registries.BIOME, biomeId)).orElse(null);
        if (biome == null || !level.getWorldBorder().isWithinBounds(center)) return;

        int reach = ritual ? 15 + strength * 16 : strength;
        int[] rows = circleWidths(reach);
        BiomePainting.paint(level, center, rows, biome);
        if (biome.value().getModifiedClimateSettings().downfall() == 0) {
            Player actor = caster == null ? FakePlayerFactory.getMinecraft(level) : caster;
            clearSnowLayers(level, center, rows, actor);
        }
    }

    private static void clearSnowLayers(ServerLevel level, BlockPos center, int[] rows, Player caster) {
        int reach = rows.length - 1;
        for (int z = -reach; z <= reach; z++) {
            int width = rows[Math.abs(z)];
            for (int x = -width; x <= width; x++) {
                int worldX = center.getX() + x;
                int worldZ = center.getZ() + z;
                if (!level.getWorldBorder().isWithinBounds(worldX, worldZ)) continue;
                var surface = new BlockPos(worldX, level.getHeight(Heightmap.Types.WORLD_SURFACE, worldX, worldZ) - 1, worldZ);
                if (level.getBlockState(surface).is(Blocks.SNOW)) {
                    BrewTerrain.setBlock(caster, level, surface, Blocks.AIR.defaultBlockState(), Direction.UP);
                }
            }
        }
    }

    static int[] circleWidths(int reach) {
        int[] rowWidth = new int[reach + 1];
        int circleX = reach;
        int circleZ = 0;
        int error = 1 - reach;
        while (circleX >= circleZ) {
            rowWidth[circleZ] = Math.max(rowWidth[circleZ], circleX);
            rowWidth[circleX] = Math.max(rowWidth[circleX], circleZ);
            circleZ++;
            if (error < 0) {
                error += 2 * circleZ + 1;
            } else {
                circleX--;
                error += 2 * (circleZ - circleX + 1);
            }
        }
        return rowWidth;
    }
}
