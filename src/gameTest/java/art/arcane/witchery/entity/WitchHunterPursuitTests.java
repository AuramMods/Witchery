package art.arcane.witchery.entity;

import art.arcane.witchery.Witchery;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class WitchHunterPursuitTests {
    private static final String TRIGGER = "WITCHunterTrigger";

    @GameTest(template = "qa_coven_arena", batch = "witchery_hunter_notice_migration", timeoutTicks = 220)
    public static void hunterNoticesUseWallClockAndDispatchOnTheWorldPulse(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos site = helper.absolutePos(new BlockPos(9, 160, 9));
        for (BlockPos pos : BlockPos.betweenClosed(site.offset(-8, -1, -8), site.offset(8, -1, 8))) {
            level.setBlockAndUpdate(pos, Blocks.STONE.defaultBlockState());
        }
        var imported = player(level, site, "ImportedNotice");
        var unripe = player(level, site, "UnripeNotice");
        var previous = player(level, site, "OldForgeNotice");
        long oldNotice = System.currentTimeMillis() / 50 - 3_000;
        notice(imported).putLong(TRIGGER, oldNotice);
        long seed = 0;
        while (RandomSource.create(seed).nextDouble() >= 0.01) seed++;
        long dispatchSeed = seed;

        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(level.getGameTime() % 100 != 1,
                "Wait outside the shared world pulse")).thenExecute(() -> {
            // An older implementation follows the player's ticks since login.
            imported.tickCount = 1;
            level.getRandom().setSeed(dispatchSeed);
            imported.getRandom().setSeed(dispatchSeed);
            WitchHunterPursuit.tick(imported);
            helper.assertTrue(notice(imported).getLong(TRIGGER) == oldNotice,
                    "An eligible original notice stays pending between world pulses");
        }).thenWaitUntil(() -> helper.assertTrue(level.getGameTime() % 100 == 1 && level.getGameTime() > 1,
                "Wait for the shared pursuit pulse")).thenExecute(() -> {
            long freshNotice = System.currentTimeMillis() / 50 - 2_000;
            notice(unripe).putLong(TRIGGER, freshNotice);
            level.getRandom().setSeed(dispatchSeed);
            WitchHunterPursuit.tick(unripe);
            helper.assertTrue(notice(unripe).getLong(TRIGGER) == freshNotice, "An original notice waits at least two minutes");

            long gameTime = level.getServer().overworld().getGameTime();
            long elapsed = Math.min(gameTime - 1, 350);
            notice(previous).putLong(TRIGGER, gameTime - elapsed);
            long before = System.currentTimeMillis() / 50;
            WitchHunterPursuit.tick(previous);
            long normalized = notice(previous).getLong(TRIGGER);
            helper.assertTrue(normalized >= before - elapsed && normalized <= System.currentTimeMillis() / 50 - elapsed,
                    "The previous port's notice carries its elapsed world ticks into the wall-clock timer");

            var arrivals = new ArrayList<WitchHunter>();
            Consumer<EntityJoinLevelEvent> watch = event -> {
                if (event.getLevel() == level && event.getEntity() instanceof WitchHunter hunter) arrivals.add(hunter);
            };
            MinecraftForge.EVENT_BUS.addListener(watch);
            try {
                imported.tickCount = 38;
                level.getRandom().setSeed(dispatchSeed);
                WitchHunterPursuit.tick(imported);
                helper.assertTrue(!notice(imported).contains(TRIGGER) && arrivals.size() == 2
                                && arrivals.stream().allMatch(hunter -> hunter.getTarget() == imported
                                        && Math.abs(hunter.getY() - site.getY() - 0.05) < 0.001),
                        "Two correctly targeted hunters arrive just above the floor on the world pulse");
            } finally {
                MinecraftForge.EVENT_BUS.unregister(watch);
                arrivals.forEach(WitchHunter::discard);
                imported.discard();
                unripe.discard();
                previous.discard();
            }
        }).thenSucceed();
    }

    private static FakePlayer player(ServerLevel level, BlockPos pos, String name) {
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), name)) {
            @Override
            public void playNotifySound(SoundEvent sound, SoundSource source, float volume, float pitch) {}
        };
        player.moveTo(Vec3.atBottomCenterOf(pos));
        CompoundTag root = player.getPersistentData();
        root.put(Player.PERSISTED_NBT_TAG, new CompoundTag());
        return player;
    }

    private static CompoundTag notice(FakePlayer player) {
        return player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
    }
}
