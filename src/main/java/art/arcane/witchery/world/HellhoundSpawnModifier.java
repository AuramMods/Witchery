package art.arcane.witchery.world;

import art.arcane.witchery.config.WitcheryServerConfig;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcheryRegistries;
import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;

public record HellhoundSpawnModifier(HolderSet<Biome> biomes) implements BiomeModifier {
    public static final Codec<HellhoundSpawnModifier> CODEC = Biome.LIST_CODEC.fieldOf("biomes")
            .xmap(HellhoundSpawnModifier::new, HellhoundSpawnModifier::biomes).codec();

    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase != Phase.ADD || !biomes.contains(biome)) return;
        var hellhound = WitcheryEntities.HELLHOUND.get();
        var category = hellhound.getCategory();
        var spawns = builder.getMobSpawnSettings();
        int available = Integer.MAX_VALUE - WeightedRandom.getTotalWeight(spawns.getSpawner(category));
        int weight = Math.min(available, WitcheryServerConfig.HELLHOUND_SPAWN_WEIGHT.get());
        if (weight > 0) spawns.addSpawn(category, new MobSpawnSettings.SpawnerData(hellhound, weight, 1, 3));
    }

    @Override
    public Codec<? extends BiomeModifier> codec() {
        return WitcheryRegistries.HELLHOUND_SPAWNS.get();
    }
}
