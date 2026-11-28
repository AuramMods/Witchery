package art.arcane.witchery.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

/** The bundled Dream definition inherits its terrain when Minecraft resolves the world dimensions. */
public final class DreamChunkGenerator extends NoiseBasedChunkGenerator {
    public static final Codec<DreamChunkGenerator> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            BiomeSource.CODEC.fieldOf("biome_source").forGetter(DreamChunkGenerator::getBiomeSource),
            NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(DreamChunkGenerator::generatorSettings)
    ).apply(instance, DreamChunkGenerator::new));

    private DreamChunkGenerator(BiomeSource source, Holder<NoiseGeneratorSettings> settings) {
        super(source, settings);
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }
}
