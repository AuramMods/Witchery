package art.arcane.witchery.block;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.entity.AltarBlockEntity;
import art.arcane.witchery.block.entity.BarrierBlockEntity;
import art.arcane.witchery.block.entity.RiteHeartBlockEntity;
import art.arcane.witchery.item.WaystoneItem;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryItems;
import art.arcane.witchery.ritual.RitualCircle;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestSequence;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
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
    // Each required offering, the optional Waystone, and the altar charge get a separate 20-tick step.
    private static final int TWO_OFFERING_CAST = 80;
    private static final int THREE_OFFERING_CAST = 100;

    @GameTest(template = TEMPLATE, batch = "witchery_weather_rite_eclipse")
    public static void portableEclipseFiresOnlyInDaylight(GameTestHelper helper) {
        floor(helper);
        ring(helper, RitualCircle.SMALL_RITE, "circleglyphritual");
        var level = helper.getLevel();
        var center = helper.absolutePos(CENTER);
        var heart = heart(helper);
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

                level.setDayTime(54_000);
                level.updateSkyBrightness();
                helper.useBlock(CENTER);
                tick(level, center, heart, TWO_OFFERING_CAST);
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
        heart(helper).load(legacy);

        helper.runAfterDelay(20, () -> {
            helper.assertBlockPresent(block("perpetualice"), CENTER.east(16));
            helper.assertBlockPresent(Blocks.STONE, CENTER.west(16));
            helper.assertBlockPresent(Blocks.AIR, CENTER.east(17));
            helper.assertFalse(helper.getBlockState(CENTER).getValue(RiteHeartBlock.ACTIVE),
                    "The migrated shell finishes at the six-member radius");
            helper.succeed();
        });
    }

    @GameTest(template = TEMPLATE, batch = "witchery_summoned_witch", setupTicks = 5, timeoutTicks = 155)
    public static void witchSummoningReturnsTheSameDamagedArthana(GameTestHelper helper) {
        floor(helper);
        ring(helper, RitualCircle.SMALL_RITE, "circleglyphinfernal");
        var altar = chargedAltar(helper, true);
        var charge = altar.saveWithoutMetadata();
        charge.putFloat("Power", 5000);
        altar.load(charge);
        var arthana = new ItemStack(WitcheryItems.getRequired("arthana").get());
        arthana.setDamageValue(13);
        start(helper, new ItemStack(WitcheryIngredient.DIAMOND_VAPOUR.item()),
                new ItemStack(WitcheryIngredient.EXHALE_OF_THE_HORNED_ONE.item()), new ItemStack(Items.MAGMA_CREAM),
                new ItemStack(Items.FERMENTED_SPIDER_EYE), arthana);
        helper.runAfterDelay(110, () -> {
            float beforeCharge = altar.power();
            int recharge = 10 * altar.rechargeMultiplier();
            // One altar recharge occurs in the twenty ticks that bracket the closing rite step.
            helper.runAfterDelay(20, () -> {
                var witches = helper.getLevel().getEntitiesOfClass(Witch.class, new AABB(helper.absolutePos(CENTER)).inflate(8));
                helper.assertTrue(witches.size() == 1, "The small Infernal circle summons exactly one witch");
                helper.assertTrue(altar.power() == beforeCharge - 2000 + recharge, "The summoning spends exactly 2,000 power");
                var returned = drops(helper);
                helper.assertTrue(returned.size() == 1 && ItemStack.matches(returned.get(0).getItem(), arthana),
                        "The summoning returns the original damaged Arthana, found "
                                + returned.stream().map(drop -> drop.getItem().serializeNBT()).toList());
                witches.forEach(Entity::discard);
                returned.forEach(ItemEntity::discard);
                helper.succeed();
            });
        });
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
        helper.assertTrue(!heart.saveWithoutMetadata().contains("Storm") && altar.power() == 8000
                        && drops(helper).size() == 3 && drops(helper).stream().anyMatch(item -> ItemStack.matches(item.getItem(), tooFar)),
                "An out-of-range Waystone returns the three offerings after charging altar power");
        drops(helper).forEach(ItemEntity::discard);

        var target = center.east(40).above(3);
        start(helper, new ItemStack(Items.STONE_SWORD), new ItemStack(WitcheryIngredient.WOOD_ASH.item()), bound(level, target));
        tick(level, center, heart, TWO_OFFERING_CAST);
        var storm = heart.saveWithoutMetadata().getCompound("Storm");
        helper.assertTrue(storm.getBoolean("Greater") && BlockPos.of(storm.getLong("Target")).equals(target)
                && altar.power() == 6000, "The valid Waystone relocates the greater storm, including its height");
        var strikes = new ArrayList<LightningBolt>();
        var area = new AABB(target).inflate(9);
        Consumer<EntityJoinLevelEvent> observe = event -> {
            if (event.getLevel() == level && event.getEntity() instanceof LightningBolt bolt && area.contains(bolt.position())) {
                strikes.add(bolt);
            }
        };
        MinecraftForge.EVENT_BUS.addListener(observe);
        try {
            tick(level, center, heart, 30);
            helper.assertTrue(strikes.size() == 1 && strikes.get(0).getY() == target.getY(),
                    "The actual strike arrives in the remote chunk");
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
            var altar = chargedAltar(helper, false);
            start(helper);
            tick(level, center, heart, TWO_OFFERING_CAST + 1);
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
                    && Math.abs(altar.power() - 9498.8F) < 0.01F, "The first lease is thirty ticks and upkeep spends 1.2");
            helper.useBlock(CENTER);
            helper.assertTrue(!level.getBlockState(center).getValue(RiteHeartBlock.ACTIVE)
                    && heart.saveWithoutMetadata().getList("ProtectionRites", Tag.TAG_COMPOUND).isEmpty(),
                    "Manual cancellation clears the heart without returning completed offerings");
            for (int tick = 0; tick < 30; tick++) BarrierBlockEntity.tick(level, wall, state, wallEntity);
            helper.assertTrue(level.isEmptyBlock(wall), "The last leased barrier expires after thirty ticks");
            cow.discard();
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
            tick(level, center, heart, TWO_OFFERING_CAST);
            var save = heart.saveWithoutMetadata();
            var ward = save.getList("ProtectionRites", Tag.TAG_COMPOUND).getCompound(0);
            helper.assertTrue(BlockPos.of(ward.getLong("Target")).equals(target) && ward.getString("Kind").equals("GREATER_BARRIER"),
                    "The same-dimension Waystone relocates a greater barrier");
            heart.load(save);
            tick(level, center, heart, 1);
            var wall = target.west(6);
            var state = level.getBlockState(wall);
            var player = fake(level);
            helper.assertTrue(state.is(block("barrier")) && level.getBlockState(target.above(6)).is(block("barrier"))
                    && level.getBlockState(target.below()).is(block("barrier")) && !level.getBlockState(center.above(6)).is(block("barrier")),
                    "The reloaded ward refreshes the entire remote shell around the original target");
            helper.assertTrue(!state.getCollisionShape(level, wall, CollisionContext.of(player)).isEmpty()
                    && Math.abs(altar.power() - 8998.6F) < 0.01F,
                    "The remote ward spends 1.4 from the altar beside the heart and blocks survival players");
            player.getAbilities().instabuild = true;
            player.setGameMode(GameType.CREATIVE);
            player.setShiftKeyDown(true);
            helper.assertTrue(state.getCollisionShape(level, wall, CollisionContext.of(player)).isEmpty(),
                    "Creative crouching bypasses the wall");
            helper.useBlock(CENTER);
        }).thenSucceed();
    }

    @GameTest(template = TEMPLATE, batch = "witchery_volcano_refund", setupTicks = 30, timeoutTicks = 130)
    public static void distantDryVolcanoRefundsTheWaystoneAndOfferings(GameTestHelper helper) {
        floor(helper);
        ring(helper, RitualCircle.SMALL_RITE, "circleglyphinfernal");
        var altar = chargedAltar(helper, true);
        var target = CENTER.south(13);
        helper.setBlock(target.below(), Blocks.BEDROCK);
        var stone = bound(helper, target);
        start(helper, new ItemStack(Items.COBBLESTONE), new ItemStack(Items.MAGMA_CREAM), new ItemStack(Items.GOLDEN_SWORD), stone);
        helper.runAfterDelay(THREE_OFFERING_CAST + 5, () -> {
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
        helper.runAfterDelay(THREE_OFFERING_CAST + 50, () -> {
            var heart = heart(helper);
            var saved = heart.saveWithoutMetadata();
            var state = saved.getCompound("Landscape");
            helper.assertTrue(state.getInt("Stage") >= 4 && state.getInt("Stage") < 8
                    && BlockPos.of(state.getLong("Target")).equals(helper.absolutePos(target)), "The volcano should be midway through growing");
            helper.assertTrue(altar.power() < 9900, "The cone construction follows the 2,000-power debit");
            heart.load(saved);
        });
        helper.runAfterDelay(THREE_OFFERING_CAST + 230, () -> {
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
        BlockPos corner = CENTER.east(9);
        for (var pos : BlockPos.betweenClosed(corner, corner.offset(1, 0, 2))) helper.setBlock(pos, block("altar"));
        if (stabilizedPower) helper.setBlock(corner.offset(1, 1, 2), block("infinityegg"));
        var altar = (AltarBlockEntity) helper.getBlockEntity(corner);
        var data = altar.saveWithoutMetadata();
        data.putFloat("Power", 10_000);
        data.putInt("PowerScale", 10);
        data.putInt("MaxPower", 1000);
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
            var entity = new ItemEntity(level, point.x, point.y, point.z, item.copy(), 0, 0, 0);
            entity.setNeverPickUp();
            entity.setNoGravity(true);
            level.addFreshEntity(entity);
        }
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
