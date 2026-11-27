package art.arcane.witchery.world;

import art.arcane.witchery.config.WitcheryServerConfig;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcheryRegistries;
import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;

public record HellhoundSpawnModifier(HolderSet<Biome> biomes) implements BiomeModifier {
    public static final Codec<HellhoundSpawnModifier> CODEC = Biome.LIST_CODEC.fieldOf("biomes")
            .xmap(HellhoundSpawnModifier::new, HellhoundSpawnModifier::biomes).codec();

    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase != Phase.ADD || !biomes.contains(biome)) return;
        WitcheryCreatureSpawns.addWeightedSpawn(builder.getMobSpawnSettings(), WitcheryEntities.HELLHOUND.get(),
                WitcheryServerConfig.HELLHOUND_SPAWN_WEIGHT.get(), 1, 3);
    }

    @Override
    public Codec<? extends BiomeModifier> codec() {
        return WitcheryRegistries.HELLHOUND_SPAWNS.get();
    }
}
