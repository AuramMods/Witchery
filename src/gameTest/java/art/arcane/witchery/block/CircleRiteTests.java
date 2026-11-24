package art.arcane.witchery.block;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.entity.AltarBlockEntity;
import art.arcane.witchery.block.entity.BarrierBlockEntity;
import art.arcane.witchery.block.entity.GrassperBlockEntity;
import art.arcane.witchery.block.entity.RiteHeartBlockEntity;
import art.arcane.witchery.item.WaystoneItem;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryItems;
import art.arcane.witchery.ritual.ItemRiteRecipe;
import art.arcane.witchery.ritual.RitualCircle;
import art.arcane.witchery.ritual.WeatherRites;
import art.arcane.witchery.world.WitcheryDimensions;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestSequence;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.PlayLevelSoundEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class CircleRiteTests {
    private static final String TEMPLATE = "qa_rite_terrain";
    private static final BlockPos CENTER = new BlockPos(15, 245, 15);
    // Common cast lengths depend on which optional offering and altar steps the rite uses.
    private static final int TWO_OFFERING_CAST = 80;
    private static final int THREE_OFFERING_CAST = 100;
    private static final int ALTAR_STEP_TICKS = 20;

    @GameTest(template = TEMPLATE, batch = "witchery_rite_redstone_pulse", setupTicks = 5)
    public static void shortRedstonePulsesStartAndStopARite(GameTestHelper helper) {
        floor(helper);
        ring(helper, RitualCircle.SMALL_RITE, "circleglyphritual");
        var level = helper.getLevel();
        var center = helper.absolutePos(CENTER);
        var heart = heart(helper);
        var power = CENTER.below();
        var sounds = new ArrayList<SoundEvent>();
        Consumer<PlayLevelSoundEvent.AtPosition> listener = event -> {
            if (event.getLevel() == level && event.getSound() != null
                    && event.getPosition().distanceToSqr(Vec3.atCenterOf(center)) < 1) {
                sounds.add(event.getSound().value());
            }
        };
        waitForOfferings(helper, new ItemStack(Items.OBSIDIAN), new ItemStack(Items.REDSTONE)).thenExecute(() -> {
            MinecraftForge.EVENT_BUS.addListener(listener);
            try {
                helper.setBlock(power, Blocks.REDSTONE_BLOCK);
                helper.setBlock(power, Blocks.STONE);
                var started = helper.getBlockState(CENTER);
                helper.assertTrue(started.getValue(RiteHeartBlock.ACTIVE) && !started.getValue(RiteHeartBlock.POWERED)
                                && sounds.isEmpty(),
                        "The rising edge starts the rite before the same-tick falling edge and the first offering sound");

                tick(level, center, heart, 20);
                helper.assertTrue(heart.saveWithoutMetadata().getInt("OfferingStep") == 1
                                && sounds.equals(List.of(SoundEvents.BUBBLE_COLUMN_BUBBLE_POP)),
                        "The staged item plays the offering pop at its own step");

                helper.setBlock(power, Blocks.REDSTONE_BLOCK);
                helper.setBlock(power, Blocks.STONE);
                var stopped = helper.getBlockState(CENTER);
                helper.assertTrue(!stopped.getValue(RiteHeartBlock.ACTIVE) && !stopped.getValue(RiteHeartBlock.POWERED)
                                && sounds.equals(List.of(SoundEvents.BUBBLE_COLUMN_BUBBLE_POP, SoundEvents.NOTE_BLOCK_SNARE.value()))
                                && drops(helper).size() == 2,
                        "The second pulse cancels and refunds the staged offering with the original snare cue");
            } finally {
                MinecraftForge.EVENT_BUS.unregister(listener);
            }
        }).thenSucceed();
    }

    @GameTest(template = TEMPLATE, batch = "witchery_rite_offering_boundary", setupTicks = 5)
    public static void smallRiteMeasuresDroppedOfferingsFromTheGlyphBlockCorner(GameTestHelper helper) {
        floor(helper);
        ring(helper, RitualCircle.SMALL_RITE, "circleglyphritual");
        var level = helper.getLevel();
        var center = helper.absolutePos(CENTER);
        var origin = Vec3.atLowerCornerOf(center);
        var obsidian = drop(level, origin.add(4.25, 0.2, 0.2), new ItemStack(Items.OBSIDIAN));
        waitForOfferings(helper, new ItemStack(Items.REDSTONE)).thenWaitUntil(() -> {
            helper.assertTrue(level.getEntitiesOfClass(ItemEntity.class, new AABB(center).inflate(5)).contains(obsidian),
                    "The obsidian exists before checking the offering boundary");
        }).thenExecute(() -> {
            helper.useBlock(CENTER);
            helper.assertFalse(helper.getBlockState(CENTER).getValue(RiteHeartBlock.ACTIVE),
                    "An offering beyond four blocks on the east side cannot start the small rite");

            obsidian.setPos(origin.add(-3.75, 0.2, 0.2));
            start(helper);
            tick(level, center, heart(helper), 20);
            helper.assertTrue(obsidian.isRemoved(), "An offering within four blocks on the west side starts and feeds the rite");
        }).thenSucceed();
    }

    @GameTest(template = TEMPLATE, batch = "witchery_rite_offering_step", setupTicks = 5)
    public static void riteUsesTheWesternGrassperAndWaitsUntilTheRedstoneStep(GameTestHelper helper) {
        floor(helper);
        ring(helper, RitualCircle.SMALL_RITE, "circleglyphritual");
        var level = helper.getLevel();
        var center = helper.absolutePos(CENTER);
        var west = filledGrassper(helper, CENTER.offset(-5, 0, 5));
        var east = filledGrassper(helper, CENTER.offset(5, 0, -5));
        var position = Vec3.atBottomCenterOf(center).add(0, 0.2, 0);
        var redstone = drop(level, position, new ItemStack(Items.REDSTONE));
        helper.startSequence().thenWaitUntil(() -> {
            helper.assertTrue(drops(helper).contains(redstone), "The Redstone is present for initial selection");
        }).thenExecute(() -> {
            start(helper);
            redstone.setPos(position.add(7, 0, 0));
            var heart = heart(helper);
            tick(level, center, heart, 20);
            helper.assertTrue(west.isEmpty() && !east.isEmpty() && heart.saveWithoutMetadata().getInt("OfferingStep") == 1,
                    "The first step takes obsidian from the western Grassper while the later item is absent");

            heart.load(heart.saveWithoutMetadata());
            redstone.setPos(position);
            tick(level, center, heart, 20);
            helper.assertTrue(redstone.isRemoved() && heart.saveWithoutMetadata().getInt("OfferingStep") == 2,
                    "Replacing the Redstone before its own step lets the restored cast continue");
        }).thenSucceed();
    }

    @GameTest(template = TEMPLATE, batch = "witchery_rite_living_boundary", setupTicks = 5)
    public static void graveFindsZombieVillagersOnTheGlyphLayer(GameTestHelper helper) {
        floor(helper);
        var level = helper.getLevel();
        var center = helper.absolutePos(CENTER);
        var above = EntityType.ZOMBIE.create(level);
        above.moveTo(Vec3.atBottomCenterOf(center).add(0, 1.3, 0));
        above.setNoAi(true);
        above.setNoGravity(true);
        level.addFreshEntity(above);
        var villager = EntityType.ZOMBIE_VILLAGER.create(level);
        villager.moveTo(Vec3.atLowerCornerOf(center).add(-5.8, 0.2, 0.2));
        villager.setNoAi(true);
        villager.setNoGravity(true);
        level.addFreshEntity(villager);
        var grave = (ItemRiteRecipe) level.getRecipeManager().byKey(ResourceLocation.parse("witchery:rite_of_infusing_grave")).orElseThrow();
        helper.startSequence().thenWaitUntil(() -> {
            var nearby = level.getEntitiesOfClass(Entity.class, new AABB(center).inflate(7));
            helper.assertTrue(nearby.contains(above) && nearby.contains(villager), "Both living candidates are tracked");
        }).thenExecute(() -> {
            try {
                helper.assertTrue(grave.findLivingOffering(level, center) == villager,
                        "The Grave accepts an in-range zombie villager and ignores a zombie above the glyph layer");
            } finally {
                above.discard();
                villager.discard();
            }
        }).thenSucceed();
    }

    @GameTest(template = TEMPLATE, setupTicks = 5)
    public static void poweredWaystoneBindsAfterTheSavedAltarOffering(GameTestHelper helper) {
        floor(helper);
        ring(helper, RitualCircle.SMALL_RITE, "circleglyphritual");
        var level = helper.getLevel();
        var center = helper.absolutePos(CENTER);
        var altar = chargedAltar(helper, false);
        waitForOfferings(helper, new ItemStack(WitcheryIngredient.WAYSTONE.item()),
                new ItemStack(WitcheryIngredient.ENDER_DEW.item()), new ItemStack(Items.GLOWSTONE_DUST)).thenExecute(() -> {
            var heart = heart(helper);
            start(helper);
            tick(level, center, heart, 79);
            helper.assertTrue(altar.power() == 10_000 && drops(helper).isEmpty(),
                    "All three items are held before the altar pulse");
            tick(level, center, heart, 1);
            var paid = heart.saveWithoutMetadata();
            helper.assertTrue(altar.power() == 9_500 && paid.getBoolean("AltarPowerOffered") && drops(helper).isEmpty(),
                    "The fourth pulse pays the altar and saves the pending binding");

            heart.load(paid);
            level.removeBlock(altar.getBlockPos(), false);
            tick(level, center, heart, 20);
            var results = drops(helper);
            helper.assertTrue(!heart.getBlockState().getValue(RiteHeartBlock.ACTIVE) && results.size() == 1
                            && results.get(0).getItem().is(WitcheryIngredient.BOUND_WAYSTONE.item()),
                    "The resumed binding uses the saved payment and creates one stone even if the altar is gone");
        }).thenSucceed();
    }

    @GameTest(template = TEMPLATE, setupTicks = 5)
    public static void missingFamiliarReturnsOnlyTheItemOfferings(GameTestHelper helper) {
        floor(helper);
        ring(helper, RitualCircle.MEDIUM_RITE, "circleglyphritual");
        var level = helper.getLevel();
        var center = helper.absolutePos(CENTER);
        var altar = chargedAltar(helper, false);
        waitForOfferings(helper, new ItemStack(WitcheryIngredient.BREATH_OF_THE_GODDESS.item()),
                new ItemStack(WitcheryIngredient.HINT_OF_REBIRTH.item()),
                new ItemStack(WitcheryIngredient.WHIFF_OF_MAGIC.item())).thenExecute(() -> {
            var heart = heart(helper);
            start(helper);
            tick(level, center, heart, 80);
            helper.assertTrue(altar.power() == 9_000 && drops(helper).isEmpty(),
                    "Calling a Familiar spends 1,000 before searching for a familiar");
            tick(level, center, heart, 20);
            var returned = drops(helper);
            helper.assertTrue(!heart.getBlockState().getValue(RiteHeartBlock.ACTIVE) && altar.power() == 9_000
                            && returned.size() == 3
                            && returned.stream().anyMatch(item -> item.getItem().is(WitcheryIngredient.BREATH_OF_THE_GODDESS.item()))
                            && returned.stream().anyMatch(item -> item.getItem().is(WitcheryIngredient.HINT_OF_REBIRTH.item()))
                            && returned.stream().anyMatch(item -> item.getItem().is(WitcheryIngredient.WHIFF_OF_MAGIC.item())),
                    "Failure returns the three spent items and keeps the altar debit");
        }).thenSucceed();
    }

    @GameTest(template = TEMPLATE, setupTicks = 5)
    public static void mirrorChargesTheAltarBeforeCheckingForADemon(GameTestHelper helper) {
        floor(helper);
        ring(helper, RitualCircle.MEDIUM_RITE, "circleglyphritual");
        var altar = chargedAltar(helper, false);
        var level = helper.getLevel();
        var center = helper.absolutePos(CENTER);
        var heart = heart(helper);
        waitForOfferings(helper, new ItemStack(WitcheryIngredient.BREW_OF_HOLLOW_TEARS.item()),
                new ItemStack(Items.GOLD_INGOT), new ItemStack(Items.GLASS_PANE)).thenExecute(() -> {
            start(helper);
            tick(level, center, heart, 80);
            var paid = heart.saveWithoutMetadata();
            helper.assertTrue(altar.power() == 8000 && paid.getBoolean("AltarPowerOffered") && drops(helper).isEmpty(),
                    "The Mirror takes 2,000 power and waits one more step for the Demon");

            heart.load(paid);
            tick(level, center, heart, 20);
            helper.assertTrue(!helper.getBlockState(CENTER).getValue(RiteHeartBlock.ACTIVE)
                            && altar.power() == 8000 && drops(helper).size() == 3,
                    "The restarted cast refunds all three items if the Demon is missing and retains the one altar debit");
        }).thenSucceed();
    }

    @GameTest(template = TEMPLATE, setupTicks = 5)
    public static void graveTakesItsZombieBeforeCheckingForPower(GameTestHelper helper) {
        floor(helper);
        ring(helper, RitualCircle.SMALL_RITE, "circleglyphritual");
        ring(helper, RitualCircle.MEDIUM_RITE, "circleglyphritual");
        var level = helper.getLevel();
        var center = helper.absolutePos(CENTER);
        var heart = heart(helper);
        var zombie = EntityType.ZOMBIE.create(level);
        zombie.moveTo(Vec3.atBottomCenterOf(center));
        zombie.setNoAi(true);
        level.addFreshEntity(zombie);
        var stone = new ItemStack(WitcheryIngredient.NECROMANTIC_STONE.item());
        waitForOfferings(helper, new ItemStack(WitcheryIngredient.INFUSED_BREW_BASE.item()),
                new ItemStack(Items.BONE), new ItemStack(WitcheryIngredient.WEB.item()), stone).thenExecute(() -> {
            start(helper);
            tick(level, center, heart, 100);
            var offered = heart.saveWithoutMetadata();
            helper.assertTrue(zombie.isRemoved() && offered.getBoolean("LivingOffered") && drops(helper).size() == 1,
                    "The Grave takes its zombie in a separate step and has returned the reusable stone");

            heart.load(offered);
            tick(level, center, heart, 20);
            helper.assertTrue(!helper.getBlockState(CENTER).getValue(RiteHeartBlock.ACTIVE) && drops(helper).size() == 4,
                    "Without an altar the Grave returns the three held offerings after restart");
        }).thenSucceed();
    }

    @GameTest(template = TEMPLATE, setupTicks = 5)
    public static void olderHellOnEarthCastKeepsItsCompletedVillagerOffering(GameTestHelper helper) {
        floor(helper);
        ring(helper, RitualCircle.SMALL_RITE, "circleglyphinfernal");
        var altar = chargedAltar(helper, false);
        var level = helper.getLevel();
        var center = helper.absolutePos(CENTER);
        var heart = heart(helper);
        var legacy = new CompoundTag();
        legacy.putString("Rite", "witchery:rite_of_hell_on_earth");
        legacy.putInt("OfferingStep", 5);
        heart.load(legacy);
        tick(level, center, heart, 20);
        var paid = heart.saveWithoutMetadata();
        helper.assertTrue(altar.power() == 5000 && paid.getBoolean("AltarPowerOffered"),
                "The saved cast at the post-Waystone step charges the altar without asking for another villager");
        helper.useBlock(CENTER);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, batch = "witchery_hell_spawn_phase")
    public static void hellOnEarthWaitsForItsSavedFortyTickPhase(GameTestHelper helper) {
        floor(helper);
        ring(helper, RitualCircle.SMALL_RITE, "circleglyphinfernal");
        var altar = chargedAltar(helper, false);
        var level = helper.getLevel();
        var center = helper.absolutePos(CENTER);
        var heart = heart(helper);
        var pending = new CompoundTag();
        pending.putString("Rite", "witchery:rite_of_hell_on_earth");
        pending.putInt("OfferingStep", 5);
        pending.putBoolean("AltarPowerOffered", true);
        pending.putInt("GlyphPhase", 19);
        heart.load(pending);
        tick(level, center, heart, 81);
        var expanded = heart.saveWithoutMetadata().getCompound("Landscape");
        helper.assertTrue(expanded.getInt("Stage") == 17 && expanded.getInt("HellSpawnOffset") == 4 && altar.power() == 10_000,
                "When the rite begins on glyph tick twenty, completing expansion at stage seventeen makes no spawn attempt");

        tick(level, center, heart, 3);
        heart.load(heart.saveWithoutMetadata());
        tick(level, center, heart, 16);
        helper.assertTrue(altar.power() == 10_000, "The reloaded wave still waits for its own summoning phase");
        var summoned = new ArrayList<Entity>();
        Consumer<EntityJoinLevelEvent> observe = event -> {
            if (event.getLevel() == level && event.getEntity().position().equals(Vec3.atBottomCenterOf(center.above(2)))) {
                summoned.add(event.getEntity());
            }
        };
        MinecraftForge.EVENT_BUS.addListener(observe);
        try {
            tick(level, center, heart, 1);
            var progress = heart.saveWithoutMetadata().getCompound("Landscape");
            helper.assertTrue(progress.getInt("Stage") == 21 && altar.power() == 9_800 && summoned.size() == 1,
                    "Stage twenty-one pays two hundred and spawns exactly one creature");
            tick(level, center, heart, 39);
            helper.assertTrue(altar.power() == 9_800 && summoned.size() == 1, "The next summon also waits forty ticks");
            tick(level, center, heart, 1);
            helper.assertTrue(altar.power() == 9_600 && summoned.size() == 2, "Forty ticks later the second summon pays once");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(observe);
            summoned.forEach(Entity::discard);
            helper.useBlock(CENTER);
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, batch = "witchery_hell_legacy_spawn_phase")
    public static void originalHellOnEarthSaveReentersItsTwentyTickActivationGate(GameTestHelper helper) {
        floor(helper);
        ring(helper, RitualCircle.SMALL_RITE, "circleglyphinfernal");
        var altar = chargedAltar(helper, false);
        var level = helper.getLevel();
        var center = helper.absolutePos(CENTER);
        var heart = heart(helper);
        var legacy = new CompoundTag();
        legacy.putByteArray("Rituals", new byte[]{39});
        legacy.putByteArray("RitualStages", new byte[]{25});
        heart.load(legacy);
        tick(level, center, heart, 7);
        heart.load(heart.saveWithoutMetadata());
        tick(level, center, heart, 12);
        var waiting = heart.saveWithoutMetadata();
        helper.assertTrue(waiting.getCompound("Landscape").getInt("Stage") == 25 && altar.power() == 10_000,
                "A 1.7.10 wave keeps its stage until the twentieth loaded tick, including on save reload");
        tick(level, center, heart, 1);
        helper.assertTrue(heart.saveWithoutMetadata().getCompound("Landscape").getInt("Stage") == 26 && altar.power() == 10_000,
                "At tick twenty the legacy wave advances without a summon");
        tick(level, center, heart, 19);
        helper.assertTrue(altar.power() == 10_000, "An expanded imported wave still waits for the fortieth loaded tick");
        level.removeBlock(altar.getBlockPos(), false);
        tick(level, center, heart, 1);
        helper.assertFalse(heart.getBlockState().getValue(RiteHeartBlock.ACTIVE), "The wave ends on its first unpaid summon");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, batch = "witchery_weather_rite_eclipse")
    public static void portableEclipseFiresOnlyInDaylight(GameTestHelper helper) {
        floor(helper);
        ring(helper, RitualCircle.SMALL_RITE, "circleglyphritual");
        var level = helper.getLevel();
        var center = helper.absolutePos(CENTER);
        waitForOfferings(helper, new ItemStack(Items.IRON_AXE), new ItemStack(WitcheryIngredient.QUICKLIME.item()),
                new ItemStack(WitcheryIngredient.CHARGED_ATTUNED_STONE.item())).thenExecute(() -> {
            long originalTime = level.getDayTime();
            float originalRain = level.getRainLevel(1);
            float originalThunder = level.getThunderLevel(1);
            try {
                level.setRainLevel(0);
                level.setThunderLevel(0);
                level.setDayTime(66_000);
                level.updateSkyBrightness();
                helper.useBlock(CENTER);
                helper.assertTrue(!helper.getBlockState(CENTER).getValue(RiteHeartBlock.ACTIVE) && drops(helper).size() == 3,
                        "At night the heart remains idle and all three offerings remain available");

                helper.setBlock(CENTER, Blocks.AIR);
                helper.setBlock(CENTER, block("circle"));
                var heart = heart(helper);
                tick(level, center, heart, 13);
                level.setDayTime(54_000);
                level.updateSkyBrightness();
                helper.useBlock(CENTER);
                tick(level, center, heart, 6);
                helper.assertTrue(heart.saveWithoutMetadata().getInt("OfferingStep") == 0,
                        "A cast on glyph tick thirteen waits for the offering pulse");
                tick(level, center, heart, 1);
                helper.assertTrue(heart.saveWithoutMetadata().getInt("OfferingStep") == 1,
                        "The first offering is taken on glyph tick twenty");
                tick(level, center, heart, 62);
                var saved = heart.saveWithoutMetadata();
                helper.assertTrue(saved.getInt("OfferingStep") == 3 && saved.getInt("GlyphPhase") == 82,
                        "The pending Eclipse and its glyph phase are saved after all three offerings");
                heart.load(saved);
                tick(level, center, heart, 7);
                helper.assertTrue(level.getDayTime() == 54_000 && heart.getBlockState().getValue(RiteHeartBlock.ACTIVE),
                        "The reloaded rite remains active through glyph tick eighty-nine");
                tick(level, center, heart, 1);
                var returned = drops(helper);
                helper.assertTrue(level.getDayTime() == 66_000 && !helper.getBlockState(CENTER).getValue(RiteHeartBlock.ACTIVE)
                                && returned.size() == 1 && returned.get(0).getItem().is(WitcheryIngredient.ATTUNED_STONE.item()),
                        "The rite reaches midnight on the same day and returns the discharged stone");
            } finally {
                level.setDayTime(originalTime);
                level.setRainLevel(originalRain);
                level.setThunderLevel(originalThunder);
                level.updateSkyBrightness();
                drops(helper).forEach(ItemEntity::discard);
            }
        }).thenSucceed();
    }

    @GameTest(template = TEMPLATE, batch = "witchery_rite_previous_forge_clock")
    public static void inProgressEclipseFromEarlierForgeKeepsItsNextPulse(GameTestHelper helper) {
        helper.setBlock(CENTER, block("circle"));
        var level = helper.getLevel();
        var center = helper.absolutePos(CENTER);
        var heart = heart(helper);
        var saved = new CompoundTag();
        saved.putString("Rite", "witchery:rite_of_total_eclipse");
        saved.putInt("OfferingStep", 2);
        saved.putBoolean("AltarPowerOffered", true);
        saved.putInt("StepTicks", 17);
        long originalTime = level.getDayTime();
        try {
            level.setDayTime(54_000);
            heart.load(saved);
            tick(level, center, heart, 2);
            helper.assertTrue(level.getDayTime() == 54_000 && heart.getBlockState().getValue(RiteHeartBlock.ACTIVE),
                    "The earlier save keeps its remaining three ticks and the altar payment");
            heart.load(heart.saveWithoutMetadata());
            tick(level, center, heart, 1);
            helper.assertTrue(level.getDayTime() == 66_000 && !heart.getBlockState().getValue(RiteHeartBlock.ACTIVE),
                    "The paid Eclipse completes at the saved deadline without another altar charge");
        } finally {
            level.setDayTime(originalTime);
            level.updateSkyBrightness();
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, batch = "witchery_rite_dimension_conditions")
    public static void legacyRitesUseEachRealmsDaylightAndThunder(GameTestHelper helper) {
        var overworld = helper.getLevel();
        var server = overworld.getServer();
        var end = server.getLevel(Level.END);
        var nether = server.getLevel(Level.NETHER);
        var mirror = server.getLevel(WitcheryDimensions.MIRROR);
        var torment = server.getLevel(WitcheryDimensions.TORMENT);
        var eclipse = rite(overworld, "rite_of_total_eclipse_portable");
        var necromancy = rite(overworld, "rite_of_necromantic_stone");
        var curse = rite(overworld, "curse_of_misfortune");
        var cure = rite(overworld, "cure_of_misfortune");
        var hell = rite(overworld, "rite_of_hell_on_earth");
        for (var world : List.of(nether, mirror, torment)) {
            helper.assertTrue(necromancy.startFailure(world) == null
                            && "message.witchery.rite.day_only".equals(eclipse.startFailure(world)),
                    "The original circle treats this realm as night: " + world.dimension().location());
        }
        helper.assertTrue(eclipse.startFailure(end) == null
                        && "message.witchery.rite.night_only".equals(necromancy.startFailure(end)),
                "The original circle treats the End as day");
        long overworldTime = overworld.getDayTime();
        try {
            overworld.setDayTime(6_000);
            helper.assertTrue(WeatherRites.eclipse(end) && overworld.getDayTime() == 6_000,
                    "An Eclipse in the End completes while the Overworld clock stays at noon");
        } finally {
            overworld.setDayTime(overworldTime);
        }
        helper.assertTrue("message.witchery.rite.hell_on_earth_overworld".equals(hell.startFailure(nether)),
                "Hell on Earth still requires the Overworld");

        float savedRain = overworld.getRainLevel(1);
        float savedThunder = savedRain == 0 ? 0 : overworld.getThunderLevel(1) / savedRain;
        float netherRain = nether.getRainLevel(1);
        float netherThunder = netherRain == 0 ? 0 : nether.getThunderLevel(1) / netherRain;
        try {
            overworld.setRainLevel(1);
            overworld.setThunderLevel(0.8F);
            nether.setRainLevel(0);
            helper.assertTrue("message.witchery.rite.storm_only".equals(curse.startFailure(torment))
                            && cure.startFailure(torment) == null,
                    "Torment waits for full thunder to start a curse and allows cures during weaker weather");
            overworld.setThunderLevel(1);
            helper.assertTrue(curse.startFailure(torment) == null
                            && "message.witchery.rite.storm_only".equals(curse.startFailure(nether)),
                    "A storm starting in the Overworld reaches Torment without changing Nether thunder");
            nether.setRainLevel(1);
            nether.setThunderLevel(1);
            helper.assertTrue(curse.startFailure(nether) == null,
                    "Nether eligibility honors the thunder strength loaded with the level");
        } finally {
            overworld.setRainLevel(savedRain);
            overworld.setThunderLevel(savedThunder);
            nether.setRainLevel(netherRain);
            nether.setThunderLevel(netherThunder);
        }
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, batch = "witchery_rite_saved_night_activation", setupTicks = 5)
    public static void queuedNecromancyKeepsItsNightActivationAfterReload(GameTestHelper helper) {
        floor(helper);
        ring(helper, RitualCircle.SMALL_RITE, "circleglyphritual");
        var level = helper.getLevel();
        var center = helper.absolutePos(CENTER);
        var altar = chargedAltar(helper, false);
        waitForOfferings(helper, new ItemStack(WitcheryIngredient.WAYSTONE.item()),
                new ItemStack(WitcheryIngredient.ENDER_DEW.item()), new ItemStack(Items.GLOWSTONE_DUST),
                new ItemStack(WitcheryIngredient.ATTUNED_STONE.item()), new ItemStack(Items.BONE),
                new ItemStack(Items.ROTTEN_FLESH), new ItemStack(WitcheryIngredient.WOOD_ASH.item()),
                new ItemStack(Items.IRON_SWORD), new ItemStack(WitcheryIngredient.SPECTRAL_DUST.item())).thenExecute(() -> {
            long originalTime = level.getDayTime();
            float originalRain = level.getRainLevel(1);
            float originalThunder = originalRain == 0 ? 0 : level.getThunderLevel(1) / originalRain;
            var heart = heart(helper);
            try {
                level.setRainLevel(0);
                level.setThunderLevel(0);
                level.setDayTime(18_000);
                level.updateSkyBrightness();
                start(helper);
                tick(level, center, heart, 20);
                var saved = heart.saveWithoutMetadata();
                helper.assertTrue(saved.getString("Rite").equals("witchery:rite_of_binding_a_waystone")
                                && saved.getList("QueuedRites", Tag.TAG_STRING).getString(0).equals("witchery:rite_of_necromantic_stone"),
                        "One activation queues Necromancy behind Waystone Binding");

                level.setDayTime(30_000);
                level.updateSkyBrightness();
                heart.load(saved);
                tick(level, center, heart, 240);
                helper.assertTrue(drops(helper).stream().anyMatch(drop -> drop.getItem().is(WitcheryIngredient.NECROMANTIC_STONE.item())),
                        "The saved queue completes the night cast after sunrise");
                helper.assertTrue(altar.power() == 8_500, "Both queued casts pay their altar charges once");
            } finally {
                level.setDayTime(originalTime);
                level.setRainLevel(originalRain);
                level.setThunderLevel(originalThunder);
                level.updateSkyBrightness();
                drops(helper).forEach(ItemEntity::discard);
            }
        }).thenSucceed();
    }

    private static ItemRiteRecipe rite(ServerLevel level, String path) {
        return (ItemRiteRecipe) level.getRecipeManager().byKey(ResourceLocation.fromNamespaceAndPath(Witchery.MODID, path)).orElseThrow();
    }

    @GameTest(template = TEMPLATE, batch = "witchery_legacy_climate_prelude", setupTicks = 5)
    public static void legacyClimatePreludeSurvivesBothSaveFormats(GameTestHelper helper) {
        helper.setBlock(CENTER, block("circle"));
        var legacy = new CompoundTag();
        legacy.putByteArray("Rituals", new byte[]{64, 4, 64, 70});
        legacy.putByteArray("RitualStages", new byte[]{1, 0, -1, 0});
        legacy.putByteArray("RitualCovens", new byte[]{4, 0, 4, 0});
        var initiators = new ListTag();
        initiators.add(StringTag.valueOf("ClimateCaster"));
        initiators.add(StringTag.valueOf("RechargeCaster"));
        legacy.put("Initiators", initiators);
        var heart = heart(helper);
        heart.load(legacy);
        var converted = heart.saveWithoutMetadata();
        var progress = converted.getCompound("Landscape");
        helper.assertTrue(progress.getInt("Stage") == 1
                        && converted.getString("InitiatorName").equals("ClimateCaster")
                        && converted.getInt("CovenSize") == 4,
                "The imported climate rite retains its first pulse, coven and initiator");
        var otherRites = converted.getList("UpkeepRites", Tag.TAG_COMPOUND);
        helper.assertTrue(otherRites.size() == 1 && otherRites.getCompound(0).getBoolean("Recharging")
                        && otherRites.getCompound(0).getString("InitiatorName").equals("RechargeCaster"),
                "A simultaneous recharge retains its name, and invalid stages do not activate");

        heart.load(converted);
        tick(helper.getLevel(), helper.absolutePos(CENTER), heart, 20);
        var advanced = heart.saveWithoutMetadata();
        helper.assertTrue(advanced.getCompound("Landscape").getInt("Stage") == 2
                        && advanced.getString("InitiatorName").equals("ClimateCaster"),
                "The modern save resumes the next pulse with the imported initiator");
        heart.toggle(helper.getLevel(), null);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, batch = "witchery_legacy_ice_shell", setupTicks = 5)
    public static void legacyIceShellReachesRadiusSixteenAndPreservesSolidTerrain(GameTestHelper helper) {
        ring(helper, RitualCircle.SMALL_RITE, "circleglyphritual");
        helper.setBlock(CENTER.east(16), Blocks.AIR);
        helper.setBlock(CENTER.west(16), Blocks.STONE);
        helper.setBlock(CENTER.east(17), Blocks.AIR);

        var legacy = new CompoundTag();
        legacy.putByteArray("Rituals", new byte[]{65});
        legacy.putByteArray("RitualStages", new byte[]{10});
        legacy.putByteArray("RitualCovens", new byte[]{6});
        var level = helper.getLevel();
        var center = helper.absolutePos(CENTER);
        var heart = heart(helper);
        heart.load(legacy);
        tick(level, center, heart, 10);
        heart.load(heart.saveWithoutMetadata());
        tick(level, center, heart, 9);
        helper.assertTrue(heart.saveWithoutMetadata().getCompound("Landscape").getInt("Stage") == 10,
                "The reloaded shell keeps its stage for the first nineteen ticks");
        tick(level, center, heart, 1);
        helper.assertTrue(heart.saveWithoutMetadata().getCompound("Landscape").getInt("Stage") == 11,
                "The first imported expansion occurs on tick twenty");
        tick(level, center, heart, 5);
        helper.assertBlockPresent(block("perpetualice"), CENTER.east(16));
        helper.assertBlockPresent(Blocks.STONE, CENTER.west(16));
        helper.assertBlockPresent(Blocks.AIR, CENTER.east(17));
        helper.assertFalse(helper.getBlockState(CENTER).getValue(RiteHeartBlock.ACTIVE),
                "The migrated shell finishes at the six-member radius on tick twenty-five");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, batch = "witchery_legacy_wave_gate")
    public static void importedFertilityAndCursesShareTheOriginalActivationGate(GameTestHelper helper) {
        floor(helper);
        helper.setBlock(CENTER, block("circle"));
        var level = helper.getLevel();
        var center = helper.absolutePos(CENTER);
        var heart = heart(helper);
        var legacy = new CompoundTag();
        legacy.putByteArray("Rituals", new byte[]{35, 36, 37, 38});
        legacy.putByteArray("RitualStages", new byte[]{2, 2, 2, 2});
        heart.load(legacy);
        tick(level, center, heart, 19);
        helper.assertTrue(waveStages(heart).equals(List.of(2, 2, 2, 2)), "All four imported waves wait until tick twenty");
        tick(level, center, heart, 1);
        helper.assertTrue(waveStages(heart).equals(List.of(3, 3, 3, 3)), "All four waves resume at their saved stages");
        tick(level, center, heart, 5);
        helper.assertTrue(waveStages(heart).equals(List.of(4, 4, 4, 4)), "Subsequent expansions run five ticks apart");
        helper.succeed();
    }

    private static List<Integer> waveStages(RiteHeartBlockEntity heart) {
        var save = heart.saveWithoutMetadata();
        var stages = new ArrayList<Integer>();
        stages.add(save.getCompound("Landscape").getInt("Stage"));
        for (Tag entry : save.getList("UpkeepRites", Tag.TAG_COMPOUND)) {
            stages.add(((CompoundTag) entry).getCompound("Landscape").getInt("Stage"));
        }
        return stages;
    }

    @GameTest(template = TEMPLATE, batch = "witchery_legacy_ward_gate")
    public static void legacyGreaterBarrierWaitsBeforeChargingAndKeepsItsCovenRange(GameTestHelper helper) {
        floor(helper);
        helper.setBlock(CENTER, block("circle"));
        var altar = chargedAltar(helper, false);
        var level = helper.getLevel();
        var center = helper.absolutePos(CENTER);
        var target = center.east(70);
        var wall = target.east(6);
        var heart = heart(helper);
        var saved = new CompoundTag();
        saved.putByteArray("Rituals", new byte[]{11});
        saved.putByteArray("RitualStages", new byte[]{0});
        saved.putByteArray("RitualCovens", new byte[]{1});
        var coordinates = new CompoundTag();
        coordinates.putInt("posX", target.getX());
        coordinates.putInt("posY", target.getY());
        coordinates.putInt("posZ", target.getZ());
        var locations = new ListTag();
        locations.add(coordinates);
        saved.put("Locations", locations);

        heart.load(saved);
        tick(level, center, heart, 19);
        var ready = heart.saveWithoutMetadata();
        helper.assertTrue(altar.power() == 10_000 && level.isEmptyBlock(wall), "The resumed ward waits before spending power or drawing a barrier");
        heart.load(ready);
        tick(level, center, heart, 1);
        helper.assertTrue(Math.abs(altar.power() - 9998.6F) < 0.01F && level.getBlockState(wall).is(block("barrier")),
                "The first powered shell appears on tick twenty at the one-member coven's bound target");

        var moved = CENTER.west(32);
        helper.setBlock(moved, block("circle"));
        var displaced = (RiteHeartBlockEntity) helper.getBlockEntity(moved);
        displaced.load(heart.saveWithoutMetadata());
        tick(level, helper.absolutePos(moved), displaced, 1);
        helper.assertTrue(!displaced.getBlockState().getValue(RiteHeartBlock.ACTIVE) && Math.abs(altar.power() - 9998.6F) < 0.01F,
                "Moving the heart outside the saved coven range ends the ward before any debit");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, batch = "witchery_legacy_portable_ward")
    public static void importedPortableBarrierFollowsTheMovedGlyphAndExpiresOnItsLastPulse(GameTestHelper helper) {
        helper.setBlock(CENTER.below(), Blocks.STONE);
        helper.setBlock(CENTER, block("circle"));
        var level = helper.getLevel();
        var oldCenter = helper.absolutePos(CENTER);
        var heart = heart(helper);
        var legacy = new CompoundTag();
        legacy.putByteArray("Rituals", new byte[]{12});
        legacy.putByteArray("RitualStages", new byte[]{58});
        heart.load(legacy);
        tick(level, oldCenter, heart, 19);
        var newCenter = CENTER.west(24);
        helper.setBlock(newCenter.below(), Blocks.STONE);
        helper.setBlock(newCenter, block("circle"));
        var moved = (RiteHeartBlockEntity) helper.getBlockEntity(newCenter);
        var absolute = helper.absolutePos(newCenter);
        moved.load(heart.saveWithoutMetadata());
        tick(level, absolute, moved, 1);
        helper.assertTrue(level.getBlockState(absolute.east(6)).is(block("barrier")) && level.isEmptyBlock(oldCenter.east(6)),
                "An unbound ward refreshes around the moved glyph on its first resume pulse");
        tick(level, absolute, moved, 19);
        helper.assertTrue(moved.getBlockState().getValue(RiteHeartBlock.ACTIVE), "The saved final second runs to its next pulse");
        tick(level, absolute, moved, 1);
        helper.assertFalse(moved.getBlockState().getValue(RiteHeartBlock.ACTIVE), "The second pulse expires the imported portable barrier");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, batch = "witchery_fissure_saved_step")
    public static void brokenEarthPreservesAnEarlierForgeDeadlineThenCutsEachTick(GameTestHelper helper) {
        helper.setBlock(CENTER, block("circle"));
        var level = helper.getLevel();
        var center = helper.absolutePos(CENTER);
        var heart = heart(helper);
        var saved = new CompoundTag();
        saved.putString("Rite", "witchery:rite_of_broken_earth");
        saved.putInt("StepTicks", 17);
        var fissure = new CompoundTag();
        fissure.putString("Action", "PART_EARTH");
        fissure.putLong("Target", center.asLong());
        fissure.putInt("Stage", 10);
        fissure.putLong("FissureSeed", 20L);
        fissure.putInt("FissureHeading", 2);
        saved.put("Landscape", fissure);
        heart.load(saved);
        tick(level, center, heart, 2);
        var converted = heart.saveWithoutMetadata();
        helper.assertTrue(converted.getCompound("Landscape").getInt("Stage") == 10, "The cut still waits for the earlier save's third tick");
        helper.assertFalse(converted.getCompound("Landscape").contains("FissureUsesLegacyRandom"),
                "An earlier Forge fissure still uses its original profile's random factory");
        heart.load(converted);
        tick(level, center, heart, 1);
        helper.assertTrue(heart.saveWithoutMetadata().getCompound("Landscape").getInt("Stage") == 11, "The cut occurs on the third tick");
        tick(level, center, heart, 2);
        helper.assertTrue(heart.saveWithoutMetadata().getCompound("Landscape").getInt("Stage") == 13,
                "Subsequent cuts advance on every loaded tick");
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, batch = "witchery_summoned_witch", setupTicks = 5, timeoutTicks = 175)
    public static void witchSummoningReturnsTheSameDamagedArthana(GameTestHelper helper) {
        floor(helper);
        ring(helper, RitualCircle.SMALL_RITE, "circleglyphinfernal");
        var altar = chargedAltar(helper, true);
        var arthana = new ItemStack(WitcheryItems.getRequired("arthana").get());
        arthana.setDamageValue(13);
        waitForOfferings(helper, new ItemStack(WitcheryIngredient.DIAMOND_VAPOUR.item()),
                new ItemStack(WitcheryIngredient.EXHALE_OF_THE_HORNED_ONE.item()), new ItemStack(Items.MAGMA_CREAM),
                new ItemStack(Items.FERMENTED_SPIDER_EYE), arthana).thenExecute(() -> {
            var charge = altar.saveWithoutMetadata();
            charge.putFloat("Power", 5000);
            altar.load(charge);
            start(helper);
            var level = helper.getLevel();
            var center = helper.absolutePos(CENTER);
            var heart = heart(helper);
            tick(level, center, heart, 100);
            float beforeCharge = altar.power();
            tick(level, center, heart, ALTAR_STEP_TICKS);
            helper.assertTrue(altar.power() == beforeCharge - 2000 && heart.saveWithoutMetadata().getBoolean("AltarPowerOffered"),
                    "The altar contributes 2,000 on its own step");
            tick(level, center, heart, 20);
            var witches = level.getEntitiesOfClass(Witch.class, new AABB(center).inflate(8));
            helper.assertTrue(witches.size() == 1, "The small Infernal circle summons exactly one witch");
            helper.assertTrue(altar.power() == beforeCharge - 2000, "The summoning uses the earlier payment");
            var returned = drops(helper);
            helper.assertTrue(returned.size() == 1 && ItemStack.matches(returned.get(0).getItem(), arthana),
                    "The summoning returns the original damaged Arthana, found "
                            + returned.stream().map(drop -> drop.getItem().serializeNBT()).toList());
            witches.forEach(Entity::discard);
            returned.forEach(ItemEntity::discard);
        }).thenSucceed();
    }

    @GameTest(template = TEMPLATE, batch = "witchery_weather_rite_waystone", setupTicks = 5)
    public static void greaterStormAcceptsAnOptionalRemoteWaystoneAndRejectsDistantBindings(GameTestHelper helper) {
        ring(helper, RitualCircle.SMALL_RITE, "circleglyphritual");
        var altar = chargedAltar(helper, false);
        var level = helper.getLevel();
        var center = helper.absolutePos(CENTER);
        var heart = heart(helper);
        var tooFar = bound(level, center.east(51));
        start(helper, new ItemStack(Items.STONE_SWORD), new ItemStack(WitcheryIngredient.WOOD_ASH.item()), tooFar);
        tick(level, center, heart, TWO_OFFERING_CAST - 20);
        var pending = heart.saveWithoutMetadata();
        helper.assertTrue(BlockPos.of(pending.getLong("WaystoneTarget")).equals(center.east(51))
                        && altar.power() == 10_000 && drops(helper).isEmpty(),
                "The Waystone is consumed before the altar charge and range check");
        tick(level, center, heart, 20);
        var paid = heart.saveWithoutMetadata();
        helper.assertTrue(paid.getBoolean("AltarPowerOffered") && altar.power() == 8000
                        && drops(helper).isEmpty(), "The altar is charged before the storm resolves the remote target");
        heart.load(paid);
        tick(level, center, heart, 1);
        helper.assertTrue(!heart.getBlockState().getValue(RiteHeartBlock.ACTIVE) && altar.power() == 8000
                        && drops(helper).size() == 3 && drops(helper).stream().anyMatch(item -> ItemStack.matches(item.getItem(), tooFar)),
                "The first loaded tick after payment rejects the distant Waystone and returns all three offerings");
        drops(helper).forEach(ItemEntity::discard);

        tick(level, center, heart, 9);
        var target = center.east(40).above(3);
        start(helper, new ItemStack(Items.STONE_SWORD), new ItemStack(WitcheryIngredient.WOOD_ASH.item()), bound(level, target));
        var strikes = new ArrayList<LightningBolt>();
        var area = new AABB(target).inflate(9);
        Consumer<EntityJoinLevelEvent> observe = event -> {
            if (event.getLevel() == level && event.getEntity() instanceof LightningBolt bolt && area.contains(bolt.position())) {
                strikes.add(bolt);
            }
        };
        MinecraftForge.EVENT_BUS.addListener(observe);
        try {
            tick(level, center, heart, 89);
            helper.assertTrue(strikes.isEmpty(), "The storm waits until the glyph's thirty-tick phase after payment");
            tick(level, center, heart, 1);
            var saved = heart.saveWithoutMetadata();
            var storm = saved.getCompound("Storm");
            helper.assertTrue(storm.getInt("Stage") == 1 && storm.getBoolean("Greater")
                            && BlockPos.of(storm.getLong("Target")).equals(target) && altar.power() == 6000,
                    "The valid Waystone relocates the first strike on the same pulse that launches the storm");
            helper.assertTrue(strikes.size() == 1 && strikes.get(0).getY() == target.getY(),
                    "The actual strike arrives in the remote chunk");
            heart.load(saved);
            tick(level, center, heart, 29);
            helper.assertTrue(strikes.size() == 1, "The saved storm waits until its original next phase");
            tick(level, center, heart, 1);
            helper.assertTrue(strikes.size() == 2, "The second storm strike comes thirty glyph ticks after the first");
        } finally {
            strikes.forEach(LightningBolt::discard);
            MinecraftForge.EVENT_BUS.unregister(observe);
        }
        helper.useBlock(CENTER);
        drops(helper).forEach(ItemEntity::discard);
        helper.succeed();
    }

    @GameTest(template = TEMPLATE, batch = "witchery_ward_barrier", setupTicks = 5)
    public static void focusedBarrierUsesTheCylinderAndLetsOnlyPlayersPass(GameTestHelper helper) {
        ring(helper, RitualCircle.SMALL_RITE, "circleglyphritual");
        var level = helper.getLevel();
        var center = helper.absolutePos(CENTER);
        var heart = heart(helper);
        var wall = center.east(4);
        var roof = center.above(5);
        var occupied = wall.above(2);
        level.setBlockAndUpdate(occupied, Blocks.CHEST.defaultBlockState());
        waitForOfferings(helper, new ItemStack(Items.OBSIDIAN), new ItemStack(Items.REDSTONE)).thenExecute(() -> {
            var altar = chargedAltar(helper, CENTER.offset(20, 0, 20), false, 2);
            start(helper);
            int phase = heart.saveWithoutMetadata().getInt("GlyphPhase") % 20;
            tick(level, center, heart, TWO_OFFERING_CAST + ALTAR_STEP_TICKS - phase);
            var barrier = block("barrier");
            helper.assertTrue(level.getBlockState(wall).is(barrier) && level.getBlockState(roof).is(barrier)
                    && level.getBlockState(wall.below()).is(barrier), "The focused barrier draws walls, roof and floor");
            helper.assertTrue(level.getBlockState(occupied).is(Blocks.CHEST) && level.getBlockState(center).is(block("circle")),
                    "The cylinder preserves the container and heart");
            var player = fake(level);
            var cow = helper.spawn(EntityType.COW, CENTER.east(2));
            var state = level.getBlockState(wall);
            helper.assertTrue(state.getCollisionShape(level, wall, CollisionContext.of(player)).isEmpty()
                    && !state.getCollisionShape(level, wall, CollisionContext.of(cow)).isEmpty(),
                    "The actual barrier collision lets a survival player pass while blocking the cow");
            var wallEntity = (BarrierBlockEntity) level.getBlockEntity(wall);
            helper.assertTrue(wallEntity.saveWithoutMetadata().getInt("remainingTicks") == 30
                    && Math.abs(altar.power() - 9498.8F) < 0.01F,
                    "The altar beyond 16 blocks pays the activation and first 1.2 upkeep charge");
            helper.useBlock(CENTER);
            helper.assertTrue(!level.getBlockState(center).getValue(RiteHeartBlock.ACTIVE)
                    && heart.saveWithoutMetadata().getList("ProtectionRites", Tag.TAG_COMPOUND).isEmpty(),
                    "Manual cancellation clears the heart without returning completed offerings");
            for (int tick = 0; tick < 30; tick++) BarrierBlockEntity.tick(level, wall, state, wallEntity);
            helper.assertTrue(level.isEmptyBlock(wall), "The last leased barrier expires after thirty ticks");
            cow.discard();
        }).thenSucceed();
    }

    @GameTest(template = TEMPLATE, batch = "witchery_recharging_range", setupTicks = 5)
    public static void infusionRechargePaysUpkeepAtArthanaRange(GameTestHelper helper) {
        ring(helper, RitualCircle.SMALL_RITE, "circleglyphritual");
        ring(helper, RitualCircle.MEDIUM_RITE, "circleglyphritual");
        var level = helper.getLevel();
        var center = helper.absolutePos(CENTER);
        var heart = heart(helper);
        var potion = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.REGENERATION);
        waitForOfferings(helper, potion).thenExecute(() -> {
            var altar = chargedAltar(helper, CENTER.offset(20, 0, 20), false, 2);
            start(helper);
            tick(level, center, heart, 60);
            helper.assertTrue(level.getBlockState(center).getValue(RiteHeartBlock.ACTIVE) && altar.power() == 5960,
                    "The rite pays 4,000 to start and 40 for its first recharge from the extended altar");

            var save = altar.saveWithoutMetadata();
            save.putInt("RangeScale", 1);
            altar.load(save);
            tick(level, center, heart, 20);
            helper.assertTrue(!level.getBlockState(center).getValue(RiteHeartBlock.ACTIVE) && altar.power() == 5960,
                    "The rite ends without another debit when the altar falls out of range");
        }).thenSucceed();
    }

    @GameTest(template = TEMPLATE, batch = "witchery_ward_remote", setupTicks = 5)
    public static void greaterBarrierResumesAtItsWaystoneAndRetainsPublicCollision(GameTestHelper helper) {
        ring(helper, RitualCircle.MEDIUM_RITE, "circleglyphritual");
        var level = helper.getLevel();
        var center = helper.absolutePos(CENTER);
        var target = center.east(43).above(6);
        waitForOfferings(helper, new ItemStack(Items.OBSIDIAN), new ItemStack(Items.GLOWSTONE_DUST), bound(level, target)).thenExecute(() -> {
            var altar = chargedAltar(helper, false);
            start(helper);
            var heart = heart(helper);
            int phase = heart.saveWithoutMetadata().getInt("GlyphPhase") % 20;
            tick(level, center, heart, TWO_OFFERING_CAST + ALTAR_STEP_TICKS - phase);
            var save = heart.saveWithoutMetadata();
            var ward = save.getList("ProtectionRites", Tag.TAG_COMPOUND).getCompound(0);
            helper.assertTrue(BlockPos.of(ward.getLong("Target")).equals(target) && ward.getString("Kind").equals("GREATER_BARRIER"),
                    "The same-dimension Waystone relocates a greater barrier");
            helper.assertTrue(Math.abs(altar.power() - 8998.6F) < 0.01F,
                    "The remote ward spends 1.4 from the altar beside the heart on its first pulse");
            heart.load(save);
            var wall = target.west(6);
            level.removeBlock(wall, false);
            tick(level, center, heart, 20);
            var state = level.getBlockState(wall);
            var player = fake(level);
            helper.assertTrue(state.is(block("barrier")) && level.getBlockState(target.above(6)).is(block("barrier"))
                    && level.getBlockState(target.below()).is(block("barrier")) && !level.getBlockState(center.above(6)).is(block("barrier")),
                    "The reloaded ward refreshes the entire remote shell around the original target");
            helper.assertTrue(!state.getCollisionShape(level, wall, CollisionContext.of(player)).isEmpty(),
                    "The remote ward blocks survival players");
            player.getAbilities().instabuild = true;
            player.setGameMode(GameType.CREATIVE);
            player.setShiftKeyDown(true);
            helper.assertTrue(state.getCollisionShape(level, wall, CollisionContext.of(player)).isEmpty(),
                    "Creative crouching bypasses the wall");
            helper.useBlock(CENTER);
        }).thenSucceed();
    }

    @GameTest(template = TEMPLATE, batch = "witchery_volcano_refund", setupTicks = 30, timeoutTicks = 150)
    public static void distantDryVolcanoRefundsTheWaystoneAndOfferings(GameTestHelper helper) {
        floor(helper);
        ring(helper, RitualCircle.SMALL_RITE, "circleglyphinfernal");
        var altar = chargedAltar(helper, true);
        var target = CENTER.south(13);
        helper.setBlock(target.below(), Blocks.BEDROCK);
        var stone = bound(helper, target);
        start(helper, new ItemStack(Items.COBBLESTONE), new ItemStack(Items.MAGMA_CREAM), new ItemStack(Items.GOLDEN_SWORD), stone);
        helper.runAfterDelay(THREE_OFFERING_CAST + ALTAR_STEP_TICKS + 5, () -> {
            helper.assertTrue(drops(helper).size() == 4 && !helper.getBlockState(CENTER).getValue(RiteHeartBlock.ACTIVE)
                            && drops(helper).stream().anyMatch(item -> ItemStack.matches(item.getItem(), stone)),
                    "A dry target must return all four offerings");
            helper.assertTrue(altar.power() < 9000, "The original reservoir check follows the power sacrifice");
            helper.assertBlockPresent(Blocks.AIR, target.east());
            helper.succeed();
        });
    }

    @GameTest(template = TEMPLATE, batch = "witchery_volcano_resume", setupTicks = 30, timeoutTicks = 370)
    public static void remoteVolcanoResumesAndSpendsItsWaystone(GameTestHelper helper) {
        floor(helper);
        ring(helper, RitualCircle.SMALL_RITE, "circleglyphinfernal");
        var altar = chargedAltar(helper, true);
        var target = CENTER.south(18);
        for (BlockPos floor : BlockPos.betweenClosed(target.offset(-9, -1, -9), target.offset(9, -1, 9))) {
            helper.setBlock(floor, Blocks.STONE);
        }
        helper.setBlock(target, Blocks.DIRT);
        helper.setBlock(target.below(3), Blocks.LAVA);
        helper.setBlock(target.below(3).west(), Blocks.LAVA);
        helper.setBlock(target.below(3).north(), Blocks.LAVA);
        helper.setBlock(target.below(4), Blocks.OBSIDIAN);
        // Container in the cone's footprint must remain intact.
        helper.setBlock(target.east(3), Blocks.CHEST);
        start(helper, new ItemStack(Items.COBBLESTONE), new ItemStack(Items.MAGMA_CREAM), new ItemStack(Items.GOLDEN_SWORD),
                bound(helper, target));
        helper.runAfterDelay(THREE_OFFERING_CAST + ALTAR_STEP_TICKS + 50, () -> {
            var heart = heart(helper);
            var saved = heart.saveWithoutMetadata();
            var state = saved.getCompound("Landscape");
            helper.assertTrue(state.getInt("Stage") >= 4 && state.getInt("Stage") < 8
                    && BlockPos.of(state.getLong("Target")).equals(helper.absolutePos(target)), "The volcano should be midway through growing");
            helper.assertTrue(altar.power() < 9900, "The cone construction follows the 2,000-power debit");
            heart.load(saved);
        });
        helper.runAfterDelay(THREE_OFFERING_CAST + ALTAR_STEP_TICKS + 230, () -> {
            helper.assertFalse(helper.getBlockState(CENTER).getValue(RiteHeartBlock.ACTIVE), "The remote eruption must complete");
            helper.assertTrue(drops(helper).isEmpty(), "The completed eruption spends the offerings and Waystone");
            long rim = List.of(target.above(7).north(), target.above(7).south(), target.above(7).west(), target.above(7).east())
                    .stream().filter(pos -> helper.getBlockState(pos).is(Blocks.STONE)).count();
            helper.assertTrue(rim >= 3, "The summit retains a stone rim and may open one vent");
            helper.assertBlockPresent(Blocks.CHEST, target.east(3));
            var summit = helper.getBlockState(target.above(7)).getFluidState();
            helper.assertTrue(summit.is(FluidTags.LAVA) && summit.isSource(),
                    "Lava should have risen to the top of the volcano");
            helper.assertTrue(!helper.getBlockState(target).is(Blocks.DIRT),
                    "The center shaft must open through the original terrain");
            helper.succeed();
        });
    }

    @GameTest(template = TEMPLATE, batch = "witchery_earth_lift", setupTicks = 5, timeoutTicks = 165)
    public static void movedHeartResumesAndCarriesContainerAndCreature(GameTestHelper helper) {
        floor(helper);
        ring(helper, RitualCircle.SMALL_RITE, "circleglyphritual");
        BlockPos chest = CENTER.east();
        helper.setBlock(chest, Blocks.CHEST.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING,
                Direction.EAST));
        ((ChestBlockEntity) helper.getBlockEntity(chest)).setItem(3, new ItemStack(Items.DIAMOND, 12));
        helper.setBlock(CENTER.south(2), Blocks.BEDROCK);
        helper.setBlock(CENTER.north(2), block("altar"));
        Pig pig = EntityType.PIG.create(helper.getLevel());
        pig.setNoAi(true);
        pig.setPos(Vec3.atBottomCenterOf(helper.absolutePos(CENTER.west(2))));
        helper.getLevel().addFreshEntity(pig);
        var cast = waitForOfferings(helper, movingEarthOfferings()).thenExecute(() -> start(helper));
        cast.thenIdle(THREE_OFFERING_CAST + 12).thenExecute(() -> {
            for (int rise = 1; rise < 7; rise++) {
                if (helper.getBlockEntity(CENTER.above(rise)) instanceof RiteHeartBlockEntity moved) {
                    var saved = moved.saveWithoutMetadata();
                    helper.assertTrue(saved.getCompound("Landscape").getInt("Stage") == rise,
                            "The relocated Heart Glyph must carry the updated stage");
                    moved.load(saved);
                    return;
                }
            }
            helper.fail("The active heart did not move");
        }).thenIdle(34).thenExecute(() -> {
            helper.assertBlockPresent(Blocks.AIR, CENTER);
            helper.assertBlockPresent(block("circle"), CENTER.above(7));
            helper.assertBlockPresent(block("circleglyphritual"), CENTER.north(3).above(7));
            helper.assertBlockPresent(Blocks.STONE, CENTER.below().above(7));
            helper.assertBlockPresent(Blocks.BEDROCK, CENTER.south(2));
            helper.assertBlockPresent(block("altar"), CENTER.north(2));
            var moved = (ChestBlockEntity) helper.getBlockEntity(chest.above(7));
            helper.assertTrue(moved.getItem(3).is(Items.DIAMOND) && moved.getItem(3).getCount() == 12,
                    "The original item stack must survive container relocation");
            helper.assertTrue(helper.getBlockState(chest.above(7)).getValue(BlockStateProperties.HORIZONTAL_FACING)
                            == Direction.EAST && !helper.getBlockState(CENTER.above(7)).getValue(RiteHeartBlock.ACTIVE),
                    "The chest retains its facing and the heart finishes");
            helper.assertTrue(pig.getBlockY() == helper.absolutePos(CENTER.above(7)).getY(), "Lift the pig with the floor");
            helper.assertTrue(helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(CENTER)).inflate(12)).isEmpty(),
                    "Raising a container must not duplicate its inventory or refund the rite");
            pig.discard();
        }).thenSucceed();
    }

    @GameTest(template = TEMPLATE, batch = "witchery_earth_remote", setupTicks = 5, timeoutTicks = 168)
    public static void remoteColumnHonorsPlacementCancellation(GameTestHelper helper) {
        floor(helper);
        ring(helper, RitualCircle.SMALL_RITE, "circleglyphritual");
        BlockPos target = CENTER.south(15);
        helper.setBlock(target.below(), Blocks.STONE);
        helper.setBlock(target, Blocks.EMERALD_BLOCK);
        helper.setBlock(target.west().below(), Blocks.GOLD_BLOCK);
        BlockPos protectedDestination = helper.absolutePos(target.above());
        Consumer<BlockEvent.EntityPlaceEvent> cancel = event -> {
            if (event.getPos().equals(protectedDestination) && event.getPlacedBlock().is(Blocks.EMERALD_BLOCK)) event.setCanceled(true);
        };
        offer(helper, movingEarthOfferings());
        offer(helper, bound(helper, target));
        helper.runAfterDelay(5, () -> {
            start(helper);
            MinecraftForge.EVENT_BUS.addListener(cancel);
            helper.runAfterDelay(THREE_OFFERING_CAST + 49, () -> {
                MinecraftForge.EVENT_BUS.unregister(cancel);
                helper.assertBlockPresent(block("circle"), CENTER);
                helper.assertBlockPresent(Blocks.EMERALD_BLOCK, target);
                helper.assertBlockPresent(Blocks.AIR, target.above());
                helper.assertBlockPresent(Blocks.GOLD_BLOCK, target.west().above(6));
                helper.assertTrue(helper.getLevel().getEntitiesOfClass(ItemEntity.class,
                        new AABB(helper.absolutePos(CENTER)).inflate(5)).isEmpty(), "The remote cast must spend the bound stone");
                helper.succeed();
            });
        });
    }

    private static void ring(GameTestHelper helper, RitualCircle size, String chalk) {
        helper.setBlock(CENTER.below(), Blocks.STONE);
        helper.setBlock(CENTER, block("circle"));
        var glyph = block(chalk);
        for (var offset : size.offsets()) {
            var pos = CENTER.offset(offset);
            helper.setBlock(pos.below(), Blocks.STONE);
            helper.setBlock(pos, glyph);
        }
    }

    private static void floor(GameTestHelper helper) {
        for (var floor : BlockPos.betweenClosed(CENTER.offset(-8, -1, -8), CENTER.offset(8, -1, 8))) {
            helper.setBlock(floor, Blocks.STONE);
        }
    }

    private static AltarBlockEntity chargedAltar(GameTestHelper helper, boolean stabilizedPower) {
        return chargedAltar(helper, CENTER.east(9), stabilizedPower, 1);
    }

    private static AltarBlockEntity chargedAltar(GameTestHelper helper, BlockPos corner, boolean stabilizedPower, int rangeScale) {
        for (var pos : BlockPos.betweenClosed(corner, corner.offset(1, 0, 2))) helper.setBlock(pos, block("altar"));
        if (stabilizedPower) helper.setBlock(corner.offset(1, 1, 2), block("infinityegg"));
        var altar = (AltarBlockEntity) helper.getBlockEntity(corner);
        var data = altar.saveWithoutMetadata();
        data.putFloat("Power", 10_000);
        data.putInt("PowerScale", 10);
        data.putInt("MaxPower", 1000);
        data.putInt("RangeScale", rangeScale);
        altar.load(data);
        return altar;
    }

    private static void start(GameTestHelper helper, ItemStack... offerings) {
        offer(helper, offerings);
        helper.useBlock(CENTER);
        helper.assertTrue(helper.getBlockState(CENTER).getValue(RiteHeartBlock.ACTIVE), "The Heart Glyph starts the rite");
    }

    private static GameTestSequence waitForOfferings(GameTestHelper helper, ItemStack... offerings) {
        offer(helper, offerings);
        return helper.startSequence().thenWaitUntil(() -> {
            var dropped = drops(helper);
            for (var offering : offerings) {
                helper.assertTrue(dropped.stream().anyMatch(item -> ItemStack.matches(item.getItem(), offering)),
                        "The dropped offering is visible before using the Heart Glyph: " + offering);
            }
        });
    }

    private static ItemStack[] movingEarthOfferings() {
        return new ItemStack[]{new ItemStack(WitcheryIngredient.BREW_OF_SPROUTING.item()),
                new ItemStack(Items.CACTUS), new ItemStack(Items.GUNPOWDER)};
    }

    private static void offer(GameTestHelper helper, ItemStack... items) {
        var level = helper.getLevel();
        var point = Vec3.atBottomCenterOf(helper.absolutePos(CENTER)).add(0, 0.3, 0);
        for (var item : items) {
            drop(level, point, item);
        }
    }

    private static ItemEntity drop(ServerLevel level, Vec3 point, ItemStack stack) {
        var entity = new ItemEntity(level, point.x, point.y, point.z, stack.copy(), 0, 0, 0);
        entity.setNeverPickUp();
        entity.setNoGravity(true);
        level.addFreshEntity(entity);
        return entity;
    }

    private static GrassperBlockEntity filledGrassper(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos, block("grassper"));
        var grassper = (GrassperBlockEntity) helper.getBlockEntity(pos);
        grassper.setItem(0, new ItemStack(Items.OBSIDIAN));
        return grassper;
    }

    private static List<ItemEntity> drops(GameTestHelper helper) {
        return helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(helper.absolutePos(CENTER)).inflate(4));
    }

    private static ItemStack bound(GameTestHelper helper, BlockPos target) {
        return bound(helper.getLevel(), helper.absolutePos(target));
    }

    private static ItemStack bound(ServerLevel level, BlockPos target) {
        var stone = new ItemStack(WitcheryIngredient.BOUND_WAYSTONE.item());
        WaystoneItem.bindLocation(stone, level, target);
        return stone;
    }

    private static RiteHeartBlockEntity heart(GameTestHelper helper) {
        return (RiteHeartBlockEntity) helper.getBlockEntity(CENTER);
    }

    private static void tick(ServerLevel level, BlockPos center, RiteHeartBlockEntity heart, int ticks) {
        for (int tick = 0; tick < ticks; tick++) {
            RiteHeartBlockEntity.serverTick(level, center, level.getBlockState(center), heart);
        }
    }

    private static Block block(String name) {
        return WitcheryBlocks.getRequired(name).get();
    }

    private static FakePlayer fake(ServerLevel level) {
        return new FakePlayer(level, new GameProfile(UUID.randomUUID(), "BarrierTraveler"));
    }
}
