package art.arcane.witchery.prediction;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.WitchOwl;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcherySounds;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.PlayLevelSoundEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class PredictionSaveTests {
    @GameTest(template = "qa_coven_arena")
    public static void readingsKeepRegisteredLegacyForecasts(GameTestHelper helper) {
        List<String> messages = new ArrayList<>();
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "SavedFortune")) {
            @Override
            public void sendSystemMessage(Component component) {
                if (component.getContents() instanceof TranslatableContents translated) {
                    messages.add(translated.getKey());
                }
            }
        };
        var iron = forecast(5, 1_200L);
        iron.putString("FixtureMarker", "original");
        var coal = forecast(17, 1_600L);
        var entries = store(player, forecast(999, 1_000L), iron, coal, iron.copy());
        Predictions.attune(player);

        Predictions.read(player, player);

        helper.assertTrue(List.copyOf(entries).equals(List.of(iron, coal, iron)),
                "A reading discards obsolete IDs and keeps the original records");
        helper.assertTrue(messages.equals(List.of("witchery.prediction.iron", "witchery.prediction.coal")),
                "A reading reports each registered forecast once");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void obsoleteForecastsCannotPreventNewReadings(GameTestHelper helper) {
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "NewFortune"));
        var entries = store(player, forecast(999, 1_000L));
        player.tickCount = 100;
        tickAtWorldTime(player, 99);
        helper.assertTrue(entries.size() == 1, "Player age does not advance the forecast between world pulses");
        player.tickCount = 17;
        tickAtWorldTime(player, 100);
        helper.assertTrue(entries.isEmpty(), "The world pulse clears obsolete forecast IDs at any player age");

        entries.add(forecast(999, 1_000L));
        Predictions.attune(player);
        Predictions.read(player, player);
        helper.assertTrue(entries.size() == 1 && Predictions.Kind.byId(entries.getCompound(0).getInt("WITCPreID")) != null
                        && entries.getCompound(0).getLong("WITCPreTime") > 1_000L,
                "An obsolete forecast cannot occupy the Crystal Ball's reading slot");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void readingUsesTheOriginalWeightedOrder(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "WeightedFortune"));
        var entries = store(player);
        Predictions.attune(player);
        // These slots fall in different readings at the same seed, depending on Nether eligibility.
        level.getRandom().setSeed(204);
        Predictions.read(player, player);
        helper.assertTrue(entries.size() == 1 && entries.getCompound(0).getInt("WITCPreID") == 4,
                "An ordinary reading uses the descending legacy weights");
        entries.clear();
        Predictions.onNetherVisit(player);
        level.getRandom().setSeed(204);
        Predictions.read(player, player);
        helper.assertTrue(entries.size() == 1 && entries.getCompound(0).getInt("WITCPreID") == 16,
                "The old Nether visitation flag restores the original Nether slot");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void overduePulseKeepsTheOriginalRollsAndSavedRecords(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "PulseFortune"));
        var love = forecast(9, 1_000);
        var entries = store(player, forecast(5, 1_000), love, forecast(8, 1_000), love.copy(), forecast(14, 1_000));
        var expected = new LegacyRandomSource(17);
        for (int i = 0; i < 6; i++) expected.nextDouble();
        level.getRandom().setSeed(17);

        tickAtWorldTime(player, 100);

        helper.assertTrue(entries.size() == 5 && entries.get(1) == love && entries.get(3) != love,
                "Individual identical forecasts stay in the original order when they do not fulfill");
        helper.assertTrue(level.getRandom().nextLong() == expected.nextLong(),
                "Overdue mining and rescue roll once, Love rolls twice, and Treasure skips the pulse");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_prediction_nether")
    public static void overdueNetherForecastTouchesThePortalAndSpawnsInTheSourceWorld(GameTestHelper helper) {
        var source = helper.getLevel();
        BlockPos ground = prepareEncounterFloor(helper);
        var player = new PortalWitness(source);
        player.setPos(Vec3.atBottomCenterOf(ground.above()));
        var entries = store(player, forecast(16, 1_000L));
        List<Blaze> blazes = new ArrayList<>();
        Consumer<EntityJoinLevelEvent> listener = event -> {
            if (event.getEntity() instanceof Blaze blaze) blazes.add(blaze);
        };
        MinecraftForge.EVENT_BUS.addListener(listener);
        try {
            rollOverdueForecast(player);
        } finally {
            MinecraftForge.EVENT_BUS.unregister(listener);
        }

        helper.assertTrue(entries.isEmpty() && player.contactsPortal() && !player.changedDimension,
                "A successful overdue forecast marks portal contact without moving the survival player");
        helper.assertTrue(player.advancePortal() == 1 && !player.contactsPortal() && player.advancePortal() == 0
                        && !player.changedDimension && player.serverLevel() == source,
                "One vanilla portal step is insufficient to carry a survival player to the Nether");
        helper.assertTrue(blazes.size() == 1, "The forecast summons exactly one Blaze");
        var blaze = blazes.get(0);
        int x = Math.abs(blaze.blockPosition().getX() - ground.getX());
        int z = Math.abs(blaze.blockPosition().getZ() - ground.getZ());
        helper.assertTrue(blaze.level() == source && blaze.getY() == ground.getY()
                        && blaze.getX() == blaze.getBlockX() && blaze.getZ() == blaze.getBlockZ()
                        && x >= 2 && x <= 4 && z >= 2 && z <= 4 && blaze.getTarget() == null,
                "The Blaze uses the original integer ground coordinates and starts without a forced target");
        blaze.discard();
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_prediction_love_cue")
    public static void overdueLoveForecastPlaysOneOriginalCueAtThePlayer(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos ground = prepareEncounterFloor(helper);
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "ForecastLove"));
        player.setPos(Vec3.atBottomCenterOf(ground.above()));
        var entries = store(player, forecast(9, 1_000L));
        List<PlayLevelSoundEvent.AtEntity> sounds = new ArrayList<>();
        Consumer<PlayLevelSoundEvent.AtEntity> listener = event -> {
            if (event.getEntity() == player) sounds.add(event);
        };
        MinecraftForge.EVENT_BUS.addListener(listener);
        try {
            setEncounterLayer(level, ground.below(), Blocks.AIR);
            rollOverdueForecast(player);
            helper.assertTrue(entries.size() == 1 && sounds.isEmpty(),
                    "An isolated platform lacks the original support below a suitor's landing block");
            setEncounterLayer(level, ground.below(), Blocks.STONE);
            rollOverdueForecast(player);
        } finally {
            MinecraftForge.EVENT_BUS.unregister(listener);
        }
        helper.assertTrue(entries.isEmpty() && sounds.size() == 1, "A spawned suitor sends one cue at the forecast player");
        var sound = sounds.get(0);
        helper.assertTrue(sound.getSound().value() == WitcherySounds.FORECAST_LOVE.get()
                        && sound.getSource() == SoundSource.NEUTRAL && sound.getNewVolume() == 0.5F
                        && sound.getNewPitch() >= 1F / 3F && sound.getNewPitch() <= 0.5F,
                "Keep the original sound ID, category, half volume and pitch range");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_prediction_encounter_air")
    public static void overdueFightRequiresTheOriginalAirColumn(GameTestHelper helper) {
        var level = helper.getLevel();
        var ground = prepareEncounterFloor(helper);
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "ForecastFight"));
        player.setPos(Vec3.atBottomCenterOf(ground.above()));
        var entries = store(player, forecast(1, 1_000L));
        List<Zombie> spawned = new ArrayList<>();
        Consumer<EntityJoinLevelEvent> listener = event -> {
            if (event.getEntity() instanceof Zombie zombie) spawned.add(zombie);
        };
        MinecraftForge.EVENT_BUS.addListener(listener);
        try {
            setEncounterLayer(level, ground.above(2), Blocks.WATER);
            rollOverdueForecast(player);
            helper.assertTrue(entries.size() == 1 && spawned.isEmpty(),
                    "Water in the second block interrupts the clear two-block column");
            setEncounterLayer(level, ground.above(2), Blocks.AIR);
            rollOverdueForecast(player);
        } finally {
            MinecraftForge.EVENT_BUS.unregister(listener);
        }
        helper.assertTrue(entries.isEmpty() && spawned.size() == 1,
                "The same forecast fulfills once the column has enough air");
        Zombie zombie = spawned.get(0);
        helper.assertTrue(Math.abs(zombie.getY() - (ground.getY() + 1.05)) < 0.0001 && zombie.getTarget() == player,
                "The arrival uses the original fractional height and targets the forecast player");
        zombie.discard();
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_prediction_rescue_slab")
    public static void wolfRescueCanArriveAboveASlab(GameTestHelper helper) {
        var level = helper.getLevel();
        var ground = prepareEncounterFloor(helper);
        setEncounterLayer(level, ground, Blocks.STONE_SLAB);
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "ForecastRescue"));
        player.setPos(Vec3.atBottomCenterOf(ground.above()));
        var entries = store(player, forecast(14, 1_000L));
        var attacker = EntityType.ZOMBIE.create(level);
        attacker.moveTo(player.getX() + 1, player.getY(), player.getZ());
        List<Wolf> spawned = new ArrayList<>();
        Consumer<EntityJoinLevelEvent> listener = event -> {
            if (event.getEntity() instanceof Wolf wolf) spawned.add(wolf);
        };
        MinecraftForge.EVENT_BUS.addListener(listener);
        try {
            Predictions.onHurt(player, level.damageSources().mobAttack(attacker));
        } finally {
            MinecraftForge.EVENT_BUS.unregister(listener);
        }
        helper.assertTrue(entries.isEmpty() && spawned.size() == 1,
                "A rescue recognizes the clear air above legacy slab footing");
        helper.assertTrue(spawned.get(0).getTarget() == attacker
                        && Math.abs(spawned.get(0).getY() - (ground.getY() + 1.05)) < 0.0001,
                "The rescue starts above the slab block with the recorded attacker as its target");
        spawned.get(0).discard();
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void hurtForecastsMatchTheOriginalDamageTypes(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "ForecastHurt"));
        var entries = store(player, forecast(2, 0), forecast(4, 0), forecast(1, 0));
        var archer = EntityType.SKELETON.create(level);
        var arrow = level.damageSources().arrow(EntityType.ARROW.create(level), archer);
        var trident = level.damageSources().trident(EntityType.TRIDENT.create(level), archer);
        var canceled = new LivingHurtEvent(player, arrow, 4);
        canceled.setCanceled(true);
        MinecraftForge.EVENT_BUS.post(canceled);
        forecastHurt(player, trident);
        forecastHurt(player, level.damageSources().stalagmite());
        helper.assertTrue(entries.size() == 3, "Canceled hits, tridents and stalagmites keep the original forecasts");

        forecastHurt(player, arrow);
        helper.assertTrue(entries.size() == 2 && entries.getCompound(0).getInt("WITCPreID") == 4,
                "An ordinary arrow clears the arrow forecast only");
        entries.add(forecast(2, 0));
        forecastHurt(player, level.damageSources().arrow(WitcheryEntities.BOLT.get().create(level), archer));
        helper.assertTrue(entries.size() == 2 && entries.getCompound(1).getInt("WITCPreID") == 1,
                "Witchery bolts also fulfill the original arrow forecast");
        forecastHurt(player, level.damageSources().fall());
        forecastHurt(player, level.damageSources().mobAttack(EntityType.ZOMBIFIED_PIGLIN.create(level)));
        helper.assertTrue(entries.isEmpty(), "A vanilla fall and a zombified piglin fulfill their own forecasts");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_prediction_rescue_order")
    public static void rescueCompanionsArriveInSavedOrder(GameTestHelper helper) {
        var level = helper.getLevel();
        var ground = prepareEncounterFloor(helper);
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "ForecastPair"));
        player.setPos(Vec3.atBottomCenterOf(ground.above()));
        var attacker = EntityType.ZOMBIE.create(level);
        var owl = forecast(13, 0);
        var wolf = forecast(14, 0);
        List<Mob> arrivals = new ArrayList<>();
        Consumer<EntityJoinLevelEvent> listener = event -> {
            if (event.getEntity() instanceof WitchOwl owlEntity) arrivals.add(owlEntity);
            if (event.getEntity() instanceof Wolf wolfEntity) arrivals.add(wolfEntity);
        };
        MinecraftForge.EVENT_BUS.addListener(listener);
        try {
            var entries = store(player, owl, wolf);
            level.getRandom().setSeed(17);
            forecastHurt(player, level.damageSources().mobAttack(attacker));
            helper.assertTrue(entries.isEmpty() && arrivals.size() == 2
                            && arrivals.get(0) instanceof WitchOwl && arrivals.get(1) instanceof Wolf
                            && arrivals.get(0).blockPosition().equals(ground.above().offset(-3, 0, -4))
                            && arrivals.get(1).blockPosition().equals(ground.above().offset(-2, 0, 3))
                            && arrivals.get(0).getTarget() == attacker && arrivals.get(1).getTarget() == attacker
                            && level.getRandom().nextInt(1000) == 404,
                    "The first owl color roll determines the following wolf's column");
            arrivals.forEach(Entity::discard);
            arrivals.clear();

            entries = store(player, wolf, owl);
            level.getRandom().setSeed(17);
            forecastHurt(player, level.damageSources().mobAttack(attacker));
            helper.assertTrue(entries.isEmpty() && arrivals.size() == 2
                            && arrivals.get(0) instanceof Wolf && arrivals.get(1) instanceof WitchOwl
                            && arrivals.get(0).blockPosition().equals(ground.above().offset(-3, 0, -4))
                            && arrivals.get(1).blockPosition().equals(ground.above().offset(4, 0, -3))
                            && level.getRandom().nextInt(1000) == 404,
                    "Reversing the saved order keeps each companion's original random draw");
            arrivals.forEach(Entity::discard);
            arrivals.clear();

            entries = store(player, wolf);
            level.getRandom().setSeed(17);
            forecastHurt(player, level.damageSources().arrow(EntityType.ARROW.create(level), player));
            helper.assertTrue(entries.isEmpty() && arrivals.size() == 1 && arrivals.get(0).getTarget() == player,
                    "A self-inflicted arrow hit can summon its forecast wolf");
            arrivals.forEach(Entity::discard);
            arrivals.clear();

            entries = store(player, wolf);
            attacker.setHealth(0);
            level.getRandom().setSeed(17);
            forecastHurt(player, level.damageSources().mobAttack(attacker));
            helper.assertTrue(entries.isEmpty() && arrivals.size() == 1 && arrivals.get(0).getTarget() == attacker,
                    "A dying attacker can trigger the same rescue");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(listener);
            arrivals.forEach(Entity::discard);
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_prediction_harvest_order")
    public static void miningRollsFollowTheSavedForecastOrder(GameTestHelper helper) {
        var level = helper.getLevel();
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "ForecastMining"));
        var pos = helper.absolutePos(new BlockPos(8, 11, 8));
        level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        var iron = forecast(5, 0);
        var emerald = forecast(7, 0);
        List<ItemStack> drops = new ArrayList<>();

        var entries = store(player, emerald, iron);
        level.getRandom().setSeed(4641);
        Predictions.onHarvest(player, pos, Blocks.STONE.defaultBlockState(), drops);
        helper.assertTrue(List.copyOf(entries).equals(List.of(iron)) && drops.size() == 1
                        && drops.get(0).is(Items.EMERALD) && level.getRandom().nextInt(1000) == 36,
                "Emerald first grants one emerald and leaves the iron forecast");

        drops.clear();
        entries = store(player, iron, emerald);
        level.getRandom().setSeed(4641);
        Predictions.onHarvest(player, pos, Blocks.STONE.defaultBlockState(), drops);
        helper.assertTrue(List.copyOf(entries).equals(List.of(emerald)) && drops.size() == 1
                        && drops.get(0).is(Items.IRON_ORE) && drops.get(0).getCount() == 17
                        && level.getRandom().nextInt(1000) == 629,
                "Iron first grants the original seventeen ores and leaves the emerald forecast");

        drops.clear();
        entries = store(player, emerald, forecast(4, 0), emerald.copy());
        level.getRandom().setSeed(23);
        Predictions.onHarvest(player, pos, Blocks.STONE.defaultBlockState(), drops);
        helper.assertTrue(entries.size() == 2 && entries.get(0) == emerald && drops.size() == 1
                        && drops.get(0).is(Items.EMERALD),
                "When only the second identical forecast succeeds, retain the first in its original slot");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_prediction_nether_pulse")
    public static void enteringTheNetherFulfillsTheForecastOnTheNextWorldPulse(GameTestHelper helper) {
        var nether = helper.getLevel().getServer().getLevel(Level.NETHER);
        var player = new FakePlayer(nether, new GameProfile(UUID.randomUUID(), "ForecastNetherVisit"));
        var entries = store(player, forecast(16, 1_000L));

        Predictions.onNetherVisit(player);
        tickAtWorldTime(player, 99);
        helper.assertTrue(entries.size() == 1
                        && player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG).getBoolean("WITCVisitedNether"),
                "Entering the Nether records eligibility and keeps the active forecast until its scheduled check");
        tickAtWorldTime(player, 100);
        helper.assertTrue(entries.isEmpty(), "The next world pulse fulfills the forecast while the player is in the Nether");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void overdueFallWorksAboveFarmlandDuringAJump(GameTestHelper helper) {
        var level = helper.getLevel();
        var ground = prepareSoil(helper);
        level.setBlockAndUpdate(ground, Blocks.FARMLAND.defaultBlockState());
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "ForecastFall"));
        player.setPos(Vec3.atBottomCenterOf(ground.above()).add(0, 0.25, 0));
        player.setOnGround(false);
        var entries = store(player, forecast(4, 1_000L));
        BlockPos warded = ground.north().below(2);
        level.setBlockAndUpdate(ground.south().below(), Blocks.BEDROCK.defaultBlockState());
        Consumer<BlockEvent.BreakEvent> veto = event -> {
            if (event.getPlayer() == player && event.getPos().equals(warded)) event.setCanceled(true);
        };
        MinecraftForge.EVENT_BUS.addListener(veto);
        try {
            rollOverdueForecast(player);
        } finally {
            MinecraftForge.EVENT_BUS.unregister(veto);
        }

        helper.assertTrue(entries.isEmpty(), "The Fall forecast can fulfill during a jump over farmland");
        helper.assertTrue(level.getBlockState(ground).is(Blocks.GRAVEL), "The cultivated roof becomes gravel");
        for (int depth = 1; depth <= 5; depth++) {
            helper.assertTrue(level.isEmptyBlock(ground.below(depth)), "The Fall cavity is five blocks deep");
        }
        helper.assertTrue(level.getBlockState(ground.below(6)).is(Blocks.STONE), "The floor stays below the cavity");
        helper.assertTrue(level.getBlockState(warded).is(Blocks.STONE)
                        && level.getBlockState(ground.south().below()).is(Blocks.BEDROCK),
                "The cavity skips protected blocks under the roof");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void overdueWetKeepsItsReadingWhenTheRoofIsProtected(GameTestHelper helper) {
        var level = helper.getLevel();
        var ground = prepareSoil(helper);
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "ForecastWet"));
        player.setPos(Vec3.atBottomCenterOf(ground.above()));
        player.setOnGround(true);
        var entries = store(player, forecast(15, 1_000L));
        Consumer<BlockEvent.EntityMultiPlaceEvent> veto = event -> {
            if (event.getEntity() == player) event.setCanceled(true);
        };
        MinecraftForge.EVENT_BUS.addListener(veto);
        try {
            rollOverdueForecast(player);
        } finally {
            MinecraftForge.EVENT_BUS.unregister(veto);
        }
        helper.assertTrue(entries.size() == 1 && level.getBlockState(ground).is(Blocks.DIRT)
                        && level.getBlockState(ground.below()).is(Blocks.STONE),
                "A veto on the gravel roof leaves the soil and active forecast intact");

        level.setBlockAndUpdate(ground, Blocks.FARMLAND.defaultBlockState());
        player.setPos(Vec3.atBottomCenterOf(ground.above()).add(0, -0.0625, 0));
        rollOverdueForecast(player);
        helper.assertTrue(entries.isEmpty(), "The same Wet forecast fulfills while standing on the shorter modern farmland surface");
        for (BlockPos surface : BlockPos.betweenClosed(ground.offset(-1, 0, -1), ground.offset(1, 0, 1))) {
            helper.assertTrue(level.getBlockState(surface).is(Blocks.GRAVEL), "The Wet forecast replaces the whole roof");
            helper.assertTrue(level.getBlockState(surface.below()).equals(Blocks.WATER.defaultBlockState())
                            && level.getBlockState(surface.below(2)).equals(Blocks.WATER.defaultBlockState())
                            && level.getBlockState(surface.below(3)).is(Blocks.STONE),
                    "The Wet forecast fills exactly two layers with water sources");
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void buriedTreasureFillsACoveredAirPocket(GameTestHelper helper) {
        var level = helper.getLevel();
        var surface = prepareSoil(helper);
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "ForecastTreasure"));
        var entries = store(player, forecast(8, 1_000L));
        var chestPos = surface.below();
        level.setBlockAndUpdate(surface, Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(chestPos, Blocks.AIR.defaultBlockState());
        var drops = new ArrayList<>(List.of(new ItemStack(Items.DIRT)));

        harvestForTreasure(player, surface, drops);

        helper.assertTrue(entries.isEmpty() && level.getBlockEntity(chestPos) instanceof ChestBlockEntity,
                "The buried chest appears in the covered air pocket and clears the forecast");
        var data = level.getBlockEntity(chestPos).saveWithoutMetadata();
        var contents = data.getList("Items", Tag.TAG_COMPOUND);
        var originalLoot = Set.of("minecraft:iron_ingot", "minecraft:gold_ingot", "minecraft:redstone",
                "minecraft:lapis_lazuli", "minecraft:diamond", "minecraft:coal", "minecraft:bread",
                "minecraft:iron_pickaxe", "minecraft:rail", "minecraft:melon_seeds", "minecraft:pumpkin_seeds",
                "minecraft:saddle", "minecraft:iron_horse_armor", "minecraft:enchanted_book");
        helper.assertTrue(!data.contains("LootTable") && !contents.isEmpty() && contents.size() <= 6
                        && contents.stream().allMatch(tag -> originalLoot.contains(((CompoundTag) tag).getString("id"))),
                "The chest immediately saves loot from the original mineshaft pool");
        var chest = (ChestBlockEntity) level.getBlockEntity(chestPos);
        helper.assertTrue(contents.size() == 3 && chest.getItem(0).is(Items.PUMPKIN_SEEDS) && chest.getItem(0).getCount() == 2
                        && chest.getItem(4).is(Items.GOLD_INGOT) && chest.getItem(4).getCount() == 2
                        && chest.getItem(23).is(Items.MELON_SEEDS) && chest.getItem(23).getCount() == 2,
                "Seed 5120 produces the same slots as the original Buried Treasure harvest");
        helper.assertTrue(level.getRandom().nextLong() == -8752557157944875801L,
                "The treasure chest consumes the original draws on the world's random source");
        helper.assertTrue(drops.size() == 1 && drops.get(0).is(Items.DIRT), "The dirt harvest keeps its normal drop");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void buriedTreasureKeepsItsReadingWhenTheSiteIsOpenOrProtected(GameTestHelper helper) {
        var level = helper.getLevel();
        var surface = prepareSoil(helper);
        var chestPos = surface.below();
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "ProtectedTreasure"));
        var entries = store(player, forecast(8, 1_000L));
        level.setBlockAndUpdate(surface, Blocks.AIR.defaultBlockState());
        BlockPos wall = chestPos.relative(Direction.EAST);
        level.setBlockAndUpdate(wall, Blocks.AIR.defaultBlockState());
        harvestForTreasure(player, surface, List.of());
        helper.assertTrue(entries.size() == 1 && level.getBlockState(chestPos).is(Blocks.STONE),
                "An exposed site leaves the forecast active");
        level.setBlockAndUpdate(wall, Blocks.STONE.defaultBlockState());

        Consumer<BlockEvent.BreakEvent> denyBreak = event -> {
            if (event.getPlayer() == player && event.getPos().equals(chestPos)) event.setCanceled(true);
        };
        MinecraftForge.EVENT_BUS.addListener(denyBreak);
        try {
            harvestForTreasure(player, surface, List.of());
        } finally {
            MinecraftForge.EVENT_BUS.unregister(denyBreak);
        }
        helper.assertTrue(entries.size() == 1 && level.getBlockState(chestPos).is(Blocks.STONE),
                "A Forge break veto preserves the site and the forecast");

        Consumer<BlockEvent.EntityPlaceEvent> denyChest = event -> {
            if (event.getEntity() == player && event.getPos().equals(chestPos)
                    && event.getPlacedBlock().is(Blocks.CHEST)) event.setCanceled(true);
        };
        MinecraftForge.EVENT_BUS.addListener(denyChest);
        try {
            harvestForTreasure(player, surface, List.of());
        } finally {
            MinecraftForge.EVENT_BUS.unregister(denyChest);
        }
        helper.assertTrue(entries.size() == 1 && level.getBlockState(chestPos).is(Blocks.STONE)
                        && level.getBlockEntity(chestPos) == null,
                "A Forge chest placement veto restores the site, with no chest or spent forecast");

        level.setBlockAndUpdate(chestPos, Blocks.WATER.defaultBlockState());
        harvestForTreasure(player, surface, List.of());
        helper.assertTrue(entries.isEmpty() && level.getBlockEntity(chestPos) instanceof ChestBlockEntity,
                "The same forecast fulfills in a covered water pocket");
        helper.succeed();
    }

    private static void harvestForTreasure(FakePlayer player, BlockPos pos, List<ItemStack> drops) {
        player.serverLevel().getRandom().setSeed(5120);
        Predictions.onHarvest(player, pos, Blocks.DIRT.defaultBlockState(), drops);
    }

    private static BlockPos prepareSoil(GameTestHelper helper) {
        var ground = helper.absolutePos(new BlockPos(6, 8, 6));
        var level = helper.getLevel();
        for (BlockPos pos : BlockPos.betweenClosed(ground.offset(-1, -6, -1), ground.offset(1, -1, 1))) {
            level.setBlockAndUpdate(pos, Blocks.STONE.defaultBlockState());
        }
        for (BlockPos pos : BlockPos.betweenClosed(ground.offset(-1, 0, -1), ground.offset(1, 0, 1))) {
            level.setBlockAndUpdate(pos.above(), Blocks.AIR.defaultBlockState());
            level.setBlockAndUpdate(pos, Blocks.DIRT.defaultBlockState());
        }
        return ground;
    }

    private static BlockPos prepareEncounterFloor(GameTestHelper helper) {
        var ground = helper.absolutePos(new BlockPos(8, 10, 8));
        var level = helper.getLevel();
        setEncounterLayer(level, ground.below(), Blocks.STONE);
        setEncounterLayer(level, ground, Blocks.STONE);
        setEncounterLayer(level, ground.above(), Blocks.AIR);
        setEncounterLayer(level, ground.above(2), Blocks.AIR);
        return ground;
    }

    private static void setEncounterLayer(ServerLevel level, BlockPos center, Block block) {
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-6, 0, -6), center.offset(6, 0, 6))) {
            level.setBlockAndUpdate(pos, block.defaultBlockState());
        }
    }

    private static final class PortalWitness extends FakePlayer {
        private boolean changedDimension;

        private PortalWitness(ServerLevel level) {
            super(level, new GameProfile(UUID.randomUUID(), "ForecastNether"));
        }

        @Override
        public Entity changeDimension(ServerLevel destination) {
            changedDimension = true;
            return this;
        }

        private int advancePortal() {
            handleNetherPortal();
            return portalTime;
        }

        private boolean contactsPortal() {
            return isInsidePortal;
        }
    }

    private static void rollOverdueForecast(FakePlayer player) {
        player.level().getRandom().setSeed(5120);
        tickAtWorldTime(player, 100);
    }

    private static void forecastHurt(FakePlayer player, DamageSource source) {
        MinecraftForge.EVENT_BUS.post(new LivingHurtEvent(player, source, 4));
    }

    private static void tickAtWorldTime(FakePlayer player, long time) {
        var clock = player.serverLevel().getServer().getWorldData().overworldData();
        long previous = clock.getGameTime();
        clock.setGameTime(time);
        try {
            Predictions.tick(player);
        } finally {
            clock.setGameTime(previous);
        }
    }

    private static CompoundTag forecast(int id, long time) {
        var reading = new CompoundTag();
        reading.putInt("WITCPreID", id);
        reading.putLong("WITCPreTime", time);
        return reading;
    }

    private static ListTag store(Player player, CompoundTag... stored) {
        var entries = new ListTag();
        entries.addAll(List.of(stored));
        var prediction = new CompoundTag();
        prediction.put("WITCPreList", entries);
        var data = new CompoundTag();
        data.put("WITCPredict", prediction);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, data);
        return entries;
    }
}
