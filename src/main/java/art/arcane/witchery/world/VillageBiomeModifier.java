package art.arcane.witchery.world;

import art.arcane.witchery.config.WitcheryServerConfig;
import art.arcane.witchery.registry.WitcheryRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.world.ModifiableStructureInfo;
import net.minecraftforge.common.world.StructureModifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.stream.Collectors;

public record VillageBiomeModifier(HolderSet<Biome> candidates, HolderSet<Structure> villages) implements StructureModifier {
    public static final Codec<VillageBiomeModifier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Biome.LIST_CODEC.fieldOf("candidates").forGetter(VillageBiomeModifier::candidates),
            RegistryCodecs.homogeneousList(Registries.STRUCTURE, Structure.DIRECT_CODEC)
                    .fieldOf("villages").forGetter(VillageBiomeModifier::villages)
    ).apply(instance, VillageBiomeModifier::new));

    @Override
    public void modify(Holder<Structure> structure, Phase phase, ModifiableStructureInfo.StructureInfo.Builder builder) {
        if (phase != Phase.ADD || !villages.contains(structure)) return;
        var style = structure.unwrapKey().map(key -> Style.forStructure(key.location())).orElse(null);
        if (style == null) return;

        // As in 1.7.10, the switches govern added biomes. Existing village eligibility comes from vanilla and datapacks.
        var assigned = villages.stream().flatMap(village -> village.value().modifiableStructureInfo()
                .getOriginalStructureInfo().structureSettings().biomes().stream()).collect(Collectors.toSet());
        var settings = builder.getStructureSettings();
        var present = settings.getBiomes();
        var added = candidates.stream().filter(biome -> !assigned.contains(biome) && !present.contains(biome))
                .filter(biome -> additionalStyle(biome) == style).toList();
        if (added.isEmpty()) return;
        var all = new ArrayList<>(present.stream().toList());
        all.addAll(added);
        settings.setBiomes(HolderSet.direct(all));
    }

    @Nullable
    static Style additionalStyle(Holder<Biome> biome) {
        boolean jungle = biome.is(BiomeTags.IS_JUNGLE);
        if (biome.is(BiomeTags.IS_OCEAN) || biome.is(BiomeTags.IS_BEACH) || biome.is(BiomeTags.IS_RIVER)
                || biome.is(BiomeTags.IS_NETHER) || biome.is(BiomeTags.IS_END) || biome.is(Tags.Biomes.IS_WATER)
                || biome.is(Tags.Biomes.IS_UNDERGROUND) || biome.is(Tags.Biomes.IS_SWAMP)) return null;
        if (jungle ? !WitcheryServerConfig.TOWN_BIOME_JUNGLE_ALLOWED.get() : biome.is(Tags.Biomes.IS_WET)) return null;

        boolean sandy = biome.is(Tags.Biomes.IS_SANDY);
        boolean snowy = biome.is(Tags.Biomes.IS_SNOWY);
        boolean mesa = biome.is(BiomeTags.IS_BADLANDS);
        boolean mountain = biome.is(BiomeTags.IS_MOUNTAIN) || biome.is(Tags.Biomes.IS_MOUNTAIN);
        boolean taiga = biome.is(BiomeTags.IS_TAIGA);
        boolean forest = taiga || biome.is(BiomeTags.IS_FOREST) || biome.is(Tags.Biomes.IS_CONIFEROUS);
        if (sandy && !WitcheryServerConfig.TOWN_BIOME_SANDY_ALLOWED.get()
                || biome.is(Tags.Biomes.IS_PLAINS) && !WitcheryServerConfig.TOWN_BIOME_PLAINS_ALLOWED.get()
                || mountain && !WitcheryServerConfig.TOWN_BIOME_MOUNTAIN_ALLOWED.get()
                || biome.is(BiomeTags.IS_HILL) && !WitcheryServerConfig.TOWN_BIOME_HILLS_ALLOWED.get()
                || forest && !WitcheryServerConfig.TOWN_BIOME_FOREST_ALLOWED.get()
                || snowy && !WitcheryServerConfig.TOWN_BIOME_SNOWY_ALLOWED.get()
                || biome.is(Tags.Biomes.IS_WASTELAND) && !WitcheryServerConfig.TOWN_BIOME_WASTELAND_ALLOWED.get()
                || mesa && !WitcheryServerConfig.TOWN_BIOME_MESA_ALLOWED.get()) return null;

        if (sandy || mesa) return Style.DESERT;
        if (snowy) return Style.SNOWY;
        if (biome.is(BiomeTags.IS_SAVANNA)) return Style.SAVANNA;
        if (taiga) return Style.TAIGA;
        return Style.PLAINS;
    }

    @Override
    public Codec<? extends StructureModifier> codec() {
        return WitcheryRegistries.VILLAGE_BIOMES.get();
    }

    enum Style {
        PLAINS, DESERT, SAVANNA, SNOWY, TAIGA;

        @Nullable
        static Style forStructure(ResourceLocation id) {
            if (!id.getNamespace().equals(ResourceLocation.DEFAULT_NAMESPACE)) return null;
            return switch (id.getPath()) {
                case "village_plains" -> PLAINS;
                case "village_desert" -> DESERT;
                case "village_savanna" -> SAVANNA;
                case "village_snowy" -> SNOWY;
                case "village_taiga" -> TAIGA;
                default -> null;
            };
        }
    }
}
