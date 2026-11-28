package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryApothecary;
import art.arcane.witchery.registry.WitcheryEffects;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.nio.file.Files;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class LegacySpawnerImportTests {
    @GameTest(template = "qa_coven_arena")
    public static void importsWitcherySpawnerBlocksAndCarts(GameTestHelper helper) throws Exception {
        var original = TagParser.parseTag("""
                {Level:{xPos:5,zPos:1,TerrainPopulated:1b,TileEntities:[
                    {id:"MobSpawner",x:94,y:120,z:31,EntityId:"witchery.hellhound",Delay:20s},
                    {id:"MobSpawner",x:95,y:120,z:31,EntityId:"witchery.toad",Delay:10s,RequiredPlayerRange:5s,
                     SpawnData:{CustomName:"SelectedToad",Health:6f},SpawnPotentials:[
                       {Type:"witchery.hellhound",Weight:7,Properties:{CustomName:"WeightedHound",Health:13f}},
                       {Type:"Skeleton",Weight:3,Properties:{CustomName:"BoneControl"}}]},
                    {id:"MobSpawner",x:96,y:120,z:31,EntityId:"Zombie"}],Entities:[
                    {id:"MinecartSpawner",Pos:[94.5d,121.0d,31.5d],EntityId:"witchery.hellhound",Delay:55s,
                     SpawnData:{CustomName:"CartHound",Health:11f},SpawnPotentials:[
                       {Type:"witchery.toad",Weight:4,Properties:{CustomName:"CartToad"}},
                       {Type:"Zombie",Weight:1,Properties:{}}]}]}}
                """);
        var root = Files.createTempDirectory("witchery-legacy-spawners");
        var snapshot = root.resolve("witchery-legacy-ids.dat");
        CompoundTag converted;
        try {
            NbtIo.writeCompressed(new CompoundTag(), snapshot.toFile());
            var fixer = helper.getLevel().getServer().getFixerUpper();
            var intermediate = LegacyChunkUpgrade.upgrade(DataFixTypes.CHUNK, fixer, original, -1, 1493,
                    LegacyWorldIds.find(root));
            ChunkStorage.injectDatafixingContext(intermediate, Level.OVERWORLD, Optional.empty());
            converted = DataFixTypes.CHUNK.updateToCurrentVersion(fixer, intermediate, 1493);
            LegacyChunkUpgrade.finish(converted, Level.OVERWORLD);
        } finally {
            Files.deleteIfExists(snapshot);
            Files.deleteIfExists(root);
        }
        var tiles = converted.getList("block_entities", Tag.TAG_COMPOUND);
        LegacyChunkUpgrade.upgradeBlockEntities(tiles, false);
        helper.assertTrue(active(tiles.getCompound(0)).getString("id").equals("witchery:hellhound"),
                "A spawner with only an original EntityId must keep its Hellhound");
        var weighted = tiles.getCompound(1);
        helper.assertTrue(weighted.getShort("Delay") == 10 && weighted.getShort("RequiredPlayerRange") == 5,
                "Spawner timing and player radius survive the data fix");
        assertMob(helper, active(weighted), "witchery:toad", "SelectedToad", 6);
        assertPotential(helper, weighted, 0, "witchery:hellhound", 7);
        assertPotential(helper, weighted, 1, "minecraft:skeleton", 3);
        assertMob(helper, potential(weighted, 0).getCompound("data").getCompound("entity"),
                "witchery:hellhound", "WeightedHound", 13);
        helper.assertTrue(active(tiles.getCompound(2)).getString("id").equals("minecraft:zombie"),
                "The vanilla spawner follows its ordinary data fix");
        var cart = converted.getList("entities", Tag.TAG_COMPOUND).getCompound(0);
        helper.assertTrue(cart.getString("id").equals("minecraft:spawner_minecart") && cart.getShort("Delay") == 55,
                "The spawner minecart and its pending delay survive");
        assertMob(helper, active(cart), "witchery:hellhound", "CartHound", 11);
        assertPotential(helper, cart, 0, "witchery:toad", 4);
        assertPotential(helper, cart, 1, "minecraft:zombie", 1);
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void repairsNamesRetainedInEarlierPortSaves(GameTestHelper helper) throws Exception {
        var saved = TagParser.parseTag("""
                {block_entities:[{id:"minecraft:mob_spawner",SpawnData:{entity:{id:"minecraft:witchery.toad"}},
                  SpawnPotentials:[{weight:4,data:{entity:{id:"witchery.hellhound"}}},
                                   {weight:1,data:{entity:{id:"minecraft:pig"}}}]}],
                 Entities:[{id:"minecraft:boat",Passengers:[{id:"minecraft:spawner_minecart",
                   SpawnData:{entity:{id:"witchery.Hellhound",Health:9f}},SpawnPotentials:[
                     {weight:2,data:{entity:{id:"witchery:toad"}}}]}]}]}
                """);
        var tiles = saved.getList("block_entities", Tag.TAG_COMPOUND);
        LegacyChunkUpgrade.upgradeBlockEntities(tiles, false);
        var tile = tiles.getCompound(0);
        helper.assertTrue(active(tile).getString("id").equals("witchery:toad"), "Restore the saved dotted Toad ID");
        assertPotential(helper, tile, 0, "witchery:hellhound", 4);
        assertPotential(helper, tile, 1, "minecraft:pig", 1);
        var entities = saved.getList("Entities", Tag.TAG_COMPOUND);
        LegacyChunkEntities.restore(entities);
        var cart = entities.getCompound(0).getList("Passengers", Tag.TAG_COMPOUND).getCompound(0);
        helper.assertTrue(active(cart).getString("id").equals("witchery:hellhound") && active(cart).getFloat("Health") == 9,
                "Preserve a nested cart's saved mob state while repairing its name");
        assertPotential(helper, cart, 0, "witchery:toad", 2);
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_spawner_template_state")
    public static void preservesSpecializedStateInVanillaSpawnerTemplates(GameTestHelper helper) throws Exception {
        var original = TagParser.parseTag("""
                {Level:{xPos:5,zPos:1,TerrainPopulated:1b,TileEntities:[
                  {id:"MobSpawner",x:94,y:120,z:31,EntityId:"Villager",Delay:60s,
                   SpawnData:{Profession:2435,Health:16f,ActiveEffects:[
                     {Id:43b,Amplifier:2b,Duration:2400,Ambient:1b,ShowParticles:0b},
                     {Id:12b,Amplifier:0b,Duration:1200}]},
                   SpawnPotentials:[{Type:"Zombie",Weight:7,Properties:{ActiveEffects:[
                     {Id:-85b,Amplifier:1b,Duration:1800}]}}]}],
                 Entities:[{id:"MinecartSpawner",Pos:[94.5d,121d,31.5d],EntityId:"Pig",
                   SpawnData:{ActiveEffects:[{Id:-85b,Amplifier:0b,Duration:3100}]},
                   SpawnPotentials:[{Type:"Villager",Weight:4,Properties:{Profession:2435}}]}]}}
                """);
        var root = Files.createTempDirectory("witchery-spawn-template-state");
        var snapshot = root.resolve("witchery-legacy-ids.dat");
        var level = helper.getLevel();
        try {
            var registry = new CompoundTag();
            registry.put("Potions", LegacyWorldEffects.capture(List.of("I:\"witchery:potion.paralysed\"=43",
                    "I:\"witchery:potion.sinking\"=171")));
            NbtIo.writeCompressed(registry, snapshot.toFile());
            try (var storage = new ChunkStorage(root.resolve("region"), level.getServer().getFixerUpper(), true)) {
                var converted = storage.upgradeChunkTag(Level.OVERWORLD, level::getDataStorage, original, Optional.empty());
                var block = converted.getList("block_entities", Tag.TAG_COMPOUND).getCompound(0);
                var cart = converted.getList("entities", Tag.TAG_COMPOUND).getCompound(0);
                helper.assertTrue(block.getShort("Delay") == 60, "Retain the original spawn delay");
                assertPotential(helper, block, 0, "minecraft:zombie", 7);
                assertPotential(helper, cart, 0, "minecraft:villager", 4);
                var cartApothecary = potential(cart, 0).getCompound("data").getCompound("entity");
                helper.assertTrue(cartApothecary.getCompound("VillagerData").getString("profession").equals("witchery:apothecary"),
                        "The weighted vanilla villager template keeps the Witchery profession");
                assertEffect(helper, active(cart), "witchery:potion.sinking", 0, 3100);
                var weightedZombie = potential(block, 0).getCompound("data").getCompound("entity");
                assertEffect(helper, weightedZombie, "witchery:potion.sinking", 1, 1800);

                var selected = active(block);
                var spawned = EntityType.loadEntityRecursive(selected, level, Function.identity());
                helper.assertTrue(spawned instanceof Villager villager
                                && villager.getVillagerData().getProfession() == WitcheryApothecary.PROFESSION.get()
                                && villager.getHealth() == 16 && villager.hasEffect(WitcheryEffects.PARALYSIS.get())
                                && villager.hasEffect(MobEffects.FIRE_RESISTANCE)
                                && villager.getAttributeValue(Attributes.MOVEMENT_SPEED) == 0,
                        "The selected template creates a paralyzed Apothecary with the original health and vanilla effect");
                selected.getList("ActiveEffects", Tag.TAG_COMPOUND).getCompound(0).remove("forge:id");
                weightedZombie.getList("ActiveEffects", Tag.TAG_COMPOUND).getCompound(0).remove("forge:id");
                var reopened = storage.upgradeChunkTag(Level.OVERWORLD, level::getDataStorage, converted, Optional.empty());
                var repaired = reopened.getList("block_entities", Tag.TAG_COMPOUND).getCompound(0);
                assertEffect(helper, active(repaired), "witchery:potion.paralysed", 2, 2400);
                assertEffect(helper, potential(repaired, 0).getCompound("data").getCompound("entity"),
                        "witchery:potion.sinking", 1, 1800);
            }
        } finally {
            Files.deleteIfExists(snapshot);
            Files.deleteIfExists(root.resolve("region"));
            Files.deleteIfExists(root);
        }
        helper.succeed();
    }

    private static void assertEffect(GameTestHelper helper, CompoundTag entity, String effect, int amplifier, int duration) {
        var first = entity.getList("ActiveEffects", Tag.TAG_COMPOUND).getCompound(0);
        helper.assertTrue(first.getString("forge:id").equals(effect) && first.getByte("Amplifier") == amplifier
                && first.getInt("Duration") == duration, "Retain the named effect and original details for " + effect);
    }

    private static CompoundTag active(CompoundTag spawner) {
        return spawner.getCompound("SpawnData").getCompound("entity");
    }

    private static CompoundTag potential(CompoundTag spawner, int index) {
        return spawner.getList("SpawnPotentials", Tag.TAG_COMPOUND).getCompound(index);
    }

    private static void assertPotential(GameTestHelper helper, CompoundTag spawner, int index, String id, int weight) {
        var entry = potential(spawner, index);
        helper.assertTrue(entry.getInt("weight") == weight && entry.getCompound("data").getCompound("entity").getString("id").equals(id),
                "Expected the original weight and mob ID for " + id);
    }

    private static void assertMob(GameTestHelper helper, CompoundTag entity, String id, String name, float health) {
        var label = Component.Serializer.fromJson(entity.getString("CustomName"));
        helper.assertTrue(entity.getString("id").equals(id) && entity.getFloat("Health") == health
                        && label != null && label.getString().equals(name) && !entity.contains("WitcheryLegacyEntityType"),
                "Expected the original name and health with the restored mob ID " + id);
    }
}
