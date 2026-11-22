package art.arcane.witchery.world;

import art.arcane.witchery.Witchery;
import com.mojang.authlib.GameProfile;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
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
        float rain = dream.getRainLevel(1);
        float thunder = dream.getThunderLevel(1);
        dream.setRainLevel(0.0F);
        dream.setThunderLevel(0.0F);
        dream.addNewPlayer(sleeper);
        try {
            dream.updateSkyBrightness();
            helper.assertTrue(dream.isDay() && dream.getSkyDarken() == 0 && dream.getTimeOfDay(1) == 0.0F,
                    "A normal Dream is bright enough for ordinary daylight mechanics");
            setNightmare(sleeper, true, false);
            dream.updateSkyBrightness();
            helper.assertTrue(dream.isNight() && dream.getSkyDarken() == 11 && dream.getTimeOfDay(1) == 0.5F,
                    "An active nightmare darkens gameplay lighting across the Dream");
            setNightmare(sleeper, true, true);
            dream.updateSkyBrightness();
            helper.assertTrue(DreamAtmosphere.mode(dream) == 2 && dream.isNight() && dream.getSkyDarken() == 11,
                    "A demonic nightmare stays dark and keeps its distinct atmosphere mode");
            setNightmare(sleeper, false, false);
            dream.updateSkyBrightness();
            helper.assertTrue(dream.isDay() && dream.getSkyDarken() == 0,
                    "Daylight returns after the last nightmare ends");
            ServerLevel overworld = helper.getLevel();
            helper.assertTrue(overworld.getTimeOfDay(1) == overworld.dimensionType().timeOfDay(overworld.dayTime()),
                    "Other dimensions keep their vanilla clock");
        } finally {
            dream.removePlayerImmediately(sleeper, Entity.RemovalReason.DISCARDED);
            dream.setRainLevel(rain);
            dream.setThunderLevel(thunder);
            dream.updateSkyBrightness();
        }
        helper.succeed();
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
