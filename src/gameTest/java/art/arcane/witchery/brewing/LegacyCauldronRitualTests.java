package art.arcane.witchery.brewing;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.entity.WitchesCauldronBlockEntity;
import art.arcane.witchery.capability.WitcheryPlayerDataProvider;
import art.arcane.witchery.item.WaystoneItem;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryItems;
import art.arcane.witchery.registry.WitcheryRecipes;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class LegacyCauldronRitualTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_gas_ritual_height")
    public static void gasWaveKeepsTheTargetHeightWhenFootingIsAboveOrBelow(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos target = helper.absolutePos(new BlockPos(8, 0, 8)).atY(198);
        var trigger = new ItemStack(WitcheryIngredient.DOG_TONGUE.item());
        var offerings = List.of(new ItemStack(WitcheryIngredient.MANDRAKE_ROOT.item()), new ItemStack(Items.NETHER_WART),
                new ItemStack(Items.SUGAR), new ItemStack(Items.COAL), new ItemStack(WitcheryIngredient.BAT_WOOL.item()), trigger);
        var brew = BrewMixture.from(offerings, null);
        var atTarget = EntityType.PIG.create(level);
        var onShelf = EntityType.PIG.create(level);
        atTarget.setPos(Vec3.atLowerCornerOf(target).add(0.25, 0.5, 0.25));
        onShelf.setPos(Vec3.atLowerCornerOf(target).add(0.25, 5, 0.25));
        for (var pig : List.of(atTarget, onShelf)) {
            pig.setNoAi(true);
            pig.setNoGravity(true);
            level.addFreshEntity(pig);
        }
        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(level.getEntity(atTarget.getUUID()) == atTarget
                        && level.getEntity(onShelf.getUUID()) == onShelf, "Both pigs are tracked"))
                .thenExecute(() -> {
                    for (int offset : new int[]{4, -4, -8}) {
                        BlockPos shelf = target.above(offset);
                        BlockPos targetFire = target.east(2);
                        BlockPos shelfFire = shelf.east(2).above();
                        level.setBlockAndUpdate(shelf, Blocks.STONE.defaultBlockState());
                        onShelf.setPos(Vec3.atLowerCornerOf(shelf.above()).add(0.25, 0, 0.25));
                        for (BlockPos fire : List.of(targetFire, shelfFire)) {
                            level.setBlockAndUpdate(fire.below(), Blocks.NETHERRACK.defaultBlockState());
                            level.setBlockAndUpdate(fire, Blocks.FIRE.defaultBlockState());
                        }
                        level.getRandom().setSeed(5120); // Make the first terrain roll less than one percent.
                        BrewRituals.pulse(level, target, brew, brew.bottleTag(offerings), trigger, 1, false);
                        helper.assertTrue(atTarget.hasEffect(MobEffects.MOVEMENT_SPEED) == (offset != -8)
                                        && !onShelf.hasEffect(MobEffects.MOVEMENT_SPEED),
                                "Only a shelf within seven blocks admits the wave at its original height: " + offset);
                        helper.assertTrue(level.isEmptyBlock(targetFire) == (offset != -8)
                                        && level.getBlockState(shelfFire).is(Blocks.FIRE),
                                "The terrain roll reaches the target row and preserves fire at the shelf: " + offset);
                        atTarget.removeEffect(MobEffects.MOVEMENT_SPEED);
                        for (BlockPos fire : List.of(targetFire, shelfFire)) {
                            level.removeBlock(fire, false);
                            level.removeBlock(fire.below(), false);
                        }
                        level.setBlockAndUpdate(shelf, Blocks.AIR.defaultBlockState());
                    }
                    atTarget.discard();
                    onShelf.discard();
                }).thenSucceed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void distinguishesMissingCirclesFromDistantTargets(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos origin = helper.absolutePos(new BlockPos(6, 4, 5));
        var trigger = new ItemStack(WitcheryIngredient.DOG_TONGUE.item());
        var missingCircles = List.of(new ItemStack(WitcheryIngredient.MANDRAKE_ROOT.item()), new ItemStack(Items.NETHER_WART),
                new ItemStack(WitcheryIngredient.TEAR_OF_THE_GODDESS.item()), new ItemStack(WitcheryIngredient.REFINED_EVIL.item()),
                new ItemStack(Items.GUNPOWDER), trigger);
        var hellGate = BrewMixture.from(missingCircles, null);
        helper.assertTrue(hellGate.requiresInfernalCircle(), "The fixture contains Hell Gate");
        helper.assertTrue(BrewRituals.pulse(level, origin, hellGate, hellGate.bottleTag(missingCircles), trigger, 1, false)
                        == BrewRituals.Result.INVALID_CIRCLES,
                "A Hell Gate without its Infernal ring reports invalid circles");

        var bound = new ItemStack(WitcheryIngredient.BOUND_WAYSTONE.item());
        WaystoneItem.bindLocation(bound, level, origin.east(33));
        var distant = List.of(new ItemStack(Items.NETHER_WART), new ItemStack(Items.SUGAR),
                new ItemStack(Items.GUNPOWDER), bound, trigger);
        var swiftness = BrewMixture.from(distant, null);
        helper.assertTrue(BrewRituals.pulse(level, origin, swiftness, swiftness.bottleTag(distant), trigger, 1, false)
                        == BrewRituals.Result.TOO_FAR,
                "An ordinary brew aimed one block past its range reports distance");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void legacyRecipeOfferingsNeedPreparedEffectCapacity(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(6, 4, 5));
        var water = boilingWater();
        var cauldron = placeCauldron(helper, pos, water);
        var mandrake = WitcheryIngredient.MANDRAKE_ROOT.item();
        var tear = WitcheryIngredient.TEAR_OF_THE_GODDESS.item();
        var evil = WitcheryIngredient.REFINED_EVIL.item();
        var extremis = WitcheryIngredient.MUTANDIS_EXTREMIS.item();

        helper.assertTrue(!offer(cauldron, evil) && !offer(cauldron, WitcheryIngredient.ENT_BRANCH.item()),
                "The empty brew should reject effect ingredients even when a fixed recipe calls for them");
        helper.assertTrue(offer(cauldron, Items.NETHER_WART) && offer(cauldron, mandrake) && offer(cauldron, tear)
                        && !offer(cauldron, evil) && !offer(cauldron, extremis),
                "Adding Wart before Mandrake leaves the mixture one effect level short of Drop of Luck");

        cauldron.load(water);
        helper.assertTrue(offer(cauldron, mandrake) && offer(cauldron, Items.NETHER_WART) && offer(cauldron, tear)
                        && offer(cauldron, evil) && offer(cauldron, extremis),
                "Adding Mandrake first supplies the capacity for all of the original offerings");
        helper.assertTrue(cauldron.saveWithFullMetadata().getString("Ritual").equals("witchery:drop_of_luck"),
                "The same fixed recipe should begin after the original ordered preparation");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void legacyFixedRecipesAcceptTheirPreparedOfferings(GameTestHelper helper) {
        var level = helper.getLevel();
        var water = boilingWater();
        var cauldron = placeCauldron(helper, helper.absolutePos(new BlockPos(6, 4, 5)), water);
        var recipes = level.getRecipeManager().getAllRecipesFor(WitcheryRecipes.CAULDRON_RITUAL.get()).stream()
                .filter(CauldronRitualRecipe::legacy).toList();
        helper.assertTrue(recipes.size() == 27, "Exercise all twenty-seven original fixed cauldron recipes");
        for (var recipe : recipes) {
            cauldron.load(water);
            for (var ingredient : recipe.ingredients()) {
                ItemStack stack = ingredient.getItems()[0];
                helper.assertTrue(offer(cauldron, stack.copyWithCount(1)), recipe.id() + " should accept " + stack.getItem());
            }
            helper.assertTrue(offer(cauldron, recipe.trigger().getItems()[0].copyWithCount(1)), recipe.id() + " should accept its trigger");
            helper.assertTrue(cauldron.saveWithFullMetadata().getString("Ritual").equals(recipe.id().toString()),
                    recipe.id() + " should start after all of its original offerings are in the cauldron");
        }
        helper.succeed();
    }

    private static CompoundTag boilingWater() {
        var water = new CompoundTag();
        water.putString("FluidName", "water");
        water.putInt("Amount", 3000);
        water.putInt("HeatTicks", 100);
        return water;
    }

    private static boolean offer(WitchesCauldronBlockEntity cauldron, Item item) {
        return offer(cauldron, new ItemStack(item));
    }

    private static boolean offer(WitchesCauldronBlockEntity cauldron, ItemStack stack) {
        Vec3 center = Vec3.atCenterOf(cauldron.getBlockPos());
        var dropped = new ItemEntity(cauldron.getLevel(), center.x, center.y, center.z, stack);
        cauldron.acceptDroppedItem(dropped);
        return dropped.isRemoved();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_legacy_healing")
    public static void originalMelonBrewsRetainTheirInstantEffects(GameTestHelper helper) {
        var wart = oldItem("minecraft:nether_wart", 0, 1);
        var melon = oldItem("minecraft:speckled_melon", 0, 1);
        var original = originalRitual(3000, 0, true, wart, melon);
        var originalBrew = original.getCompound("Tag");
        originalBrew.remove("RitualTriggered");
        var cauldron = placeCauldron(helper, helper.absolutePos(new BlockPos(6, 4, 5)), original);
        cauldron.load(cauldron.saveWithFullMetadata());
        var saved = cauldron.saveWithFullMetadata();
        var ingredients = saved.getList("Ingredients", Tag.TAG_COMPOUND);
        helper.assertTrue(ingredients.size() == 2 && ingredients.getCompound(0).getString("id").equals("minecraft:nether_wart")
                        && ingredients.getCompound(1).getString("id").equals("minecraft:glistering_melon_slice")
                        && saved.getInt("Color") == originalBrew.getInt("Color") && tank(cauldron).getFluidInTank(0).getAmount() == 3000,
                "The original cauldron should retain its ordered healing brew, amount and color through a save round trip");

        var bottle = new ItemStack(WitcheryItems.getRequired("brewbottle").get());
        bottle.setTag(originalBrew.copy());
        var healed = helper.spawn(EntityType.PIG, new BlockPos(4, 3, 5));
        healed.setHealth(4.0F);
        helper.assertTrue(bottle.finishUsingItem(helper.getLevel(), healed).is(Items.GLASS_BOTTLE) && healed.getHealth() == 8.0F,
                "A saved healing bottle should heal by four points and return glass");

        var inverted = originalRitual(3000, 0, true, wart, oldItem("minecraft:fermented_spider_eye", 0, 1), melon)
                .getCompound("Tag");
        inverted.remove("RitualTriggered");
        var second = new ItemStack(WitcheryItems.getRequired("brewbottle").get());
        second.setTag(inverted);
        var hurt = helper.spawn(EntityType.PIG, new BlockPos(8, 3, 5));
        helper.assertTrue(second.finishUsingItem(helper.getLevel(), hurt).is(Items.GLASS_BOTTLE) && hurt.getHealth() == 4.0F,
                "The original inverted bottle should deal six points of instant damage");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void resumesOriginalMutandisWarmupAcrossBothSaveFormats(GameTestHelper helper) {
        BlockPos relative = new BlockPos(6, 4, 5);
        BlockPos pos = helper.absolutePos(relative);
        var cauldron = placeCauldron(helper, pos, originalRitual(3000, 19, true,
                oldItem("witchery:ingredient", 22, 1), oldItem("witchery:ingredient", 31, 1), oldItem("minecraft:egg", 0, 1)));
        CompoundTag beforeFirstTick = cauldron.saveWithFullMetadata();
        helper.assertTrue(beforeFirstTick.getBoolean("LegacyRitualPending") && beforeFirstTick.getInt("RitualTicks") == 19,
                "Saving before recipe lookup preserves the original trigger and pulse count");
        cauldron.load(beforeFirstTick);
        helper.runAfterDelay(4, () -> {
            CompoundTag resumed = cauldron.saveWithFullMetadata();
            helper.assertTrue(resumed.getString("Ritual").equals("witchery:mutandis") && resumed.getInt("RitualTicks") >= 190
                            && resumed.getInt("RitualTicks") < 200 && resumed.getList("Ingredients", Tag.TAG_COMPOUND).size() == 2,
                    "The server selects Mutandis and converts warmup pulses to ticks without keeping the Egg as an ingredient");
            cauldron.load(resumed);
        });
        helper.runAfterDelay(28, () -> {
            helper.assertItemEntityCountIs(WitcheryIngredient.MUTANDIS.item(), relative.above(2), 3, 6);
            helper.assertTrue(tank(cauldron).getFluidInTank(0).isEmpty(), "The resumed recipe yields six Mutandis and clears the tank");
            helper.succeed();
        });
    }

    @GameTest(template = "qa_coven_arena")
    public static void readsComparatorEffectsFromAnOriginalPartialCast(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(6, 4, 5));
        var bowl = placeCauldron(helper, pos, originalRitual(2500, 0, true,
                oldItem("witchery:ingredient", 22, 1), oldItem("witchery:ingredient", 31, 1),
                oldItem("minecraft:wheat", 0, 1), oldItem("minecraft:egg", 0, 1)));
        bowl.load(bowl.saveWithFullMetadata());
        helper.succeedWhen(() -> {
            helper.assertTrue(bowl.comparatorSignal() == 15,
                    "The original-format cast reports full brew capacity while it waits for its final liquid");
            helper.assertTrue(bowl.saveWithoutMetadata().getBoolean("LegacyRitualPending"),
                    "Reading the comparator leaves the original trigger in its pending save format");
        });
    }

    @GameTest(template = "qa_coven_arena")
    public static void preservesFixedOfferingCountsThroughBrewTransfer(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(6, 4, 5));
        var source = placeCauldron(helper, pos, boilingWater());
        var receiver = placeCauldron(helper, pos.east(4), new CompoundTag());
        var root = new ItemStack(WitcheryIngredient.MANDRAKE_ROOT.item(), 3);
        var exhale = new ItemStack(WitcheryIngredient.EXHALE_OF_THE_HORNED_ONE.item(), 2);
        var eggs = new ItemStack(Items.EGG, 4).setHoverName(Component.literal("Stacked Trigger"));
        helper.assertTrue(offer(source, root) && offer(source, exhale) && offer(source, eggs),
                "A dropped stack is accepted as one offering for each ingredient and trigger");
        var saved = source.saveWithFullMetadata();
        helper.assertTrue(saved.getString("Ritual").equals("witchery:mutandis")
                        && ItemStack.matches(eggs, ItemStack.of(saved.getCompound("RitualTrigger"))),
                "The active fixed recipe keeps all four eggs and their name");
        var brew = tank(source).drain(3000, IFluidHandler.FluidAction.EXECUTE);
        helper.assertTrue(tank(receiver).fill(brew, IFluidHandler.FluidAction.EXECUTE) == 3000,
                "The destination can accept the full tagged cast");
        receiver.load(receiver.saveWithFullMetadata());
        helper.runAfterDelay(5, () -> {
            var restored = receiver.saveWithoutMetadata();
            var offerings = restored.getList("Ingredients", Tag.TAG_COMPOUND);
            helper.assertTrue(restored.getString("Ritual").equals("witchery:mutandis") && offerings.size() == 2
                            && ItemStack.matches(root, ItemStack.of(offerings.getCompound(0)))
                            && ItemStack.matches(exhale, ItemStack.of(offerings.getCompound(1)))
                            && ItemStack.matches(eggs, ItemStack.of(restored.getCompound("RitualTrigger"))),
                    "Resuming a transferred fixed cast retains the quantities and tags of all original offerings");
            helper.succeed();
        });
    }

    @GameTest(template = "qa_coven_arena")
    public static void withdrawsFromAnOriginalPartialCast(GameTestHelper helper) {
        BlockPos pos = helper.absolutePos(new BlockPos(6, 4, 5));
        var bowl = placeCauldron(helper, pos, originalRitual(2500, 0, true,
                oldItem("witchery:ingredient", 22, 1), oldItem("witchery:ingredient", 31, 1),
                oldItem("minecraft:wheat", 0, 1), oldItem("minecraft:egg", 0, 1)));
        bowl.load(bowl.saveWithFullMetadata());
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "BottleImportedCast"));
        WitcheryPlayerDataProvider.get(player).orElseThrow(IllegalStateException::new).setPotionBottling(30);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GLASS_BOTTLE));
        helper.assertTrue(bowl.bottle(player, InteractionHand.MAIN_HAND), "The imported brewing remainder can be bottled before a refill");
        var bottled = player.getMainHandItem().getOrCreateTag();
        helper.assertTrue(bottled.getString("FixedRitual").equals("witchery:mutandis")
                        && bottled.getBoolean("RitualTriggered") && tank(bowl).getFluidInTank(0).getAmount() == 1000,
                "The bottle keeps the original cast and the trained player leaves exactly one bucket");
        var drained = tank(bowl).drain(1000, IFluidHandler.FluidAction.EXECUTE);
        helper.assertTrue(drained.getAmount() == 1000 && drained.getTag() != null && drained.getTag().getBoolean("RitualTriggered")
                        && drained.getTag().getList("Items", Tag.TAG_COMPOUND).equals(bottled.getList("Items", Tag.TAG_COMPOUND))
                        && !bowl.saveWithoutMetadata().contains("LegacyRitualPending"),
                "Draining the final bucket retains the original trigger and clears the cauldron");
        helper.assertTrue(tank(bowl).fill(drained, IFluidHandler.FluidAction.EXECUTE) == 1000
                        && bowl.saveWithoutMetadata().getBoolean("LegacyRitualPending"),
                "Returning the imported bucket restores the part-filled pending cast");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void refillsLegacyCastFromItsSavedBrewBucket(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(6, 4, 5));
        BlockPos receiver = pos.east(4);
        CompoundTag original = originalRitual(2000, 0, false, oldItem("witchery:ingredient", 22, 1),
                oldItem("minecraft:glowstone_dust", 0, 1), oldItem("minecraft:coal", 1, 1));
        var cauldron = placeCauldron(helper, pos, original);
        var emptyCauldron = placeCauldron(helper, receiver, new CompoundTag());
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "LegacyCauldron"));
        helper.runAfterDelay(3, () -> {
            cauldron.load(cauldron.saveWithFullMetadata());
            helper.assertTrue(cauldron.saveWithFullMetadata().getBoolean("LegacyRitualPending")
                            && tank(cauldron).getFluidInTank(0).getAmount() == 2000,
                    "A partially drained legacy cast retains its trigger and liquid across a saved round trip");
            var bucket = new ItemStack(WitcheryItems.getRequired("bucketbrew").get());
            bucket.setTag(original.getCompound("Tag").copy());
            player.setItemInHand(InteractionHand.MAIN_HAND, bucket);
            var hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
            level.getBlockState(pos).use(level, player, InteractionHand.MAIN_HAND, hit);
            helper.assertTrue(player.getMainHandItem().is(Items.BUCKET), "Using the original matching brew bucket restores the final bucket");

            for (int index = 0; index < 3; index++) {
                var portion = new ItemStack(WitcheryItems.getRequired("bucketbrew").get());
                portion.setTag(original.getCompound("Tag").copy());
                player.setItemInHand(InteractionHand.MAIN_HAND, portion);
                var receiverHit = new BlockHitResult(Vec3.atCenterOf(receiver), Direction.UP, receiver, false);
                level.getBlockState(receiver).use(level, player, InteractionHand.MAIN_HAND, receiverHit);
                helper.assertTrue(player.getMainHandItem().is(Items.BUCKET), "The empty cauldron accepts all three saved Brew Buckets");
            }
        });
        helper.runAfterDelay(6, () -> {
            CompoundTag restored = cauldron.saveWithFullMetadata();
            helper.assertTrue(restored.getString("Ritual").equals("witchery:brew_fuel_1") && !restored.contains("LegacyRitualPending")
                            && tank(cauldron).getFluidInTank(0).getAmount() == 3000,
                    "Charcoal resolves to the highest matching Brew Fuel recipe and the full cast waits for heat and power");
            helper.assertTrue(emptyCauldron.saveWithFullMetadata().getString("Ritual").equals("witchery:brew_fuel_1"),
                    "The legacy buckets also retain their cast in a previously empty cauldron");
            helper.succeed();
        });
    }

    @GameTest(template = "qa_coven_arena")
    public static void preservesLegacyGeneralBrewTrigger(GameTestHelper helper) {
        var pos = helper.absolutePos(new BlockPos(6, 4, 5));
        var cauldron = placeCauldron(helper, pos, originalRitual(3000, 0, false, oldItem("minecraft:nether_wart", 0, 1),
                oldItem("minecraft:sugar", 0, 1), oldItem("minecraft:gunpowder", 0, 1), oldItem("witchery:dog_tongue", 0, 1)));
        helper.runAfterDelay(5, () -> {
            var saved = cauldron.saveWithFullMetadata();
            helper.assertTrue(saved.getBoolean("RitualTriggered") && !saved.contains("LegacyRitualPending") && !saved.contains("Ritual")
                            && saved.getList("Ingredients", Tag.TAG_COMPOUND).size() == 4,
                    "The old Tongue of Dog retains its normal cast state and place at the end of the mixture");
            helper.succeed();
        });
    }

    @GameTest(template = "qa_coven_arena")
    public static void refundsUnmatchedLegacyTriggerAndKeepsMixture(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(6, 4, 5));
        var cauldron = placeCauldron(helper, pos, originalRitual(3000, 0, false, oldItem("minecraft:nether_wart", 0, 1),
                oldItem("minecraft:egg", 0, 3)));
        helper.runAfterDelay(5, () -> {
            cauldron.load(cauldron.saveWithFullMetadata());
            CompoundTag saved = cauldron.saveWithFullMetadata();
            helper.assertTrue(!saved.contains("Ritual") && !saved.contains("LegacyRitualPending")
                            && saved.getList("Ingredients", Tag.TAG_COMPOUND).size() == 1
                            && tank(cauldron).getFluidInTank(0).getAmount() == 3000,
                    "A recipe missing from the installed data releases the cauldron and preserves the brew");
        });
        helper.runAfterDelay(10, () -> {
            var eggs = level.getEntitiesOfClass(ItemEntity.class, new AABB(pos).inflate(3), item -> item.getItem().is(Items.EGG));
            helper.assertTrue(eggs.size() == 1 && eggs.get(0).getItem().getCount() == 3,
                    "The original trigger stack is refunded once even across a saved round trip");
            helper.succeed();
        });
    }

    private static WitchesCauldronBlockEntity placeCauldron(GameTestHelper helper, BlockPos pos, CompoundTag original) {
        var level = helper.getLevel();
        if (original.getInt("TicksHeated") != 0) {
            level.setBlockAndUpdate(pos.below(2), Blocks.NETHERRACK.defaultBlockState());
            level.setBlockAndUpdate(pos.below(), Blocks.FIRE.defaultBlockState());
        }
        level.setBlockAndUpdate(pos, WitcheryBlocks.getRequired("cauldron").get().defaultBlockState());
        var cauldron = (WitchesCauldronBlockEntity) level.getBlockEntity(pos);
        cauldron.load(original);
        return cauldron;
    }

    private static IFluidHandler tank(WitchesCauldronBlockEntity cauldron) {
        return cauldron.getCapability(ForgeCapabilities.FLUID_HANDLER).orElseThrow(IllegalStateException::new);
    }

    private static CompoundTag originalRitual(int fluid, int pulses, boolean heated, CompoundTag... offerings) {
        var root = new CompoundTag();
        root.putString("FluidName", "witchery:brew");
        root.putInt("Amount", fluid);
        root.putInt("TicksHeated", heated ? 100 : 0);
        root.putInt("RitualTicks", pulses);
        var brew = new CompoundTag();
        brew.putInt("Color", 0xb4a96c);
        brew.putBoolean("RitualTriggered", true);
        var items = new ListTag();
        for (var offering : offerings) items.add(offering);
        brew.put("Items", items);
        root.put("Tag", brew);
        return root;
    }

    private static CompoundTag oldItem(String id, int damage, int count) {
        var item = new CompoundTag();
        item.putString("id", id);
        item.putShort("Damage", (short) damage);
        item.putByte("Count", (byte) count);
        return item;
    }
}
