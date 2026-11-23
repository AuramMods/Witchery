package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class LegacySpawnEggImportTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_legacy_spawn_egg")
    public static void importsWitcheryEggsBeforeVanillaFixes(GameTestHelper helper) throws Exception {
        var forge = TagParser.parseTag("""
                {ModList:[{ModId:"mcp",ModVersion:"9.05"},{ModId:"FML",ModVersion:"7.10.99.99"},
                          {ModId:"Forge",ModVersion:"10.13.4.1614"},{ModId:"witchery",ModVersion:"0.24.1"}]}
                """);
        var eggs = LegacySpawnEggs.capture(forge, null);
        helper.assertTrue(eggs.size() == 32, "Capture every egg from the original standalone registry");
        Path root = Files.createTempDirectory("witchery-legacy-egg");
        Path snapshot = root.resolve("witchery-legacy-ids.dat");
        try {
            var registry = new CompoundTag();
            var item = new CompoundTag();
            item.putString("K", "\u0002minecraft:spawn_egg");
            item.putInt("V", 383);
            var itemsById = new ListTag();
            itemsById.add(item);
            registry.put("ItemData", itemsById);
            registry.put("SpawnEggs", eggs);
            NbtIo.writeCompressed(registry, snapshot.toFile());
            var ids = LegacyWorldIds.find(root);
            var fixer = helper.getLevel().getServer().getFixerUpper();
            var chunk = TagParser.parseTag("""
                    {Level:{xPos:0,zPos:0,TerrainPopulated:1b,TileEntities:[{id:"Chest",x:1,y:64,z:1,Items:[
                      {Slot:0b,id:383s,Count:2b,Damage:6396s,tag:{LegacyEggMarker:"Demon"}},
                      {Slot:1b,id:383s,Count:1b,Damage:54s}]}]}}
                    """);
            var intermediate = LegacyChunkUpgrade.upgrade(DataFixTypes.CHUNK, fixer, chunk, -1, 1493, ids);
            ChunkStorage.injectDatafixingContext(intermediate, Level.OVERWORLD, Optional.empty());
            var upgraded = DataFixTypes.CHUNK.updateToCurrentVersion(fixer, intermediate, 1493);
            LegacyChunkUpgrade.finish(upgraded, Level.OVERWORLD);
            var chest = upgraded.getList("block_entities", Tag.TAG_COMPOUND).getCompound(0);
            var items = chest.getList("Items", Tag.TAG_COMPOUND);
            var demon = items.getCompound(0);
            helper.assertTrue(demon.getString("id").equals("witchery:demon_spawn_egg") && demon.getByte("Slot") == 0
                            && demon.getByte("Count") == 2 && demon.getCompound("tag").getString("LegacyEggMarker").equals("Demon"),
                    "The numeric Demon egg keeps its slot, count and custom tag");
            helper.assertTrue(items.getCompound(1).getString("id").equals("minecraft:zombie_spawn_egg"),
                    "The ordinary Zombie egg still uses vanilla conversion");

            var player = TagParser.parseTag("""
                    {Inventory:[{Slot:8b,id:"minecraft:spawn_egg",Count:1b,Damage:6418s}],
                     EnderItems:[{Slot:5b,id:383s,Count:1b,Damage:6427s}]}
                    """);
            ids.upgradePlayer(player, fixer, -1);
            var loaded = DataFixTypes.PLAYER.updateToCurrentVersion(fixer, player, -1);
            helper.assertTrue(loaded.getList("Inventory", Tag.TAG_COMPOUND).getCompound(0).getString("id")
                            .equals("witchery:leonard_spawn_egg")
                            && loaded.getList("EnderItems", Tag.TAG_COMPOUND).getCompound(0).getString("id")
                            .equals("witchery:reflection_spawn_egg"),
                    "Player inventory and Ender Chest also use the saved original metadata assignments");
        } finally {
            Files.deleteIfExists(snapshot);
            Files.deleteIfExists(root);
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_legacy_spawn_egg")
    public static void acceptsKnownEggAssignmentsFromMixedForgeProfiles(GameTestHelper helper) throws Exception {
        var forge = TagParser.parseTag("""
                {ModList:[{ModId:"witchery",ModVersion:"0.24.1"},{ModId:"fixture_mod",ModVersion:"1.0"}]}
                """);
        helper.assertTrue(LegacySpawnEggs.capture(forge, null).isEmpty(),
                "Additional legacy mods could have shifted the global egg IDs");
        Path mapping = Files.createTempFile("witchery-legacy-egg-mapping", ".properties");
        try {
            Files.writeString(mapping, "6500=leonard\n");
            CompoundTag captured = LegacySpawnEggs.capture(forge, mapping);
            helper.assertTrue(captured.size() == 1 && captured.getString("6500").equals("leonard"),
                    "Use the exact supplied assignment for a mixed Forge profile");
        } finally {
            Files.deleteIfExists(mapping);
        }
        helper.succeed();
    }
}
