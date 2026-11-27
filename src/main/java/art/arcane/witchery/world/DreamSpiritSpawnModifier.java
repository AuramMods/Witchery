package art.arcane.witchery.world;

import art.arcane.witchery.config.WitcheryServerConfig;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcheryRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;

import java.util.List;

public record DreamSpiritSpawnModifier(HolderSet<Biome> biomes, int weightMultiplier,
                                       List<HolderSet<Biome>> moddedHabitats) implements BiomeModifier {
    public static final Codec<DreamSpiritSpawnModifier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Biome.LIST_CODEC.fieldOf("biomes").forGetter(DreamSpiritSpawnModifier::biomes),
            Codec.intRange(1, 9).optionalFieldOf("weight_multiplier", 1).forGetter(DreamSpiritSpawnModifier::weightMultiplier),
            Biome.LIST_CODEC.listOf().optionalFieldOf("modded_habitats", List.of()).forGetter(DreamSpiritSpawnModifier::moddedHabitats)
    ).apply(instance, DreamSpiritSpawnModifier::new));

    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase != Phase.ADD || !biomes.contains(biome)) return;
        long entries = 1;
        if (!moddedHabitats.isEmpty()) {
            var key = biome.unwrapKey();
            if (key.isEmpty() || key.get().location().getNamespace().equals("minecraft")) return;
            entries = moddedHabitats.stream().filter(habitat -> habitat.contains(biome)).count();
        }
        long weight = entries * weightMultiplier * WitcheryServerConfig.SPIRIT_SPAWN_WEIGHT.get();
        WitcheryCreatureSpawns.addWeightedSpawn(builder.getMobSpawnSettings(), WitcheryEntities.SPIRIT.get(), weight, 2, 5);
    }

    @Override
    public Codec<? extends BiomeModifier> codec() {
        return WitcheryRegistries.DREAM_SPIRIT_SPAWNS.get();
    }
}
