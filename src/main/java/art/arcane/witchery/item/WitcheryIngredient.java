package art.arcane.witchery.item;

import art.arcane.witchery.block.StockadeBlock;
import net.minecraft.world.item.BlockItem;
import art.arcane.witchery.block.PlantMineBlock;
import art.arcane.witchery.block.ShadedGlassBlock;
import art.arcane.witchery.block.WickerBundleBlock;
import art.arcane.witchery.block.CritterSnareBlock;
import art.arcane.witchery.block.BrambleBlock;
import art.arcane.witchery.brewing.LousePotion;

import art.arcane.witchery.registry.WitcheryItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;

public enum WitcheryIngredient {
    CANDELABRA(0, "candelabra"),
    EMPTY_CHALICE(1, "chalice"),
    FILLED_CHALICE(2, "filled_chalice"),
    DREAMWEAVE_OF_FLEET_FEET(3, "dreamweave_of_fleet_feet"),
    DREAMWEAVE_OF_THE_IRON_ARM(4, "dreamweave_of_the_iron_arm"),
    DREAMWEAVE_OF_FASTING(5, "dreamweave_of_fasting"),
    DREAMWEAVE_OF_NIGHTMARES(6, "dreamweave_of_nightmares"),
    BONE_NEEDLE(7, "bone_needle"),
    BROOM(8, "broom"),
    ENCHANTED_BROOM(9, "enchanted_broom"),
    ATTUNED_STONE(10, "attuned_stone"),
    CHARGED_ATTUNED_STONE(11, "charged_attuned_stone"),
    WAYSTONE(12, "waystone"),
    BOUND_WAYSTONE(13, "bound_waystone"),
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
    FLYING_OINTMENT(41, "flying_ointment"),
    GHOST_OF_THE_LIGHT(42, "ghost_of_the_light"),
    SOUL_OF_THE_WORLD(43, "soul_of_the_world"),
    SPIRIT_OF_OTHERWHERE(44, "spirit_of_otherwhere"),
    INFERNAL_ANIMUS(45, "infernal_animus"),
    BOOK_COLLECTING_FUMES(46, "book_collecting_fumes"),
    BOOK_DISTILLING(47, "book_distilling"),
    BOOK_CIRCLE_MAGIC(48, "book_circle_magic"),
    BOOK_BREWS_AND_INFUSIONS(49, "book_brews_and_infusions"),
    ODD_PORKCHOP_RAW(50, "odd_porkchop_raw"),
    ODD_PORKCHOP_COOKED(51, "odd_porkchop_cooked"),
    ROWAN_DOOR(52, "rowanwooddoor"),
    ALDER_DOOR(53, "alderwooddoor"),
    DOOR_KEY(54, "door_key"),
    ROCK(55, "rock"),
    WEB(56, "thrown_web"),
    BREW_OF_VINES(57, "brew_of_vines"),
    BREW_OF_WEBS(58, "brew_of_webs"),
    BREW_OF_THORNS(59, "brew_of_thorns"),
    BREW_OF_INK(60, "brew_of_ink"),
    BREW_OF_SPROUTING(61, "brew_of_sprouting"),
    BREW_OF_EROSION(62, "brew_of_erosion"),
    ROWAN_BERRIES(63, "rowan_berries"),
    NECROMANTIC_STONE(64, "necromantic_stone"),
    BREW_OF_RAISING(65, "brew_of_raising"),
    SPECTRAL_DUST(66, "spectral_dust"),
    ENDER_DEW(67, "ender_dew"),
    GLOBE_ARTICHOKE(69, "globe_artichoke"),
    TREEFYD_SEEDS(70, "treefyd_seeds"),
    BREW_OF_GROTESQUE(71, "brew_of_grotesque"),
    IMPREGNATED_LEATHER(72, "impregnated_leather"),
    FUME_FILTER(73, "fume_filter"),
    CREEPER_HEART(74, "creeper_heart"),
    BREW_OF_LOVE(75, "brew_of_love"),
    BREW_OF_ICE(76, "brew_of_ice"),
    BREW_OF_THE_DEPTHS(77, "brew_of_the_depths"),
    ICY_NEEDLE(78, "icy_needle"),
    FROZEN_HEART(79, "frozen_heart"),
    INFERNAL_BLOOD(80, "infernal_blood"),
    BOOK_HERBOLOGY(81, "book_herbology"),
    ENT_BRANCH(82, "ent_branch"),
    MYSTIC_UNGUENT(83, "mystic_unguent"),
    DOOR_KEYRING(84, "door_keyring"),
    BREW_OF_FROGS_TONGUE(85, "brew_of_frogs_tongue"),
    BREW_OF_CURSED_LEAPING(86, "brew_of_cursed_leaping"),
    BREW_OF_HITCHCOCK(87, "brew_of_hitchcock"),
    BREW_OF_INFECTION(88, "brew_of_infection"),
    OWLETS_WING(89, "owlets_wing"),
    TOE_OF_FROG(90, "toe_of_frog"),
    WORMY_APPLE(91, "wormy_apple"),
    QUARTZ_SPHERE(92, "quartz_sphere"),
    HAPPENSTANCE_OIL(93, "happenstance_oil"),
    SEER_STONE(94, "seer_stone"),
    BREW_OF_SLEEPING(95, "brew_of_sleeping"),
    BREW_OF_FLOWING_SPIRIT(96, "brew_of_flowing_spirit"),
    BREW_OF_WASTING(97, "brew_of_wasting"),
    SLEEPING_APPLE(98, "sleeping_apple"),
    DISTURBED_COTTON(99, "disturbed_cotton"),
    FANCIFUL_THREAD(100, "fanciful_thread"),
    TORMENTED_TWINE(101, "tormented_twine"),
    GOLDEN_THREAD(102, "golden_thread"),
    MELLIFLUOUS_HUNGER(103, "mellifluous_hunger"),
    DREAMWEAVE_OF_INTENSITY(104, "dreamweave_of_intensity"),
    PURIFIED_MILK(105, "purified_milk"),
    BOOK_OF_BIOMES(106, "book_of_biomes"),
    BOOK_SYMBOLOGY(107, "book_symbology"),
    BAT_BALL(108, "bat_ball"),
    BREW_OF_BATS(109, "brew_of_bats"),
    CHARM_OF_DISRUPTED_DREAMS(110, "charm_of_disrupted_dreams"),
    WORMWOOD(111, "wormwood_leaf"),
    SUBDUED_SPIRIT(112, "subdued_spirit"),
    FOCUSED_WILL(113, "focused_will"),
    CONDENSED_FEAR(114, "condensed_fear"),
    BREW_OF_HOLLOW_TEARS(115, "brew_of_hollow_tears"),
    BREW_OF_SOLID_ROCK(116, "brew_of_solid_rock"),
    BREW_OF_SOLID_DIRT(117, "brew_of_solid_dirt"),
    BREW_OF_SOLID_SAND(118, "brew_of_solid_sand"),
    BREW_OF_SOLID_SANDSTONE(119, "brew_of_solid_sandstone"),
    BREW_OF_SOLID_EROSION(120, "brew_of_solid_erosion"),
    INFUSED_BREW_BASE(121, "infused_brew_base"),
    BREW_OF_SOARING(122, "brew_of_soaring"),
    BREW_OF_THE_GRAVE(123, "brew_of_the_grave"),
    BREW_OF_REVEALING(124, "brew_of_revealing"),
    BREW_OF_SUBSTITUTION(125, "brew_of_substitution"),
    CONGEALED_SPIRIT(126, "congealed_spirit"),
    BOOK_CONJURATION_AND_FETISHES(127, "book_conjuration_and_fetishes"),
    GRAVEYARD_DUST(128, "graveyard_dust"),
    BINKY_HEAD(129, "binky_head"),
    NULL_CATALYST(130, "null_catalyst"),
    NULLIFIED_LEATHER(131, "nullified_leather"),
    STAKE_BOLT(132, "stake_bolt"),
    ANTI_MAGIC_BOLT(133, "anti_magic_bolt"),
    HOLY_BOLT(134, "holy_bolt"),
    SPLITTING_BOLT(135, "splitting_bolt"),
    BREW_SOUL_HUNGER(136, "brew_of_soul_hunger"),
    BREW_SOUL_ANGUISH(137, "brew_of_soul_anguish"),
    BREW_SOUL_FEAR(138, "brew_of_soul_fear"),
    BREW_SOUL_TORMENT(139, "brew_of_soul_torment"),
    CONTRACT_OF_OWNERSHIP(140, "contract_of_ownership"),
    CONTRACT_OF_TORMENT(141, "contract_of_torment"),
    CONTRACT_LIVING_FLAME(142, "contract_living_flame"),
    CONTRACT_FIERY_TOLERANCE(143, "contract_fiery_tolerance"),
    CONTRACT_EVAPORATION(144, "contract_evaporation"),
    CONTRACT_FIERY_TOUCH(145, "contract_fiery_touch"),
    CONTRACT_MELTING_TOUCH(146, "contract_melting_touch"),
    CREATURE_BOUND_WAYSTONE(147, "creature_bound_waystone"),
    KOBOLDITE_DUST(148, "koboldite_dust"),
    KOBOLDITE_NUGGET(149, "koboldite_nugget"),
    KOBOLDITE_INGOT(150, "koboldite_ingot"),
    KOBOLDITE_PENTACLE(151, "koboldite_pentacle"),
    ICE_DOOR(152, "icedoor"),
    ANOINTING_PASTE(153, "anointing_paste"),
    SUBDUED_VILLAGE_SPIRIT(154, "subdued_village_spirit"),
    SILVER_BOLT(155, "silver_bolt"),
    WOLFSBANE(156, "wolfsbane_flower"),
    SILVER_DUST(157, "silver_dust"),
    TORN_VAMPIRE_PAGE(160, "torn_vampire_page"),
    DARK_CLOTH(161, "dark_cloth"),
    WARM_BLOOD(163, "warm_blood"),
    LILITHS_BLOOD(164, "liliths_blood"),
    HEART_OF_GOLD(165, "heart_of_gold");

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

