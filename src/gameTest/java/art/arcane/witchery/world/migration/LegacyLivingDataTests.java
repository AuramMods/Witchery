package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.DeathHorse;
import art.arcane.witchery.entity.Treefyd;
import com.mojang.authlib.GameProfile;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.TagParser;
import net.minecraft.util.datafix.DataFixers;
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
import java.util.UUID;
import java.util.function.Function;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class LegacyLivingDataTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_earlier_creature_attributes")
    public static void retainsCreatureUpgradesAndPassengerAttributes(GameTestHelper helper) throws Exception {
        var directory = Files.createTempDirectory("witchery-creature-attributes");
        var chunk = new ChunkPos(0, 0);
        try {
            var records = NbtUtils.addCurrentDataVersion(TagParser.parseTag("""
                    {Position:[I;0,0],Entities:[
                     {id:"witchery:treefyd",Health:128s,HealF:127.75f,Attributes:[
                       {Name:"generic.maxHealth",Base:150d,Modifiers:[
                         {UUIDMost:7788329159879770329L,UUIDLeast:-7656367041605928112L,
                          Name:"Treefyd vigor",Amount:10d,Operation:0}]},
                       {Name:"generic.attackDamage",Base:5d,Modifiers:[
                         {UUIDMost:3L,UUIDLeast:4L,Name:"Treefyd thorns",Amount:0.5d,Operation:1}]}]},
                     {id:"minecraft:boat",Passengers:[{id:"witchery:treefyd",Health:76.875f,HealF:72.625f,
                       Attributes:[{Name:"generic.maxHealth",Base:100d},
                         {Name:"minecraft:generic.max_health",Base:108d},{Name:"generic.attackDamage",Base:4d}]}]},
                     {id:"witchery:deathhorse",Health:13s,HealF:12.25f,Attributes:[
                       {Name:"generic.maxHealth",Base:53d},{Name:"horse.jumpStrength",Base:0.78d},
                       {Name:"minecraft:generic.movement_speed",Base:0.29d,Modifiers:[
                         {UUIDMost:5L,UUIDLeast:6L,UUID:[I;0,7,0,8],Name:"Saddle pace",Amount:0.125d,Operation:2}]}]}]}
                    """));
            try (var region = new RegionFile(directory.resolve("r.0.0.mca"), directory, true);
                 var stream = region.getChunkDataOutputStream(chunk)) {
                NbtIo.write(records, stream);
            }
            try (var storage = new EntityStorage(helper.getLevel(), directory, DataFixers.getDataFixer(), true, Runnable::run)) {
                var entities = storage.loadEntities(chunk).join().getEntities().toList();
                var treefyd = (Treefyd) entities.stream().filter(Treefyd.class::isInstance).findFirst().orElseThrow();
                helper.assertTrue(treefyd.getHealth() == 127.75F && treefyd.getMaxHealth() == 160
                                && treefyd.getAttributeBaseValue(Attributes.MAX_HEALTH) == 150
                                && treefyd.getAttributeValue(Attributes.ATTACK_DAMAGE) == 7.5,
                        "Demon Heart upgrades, both saved modifiers and fractional health survive storage");
                var boat = entities.stream().filter(entity -> entity.getType() == EntityType.BOAT).findFirst().orElseThrow();
                var passenger = (Treefyd) boat.getFirstPassenger();
                helper.assertTrue(passenger != null && passenger.getHealth() == 76.875F && passenger.getMaxHealth() == 108
                                && passenger.getAttributeValue(Attributes.ATTACK_DAMAGE) == 4,
                        "A passenger preserves the current health and maximum alongside its original attack upgrade");
                var horse = (DeathHorse) entities.stream().filter(DeathHorse.class::isInstance).findFirst().orElseThrow();
                helper.assertTrue(horse.getHealth() == 12.25F && horse.getMaxHealth() == 53
                                && horse.getAttributeValue(Attributes.JUMP_STRENGTH) == 0.78
                                && Math.abs(horse.getAttributeValue(Attributes.MOVEMENT_SPEED) - 0.32625) < 1.0E-8
                                && horse.getAttribute(Attributes.MOVEMENT_SPEED).getModifier(new UUID(7, 8)) != null,
                        "Binky recovers its original jump and speed while retaining the explicit current modifier UUID");
            }
        } finally {
            FileUtils.deleteDirectory(directory.toFile());
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_earlier_player_attributes")
    public static void retainsPlayerHealthAndVehicleAttributesOnOrdinaryLoad(GameTestHelper helper) throws Exception {
        var root = Files.createTempDirectory("witchery-player-attributes");
        try (var access = LevelStorageSource.createDefault(root).createAccess("world")) {
            var storage = access.createPlayerStorage();
            var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "EarlierAttributes"));
            var record = NbtUtils.addCurrentDataVersion(TagParser.parseTag("""
                    {Health:28s,HealF:27.375f,Dimension:"minecraft:overworld",Attributes:[
                      {Name:"generic.maxHealth",Base:28d,Modifiers:[
                        {UUIDMost:9L,UUIDLeast:10L,Name:"Saved vigor",Amount:2d,Operation:0}]}],
                     RootVehicle:{Attach:[I;0,3,0,4],Entity:{id:"minecraft:horse",UUID:[I;0,3,0,4],
                       Health:21s,HealF:20.5f,Attributes:[{Name:"generic.maxHealth",Base:24d},
                         {Name:"horse.jumpStrength",Base:0.75d}]}}}
                    """));
            NbtIo.writeCompressed(record, access.getLevelPath(LevelResource.PLAYER_DATA_DIR)
                    .resolve(player.getStringUUID() + ".dat").toFile());
            var saved = storage.load(player);
            helper.assertTrue(player.getHealth() == 27.375F && player.getMaxHealth() == 30
                            && player.getAttribute(Attributes.MAX_HEALTH).getModifier(new UUID(9, 10)) != null,
                    "An ordinary player load restores exact health and the modifier without a captured registry");
            var vehicle = EntityType.loadEntityRecursive(saved.getCompound("RootVehicle").getCompound("Entity"),
                    helper.getLevel(), Function.identity());
            helper.assertTrue(vehicle instanceof LivingEntity horse && horse.getHealth() == 20.5F
                            && horse.getMaxHealth() == 24 && horse.getAttributeValue(Attributes.JUMP_STRENGTH) == 0.75,
                    "The original mount retains its saved health, maximum and jump strength");
            storage.save(player);
            storage.load(player);
            helper.assertTrue(player.getHealth() == 27.375F && player.getMaxHealth() == 30,
                    "Saving under the current schema produces the same live stats on the next load");
        } finally {
            FileUtils.deleteDirectory(root.toFile());
        }
        helper.succeed();
    }
}
