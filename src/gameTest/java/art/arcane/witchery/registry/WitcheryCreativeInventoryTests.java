package art.arcane.witchery.registry;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.item.WitcheryIngredient;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import net.minecraftforge.registries.ForgeRegistries;

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
}
