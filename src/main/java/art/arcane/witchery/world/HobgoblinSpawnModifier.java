package art.arcane.witchery.world;

import art.arcane.witchery.config.WitcheryServerConfig;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcheryRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;

import java.util.Optional;

public record HobgoblinSpawnModifier(HolderSet<Biome> biomes, int maxCount, boolean plainsWeight,
                                     Optional<HolderSet<Biome>> forestOverlap, Optional<HolderSet<Biome>> plainsOverlap,
                                     Optional<HolderSet<Biome>> earlierHabitats) implements BiomeModifier {
    public static final Codec<HobgoblinSpawnModifier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Biome.LIST_CODEC.fieldOf("biomes").forGetter(HobgoblinSpawnModifier::biomes),
            ExtraCodecs.POSITIVE_INT.fieldOf("max_count").forGetter(HobgoblinSpawnModifier::maxCount),
            Codec.BOOL.optionalFieldOf("plains_weight", false).forGetter(HobgoblinSpawnModifier::plainsWeight),
            Biome.LIST_CODEC.optionalFieldOf("forest_overlap").forGetter(HobgoblinSpawnModifier::forestOverlap),
            Biome.LIST_CODEC.optionalFieldOf("plains_overlap").forGetter(HobgoblinSpawnModifier::plainsOverlap),
            Biome.LIST_CODEC.optionalFieldOf("earlier_habitats").forGetter(HobgoblinSpawnModifier::earlierHabitats)
    ).apply(instance, HobgoblinSpawnModifier::new));

    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase != Phase.ADD || !biomes.contains(biome)) return;
        var type = WitcheryEntities.GOBLIN.get();
        int groupSize = maxCount;
        boolean reduceWeight = plainsWeight;
        // Legacy Forge replaced the first matching entry and then appended the new entry.
        if (earlierHabitats.filter(earlier -> earlier.contains(biome)).isEmpty()) {
            if (plainsOverlap.filter(plains -> plains.contains(biome)).isPresent()) {
                groupSize = 2;
                reduceWeight = true;
            } else if (forestOverlap.filter(forest -> forest.contains(biome)).isPresent()) {
                groupSize = 3;
            }
        }
        int weight = Math.max(1, WitcheryServerConfig.HOBGOBLIN_SPAWN_WEIGHT.get() - (reduceWeight ? 1 : 0));
        WitcheryCreatureSpawns.addWeightedSpawn(builder.getMobSpawnSettings(), type, weight, 1, groupSize);
    }

    @Override
    public Codec<? extends BiomeModifier> codec() {
        return WitcheryRegistries.HOBGOBLIN_SPAWNS.get();
    }
}
