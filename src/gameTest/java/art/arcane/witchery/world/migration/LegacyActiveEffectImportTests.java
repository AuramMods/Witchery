package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryEffects;
import com.mojang.authlib.GameProfile;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.storage.EntityStorage;
import net.minecraft.world.level.chunk.storage.RegionFile;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import org.apache.commons.io.FileUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class LegacyActiveEffectImportTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_earlier_player_effects")
    public static void repairsExistingPlayerAndMountedEffects(GameTestHelper helper) throws Exception {
        var root = Files.createTempDirectory("witchery-player-active-effects");
        try (var access = LevelStorageSource.createDefault(root).createAccess("world")) {
            writeSnapshot(access.getLevelPath(LevelResource.ROOT));
            var storage = access.createPlayerStorage();
            var nativePlayer = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "NativeEffects"));
            var nativeRecord = TagParser.parseTag("""
                    {Health:20s,Dimension:0,ActiveEffects:[{Id:32b,Amplifier:1b,Duration:400},
                      {Id:33b,Amplifier:0b,Duration:600}]}
                    """);
            NbtIo.writeCompressed(nativeRecord, access.getLevelPath(LevelResource.PLAYER_DATA_DIR)
                    .resolve(nativePlayer.getStringUUID() + ".dat").toFile());
            storage.load(nativePlayer);
            helper.assertTrue(nativePlayer.hasEffect(WitcheryEffects.QUEASY.get()) && nativePlayer.hasEffect(WitcheryEffects.GAS_MASK.get()),
                    "Native bytes in slots 32 and 33 use the original Witchery assignments");

            var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "EarlierEffects"));
            var earlier = NbtUtils.addCurrentDataVersion(TagParser.parseTag("""
                    {Health:20f,Dimension:"minecraft:overworld",ActiveEffects:[
                      {Id:43b,Amplifier:2b,Duration:2400,Ambient:1b,ShowParticles:0b,ShowIcon:0b},
                      {Id:43b,"forge:id":"minecraft:fire_resistance",Amplifier:0b,Duration:1100},
                      {Id:1b,Amplifier:0b,Duration:800},{Id:32b,Duration:500},{Id:33,Duration:600}],
                     ForgeData:{PlayerPersisted:{WITCPoSpawn:[{Id:32b,Duration:900},{Id:33,Duration:950},
                       {Id:-85b,Duration:980}]}},
                     RootVehicle:{Attach:[I;4,3,2,1],Entity:{id:"minecraft:horse",UUID:[I;4,3,2,1],
                       ActiveEffects:[{Id:-85b,Amplifier:1b,Duration:1500}]}}}
                    """));
            NbtIo.writeCompressed(earlier, access.getLevelPath(LevelResource.PLAYER_DATA_DIR)
                    .resolve(player.getStringUUID() + ".dat").toFile());

            var loaded = storage.load(player);
            var paralysis = player.getEffect(WitcheryEffects.PARALYSIS.get());
            helper.assertTrue(paralysis != null && paralysis.getAmplifier() == 2 && paralysis.getDuration() == 2400
                            && paralysis.isAmbient() && !paralysis.isVisible() && !paralysis.showIcon(),
                    "Ordinary login repairs legacy Paralysis with its original details");
            helper.assertTrue(player.hasEffect(MobEffects.FIRE_RESISTANCE) && player.hasEffect(MobEffects.MOVEMENT_SPEED),
                    "Keep the named effect with the same numeric ID and the original vanilla speed effect");
            helper.assertTrue(player.hasEffect(MobEffects.HERO_OF_THE_VILLAGE) && player.hasEffect(MobEffects.DARKNESS)
                            && !player.hasEffect(WitcheryEffects.QUEASY.get()) && !player.hasEffect(WitcheryEffects.GAS_MASK.get()),
                    "A converted player keeps the vanilla effects that share original Witchery IDs");
            var pending = loaded.getCompound("ForgeData").getCompound("PlayerPersisted").getList("WITCPoSpawn", Tag.TAG_COMPOUND);
            helper.assertTrue(!pending.getCompound(0).contains("forge:id") && !pending.getCompound(1).contains("forge:id")
                            && pending.getCompound(2).getString("forge:id").equals("witchery:potion.sinking"),
                    "Keep the two vanilla pending effects and recover the remaining original byte");
            helper.assertTrue(player.getAttributeValue(Attributes.MOVEMENT_SPEED) == 0,
                    "The migrated Paralysis applies its movement modifier during login");
            var mount = EntityType.loadEntityRecursive(loaded.getCompound("RootVehicle").getCompound("Entity"),
                    helper.getLevel(), Function.identity());
            helper.assertTrue(mount instanceof LivingEntity horse && horse.hasEffect(WitcheryEffects.SINKING.get()),
                    "Repair the mount returned through the ordinary player storage path");

            storage.save(player);
            storage.load(player);
            helper.assertTrue(player.hasEffect(WitcheryEffects.PARALYSIS.get()) && player.hasEffect(MobEffects.FIRE_RESISTANCE),
                    "The resaved named effects keep their identity on the next login");
            helper.assertTrue(player.getAttributeValue(Attributes.MOVEMENT_SPEED) == 0,
                    "The ordinary attribute save retains the freeze on the next login");
        } finally {
            FileUtils.deleteDirectory(root.toFile());
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_earlier_chunk_effects")
    public static void repairsEffectsOnSavedEntityPassengers(GameTestHelper helper) throws Exception {
        var root = Files.createTempDirectory("witchery-chunk-active-effects");
        try {
            writeSnapshot(root);
            var directory = Files.createDirectories(root.resolve("entities"));
            var chunk = new ChunkPos(0, 0);
            var earlier = NbtUtils.addCurrentDataVersion(TagParser.parseTag("""
                    {Position:[I;0,0],Entities:[{id:"minecraft:boat",Pos:[4.5d,70d,4.5d],Passengers:[
                      {id:"minecraft:pig",Health:10f,ActiveEffects:[
                       {Id:-85b,Amplifier:2b,Duration:1600,ShowParticles:0b,
                        HiddenEffect:{Id:-85b,Amplifier:0b,Duration:2500,ShowParticles:1b}},
                       {Id:-85b,"forge:id":"minecraft:fire_resistance",Amplifier:0b,Duration:720},
                       {Id:32b,Duration:750},{Id:33,Duration:620}]}]},
                     {id:"minecraft:arrow",Pos:[6.5d,70d,6.5d],CustomPotionEffects:[
                       {Id:43b,Amplifier:1b,Duration:1000},{Id:32b,Duration:300}]},
                     {id:"minecraft:area_effect_cloud",Pos:[8.5d,70d,8.5d],Radius:2f,Duration:600,Effects:[
                       {Id:-85b,Amplifier:1b,Duration:1500},{Id:33,Duration:500}]}]}
                    """));
            try (var region = new RegionFile(directory.resolve("r.0.0.mca"), directory, true);
                 var stream = region.getChunkDataOutputStream(chunk)) {
                NbtIo.write(earlier, stream);
            }
            try (var storage = new EntityStorage(helper.getLevel(), directory, DataFixers.getDataFixer(), true, Runnable::run)) {
                var loaded = storage.loadEntities(chunk).join();
                var boat = loaded.getEntities().filter(entity -> entity.getType() == EntityType.BOAT).findFirst().orElseThrow();
                helper.assertTrue(boat.getFirstPassenger() instanceof LivingEntity, "The pig remains a passenger of the saved boat");
                var pig = (LivingEntity) boat.getFirstPassenger();
                var sinking = pig.getEffect(WitcheryEffects.SINKING.get());
                helper.assertTrue(sinking != null && sinking.getAmplifier() == 2 && sinking.getDuration() == 1600
                                && !sinking.isVisible() && sinking.save(new CompoundTag()).getCompound("HiddenEffect").getInt("Duration") == 2500,
                        "Entity storage repairs the original unsigned effect slot and retains the hidden weaker tier");
                helper.assertTrue(pig.hasEffect(MobEffects.FIRE_RESISTANCE), "The named passenger effect keeps its identity");
                helper.assertTrue(pig.hasEffect(MobEffects.HERO_OF_THE_VILLAGE) && pig.hasEffect(MobEffects.DARKNESS),
                        "A converted passenger keeps its unnamed vanilla effects");
                var arrow = loaded.getEntities().filter(entity -> entity.getType() == EntityType.ARROW).findFirst().orElseThrow();
                var cloud = loaded.getEntities().filter(entity -> entity.getType() == EntityType.AREA_EFFECT_CLOUD).findFirst().orElseThrow();
                var arrowEffects = arrow.saveWithoutId(new CompoundTag()).getList("CustomPotionEffects", Tag.TAG_COMPOUND);
                var cloudEffects = cloud.saveWithoutId(new CompoundTag()).getList("Effects", Tag.TAG_COMPOUND);
                helper.assertTrue(arrowEffects.stream().map(tag -> MobEffectInstance.load((CompoundTag) tag))
                                .anyMatch(effect -> effect != null && effect.getEffect() == WitcheryEffects.PARALYSIS.get()
                                        && effect.getAmplifier() == 1 && effect.getDuration() == 1000)
                                && arrowEffects.stream().map(tag -> MobEffectInstance.load((CompoundTag) tag))
                                .anyMatch(effect -> effect != null && effect.getEffect() == MobEffects.HERO_OF_THE_VILLAGE),
                        "A saved arrow recovers legacy Paralysis alongside the vanilla effect");
                helper.assertTrue(cloudEffects.stream().map(tag -> MobEffectInstance.load((CompoundTag) tag))
                                .anyMatch(effect -> effect != null && effect.getEffect() == WitcheryEffects.SINKING.get()
                                        && effect.getAmplifier() == 1 && effect.getDuration() == 1500)
                                && cloudEffects.stream().map(tag -> MobEffectInstance.load((CompoundTag) tag))
                                .anyMatch(effect -> effect != null && effect.getEffect() == MobEffects.DARKNESS),
                        "A saved cloud recovers legacy Sinking alongside the vanilla effect");
            }
        } finally {
            FileUtils.deleteDirectory(root.toFile());
        }
        helper.succeed();
    }

    private static void writeSnapshot(Path world) throws Exception {
        var registry = new CompoundTag();
        registry.put("Potions", LegacyWorldEffects.capture(List.of("I:PotionStartID=96",
                "I:\"witchery:potion.paralysed\"=43", "I:\"witchery:potion.sinking\"=171",
                "I:\"witchery:potion.queasy\"=32", "I:\"witchery:potion.gasmask\"=33")));
        NbtIo.writeCompressed(registry, world.resolve("witchery-legacy-ids.dat").toFile());
    }
}
