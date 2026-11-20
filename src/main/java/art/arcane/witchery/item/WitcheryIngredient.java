package art.arcane.witchery.item;

import art.arcane.witchery.registry.WitcheryItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public enum WitcheryIngredient {
    CANDELABRA(0, "candelabra"),
    EMPTY_CHALICE(1, "chalice"),
    FILLED_CHALICE(2, "filled_chalice"),
    DREAMWEAVE_OF_FLEET_FEET(3, "dreamweave_of_fleet_feet"),
    DREAMWEAVE_OF_THE_IRON_ARM(4, "dreamweave_of_the_iron_arm"),
    DREAMWEAVE_OF_FASTING(5, "dreamweave_of_fasting"),
    DREAMWEAVE_OF_NIGHTMARES(6, "dreamweave_of_nightmares"),
    BONE_NEEDLE(7, "bone_needle"),
    ATTUNED_STONE(10, "attuned_stone"),
    CHARGED_ATTUNED_STONE(11, "charged_attuned_stone"),
    MUTANDIS(14, "mutandis"),
    MUTANDIS_EXTREMIS(15, "mutandis_extremis"),
    QUICKLIME(16, "quicklime"),
    GYPSUM(17, "gypsum"),
    WOOD_ASH(18, "wood_ash"),
    BELLADONNA_FLOWER(21, "belladonna_flower"),
    MANDRAKE_ROOT(22, "mandrake_root"),
    DEMON_HEART(23, "demon_heart"),
    BAT_WOOL(24, "bat_wool"),
    DOG_TONGUE(25, "dog_tongue"),
    SOFT_CLAY_JAR(26, "soft_clay_jar"),
    CLAY_JAR(27, "clay_jar"),
    FOUL_FUME(28, "foul_fume"),
    DIAMOND_VAPOUR(29, "diamond_vapour"),
    OIL_OF_VITRIOL(30, "oil_of_vitriol"),
    EXHALE_OF_THE_HORNED_ONE(31, "exhale_of_the_horned_one"),
    BREATH_OF_THE_GODDESS(32, "breath_of_the_goddess"),
    HINT_OF_REBIRTH(33, "hint_of_rebirth"),
    WHIFF_OF_MAGIC(34, "whiff_of_magic"),
    REEK_OF_MISFORTUNE(35, "reek_of_misfortune"),
    ODOUR_OF_PURITY(36, "odour_of_purity"),
    TEAR_OF_THE_GODDESS(37, "tear_of_the_goddess"),
    REFINED_EVIL(38, "refined_evil"),
    DROP_OF_LUCK(39, "drop_of_luck"),
    REDSTONE_SOUP(40, "redstone_soup"),
    ODD_PORKCHOP_RAW(50, "odd_porkchop_raw"),
    BREW_OF_INK(60, "brew_of_ink"),
    ROWAN_BERRIES(63, "rowan_berries"),
    NECROMANTIC_STONE(64, "necromantic_stone"),
    SPECTRAL_DUST(66, "spectral_dust"),
    ENDER_DEW(67, "ender_dew"),
    GLOBE_ARTICHOKE(69, "globe_artichoke"),
    BREW_OF_GROTESQUE(71, "brew_of_grotesque"),
    FUME_FILTER(73, "fume_filter"),
    CREEPER_HEART(74, "creeper_heart"),
    BREW_OF_LOVE(75, "brew_of_love"),
    BREW_OF_THE_DEPTHS(77, "brew_of_the_depths"),
    ICY_NEEDLE(78, "icy_needle"),
    INFERNAL_BLOOD(80, "infernal_blood"),
    ENT_BRANCH(82, "ent_branch"),
    OWLETS_WING(89, "owlets_wing"),
    TOE_OF_FROG(90, "toe_of_frog"),
    QUARTZ_SPHERE(92, "quartz_sphere"),
    HAPPENSTANCE_OIL(93, "happenstance_oil"),
    SEER_STONE(94, "seer_stone"),
    BREW_OF_SLEEPING(95, "brew_of_sleeping"),
    BREW_OF_FLOWING_SPIRIT(96, "brew_of_flowing_spirit"),
    DISTURBED_COTTON(99, "disturbed_cotton"),
    FANCIFUL_THREAD(100, "fanciful_thread"),
    TORMENTED_TWINE(101, "tormented_twine"),
    GOLDEN_THREAD(102, "golden_thread"),
    MELLIFLUOUS_HUNGER(103, "mellifluous_hunger"),
    DREAMWEAVE_OF_INTENSITY(104, "dreamweave_of_intensity"),
    PURIFIED_MILK(105, "purified_milk"),
    CHARM_OF_DISRUPTED_DREAMS(110, "charm_of_disrupted_dreams"),
    WORMWOOD(111, "wormwood_leaf"),
    FOCUSED_WILL(113, "focused_will"),
    CONDENSED_FEAR(114, "condensed_fear"),
    BREW_OF_HOLLOW_TEARS(115, "brew_of_hollow_tears"),
    BREW_SOUL_HUNGER(136, "brew_of_soul_hunger"),
    BREW_SOUL_ANGUISH(137, "brew_of_soul_anguish"),
    BREW_SOUL_FEAR(138, "brew_of_soul_fear"),
    BREW_SOUL_TORMENT(139, "brew_of_soul_torment"),
    CONTRACT_OF_OWNERSHIP(140, "contract_of_ownership"),
    CONTRACT_OF_TORMENT(141, "contract_of_torment"),
    ANOINTING_PASTE(153, "anointing_paste"),
    WOLFSBANE(156, "wolfsbane_flower");

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
