package art.arcane.witchery.brewing;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryItems;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class BrewBottleDisplayTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_brew_display")
    public static void describesOrderedEffectsAndDispersalFromLegacyItems(GameTestHelper helper) throws Exception {
        var tag = TagParser.parseTag("""
                {Color:55553,Items:[{id:"minecraft:nether_wart",Count:1b},
                    {id:"witchery:ingredient",Damage:37s,Count:1b},
                    {id:"minecraft:snowball",Count:1b},{id:"minecraft:glowstone_dust",Count:1b},
                    {id:"minecraft:redstone",Count:1b},{id:"minecraft:sugar",Count:1b},
                    {id:"witchery:ingredient",Damage:18s,Count:1b},{id:"minecraft:gunpowder",Count:1b},
                    {id:"minecraft:end_stone",Count:1b}],BrewName:"Old cached name"}
                """);
        var description = BrewMixture.describe(tag);
        helper.assertTrue(description != null && description.delivery() == BrewMixture.Delivery.SPLASH
                        && description.extent() == 1 && description.lifetime() == 0,
                "The original dispersal label includes modifiers offered before it");
        var effects = description.effects();
        helper.assertTrue(effects.size() == 2 && translationKey(effects.get(0).name()).equals("brew.witchery.snow")
                        && translationKey(effects.get(1).name()).equals("effect.minecraft.speed"),
                "Keep an action that precedes the first potion effect in the bottle display");
        helper.assertTrue(effects.get(0).duration() == 3600 && effects.get(1).duration() == 7200 && effects.get(1).strength() == 1,
                "Apply the original label's duration and strength to the matching ingredient");

        var stack = new ItemStack(WitcheryItems.getRequired("brewbottle").get());
        stack.setTag(tag);
        var title = (TranslatableContents) stack.getHoverName().getContents();
        helper.assertTrue(title.getKey().equals("item.witchery.brewbottle.splash")
                        && title.getArgs()[0] instanceof Component names && names.getString().contains(" & "),
                "The generated name uses the translated splash prefix and original conjunction");
        var tooltip = new ArrayList<Component>();
        stack.getItem().appendHoverText(stack, helper.getLevel(), tooltip, TooltipFlag.Default.NORMAL);
        helper.assertTrue(tooltip.size() == 3 && translationKey(tooltip.get(0)).equals("item.witchery.brewbottle.dispersal.splash")
                        && tooltip.get(1).getString().endsWith(" [1:30]") && tooltip.get(2).getString().endsWith(" [3:00]"),
                "The splash tooltip lists both effects with the original halved times");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_brew_display")
    public static void keepsSavedQuaffingSpeedAndOriginalInvertedDurationLabel(GameTestHelper helper) throws Exception {
        var tag = TagParser.parseTag("""
                {Color:40,BrewDrinkSpeed:12,Items:[{id:"minecraft:nether_wart",Count:1b},
                  {id:"minecraft:fermented_spider_eye",Count:1b},{id:"minecraft:sugar",Count:1b}]}
                """);
        var stack = new ItemStack(WitcheryItems.getRequired("brewbottle").get());
        stack.setTag(tag);
        var tooltip = new ArrayList<Component>();
        stack.getItem().appendHoverText(stack, helper.getLevel(), tooltip, TooltipFlag.Default.NORMAL);
        helper.assertTrue(tooltip.size() == 2 && translationKey(tooltip.get(0)).equals("effect.minecraft.slowness")
                        && tooltip.get(0).getString().endsWith(" [3:00]")
                        && translationKey(tooltip.get(1)).equals("item.witchery.brewbottle.quaffing"),
                "The original inverted display retains the base duration and shows the quaffing tier");
        var mixture = BrewMixture.from(tag);
        helper.assertTrue(mixture != null && mixture.effects().get(0).ticks() == 1800,
                "The actual inverted effect uses the original shorter duration");
        helper.assertTrue(stack.getUseDuration() == 12, "An existing bottle retains its positive cached drink time");
        tag.remove("BrewDrinkSpeed");
        helper.assertTrue(stack.getUseDuration() == 32, "Earlier bottles without a cached time derive it from the recipe");
        helper.succeed();
    }

    private static String translationKey(Component component) {
        return ((TranslatableContents) component.getContents()).getKey();
    }
}
