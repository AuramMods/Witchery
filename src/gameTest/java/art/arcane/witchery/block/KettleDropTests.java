package art.arcane.witchery.block;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.entity.AltarBlockEntity;
import art.arcane.witchery.block.entity.KettleBlockEntity;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryItems;
import art.arcane.witchery.registry.WitcheryRecipes;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.HopperBlockEntity;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
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
    @GameTest(template = "qa_rite_terrain", batch = "witchery_kettle_timing")
    public static void kettleUsesItsOwnTicksAndWaitsForTheClosestAltar(GameTestHelper helper) {
        var kettle = heatedKettle(helper, new BlockPos(5, 4, 5));
        for (var ingredient : List.of(WitcheryIngredient.ICY_NEEDLE.item(), Items.SNOWBALL,
                WitcheryIngredient.GLOBE_ARTICHOKE.item(), Items.GLISTERING_MELON_SLICE, Items.RED_MUSHROOM,
                WitcheryIngredient.ODOUR_OF_PURITY.item())) {
            kettle.addDroppedStack(new ItemStack(ingredient));
        }
        var far = placeAltar(helper, kettle.getBlockPos().south(12));
        var near = placeAltar(helper, kettle.getBlockPos().east(4));
        charge(far, 2_400);

        tickKettle(helper, kettle, 19);
        helper.assertTrue(kettle.saveWithoutMetadata().getInt("LiquidColor") == 0 && !kettle.isReady(),
                "The kettle leaves its new mixture unevaluated for its first 19 ticks");
        tickKettle(helper, kettle, 1);
        helper.assertTrue(kettle.saveWithoutMetadata().getInt("LiquidColor") == -13565953
                        && !kettle.isReady() && far.power() == 2_400,
                "Its twentieth tick colors the brew but cannot draw from the closer empty altar");

        charge(near, 1_200);
        tickKettle(helper, kettle, 19);
        helper.assertTrue(!kettle.isReady() && near.power() == 1_200,
                "New power waits for the kettle's next brew check");
        tickKettle(helper, kettle, 1);
        var inventory = kettle.getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(IllegalStateException::new);
        helper.assertTrue(kettle.isReady() && kettle.isPowered() && far.power() == 2_400 && near.power() == 200
                        && inventory.getStackInSlot(6).is(WitcheryIngredient.BREW_OF_ICE.item())
                        && inventory.getStackInSlot(6).getCount() == 3,
                "Tick 40 spends exactly 1,000 power from the closer altar and produces three servings");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_kettle_fluid_transfer")
    public static void automationPreservesTheBrewWhenWaterMoves(GameTestHelper helper) throws Exception {
        var kettle = heatedKettle(helper, new BlockPos(5, 4, 5));
        kettle.load(TagParser.parseTag("""
                {Items:[{Slot:6b,id:"witchery:brew_of_ice",Count:3b},{Slot:7b,id:"minecraft:glass_bottle",Count:3b}],
                 Powered:1b,LiquidColor:-13565953,Tank:{FluidName:"minecraft:water",Amount:1000},
                 FinishedRecipe:"witchery:kettle/brew_of_ice"}
                """));
        var tank = kettle.getCapability(ForgeCapabilities.FLUID_HANDLER).orElseThrow(IllegalStateException::new);
        var inventory = kettle.getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(IllegalStateException::new);
        helper.assertTrue(tank.drain(400, IFluidHandler.FluidAction.EXECUTE).getAmount() == 400,
                "The fluid handler can drain 400 mB of water from a ready kettle");
        kettle.load(kettle.saveWithFullMetadata());
        helper.assertTrue(kettle.isReady() && tank.getFluidInTank(0).getAmount() == 600
                        && inventory.getStackInSlot(6).getCount() == 3 && inventory.extractItem(6, 1, false).isEmpty(),
                "The partly drained kettle keeps its three servings and blocks automated extraction across a save");

        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "KettleFluidBottler"));
        var hand = InteractionHand.MAIN_HAND;
        var pos = kettle.getBlockPos();
        player.setItemInHand(hand, new ItemStack(Items.GLASS_BOTTLE));
        kettle.getBlockState().use(helper.getLevel(), player, hand,
                new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
        helper.assertTrue(player.getItemInHand(hand).is(WitcheryIngredient.BREW_OF_ICE.item())
                        && inventory.getStackInSlot(6).getCount() == 2 && kettle.bottleCount() == 3
                        && tank.getFluidInTank(0).getAmount() == 600,
                "Hand bottling takes one serving and leaves the water and stored bottles alone");

        tank.fill(new FluidStack(Fluids.WATER, 400), IFluidHandler.FluidAction.EXECUTE);
        var brewed = inventory.extractItem(6, 2, false);
        helper.assertTrue(brewed.is(WitcheryIngredient.BREW_OF_ICE.item()) && brewed.getCount() == 2
                        && kettle.bottleCount() == 1 && !kettle.isReady() && tank.getFluidInTank(0).isEmpty(),
                "Refilling unblocks automation and the last serving empties the kettle");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void bottlingPlainWaterClearsASpoiledKettle(GameTestHelper helper) throws Exception {
        var kettle = heatedKettle(helper, new BlockPos(5, 4, 5));
        kettle.load(TagParser.parseTag("""
                {Items:[{Slot:6b,id:"witchery:brew_of_vines",Count:2b},{Slot:7b,id:"minecraft:glass_bottle",Count:3b}],
                 Ruined:1b,Tank:{FluidName:"minecraft:water",Amount:1000}}
                """));
        var tank = kettle.getCapability(ForgeCapabilities.FLUID_HANDLER).orElseThrow(IllegalStateException::new);
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "BottleKettleWater"));
        var hand = InteractionHand.MAIN_HAND;
        var pos = kettle.getBlockPos();
        var hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
        player.setItemInHand(hand, new ItemStack(Items.GLASS_BOTTLE));
        helper.getLevel().getBlockState(pos).use(helper.getLevel(), player, hand, hit);
        helper.assertTrue(PotionUtils.getPotion(player.getItemInHand(hand)) == Potions.WATER && tank.getFluidInTank(0).isEmpty()
                        && !kettle.isRuined() && !kettle.isReady() && kettle.bottleCount() == 3,
                "Bottling plain water drains the spoiled Kettle and preserves the stored glass bottles");

        tank.fill(new FluidStack(Fluids.WATER, 500), IFluidHandler.FluidAction.EXECUTE);
        helper.getLevel().getBlockState(pos).use(helper.getLevel(), player, hand, hit);
        helper.assertTrue(player.getItemInHand(hand).isEmpty() && tank.getFluidInTank(0).getAmount() == 1000
                        && kettle.bottleCount() == 3,
                "A Water Bottle fills the last 500 mB and is consumed");

        kettle.addDroppedStack(new ItemStack(Items.RED_MUSHROOM, 2));
        player.setItemInHand(hand, PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER));
        helper.getLevel().getBlockState(pos).use(helper.getLevel(), player, hand, hit);
        helper.assertTrue(PotionUtils.getPotion(player.getItemInHand(hand)) == Potions.WATER && !kettle.hasIngredients()
                        && tank.getFluidInTank(0).getAmount() == 1000 && kettle.bottleCount() == 3,
                "A Water Bottle rinses a full Kettle and stays filled when no water is transferred");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void acceptsDroppedBottlesOnlyWhenTheWholeStackFits(GameTestHelper helper) {
        var kettle = heatedKettle(helper, new BlockPos(5, 4, 5));
        var inventory = kettle.getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(IllegalStateException::new);
        var stored = new ItemStack(Items.GLASS_BOTTLE, 63).setHoverName(Component.literal("Stored Bottles"));
        var offered = new ItemStack(Items.GLASS_BOTTLE, 2).setHoverName(Component.literal("Offered Bottles"));
        helper.assertTrue(inventory.insertItem(7, stored, false).isEmpty(), "Prepare a nearly full bottle buffer");
        helper.assertTrue(ItemStack.matches(kettle.addDroppedStack(offered), offered)
                        && ItemStack.matches(inventory.getStackInSlot(7), stored),
                "The Kettle leaves the whole two-bottle drop on the ground when there is room for one");

        var saved = kettle.saveWithFullMetadata();
        saved.getList("Items", Tag.TAG_COMPOUND).getCompound(0).putByte("Count", (byte) 62);
        kettle.load(saved);
        helper.assertTrue(kettle.addDroppedStack(offered).isEmpty()
                        && ItemStack.matches(inventory.getStackInSlot(7), stored.copyWithCount(64)),
                "The whole drop fills the buffer and keeps the bottles' original stored name");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void acceptsDroppedIngredientsAroundAFinishedBrew(GameTestHelper helper) throws Exception {
        var kettle = heatedKettle(helper, new BlockPos(5, 4, 5));
        kettle.load(TagParser.parseTag("""
                {Items:[{Slot:6b,id:"witchery:brew_of_vines",Count:3b}],Powered:1b,LiquidColor:-16753913,
                 Tank:{FluidName:"minecraft:water",Amount:1000},FinishedRecipe:"witchery:kettle/brew_of_vines"}
                """));
        var inventory = kettle.getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(IllegalStateException::new);
        var vines = new ItemStack(Items.VINE, 2);
        helper.assertTrue(ItemStack.matches(inventory.insertItem(0, vines, true), vines) && kettle.isReady(),
                "The item handler refuses new ingredients while the result slot contains a brew");
        for (int slot = 0; slot < 6; slot++) {
            helper.assertTrue(kettle.addDroppedStack(vines).isEmpty() && ItemStack.matches(inventory.getStackInSlot(slot), vines),
                    "The full Kettle accepts each dropped stack into the next free slot");
        }
        helper.assertTrue(kettle.isReady() && kettle.addDroppedStack(vines).isEmpty() && kettle.isRuined(),
                "The seventh dropped stack spoils the batch");
        kettle.load(kettle.saveWithFullMetadata());
        helper.assertTrue(!kettle.isReady() && kettle.bottleBrew().isEmpty()
                        && inventory.getStackInSlot(6).is(WitcheryIngredient.BREW_OF_VINES.item())
                        && inventory.getStackInSlot(6).getCount() == 3,
                "The spoiled result stays in the saved Kettle and can no longer be bottled");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void hoppersMergeIngredientsIntoTheSameSlotEvenWhenSpoiled(GameTestHelper helper) {
        var pos = new BlockPos(5, 4, 5);
        var kettle = heatedKettle(helper, pos);
        var inventory = kettle.getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(IllegalStateException::new);
        kettle.addDroppedStack(new ItemStack(Items.VINE));
        helper.setBlock(pos.above(), Blocks.HOPPER);
        var hopper = (HopperBlockEntity) helper.getLevel().getBlockEntity(helper.absolutePos(pos.above()));
        hopper.setItem(0, new ItemStack(Items.VINE));

        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(inventory.getStackInSlot(0).getCount() == 2
                        && inventory.getStackInSlot(1).isEmpty(), "The hopper merges a vine into the first ingredient stack"))
                .thenExecute(() -> {
                    var saved = kettle.saveWithFullMetadata();
                    saved.putBoolean("Ruined", true);
                    kettle.load(saved);
                    hopper.setItem(0, new ItemStack(Items.VINE));
                }).thenWaitUntil(() -> helper.assertTrue(kettle.isRuined() && inventory.getStackInSlot(0).getCount() == 3
                        && inventory.getStackInSlot(1).isEmpty(), "A spoiled Kettle without a result still accepts the hopper ingredient"))
                .thenSucceed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void topsUpAndRinsesAKettleWithWaterBuckets(GameTestHelper helper) {
        var kettle = heatedKettle(helper, new BlockPos(5, 4, 5));
        var tank = kettle.getCapability(ForgeCapabilities.FLUID_HANDLER).orElseThrow(IllegalStateException::new);
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "RinseKettle"));
        var hand = InteractionHand.MAIN_HAND;
        var pos = kettle.getBlockPos();
        var hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);

        helper.assertTrue(tank.drain(500, IFluidHandler.FluidAction.EXECUTE).getAmount() == 500, "Prepare a half-filled kettle");
        kettle.addDroppedStack(new ItemStack(Items.GLASS_BOTTLE, 3));
        player.setItemInHand(hand, new ItemStack(Items.WATER_BUCKET));
        helper.getLevel().getBlockState(pos).use(helper.getLevel(), player, hand, hit);
        helper.assertTrue(tank.getFluidInTank(0).getAmount() == 1000 && player.getItemInHand(hand).is(Items.BUCKET)
                        && kettle.bottleCount() == 3,
                "A Water Bucket tops up the half-filled kettle and preserves its spare bottles");

        kettle.addDroppedStack(new ItemStack(Items.RED_MUSHROOM));
        var spoiled = kettle.saveWithoutMetadata();
        spoiled.putBoolean("Ruined", true);
        kettle.load(spoiled);
        helper.assertTrue(kettle.isRuined() && kettle.hasIngredients(), "Prepare a full ruined batch");
        player.setItemInHand(hand, new ItemStack(Items.WATER_BUCKET));
        helper.getLevel().getBlockState(pos).use(helper.getLevel(), player, hand, hit);
        helper.assertTrue(tank.getFluidInTank(0).getAmount() == 1000 && player.getItemInHand(hand).is(Items.WATER_BUCKET)
                        && !kettle.isRuined() && !kettle.hasIngredients() && kettle.bottleCount() == 3,
                "Rinsing a full kettle clears the spoiled batch and keeps its water and three spare bottles");
        helper.succeed();
    }

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

    private static void tickKettle(GameTestHelper helper, KettleBlockEntity kettle, int ticks) {
        // Keep world time fixed to distinguish a loaded tile's clock from the server's clock.
        for (int tick = 0; tick < ticks; tick++) {
            KettleBlockEntity.serverTick(helper.getLevel(), kettle.getBlockPos(), kettle.getBlockState(), kettle);
        }
    }

    private static AltarBlockEntity placeAltar(GameTestHelper helper, BlockPos core) {
        var level = helper.getLevel();
        var state = WitcheryBlocks.getRequired("altar").get().defaultBlockState();
        for (var member : BlockPos.betweenClosed(core, core.offset(2, 0, 1))) {
            level.setBlockAndUpdate(member, state);
        }
        AltarBlockEntity.form(level, core);
        return (AltarBlockEntity) level.getBlockEntity(core);
    }

    private static void charge(AltarBlockEntity altar, float amount) {
        var saved = altar.saveWithoutMetadata();
        saved.putFloat("Power", amount);
        altar.load(saved);
    }
}
