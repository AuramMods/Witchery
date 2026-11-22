package art.arcane.witchery.block;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.capability.WitcheryPlayerDataProvider;
import art.arcane.witchery.config.WitcheryServerConfig;
import art.arcane.witchery.registry.WitcheryBlocks;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class DreamWeaveSleepTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_dreamweave_sleep")
    public static void dreamweavesRespectSleepThresholdAndWakeTime(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos catcher = new BlockPos(5, 3, 5);
        helper.setBlock(catcher.south(), Blocks.STONE);
        helper.setBlock(catcher, WitcheryBlocks.getRequired("dreamcatcher").get());
        var mortal = new Sleeper(level, "DaySleeper");
        var vampire = new Sleeper(level, "NightSleeper");
        mortal.setPos(Vec3.atBottomCenterOf(helper.absolutePos(catcher.east(2))));
        vampire.setPos(Vec3.atBottomCenterOf(helper.absolutePos(catcher.west(2))));
        level.addNewPlayer(mortal);
        level.addNewPlayer(vampire);
        WitcheryPlayerDataProvider.get(vampire).orElseThrow(IllegalStateException::new).setVampireLevel(1);

        var threshold = WitcheryServerConfig.PERCENTAGE_OF_PLAYERS_SLEEPING_FOR_BUFF;
        int previousThreshold = threshold.get();
        long previousTime = level.getDayTime();
        try {
            threshold.set(100);
            transition(level, mortal, 18000, 24000);
            helper.assertTrue(!mortal.hasEffect(MobEffects.MOVEMENT_SPEED) && !vampire.hasEffect(MobEffects.MOVEMENT_SPEED),
                    "One of two players sleeping does not meet the original default threshold");

            threshold.set(50);
            transition(level, mortal, 18000, 24000);
            helper.assertTrue(mortal.hasEffect(MobEffects.MOVEMENT_SPEED) && !vampire.hasEffect(MobEffects.MOVEMENT_SPEED),
                    "Dawn grants the movement weave only to nearby mortals when the threshold is met");
            mortal.removeEffect(MobEffects.MOVEMENT_SPEED);

            transition(level, vampire, 6000, 13000);
            helper.assertTrue(vampire.hasEffect(MobEffects.MOVEMENT_SPEED) && !mortal.hasEffect(MobEffects.MOVEMENT_SPEED),
                    "Dusk grants the movement weave only to nearby vampires");
        } finally {
            threshold.set(previousThreshold);
            level.setDayTime(previousTime);
            level.updateSkyBrightness();
            level.removePlayerImmediately(mortal, Entity.RemovalReason.DISCARDED);
            level.removePlayerImmediately(vampire, Entity.RemovalReason.DISCARDED);
            DreamCatcherBlock.tickSleep(level);
        }
        helper.succeed();
    }

    private static void transition(ServerLevel level, Sleeper sleeper, long sleepTime, long wakeTime) {
        level.setDayTime(sleepTime);
        level.updateSkyBrightness();
        sleeper.resting = true;
        DreamCatcherBlock.tickSleep(level);
        level.setDayTime(wakeTime);
        level.updateSkyBrightness();
        sleeper.resting = false;
        DreamCatcherBlock.tickSleep(level);
    }

    private static final class Sleeper extends FakePlayer {
        private boolean resting;

        private Sleeper(ServerLevel level, String name) {
            super(level, new GameProfile(UUID.randomUUID(), name));
        }

        @Override
        public boolean isSleeping() {
            return resting;
        }
    }
}
