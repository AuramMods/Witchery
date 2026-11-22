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
public final class HellhoundSpawnTests {
    @GameTest(template = "qa_coven_arena")
    public static void registeredNetherSpawnUsesConfiguredWeight(GameTestHelper helper) {
        var registries = helper.getLevel().registryAccess();
        var biomes = registries.registryOrThrow(Registries.BIOME);
        var nether = biomes.getHolderOrThrow(Biomes.NETHER_WASTES);
        var plains = biomes.getHolderOrThrow(Biomes.PLAINS);
        var config = WitcheryServerConfig.HELLHOUND_SPAWN_WEIGHT;
        int previous = config.get();
        helper.assertTrue(config.getDefault() == 25, "Hellhound weight defaults to 25");
        verifySpawn(helper, nether.value().getMobSettings(), previous);

        var key = ResourceKey.create(ForgeRegistries.Keys.BIOME_MODIFIERS,
                ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "hellhounds_in_the_nether"));
        var modifier = registries.registryOrThrow(ForgeRegistries.Keys.BIOME_MODIFIERS).getOrThrow(key);
        var original = nether.value().modifiableBiomeInfo().getOriginalBiomeInfo();
        var originalSpawns = original.mobSpawnSettings().getMobs(MobCategory.MONSTER).unwrap();
        int originalWeight = originalSpawns.stream().mapToInt(entry -> entry.getWeight().asInt()).sum();
        try {
            for (int customWeight : new int[]{1, 73, Integer.MAX_VALUE}) {
                config.set(customWeight);
                var builder = ModifiableBiomeInfo.BiomeInfo.Builder.copyOf(original);
                modifier.modify(nether, BiomeModifier.Phase.ADD, builder);
                verifySpawn(helper, builder.build().mobSpawnSettings(), Math.min(customWeight, Integer.MAX_VALUE - originalWeight));
            }
            var outsideNether = ModifiableBiomeInfo.BiomeInfo.Builder.copyOf(plains.value().modifiableBiomeInfo().getOriginalBiomeInfo());
            modifier.modify(plains, BiomeModifier.Phase.ADD, outsideNether);
            helper.assertTrue(outsideNether.getMobSpawnSettings().getSpawner(MobCategory.MONSTER).stream()
                    .noneMatch(entry -> entry.type == WitcheryEntities.HELLHOUND.get()), "The modifier only adds Hellhounds in tagged Nether biomes");
        } finally {
            config.set(previous);
        }
        helper.succeed();
    }

    private static void verifySpawn(GameTestHelper helper, MobSpawnSettings settings, int weight) {
        var hellhounds = settings.getMobs(MobCategory.MONSTER).unwrap().stream()
                .filter(entry -> entry.type == WitcheryEntities.HELLHOUND.get()).toList();
        helper.assertTrue(hellhounds.size() == 1, "Expected one Hellhound spawn entry");
        var spawn = hellhounds.get(0);
        helper.assertTrue(spawn.getWeight().asInt() == weight && spawn.minCount == 1 && spawn.maxCount == 3,
                "The Hellhound entry uses the configured weight and groups of one to three");
    }
}
