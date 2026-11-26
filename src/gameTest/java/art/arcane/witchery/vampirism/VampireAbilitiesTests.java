package art.arcane.witchery.vampirism;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.capability.WitcheryPlayerDataProvider;
import art.arcane.witchery.entity.AttackBat;
import art.arcane.witchery.event.WitcheryEventHooks;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryEffects;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.world.WitcheryDimensions;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class VampireAbilitiesTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_vampire_storm_weather")
    public static void thunderstormUsesTheOriginalWeatherRulesInEveryDimension(GameTestHelper helper) {
        var server = helper.getLevel().getServer();
        var overworld = server.overworld();
        var weather = server.getWorldData().overworldData();
        int clearTime = weather.getClearWeatherTime();
        int rainTime = weather.getRainTime();
        int thunderTime = weather.getThunderTime();
        boolean raining = weather.isRaining();
        boolean thundering = weather.isThundering();
        try {
            var dimensions = List.of(Level.OVERWORLD, Level.NETHER, Level.END,
                    WitcheryDimensions.DREAM, WitcheryDimensions.TORMENT, WitcheryDimensions.MIRROR);
            for (var dimension : dimensions) {
                var level = server.getLevel(dimension);
                helper.assertTrue(level != null, "The original dimension is loaded: " + dimension.location());
                overworld.setWeatherParameters(4000, 0, false, false);
                int remaining = castThunderstorm(level, false);
                int duration = weather.getRainTime();
                helper.assertTrue(remaining == 2, "A clear-weather cast always spends one charge: " + dimension.location());
                if (dimension.equals(Level.OVERWORLD)) {
                    helper.assertTrue(weather.isRaining() && weather.isThundering() && weather.getClearWeatherTime() == 0
                                    && weather.getThunderTime() == duration && duration >= 6000 && duration <= 17980 && duration % 20 == 0,
                            "The Overworld cast starts both types of weather for the original rolled duration");
                } else {
                    helper.assertTrue(!weather.isRaining() && !weather.isThundering()
                                    && duration == 0 && weather.getThunderTime() == 0 && weather.getClearWeatherTime() == 4000,
                            "The derived dimension spends the charge and leaves the Overworld weather alone: " + dimension.location());
                }

                overworld.setWeatherParameters(0, 1234, true, false);
                weather.setThunderTime(456);
                int kept = castThunderstorm(level, false);
                helper.assertTrue(kept == 3 && weather.isRaining() && !weather.isThundering()
                                && weather.getRainTime() == 1234 && weather.getThunderTime() == 456,
                        "A cast during rain leaves the weather and charges alone: " + dimension.location());
            }
            overworld.setWeatherParameters(4000, 0, false, false);
            helper.assertTrue(castThunderstorm(overworld, true) == 3 && weather.isThundering(),
                    "Creative mode starts the Overworld storm without spending a charge");
        } finally {
            weather.setClearWeatherTime(clearTime);
            weather.setRainTime(rainTime);
            weather.setThunderTime(thunderTime);
            weather.setRaining(raining);
            weather.setThundering(thundering);
        }
        helper.succeed();
    }

    private static int castThunderstorm(ServerLevel level, boolean creative) {
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "VampireThunderstorm"));
        var data = WitcheryPlayerDataProvider.get(player).orElseThrow(IllegalStateException::new);
        player.getAbilities().instabuild = creative;
        data.setVampireLevel(10);
        data.setVampireUltimate(VampireUltimate.STORM, 3);
        VampireAbilities.select(player, VampireAbilities.ULTIMATE);
        VampireAbilities.activate(player);
        player.discard();
        return data.getVampireUltimateCharges();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_vampire_swarm_spawn")
    public static void swarmUsesOriginalColumnsAndSpendsBlockedCharge(GameTestHelper helper) {
        var world = helper.getLevel();
        var origin = helper.absolutePos(new BlockPos(5, 200, 5));
        var center = new BlockPos(Math.floorDiv(origin.getX(), 16) * 16 + 8, origin.getY(),
                Math.floorDiv(origin.getZ(), 16) * 16 + 8);
        var bounds = new AABB(center).inflate(15);
        for (BlockPos point : BlockPos.betweenClosed(center.offset(-4, -1, -4), center.offset(4, -1, 4))) {
            world.setBlockAndUpdate(point, Blocks.STONE.defaultBlockState());
        }
        var first = castSwarm(world, center);
        var bats = world.getEntitiesOfClass(AttackBat.class, bounds);
        helper.assertTrue(bats.size() == 15 && first == 2,
                "An open cast adds 15 bats and spends one of three charges. Bats: " + bats.size() + ", charges: " + first);
        for (AttackBat bat : bats) {
            helper.assertTrue(isSwarmOffset(bat.blockPosition().getX() - center.getX())
                            && isSwarmOffset(bat.blockPosition().getZ() - center.getZ())
                            && Math.abs(bat.getY() - center.getY() - 0.05) < 1e-6,
                    "Use the original seven offsets on each axis and release the bat just above the floor");
            bat.discard();
        }
        for (BlockPos point : BlockPos.betweenClosed(center.offset(-4, 0, -4), center.offset(4, 11, 4))) {
            world.setBlockAndUpdate(point, Blocks.STONE.defaultBlockState());
        }
        int raised = castSwarm(world, center);
        var raisedBats = world.getEntitiesOfClass(AttackBat.class, bounds);
        helper.assertTrue(raisedBats.size() == 15 && raised == 2, "The top of the eight-block scan can support the whole swarm");
        for (AttackBat bat : raisedBats) {
            helper.assertTrue(Math.abs(bat.getY() - center.getY() - 12.05) < 1e-6, "Release each bat above the raised floor");
            bat.discard();
        }
        for (BlockPos point : BlockPos.betweenClosed(center.offset(-4, 12, -4), center.offset(4, 13, 4))) {
            world.setBlockAndUpdate(point, Blocks.STONE.defaultBlockState());
        }
        int blocked = castSwarm(world, center);
        helper.assertTrue(world.getEntitiesOfClass(AttackBat.class, bounds).isEmpty() && blocked == 2,
                "A valid cast still spends a charge when every column lacks headroom");
        helper.succeed();
    }

    private static int castSwarm(ServerLevel world, BlockPos center) {
        var player = new FakePlayer(world, new GameProfile(UUID.randomUUID(), "VampireSwarm"));
        player.setPos(Vec3.atBottomCenterOf(center));
        var data = WitcheryPlayerDataProvider.get(player).orElseThrow(IllegalStateException::new);
        data.setVampireLevel(10);
        data.setVampireUltimate(VampireUltimate.SWARM, 3);
        VampireAbilities.select(player, VampireAbilities.ULTIMATE);
        VampireAbilities.activate(player);
        player.discard();
        return data.getVampireUltimateCharges();
    }

    private static boolean isSwarmOffset(int offset) {
        return offset >= -4 && offset <= 4 && offset != 0 && offset != 1;
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_vampire_swarm_life")
    public static void attackBatBitesAndRetainsItsRemainingOriginalLifetime(GameTestHelper helper) {
        var world = helper.getLevel();
        var contact = new BlockPos(5, 200, 5);
        var target = helper.spawn(EntityType.COW, contact);
        target.setNoAi(true);
        target.setNoGravity(true);
        var biter = helper.spawn(WitcheryEntities.ATTACK_BAT.get(), contact);
        biter.setNoAi(true);
        biter.setNoGravity(true);
        biter.setResting(false);
        float health = target.getHealth();
        biter.tick();
        helper.assertTrue(target.getHealth() == health - 4 && biter.isRemoved(),
                "A native contact deals four magic damage and removes the bat. Health: " + target.getHealth() + ", removed: " + biter.isRemoved());

        var original = WitcheryEntities.ATTACK_BAT.get().create(world);
        original.setPos(Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(6, 3, 6))));
        original.setNoAi(true);
        original.setNoGravity(true);
        var saved = original.saveWithoutId(new CompoundTag());
        helper.assertTrue(saved.getInt("SwarmTicks") == 301, "A new bat stays active through tick 300");
        saved.putInt("SwarmTicks", 2);
        var loaded = WitcheryEntities.ATTACK_BAT.get().create(world);
        loaded.load(saved);
        loaded.tick();
        helper.assertTrue(!loaded.isRemoved() && loaded.saveWithoutId(new CompoundTag()).getInt("SwarmTicks") == 1,
                "Reloaded bats keep their saved timer");
        loaded.tick();
        helper.assertTrue(loaded.isRemoved(), "Expire on the final saved tick");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void crouchingStrikeUsesTheOriginalPlayerAndMobMotion(GameTestHelper helper) {
        var level = helper.getLevel();
        var attacker = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "CrouchingStrike"));
        WitcheryPlayerDataProvider.get(attacker).orElseThrow(IllegalStateException::new).setVampireLevel(3);
        attacker.setPos(Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(2, 2, 1))));
        attacker.setYRot(0);
        attacker.setShiftKeyDown(true);
        var target = helper.spawn(EntityType.PIG, new BlockPos(2, 2, 3));
        target.setOnGround(true);
        target.setAbsorptionAmount(8);

        float health = target.getHealth();
        var strike = level.damageSources().playerAttack(attacker);
        target.hurt(strike, 1);
        helper.assertTrue(target.getHealth() == health && target.getAbsorptionAmount() == 7,
                "The strike spends absorption before damage reaches health");
        var ordinaryKnockback = new Vec3(0, 0.4, 1.3);
        helper.assertTrue(target.getDeltaMovement().distanceToSqr(ordinaryKnockback) < 1e-10,
                "Vanilla modifies the mob's launch as it did in Witchery 0.24.1");

        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "StrikeTarget"));
        player.setPos(target.position().add(1, 0, 0));
        level.addNewPlayer(player);
        try {
            WitcheryEventHooks.queueVampireCrouchingStrike(new LivingHurtEvent(player, strike, 1));
            player.setDeltaMovement(ordinaryKnockback);
            Vampirism.finishCrouchingStrikes(level);
            helper.assertTrue(player.getDeltaMovement().distanceToSqr(new Vec3(0, 0.9, 1.8)) < 1e-10,
                    "The player's full launch follows vanilla knockback");
        } finally {
            player.discard();
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_legacy_village_travel")
    public static void travelChecksTheOriginalVillageColumn(GameTestHelper helper) {
        var level = helper.getLevel();
        var center = helper.absolutePos(new BlockPos(5, 220, 5));
        var vampire = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "VillageTravel"));
        vampire.setPos(Vec3.atBottomCenterOf(center.south(4)));
        for (BlockPos point : BlockPos.betweenClosed(center.offset(-1, -15, -1), center.offset(1, 19, 1))) {
            level.setBlockAndUpdate(point, Blocks.AIR.defaultBlockState());
        }
        level.setBlockAndUpdate(center.below(), Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(center.above(3), Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(center.east().above(), Blocks.STONE.defaultBlockState());
        helper.assertTrue(Vec3.atLowerCornerOf(center.above(4)).equals(VampireUltimate.legacyVillageFooting(vampire, level, center)),
                "Travel picks the upper surface when the two village floors are equally close");
        level.setBlockAndUpdate(center.above(4), Blocks.STONE.defaultBlockState());
        helper.assertTrue(Vec3.atLowerCornerOf(center).equals(VampireUltimate.legacyVillageFooting(vampire, level, center)),
                "Obstructed headroom sends the vampire to the equally close lower floor first");
        for (int y = -15; y <= 19; y++) level.setBlockAndUpdate(center.above(y), Blocks.STONE.defaultBlockState());
        helper.assertTrue(VampireUltimate.legacyVillageFooting(vampire, level, center) == null,
                "A blocked village center prevents travel even with a safe surface in the next column");
        vampire.discard();
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void mesmerizesOrdinaryVillagersAndRejectsWerevillagers(GameTestHelper helper) {
        var level = helper.getLevel();
        var vampire = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "EyePower"));
        var data = WitcheryPlayerDataProvider.get(vampire).orElseThrow(IllegalStateException::new);
        data.setVampireLevel(2);
        data.setBloodPower(400);
        data.setSelectedVampirePower(VampireAbilities.MESMERIZE);

        var werevillager = WitcheryEntities.WERE_VILLAGER.get().create(level);
        VampireAbilities.interact(vampire, werevillager);
        helper.assertTrue(!werevillager.hasEffect(WitcheryEffects.PARALYSIS.get()) && data.getBloodPower() == 400,
                "An ordinary Mesmerize cannot paralyze a Werevillager or spend blood on it");

        var villager = EntityType.VILLAGER.create(level);
        VampireAbilities.interact(vampire, villager);
        var effect = villager.getEffect(WitcheryEffects.PARALYSIS.get());
        helper.assertTrue(effect != null && effect.getDuration() == 120 && effect.getAmplifier() == 4
                        && data.getBloodPower() == 350,
                "At rank two an ordinary villager receives six seconds of paralysis for 50 blood");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void activePowerReplacesBlockUseButAllowsMining(GameTestHelper helper) {
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "PowerSlot"));
        var data = WitcheryPlayerDataProvider.get(player).orElseThrow(IllegalStateException::new);
        data.setVampireLevel(2);
        data.setSelectedVampirePower(VampireAbilities.DRINK);
        BlockPos block = helper.absolutePos(new BlockPos(2, 1, 2));
        var hit = new BlockHitResult(Vec3.atCenterOf(block), Direction.UP, block, false);
        helper.assertTrue(MinecraftForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickBlock(player, InteractionHand.MAIN_HAND, block, hit)),
                "The selected vampire power replaces ordinary block use");
        helper.assertTrue(!MinecraftForge.EVENT_BUS.post(new PlayerInteractEvent.LeftClickBlock(player, block, Direction.UP,
                        PlayerInteractEvent.LeftClickBlock.Action.START)),
                "A selected vampire power still allows ordinary block mining");
        player.getInventory().selected = 1;
        helper.assertTrue(!MinecraftForge.EVENT_BUS.post(new PlayerInteractEvent.RightClickBlock(player, InteractionHand.MAIN_HAND, block, hit)),
                "A regular hotbar slot restores block use");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void garlicGarlandIgnitesVampireDuringOrdinaryMining(GameTestHelper helper) {
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "GarlandWard"));
        var data = WitcheryPlayerDataProvider.get(player).orElseThrow(IllegalStateException::new);
        BlockPos fixture = new BlockPos(2, 1, 2);
        helper.setBlock(fixture, WitcheryBlocks.getRequired("garlicgarland").get());
        BlockPos garland = helper.absolutePos(fixture);
        var bus = MinecraftForge.EVENT_BUS;
        var start = PlayerInteractEvent.LeftClickBlock.Action.START;
        helper.assertTrue(!bus.post(new PlayerInteractEvent.LeftClickBlock(player, garland, Direction.UP, start))
                && !player.isOnFire(), "The garland allows a mortal to mine");
        data.setVampireLevel(1);
        data.setSelectedVampirePower(VampireAbilities.DRINK);
        helper.assertTrue(!bus.post(new PlayerInteractEvent.LeftClickBlock(player, garland, Direction.UP, start))
                && !player.isOnFire(), "An active vampire power bypasses the garland's mining response");
        data.setSelectedVampirePower(0);
        helper.assertTrue(bus.post(new PlayerInteractEvent.LeftClickBlock(player, garland, Direction.UP, start))
                && player.getRemainingFireTicks() == 20, "The garland interrupts mining and ignites the vampire for one second");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void activePowerDisablesStoneShattering(GameTestHelper helper) {
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "StonePower"));
        var data = WitcheryPlayerDataProvider.get(player).orElseThrow(IllegalStateException::new);
        data.setVampireLevel(6);
        data.setSelectedVampirePower(VampireAbilities.DRINK);
        player.setShiftKeyDown(true);
        var stone = Blocks.STONE.defaultBlockState();
        var pos = helper.absolutePos(new BlockPos(2, 1, 2));
        var speed = new PlayerEvent.BreakSpeed(player, stone, 1, pos);
        var harvest = new PlayerEvent.HarvestCheck(player, stone, false);
        MinecraftForge.EVENT_BUS.post(speed);
        MinecraftForge.EVENT_BUS.post(harvest);
        helper.assertTrue(speed.getNewSpeed() == 1 && !harvest.canHarvest(),
                "An active power preserves ordinary bare-hand mining speed and drops");
        data.setSelectedVampirePower(0);
        speed = new PlayerEvent.BreakSpeed(player, stone, 1, pos);
        harvest = new PlayerEvent.HarvestCheck(player, stone, false);
        MinecraftForge.EVENT_BUS.post(speed);
        MinecraftForge.EVENT_BUS.post(harvest);
        helper.assertTrue(speed.getNewSpeed() >= 1_000 && harvest.canHarvest(),
                "Clearing the power restores instant shattering and drops");
        helper.succeed();
    }
}
