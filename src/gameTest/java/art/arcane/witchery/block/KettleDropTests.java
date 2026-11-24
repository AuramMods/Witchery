package art.arcane.witchery.block;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.entity.KettleBlockEntity;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryItems;
import art.arcane.witchery.registry.WitcheryRecipes;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class KettleDropTests {
    @GameTest(template = "qa_coven_arena")
    public static void matchesPotionTypeWithCustomNames(GameTestHelper helper) {
        var recipes = helper.getLevel().getRecipeManager().getAllRecipesFor(WitcheryRecipes.KETTLE.get());
        int potionIngredients = 0;
        for (var recipe : recipes) {
            if (!recipe.id().getNamespace().equals(Witchery.MODID)) continue;
            for (var ingredient : recipe.ingredients()) {
                for (ItemStack option : ingredient.getItems()) {
                    boolean splash = option.is(Items.SPLASH_POTION);
                    if (!splash && !option.is(Items.POTION)) continue;
                    potionIngredients++;
                    var named = option.copy();
                    named.setHoverName(Component.literal("Apothecary's Potion"));
                    named.getOrCreateTag().putString("Fixture", "legacy-label");
                    helper.assertTrue(ingredient.test(named), recipe.id() + " should accept the correct potion with extra item data");
                    helper.assertTrue(!ingredient.test(PotionUtils.setPotion(named.copy(), Potions.WATER)),
                            recipe.id() + " should still require the original effect and duration");
                    var otherBottle = PotionUtils.setPotion(new ItemStack(splash ? Items.POTION : Items.SPLASH_POTION),
                            PotionUtils.getPotion(option));
                    helper.assertTrue(!ingredient.test(otherBottle), recipe.id() + " should require the correct delivery form");
                }
            }
        }
        helper.assertTrue(potionIngredients == 11, "Exercise all eleven built-in Kettle potion ingredients");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_kettle_order", setupTicks = 3)
    public static void usesOriginalRecipeOrderForPartialMixtures(GameTestHelper helper) {
        var vines = heatedKettle(helper, new BlockPos(3, 4, 4));
        var raising = heatedKettle(helper, new BlockPos(9, 4, 4));
        vines.addDroppedStack(new ItemStack(WitcheryIngredient.DOG_TONGUE.item()));
        raising.addDroppedStack(new ItemStack(WitcheryIngredient.MUTANDIS.item()));

        helper.startSequence().thenIdle(21).thenExecute(() -> {
            helper.assertTrue(vines.isPowered() && vines.saveWithoutMetadata().getInt("LiquidColor") == -16753913,
                    "Dog Tongue alone should show the powered, green Vines mixture without an altar");
            helper.assertTrue(!raising.isPowered() && raising.saveWithoutMetadata().getInt("LiquidColor") == -12120505,
                    "Mutandis alone should show the Raising mixture waiting for altar power");
            helper.assertTrue(!vines.isReady() && !raising.isReady(), "The partial mixtures should retain their ingredients");
            for (var ingredient : List.of(Items.VINE, Items.RED_MUSHROOM, Items.BROWN_MUSHROOM, Items.WHEAT,
                    WitcheryIngredient.REEK_OF_MISFORTUNE.item())) {
                vines.addDroppedStack(new ItemStack(ingredient));
            }
        }).thenIdle(21).thenExecute(() -> {
            helper.assertTrue(vines.isReady() && vines.bottleBrew().is(WitcheryIngredient.BREW_OF_VINES.item()),
                    "A complete Vines recipe should still brew and bottle without altar power");
        }).thenSucceed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void doublesInfusedBaseWithTheOriginalBottlingBonus(GameTestHelper helper) throws Exception {
        var kettle = heatedKettle(helper, new BlockPos(3, 4, 4));
        kettle.load(TagParser.parseTag("""
                {Items:[{Slot:6b,id:"witchery:infused_brew_base",Count:2b}],Powered:1b,
                 Tank:{FluidName:"minecraft:water",Amount:1000},FinishedRecipe:"witchery:kettle/infused_brew_base_from_bramble"}
                """));
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "KettleBottler"));
        player.setItemSlot(EquipmentSlot.HEAD, new ItemStack(WitcheryItems.getRequired("witchhat").get()));
        long seed = 0;
        while (RandomSource.create(seed).nextDouble() >= 0.35) seed++;
        player.getRandom().setSeed(seed);
        var result = kettle.bottleBrew(player);
        helper.assertTrue(result.is(WitcheryIngredient.INFUSED_BREW_BASE.item()) && result.getCount() == 2,
                "A successful Witch's Hat roll should add one serving to the duplicate-base recipe");
        helper.assertTrue(kettle.isReady(), "The bonus serving should leave the second base in the kettle");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void kettleCollectsIngredientsAtItsRim(GameTestHelper helper) {
        var level = helper.getLevel();
        var relative = new BlockPos(5, 3, 5);
        helper.setBlock(relative, WitcheryBlocks.getRequired("kettle").get());
        helper.setBlock(relative.below(), Blocks.STONE);
        helper.setBlock(relative.east().below(), Blocks.STONE);
        BlockPos pos = helper.absolutePos(relative);
        var kettle = level.getBlockEntity(pos);
        var fluid = kettle.getCapability(ForgeCapabilities.FLUID_HANDLER).orElseThrow(IllegalStateException::new);
        var inventory = kettle.getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(IllegalStateException::new);
        helper.assertTrue(fluid.fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE) == 1000,
                "The fixture must fill the kettle");

        var atRim = new ItemEntity(level, pos.getX() + 0.81, pos.getY() + 1.35, pos.getZ() + 0.5,
                new ItemStack(Items.POPPY));
        var beside = new ItemEntity(level, pos.getX() + 1.5, pos.getY() + 1.35, pos.getZ() + 0.5,
                new ItemStack(Items.GOLDEN_CARROT));
        atRim.setDeltaMovement(0, -0.1, 0);
        beside.setDeltaMovement(0, -0.1, 0);
        level.addFreshEntity(atRim);
        level.addFreshEntity(beside);

        helper.runAfterDelay(25, () -> {
            helper.assertTrue(!atRim.isAlive() && inventory.getStackInSlot(0).is(Items.POPPY),
                    "An ingredient that lands on the inside edge of the kettle must enter its first slot");
            helper.assertTrue(beside.isAlive() && inventory.getStackInSlot(1).isEmpty(),
                    "An ingredient outside the kettle block must stay on the ground");
            helper.succeed();
        });
    }

    private static KettleBlockEntity heatedKettle(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos.below(2), Blocks.NETHERRACK);
        helper.setBlock(pos.below(), Blocks.FIRE);
        helper.setBlock(pos, WitcheryBlocks.getRequired("kettle").get());
        var kettle = (KettleBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(pos));
        var fluid = kettle.getCapability(ForgeCapabilities.FLUID_HANDLER).orElseThrow(IllegalStateException::new);
        helper.assertTrue(fluid.fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE) == 1000,
                "The fixture must fill the kettle");
        return kettle;
    }
}
