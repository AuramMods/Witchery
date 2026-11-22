package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.capability.WitcheryPlayerData.WolfmanQuestState;
import art.arcane.witchery.capability.WitcheryPlayerDataProvider;
import com.mojang.authlib.GameProfile;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import org.apache.commons.io.FileUtils;

import java.nio.file.Files;
import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class LegacyPlayerSaveImportTests {
    @GameTest(template = "qa_coven_arena")
    public static void restoresLegacyPlayerRanksAndTrials(GameTestHelper helper) throws Exception {
        var root = Files.createTempDirectory("witchery-player-import-test");
        try (var access = LevelStorageSource.createDefault(root).createAccess("world")) {
            var world = access.getLevelPath(LevelResource.ROOT);
            NbtIo.writeCompressed(new CompoundTag(), world.resolve("witchery-legacy-ids.dat").toFile());
            var storage = access.createPlayerStorage();
            var playerDirectory = access.getLevelPath(LevelResource.PLAYER_DATA_DIR);
            var wolfProfile = new GameProfile(UUID.randomUUID(), "LegacyWolf");
            var vampireProfile = new GameProfile(UUID.randomUUID(), "LegacyVampire");
            long visitedChunk = ((long) -3 << 32) | 4;
            var wolfFile = TagParser.parseTag("""
                    {Dimension:0,WitcheryExtendedPlayer:{CreatureType:1,WerewolfLevel:6,WolfmanQuestState:1,
                     WolfmanQuestCounter:9,WolfmanQuestChunks:[{Location:%dL}],PotionBottling:32}}
                    """.formatted(visitedChunk));
            var vampireFile = TagParser.parseTag("""
                    {Dimension:0,WitcheryExtendedPlayer:{CreatureType:3,VampireLevel:8,VampireLevelCap:10,
                     VampireQuestCounter:5,VampireQuestChunks:[{Location:%dL}],BloodPower:1200,BloodReserve:34,
                     HumanBlood:21,VampireVision:1b}}
                    """.formatted(visitedChunk));
            NbtIo.writeCompressed(wolfFile, playerDirectory.resolve(wolfProfile.getId() + ".dat").toFile());
            NbtIo.writeCompressed(vampireFile, playerDirectory.resolve(vampireProfile.getId() + ".dat").toFile());

            var wolf = new FakePlayer(helper.getLevel(), wolfProfile);
            var vampire = new FakePlayer(helper.getLevel(), vampireProfile);
            storage.load(wolf);
            storage.load(vampire);
            var wolfData = WitcheryPlayerDataProvider.get(wolf).orElseThrow(() -> new IllegalStateException("Missing Wolf player data"));
            var vampireData = WitcheryPlayerDataProvider.get(vampire).orElseThrow(() -> new IllegalStateException("Missing Vampire player data"));
            helper.assertTrue(wolfData.getWerewolfLevel() == 6 && wolfData.getCreatureTypeOrdinal() == 1
                            && wolfData.getWolfmanQuestState() == WolfmanQuestState.STARTED && wolfData.getWolfmanQuestCounter() == 9
                            && wolfData.getPotionBottling() == 32 && !wolfData.markWolfmanQuestChunk(-3, 4),
                    "Import the Wolf form, trial, visited chunk, and brew skill through Forge player storage");
            helper.assertTrue(vampireData.getVampireLevel() == 8 && vampireData.getCreatureTypeOrdinal() == 3
                            && vampireData.getVampireLevelCap() == 10 && vampireData.getVampireQuestCounter() == 5
                            && vampireData.getBloodPower() == 1200 && vampireData.getBloodReserve() == 34
                            && vampireData.getHumanBlood() == 21 && vampireData.hasVampireVision()
                            && !vampireData.visitVampireVillage("minecraft:overworld", -3, 4),
                    "Import the Vampire form, quests, and blood through Forge player storage");

            wolfData.advanceWolfmanQuest();
            storage.save(wolf);
            var reloadedWolf = new FakePlayer(helper.getLevel(), wolfProfile);
            storage.load(reloadedWolf);
            helper.assertTrue(WitcheryPlayerDataProvider.get(reloadedWolf).map(data -> data.getWolfmanQuestCounter() == 10
                            && data.getCreatureTypeOrdinal() == 1 && !data.markWolfmanQuestChunk(-3, 4)).orElse(false),
                    "The modern save must keep the next trial step and original form and visited chunk");
        } finally {
            FileUtils.deleteDirectory(root.toFile());
        }
        helper.succeed();
    }
}
