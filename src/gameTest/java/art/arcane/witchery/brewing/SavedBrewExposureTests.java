package art.arcane.witchery.brewing;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.entity.BrewFluidBlockEntity;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryEffects;
import art.arcane.witchery.registry.WitcheryItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class SavedBrewExposureTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_saved_brew_exposure")
    public static void drinksAndDescribesSavedEffectsInOrder(GameTestHelper helper) {
        var original = recipe();
        var bottle = WitcheryItems.getRequired("brewbottle").get().getDefaultInstance();
        bottle.setTag(original.copy());
        var description = BrewMixture.describe(bottle.getTag());
        helper.assertTrue(description != null && description.effects().size() == 2
                        && description.effects().get(0).strength() == 1 && description.effects().get(0).duration() == 3600
                        && description.effects().get(1).duration() == 1800,
                "The label must retain both ordered effects and their modifiers");
        var drinker = EntityType.PIG.create(helper.getLevel());
        var remainder = bottle.getItem().finishUsingItem(bottle, helper.getLevel(), drinker);
        helper.assertTrue(remainder.is(Items.GLASS_BOTTLE) && original.equals(bottle.getTag()),
                "A saved brew must drink normally and keep the original entries intact");
        assertEffects(helper, drinker, 3600, 1800);

        var unavailable = BrewMixture.ingredientTag(List.of(Items.DIAMOND_PICKAXE.getDefaultInstance()), 0x624391);
        helper.assertTrue(BrewMixture.from(unavailable) == null && BrewMixture.describe(unavailable) == null,
                "A recipe with only inactive ingredients must stay dormant");
        var water = BrewMixture.ingredientTag(List.of(), 0x624391);
        helper.assertTrue(BrewMixture.from(water) != null && BrewMixture.describe(water) != null,
                "An empty colored brew must remain usable");
        original.putBoolean("Splash", true);
        helper.assertTrue(BrewMixture.from(original) == null && BrewMixture.describe(original) == null,
                "A declared splash brew without a usable dispersal cannot become a drink");
        helper.assertTrue(!BrewMixture.canAdd(List.of(), Items.DIAMOND_PICKAXE.getDefaultInstance()),
                "Fresh mixing must still reject an inactive ingredient");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_saved_brew_exposure")
    public static void reloadsSavedGasAndLiquidWithTheirOriginalContactRules(GameTestHelper helper) {
        var level = helper.getLevel();
        for (boolean gas : List.of(true, false)) {
            var pos = new BlockPos(gas ? 2 : 5, 4, 2);
            helper.setBlock(pos, WitcheryBlocks.getRequired(gas ? "brewgas" : "brewliquid").get());
            var fluid = (BrewFluidBlockEntity) helper.getBlockEntity(pos);
            var recipe = recipe();
            var dispersal = gas ? WitcheryIngredient.BAT_WOOL : WitcheryIngredient.WORMWOOD;
            recipe.getList("Items", Tag.TAG_COMPOUND).add(dispersal.item().getDefaultInstance().save(new CompoundTag()));
            recipe.putBoolean("Splash", true);
            var stored = fluid.saveWithoutMetadata();
            stored.put("Effect", recipe.copy());
            stored.putInt("Duration", 50);
            fluid.load(stored);
            var reopened = fluid.saveWithoutMetadata();
            helper.assertTrue(reopened.equals(stored), "Saving the fluid must retain the full ordered recipe and state");
            fluid.load(reopened);

            var exposed = EntityType.PIG.create(level);
            var protectedPig = EntityType.PIG.create(level);
            protectedPig.addEffect(new MobEffectInstance(WitcheryEffects.GAS_MASK.get(), 200));
            fluid.expose(level, exposed, gas);
            fluid.expose(level, protectedPig, gas);
            assertEffects(helper, exposed, 1800, 900);
            assertEffects(helper, protectedPig, 1800, gas ? 0 : 900);
        }
        helper.succeed();
    }

    private static CompoundTag recipe() {
        var items = List.of(Items.NETHER_WART, WitcheryIngredient.TEAR_OF_THE_GODDESS.item(),
                Items.GLOWSTONE_DUST, Items.DIAMOND_PICKAXE, Items.SUGAR, Items.REDSTONE,
                Items.FERMENTED_SPIDER_EYE, Items.GHAST_TEAR);
        var recipe = BrewMixture.ingredientTag(items.stream().map(Item::getDefaultInstance).toList(), 0x624391);
        var pending = new CompoundTag();
        pending.putString("id", "witchery_missing_fixture:reagent");
        pending.putByte("Count", (byte) 1);
        recipe.getList("Items", Tag.TAG_COMPOUND).add(4, pending);
        return recipe;
    }

    private static void assertEffects(GameTestHelper helper, LivingEntity target, int speedTicks, int poisonTicks) {
        var speed = target.getEffect(MobEffects.MOVEMENT_SPEED);
        var poison = target.getEffect(MobEffects.POISON);
        boolean expectedPoison = poisonTicks == 0 ? poison == null
                : poison != null && poison.getAmplifier() == 0 && poison.getDuration() == poisonTicks;
        helper.assertTrue(speed != null && speed.getAmplifier() == 1 && speed.getDuration() == speedTicks
                        && expectedPoison,
                "Expected Speed II for " + speedTicks + " ticks and Poison I for " + poisonTicks + " ticks");
    }
}
