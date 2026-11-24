package art.arcane.witchery.block;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.entity.WitchesOvenBlockEntity;
import art.arcane.witchery.item.WitcheryIngredient;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class WitchesOvenFumeTests {
    @GameTest(template = "qa_coven_arena")
    public static void ovenReadsLegacyForestrySpeciesWithoutTheMod(GameTestHelper helper) {
        helper.assertTrue(WitchesOvenBlockEntity.fumeFor(legacyTree("forestry.treeOak"))
                        == WitcheryIngredient.EXHALE_OF_THE_HORNED_ONE,
                "Oak genomes give Exhale of the Horned One");
        helper.assertTrue(WitchesOvenBlockEntity.fumeFor(legacyTree("forestry.treeSpruce"))
                        == WitcheryIngredient.HINT_OF_REBIRTH,
                "Spruce genomes give Hint of Rebirth");
        helper.assertTrue(WitchesOvenBlockEntity.fumeFor(legacyTree("forestry.treeBirch"))
                        == WitcheryIngredient.BREATH_OF_THE_GODDESS,
                "Birch genomes give Breath of the Goddess");
        helper.assertTrue(WitchesOvenBlockEntity.fumeFor(legacyTree("forestry.treeLarch")) == null
                        && WitchesOvenBlockEntity.fumeFor(legacyTree("")) == null,
                "Other or incomplete genomes leave the jar empty");
        helper.assertTrue(WitchesOvenBlockEntity.fumeFor(new ItemStack(Items.JUNGLE_SAPLING)) == WitcheryIngredient.FOUL_FUME,
                "Ordinary approved saplings retain the generic fume");
        helper.succeed();
    }

    private static ItemStack legacyTree(String species) {
        var input = new ItemStack(Items.JUNGLE_SAPLING);
        var chromosomes = new ListTag();
        var active = new CompoundTag();
        active.putString("UID0", species);
        chromosomes.add(active);
        input.getOrCreateTagElement("Genome").put("Chromosomes", chromosomes);
        return input;
    }
}
