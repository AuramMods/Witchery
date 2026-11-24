package art.arcane.witchery.integration;

import art.arcane.witchery.config.WitcheryCommonConfig;
import art.arcane.witchery.item.WitcheryIngredient;
import forestry.api.IForestryApi;
import forestry.api.arboriculture.genetics.ITreeSpeciesType;
import forestry.api.arboriculture.genetics.TreeLifeStage;
import forestry.api.genetics.ForestrySpeciesTypes;
import forestry.api.genetics.capability.IIndividualHandlerItem;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class ForestrySaplings {
    private static final ResourceLocation SAPLING = ResourceLocation.fromNamespaceAndPath("forestry", "sapling");

    private ForestrySaplings() {}

    public static boolean isSapling(ItemStack stack) {
        return SAPLING.equals(ForgeRegistries.ITEMS.getKey(stack.getItem()));
    }

    @Nullable
    public static WitcheryIngredient fumeFor(ItemStack stack) {
        String species = "";
        var tag = stack.getTag();
        if (tag != null && tag.contains("Genome")) {
            species = tag.getCompound("Genome").getList("Chromosomes", Tag.TAG_COMPOUND).getCompound(0).getString("UID0");
        } else if (isSapling(stack) && available()) {
            species = Arboriculture.activeSpecies(stack);
        }
        return switch (species) {
            case "forestry.treeOak", "forestry:tree_oak" -> WitcheryIngredient.EXHALE_OF_THE_HORNED_ONE;
            case "forestry.treeSpruce", "forestry:tree_spruce" -> WitcheryIngredient.HINT_OF_REBIRTH;
            case "forestry.treeBirch", "forestry:tree_birch" -> WitcheryIngredient.BREATH_OF_THE_GODDESS;
            default -> null;
        };
    }

    public static List<ItemStack> variants() {
        return available() ? Arboriculture.variants() : List.of();
    }

    private static boolean available() {
        return ModList.get().isLoaded("forestry") && WitcheryCommonConfig.ALLOW_MOD_INTEGRATION.get();
    }

    private static final class Arboriculture {
        private static String activeSpecies(ItemStack stack) {
            var individual = IIndividualHandlerItem.getIndividual(stack);
            return individual == null ? "" : individual.getSpecies().id().toString();
        }

        private static List<ItemStack> variants() {
            var trees = IForestryApi.INSTANCE.getGeneticManager().getSpeciesType(ForestrySpeciesTypes.TREE, ITreeSpeciesType.class);
            return trees.getAllSpecies().stream().map(species -> species.createStack(TreeLifeStage.SAPLING)).toList();
        }
    }
}
