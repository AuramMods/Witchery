package art.arcane.witchery.registry;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.entity.FetishBlockEntity;
import art.arcane.witchery.item.CircleTalismanItem;
import art.arcane.witchery.item.LeonardsUrnItem;
import art.arcane.witchery.item.SpectralStoneItem;
import art.arcane.witchery.item.VampireBookItem;
import art.arcane.witchery.item.WitcheryIngredient;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class WitcheryCreativeInventoryTests {
    @GameTest(template = "qa_imp_contracts")
    public static void creativeInventoryKeepsOriginalEggsAndBroomIcon(GameTestHelper helper) {
        var level = helper.getLevel();
        var tab = WitcheryCreativeTabs.WITCHERY_TAB.get();
        tab.buildContents(new CreativeModeTab.ItemDisplayParameters(level.enabledFeatures(), true, level.registryAccess()));
        helper.assertTrue(tab.getIconItem().is(WitcheryIngredient.ENCHANTED_BROOM.item()),
                "The Witchery tab uses the original Enchanted Broom icon");

        Set<String> eggs = tab.getDisplayItems().stream()
                .map(stack -> ForgeRegistries.ITEMS.getKey(stack.getItem()).getPath())
                .filter(path -> path.endsWith("_spawn_egg"))
                .collect(Collectors.toSet());
        helper.assertTrue(eggs.size() == 32 && eggs.containsAll(Set.of("demon_spawn_egg", "death_spawn_egg",
                "hellhound_spawn_egg", "leonard_spawn_egg", "lostsoul_spawn_egg", "reflection_spawn_egg")),
                "The tab shows the 32 original eggs, including the boss eggs");

        var deathEgg = (SpawnEggItem) WitcheryItems.LEGACY_SPAWN_EGGS.get("death_spawn_egg").get();
        var demonEgg = (SpawnEggItem) WitcheryItems.LEGACY_SPAWN_EGGS.get("demon_spawn_egg").get();
        helper.assertTrue(deathEgg.getColor(0) == 0 && deathEgg.getColor(1) == 0
                        && demonEgg.getColor(0) == 9502720 && demonEgg.getColor(1) == 11430927,
                "Original egg layers use their legacy colors");

        for (String path : Set.of("deathhorse_spawn_egg", "illusioncreeper_spawn_egg", "brew_spawn_egg", "bolt_spawn_egg")) {
            helper.assertTrue(!eggs.contains(path) && ForgeRegistries.ITEMS.containsKey(new ResourceLocation(Witchery.MODID, path)),
                    "Compatibility egg is hidden and still registered: " + path);
        }
        helper.succeed();
    }

    @GameTest(template = "qa_imp_contracts")
    public static void creativeInventoryOffersTheOriginalFunctionalVariants(GameTestHelper helper) {
        var level = helper.getLevel();
        var tab = WitcheryCreativeTabs.WITCHERY_TAB.get();
        tab.buildContents(new CreativeModeTab.ItemDisplayParameters(level.enabledFeatures(), true, level.registryAccess()));

        var talismans = variants(tab, "circletalisman");
        helper.assertTrue(talismans.stream().map(CircleTalismanItem::storedPattern).toList()
                        .equals(List.of(0, 1, 2, 3, 8, 16, 24, 64, 128, 192)),
                "Creative has an empty talisman and every original single-ring and chalk combination");
        helper.assertTrue(talismans.stream().map(CircleTalismanItem::model).toList()
                        .equals(List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)),
                "Each offered talisman uses its corresponding model");

        var stones = variants(tab, "spectralstone");
        helper.assertTrue(stones.stream().map(SpectralStoneItem::typeId).toList().equals(List.of(0, 1, 2, 3))
                        && stones.stream().map(SpectralStoneItem::creatureCount).toList().equals(List.of(0, 1, 1, 1)),
                "Creative has an empty Spectral Stone and one specimen of each inhabitant");
        var urns = variants(tab, "leonardsurn");
        helper.assertTrue(urns.stream().map(LeonardsUrnItem::tier).toList().equals(List.of(0, 1, 2, 3))
                        && urns.stream().map(stack -> LeonardsUrnItem.contents(stack).size()).toList().equals(List.of(1, 2, 3, 4)),
                "The four displayed Urn tiers have their original storage capacities");
        helper.assertTrue(variants(tab, "vampirebook").stream().map(VampireBookItem::tornPages).toList().equals(List.of(0, 9)),
                "Both the starting and complete vampire journals are available");

        helper.assertTrue(fetishEffects(tab, "scarecrow").equals(List.of(0, 1, 3, 2, 4, 5))
                        && fetishEffects(tab, "witchsladder").equals(List.of(0, 3))
                        && fetishEffects(tab, "trent").equals(List.of(0)),
                "Each fetish exposes the exact effects offered by the original creative inventory");
        for (String hidden : List.of("ingredient", "bound_waystone", "creature_bound_waystone")) {
            helper.assertTrue(variants(tab, hidden).isEmpty()
                            && ForgeRegistries.ITEMS.containsKey(new ResourceLocation(Witchery.MODID, hidden)),
                    "Save-compatible item stays out of creative: " + hidden);
        }
        helper.assertTrue(variants(tab, "waystone").size() == 1 && variants(tab, "globe_artichoke").size() == 1,
                "The ordinary Waystone and Globe Artichoke remain available");
        helper.succeed();
    }

    @GameTest(template = "qa_imp_contracts")
    public static void creativeInventoryHidesInternalBlocksAndKeepsPlaceableItems(GameTestHelper helper) {
        var level = helper.getLevel();
        var tab = WitcheryCreativeTabs.WITCHERY_TAB.get();
        tab.buildContents(new CreativeModeTab.ItemDisplayParameters(level.enabledFeatures(), true, level.registryAccess()));

        for (String path : List.of("barrier", "belladonna", "brew", "bucketbrew", "cactus", "circle",
                "circleglyphinfernal", "circleglyphotherwhere", "circleglyphritual", "coffinblock", "demonheart",
                "distilleryburning", "dreamcatcher", "force", "garlicplant", "light", "lilypad", "mirrorblock", "mirrorblock2",
                "placeditem", "slurp", "spiritflowing", "spiritportal", "tormentstone", "vine", "wallgen", "web",
                "witchesovenburning")) {
            helper.assertTrue(ForgeRegistries.ITEMS.containsKey(ResourceLocation.fromNamespaceAndPath(Witchery.MODID, path))
                            && variants(tab, path).isEmpty(),
                    "Keep the legacy internal ID registered and out of creative: " + path);
        }
        for (String path : List.of("brew_of_vines", "candelabra", "chalice", "chalkheart", "chalkinfernal", "coffin",
                "demon_heart", "distilleryidle", "dreamweave_of_nightmares", "garlic", "mirror", "seedsbelladonna",
                "seedswolfsbane", "thrown_web", "tormentportal", "witchesovenidle")) {
            helper.assertTrue(variants(tab, path).size() == 1, "Show one usable entry in creative: " + path);
        }
        helper.succeed();
    }

    private static List<ItemStack> variants(CreativeModeTab tab, String path) {
        var item = ForgeRegistries.ITEMS.getValue(ResourceLocation.fromNamespaceAndPath(Witchery.MODID, path));
        return tab.getDisplayItems().stream().filter(stack -> stack.is(item)).toList();
    }

    private static List<Integer> fetishEffects(CreativeModeTab tab, String path) {
        var item = WitcheryItems.LEGACY_BLOCK_ITEMS.get(path).get();
        return tab.getDisplayItems().stream().filter(stack -> stack.is(item)).map(FetishBlockEntity::itemEffect).toList();
    }
}
