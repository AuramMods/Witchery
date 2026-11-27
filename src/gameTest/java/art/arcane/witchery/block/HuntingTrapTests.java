package art.arcane.witchery.block;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.entity.HuntingTrapBlockEntity;
import art.arcane.witchery.entity.Wolfman;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryEffects;
import art.arcane.witchery.registry.WitcheryEntities;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class HuntingTrapTests {
    private static final Field LEVEL_RANDOM = ObfuscationReflectionHelper.findField(Level.class, "f_46441_");

    @GameTest(template = "qa_coven_arena", batch = "witchery_wolf_trap_bait")
    public static void savedBaitUsesTheLoadedTrapClockAndOriginalLandingColumn(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(7, 2, 7)).atY(260);
        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "TrapBait"));
        var sheep = EntityType.SHEEP.create(level);
        helper.assertTrue(sheep != null, "Create the bait sheep");
        sheep.moveTo(pos.getX() + 2.5, pos.getY(), pos.getZ() + 0.5, 0, 0);
        sheep.setNoAi(true);
        sheep.setNoGravity(true);
        sheep.setLeashedTo(player, false);
        level.setBlockAndUpdate(pos.east(), WitcheryBlocks.getRequired("wolfaltar").get().defaultBlockState());
        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(level.isPositionEntityTicking(pos), "The bait chunk has activated"))
                .thenExecute(() -> level.addFreshEntity(sheep))
                .thenWaitUntil(() -> helper.assertTrue(level.getEntity(sheep.getUUID()) == sheep, "The bait is searchable"))
                .thenExecute(() -> verifyLure(helper, pos, player)).thenSucceed();
    }

    private static void verifyLure(GameTestHelper helper, BlockPos pos, FakePlayer player) {
        var level = helper.getLevel();
        var trap = trap(level, pos, "wolftrap");
        var floor = pos.offset(32, -1, -32);
        for (int y = 0; y < 12; y++) level.setBlockAndUpdate(floor.above(y), Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(floor, Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(floor.above(2), Blocks.STONE.defaultBlockState());
        var arrivals = new ArrayList<Wolfman>();
        Consumer<EntityJoinLevelEvent> observe = event -> {
            if (event.getLevel() == level && event.getEntity() instanceof Wolfman wolf) arrivals.add(wolf);
        };
        var daytime = level.getDayTime();
        var previousRandom = level.random;
        MinecraftForge.EVENT_BUS.addListener(observe);
        try {
            level.setDayTime(18000);
            level.updateSkyBrightness();
            trap.toggle(player);
            age(level, trap, 199);
            helper.assertTrue(trap.saveWithoutMetadata().getLong("WolftrapStart") == 0, "No bait sample before loaded tick 200");
            age(level, trap, 1);
            long baitTime = trap.saveWithoutMetadata().getLong("WolftrapStart");
            helper.assertTrue(baitTime == level.getGameTime() && baitTime > 0, "Loaded tick 200 records the original bait time");
            trap.toggle(player);
            trap.toggle(player);
            CompoundTag saved = trap.saveWithoutMetadata();
            helper.assertTrue(saved.getLong("WolftrapStart") == baitTime, "Disarming and rearming retain the bait timer");

            var reloaded = trap(level, pos, "wolftrap");
            saved.putLong("WolftrapStart", baitTime - 1);
            reloaded.load(saved);
            var random = new LureRolls();
            LEVEL_RANDOM.set(level, random);
            age(level, reloaded, 199);
            helper.assertTrue(random.bounds.isEmpty(), "Reopening the tile starts a new 200-tick poll");
            age(level, reloaded, 1);
            helper.assertTrue(random.bounds.equals(List.of(33, 33)) && arrivals.isEmpty(),
                    "One clear cell fails the original search and consumes only the two offset rolls");
            helper.assertTrue(reloaded.saveWithoutMetadata().getLong("WolftrapStart") == baitTime - 1,
                    "No clearance leaves the saved bait timestamp available for the next poll");

            for (int dy = 1; dy <= 9; dy++) level.setBlockAndUpdate(floor.above(dy), Blocks.STONE.defaultBlockState());
            random.bounds.clear();
            age(level, reloaded, 200);
            helper.assertTrue(random.bounds.equals(List.of(33, 33)) && arrivals.size() == 1,
                    "The next poll finds a landing floor at the original eight-block climb limit");
            Wolfman wolf = arrivals.get(0);
            helper.assertTrue(wolf.getX() == floor.getX() + 0.5 && Math.abs(wolf.getY() - (pos.getY() + 9.05)) < 1e-6
                            && wolf.getZ() == floor.getZ() + 0.5 && wolf.isPersistenceRequired(),
                    "The summoned Wolfman uses the original coordinates and persists");
            var lure = reloaded.saveWithoutMetadata();
            helper.assertTrue(new UUID(lure.getLong("WolfMost"), lure.getLong("WolfLeast")).equals(wolf.getUUID()),
                    "The lure persists the UUID of the one summoned Wolfman");
            var stranger = WitcheryEntities.WOLFMAN.get().create(level);
            helper.assertTrue(stranger != null, "Create the unrelated Wolfman control");
            lure.putLong("ArmedAt", level.getGameTime() - 21);
            reloaded.load(lure);
            reloaded.tryCatch(stranger);
            helper.assertFalse(reloaded.sprung() || stranger.isInfectious(), "A different Wolfman cannot spring the trap");
            reloaded.tryCatch(wolf);
            helper.assertTrue(reloaded.sprung() && wolf.isInfectious()
                            && reloaded.saveWithoutMetadata().getLong("WolftrapStart") == baitTime - 1,
                    "Capture infects the correct Wolfman and keeps the original saved bait time");
            wolf.discard();
        } catch (IllegalAccessException ex) {
            throw new IllegalStateException(ex);
        } finally {
            try {
                LEVEL_RANDOM.set(level, previousRandom);
            } catch (IllegalAccessException ex) {
                throw new IllegalStateException(ex);
            } finally {
                MinecraftForge.EVENT_BUS.unregister(observe);
                level.setDayTime(daytime);
                level.updateSkyBrightness();
            }
        }
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_bear_trap_arming")
    public static void bearTrapCatchesOnTickTwentyOneAndOnlyInsideTheJaws(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(7, 2, 7));
        var bear = WitcheryBlocks.getRequired("beartrap").get().defaultBlockState();
        level.setBlockAndUpdate(pos, bear);
        var trap = (HuntingTrapBlockEntity) level.getBlockEntity(pos);
        var pig = EntityType.PIG.create(level);
        helper.assertTrue(trap != null && pig != null, "Create a bear trap and pig");
        var record = new CompoundTag();
        record.putBoolean("Sprung", false);
        record.putLong("ArmedAt", level.getGameTime() - 20);
        trap.load(record);
        pig.moveTo(pos.getX() + 0.5, pos.getY() + 0.05, pos.getZ() + 0.5, 0, 0);
        bear.entityInside(level, pos, pig);
        helper.assertFalse(trap.sprung(), "Touching the jaws on tick twenty stays within the arming grace");
        record.putLong("ArmedAt", level.getGameTime() - 21);
        trap.load(record);
        pig.moveTo(pos.getX() + 0.5, pos.getY() + 0.11, pos.getZ() + 0.5, 0, 0);
        bear.entityInside(level, pos, pig);
        helper.assertFalse(trap.sprung(), "Passing above the low jaws does not activate the trap");
        pig.moveTo(pos.getX() + 0.5, pos.getY() + 0.05, pos.getZ() + 0.5, 0, 0);
        bear.entityInside(level, pos, pig);
        var paralysed = pig.getEffect(WitcheryEffects.PARALYSIS.get());
        helper.assertTrue(trap.sprung() && pig.getHealth() == 6 && paralysed != null && paralysed.getAmplifier() == 2
                        && paralysed.getDuration() == 600 && paralysed.isAmbient(),
                "Tick twenty-one activates the jaws, four damage, and thirty seconds of Paralysis III");
        helper.succeed();
    }

    private static HuntingTrapBlockEntity trap(ServerLevel level, BlockPos pos, String id) {
        var state = WitcheryBlocks.getRequired(id).get().defaultBlockState();
        var trap = new HuntingTrapBlockEntity(pos, state);
        trap.setLevel(level);
        return trap;
    }

    private static void age(ServerLevel level, HuntingTrapBlockEntity trap, int ticks) {
        for (int i = 0; i < ticks; i++) HuntingTrapBlockEntity.serverTick(level, trap.getBlockPos(), trap.getBlockState(), trap);
    }

    private static final class LureRolls extends LegacyRandomSource {
        private final List<Integer> bounds = new ArrayList<>();

        private LureRolls() { super(0L); }

        @Override
        public int nextInt(int bound) {
            bounds.add(bound);
            return bounds.size() == 1 ? 32 : 0;
        }
    }
}
