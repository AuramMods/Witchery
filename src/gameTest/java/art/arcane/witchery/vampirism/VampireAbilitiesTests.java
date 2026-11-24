package art.arcane.witchery.vampirism;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.capability.WitcheryPlayerDataProvider;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryEffects;
import art.arcane.witchery.registry.WitcheryEntities;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class VampireAbilitiesTests {
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
