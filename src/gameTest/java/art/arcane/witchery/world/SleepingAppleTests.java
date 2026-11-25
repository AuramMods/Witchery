package art.arcane.witchery.world;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.DreamCatcherBlock;
import art.arcane.witchery.block.DreamWeave;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryBlocks;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class SleepingAppleTests {
    @GameTest(template = "qa_coven_arena")
    public static void originalRecipeAndSubtypeProduceTheSameApple(GameTestHelper helper) throws Exception {
        var grid = new TransientCraftingContainer(new AbstractContainerMenu(MenuType.CRAFTING, 0) {
            @Override
            public ItemStack quickMoveStack(Player player, int slot) {
                return ItemStack.EMPTY;
            }

            @Override
            public boolean stillValid(Player player) {
                return true;
            }
        }, 3, 3);
        var mutandis = WitcheryIngredient.MUTANDIS.item();
        var misfortune = WitcheryIngredient.REEK_OF_MISFORTUNE.item();
        grid.setItem(1, new ItemStack(mutandis));
        grid.setItem(3, new ItemStack(misfortune));
        grid.setItem(4, new ItemStack(WitcheryIngredient.WORMY_APPLE.item()));
        grid.setItem(5, new ItemStack(misfortune));
        grid.setItem(6, new ItemStack(mutandis));
        grid.setItem(7, new ItemStack(WitcheryIngredient.BREW_OF_SLEEPING.item()));
        grid.setItem(8, new ItemStack(mutandis));
        var level = helper.getLevel();
        var recipes = level.getRecipeManager();
        var recipe = recipes.getRecipeFor(RecipeType.CRAFTING, grid, level).orElseThrow();
        var apple = WitcheryIngredient.SLEEPING_APPLE.item();
        var result = recipe.assemble(grid, level.registryAccess());
        helper.assertTrue(recipe.getId().equals(ResourceLocation.parse("witchery:sleeping_apple"))
                        && result.is(apple) && result.getCount() == 1,
                "The original seven-slot grid crafts one apple");
        grid.removeItemNoUpdate(1);
        helper.assertFalse(recipe.matches(grid, level), "The top Mutandis is required");

        var imported = WitcheryIngredient.loadInventoryStack(TagParser.parseTag(
                "{id:\"witchery:ingredient\",Damage:98s,Count:2b,tag:{Keepsake:1b}}"));
        helper.assertTrue(imported.is(apple) && imported.getCount() == 2 && imported.hasTag()
                        && imported.getTag().getBoolean("Keepsake"),
                "Legacy subtype 98 preserves the stack and its tag");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_sleeping_apple", timeoutTicks = 200)
    public static void eatingForcesAnOrdinaryNightmareAndParksRemainingApples(GameTestHelper helper) {
        var level = helper.getLevel();
        var dream = level.getServer().getLevel(WitcheryDimensions.DREAM);
        var anchor = helper.absolutePos(new BlockPos(8, 4, 8));
        level.setBlockAndUpdate(anchor.below(), Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(anchor, Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(anchor.above(), Blocks.AIR.defaultBlockState());

        // These allow a demonic nightmare with the ordinary brew and fixed random seed below.
        BlockPos catcher = anchor.east(2);
        level.setBlockAndUpdate(catcher.south(), Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(catcher, WitcheryBlocks.getRequired("dreamcatcher").get().defaultBlockState()
                .setValue(DreamCatcherBlock.WEAVE, DreamWeave.NIGHTMARES));
        level.setBlockAndUpdate(anchor.north(2), WitcheryBlocks.getRequired("spiritflowing").get().defaultBlockState());
        level.setBlockAndUpdate(anchor.west(2), WitcheryBlocks.getRequired("demonheart").get().defaultBlockState());

        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "SleepingAppleQA"));
        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        player.setPos(Vec3.atBottomCenterOf(anchor));
        player.getFoodData().setFoodLevel(20);
        var apple = WitcheryIngredient.SLEEPING_APPLE.item();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(apple, 2));
        player.getInventory().setItem(1, new ItemStack(WitcheryIngredient.ICY_NEEDLE.item()));
        player.getInventory().setItem(3, new ItemStack(Items.DIAMOND, 4));
        try {
            helper.assertTrue(apple.use(level, player, InteractionHand.MAIN_HAND).getResult().consumesAction(),
                    "The apple can start eating at full hunger");
            player.getFoodData().setFoodLevel(11);
            player.setItemInHand(InteractionHand.MAIN_HAND, apple.finishUsingItem(player.getMainHandItem(), level, player));
            player.stopUsingItem();
            helper.assertTrue(player.getMainHandItem().getCount() == 1 && player.getFoodData().getFoodLevel() == 14,
                    "Eating spends one apple and adds three hunger points before travel");

            player.getRandom().setSeed(0);
            helper.assertTrue(DreamTravel.processRequests(player) && player.serverLevel() == dream
                            && DreamTravel.isSpiritWalking(player) && DreamTravel.isNightmare(player),
                    "The scheduled use enters a nightmare");
            var session = player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getCompound("WITCSpiritWorld");
            helper.assertFalse(session.getBoolean("Demonic"), "The apple bypasses conditions for a demonic nightmare");
            helper.assertTrue(player.getInventory().countItem(apple) == 0 && player.getInventory().countItem(Items.DIAMOND) == 0
                            && player.getInventory().countItem(WitcheryIngredient.ICY_NEEDLE.item()) == 1,
                    "The normal Dream inventory separates the remaining apple and diamonds from the needle");

            player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(apple));
            player.setItemInHand(InteractionHand.OFF_HAND, apple.finishUsingItem(player.getOffhandItem(), dream, player));
            helper.assertFalse(DreamTravel.processRequests(player), "Eating an apple in the Dream cannot queue another transition");
        } finally {
            if (player.serverLevel() == dream) {
                DreamTravel.requestWake(player);
                DreamTravel.processRequests(player);
            }
            player.discard();
        }
        helper.assertTrue(player.serverLevel() == level && player.getInventory().countItem(apple) == 1
                        && player.getInventory().countItem(Items.DIAMOND) == 4 && player.getFoodData().getFoodLevel() == 14,
                "Waking restores the parked stack and the post-meal hunger");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void dreamArrivalChecksTheWholeColumnAndWakingPushesUp(GameTestHelper helper) {
        var level = helper.getLevel();
        var dream = level.getServer().getLevel(WitcheryDimensions.DREAM);
        var anchor = helper.absolutePos(new BlockPos(3, 2, 3));
        level.setBlockAndUpdate(anchor.below(), Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(anchor, Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(anchor.above(), Blocks.AIR.defaultBlockState());
        for (int offset = -14; offset <= 14; offset++) {
            dream.setBlockAndUpdate(anchor.above(offset), Blocks.AIR.defaultBlockState());
        }
        dream.setBlockAndUpdate(anchor.above(11), Blocks.STONE.defaultBlockState());
        dream.setBlockAndUpdate(anchor.below(13), Blocks.STONE.defaultBlockState());
        dream.setBlockAndUpdate(anchor.east().below(), Blocks.STONE.defaultBlockState());
        dream.setBlockAndUpdate(anchor.east(), Blocks.AIR.defaultBlockState());
        dream.setBlockAndUpdate(anchor.east().above(), Blocks.AIR.defaultBlockState());

        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "DreamArrivalQA"));
        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        player.setPos(Vec3.atBottomCenterOf(anchor));
        var brew = WitcheryIngredient.BREW_OF_SLEEPING.item();
        var needle = WitcheryIngredient.ICY_NEEDLE.item();
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(brew));
        player.getInventory().setItem(1, new ItemStack(needle));
        try {
            player.setItemInHand(InteractionHand.MAIN_HAND, brew.finishUsingItem(player.getMainHandItem(), level, player));
            helper.assertTrue(DreamTravel.processRequests(player) && player.serverLevel() == dream,
                    "The Brew of Sleeping must take the player to Dream");
            assertArrival(helper, player, anchor.above(12));

            player.getCooldowns().removeCooldown(needle);
            player.getInventory().selected = 1;
            if (!player.getMainHandItem().is(needle)) {
                player.getInventory().selected = 0;
            }
            helper.assertTrue(player.getMainHandItem().is(needle), "The Icy Needle must carry into Dream");
            needle.use(dream, player, InteractionHand.MAIN_HAND);
            helper.assertTrue(DreamTravel.processRequests(player) && player.serverLevel() == level,
                    "Waking must return the player to the sleeping body");
            assertArrival(helper, player, anchor);
        } finally {
            if (player.serverLevel() == dream) {
                DreamTravel.requestWake(player);
                DreamTravel.processRequests(player);
            }
            player.discard();
        }
        helper.succeed();
    }

    private static void assertArrival(GameTestHelper helper, FakePlayer player, BlockPos position) {
        helper.assertTrue(player.position().distanceToSqr(Vec3.atBottomCenterOf(position).add(0, 0.1, 0)) < 1e-10,
                "Arrival must be centered above the closest standing spot in the original column, preferring the upper tie");
        helper.assertTrue(player.getDeltaMovement().equals(new Vec3(0, 0.1, 0)) && player.fallDistance == 0,
                "Arrival must apply the original upward push and clear falling distance");
    }
}
