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
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;

public record HobgoblinSpawnModifier(HolderSet<Biome> biomes, int maxCount, boolean plainsWeight) implements BiomeModifier {
    public static final Codec<HobgoblinSpawnModifier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Biome.LIST_CODEC.fieldOf("biomes").forGetter(HobgoblinSpawnModifier::biomes),
            ExtraCodecs.POSITIVE_INT.fieldOf("max_count").forGetter(HobgoblinSpawnModifier::maxCount),
            Codec.BOOL.optionalFieldOf("plains_weight", false).forGetter(HobgoblinSpawnModifier::plainsWeight)
    ).apply(instance, HobgoblinSpawnModifier::new));

    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase != Phase.ADD || !biomes.contains(biome)) return;
        var type = WitcheryEntities.GOBLIN.get();
        int weight = Math.max(1, WitcheryServerConfig.HOBGOBLIN_SPAWN_WEIGHT.get() - (plainsWeight ? 1 : 0));
        builder.getMobSpawnSettings().addSpawn(type.getCategory(), new MobSpawnSettings.SpawnerData(type, weight, 1, maxCount));
    }

    @Override
    public Codec<? extends BiomeModifier> codec() {
        return WitcheryRegistries.HOBGOBLIN_SPAWNS.get();
    }
}
