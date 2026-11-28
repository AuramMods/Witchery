package art.arcane.witchery.world;

import art.arcane.witchery.Witchery;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.FlatLevelSource;
import net.minecraft.world.level.levelgen.WorldDimensions;
import net.minecraft.world.level.levelgen.flat.FlatLevelGeneratorSettings;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class DreamGeneratorTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_dream_generator")
    public static void dreamInheritsNewTerrainAndKeepsExplicitGenerators(GameTestHelper helper) {
        var access = helper.getLevel().registryAccess();
        var live = access.registryOrThrow(Registries.LEVEL_STEM);
        var dream = live.getOrThrow(WitcheryDimensions.DREAM_STEM);
        var normal = live.getOrThrow(LevelStem.OVERWORLD);
        var flat = new FlatLevelSource(FlatLevelGeneratorSettings.getDefault(access.lookupOrThrow(Registries.BIOME),
                access.lookupOrThrow(Registries.STRUCTURE_SET), access.lookupOrThrow(Registries.PLACED_FEATURE)));
        var overworld = new LevelStem(normal.type(), flat);
        var bundled = ChunkGenerator.CODEC.parse(RegistryOps.create(JsonOps.INSTANCE, access), JsonParser.parseString("""
                {"type":"witchery:dream","biome_source":{"type":"minecraft:multi_noise","preset":"minecraft:overworld"},
                 "settings":"minecraft:overworld"}
                """)).getOrThrow(false, helper::fail);
        var defaults = stems(overworld, new LevelStem(dream.type(), bundled));

        var fresh = new WorldDimensions(stems(overworld, null));
        var inherited = fresh.bake(defaults).dimensions().getOrThrow(WitcheryDimensions.DREAM_STEM);
        helper.assertTrue(inherited.type() == dream.type() && inherited.generator() == flat,
                "A new Dream keeps its dimension type and inherits the selected Overworld generator");

        var savedDream = new LevelStem(dream.type(), normal.generator());
        var reopened = new WorldDimensions(stems(overworld, savedDream)).bake(defaults);
        helper.assertTrue(reopened.dimensions().getOrThrow(WitcheryDimensions.DREAM_STEM).generator() == normal.generator(),
                "An existing Dream keeps the terrain generator recorded with its world");

        var override = new LevelStem(dream.type(), flat);
        var replaced = new WorldDimensions(stems(normal, savedDream)).bake(stems(normal, override));
        helper.assertTrue(replaced.dimensions().getOrThrow(WitcheryDimensions.DREAM_STEM) == override,
                "A Dream generator explicitly set by a datapack takes precedence");
        helper.succeed();
    }

    private static Registry<LevelStem> stems(LevelStem overworld, LevelStem dream) {
        var registry = new MappedRegistry<LevelStem>(Registries.LEVEL_STEM, Lifecycle.stable());
        Registry.register(registry, LevelStem.OVERWORLD, overworld);
        if (dream != null) Registry.register(registry, WitcheryDimensions.DREAM_STEM, dream);
        return registry.freeze();
    }
}
