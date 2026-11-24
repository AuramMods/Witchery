package art.arcane.witchery.prediction;

import art.arcane.witchery.Witchery;
import com.mojang.authlib.GameProfile;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class PredictionSaveTests {
    @GameTest(template = "qa_coven_arena")
    public static void readingsKeepRegisteredLegacyForecasts(GameTestHelper helper) {
        List<String> messages = new ArrayList<>();
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "SavedFortune")) {
            @Override
            public void sendSystemMessage(Component component) {
                if (component.getContents() instanceof TranslatableContents translated) {
                    messages.add(translated.getKey());
                }
            }
        };
        var iron = forecast(5, 1_200L);
        iron.putString("FixtureMarker", "original");
        var coal = forecast(17, 1_600L);
        var entries = store(player, forecast(999, 1_000L), iron, coal, iron.copy());
        Predictions.attune(player);

        Predictions.read(player, player);

        helper.assertTrue(List.copyOf(entries).equals(List.of(iron, coal, iron)),
                "A reading discards obsolete IDs and keeps the original records");
        helper.assertTrue(messages.equals(List.of("witchery.prediction.iron", "witchery.prediction.coal")),
                "A reading reports each registered forecast once");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void obsoleteForecastsCannotPreventNewReadings(GameTestHelper helper) {
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "NewFortune"));
        var entries = store(player, forecast(999, 1_000L));
        player.tickCount = 100;
        Predictions.tick(player);
        helper.assertTrue(entries.isEmpty(), "The player tick clears obsolete forecast IDs");

        entries.add(forecast(999, 1_000L));
        Predictions.attune(player);
        Predictions.read(player, player);
        helper.assertTrue(entries.size() == 1 && Predictions.Kind.byId(entries.getCompound(0).getInt("WITCPreID")) != null
                        && entries.getCompound(0).getLong("WITCPreTime") > 1_000L,
                "An obsolete forecast cannot occupy the Crystal Ball's reading slot");
        helper.succeed();
    }

    private static CompoundTag forecast(int id, long time) {
        var reading = new CompoundTag();
        reading.putInt("WITCPreID", id);
        reading.putLong("WITCPreTime", time);
        return reading;
    }

    private static ListTag store(Player player, CompoundTag... stored) {
        var entries = new ListTag();
        entries.addAll(List.of(stored));
        var prediction = new CompoundTag();
        prediction.put("WITCPreList", entries);
        var data = new CompoundTag();
        data.put("WITCPredict", prediction);
        player.getPersistentData().put(Player.PERSISTED_NBT_TAG, data);
        return entries;
    }
}
