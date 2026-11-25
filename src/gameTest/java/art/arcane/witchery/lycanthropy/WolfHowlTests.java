package art.arcane.witchery.lycanthropy;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.capability.WitcheryPlayerDataProvider;
import art.arcane.witchery.registry.WitcheryEffects;
import art.arcane.witchery.registry.WitcherySounds;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.PlayLevelSoundEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class WolfHowlTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_howl")
    public static void howlPlaysTheOriginalCooldownCue(GameTestHelper helper) {
        var level = helper.getLevel();
        var at = helper.absolutePos(new BlockPos(6, 4, 5));
        var player = playerAt(helper, at, 7, 2);
        var pig = helper.spawn(EntityType.PIG, new BlockPos(7, 4, 5));
        helper.startSequence().thenWaitUntil(() ->
                helper.assertTrue(level.getEntitiesOfClass(Pig.class, player.getBoundingBox().inflate(16)).contains(pig),
                        "The test pig is tracked near the player"))
                .thenExecute(() -> checkHowlCues(helper, player, pig))
                .thenSucceed();
    }

    private static void checkHowlCues(GameTestHelper helper, FakePlayer player, Pig pig) {
        record Cue(SoundEvent sound, float volume, float pitch) {}
        var sounds = new ArrayList<Cue>();
        Consumer<PlayLevelSoundEvent.AtEntity> listener = event -> {
            if (event.getEntity() == player && event.getSound() != null) {
                sounds.add(new Cue(event.getSound().value(), event.getNewVolume(), event.getNewPitch()));
            }
        };
        MinecraftForge.EVENT_BUS.addListener(listener);
        try {
            player.addEffect(new MobEffectInstance(WitcheryEffects.PARALYSIS.get(), 20, 4));
            WolfTrials.howl(player);
            helper.assertTrue(sounds.isEmpty() && !pig.hasEffect(WitcheryEffects.PARALYSIS.get()),
                    "A fully paralyzed Wolfman cannot howl on the server");
            player.removeEffect(WitcheryEffects.PARALYSIS.get());

            WolfTrials.howl(player);
            helper.assertTrue(pig.hasEffect(WitcheryEffects.PARALYSIS.get()), "A successful Wolfman howl paralyzes the pig");
            pig.removeEffect(WitcheryEffects.PARALYSIS.get());
            WolfTrials.howl(player);

            helper.assertTrue(sounds.size() == 2 && sounds.get(0).sound() == WitcherySounds.WOLFMAN_HOWL.get()
                            && sounds.get(0).volume() == 1 && sounds.get(1).sound() == SoundEvents.NOTE_BLOCK_SNARE.value()
                            && sounds.get(1).volume() == 0.5F
                            && sounds.stream().allMatch(cue -> cue.pitch() >= 0.33F && cue.pitch() <= 0.5F),
                    "A successful howl and blocked repeat play the original pitches and cues");
            helper.assertTrue(!pig.hasEffect(WitcheryEffects.PARALYSIS.get()), "The blocked repeat does not reapply paralysis");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(listener);
        }
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_howl")
    public static void howledPackNeedsTwoClearBlocks(GameTestHelper helper) {
        var level = helper.getLevel();
        var at = helper.absolutePos(new BlockPos(8, 4, 5));
        for (BlockPos pos : BlockPos.betweenClosed(at.offset(-6, 0, -6), at.offset(6, 0, 6))) {
            level.setBlockAndUpdate(pos.below(), Blocks.STONE.defaultBlockState());
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            level.setBlockAndUpdate(pos.above(), Blocks.STONE.defaultBlockState());
        }
        var first = playerAt(helper, at, 8, 1);
        var second = playerAt(helper, at, 8, 1);
        var pack = new ArrayList<Wolf>();
        Consumer<EntityJoinLevelEvent> listener = event -> {
            if (event.getLevel() == level && event.getEntity() instanceof Wolf wolf
                    && (first.getUUID().equals(wolf.getOwnerUUID()) || second.getUUID().equals(wolf.getOwnerUUID()))) {
                pack.add(wolf);
            }
        };
        MinecraftForge.EVENT_BUS.addListener(listener);
        try {
            WolfTrials.howl(first);
            helper.assertTrue(pack.isEmpty(), "The legacy pack cannot spawn in a one-block-high tunnel");

            for (BlockPos pos : BlockPos.betweenClosed(at.offset(-6, 1, -6), at.offset(6, 1, 6))) {
                level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            }
            WolfTrials.howl(second);
            helper.assertTrue(!pack.isEmpty() && pack.size() <= 2
                            && pack.stream().allMatch(wolf -> second.getUUID().equals(wolf.getOwnerUUID())
                            && wolf.hasEffect(WitcheryEffects.MORTAL_COIL.get())
                            && wolf.getPersistentData().getBoolean("WITCNoDrops")),
                    "An open rank-eight howl summons up to two owned wolves with the original lifetime and no rewards");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(listener);
            pack.forEach(Wolf::discard);
        }
        helper.succeed();
    }

    private static FakePlayer playerAt(GameTestHelper helper, BlockPos at, int rank, int form) {
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "HowlTester"));
        player.setPos(Vec3.atBottomCenterOf(at));
        player.setShiftKeyDown(true);
        player.setXRot(-90);
        var data = WitcheryPlayerDataProvider.get(player).resolve().orElseThrow();
        data.setWerewolfLevel(rank);
        data.setCreatureTypeOrdinal(form);
        return player;
    }
}
