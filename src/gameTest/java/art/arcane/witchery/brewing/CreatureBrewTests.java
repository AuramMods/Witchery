package art.arcane.witchery.brewing;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.ThrownBrew;
import art.arcane.witchery.item.BrewBottleItem;
import art.arcane.witchery.item.ThrowableBrewItem;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryEffects;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcheryItems;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class CreatureBrewTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_brew_purification")
    public static void taglockPurificationRollsBeforeForgeRemovalHooks(GameTestHelper helper) {
        var level = helper.getLevel();
        var pig = stationaryPig(helper, Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(5, 4, 5))));
        pig.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 800, 1));
        pig.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 800, 1));
        pig.addEffect(new MobEffectInstance(MobEffects.POISON, 800, 1));
        var listener = new Object() {
            @SubscribeEvent
            public void onRemove(MobEffectEvent.Remove event) {
                if (event.getEntity() != pig) return;
                if (event.getEffect() == MobEffects.POISON) event.setCanceled(true);
                level.random.setSeed(0L);
                for (int draw = 0; draw < 7; draw++) level.random.nextDouble();
            }
        };
        MinecraftForge.EVENT_BUS.register(listener);
        try {
            level.random.setSeed(0L);
            BrewStatusActions.ritualCleanse(pig, 0);
            var weakness = pig.getEffect(MobEffects.WEAKNESS);
            var slowness = pig.getEffect(MobEffects.MOVEMENT_SLOWDOWN);
            var poison = pig.getEffect(MobEffects.POISON);
            helper.assertTrue(weakness != null && weakness.getAmplifier() == 2
                            && slowness != null && slowness.getAmplifier() == 2,
                    "Both debuffs use their rolls from before the Forge effect removals");
            helper.assertTrue(poison != null && poison.getAmplifier() == 1,
                    "The Forge veto preserves Poison at its original amplifier");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(listener);
            pig.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_brew_movement")
    public static void frogsTonguePullsFromTheOriginalSplashBounds(GameTestHelper helper) {
        var level = helper.getLevel();
        var origin = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(5, 4, 5)));
        var owner = stationaryPig(helper, origin.add(0, 0, -2));
        var near = stationaryPig(helper, origin.add(1, 0, -3.5));
        var atRadius = stationaryPig(helper, origin.add(0, 0, 4));
        var above = stationaryPig(helper, origin.add(1, 2.5, 0));
        var pigs = List.of(owner, near, atRadius, above);

        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(pigs.stream().allMatch(pig -> level.getEntity(pig.getUUID()) == pig),
                        "All four pigs are tracked by the level"))
                .thenExecute(() -> {
                    var brew = new ThrownBrew(WitcheryEntities.THROWN_BREW.get(), level);
                    brew.setOwner(owner);
                    brew.setPos(origin);
                    var hit = origin.add(0, 0, 1.5);
                    var item = (ThrowableBrewItem) WitcheryIngredient.BREW_OF_FROGS_TONGUE.item();
                    item.impact(brew, new BlockHitResult(hit, Direction.UP, BlockPos.containing(hit).below(), false));
                    assertJump(helper, near, 1, 20, new Vec3(-0.15F, 0.4, 1.5 * 0.15F));
                    helper.assertTrue(List.of(owner, atRadius, above).stream()
                                    .noneMatch(pig -> pig.hasEffect(MobEffects.JUMP)),
                            "The owner, the radius boundary and the pig above the splash remain untouched");
                    pigs.forEach(Pig::discard);
                }).thenSucceed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_brew_movement")
    public static void cursedLeapingAddsLiftFromTheOriginalSplashCenter(GameTestHelper helper) {
        var level = helper.getLevel();
        var origin = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(5, 4, 5)));
        var direct = stationaryPig(helper, origin.add(0, 0, 1.5));
        var glancing = stationaryPig(helper, origin.add(0, 0, -4));
        var atRadius = stationaryPig(helper, origin.add(0, 0, 5));
        var pigs = List.of(direct, glancing, atRadius);
        var motion = new Vec3(0.125, 0.25, -0.2);

        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(pigs.stream().allMatch(pig -> level.getEntity(pig.getUUID()) == pig),
                        "All three pigs are tracked by the level"))
                .thenExecute(() -> {
                    var brew = new ThrownBrew(WitcheryEntities.THROWN_BREW.get(), level);
                    brew.setPos(origin);
                    direct.setDeltaMovement(motion);
                    glancing.setDeltaMovement(motion);
                    var item = (ThrowableBrewItem) WitcheryIngredient.BREW_OF_CURSED_LEAPING.item();
                    item.impact(brew, new EntityHitResult(direct));
                    assertJump(helper, direct, 3, 200, motion.add(0, 1.6, 0));
                    assertJump(helper, glancing, 3, 200, motion.add(0, 1.28, 0));
                    helper.assertTrue(!atRadius.hasEffect(MobEffects.JUMP), "The radius boundary is excluded");
                    pigs.forEach(Pig::discard);
                }).thenSucceed();
    }

    private static Pig stationaryPig(GameTestHelper helper, Vec3 pos) {
        var pig = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(5, 4, 5));
        pig.setNoGravity(true);
        pig.setPos(pos);
        return pig;
    }

    private static void assertJump(GameTestHelper helper, Pig pig, int amplifier, int ticks, Vec3 velocity) {
        var jump = pig.getEffect(MobEffects.JUMP);
        helper.assertTrue(jump != null && jump.getAmplifier() == amplifier && jump.getDuration() == ticks,
                "The pig gets Jump Boost with the original level and duration");
        helper.assertTrue(pig.getDeltaMovement().distanceToSqr(velocity) < 1.0E-12,
                "The pig gets the original motion vector");
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_cauldron_splash")
    public static void instantBlockSplashPullsTargetsAtTheOriginalBoundary(GameTestHelper helper) {
        var level = helper.getLevel();
        var origin = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(5, 4, 5)));
        var atRadius = stationaryPig(helper, origin.add(0, 0, -3));
        var outside = stationaryPig(helper, origin.add(0, 0, 3.25));
        var drop = stationaryItem(helper, origin.add(3, 0, 0));
        var outsideDrop = stationaryItem(helper, origin.add(0, 0, 3.25));
        List<Entity> targets = List.of(atRadius, outside, drop, outsideDrop);

        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(targets.stream().allMatch(target -> level.getEntity(target.getUUID()) == target),
                        "Both pigs and both dropped items are tracked by the level"))
                .thenExecute(() -> {
                    var hit = origin.add(0, 0, 0.5);
                    splash(helper, origin, new BlockHitResult(hit, Direction.NORTH, BlockPos.containing(hit), false),
                            Items.SLIME_BALL);
                    assertJump(helper, atRadius, 1, 20, new Vec3(0, 0.4, 3 * 0.15F));
                    helper.assertTrue(drop.getDeltaMovement().distanceToSqr(new Vec3(-3 * 0.15F, 0.4, 0)) < 1.0E-12,
                            "A block impact pulls loose items from the same inclusive sphere");
                    helper.assertTrue(!outside.hasEffect(MobEffects.JUMP) && outsideDrop.getDeltaMovement().equals(Vec3.ZERO),
                            "The targets outside the original radius stay untouched");
                    targets.forEach(Entity::discard);
                }).thenSucceed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_cauldron_splash")
    public static void instantEntitySplashScalesFromTheProjectile(GameTestHelper helper) {
        var level = helper.getLevel();
        var origin = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(5, 4, 5)));
        var direct = stationaryPig(helper, origin.add(0, 0, 0.5));
        var glancing = stationaryPig(helper, origin.add(0, 0, -1.5));
        var extended = stationaryPig(helper, origin.add(3.5, 0, 0));
        var outside = stationaryPig(helper, origin.add(0, 0, 4.25));
        var pigs = List.of(direct, glancing, extended, outside);

        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(pigs.stream().allMatch(pig -> level.getEntity(pig.getUUID()) == pig),
                        "All four pigs are tracked by the level"))
                .thenExecute(() -> {
                    var hit = new EntityHitResult(direct);
                    splash(helper, origin, hit, Items.SPIDER_EYE, WitcheryIngredient.WOOD_ASH.item());
                    helper.assertTrue(direct.getEffect(MobEffects.POISON) != null
                                    && direct.getEffect(MobEffects.POISON).getDuration() == 450,
                            "The directly struck pig receives the full splash duration");
                    helper.assertTrue(glancing.getEffect(MobEffects.POISON) != null
                                    && glancing.getEffect(MobEffects.POISON).getDuration() == 282,
                            "The glancing pig's exposure is measured from the projectile");
                    helper.assertTrue(extended.getEffect(MobEffects.POISON) != null
                                    && extended.getEffect(MobEffects.POISON).getDuration() == 57,
                            "Wood Ash extends the splash to its original four-block radius");
                    splash(helper, origin, new EntityHitResult(outside), Items.SPIDER_EYE, WitcheryIngredient.WOOD_ASH.item());
                    helper.assertTrue(!outside.hasEffect(MobEffects.POISON), "A direct hit still requires the target within the sphere");

                    splash(helper, origin, hit, Items.GLOWSTONE_DUST, Items.STICK);
                    helper.assertTrue(direct.getDeltaMovement().distanceToSqr(new Vec3(0, 0.7, 2)) < 1.0E-12
                                    && glancing.getDeltaMovement().distanceToSqr(new Vec3(0, 0.7, -1.5)) < 1.0E-12,
                            "Boosted Knockback uses the original direct and glancing push vectors");
                    pigs.forEach(Pig::discard);
                }).thenSucceed();
    }

    private static ItemEntity stationaryItem(GameTestHelper helper, Vec3 pos) {
        var level = helper.getLevel();
        var item = new ItemEntity(level, pos.x, pos.y, pos.z, new ItemStack(Items.STICK), 0, 0, 0);
        item.setNoGravity(true);
        level.addFreshEntity(item);
        return item;
    }

    private static void splash(GameTestHelper helper, Vec3 origin, HitResult hit, ItemLike... ingredients) {
        var recipe = new ArrayList<>(List.of(new ItemStack(Items.NETHER_WART), new ItemStack(Items.GUNPOWDER)));
        Arrays.stream(ingredients).map(ItemStack::new).forEach(recipe::add);
        var item = (BrewBottleItem) WitcheryItems.getRequired("brewbottle").get();
        var stack = item.getDefaultInstance();
        stack.setTag(BrewMixture.ingredientTag(recipe, 0x008000));
        var projectile = new ThrownBrew(WitcheryEntities.THROWN_BREW.get(), helper.getLevel());
        projectile.setItem(stack);
        projectile.setPos(origin);
        item.impact(helper.getLevel(), projectile, hit);
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_brew_summon_cues")
    public static void batBrewsUseTheOriginalReleaseCornerAndSlownessOrigin(GameTestHelper helper) {
        var level = helper.getLevel();
        var floor = new BlockPos(5, 4, 5);
        helper.setBlock(floor, Blocks.STONE);
        var hit = helper.absolutePos(floor);
        var release = hit.above();
        var hitPoint = Vec3.atBottomCenterOf(release);
        var position = hitPoint.add(0, 0.01, -1.5);
        var near = helper.spawnWithNoFreeWill(EntityType.PIG, floor.east(2).above());
        var outside = helper.spawnWithNoFreeWill(EntityType.PIG, floor.south(2).above());
        near.setPos(position.add(2, 0, 0));
        outside.setPos(position.add(0, 0, 3.4));
        near.setNoGravity(true);
        outside.setNoGravity(true);
        var captures = new ArrayList<Bat>();
        var listener = new Object() {
            @SubscribeEvent
            public void batJoined(EntityJoinLevelEvent event) {
                if (event.getLevel() == level && event.getEntity() instanceof Bat bat
                        && bat.blockPosition().distManhattan(release) < 2) captures.add(bat);
            }
        };
        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(level.getEntity(near.getUUID()) == near
                        && level.getEntity(outside.getUUID()) == outside, "Both pigs are loaded for the splash"))
                .thenExecute(() -> {
                    MinecraftForge.EVENT_BUS.register(listener);
                    try {
                        var bottle = new ThrownBrew(WitcheryEntities.THROWN_BREW.get(), level);
                        bottle.setItem(WitcheryIngredient.BREW_OF_BATS.item().getDefaultInstance());
                        bottle.setPos(position);
                        BrewCreatureEffects.bats(bottle, new BlockHitResult(hitPoint, Direction.UP, hit, false));
                        verifyBatRelease(helper, captures, release);
                        var slowed = near.getEffect(MobEffects.MOVEMENT_SLOWDOWN);
                        helper.assertTrue(slowed != null && slowed.getAmplifier() == 5 && slowed.getDuration() == 33
                                        && !outside.hasEffect(MobEffects.MOVEMENT_SLOWDOWN),
                                "The fixed brew measures slowness from the projectile's position before impact");

                        captures.forEach(Bat::discard);
                        captures.clear();
                        var mixture = BrewMixture.from(List.of(Items.NETHER_WART.getDefaultInstance(),
                                WitcheryIngredient.BAT_BALL.item().getDefaultInstance()));
                        mixture.applySplashToBlock(level, hit, Direction.UP, hitPoint, null);
                        verifyBatRelease(helper, captures, release);
                    } finally {
                        MinecraftForge.EVENT_BUS.unregister(listener);
                        captures.forEach(Bat::discard);
                    }
                }).thenSucceed();
    }

    private static void verifyBatRelease(GameTestHelper helper, List<Bat> bats, BlockPos release) {
        helper.assertTrue(bats.size() == 10 && bats.stream().allMatch(bat -> bat.position().equals(Vec3.atLowerCornerOf(release))
                        && bat.isResting() && bat.getPersistentData().getBoolean("WITCNoDrops")),
                "Ten bats spawn at the original block corner with vanilla rest and no drops");
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_fear_brew")
    public static void infernalBloodFearRepelsWitchesAndSparesGolems(GameTestHelper helper) {
        var level = helper.getLevel();
        var carrier = helper.spawnWithNoFreeWill(EntityType.PIG, new BlockPos(6, 3, 6));
        var witch = helper.spawnWithNoFreeWill(EntityType.WITCH, new BlockPos(8, 3, 6));
        var golem = helper.spawnWithNoFreeWill(EntityType.IRON_GOLEM, new BlockPos(6, 3, 9));
        var distantCow = helper.spawnWithNoFreeWill(EntityType.COW, new BlockPos(11, 3, 6));
        var creatures = List.of(carrier, witch, golem, distantCow);
        double witchX = witch.getX();
        Vec3 golemStart = golem.position();
        Vec3 distantCowStart = distantCow.position();

        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(creatures.stream().allMatch(mob -> level.getEntity(mob.getUUID()) == mob),
                        "All four creatures are tracked by the level"))
                .thenExecute(() -> {
                    var mixture = BrewMixture.from(List.of(Items.NETHER_WART.getDefaultInstance(),
                            WitcheryIngredient.INFERNAL_BLOOD.item().getDefaultInstance()));
                    mixture.apply(carrier, null, null, 1, 1);
                    helper.assertTrue(carrier.hasEffect(WitcheryEffects.GROTESQUE.get()), "Infernal Blood applies Fear to a creature");
                })
                .thenWaitUntil(() -> helper.assertTrue(witch.getX() > witchX + 0.5, "Fear pushes the nearby witch away"))
                .thenExecute(() -> {
                    helper.assertTrue(golem.position().subtract(golemStart).horizontalDistanceSqr() < 1.0E-6
                                    && distantCow.position().subtract(distantCowStart).horizontalDistanceSqr() < 1.0E-6,
                            "Fear spares nearby golems and creatures outside its four-block radius");
                    creatures.forEach(mob -> mob.discard());
                }).thenSucceed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_heart_brew")
    public static void heartOfGoldPreservesOwnedPetsAndFamiliars(GameTestHelper helper) {
        var level = helper.getLevel();
        var actor = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "BrewOwner"));
        actor.setPos(Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(5, 3, 5))));
        var wild = helper.spawn(EntityType.WOLF, new BlockPos(3, 3, 5));
        var foreign = helper.spawn(EntityType.WOLF, new BlockPos(4, 3, 5));
        var familiar = helper.spawn(WitcheryEntities.TOAD.get(), new BlockPos(7, 3, 5));
        var other = UUID.randomUUID();
        for (var pet : List.of(foreign, familiar)) {
            pet.setOwnerUUID(other);
            pet.setTame(true);
        }
        familiar.setFamiliar(true);
        var pets = List.of(wild, foreign, familiar);
        pets.forEach(pet -> pet.setNoAi(true));

        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(pets.stream().allMatch(pet -> level.getEntity(pet.getUUID()) == pet),
                        "All three pets must be visible to the brew"))
                .thenExecute(() -> {
                    try {
                        var capacity = Items.NETHER_STAR.getDefaultInstance();
                        var heart = WitcheryIngredient.HEART_OF_GOLD.item().getDefaultInstance();
                        var ingredients = List.of(capacity, heart);
                        var attraction = BrewMixture.from(ingredients);
                        helper.assertTrue(attraction.used() == 4 && attraction.power() == 650,
                                "Heart of Gold costs four levels and 500 power on top of its capacity ingredient");
                        BrewMixture.from(attraction.bottleTag(ingredients)).apply(actor, actor, actor, 1, 1);
                        helper.assertTrue(actor.getUUID().equals(wild.getOwnerUUID()) && other.equals(foreign.getOwnerUUID()),
                                "Attraction tames a wild wolf and preserves an existing owner");
                        var repulsion = BrewMixture.from(List.of(capacity, Items.FERMENTED_SPIDER_EYE.getDefaultInstance(), heart));
                        repulsion.apply(actor, actor, actor, 1, 1);
                        helper.assertTrue(!foreign.isTame() && foreign.getOwnerUUID() == null && actor.getUUID().equals(wild.getOwnerUUID())
                                        && familiar.isFamiliar() && other.equals(familiar.getOwnerUUID()),
                                "Repulsion releases foreign pets and preserves the actor's pet and the bound familiar");
                    } finally {
                        pets.forEach(pet -> pet.discard());
                    }
                }).thenSucceed();
    }

    @GameTest(template = "qa_rite_terrain", batch = "witchery_animal_repulsion_landing", setupTicks = 5)
    public static void animalRepulsionUsesTheLowerPocketInItsOriginalRandomColumn(GameTestHelper helper) {
        var level = helper.getLevel();
        var origin = helper.absolutePos(new BlockPos(6, 4, 6)).atY(250);
        var actor = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "BrewRepulsion"));
        actor.setPos(Vec3.atBottomCenterOf(origin));
        var cow = EntityType.COW.create(level);
        var start = Vec3.atBottomCenterOf(origin.east(2));
        cow.setPos(start);
        cow.setNoGravity(true);
        cow.setNoAi(true);
        helper.assertTrue(level.addFreshEntity(cow), "Place an airborne cow whose ground navigation cannot start");

        var column = origin.offset(6, 0, 9);
        var originalBlocks = new LinkedHashMap<BlockPos, BlockState>();
        for (int y = 54; y <= 80; y++) {
            var pos = column.atY(y);
            originalBlocks.put(pos, level.getBlockState(pos));
        }
        var repulsion = BrewMixture.from(List.of(Items.NETHER_STAR.getDefaultInstance(),
                Items.FERMENTED_SPIDER_EYE.getDefaultInstance(), WitcheryIngredient.HEART_OF_GOLD.item().getDefaultInstance()));

        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(level.getEntity(cow.getUUID()) == cow, "Track the cow"))
                .thenExecute(() -> {
                    try {
                        originalBlocks.keySet().forEach(pos -> level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState()));
                        level.setBlockAndUpdate(column.atY(61), Blocks.STONE.defaultBlockState());
                        level.setBlockAndUpdate(column.atY(80), Blocks.STONE.defaultBlockState());
                        cow.setOnGround(false);
                        level.getRandom().setSeed(117);
                        repulsion.apply(actor, actor, actor, 1, 1);
                        helper.assertTrue(cow.position().equals(Vec3.atBottomCenterOf(column.atY(62))),
                                "The cow lands in the first clear pocket under the roof at " + column.atY(62)
                                        + ", actual " + cow.position());
                        var expected = RandomSource.create(117);
                        expected.nextBoolean();
                        expected.nextInt(6);
                        expected.nextBoolean();
                        expected.nextInt(6);
                        helper.assertTrue(level.getRandom().nextInt() == expected.nextInt(),
                                "Selecting a column spends the original four random draws");

                        cow.setPos(start);
                        cow.setOnGround(false);
                        for (int y = 62; y <= 70; y++) level.setBlockAndUpdate(column.atY(y), Blocks.STONE.defaultBlockState());
                        level.getRandom().setSeed(117);
                        repulsion.apply(actor, actor, actor, 1, 1);
                        helper.assertTrue(cow.position().equals(Vec3.atBottomCenterOf(column.atY(71))),
                                "A solid band ends at the first open space with room for the cow above it");

                        cow.setPos(start);
                        cow.setOnGround(false);
                        for (int y = 55; y <= 70; y++) level.setBlockAndUpdate(column.atY(y), Blocks.AIR.defaultBlockState());
                        level.setBlockAndUpdate(column.atY(54), Blocks.STONE.defaultBlockState());
                        level.getRandom().setSeed(117);
                        repulsion.apply(actor, actor, actor, 1, 1);
                        helper.assertTrue(cow.position().equals(Vec3.atBottomCenterOf(column.atY(55))),
                                "An open column uses its actual lower floor in 1.20.1");
                    } finally {
                        cow.discard();
                        actor.discard();
                        originalBlocks.forEach(level::setBlockAndUpdate);
                    }
                }).thenSucceed();
    }
}
