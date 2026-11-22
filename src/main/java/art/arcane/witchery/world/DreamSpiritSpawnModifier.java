package art.arcane.witchery.world;

import art.arcane.witchery.config.WitcheryServerConfig;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcheryRegistries;
import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;

public record DreamSpiritSpawnModifier(HolderSet<Biome> biomes) implements BiomeModifier {
    public static final Codec<DreamSpiritSpawnModifier> CODEC = Biome.LIST_CODEC.fieldOf("biomes")
            .xmap(DreamSpiritSpawnModifier::new, DreamSpiritSpawnModifier::biomes).codec();

    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase != Phase.ADD || !biomes.contains(biome)) return;
        var spirit = WitcheryEntities.SPIRIT.get();
        var spawn = new MobSpawnSettings.SpawnerData(spirit, WitcheryServerConfig.SPIRIT_SPAWN_WEIGHT.get(), 2, 5);
        builder.getMobSpawnSettings().addSpawn(spirit.getCategory(), spawn);
    }

    @Override
    public Codec<? extends BiomeModifier> codec() {
        return WitcheryRegistries.DREAM_SPIRIT_SPAWNS.get();
    }
}
