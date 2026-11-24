package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.capability.WitcheryPlayerData.WolfmanQuestState;
import art.arcane.witchery.capability.WitcheryPlayerDataProvider;
import com.mojang.authlib.GameProfile;
import net.minecraft.SharedConstants;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.entity.player.Player;
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
            long now = System.currentTimeMillis();
            var wolfFile = TagParser.parseTag("""
                    {Dimension:0,WitcheryExtendedPlayer:{CreatureType:1,WerewolfLevel:6,WolfmanQuestState:1,
                     WolfmanQuestCounter:9,WolfmanQuestChunks:[{Location:%dL}],PotionBottling:32,PotionThrowing:57,
                     LastHowl:%dL,LastBoneFind:%dL}}
                    """.formatted(visitedChunk, now, now - 61_000));
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
            helper.assertTrue(wolfData.howlCoolingDown(10_000L) && !wolfData.boneFindCoolingDown(10_000L),
                    "An original wall-clock cooldown is independent of the current world tick");
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
                            && data.getCreatureTypeOrdinal() == 1 && !data.markWolfmanQuestChunk(-3, 4)
                            && data.serializeNBT().getInt("PotionThrowing") == 57
                            && data.serializeNBT().getLong("LastHowl") == now
                            && data.serializeNBT().getLong("LastBoneFind") == now - 61_000).orElse(false),
                    "The modern save must keep the next trial step and original form and visited chunk");
        } finally {
            FileUtils.deleteDirectory(root.toFile());
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void importsMirrorEntryAndEscapeTimesWithoutReplacingNewerState(GameTestHelper helper) throws Exception {
        var root = Files.createTempDirectory("witchery-player-mirror-import");
        try (var access = LevelStorageSource.createDefault(root).createAccess("world")) {
            var world = access.getLevelPath(LevelResource.ROOT);
            NbtIo.writeCompressed(TagParser.parseTag("{Dimensions:{mirror:64}}"),
                    world.resolve("witchery-legacy-ids.dat").toFile());
            var storage = access.createPlayerStorage();
            var files = access.getLevelPath(LevelResource.PLAYER_DATA_DIR);
            var original = new GameProfile(UUID.randomUUID(), "LegacyMirror");
            var upgraded = new GameProfile(UUID.randomUUID(), "ModernMirror");
            var outside = new GameProfile(UUID.randomUUID(), "ExitedMirror");
            var legacy = TagParser.parseTag("""
                    {Dimension:64,WitcheryExtendedPlayer:{MirrorWorldEntryPoint:{posX:-22,posY:48,posZ:39},
                     MirrorEscape1:6412L,MirrorEscape2:-9223372036854775808L}}
                    """);
            var modern = legacy.copy();
            modern.putInt("DataVersion", SharedConstants.getCurrentVersion().getDataVersion().getVersion());
            modern.putString("Dimension", "witchery:mirror");
            modern.put("ForgeData", TagParser.parseTag("""
                    {PlayerPersisted:{MirrorWorldEntryPoint:{posX:17,posY:104,posZ:-6},MirrorEscape1:8420L,OwnerMarker:1b}}
                    """));
            modern.put("ForgeCaps", TagParser.parseTag("""
                    {"witchery:player_data":{LastHowlGameTime:500L,LastBoneFindGameTime:550L}}
                    """));
            var exited = legacy.copy();
            exited.putInt("Dimension", 0);
            NbtIo.writeCompressed(legacy, files.resolve(original.getId() + ".dat").toFile());
            NbtIo.writeCompressed(modern, files.resolve(upgraded.getId() + ".dat").toFile());
            NbtIo.writeCompressed(exited, files.resolve(outside.getId() + ".dat").toFile());

            var occupant = new FakePlayer(helper.getLevel(), original);
            storage.load(occupant);
            var saved = occupant.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
            var entrance = saved.getCompound("MirrorWorldEntryPoint");
            helper.assertTrue(entrance.getInt("posX") == -22 && entrance.getInt("posY") == 48 && entrance.getInt("posZ") == 39
                            && saved.getLong("MirrorEscape1") == 6412 && !saved.contains("MirrorEscape2"),
                    "The custom legacy dimension restores the exact room and an actual cooldown");
            storage.save(occupant);
            var reloaded = new FakePlayer(helper.getLevel(), original);
            storage.load(reloaded);
            helper.assertTrue(saved.equals(reloaded.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG)),
                    "The migrated room and cooldown last across a normal save");

            var modernOccupant = new FakePlayer(helper.getLevel(), upgraded);
            storage.load(modernOccupant);
            var modernSaved = modernOccupant.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
            helper.assertTrue(modernSaved.getBoolean("OwnerMarker") && modernSaved.getLong("MirrorEscape1") == 8420
                            && modernSaved.getCompound("MirrorWorldEntryPoint").getInt("posY") == 104,
                    "An existing modern room and cooldown take precedence over the old copy");
            var modernData = WitcheryPlayerDataProvider.get(modernOccupant).orElseThrow(IllegalStateException::new);
            helper.assertTrue(modernData.howlCoolingDown(1699) && !modernData.howlCoolingDown(1700)
                            && modernData.boneFindCoolingDown(1749) && !modernData.boneFindCoolingDown(1750),
                    "An existing port save honors the remainder of both game-tick cooldowns");
            var formerOccupant = new FakePlayer(helper.getLevel(), outside);
            storage.load(formerOccupant);
            var formerState = formerOccupant.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
            helper.assertTrue(!formerState.contains("MirrorWorldEntryPoint") && formerState.getLong("MirrorEscape1") == 6412,
                    "A former occupant retains the cooldown without reviving the last room");
        } finally {
            FileUtils.deleteDirectory(root.toFile());
        }
        helper.succeed();
    }
}
