package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.entity.HuntingTrapBlockEntity;
import art.arcane.witchery.registry.WitcheryBlockEntities;
import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import org.apache.commons.io.FileUtils;

import java.nio.file.Files;
import java.util.Map;
import java.util.Optional;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class ObsoleteBlockEntityImportTests {
    @GameTest(template = "qa_coven_arena")
    public static void restoresSharedTileIdsInConvertedChunks(GameTestHelper helper) throws Exception {
        var aliases = Map.ofEntries(
                Map.entry("cbuttonwood", "cbuttonstone"),
                Map.entry("clever", "cbuttonstone"),
                Map.entry("csnowpressureplate", "cbuttonstone"),
                Map.entry("cstonepressureplate", "cbuttonstone"),
                Map.entry("cwoodendoor", "cbuttonstone"),
                Map.entry("cwoodpressureplate", "cbuttonstone"),
                Map.entry("brewgas", "brewliquid"),
                Map.entry("mirrorblock", "mirrorblock2"),
                Map.entry("scarecrow", "witchsladder"),
                Map.entry("trent", "witchsladder"));
        for (var entry : aliases.entrySet()) {
            String path = entry.getKey();
            var saved = TagParser.parseTag("""
                    {Color:5350513,Count:3,Duration:50,Spectral:1b,BlockColor:11b,DemonSlain:1b}
                    """);
            String alias = "witchery:" + entry.getValue();
            saved.putString("id", alias);
            var block = WitcheryBlocks.getRequired(path).get();
            var tile = BlockEntity.loadStatic(BlockPos.ZERO, block.defaultBlockState(), saved);
            helper.assertTrue(tile != null && tile.getType() == WitcheryBlockEntities.all().get(path).get()
                            && tile.saveWithFullMetadata().getString("id").equals("witchery:" + path),
                    "The placed " + path + " selects its own type and writes that ID on the next save");
            helper.assertTrue(saved.getString("id").equals(alias), "Loading a shared ID leaves the input tag reusable");
        }
        helper.assertTrue(LegacyChunkUpgrade.restoreSharedTileType("witchery:cbuttonstone",
                        ResourceLocation.parse("other_mod:cbuttonwood")).equals("witchery:cbuttonstone"),
                "The shared ID applies only to the matching Witchery blocks");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void restoresWolfTrapFromEarlierModernChunk(GameTestHelper helper) throws Exception {
        var original = TagParser.parseTag("""
                {xPos:0,zPos:0,block_entities:[
                  {id:"witchery:wolftrap",x:1,y:4,z:1,Sprung:0b,ArmedAt:20L,WolftrapStart:600L,
                    WolfMost:23L,WolfLeast:45L,Owner:{Name:"Morgan"}},
                  {id:"witchery:candelabra",x:2,y:4,z:1},
                  {id:"witchery:fumefunnel",x:3,y:4,z:1},
                  {id:"witchery:filteredfumefunnel",x:4,y:4,z:1},
                  {id:"witchery:garlicgarland",x:5,y:4,z:1},
                  {id:"witchery:wallgen",x:6,y:4,z:1},
                  {id:"minecraft:chest",x:7,y:4,z:1}]}
                """);
        original.putInt("DataVersion", SharedConstants.getCurrentVersion().getDataVersion().getVersion());
        var root = Files.createTempDirectory("witchery-obsolete-block-entity");
        var level = helper.getLevel();
        try (var storage = new ChunkStorage(root.resolve("region"), level.getServer().getFixerUpper(), true)) {
            var updated = storage.upgradeChunkTag(Level.OVERWORLD, level::getDataStorage, original, Optional.empty());
            var tiles = updated.getList("block_entities", Tag.TAG_COMPOUND);
            var wolfTrap = tiles.getCompound(0);
            var state = WitcheryBlocks.getRequired("wolftrap").get().defaultBlockState();
            var trap = BlockEntity.loadStatic(new BlockPos(1, 4, 1), state, wolfTrap);
            helper.assertTrue(trap instanceof HuntingTrapBlockEntity huntingTrap && !huntingTrap.sprung()
                            && trap.getType() == WitcheryBlockEntities.HUNTING_TRAP.get(),
                    "The older tile must load as the armed Wolf Trap");
            var loaded = trap.saveWithFullMetadata();
            helper.assertTrue(loaded.getLong("ArmedAt") == 20 && loaded.getLong("WolftrapStart") == 600
                            && loaded.getLong("WolfMost") == 23 && loaded.getLong("WolfLeast") == 45
                            && loaded.getCompound("Owner").getString("Name").equals("Morgan"),
                    "The recovered trap must preserve its owner, bait timer, and wolf UUID");
            helper.assertTrue(tiles.size() == 2 && tiles.getCompound(1).getString("id").equals("minecraft:chest"),
                    "Only the obsolete, empty Witchery tiles should be discarded");
        } finally {
            FileUtils.deleteDirectory(root.toFile());
        }
        helper.succeed();
    }
}
