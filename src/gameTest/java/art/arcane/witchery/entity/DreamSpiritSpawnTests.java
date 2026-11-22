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

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class DreamSpiritSpawnTests {
    @GameTest(template = "qa_coven_arena")
    public static void registeredSpiritSpawnUsesConfiguredWeight(GameTestHelper helper) {
        var registries = helper.getLevel().registryAccess();
        var plains = registries.registryOrThrow(Registries.BIOME).getHolderOrThrow(Biomes.PLAINS);
        var weight = WitcheryServerConfig.SPIRIT_SPAWN_WEIGHT;
        int previousWeight = weight.get();
        verifySpawn(helper, plains.value().getMobSettings(), previousWeight);

        var modifierKey = ResourceKey.create(ForgeRegistries.Keys.BIOME_MODIFIERS,
                ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "dream_spirits"));
        var modifier = registries.registryOrThrow(ForgeRegistries.Keys.BIOME_MODIFIERS).getOrThrow(modifierKey);
        var builder = ModifiableBiomeInfo.BiomeInfo.Builder.copyOf(plains.value().modifiableBiomeInfo().getOriginalBiomeInfo());
        try {
            int customWeight = previousWeight == 37 ? 38 : 37;
            weight.set(customWeight);
            modifier.modify(plains, BiomeModifier.Phase.ADD, builder);
            verifySpawn(helper, builder.build().mobSpawnSettings(), customWeight);
        } finally {
            weight.set(previousWeight);
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
