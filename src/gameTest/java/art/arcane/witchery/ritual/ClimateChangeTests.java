package art.arcane.witchery.ritual;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.item.WitcheryIngredient;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.storage.ServerLevelData;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class ClimateChangeTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_climate_focus_slots")
    public static void climateFocusKeepsOriginalDustSlotsWhenBiomesMerged(GameTestHelper helper) {
        var registry = helper.getLevel().registryAccess().registryOrThrow(Registries.BIOME);
        var forest = ClimateChange.Focus.FOREST.candidates(registry);
        helper.assertTrue(forest.subList(0, 4).stream().map(biome -> biome.unwrapKey().orElseThrow()).toList()
                        .equals(List.of(Biomes.FOREST, Biomes.TAIGA, Biomes.TAIGA, Biomes.FOREST)),
                "The original Forest and Taiga hills still occupy separate Glowstone Dust slots");
        helper.assertTrue(ClimateChange.Focus.WASTELAND.candidates(registry).get(0).is(Biomes.SNOWY_PLAINS),
                "Netherrack selects the original Ice Plains equivalent first");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_climate_focus_order")
    public static void climateFocusUsesOriginalChunkOrderAndCenteredDropBounds(GameTestHelper helper) {
        var level = helper.getLevel();
        var origin = helper.absolutePos(BlockPos.ZERO).offset(512, 80, -512);
        int x = SectionPos.sectionToBlockCoord(SectionPos.blockToSectionCoord(origin.getX()));
        int z = SectionPos.sectionToBlockCoord(SectionPos.blockToSectionCoord(origin.getZ()));
        var heart = new BlockPos(x, origin.getY(), z);
        for (int chunkX = (x >> 4) - 2; chunkX <= (x >> 4) + 2; chunkX++) {
            for (int chunkZ = (z >> 4) - 2; chunkZ <= (z >> 4) + 2; chunkZ++) {
                level.getChunk(chunkX, chunkZ);
            }
        }
        var outside = heart.offset(20, 0, 0);
        var oldOutside = biome(level, outside);
        var mountain = drop(level, heart.offset(1, 0, 1), 0, Items.OBSIDIAN, 1);
        var needle = drop(level, heart.offset(1, 0, -1), 0, WitcheryIngredient.ICY_NEEDLE.item(), 1);
        var cactus = drop(level, heart.offset(-1, 0, 1), 0, Items.CACTUS, 1);
        var glowstone = drop(level, heart.offset(2, 0, 1), 2.0625, Items.GLOWSTONE_DUST, 2);
        var tooLow = drop(level, heart, -2.1875, Items.CACTUS, 1);
        var weather = (ServerLevelData) level.getLevelData();
        int oldClear = weather.getClearWeatherTime();
        int oldRain = weather.getRainTime();
        int oldThunder = weather.getThunderTime();
        boolean raining = weather.isRaining();
        boolean thundering = weather.isThundering();
        float rainLevel = level.getRainLevel(1);
        float thunderLevel = level.getThunderLevel(1);
        try {
            level.setWeatherParameters(0, 0, false, false);
            level.setRainLevel(0);
            level.setThunderLevel(0);
            level.getRandom().setSeed(42);
            ClimateChange.playStartCue(level, heart);
            helper.assertTrue(ClimateChange.pulse(level, heart, ClimateChange.CAST_STAGE, 4, null, true),
                    "The foci activate the final pulse");
            helper.assertTrue(!cactus.isAlive() && !needle.isAlive() && !mountain.isAlive() && !glowstone.isAlive() && tooLow.isAlive(),
                    "The upper hitbox overlap counts, and the drop just below the search stays intact");
            helper.assertTrue(biome(level, heart).is(Biomes.SNOWY_PLAINS)
                            && biome(level, heart.offset(16, 16, 2)).is(Biomes.SNOWY_PLAINS),
                    "The last focus selects its second variant and paints the intersecting edge column at all heights");
            helper.assertTrue(biome(level, outside).equals(oldOutside), "The next biome column stays intact");
            helper.assertTrue(weather.isRaining() && !weather.isThundering()
                            && weather.getRainTime() == 6960 && weather.getThunderTime() == 6960,
                    "The last weather focus wins independently and uses the original duration draw");
        } finally {
            for (var item : List.of(mountain, needle, cactus, glowstone, tooLow)) item.discard();
            level.setWeatherParameters(oldClear, oldRain, raining, thundering);
            weather.setThunderTime(oldThunder);
            level.setRainLevel(rainLevel);
            level.setThunderLevel(thunderLevel);
        }
        helper.succeed();
    }

    private static ItemEntity drop(ServerLevel level, BlockPos pos, double yOffset, Item item, int count) {
        var drop = new ItemEntity(level, pos.getX(), pos.getY() + yOffset, pos.getZ(), new ItemStack(item, count), 0, 0, 0);
        level.addFreshEntity(drop);
        return drop;
    }

    private static Holder<Biome> biome(ServerLevel level, BlockPos pos) {
        return level.getChunkAt(pos).getNoiseBiome(QuartPos.fromBlock(pos.getX()), QuartPos.fromBlock(pos.getY()), QuartPos.fromBlock(pos.getZ()));
    }
}
