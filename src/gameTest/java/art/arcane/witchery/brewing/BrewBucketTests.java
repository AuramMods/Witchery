package art.arcane.witchery.brewing;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.WitchesCauldronBlock;
import art.arcane.witchery.block.entity.WitchesCauldronBlockEntity;
import art.arcane.witchery.item.BrewBucketItem;
import art.arcane.witchery.item.TaglockItem;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryFluids;
import art.arcane.witchery.registry.WitcheryItems;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.templates.FluidTank;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class BrewBucketTests {
    @GameTest(template = "qa_coven_arena")
    public static void movesOneBucketOfPlainWaterWithBottles(GameTestHelper helper) {
        var bowl = hotCauldron(helper, new BlockPos(4, 4, 4));
        var saved = bowl.saveWithFullMetadata();
        saved.putInt("HeatTicks", 0);
        bowl.load(saved);
        var tank = bowl.getCapability(ForgeCapabilities.FLUID_HANDLER).orElseThrow(IllegalStateException::new);
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "BottleWater"));
        var hand = InteractionHand.OFF_HAND;
        var pos = bowl.getBlockPos();
        var hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);

        tank.drain(500, IFluidHandler.FluidAction.EXECUTE);
        player.getInventory().items.replaceAll(stack -> new ItemStack(Items.DIRT, 64));
        player.setItemInHand(hand, new ItemStack(Items.GLASS_BOTTLE, 2));
        helper.getLevel().getBlockState(pos).use(helper.getLevel(), player, hand, hit);
        helper.assertTrue(tank.getFluidInTank(0).getAmount() == 2500 && player.getItemInHand(hand).getCount() == 2,
                "A full inventory leaves a stacked glass bottle and its water in place");

        player.getInventory().items.set(0, ItemStack.EMPTY);
        helper.getLevel().getBlockState(pos).use(helper.getLevel(), player, hand, hit);
        var waterBottle = player.getInventory().items.get(0);
        helper.assertTrue(tank.getFluidInTank(0).getAmount() == 1500 && player.getItemInHand(hand).is(Items.GLASS_BOTTLE)
                        && player.getItemInHand(hand).getCount() == 1 && PotionUtils.getPotion(waterBottle) == Potions.WATER,
                "A glass bottle withdraws exactly 1,000 mB from a cold cauldron");

        player.setItemInHand(hand, waterBottle);
        player.getInventory().items.set(0, ItemStack.EMPTY);
        helper.getLevel().getBlockState(pos).use(helper.getLevel(), player, hand, hit);
        helper.assertTrue(player.getItemInHand(hand).isEmpty() && tank.getFluidInTank(0).getAmount() == 2500,
                "Pouring the water back consumes its bottle");

        tank.drain(2000, IFluidHandler.FluidAction.EXECUTE);
        player.setItemInHand(hand, new ItemStack(Items.GLASS_BOTTLE));
        helper.getLevel().getBlockState(pos).use(helper.getLevel(), player, hand, hit);
        helper.assertTrue(player.getItemInHand(hand).is(Items.GLASS_BOTTLE) && tank.getFluidInTank(0).getAmount() == 500,
                "Bottling leaves a partial water portion intact");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void topsUpPlainWaterWithoutDilutingBrew(GameTestHelper helper) {
        var bowl = hotCauldron(helper, new BlockPos(4, 4, 4));
        var tank = bowl.getCapability(ForgeCapabilities.FLUID_HANDLER).orElseThrow(IllegalStateException::new);
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "TopUpWater"));
        var hand = InteractionHand.OFF_HAND;
        var pos = bowl.getBlockPos();
        var hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);

        helper.assertTrue(tank.drain(500, IFluidHandler.FluidAction.EXECUTE).getAmount() == 500, "Prepare a partial water tank");
        player.setItemInHand(hand, new ItemStack(Items.WATER_BUCKET));
        helper.getLevel().getBlockState(pos).use(helper.getLevel(), player, hand, hit);
        helper.assertTrue(tank.getFluidInTank(0).getAmount() == 3000 && player.getItemInHand(hand).is(Items.BUCKET),
                "A survival Water Bucket fills the last 500 mB and returns empty to the offhand");

        tank.drain(1, IFluidHandler.FluidAction.EXECUTE);
        player.getAbilities().instabuild = true;
        player.setItemInHand(hand, new ItemStack(Items.WATER_BUCKET));
        helper.getLevel().getBlockState(pos).use(helper.getLevel(), player, hand, hit);
        helper.assertTrue(tank.getFluidInTank(0).getAmount() == 3000 && player.getItemInHand(hand).is(Items.WATER_BUCKET),
                "A Creative player fills the final millibucket while keeping the Water Bucket");
        player.getAbilities().instabuild = false;

        offer(helper, bowl, new ItemStack(Items.NETHER_WART));
        offer(helper, bowl, new ItemStack(Items.SUGAR));
        tank.drain(500, IFluidHandler.FluidAction.EXECUTE);
        var remainder = tank.getFluidInTank(0);
        helper.getLevel().getBlockState(pos).use(helper.getLevel(), player, hand, hit);
        var after = tank.getFluidInTank(0);
        helper.assertTrue(player.getItemInHand(hand).is(Items.WATER_BUCKET) && after.getAmount() == 2500 && after.isFluidEqual(remainder),
                "Pouring plain water into a partial brew preserves its volume and complete tag");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void acceptsAnEntireLegacyStackAsOneBrewAction(GameTestHelper helper) {
        var bowl = hotCauldron(helper, new BlockPos(4, 4, 4));
        offer(helper, bowl, new ItemStack(Items.NETHER_WART, 3));
        offer(helper, bowl, new ItemStack(Items.WHITE_WOOL, 6));
        offer(helper, bowl, new ItemStack(Items.SUGAR, 4));
        var pos = bowl.getBlockPos();
        var duplicate = new ItemEntity(helper.getLevel(), pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5,
                new ItemStack(Items.SUGAR, 5));
        bowl.acceptDroppedItem(duplicate);
        helper.assertTrue(!duplicate.isRemoved() && duplicate.getItem().getCount() == 5,
                "An unaccepted stack keeps its full count");
        bowl.load(bowl.saveWithFullMetadata());
        var tank = bowl.getCapability(ForgeCapabilities.FLUID_HANDLER).orElseThrow(IllegalStateException::new);
        var tag = tank.getFluidInTank(0).getTag();
        helper.assertTrue(tag != null && tag.getInt("UsedCapacity") == 2 && tag.getInt("EffectCount") == 1,
                "An accepted Sugar stack supplies one two-level effect");
        var stored = tag.getList("Items", Tag.TAG_COMPOUND);
        helper.assertTrue(stored.size() == 2 && ItemStack.matches(new ItemStack(Items.NETHER_WART, 3), ItemStack.of(stored.getCompound(0)))
                        && ItemStack.matches(new ItemStack(Items.SUGAR, 4), ItemStack.of(stored.getCompound(1))),
                "The saved brew tag keeps the complete Wart and Sugar stacks while Wool only sets its dye");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void movesAnActiveTaglockCastThroughBucketsAndFluidCapabilities(GameTestHelper helper) {
        var source = hotCauldron(helper, new BlockPos(4, 4, 4));
        var destination = hotCauldron(helper, new BlockPos(7, 4, 4));
        var from = source.getCapability(ForgeCapabilities.FLUID_HANDLER).orElseThrow(IllegalStateException::new);
        var to = destination.getCapability(ForgeCapabilities.FLUID_HANDLER).orElseThrow(IllegalStateException::new);
        to.drain(3000, IFluidHandler.FluidAction.EXECUTE);
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "MoveTaglock"));
        var taglock = new ItemStack(WitcheryItems.getRequired("taglockkit").get());
        TaglockItem.bindSignature(taglock, helper.spawn(EntityType.PIG, new BlockPos(6, 4, 7)));
        offer(helper, source, new ItemStack(WitcheryIngredient.MANDRAKE_ROOT.item()));
        offer(helper, source, new ItemStack(Items.WHEAT));
        offer(helper, source, taglock);
        var startingCast = source.saveWithFullMetadata();
        startingCast.putInt("RitualTicks", 12);
        source.load(startingCast);

        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BUCKET));
        use(helper, player, source.getBlockPos());
        var bucket = player.getMainHandItem().copy();
        var captured = bucket.getTag();
        helper.assertTrue(bucket.is(WitcheryItems.getRequired("bucketbrew").get()) && captured != null
                        && captured.getBoolean("RitualTriggered") && ItemStack.matches(taglock,
                        ItemStack.of(captured.getList("Items", Tag.TAG_COMPOUND).getCompound(2))),
                "The Brew Bucket retains the bound Taglock and the active cast flag");
        helper.assertTrue(from.getFluidInTank(0).getAmount() == 2000 && source.saveWithoutMetadata().getInt("RitualTicks") == 0,
                "Scooping part of the cast resets its unfinished warmup");
        use(helper, player, destination.getBlockPos());
        helper.assertTrue(player.getMainHandItem().is(Items.BUCKET), "The receiving cauldron accepts the cast by hand");
        destination.load(destination.saveWithFullMetadata());
        var saved = destination.saveWithoutMetadata();
        helper.assertTrue(saved.getBoolean("LegacyRitualPending") && to.getFluidInTank(0).isFluidEqual(from.getFluidInTank(0)),
                "The two portions retain the same triggered brew across a save round trip");

        var remainder = from.drain(2000, IFluidHandler.FluidAction.SIMULATE);
        var untriggered = remainder.copy();
        untriggered.getOrCreateTag().remove("RitualTriggered");
        helper.assertTrue(from.fill(untriggered, IFluidHandler.FluidAction.SIMULATE) == 0
                        && to.fill(untriggered, IFluidHandler.FluidAction.SIMULATE) == 0,
                "Neither a live cast nor a transported remainder merges with an untriggered portion");
        helper.assertTrue(to.fill(from.drain(remainder, IFluidHandler.FluidAction.EXECUTE), IFluidHandler.FluidAction.EXECUTE) == 2000
                        && from.getFluidInTank(0).isEmpty(),
                "A tagged capability transfer completes the batch in the receiving cauldron");
        helper.runAfterDelay(3, () -> {
            var resumed = destination.saveWithoutMetadata();
            helper.assertTrue(resumed.getBoolean("RitualTriggered") && !resumed.contains("LegacyRitualPending")
                            && to.getFluidInTank(0).getAmount() == 3000,
                    "The full cauldron restores the Taglock ritual after a tagged fluid transfer");
            helper.succeed();
        });
    }

    @GameTest(template = "qa_coven_arena", timeoutTicks = 255)
    public static void topsUpAndCompletesAnActiveFixedCast(GameTestHelper helper) {
        var location = new BlockPos(4, 4, 4);
        helper.setBlock(location.below(2), Blocks.NETHERRACK);
        helper.setBlock(location.below(), Blocks.FIRE);
        var bowl = hotCauldron(helper, location);
        var pos = bowl.getBlockPos();
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "BrewTopUp"));
        var tank = bowl.getCapability(ForgeCapabilities.FLUID_HANDLER).orElseThrow(IllegalStateException::new);
        offer(helper, bowl, new ItemStack(WitcheryIngredient.MANDRAKE_ROOT.item()));
        offer(helper, bowl, new ItemStack(WitcheryIngredient.EXHALE_OF_THE_HORNED_ONE.item()));
        var egg = new ItemStack(Items.EGG).setHoverName(Component.literal("Original Trigger"));
        offer(helper, bowl, egg);

        var withdrawn = tank.drain(500, IFluidHandler.FluidAction.EXECUTE);
        helper.assertTrue(withdrawn.getAmount() == 500 && tank.getFluidInTank(0).getAmount() == 2500,
                "The pending cast can save a 2,500 mB remainder");
        bowl.load(bowl.saveWithFullMetadata());
        var bucket = BrewBucketItem.filled(withdrawn);
        var wrong = bucket.copy();
        var ingredients = wrong.getOrCreateTag().getList("Items", Tag.TAG_COMPOUND);
        ingredients.add(ingredients.remove(0));
        player.setItemInHand(InteractionHand.MAIN_HAND, wrong.copy());
        use(helper, player, pos);
        helper.assertTrue(ItemStack.matches(player.getMainHandItem(), wrong) && tank.getFluidInTank(0).getAmount() == 2500,
                "The cauldron retains the mismatched bucket and the active cast");

        player.setItemInHand(InteractionHand.MAIN_HAND, bucket.copy());
        use(helper, player, pos);
        helper.assertTrue(player.getMainHandItem().is(Items.BUCKET) && tank.getFluidInTank(0).getAmount() == 3000
                        && ItemStack.matches(ItemStack.of(bowl.saveWithoutMetadata().getCompound("RitualTrigger")), egg),
                "The matching bucket contributes the last 500 mB and returns empty while preserving the trigger");
        player.setItemInHand(InteractionHand.MAIN_HAND, bucket.copy());
        use(helper, player, pos);
        helper.assertTrue(ItemStack.matches(player.getMainHandItem(), bucket) && tank.getFluidInTank(0).getAmount() == 3000,
                "An already full cauldron returns the filled bucket");
        helper.startSequence().thenWaitUntil(() -> {
            helper.assertItemEntityCountIs(WitcheryIngredient.MUTANDIS.item(), location.above(), 2, 6);
            helper.assertTrue(tank.getFluidInTank(0).isEmpty(), "Completing the refilled ritual empties the cauldron");
        }).thenSucceed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_brew_survival_transport", timeoutTicks = 160)
    public static void movesBrewThroughSurvivalBucketsAndFluidCapabilities(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "BrewTransport"));
        var cauldron = WitcheryBlocks.getRequired("cauldron").get();
        BlockPos source = helper.absolutePos(new BlockPos(4, 4, 4));
        BlockPos destination = source.east(3);
        level.setBlockAndUpdate(source.below(2), Blocks.NETHERRACK.defaultBlockState());
        level.setBlockAndUpdate(source.below(), Blocks.FIRE.defaultBlockState());
        level.setBlockAndUpdate(source, cauldron.defaultBlockState());
        level.setBlockAndUpdate(destination, cauldron.defaultBlockState());
        var from = (WitchesCauldronBlockEntity) level.getBlockEntity(source);
        var to = (WitchesCauldronBlockEntity) level.getBlockEntity(destination);
        var sourceTank = from.getCapability(ForgeCapabilities.FLUID_HANDLER).orElseThrow(IllegalStateException::new);
        var destinationTank = to.getCapability(ForgeCapabilities.FLUID_HANDLER).orElseThrow(IllegalStateException::new);
        for (int i = 0; i < 3; i++) {
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WATER_BUCKET));
            use(helper, player, source);
            helper.assertTrue(player.getMainHandItem().is(Items.BUCKET), "Filling the cauldron returns an empty bucket");
        }
        helper.startSequence().thenWaitUntil(() -> {
            helper.assertTrue(level.getBlockState(source).getValue(WitchesCauldronBlock.BOILING), "Fire brings the brew to a boil");
        }).thenExecute(() -> {
            for (var item : new ItemStack[]{new ItemStack(Items.NETHER_WART), new ItemStack(Items.SUGAR), new ItemStack(Items.BLUE_WOOL)}) {
                var dropped = new ItemEntity(level, source.getX() + 0.5, source.getY() + 0.4, source.getZ() + 0.5, item);
                level.getBlockState(source).entityInside(level, source, dropped);
                helper.assertTrue(dropped.isRemoved(), "The boiling cauldron accepts the dropped ingredient");
            }
            FluidStack original = sourceTank.getFluidInTank(0);
            helper.assertTrue(original.getFluid() == WitcheryFluids.getRequiredSource("brew").get() && original.getAmount() == 3000,
                    "The exposed tank represents an ordered brew as its registered fluid");
            var originalTag = original.getTag();
            helper.assertTrue(originalTag != null && originalTag.getList("Items", Tag.TAG_COMPOUND).size() == 2,
                    "The transport tag retains the two brewing ingredients in order");

            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BUCKET));
            use(helper, player, source);
            var bucket = player.getMainHandItem();
            helper.assertTrue(bucket.is(WitcheryItems.getRequired("bucketbrew").get()) && originalTag.equals(bucket.getTag())
                            && sourceTank.getFluidInTank(0).getAmount() == 2000,
                    "An empty bucket collects exactly 1,000 mB with its brew tag");
            bucket = ItemStack.of(bucket.save(new CompoundTag()));
            player.setItemInHand(InteractionHand.MAIN_HAND, bucket);
            use(helper, player, destination);
            helper.assertTrue(player.getMainHandItem().is(Items.BUCKET), "Pouring the saved bucket returns the empty bucket");
            var transferred = destinationTank.getFluidInTank(0);
            helper.assertTrue(transferred.getAmount() == 1000 && transferred.isFluidEqual(original),
                    "Refilling a cold cauldron retains the complete brew");
            to.load(to.saveWithFullMetadata());
            var reopened = destinationTank.getFluidInTank(0);
            var brewedEffect = BrewMixture.from(reopened.getTag());
            helper.assertTrue(reopened.getAmount() == 1000 && reopened.isFluidEqual(original)
                            && brewedEffect != null && brewedEffect.effects().stream().anyMatch(effect -> effect.effect() == MobEffects.MOVEMENT_SPEED),
                    "The transferred color, volume and Swiftness effect survive cauldron serialization");

            var incompatible = reopened.copy();
            var alternatives = incompatible.getTag().getList("Items", Tag.TAG_COMPOUND);
            Tag first = alternatives.remove(0);
            alternatives.add(first);
            helper.assertTrue(destinationTank.fill(incompatible, IFluidHandler.FluidAction.EXECUTE) == 0,
                    "Reordering the ingredients makes a different mixture");
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WATER_BUCKET));
            use(helper, player, destination);
            helper.assertTrue(player.getMainHandItem().is(Items.WATER_BUCKET), "Water cannot dilute a cauldron of brew");

            var externalTank = new FluidTank(1000);
            var moved = FluidUtil.tryFluidTransfer(externalTank, sourceTank, 1000, false);
            helper.assertTrue(moved.getAmount() == 1000 && moved.isFluidEqual(original) && externalTank.isEmpty()
                            && sourceTank.getFluidInTank(0).getAmount() == 2000, "Simulation does not move fluid");
            moved = FluidUtil.tryFluidTransfer(externalTank, sourceTank, 1000, true);
            helper.assertTrue(moved.getAmount() == 1000 && externalTank.getFluid().isFluidEqual(original),
                    "A Forge tank receives the same complete mixture");
            moved = FluidUtil.tryFluidTransfer(destinationTank, externalTank, 1000, true);
            helper.assertTrue(externalTank.isEmpty() && moved.getAmount() == 1000
                            && destinationTank.getFluidInTank(0).getAmount() == 2000,
                    "An identical mixture can be combined again");
        }).thenSucceed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void woolColorsTheStoredBrewWithoutAddingAnIngredient(GameTestHelper helper) {
        int blue = 2247599;
        int yellow = 15197994;
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(4, 4, 4));
        level.setBlockAndUpdate(pos, WitcheryBlocks.getRequired("cauldron").get().defaultBlockState());
        var bowl = (WitchesCauldronBlockEntity) level.getBlockEntity(pos);
        var water = bowl.saveWithoutMetadata();
        water.put("Tank", new FluidStack(Fluids.WATER, 3000).writeToNBT(new CompoundTag()));
        water.putInt("HeatTicks", 100);
        bowl.load(water);

        offer(helper, bowl, new ItemStack(Items.BLUE_WOOL));
        offer(helper, bowl, new ItemStack(Items.BLUE_WOOL));
        var save = bowl.saveWithoutMetadata();
        helper.assertTrue(save.getInt("Color") == blue && save.getList("Ingredients", Tag.TAG_COMPOUND).isEmpty()
                        && bowl.getUpdateTag().getInt("Color") == blue,
                "Repeated wool is consumed and saves the original blue without entering the recipe");
        var tank = bowl.getCapability(ForgeCapabilities.FLUID_HANDLER).orElseThrow(IllegalStateException::new);
        var sampled = tank.drain(1000, IFluidHandler.FluidAction.SIMULATE);
        var payload = sampled.getTag();
        helper.assertTrue(sampled.getFluid() == WitcheryFluids.getRequiredSource("brew").get() && sampled.getAmount() == 1000
                        && payload != null && payload.getInt("Color") == blue
                        && payload.getList("Items", Tag.TAG_COMPOUND).isEmpty() && tank.getFluidInTank(0).getAmount() == 3000,
                "Simulated transfer exposes the saved color and leaves all 3,000 mB in the cauldron");

        var mandrake = new ItemStack(WitcheryIngredient.MANDRAKE_ROOT.item());
        offer(helper, bowl, mandrake);
        helper.assertTrue(bowl.getUpdateTag().getInt("Color") == BrewMixture.blendColor(blue, mandrake),
                "Later recipe ingredients update the active brew color");
        offer(helper, bowl, new ItemStack(Items.RED_DYE));
        offer(helper, bowl, new ItemStack(Items.YELLOW_WOOL));
        save = bowl.saveWithoutMetadata();
        bowl.load(save);
        helper.assertTrue(bowl.getUpdateTag().getInt("Color") == yellow && save.getList("Ingredients", Tag.TAG_COMPOUND).size() == 2,
                "The last wool sets the visible color and reloading preserves the two actual ingredients");
        offer(helper, bowl, new ItemStack(WitcheryIngredient.GYPSUM.item()));
        helper.assertTrue(bowl.getUpdateTag().getInt("Color") == -1 && !bowl.saveWithoutMetadata().contains("Color"),
                "Clearing the pot resets the saved color");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void legacyColorSurvivesFixedRecipeAndBottling(GameTestHelper helper) {
        var bowl = hotCauldron(helper, new BlockPos(4, 4, 4));
        offer(helper, bowl, new ItemStack(WitcheryIngredient.MANDRAKE_ROOT.item()));
        offer(helper, bowl, new ItemStack(WitcheryIngredient.EXHALE_OF_THE_HORNED_ONE.item()));
        var save = bowl.saveWithoutMetadata();
        helper.assertTrue(save.getInt("Color") == -1870303639 && bowl.getUpdateTag().getInt("Color") == 0x856E69,
                "The two legacy ingredient subtypes use the same hash and retain a signed color in the save");
        bowl.load(save);
        offer(helper, bowl, new ItemStack(Items.EGG));
        var bottled = bowl.dispenseBottle().getTag();
        helper.assertTrue(bottled != null && bottled.getInt("Color") == 694624844
                        && bottled.getString("FixedRitual").equals("witchery:mutandis"),
                "Bottling the active Mutandis ritual preserves the original color of all three ingredients");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void dispersalKeepsColorHistoryThroughFluidTransfer(GameTestHelper helper) {
        var bowl = hotCauldron(helper, new BlockPos(4, 4, 4));
        offer(helper, bowl, new ItemStack(WitcheryIngredient.MANDRAKE_ROOT.item()));
        offer(helper, bowl, new ItemStack(Items.WHEAT));
        var source = bowl.getCapability(ForgeCapabilities.FLUID_HANDLER).orElseThrow(IllegalStateException::new);
        var moved = source.drain(3000, IFluidHandler.FluidAction.EXECUTE);
        helper.assertTrue(moved.getTag() != null && moved.getTag().getInt("Color") == -1781812758,
                "Transferring a mixed brew preserves its original signed color");

        var target = hotCauldron(helper, new BlockPos(7, 4, 4));
        var tank = target.getCapability(ForgeCapabilities.FLUID_HANDLER).orElseThrow(IllegalStateException::new);
        tank.drain(3000, IFluidHandler.FluidAction.EXECUTE);
        helper.assertTrue(tank.fill(moved, IFluidHandler.FluidAction.EXECUTE) == 3000, "The receiving cauldron accepts the brew");
        var heated = target.saveWithoutMetadata();
        heated.putInt("HeatTicks", 100);
        target.load(heated);
        offer(helper, target, new ItemStack(Items.GUNPOWDER));

        var bottle = target.dispenseBottle().getTag();
        helper.assertTrue(bottle != null && bottle.getInt("Color") == 62622027
                        && bottle.getList("Items", Tag.TAG_COMPOUND).size() == 2
                        && bottle.getInt("UsedCapacity") == 0 && bottle.getBoolean("Splash"),
                "Gunpowder removes Wheat's effect and still blends against the color Wheat already produced");
        helper.succeed();
    }

    private static WitchesCauldronBlockEntity hotCauldron(GameTestHelper helper, BlockPos position) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(position);
        level.setBlockAndUpdate(pos, WitcheryBlocks.getRequired("cauldron").get().defaultBlockState());
        var bowl = (WitchesCauldronBlockEntity) level.getBlockEntity(pos);
        var save = bowl.saveWithoutMetadata();
        save.put("Tank", new FluidStack(Fluids.WATER, 3000).writeToNBT(new CompoundTag()));
        save.putInt("HeatTicks", 100);
        bowl.load(save);
        return bowl;
    }

    private static void offer(GameTestHelper helper, WitchesCauldronBlockEntity bowl, ItemStack stack) {
        var pos = bowl.getBlockPos();
        var dropped = new ItemEntity(helper.getLevel(), pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, stack.copy());
        bowl.acceptDroppedItem(dropped);
        helper.assertTrue(dropped.isRemoved(), "The cauldron accepts " + stack.getHoverName().getString());
    }

    private static void use(GameTestHelper helper, Player player, BlockPos cauldron) {
        var level = helper.getLevel();
        var hit = new BlockHitResult(Vec3.atCenterOf(cauldron), Direction.UP, cauldron, false);
        var state = level.getBlockState(cauldron);
        var held = player.getMainHandItem().getItem();
        helper.assertTrue(state.use(level, player, InteractionHand.MAIN_HAND, hit).consumesAction(),
                "The cauldron handles " + held + " at " + cauldron + ", found " + state);
    }
}
