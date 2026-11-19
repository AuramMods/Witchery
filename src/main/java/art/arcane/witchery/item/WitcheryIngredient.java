package art.arcane.witchery.item;

import art.arcane.witchery.registry.WitcheryItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public enum WitcheryIngredient {
    ATTUNED_STONE(10, "attuned_stone"),
    CHARGED_ATTUNED_STONE(11, "charged_attuned_stone"),
    WOOD_ASH(18, "wood_ash"),
    SOFT_CLAY_JAR(26, "soft_clay_jar"),
    CLAY_JAR(27, "clay_jar"),
    FOUL_FUME(28, "foul_fume"),
    EXHALE_OF_THE_HORNED_ONE(31, "exhale_of_the_horned_one"),
    BREATH_OF_THE_GODDESS(32, "breath_of_the_goddess"),
    HINT_OF_REBIRTH(33, "hint_of_rebirth"),
    WHIFF_OF_MAGIC(34, "whiff_of_magic"),
    REEK_OF_MISFORTUNE(35, "reek_of_misfortune"),
    ODOUR_OF_PURITY(36, "odour_of_purity"),
    ROWAN_BERRIES(63, "rowan_berries"),
    FUME_FILTER(73, "fume_filter");

    private static final String LEGACY_ID = "witchery:ingredient";
    private static final String LEGACY_DAMAGE_TAG = "LegacyIngredientDamage";

    private final int legacyDamage;
    private final String path;

    WitcheryIngredient(int legacyDamage, String path) {
        this.legacyDamage = legacyDamage;
        this.path = path;
    }

    public String path() {
        return path;
    }

    public Item item() {
        return WitcheryItems.getRequired(path).get();
    }

    /** Loads an inventory stack after upgrading the legacy Witchery subtype, when present. */
    public static ItemStack loadInventoryStack(CompoundTag stackTag) {
        ItemStack stack = ItemStack.of(stackTag);
        if (stack.isEmpty() || !LEGACY_ID.equals(stackTag.getString("id"))) {
            return stack;
        }

        CompoundTag itemTag = stack.getTag();
        int damage;
        if (stackTag.contains("Damage", Tag.TAG_ANY_NUMERIC)) {
            damage = stackTag.getInt("Damage");
        } else if (itemTag != null && itemTag.contains(LEGACY_DAMAGE_TAG, Tag.TAG_ANY_NUMERIC)) {
            damage = itemTag.getInt(LEGACY_DAMAGE_TAG);
        } else {
            return stack;
        }

        for (WitcheryIngredient ingredient : values()) {
            if (ingredient.legacyDamage == damage) {
                ItemStack converted = new ItemStack(ingredient.item(), stack.getCount());
                if (itemTag != null) {
                    CompoundTag convertedTag = itemTag.copy();
                    convertedTag.remove(LEGACY_DAMAGE_TAG);
                    if (!convertedTag.isEmpty()) {
                        converted.setTag(convertedTag);
                    }
                }
                return converted;
            }
        }

        stack.getOrCreateTag().putInt(LEGACY_DAMAGE_TAG, damage);
        return stack;
    }
}
