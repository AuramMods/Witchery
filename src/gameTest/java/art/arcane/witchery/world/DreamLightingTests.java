package art.arcane.witchery.world;

import art.arcane.witchery.Witchery;
import com.mojang.authlib.GameProfile;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.LegacyRandomSource;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.Objects;
import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class DreamLightingTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_dream_lighting")
    public static void sharedNightmareControlsGameplaySkylight(GameTestHelper helper) {
        ServerLevel dream = Objects.requireNonNull(helper.getLevel().getServer().getLevel(WitcheryDimensions.DREAM));
        var sleeper = new FakePlayer(dream, new GameProfile(UUID.randomUUID(), "DreamLighting"));
        var demonic = new FakePlayer(dream, new GameProfile(UUID.randomUUID(), "DemonicLighting"));
        float rain = dream.getRainLevel(1);
        float thunder = dream.getThunderLevel(1);
        dream.setRainLevel(0.0F);
        dream.setThunderLevel(0.0F);
        dream.addNewPlayer(sleeper);
        dream.addNewPlayer(demonic);
        var update = weatherRoll(0);
        var skip = weatherRoll(1);
        try {
            DreamAtmosphere.sample(dream, update);
            dream.updateSkyBrightness();
            helper.assertTrue(dream.isDay() && dream.getSkyDarken() == 0 && dream.getTimeOfDay(1) == 0.0F,
                    "A normal Dream is bright enough for ordinary daylight mechanics");
            setNightmare(sleeper, true, false);
            setNightmare(demonic, true, true);
            helper.assertTrue(!DreamAtmosphere.sample(dream, skip) && dream.isDay(),
                    "The world keeps its current lighting until the weather roll succeeds");
            helper.assertTrue(DreamAtmosphere.sample(dream, update) && DreamAtmosphere.mode(dream) == 1
                            && dream.isNight() && dream.getSkyDarken() == 11 && dream.getTimeOfDay(1) == 0.5F,
                    "The first ordinary nightmare takes priority over the later demonic nightmare");
            helper.assertFalse(DreamTravel.isDemonicNightmare(dream), "Demons use the shared world mode");
            setNightmare(sleeper, false, false);
            helper.assertTrue(DreamAtmosphere.sample(dream, update) && DreamAtmosphere.mode(dream) == 2
                            && DreamTravel.isDemonicNightmare(dream) && dream.getSkyDarken() == 11,
                    "The next dreamer supplies the demonic mode once the earlier nightmare ends");
            setNightmare(demonic, false, false);
            DreamAtmosphere.sample(dream, update);
            helper.assertTrue(dream.isDay() && dream.getSkyDarken() == 0,
                    "Daylight returns after the last nightmare ends");
            ServerLevel overworld = helper.getLevel();
            helper.assertTrue(overworld.getTimeOfDay(1) == overworld.dimensionType().timeOfDay(overworld.dayTime()),
                    "Other dimensions keep their vanilla clock");
        } finally {
            dream.removePlayerImmediately(sleeper, Entity.RemovalReason.DISCARDED);
            dream.removePlayerImmediately(demonic, Entity.RemovalReason.DISCARDED);
            dream.setRainLevel(rain);
            dream.setThunderLevel(thunder);
            DreamAtmosphere.sample(dream, update);
            dream.updateSkyBrightness();
        }
        helper.succeed();
    }

    private static LegacyRandomSource weatherRoll(int value) {
        return new LegacyRandomSource(0) {
            @Override
            public int nextInt(int bound) {
                if (bound != 20) throw new AssertionError("Expected the original Dream weather roll bounds");
                return value;
            }
        };
    }

    private static void setNightmare(FakePlayer sleeper, boolean nightmare, boolean demonic) {
        CompoundTag persistent = sleeper.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
        CompoundTag dream = persistent.getCompound("WITCSpiritWorld");
        dream.putBoolean("Nightmare", nightmare);
        dream.putBoolean("Demonic", demonic);
        persistent.put("WITCSpiritWorld", dream);
        sleeper.getPersistentData().put(Player.PERSISTED_NBT_TAG, persistent);
    }
}
