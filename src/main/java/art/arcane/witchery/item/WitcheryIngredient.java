package art.arcane.witchery.item;

import art.arcane.witchery.registry.WitcheryItems;
import net.minecraft.world.item.Item;

public enum WitcheryIngredient {
    ATTUNED_STONE("attuned_stone"),
    CHARGED_ATTUNED_STONE("charged_attuned_stone"),
    WOOD_ASH("wood_ash"),
    SOFT_CLAY_JAR("soft_clay_jar"),
    CLAY_JAR("clay_jar"),
    FOUL_FUME("foul_fume"),
    EXHALE_OF_THE_HORNED_ONE("exhale_of_the_horned_one"),
    BREATH_OF_THE_GODDESS("breath_of_the_goddess"),
    HINT_OF_REBIRTH("hint_of_rebirth"),
    WHIFF_OF_MAGIC("whiff_of_magic"),
    REEK_OF_MISFORTUNE("reek_of_misfortune"),
    ODOUR_OF_PURITY("odour_of_purity"),
    FUME_FILTER("fume_filter");

    private final String path;

    WitcheryIngredient(String path) {
        this.path = path;
    }

    public String path() {
        return path;
    }

    public Item item() {
        return WitcheryItems.getRequired(path).get();
    }
}
