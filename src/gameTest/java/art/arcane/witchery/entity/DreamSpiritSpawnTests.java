package art.arcane.witchery.entity;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.config.WitcheryServerConfig;
import art.arcane.witchery.registry.WitcheryEntities;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class DreamSpiritSpawnTests {
    @GameTest(template = "qa_coven_arena")
    public static void registeredSpiritSpawnUsesConfiguredWeight(GameTestHelper helper) {
        var registries = helper.getLevel().registryAccess();
        var biomes = registries.registryOrThrow(Registries.BIOME);
        var modifiers = registries.registryOrThrow(ForgeRegistries.Keys.BIOME_MODIFIERS);
        var weight = WitcheryServerConfig.SPIRIT_SPAWN_WEIGHT;
        int previousWeight = weight.get();
        try {
            int customWeight = previousWeight == 37 ? 38 : 37;
            weight.set(customWeight);
            var habitats = List.of(Biomes.PLAINS, Biomes.SNOWY_TAIGA, Biomes.GROVE);
            var modifierNames = List.of("dream_spirits", "dream_spirits_in_hills", "dream_spirits_in_snowy_groves");
            for (int i = 0; i < habitats.size(); i++) {
                var biome = biomes.getHolderOrThrow(habitats.get(i));
                int multiplier = i + 1;
                verifySpawn(helper, biome.value().getMobSettings(), previousWeight * multiplier);
                var key = ResourceKey.create(ForgeRegistries.Keys.BIOME_MODIFIERS,
                        ResourceLocation.fromNamespaceAndPath(Witchery.MODID, modifierNames.get(i)));
                var builder = ModifiableBiomeInfo.BiomeInfo.Builder.copyOf(biome.value().modifiableBiomeInfo().getOriginalBiomeInfo());
                modifiers.getOrThrow(key).modify(biome, BiomeModifier.Phase.ADD, builder);
                verifySpawn(helper, builder.build().mobSpawnSettings(), customWeight * multiplier);
            }
        } finally {
            weight.set(previousWeight);
        }
        for (var excluded : List.of(Biomes.OCEAN, Biomes.JUNGLE, Biomes.ICE_SPIKES, Biomes.DEEP_DARK)) {
            var settings = biomes.getOrThrow(excluded).getMobSettings();
            helper.assertTrue(settings.getMobs(MobCategory.AMBIENT).unwrap().stream()
                            .noneMatch(entry -> entry.type == WitcheryEntities.SPIRIT.get()),
                    "No Spirit spawn entry in " + excluded.location());
        }
        helper.succeed();
    }

    private static void verifySpawn(GameTestHelper helper, MobSpawnSettings settings, int weight) {
        var spawns = settings.getMobs(MobCategory.AMBIENT).unwrap().stream()
                .filter(entry -> entry.type == WitcheryEntities.SPIRIT.get()).toList();
        helper.assertTrue(spawns.size() == 1, "Expected one ambient Spirit spawn entry");
        var spawn = spawns.get(0);
        helper.assertTrue(spawn.getWeight().asInt() == weight && spawn.minCount == 2 && spawn.maxCount == 5,
                "The Spirit entry must use the configured weight and groups of two to five");
    }
}
