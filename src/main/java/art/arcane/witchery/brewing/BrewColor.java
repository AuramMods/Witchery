package art.arcane.witchery.brewing;

import art.arcane.witchery.item.WitcheryIngredient;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class BrewColor {
    private static final Map<ResourceLocation, Integer> LEGACY_HASHES = legacyHashes();

    private BrewColor() {}

    public static int append(int previous, ItemStack ingredient) {
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(ingredient.getItem());
        int hash = LEGACY_HASHES.getOrDefault(id, id.hashCode());
        return 37 * (previous == 0 ? 17 : previous) + hash;
    }

    public static int fromIngredients(List<ItemStack> ingredients) {
        int color = 0;
        for (ItemStack ingredient : ingredients) {
            int wool = BrewMixture.woolColor(ingredient);
            color = wool >= 0 ? wool : append(color, ingredient);
        }
        return color;
    }

    @Nullable
    public static Integer saved(@Nullable CompoundTag tag) {
        return tag != null && tag.contains("Color", Tag.TAG_ANY_NUMERIC) ? tag.getInt("Color") : null;
    }

    private static Map<ResourceLocation, Integer> legacyHashes() {
        var keys = new HashMap<ResourceLocation, Integer>();
        // Witchery 0.24.1 hashes the original base item's translation key without its metadata.
        add(keys, "item.apple", "apple");
        add(keys, "item.beefRaw", "beef");
        add(keys, "item.blazePowder", "blaze_powder");
        add(keys, "item.blazeRod", "blaze_rod");
        add(keys, "item.bone", "bone");
        add(keys, "item.brick", "brick");
        add(keys, "tile.mushroom", "brown_mushroom", "red_mushroom");
        add(keys, "tile.cactus", "cactus");
        add(keys, "item.chickenRaw", "chicken");
        add(keys, "item.clay", "clay_ball");
        add(keys, "item.coal", "coal", "charcoal");
        add(keys, "tile.stonebrick", "cobblestone");
        add(keys, "item.compass", "compass");
        add(keys, "tile.deadbush", "dead_bush");
        add(keys, "item.diamond", "diamond");
        add(keys, "tile.dirt", "dirt");
        add(keys, "tile.doublePlant", "sunflower");
        add(keys, "item.egg", "egg");
        add(keys, "item.emerald", "emerald");
        add(keys, "tile.whiteStone", "end_stone");
        add(keys, "item.eyeOfEnder", "ender_eye");
        add(keys, "item.enderPearl", "ender_pearl");
        add(keys, "item.feather", "feather");
        add(keys, "item.fermentedSpiderEye", "fermented_spider_eye");
        add(keys, "item.fish", "cod", "salmon", "tropical_fish", "pufferfish");
        add(keys, "item.flint", "flint");
        add(keys, "item.ghastTear", "ghast_tear");
        add(keys, "item.yellowDust", "glowstone_dust");
        add(keys, "item.ingotGold", "gold_ingot");
        add(keys, "item.goldNugget", "gold_nugget");
        add(keys, "item.appleGold", "golden_apple", "enchanted_golden_apple");
        add(keys, "item.carrotGolden", "golden_carrot");
        add(keys, "tile.gravel", "gravel");
        add(keys, "item.sulphur", "gunpowder");
        add(keys, "tile.ice", "ice");
        add(keys, "item.ingotIron", "iron_ingot");
        add(keys, "item.leather", "leather");
        add(keys, "item.magmaCream", "magma_cream");
        add(keys, "item.netherStar", "nether_star");
        add(keys, "item.netherStalkSeeds", "nether_wart");
        add(keys, "item.netherbrick", "nether_brick");
        add(keys, "tile.hellrock", "netherrack");
        add(keys, "tile.obsidian", "obsidian");
        add(keys, "item.potatoPoisonous", "poisonous_potato");
        add(keys, "item.porkchopRaw", "porkchop");
        add(keys, "tile.pumpkin", "pumpkin");
        add(keys, "item.netherquartz", "quartz");
        add(keys, "tile.flower2", "poppy", "blue_orchid");
        add(keys, "item.redstone", "redstone");
        add(keys, "item.reeds", "sugar_cane");
        add(keys, "item.rottenFlesh", "rotten_flesh");
        add(keys, "tile.sand", "sand");
        add(keys, "tile.sapling", "oak_sapling", "spruce_sapling", "birch_sapling", "jungle_sapling",
                "acacia_sapling", "dark_oak_sapling");
        add(keys, "item.skull", "skeleton_skull", "wither_skeleton_skull", "zombie_head", "creeper_head");
        add(keys, "item.slimeball", "slime_ball");
        add(keys, "tile.snow", "snow_block");
        add(keys, "item.snowball", "snowball");
        add(keys, "tile.hellsand", "soul_sand");
        add(keys, "item.speckledMelon", "glistering_melon_slice");
        add(keys, "item.spiderEye", "spider_eye");
        add(keys, "item.stick", "stick");
        add(keys, "tile.stone", "stone");
        add(keys, "item.string", "string");
        add(keys, "item.sugar", "sugar");
        add(keys, "tile.vine", "vine");
        add(keys, "tile.waterlily", "lily_pad");
        add(keys, "tile.web", "cobweb");
        add(keys, "item.wheat", "wheat");
        add(keys, "item.seeds", "wheat_seeds");
        add(keys, "tile.flower1", "dandelion");
        add(keys, "item.witchery:biomenote", "witchery:biomenote");
        add(keys, "tile.witchery:bramble", "witchery:bramble");
        add(keys, "item.witchery:chalkritual", "witchery:chalkritual");
        add(keys, "tile.witchery:embermoss", "witchery:embermoss");
        add(keys, "item.witchery:garlic", "witchery:garlic");
        add(keys, "tile.witchery:glintweed", "witchery:glintweed");
        add(keys, "item.witchery:seedsmindrake", "witchery:seedsmindrake");
        add(keys, "tile.witchery:somniancotton", "witchery:somniancotton");
        add(keys, "tile.witchery:spanishmoss", "witchery:spanishmoss");
        add(keys, "item.witchery:taglockkit", "witchery:taglockkit");
        add(keys, "item.witchery:witchhand", "witchery:witchhand");
        add(keys, "item.witchery:witchhat", "witchery:witchhat");
        add(keys, "tile.witchery:witchsapling", "witchery:witchsapling");
        for (BrewDye dye : BrewDye.values()) add(keys, "item.dyePowder", dye.item());
        add(keys, "item.witchery:ingredient", "witchery:ingredient", "mutton");
        for (WitcheryIngredient ingredient : WitcheryIngredient.values()) {
            add(keys, "item.witchery:ingredient", "witchery:" + ingredient.path());
        }
        return Map.copyOf(keys);
    }

    private static void add(Map<ResourceLocation, Integer> keys, String legacyName, String... ids) {
        int hash = 37 * 17 + legacyName.hashCode();
        for (String id : ids) keys.putIfAbsent(ResourceLocation.parse(id), hash);
    }
}