    public boolean matches(ItemStack stack) {
        if (stack.is(item())) return true;
        var tag = stack.getTag();
        return stack.is(WitcheryItems.getRequired("ingredient").get()) && tag != null
                && tag.contains(LEGACY_DAMAGE_TAG, Tag.TAG_ANY_NUMERIC) && tag.getInt(LEGACY_DAMAGE_TAG) == legacyDamage;
    }

    /** Loads an inventory stack after upgrading the legacy Witchery subtype, when present. */
    public static ItemStack loadInventoryStack(CompoundTag stackTag) {
        boolean hasLegacyDamage = stackTag.contains("Damage", Tag.TAG_ANY_NUMERIC);
        // Deserializing a damageable item can initialize its nested Damage field.
        boolean hasModernDamage = hasLegacyDamage && stackTag.getCompound("tag").contains("Damage", Tag.TAG_ANY_NUMERIC);
        ItemStack stack = ItemStack.of(stackTag);
        if (stack.is(Items.POTION) && stackTag.getInt("Damage") == 8198
                && (!stack.hasTag() || !stack.getTag().contains("Potion", Tag.TAG_STRING))) {
            return PotionUtils.setPotion(stack, Potions.NIGHT_VISION);
        }
        if (hasLegacyDamage) {
            if (stack.getItem() instanceof WitchWoodBlockItem) {
                WitchWoodBlockItem.importLegacyMetadata(stack, stackTag.getInt("Damage"));
            }
            if (stack.getItem() instanceof ShadedGlassBlock.Item) {
                ShadedGlassBlock.Color.importLegacyMetadata(stack, stackTag.getInt("Damage"));
            }
            if (stack.getItem() instanceof StockadeBlock.Item
                    && (stack.getTagElement(BlockItem.BLOCK_STATE_TAG) == null
                    || !stack.getTagElement(BlockItem.BLOCK_STATE_TAG).contains(StockadeBlock.WOOD.getName(), Tag.TAG_STRING))) {
                StockadeBlock.Wood.importLegacyMetadata(stack, stackTag.getInt("Damage"));
            }
            if (stack.getItem() instanceof ParasyticLouseItem
                    && (!stack.hasTag() || !stack.getTag().contains(LousePotion.TAG, Tag.TAG_ANY_NUMERIC))) {
                LousePotion.set(stack, stackTag.getInt("Damage"));
            }
            if (stack.getItem() instanceof SpectralStoneItem && SpectralStoneItem.typeId(stack) == 0) {
                SpectralStoneItem.applyLegacyContents(stack, stackTag.getInt("Damage"));
            }
            if (stack.getItem() instanceof LeonardsUrnItem) LeonardsUrnItem.setTier(stack, stackTag.getInt("Damage"));
            if (stack.getItem() instanceof BrewFuelItem) BrewFuelItem.setTier(stack, stackTag.getInt("Damage"));
            if (stack.getItem() instanceof BiomeNoteItem) BiomeNoteItem.importPage(stack, stackTag.getInt("Damage"));
            if (stack.getItem() instanceof CircleTalismanItem && CircleTalismanItem.storedPattern(stack) == 0) {
                CircleTalismanItem.setStoredPattern(stack, stackTag.getInt("Damage"));
            }
            if (stack.getItem() instanceof EndlessWaterBrewItem) {
                stack.setDamageValue(Math.max(0, Math.min(stackTag.getInt("Damage"), stack.getMaxDamage() - 1)));
            }
            if (stack.getItem() instanceof DivinerItem && !hasModernDamage) {
                stack.setDamageValue(Math.max(0, Math.min(stackTag.getInt("Damage"), stack.getMaxDamage())));
            }
        }
        if (!stack.isEmpty() && "witchery:wolfhead".equals(stackTag.getString("id")) && stackTag.getInt("Damage") == 1) {
            ItemStack head = new ItemStack(WitcheryItems.getRequired("hellhound_head").get(), stack.getCount());
            if (stack.hasTag()) head.setTag(stack.getTag().copy());
            return head;
        }
        if (!stack.isEmpty() && "witchery:crittersnare".equals(stackTag.getString("id"))) {
            CritterSnareBlock.applyLegacyMetadata(stack, stackTag.getInt("Damage"));
            return stack;
        }
        if (stack.getItem() instanceof PlantMineBlock.Item && stackTag.contains("Damage", Tag.TAG_ANY_NUMERIC)) {
            PlantMineBlock.Variant.applyLegacyMetadata(stack, stackTag.getInt("Damage"));
            return stack;
        }
        if (stack.getItem() instanceof WickerBundleBlock.Item && (stackTag.getInt("Damage") & 1) != 0) {
            WickerBundleBlock.markBloodied(stack);
            return stack;
        }
        if (stack.getItem() instanceof BrambleBlock.Item && stackTag.contains("Damage", Tag.TAG_ANY_NUMERIC)) {
            BrambleBlock.setWild(stack, stackTag.getInt("Damage") == 1);
            return stack;
        }
        if ((stack.getItem() instanceof WitchClothingItem || stack.getItem() instanceof DeathClothingItem
                || stack.getItem() instanceof MoonCharmItem)
                && stackTag.contains("Damage", Tag.TAG_ANY_NUMERIC)) {
            stack.setDamageValue(Math.min(stackTag.getInt("Damage"), stack.getMaxDamage()));
            return stack;
        }
        if (stack.getItem() instanceof VampireBookItem && stackTag.contains("Damage", Tag.TAG_ANY_NUMERIC)) {
            VampireBookItem.setTornPages(stack, stackTag.getInt("Damage"));
            return stack;
        }
        if (!stack.isEmpty() && "witchery:poppet".equals(stackTag.getString("id"))) {
            String poppet = switch (stackTag.getInt("Damage")) {
                case 1 -> "earth_protection_poppet";
                case 2 -> "water_protection_poppet";
                case 3 -> "fire_protection_poppet";
                case 4 -> "hunger_protection_poppet";
                case 5 -> "tool_protection_poppet";
                case 6 -> "death_protection_poppet";
                case 7 -> "voodoo_protection_poppet";
                case 8 -> "voodoo_poppet";
                case 9 -> "vampiric_poppet";
                case 10 -> "poppet_protection";
                case 11 -> "armor_protection_poppet";
                default -> null;
            };
            if (poppet == null) return stack;
            ItemStack subtype = new ItemStack(WitcheryItems.getRequired(poppet).get(), stack.getCount());
            if (stack.hasTag()) subtype.setTag(stack.getTag().copy());
            return subtype;
        }
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

        Item upgraded = switch (damage) {
            case 158 -> Items.MUTTON;
            case 159 -> Items.COOKED_MUTTON;
            default -> null;
        };
        if (upgraded == null) {
            for (WitcheryIngredient ingredient : values()) {
                if (ingredient.legacyDamage == damage) {
                    upgraded = ingredient.item();
                    break;
                }
            }
        }

        if (upgraded != null) {
            ItemStack converted = new ItemStack(upgraded, stack.getCount());
            if (itemTag != null) {
                CompoundTag convertedTag = itemTag.copy();
                convertedTag.remove(LEGACY_DAMAGE_TAG);
                if (!convertedTag.isEmpty()) converted.setTag(convertedTag);
            }
            if (hasLegacyDamage && GuideBookItem.volume(converted) == GuideBookItem.Volume.CIRCLES) {
                CircleBookIndex.importLegacyBookmark(converted);
            }
            return converted;
        }

        stack.getOrCreateTag().putInt(LEGACY_DAMAGE_TAG, damage);
        return stack;
    }
}
