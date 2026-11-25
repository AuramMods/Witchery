package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.nio.file.Files;
import java.util.Optional;

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
        LegacyChunkUpgrade.updateBlockEntityTypes(tiles);
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
        LegacyChunkUpgrade.updateBlockEntityTypes(tiles);
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
