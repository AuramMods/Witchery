package art.arcane.witchery.entity;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.WitchCrop;
import art.arcane.witchery.brewing.BrewMixture;
import art.arcane.witchery.item.TaglockItem;
import art.arcane.witchery.item.WaystoneItem;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcheryItems;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class FlyingCompanionTests {
    private static final String TEMPLATE = "qa_flying_route";
    private static final InteractionHand HAND = InteractionHand.MAIN_HAND;

    @GameTest(template = TEMPLATE)
    public static void flyingCompanionsApproachFoodAndStartleAtCloseMovement(GameTestHelper helper) {
        var level = helper.getLevel();
        Vec3 start = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(8, 0, 8)).atY(188));
        for (var type : List.of(WitcheryEntities.OWL.get(), WitcheryEntities.WINGED_MONKEY.get())) {
            TamableAnimal pet = type.create(level);
            pet.setPos(start);
            var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "FlyingTempt"));
            player.setPos(start.add(7, 0, 0));
            level.addNewPlayer(player);
            try {
                var tempt = new FlyingCompanionGoals.Tempt(pet);
                player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.BEEF));
                helper.assertTrue(!tempt.canUse(), "Food in the off hand does not lure the companion");
                player.setItemInHand(HAND, new ItemStack(Items.PORKCHOP));
                helper.assertTrue(tempt.canUse(), "The untamed companion recognizes raw food");
                tempt.start();
                tempt.tick();
                helper.assertTrue(pet.getDeltaMovement().distanceTo(new Vec3(0.05, 0.025, 0)) < 0.00001,
                        "The companion approaches the food with the original forward and upward impulse");
                player.setPos(start.add(8, 0, 0));
                helper.assertTrue(tempt.canContinueToUse(), "Movement beyond six blocks updates the tracked point");
                pet.setPos(start.add(3, 0, 0));
                player.setYRot(180);
                helper.assertTrue(tempt.canContinueToUse(), "Turning in place does not startle the companion");
                player.setPos(start.add(8.11, 0, 0));
                helper.assertTrue(!tempt.canContinueToUse(), "Moving over a tenth of a block nearby interrupts the lure");
                tempt.stop();
                for (int tick = 0; tick < 100; tick++) {
                    helper.assertTrue(!tempt.canUse(), "An interrupted lure waits through the original retry count");
                }
                helper.assertTrue(tempt.canUse(), "A wild companion can approach the food again");
                pet.tame(player);
                helper.assertTrue(!tempt.canUse(), "Raw food no longer controls an owned companion's flight");
            } finally {
                level.removePlayerImmediately(player, net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
            }
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void flyingCompanionsBreedAfterSixtyTicksWithinThreeBlocks(GameTestHelper helper) {
        var level = helper.getLevel();
        Vec3 start = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(8, 0, 8)).atY(188));
        for (var type : List.of(WitcheryEntities.OWL.get(), WitcheryEntities.WINGED_MONKEY.get())) {
            TamableAnimal parent = type.create(level);
            TamableAnimal partner = type.create(level);
            var owner = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "FlyingBreeder"));
            parent.tame(owner);
            partner.tame(owner);
            parent.setPos(start);
            partner.setPos(start.add(3, 0, 0));
            level.addFreshEntity(partner);
            parent.setInLove(null);
            partner.setInLove(null);
            var mate = new FlyingCompanionGoals.Mate(parent);
            helper.assertTrue(mate.canUse(), "Two tamed companions in love should pair");
            mate.start();
            mate.tick();
            helper.assertTrue(parent.getDeltaMovement().distanceTo(new Vec3(0.25, 0, 0)) < 0.00001,
                    "Courtship begins with the original clear-flight impulse");
            for (int tick = 1; tick < 60; tick++) mate.tick();
            helper.assertTrue(parent.isInLove() && partner.isInLove(),
                    "The original three-block boundary is outside breeding distance");

            mate.stop();
            partner.setPos(start.add(2, 0, 0));
            owner.setItemInHand(HAND, new ItemStack(Items.PINK_DYE));
            parent.mobInteract(owner, HAND);
            helper.assertTrue(mate.canUse(), "The same pair can retry from closer range");
            mate.start();
            for (int tick = 0; tick < 59; tick++) mate.tick();
            helper.assertTrue(parent.isInLove() && partner.isInLove(), "Courtship lasts sixty ticks");
            mate.tick();
            var offspring = level.getEntitiesOfClass(TamableAnimal.class, new AABB(start, start).inflate(2),
                    baby -> baby.getType() == type && baby.isBaby());
            helper.assertTrue(offspring.size() == 1 && parent.getAge() == 6000 && partner.getAge() == 6000,
                    "One hatchling is born and both parents receive the original cooldown");
            var baby = offspring.get(0);
            int color = baby instanceof WitchOwl owl ? owl.color() : ((WingedMonkey) baby).color();
            helper.assertTrue(color == 6 && baby.isPersistenceRequired() && !baby.isTame(),
                    "The persistent hatchling inherits the initiating parent's color");
            baby.discard();
            partner.discard();
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void flyingCompanionsFollowAndRejoinAtOriginalDistances(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos origin = helper.absolutePos(new BlockPos(8, 0, 8)).atY(185);
        Vec3 start = Vec3.atBottomCenterOf(origin);
        for (var type : List.of(WitcheryEntities.OWL.get(), WitcheryEntities.WINGED_MONKEY.get())) {
            TamableAnimal pet = type.create(level);
            var owner = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "FlyingFollow"));
            pet.tame(owner);
            pet.setOrderedToSit(false);
            pet.setPos(start);
            owner.setPos(start.add(13.99, 0, 0));
            level.addNewPlayer(owner);
            try {
                var follow = new FlyingCompanionGoals.Follow(pet);
                helper.assertTrue(!follow.canUse(), "The owner has not crossed the follow boundary");
                owner.setPos(start.add(14, 0, 0));
                helper.assertTrue(follow.canUse(), "Follow starts at fourteen blocks");
                follow.start();
                follow.tick();
                helper.assertTrue(pet.getDeltaMovement().distanceTo(new Vec3(0.1, 0.1, 0)) < 0.00001,
                        "The unobstructed owner attracts the original forward and upward impulse");
                for (int tick = 0; tick < 9; tick++) follow.tick();
                helper.assertTrue(pet.getDeltaMovement().distanceTo(new Vec3(0.1, 0.1, 0)) < 0.00001,
                        "The next impulse waits ten ticks");
                follow.tick();
                helper.assertTrue(pet.getDeltaMovement().distanceTo(new Vec3(0.2, 0.2, 0)) < 0.00001,
                        "The next impulse arrives on tick ten");
                owner.setPos(start.add(5, 0, 0));
                helper.assertTrue(!follow.canContinueToUse(), "The owner is already within five blocks");
                follow.stop();

                owner.setPos(start.add(16, 0, 0));
                BlockPos firstLanding = owner.blockPosition().offset(-2, 0, -2);
                level.setBlockAndUpdate(firstLanding.below(), Blocks.STONE.defaultBlockState());
                level.setBlockAndUpdate(owner.blockPosition().offset(-2, -1, -1), Blocks.STONE.defaultBlockState());
                helper.assertTrue(follow.canUse(), "The distant owner is a valid follow target");
                follow.start();
                follow.tick();
                helper.assertTrue(pet.position().distanceTo(Vec3.atBottomCenterOf(firstLanding).add(0, 0.01, 0)) < 0.00001,
                        "Sixteen blocks triggers the first supported diagonal landing cell");
            } finally {
                level.removePlayerImmediately(owner, net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
            }
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE)
    public static void flyingCompanionsPursueStrikeAndPerch(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos origin = helper.absolutePos(new BlockPos(8, 0, 8)).atY(185);
        Vec3 start = Vec3.atBottomCenterOf(origin);
        for (var type : List.of(WitcheryEntities.OWL.get(), WitcheryEntities.WINGED_MONKEY.get())) {
            TamableAnimal pet = type.create(level);
            Pig target = EntityType.PIG.create(level);
            pet.setPos(start);
            target.setPos(start.add(6, 0, 0));
            pet.setTarget(target);
            var defend = new FlyingCompanionGoals.Defend(pet);
            helper.assertTrue(defend.canUse(), "A living target starts air pursuit");
            defend.start();
            defend.tick();
            helper.assertTrue(pet.getDeltaMovement().distanceTo(new Vec3(0.15, 0, 0)) < 0.00001,
                    "An open route gives the original pursuit impulse");
            pet.setDeltaMovement(Vec3.ZERO);
            level.setBlockAndUpdate(origin.east(3), Blocks.STONE.defaultBlockState());
            defend.start();
            defend.tick();
            helper.assertTrue(pet.getDeltaMovement().equals(Vec3.ZERO), "A wall blocks the flight impulse");
            level.removeBlock(origin.east(3), false);
            target.setPos(start.add(1.1, 0, 0));
            defend.tick();
            helper.assertTrue(target.getHealth() == 8, "A strike within reach deals two damage");
            for (int tick = 0; tick < 19; tick++) {
                target.invulnerableTime = 0;
                defend.tick();
            }
            helper.assertTrue(target.getHealth() == 8, "The 20-tick strike interval protects against a second immediate hit");
            target.invulnerableTime = 0;
            defend.tick();
            helper.assertTrue(target.getHealth() == 6, "The next strike waits twenty ticks");

            pet.setDeltaMovement(Vec3.ZERO);
            BlockPos leaves = origin.offset(-4, -1, -4);
            level.setBlockAndUpdate(leaves, Blocks.OAK_LEAVES.defaultBlockState().setValue(LeavesBlock.PERSISTENT, true));
            var land = new FlyingCompanionGoals.Land(pet);
            helper.assertTrue(land.canContinueToUse(), "An airborne companion can look for a perch");
            land.start();
            land.tick();
            helper.assertTrue(pet.getDeltaMovement().x < 0 && pet.getDeltaMovement().y > 0 && pet.getDeltaMovement().z < 0
                            && Math.abs(pet.getDeltaMovement().length() - 0.05) < 0.00001,
                    "The tree canopy takes precedence over a vertical descent");
            pet.setPos(Vec3.atBottomCenterOf(leaves.above()));
            helper.assertTrue(!land.canContinueToUse(), "The companion is landed on the leaf canopy");
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 450)
    public static void taglockMonkeyFetchesItsSubjectAfterReload(GameTestHelper helper) {
        FlightRoute route = runway(helper);
        WingedMonkey monkey = companion(helper, route, WitcheryEntities.WINGED_MONKEY.get());
        Pig pig = stationaryPig(helper, route);
        var taglock = new ItemStack(WitcheryItems.getRequired("taglockkit").get());
        TaglockItem.bindSignature(taglock, pig);
        route.owner().setItemInHand(HAND, taglock);
        monkey.mobInteract(route.owner(), HAND);

        CompoundTag saved = monkey.saveWithoutId(new CompoundTag());
        helper.assertTrue(taglock.isEmpty() && saved.contains("WITCWaypoint") && monkey.isOrderedToSit() && !monkey.hasActiveFlight(),
                "The seated monkey should save its assigned route and consume one Taglock");
        monkey.load(saved);
        monkey.mobInteract(route.owner(), HAND);
        helper.assertTrue(monkey.hasActiveFlight(), "The launched monkey should keep ticking during its flight");
        helper.succeedWhen(() -> {
            helper.assertTrue(monkey.isOrderedToSit() && !monkey.hasActiveFlight()
                            && monkey.distanceToSqr(Vec3.atBottomCenterOf(route.origin())) < 2.25,
                    "The monkey should return to its launch point and sit");
            helper.assertTrue(!pig.isPassenger() && pig.distanceToSqr(Vec3.atBottomCenterOf(route.origin())) < 3,
                    "The same pig should be released at the launch point");
            helper.assertTrue(pig.getHealth() == pig.getMaxHealth(), "The pig should arrive unharmed");
        });
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 320)
    public static void waystoneMonkeyCarriesItsOwner(GameTestHelper helper) {
        FlightRoute route = runway(helper);
        WingedMonkey monkey = companion(helper, route, WitcheryEntities.WINGED_MONKEY.get());
        ItemStack stone = locationStone(helper, route);
        route.owner().setItemInHand(HAND, stone);
        monkey.mobInteract(route.owner(), HAND);
        helper.assertTrue(route.owner().getVehicle() == monkey, "The owner should mount the monkey on using a Waystone");

        helper.succeedWhen(() -> {
            Vec3 destination = Vec3.atBottomCenterOf(route.destination());
            helper.assertTrue(monkey.isOrderedToSit() && monkey.distanceToSqr(destination) < 8,
                    "The monkey should land near the Waystone destination");
            helper.assertTrue(!route.owner().isPassenger() && route.owner().distanceToSqr(destination) < 8,
                    "The owner should arrive and dismount");
            helper.assertTrue(stone.getCount() == 1, "The owner should keep the Waystone");
        });
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 450)
    public static void owlDeliversAndReturnsWithAnOlderWaystone(GameTestHelper helper) {
        FlightRoute route = runway(helper);
        WitchOwl owl = companion(helper, route, WitcheryEntities.OWL.get());
        route.owner().setItemInHand(HAND, new ItemStack(Items.PAPER).setHoverName(Component.literal("Waystone Letter")));
        owl.mobInteract(route.owner(), HAND);
        route.owner().setItemInHand(HAND, locationStone(helper, route));
        owl.mobInteract(route.owner(), HAND);

        CompoundTag saved = owl.saveWithoutId(new CompoundTag());
        CompoundTag waypoint = saved.getCompound("WITCWaypoint");
        waypoint.putString("id", "witchery:ingredient");
        waypoint.putShort("Damage", (short) 13);
        waypoint.getCompound("tag").remove("Dimension");
        owl.load(saved);
        helper.assertTrue(!owl.hasActiveDelivery(), "The saved route should wait while the owl is told to sit");
        launchOwl(route, owl);
        helper.assertTrue(owl.hasActiveDelivery(), "The launched owl should keep ticking during its delivery");

        helper.succeedWhen(() -> {
            var letters = helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(route.destination()).inflate(2),
                    drop -> drop.getItem().is(Items.PAPER) && drop.getItem().getHoverName().getString().equals("Waystone Letter"));
            helper.assertTrue(letters.size() == 1, "The owl should deliver the original named parcel once");
            helper.assertTrue(owl.getMainHandItem().isEmpty() && owl.isOrderedToSit() && !owl.hasActiveDelivery()
                            && owl.distanceToSqr(Vec3.atBottomCenterOf(route.origin())) < 2.25,
                    "The owl should return to its launch point without the parcel");
        });
    }

    @GameTest(template = TEMPLATE, timeoutTicks = 320)
    public static void owlThrowsInkAtItsBoundCreature(GameTestHelper helper) {
        FlightRoute route = runway(helper);
        WitchOwl owl = companion(helper, route, WitcheryEntities.OWL.get());
        Pig pig = stationaryPig(helper, route);
        route.owner().setItemInHand(HAND, new ItemStack(WitcheryIngredient.BREW_OF_INK.item()));
        owl.mobInteract(route.owner(), HAND);
        var stone = new ItemStack(WitcheryIngredient.CREATURE_BOUND_WAYSTONE.item());
        TaglockItem.bindSignature(stone, pig);
        route.owner().setItemInHand(HAND, stone);
        owl.mobInteract(route.owner(), HAND);
        launchOwl(route, owl);

        helper.succeedWhen(() -> helper.assertTrue(pig.hasEffect(MobEffects.BLINDNESS) && owl.getMainHandItem().isEmpty(),
                "The owl should throw its Ink at the bound pig"));
    }

    @GameTest(template = TEMPLATE, batch = "witchery_owl_undeliverable_parcels", timeoutTicks = 160)
    public static void owlsDropTheSavedParcelsAtHomeWhenTheRecipientIsMissing(GameTestHelper helper) {
        FlightRoute route = runway(helper);
        var stone = new ItemStack(WitcheryIngredient.CREATURE_BOUND_WAYSTONE.item());
        var absentPig = EntityType.PIG.create(helper.getLevel());
        TaglockItem.bindSignature(stone, absentPig);
        BlockPos mindrakeHome = route.origin();
        BlockPos mandrakeHome = mindrakeHome.east(6);
        WitchOwl mindrakeOwl = companion(helper, route, WitcheryEntities.OWL.get());
        WitchOwl mandrakeOwl = companion(helper, route, WitcheryEntities.OWL.get());
        mandrakeOwl.setPos(Vec3.atBottomCenterOf(mandrakeHome));

        for (WitchOwl owl : List.of(mindrakeOwl, mandrakeOwl)) {
            var crop = owl == mindrakeOwl ? WitchCrop.MINDRAKE : WitchCrop.MANDRAKE;
            route.owner().setItemInHand(HAND, new ItemStack(crop.seed()));
            owl.mobInteract(route.owner(), HAND);
            route.owner().setItemInHand(HAND, stone.copy());
            owl.mobInteract(route.owner(), HAND);
            CompoundTag saved = owl.saveWithoutId(new CompoundTag());
            helper.assertTrue(saved.contains("WITCWaypoint"), "The unavailable recipient should still give the owl a saved route");
            owl.load(saved);
            launchOwl(route, owl);
        }

        helper.succeedWhen(() -> {
            var mindrake = helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(mindrakeHome).inflate(2),
                    drop -> drop.getItem().is(WitchCrop.MINDRAKE.seed()));
            var mandrake = helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(mandrakeHome).inflate(2),
                    drop -> drop.getItem().is(WitchCrop.MANDRAKE.seed()));
            helper.assertTrue(mindrake.size() == 1 && mindrake.get(0).lifespan == 60,
                    "One Mindrake parcel should fall at home and expire after three seconds");
            helper.assertTrue(mandrake.size() == 1 && mandrake.get(0).lifespan == 6000,
                    "One Mandrake parcel should fall at home with the ordinary item lifetime");
            helper.assertTrue(mindrakeOwl.isOrderedToSit() && mandrakeOwl.isOrderedToSit()
                            && mindrakeOwl.getMainHandItem().isEmpty() && mandrakeOwl.getMainHandItem().isEmpty(),
                    "Both owls should finish their routes without the parcels");
        });
    }

    @GameTest(template = TEMPLATE, batch = "witchery_owl_custom_brews", timeoutTicks = 450)
    public static void owlThrowsSavedCauldronSplashAndReturns(GameTestHelper helper) {
        FlightRoute route = runway(helper);
        WitchOwl owl = companion(helper, route, WitcheryEntities.OWL.get());
        Pig pig = stationaryPig(helper, route);
        loadOwlRoute(helper, route, owl, brewedBottle(List.of(new ItemStack(Items.NETHER_WART),
                new ItemStack(Items.GUNPOWDER), new ItemStack(Items.SPIDER_EYE))));
        helper.succeedWhen(() -> {
            helper.assertTrue(pig.hasEffect(MobEffects.POISON), "The splash bottle should poison the pig on impact");
            helper.assertTrue(owl.getMainHandItem().isEmpty() && owl.isOrderedToSit()
                            && owl.distanceToSqr(Vec3.atBottomCenterOf(route.origin())) < 2.25,
                    "The owl should return to its saved launch point without the bottle");
        });
    }

    @GameTest(template = TEMPLATE, batch = "witchery_owl_brew_parcel", timeoutTicks = 450)
    public static void owlDeliversDrinkableCauldronBrewIntact(GameTestHelper helper) {
        FlightRoute route = runway(helper);
        WitchOwl owl = companion(helper, route, WitcheryEntities.OWL.get());
        Pig pig = stationaryPig(helper, route);
        ItemStack parcel = brewedBottle(List.of(new ItemStack(Items.NETHER_WART), new ItemStack(Items.SPIDER_EYE)));
        ItemStack expected = parcel.copy();
        loadOwlRoute(helper, route, owl, parcel);
        helper.succeedWhen(() -> {
            var bottles = helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(route.destination()).inflate(2),
                    drop -> ItemStack.matches(drop.getItem(), expected));
            helper.assertTrue(bottles.size() == 1 && !pig.hasEffect(MobEffects.POISON),
                    "The drinkable brew should land as one intact bottle without splashing");
        });
    }

    private static ItemStack brewedBottle(List<ItemStack> ingredients) {
        ItemStack bottle = WitcheryItems.getRequired("brewbottle").get().getDefaultInstance();
        bottle.setTag(BrewMixture.from(ingredients).bottleTag(ingredients));
        return bottle;
    }

    private static void loadOwlRoute(GameTestHelper helper, FlightRoute route, WitchOwl owl, ItemStack parcel) {
        route.owner().setItemInHand(HAND, parcel);
        owl.mobInteract(route.owner(), HAND);
        route.owner().setItemInHand(HAND, locationStone(helper, route));
        owl.mobInteract(route.owner(), HAND);
        owl.load(owl.saveWithoutId(new CompoundTag()));
        launchOwl(route, owl);
    }

    private static FlightRoute runway(GameTestHelper helper) {
        BlockPos origin = helper.absolutePos(new BlockPos(3, 2, 4)).atY(220);
        BlockPos destination = origin.east(12);
        for (BlockPos pos : BlockPos.betweenClosed(origin.offset(-2, -1, -3), destination.offset(2, -1, 3))) {
            helper.getLevel().setBlockAndUpdate(pos, Blocks.STONE.defaultBlockState());
        }
        var owner = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "FlightOwner"));
        owner.getAbilities().instabuild = false;
        owner.setPos(Vec3.atBottomCenterOf(origin.west()));
        return new FlightRoute(origin, destination, owner);
    }

    private static <T extends TamableAnimal> T companion(GameTestHelper helper, FlightRoute route, EntityType<T> type) {
        T companion = type.create(helper.getLevel());
        companion.setPos(Vec3.atBottomCenterOf(route.origin()));
        companion.tame(route.owner());
        companion.setOrderedToSit(true);
        helper.getLevel().addFreshEntity(companion);
        return companion;
    }

    private static Pig stationaryPig(GameTestHelper helper, FlightRoute route) {
        Pig pig = EntityType.PIG.create(helper.getLevel());
        pig.setNoAi(true);
        pig.setPos(Vec3.atBottomCenterOf(route.destination()));
        helper.getLevel().addFreshEntity(pig);
        return pig;
    }

    private static ItemStack locationStone(GameTestHelper helper, FlightRoute route) {
        var stone = new ItemStack(WitcheryIngredient.BOUND_WAYSTONE.item());
        WaystoneItem.bindLocation(stone, helper.getLevel(), route.destination());
        return stone;
    }

    private static void launchOwl(FlightRoute route, WitchOwl owl) {
        route.owner().setItemInHand(HAND, ItemStack.EMPTY);
        owl.mobInteract(route.owner(), HAND);
    }

    private record FlightRoute(BlockPos origin, BlockPos destination, FakePlayer owner) {}
}
