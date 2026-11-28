package art.arcane.witchery.block;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.entity.AltarBlockEntity;
import art.arcane.witchery.block.entity.FetishBlockEntity;
import art.arcane.witchery.block.entity.FetishEffect;
import art.arcane.witchery.block.entity.RiteHeartBlockEntity;
import art.arcane.witchery.entity.SpectralUndead;
import art.arcane.witchery.item.TaglockItem;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.registry.WitcheryItems;
import art.arcane.witchery.ritual.RitualCircle;
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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedstoneLampBlock;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class FetishBehaviorTests {
    private static final String TEMPLATE = "qa_rite_terrain";
    private static final BlockPos CENTER = new BlockPos(19, 220, 19);

    @GameTest(template = TEMPLATE, batch = "witchery_localized_alarm", setupTicks = 5, timeoutTicks = 160)
    public static void localizedAlarmGroupsSurvivePlacementAndReload(GameTestHelper helper) {
        floor(helper);
        helper.setBlock(CENTER, WitcheryBlocks.getRequired("scarecrow").get());
        helper.setBlock(CENTER.east(), Blocks.REDSTONE_LAMP);
        var fetish = (FetishBlockEntity) helper.getBlockEntity(CENTER);
        var data = alarm(FetishEffect.SCREAMER);
        var groups = new ListTag();
        for (String name : List.of("Duende", "Шабашная ведьма", "Mooshroom", "妖精")) {
            var group = new CompoundTag();
            group.putString("CreatureTypeName", name);
            if (name.equals("妖精")) group.putString("CreatureTypeId", "minecraft:sheep");
            groups.add(group);
        }
        data.put("KnownCreatureTypes", groups);
        var item = new ItemStack(WitcheryBlocks.getRequired("scarecrow").get());
        item.getOrCreateTag().put("TileData", data);
        fetish.loadItem(item);
        var saved = fetish.saveWithoutMetadata();
        var records = saved.getList("KnownCreatureTypes", Tag.TAG_COMPOUND);
        var ids = List.of("witchery:goblin", "witchery:covenwitch", "minecraft:mooshroom", "minecraft:sheep");
        for (int index = 0; index < ids.size(); index++) {
            helper.assertTrue(ids.get(index).equals(records.getCompound(index).getString("CreatureTypeId"))
                            && groups.getCompound(index).getString("CreatureTypeName")
                                    .equals(records.getCompound(index).getString("CreatureTypeName")),
                    "The saved label and its stable entity type round trip together");
        }
        fetish.load(saved);
        var level = helper.getLevel();
        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(
                level.isPositionEntityTicking(helper.absolutePos(CENTER.south(3)))
                        && level.isPositionEntityTicking(helper.absolutePos(CENTER.north(3))),
                "The alarm fixture accepts ticking subjects")).thenExecute(() -> {
            var goblin = placeMob(helper, WitcheryEntities.GOBLIN.get(), CENTER.south(3));
            var witch = placeMob(helper, WitcheryEntities.COVEN_WITCH.get(), CENTER.north(3));
            placeMob(helper, EntityType.MOOSHROOM, CENTER.south(4));
            placeMob(helper, EntityType.SHEEP, CENTER.north(4));
            helper.startSequence().thenIdle(24).thenExecute(() -> {
                helper.assertFalse(lampLit(helper), "All four loaded groups leave the alarm quiet");
                goblin.setCustomName(Component.literal("Named Hobgoblin"));
            }).thenWaitUntil(() -> helper.assertTrue(lampLit(helper), "A named individual still needs its own binding"))
                    .thenExecute(() -> {
                        var taglock = new ItemStack(WitcheryItems.getRequired("taglockkit").get());
                        TaglockItem.bindSignature(taglock, goblin);
                        use(helper, actor(helper, "LocalizedAlarmTester"), taglock);
                        helper.assertTrue(taglock.isEmpty(), "A normal Taglock use registers the named individual");
                        var bound = fetish.saveWithoutMetadata();
                        var individual = bound.getList("KnownCreatures", Tag.TAG_COMPOUND).getCompound(0);
                        helper.assertTrue(individual.getLong("CreatureMost") == goblin.getUUID().getMostSignificantBits()
                                        && individual.getLong("CreatureLeast") == goblin.getUUID().getLeastSignificantBits(),
                                "The alarm saved the named Hobgoblin's UUID before loading");
                        fetish.load(bound);
                    }).thenWaitUntil(() -> helper.assertFalse(lampLit(helper), "The saved individual binding clears the alarm"))
                    .thenExecute(() -> witch.setCustomName(Component.literal("Named Coven Witch")))
                    .thenWaitUntil(() -> helper.assertTrue(lampLit(helper), "The previous group has no named witch binding"))
                    .thenSucceed();
        });
    }

    @GameTest(template = TEMPLATE, batch = "witchery_screamer_alarm", setupTicks = 5, timeoutTicks = 160)
    public static void screamerUsesLegacySpeciesAndAlarmControls(GameTestHelper helper) {
        floor(helper);
        var level = helper.getLevel();
        helper.setBlock(CENTER, WitcheryBlocks.getRequired("scarecrow").get());
        helper.setBlock(CENTER.east(), Blocks.REDSTONE_LAMP);
        var fetish = (FetishBlockEntity) helper.getBlockEntity(CENTER);
        var data = alarm(FetishEffect.SCREAMER);
        var pigGroup = new CompoundTag();
        pigGroup.putString("CreatureTypeName", "Pig");
        var creatures = new ListTag();
        creatures.add(pigGroup);
        data.put("KnownCreatureTypes", creatures);
        fetish.load(data);
        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(
                level.isPositionEntityTicking(helper.absolutePos(CENTER.south(3)))
                        && level.isPositionEntityTicking(helper.absolutePos(CENTER.north(3))),
                "The two alarm fixture positions accept ticking entities"))
                .thenExecute(() -> exerciseScreamer(helper, fetish));
    }

    private static void exerciseScreamer(GameTestHelper helper, FetishBlockEntity fetish) {
        var level = helper.getLevel();
        var pig = placeMob(helper, EntityType.PIG, CENTER.south(3));
        var soul = placeMob(helper, WitcheryEntities.LOST_SOUL.get(), CENTER.east(3));
        var player = actor(helper, "FetishTester");

        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(
                level.getEntity(pig.getUUID()) == pig && level.getEntity(soul.getUUID()) == soul,
                "The alarm can query the trusted pig and the Lost Soul")).thenIdle(24).thenExecute(() -> {
            helper.assertFalse(lampLit(helper), "The legacy Pig group and the ignored Lost Soul keep the lamp off");
        }).thenExecute(() -> placeMob(helper, EntityType.SHEEP, CENTER.north(3)))
                .thenWaitUntil(() -> helper.assertTrue(lampLit(helper), "The unlisted sheep lights the lamp"))
                .thenExecute(() -> {
                    var sheep = level.getEntitiesOfClass(Sheep.class,
                            new AABB(helper.absolutePos(CENTER.north(3))).inflate(1));
                    helper.assertTrue(sheep.size() == 1, "The unlisted sheep stays within the alarm");
                    var sample = new ItemStack(WitcheryItems.getRequired("taglockkit").get());
                    TaglockItem.bindSignature(sample, sheep.get(0));
                    use(helper, player, sample);
                    helper.assertTrue(sample.isEmpty(), "A filled Taglock Kit adds the sheep group");
                    var saved = fetish.saveWithoutMetadata();
                    helper.assertTrue(saved.getList("KnownCreatureTypes", Tag.TAG_COMPOUND).size() == 2,
                            "Both groups are present in the saved alarm");
                    fetish.load(saved);
                }).thenWaitUntil(() -> helper.assertFalse(lampLit(helper), "The saved trusted groups leave the lamp off"))
                .thenExecute(() -> {
                    level.getEntitiesOfClass(Sheep.class, new AABB(helper.absolutePos(CENTER.north(3))).inflate(1))
                            .forEach(Sheep::discard);
                    use(helper, player, new ItemStack(Items.WATER_BUCKET));
                    var washed = fetish.saveWithoutMetadata();
                    helper.assertTrue(player.getMainHandItem().is(Items.BUCKET)
                                    && washed.getList("KnownCreatureTypes", Tag.TAG_COMPOUND).isEmpty()
                                    && washed.getInt("AlarmMode") == 2,
                            "The wash clears the groups, preserves the alarm mode, and returns the empty bucket");
                }).thenWaitUntil(() -> helper.assertTrue(pig.isAlive() && lampLit(helper),
                        "The original pig lights the lamp after the list is cleared"))
                .thenExecute(() -> {
                    pig.discard();
                    soul.discard();
                }).thenSucceed();
    }

    @GameTest(template = TEMPLATE, batch = "witchery_bind_witchs_ladder", setupTicks = 5, timeoutTicks = 400)
    public static void witchsLadderBindsBansheesThroughTheMediumRite(GameTestHelper helper) {
        floor(helper);
        var level = helper.getLevel();
        var center = helper.absolutePos(CENTER);
        helper.setBlock(CENTER, WitcheryBlocks.getRequired("circle").get());
        var glyph = WitcheryBlocks.getRequired("circleglyphritual").get();
        for (BlockPos offset : RitualCircle.MEDIUM_RITE.offsets()) helper.setBlock(CENTER.offset(offset), glyph);
        var altar = chargedAltar(helper);
        var spirits = List.of(CENTER.offset(-2, 0, 2), CENTER.south(2), CENTER.offset(2, 0, 2),
                CENTER.offset(-1, 0, -2), CENTER.offset(1, 0, -2));
        var bystander = CENTER.east(3);
        var ladder = WitcheryBlocks.getRequired("witchsladder").get().asItem();
        var offering = new ItemStack(ladder);
        offering.getOrCreateTag().putByte("BlockColor", (byte) 14);
        var stone = new ItemStack(WitcheryIngredient.NECROMANTIC_STONE.item());
        stone.setHoverName(Component.literal("Circle keeper"));
        var boline = new ItemStack(WitcheryItems.getRequired("boline").get());
        boline.setDamageValue(9);

        // Spawn the offerings after the destination chunks can accept ticking entities.
        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(
                level.isPositionEntityTicking(center) && level.isPositionEntityTicking(helper.absolutePos(bystander))
                        && spirits.stream().map(helper::absolutePos).allMatch(level::isPositionEntityTicking),
                "The circle and its offerings are in entity-ticking chunks")).thenExecute(() -> {
            var needed = List.of(
                    placeMob(helper, WitcheryEntities.SPIRIT.get(), spirits.get(0)),
                    placeMob(helper, WitcheryEntities.SPIRIT.get(), spirits.get(1)),
                    placeMob(helper, WitcheryEntities.LOST_SOUL.get(), spirits.get(2)),
                    placeMob(helper, WitcheryEntities.BANSHEE.get(), spirits.get(3)),
                    placeMob(helper, WitcheryEntities.BANSHEE.get(), spirits.get(4)));
            var unrelated = placeMob(helper, WitcheryEntities.POLTERGEIST.get(), bystander);
            offer(helper, offering, new ItemStack(WitcheryIngredient.ATTUNED_STONE.item()), stone, boline);
            helper.useBlock(CENTER);
            helper.assertTrue(helper.getBlockState(CENTER).getValue(RiteHeartBlock.ACTIVE), "The medium circle starts the binding rite");
            var heart = (RiteHeartBlockEntity) helper.getBlockEntity(CENTER);
            // Advance the glyph within one server tick so altar recharge cannot race the debit.
            tickHeart(heart, 60);
            heart.load(heart.saveWithoutMetadata());
            tickHeart(heart, 20);
            float beforeCharge = altar.power();
            tickHeart(heart, 20);
            helper.assertTrue(altar.power() == beforeCharge - 6_000
                            && heart.saveWithoutMetadata().getBoolean("AltarPowerOffered"),
                    "The altar contributes 6,000 while the sacrificed spirits remain alive");
            helper.assertTrue(needed.stream().allMatch(Mob::isAlive), "The binding waits until its next step");

            tickHeart(heart, 20);
            var items = level.getEntitiesOfClass(ItemEntity.class, new AABB(center).inflate(6));
            var drop = items.stream().filter(item -> item.getItem().is(ladder)).findFirst().orElseThrow();
            var bound = drop.getItem();
            helper.assertTrue(FetishBlockEntity.itemEffect(bound) == FetishEffect.SCREAMER.id
                            && FetishBlockEntity.itemColor(bound) == 14 && !drop.hasPickUpDelay(),
                    "The saved rite returns the dyed Screamer Ladder with immediate pickup");
            helper.assertTrue(altar.power() == beforeCharge - 6_000, "The binding uses the earlier payment");
            helper.assertTrue(needed.stream().allMatch(Mob::isRemoved) && unrelated.isAlive(),
                    "The rite consumes two Dream Spirits, a Lost Soul, and two Banshees and leaves the Poltergeist");
            helper.assertTrue(items.size() == 3 && items.stream().anyMatch(item -> ItemStack.matches(item.getItem(), stone))
                            && items.stream().anyMatch(item -> ItemStack.matches(item.getItem(), boline)),
                    "The rite also returns the original named Necromantic Stone and worn Boline");
            unrelated.discard();
            items.forEach(ItemEntity::discard);
            helper.succeed();
        });
    }

    private static void tickHeart(RiteHeartBlockEntity heart, int ticks) {
        for (int tick = 0; tick < ticks; tick++) {
            RiteHeartBlockEntity.serverTick(heart.getLevel(), heart.getBlockPos(), heart.getBlockState(), heart);
        }
    }

    @GameTest(template = TEMPLATE, batch = "witchery_sentinel_target", setupTicks = 5, timeoutTicks = 120)
    public static void sentinelGuardsKeepTheirIntruderAndRespectTheTrustedPlayer(GameTestHelper helper) {
        floor(helper);
        var level = helper.getLevel();
        var center = helper.absolutePos(CENTER);
        helper.setBlock(CENTER, WitcheryBlocks.getRequired("trent").get());
        var fetish = (FetishBlockEntity) helper.getBlockEntity(CENTER);
        var owner = actor(helper, "SentinelKeeper");
        owner.setPos(Vec3.atBottomCenterOf(helper.absolutePos(CENTER.south(5))));
        level.addNewPlayer(owner);
        var data = alarm(FetishEffect.SENTINEL);
        long lastActivation = level.getGameTime() - FetishEffect.SENTINEL.cooldown - 1;
        data.putLong("LastActivation", lastActivation);
        var trusted = new CompoundTag();
        trusted.putString("PlayerName", owner.getGameProfile().getName());
        var players = new ListTag();
        players.add(trusted);
        data.put("KnownPlayers", players);
        fetish.load(data);
        var pig = placeMob(helper, EntityType.PIG, CENTER.south(3));
        // Keep the intruder alive without making it ineligible for the target goal.
        pig.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);
        pig.setHealth(pig.getMaxHealth());
        var guardArea = new AABB(center).inflate(8);
        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(fetish.saveWithoutMetadata().getLong("LastActivation") != lastActivation,
                "The loaded fetish detects the intruding pig on its scheduled tick")).thenExecute(() -> {
            var guards = level.getEntitiesOfClass(SpectralUndead.class, guardArea);
            try {
                helper.assertTrue(guards.size() == 2 && guards.stream().allMatch(guard -> guard.temporary() && guard.getTarget() == pig),
                        "The Sentinel summons exactly two temporary guards targeting the pig: "
                                + guards.stream().map(guard -> guard + " target=" + guard.getTarget()).toList());
                // The dedicated test server may load chunks before ticking their entities.
                for (int tick = 0; tick < 12; tick++) {
                    guards.forEach(level::tickNonPassenger);
                    helper.assertTrue(guards.stream().allMatch(guard -> guard.isAlive() && guard.getTarget() == pig),
                            "Both guards stay on the pig while the trusted player is nearby");
                }
                for (var guard : guards) {
                    var saved = guard.saveWithoutId(new CompoundTag());
                    helper.assertTrue(saved.getInt("SuicideIn") > 550 && saved.getUUID("SentinelTarget").equals(pig.getUUID()),
                            "The guard saves its remaining lifetime and actual intruder");
                    guard.setTarget(null);
                    guard.load(saved);
                }
                for (int tick = 0; tick < 12; tick++) {
                    guards.forEach(level::tickNonPassenger);
                    helper.assertTrue(guards.stream().allMatch(guard -> guard.isAlive() && guard.getTarget() == pig),
                            "Reloaded guards restore the intruder and continue to ignore the trusted player");
                }
            } finally {
                owner.discard();
                pig.discard();
            }
            guards.forEach(level::tickNonPassenger);
            helper.assertTrue(guards.stream().allMatch(SpectralUndead::isRemoved), "The guards vanish with their target");
        }).thenSucceed();
    }

    @GameTest(template = TEMPLATE, batch = "witchery_sentinel_column", timeoutTicks = 120)
    public static void sentinelUsesTheNorthwestColumnAndItsOriginalHeadroom(GameTestHelper helper) {
        var level = helper.getLevel();
        var bases = List.of(CENTER, CENTER.above(20), CENTER.above(40));
        var floors = List.of(-4, 3, 10);
        var intruders = new ArrayList<Mob>();
        for (int index = 0; index < bases.size(); index++) {
            BlockPos pigPos = bases.get(index);
            BlockPos column = pigPos.north().west();
            for (BlockPos pos : BlockPos.betweenClosed(column.below(4), column.above(12))) {
                helper.setBlock(pos, Blocks.AIR);
            }
            for (BlockPos pos : BlockPos.betweenClosed(pigPos.offset(-1, -1, -1), pigPos.offset(1, -1, 1))) {
                helper.setBlock(pos, Blocks.STONE);
            }
            helper.setBlock(column.below(), Blocks.AIR);
            int floor = floors.get(index);
            if (floor < 0) {
                helper.setBlock(column.above(floor), Blocks.STONE);
            } else {
                for (int y = 0; y <= floor; y++) helper.setBlock(column.above(y), Blocks.STONE);
                if (floor == 3) helper.setBlock(column.above(6), Blocks.STONE);
            }
            helper.setBlock(pigPos.north(4), WitcheryBlocks.getRequired("trent").get());
            intruders.add(placeMob(helper, EntityType.PIG, pigPos));
        }

        BlockPos firstAlarm = helper.absolutePos(CENTER.north(4));
        helper.startSequence().thenWaitUntil(() -> helper.assertTrue((level.getGameTime() + firstAlarm.asLong()) % 20 == 0
                        && intruders.stream().allMatch(pig -> level.getEntity(pig.getUUID()) == pig),
                "The pigs are tracked and the alarms are due to tick")).thenExecute(() -> {
            for (int index = 0; index < bases.size(); index++) {
                BlockPos pigPos = bases.get(index);
                BlockPos alarmPos = helper.absolutePos(pigPos.north(4));
                var fetish = (FetishBlockEntity) level.getBlockEntity(alarmPos);
                var data = alarm(FetishEffect.SENTINEL);
                data.putLong("LastActivation", level.getGameTime() - FetishEffect.SENTINEL.cooldown);
                fetish.load(data);
                var guardArea = new AABB(helper.absolutePos(pigPos)).inflate(8);
                FetishBlockEntity.tick(level, alarmPos, fetish.getBlockState(), fetish);
                helper.assertTrue(level.getEntitiesOfClass(SpectralUndead.class, guardArea).isEmpty(),
                        "The exact 600-tick cooldown boundary still suppresses the alarm");

                data.putLong("LastActivation", level.getGameTime() - FetishEffect.SENTINEL.cooldown - 1);
                fetish.load(data);
                FetishBlockEntity.tick(level, alarmPos, fetish.getBlockState(), fetish);
                var guards = level.getEntitiesOfClass(SpectralUndead.class, guardArea);
                int floor = floors.get(index);
                if (floor == 10) {
                    helper.assertTrue(guards.isEmpty(), "The blocked column prevents spawning even with neighboring floors");
                } else {
                    BlockPos column = helper.absolutePos(pigPos.north().west()).above(floor);
                    Vec3 spawn = Vec3.atBottomCenterOf(column).add(0, 1.05, 0);
                    Mob pig = intruders.get(index);
                    helper.assertTrue(guards.size() == 2 && guards.stream().allMatch(guard -> guard.getTarget() == pig
                                    && guard.position().distanceToSqr(spawn) < 1.0E-6 && guard.getBbHeight() == 1.8F),
                            "Two original-sized Spectres appear at " + spawn + ": " + guards);
                }
                guards.forEach(SpectralUndead::discard);
                intruders.get(index).discard();
            }
        }).thenSucceed();
    }

    private static CompoundTag alarm(FetishEffect effect) {
        var data = new CompoundTag();
        data.putInt("EffectTypeID", effect.id);
        data.putInt("AlarmMode", 2);
        return data;
    }

    private static boolean lampLit(GameTestHelper helper) {
        return helper.getBlockState(CENTER.east()).getValue(RedstoneLampBlock.LIT);
    }

    private static void use(GameTestHelper helper, FakePlayer player, ItemStack stack) {
        var pos = helper.absolutePos(CENTER);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        helper.getBlockState(CENTER).use(helper.getLevel(), player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false));
    }

    private static FakePlayer actor(GameTestHelper helper, String name) {
        return new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), name));
    }

    private static <T extends Mob> T placeMob(GameTestHelper helper, EntityType<T> type, BlockPos relative) {
        var level = helper.getLevel();
        var mob = type.create(level);
        mob.setPos(Vec3.atBottomCenterOf(helper.absolutePos(relative)));
        mob.setNoAi(true);
        mob.setPersistenceRequired();
        helper.assertTrue(level.addFreshEntity(mob), "The alarm fixture can spawn " + type.toShortString());
        return mob;
    }

    private static void floor(GameTestHelper helper) {
        for (var pos : BlockPos.betweenClosed(CENTER.offset(-6, -1, -6), CENTER.offset(10, -1, 6))) {
            helper.setBlock(pos, Blocks.STONE);
        }
    }

    private static void offer(GameTestHelper helper, ItemStack... stacks) {
        var center = Vec3.atBottomCenterOf(helper.absolutePos(CENTER));
        var level = helper.getLevel();
        for (var stack : stacks) {
            var item = new ItemEntity(level, center.x, center.y + 0.2, center.z, stack.copy(), 0, 0, 0);
            item.setNoGravity(true);
            item.setNeverPickUp();
            level.addFreshEntity(item);
        }
    }

    private static AltarBlockEntity chargedAltar(GameTestHelper helper) {
        var corner = CENTER.offset(7, 0, -2);
        var block = WitcheryBlocks.getRequired("altar").get();
        for (var pos : BlockPos.betweenClosed(corner, corner.offset(1, 0, 2))) helper.setBlock(pos, block);
        helper.setBlock(corner.above(), WitcheryBlocks.getRequired("infinityegg").get());
        var altar = (AltarBlockEntity) helper.getBlockEntity(corner);
        var saved = altar.saveWithoutMetadata();
        saved.putInt("MaxPower", 1000);
        saved.putInt("PowerScale", 10);
        saved.putFloat("Power", 8500);
        altar.load(saved);
        return altar;
    }
}
