package art.arcane.witchery.brewing;

import art.arcane.witchery.block.BrewDispersalBlock;
import art.arcane.witchery.block.WitchLilyPadBlock;
import art.arcane.witchery.block.entity.SlurpBlockEntity;
import art.arcane.witchery.registry.WitcheryBlocks;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fluids.IFluidBlock;
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
        int bottom = Math.max(level.getMinBuildHeight(), center.getY() - radius);
        int top = Math.min(level.getMaxBuildHeight() - 1, center.getY() + radius);
        var cursor = new BlockPos.MutableBlockPos();
        BrewTerrain.forEachColumn(level, center, radius + (ritual ? 5 : 0), column -> {
            for (int y = bottom; y <= top; y++) {
                cursor.set(column.getX(), y, column.getZ());
                if (level.getBlockState(cursor).is(Blocks.FIRE)
                        && BrewTerrain.setBlock(actor, level, cursor, Blocks.AIR.defaultBlockState(), Direction.UP)) {
                    level.playSound(null, cursor.getX(), y, cursor.getZ(),
                            SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1, 2);
                }
            }
        });
    }

    static void dissipate(ServerLevel level, BlockPos center, int radius, int strength, @Nullable Player caster) {
        Player actor = caster != null ? caster : FakePlayerFactory.getMinecraft(level);
        var cursor = new BlockPos.MutableBlockPos();
        BrewTerrain.forEachColumn(level, center, radius, column -> {
            for (int y = -radius; y <= radius; y++) {
                cursor.setWithOffset(column, 0, y, 0);
                if (!level.isInWorldBounds(cursor)) continue;
                BlockState state = level.getBlockState(cursor);
                if ((state.getBlock() instanceof BrewDispersalBlock brew && brew.isGas()
                        || strength > 0 && isGaseousFluid(state))
                        && BrewTerrain.setBlock(actor, level, cursor, Blocks.AIR.defaultBlockState(), Direction.UP)) {
                    level.playSound(null, cursor.getX(), cursor.getY(), cursor.getZ(),
                            SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 1, 2);
                }
            }
        });
    }

    private static boolean isGaseousFluid(BlockState state) {
        if (state.getBlock() instanceof IFluidBlock modFluid) {
            var fluid = modFluid.getFluid();
            return fluid != null && fluid.getFluidType().isLighterThanAir();
        }
        return state.getBlock() instanceof LiquidBlock && !state.getFluidState().isEmpty()
                && state.getFluidState().getFluidType().isLighterThanAir();
    }

    static void holdFluid(ServerLevel level, BlockPos center, int radius, int duration, Block fluid, @Nullable Player caster) {
        Player actor = caster != null ? caster : FakePlayerFactory.getMinecraft(level);
        int reach = radius + 2;
        int distanceLimit = reach * reach - 1;
        BlockState barrier = WitcheryBlocks.getRequired("slurp").get().defaultBlockState();
        var cursor = new BlockPos.MutableBlockPos();
        for (int x = -reach; x <= reach; x++) {
            for (int z = -reach; z <= reach; z++) {
                for (int y = -reach; y <= reach; y++) {
                    if (x * x + y * y + z * z >= distanceLimit) continue;
                    cursor.setWithOffset(center, x, y, z);
                    if (!level.isInWorldBounds(cursor) || !level.hasChunkAt(cursor)) continue;
                    BlockState previous = level.getBlockState(cursor);
                    if (previous.is(fluid) && BrewTerrain.setBlock(actor, level, cursor, barrier, Direction.UP)
                            && level.getBlockEntity(cursor) instanceof SlurpBlockEntity hold) {
                        hold.hold(previous, duration);
                    }
                }
            }
        }
    }

    static void lilify(ServerLevel level, BlockPos pos, Direction face, int strength, @Nullable Player caster) {
        var water = pos.relative(face).mutable();
        if (!level.hasChunkAt(water)) return;
        while (water.getY() < level.getMaxBuildHeight() - 1) {
            if (BrewTerrain.isLegacyWater(level.getBlockState(water)) && level.isEmptyBlock(water.above())) {
                BlockState lily = WitcheryBlocks.getRequired("lilypad").get().defaultBlockState()
                        .setValue(WitchLilyPadBlock.GROWTH, strength & 3);
                Player actor = caster != null ? caster : FakePlayerFactory.getMinecraft(level);
                BrewTerrain.setBlockCheckingAir(actor, level, water.above(), lily, Direction.UP);
                return;
            }
            water.move(Direction.UP);
        }
    }

    static void changeSeasons(ServerLevel level, BlockPos center, ResourceLocation biomeId, int strength,
                              boolean ritual, @Nullable Player caster) {
        var biome = level.registryAccess().registryOrThrow(Registries.BIOME)
                .getHolder(ResourceKey.create(Registries.BIOME, biomeId)).orElse(null);
        if (biome == null || !level.getWorldBorder().isWithinBounds(center)) return;

        int reach = ritual ? 15 + strength * 16 : strength;
        int[] rows = BiomePainting.circleWidths(reach);
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
}
