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
import java.util.Optional;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class InfinityEggSaveTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_infinity_egg")
    public static void importsScheduledAndInFlightInfinityEggs(GameTestHelper helper) throws Exception {
        var registry = TagParser.parseTag("{ItemData:[{V:228}]}");
        registry.getList("ItemData", Tag.TAG_COMPOUND).getCompound(0).putString("K", "\u0001witchery:infinityegg");
        var original = TagParser.parseTag("""
                {Level:{xPos:0,zPos:0,TerrainPopulated:1b,TileTicks:[{i:228,x:1,y:80,z:1,t:4,p:-1}],Entities:[
                  {id:"FallingSand",TileID:228,Time:0b,DropItem:1b,Pos:[3.5d,80.5d,1.5d],Motion:[0d,0d,0d]},
                  {id:"FallingSand",TileID:228,Time:8b,DropItem:1b,Pos:[5.5d,78.5d,1.5d],Motion:[0d,-0.3d,0d]},
                  {id:"FallingSand",TileID:12,Time:5b,DropItem:1b,Pos:[7.5d,78.5d,1.5d],Motion:[0d,-0.2d,0d]}]}}
                """);
        var section = new CompoundTag();
        section.putByte("Y", (byte) 5);
        byte[] blocks = new byte[4096];
        blocks[17] = (byte) 228;
        blocks[19] = (byte) 228;
        section.putByteArray("Blocks", blocks);
        section.putByteArray("Data", new byte[2048]);
        var sections = new ListTag();
        sections.add(section);
        original.getCompound("Level").put("Sections", sections);
        var root = Files.createTempDirectory("witchery-infinity-egg-import");
        var snapshot = root.resolve("witchery-legacy-ids.dat");
        CompoundTag imported;
        try {
            NbtIo.writeCompressed(registry, snapshot.toFile());
            var fixer = helper.getLevel().getServer().getFixerUpper();
            var intermediate = LegacyChunkUpgrade.upgrade(DataFixTypes.CHUNK, fixer, original, -1, 1493,
                    LegacyWorldIds.find(root));
            ChunkStorage.injectDatafixingContext(intermediate, Level.OVERWORLD, Optional.empty());
            imported = DataFixTypes.CHUNK.updateToCurrentVersion(fixer, intermediate, 1493);
            LegacyChunkUpgrade.finish(imported, Level.OVERWORLD);
        } finally {
            Files.deleteIfExists(snapshot);
            Files.deleteIfExists(root);
        }

        var ticks = imported.getList("block_ticks", Tag.TAG_COMPOUND);
        var pending = ticks.getCompound(0);
        var unstarted = ticks.getCompound(1);
        helper.assertTrue(ticks.size() == 2 && pending.getString("i").equals("witchery:infinityegg")
                        && pending.getInt("x") == 1 && pending.getInt("t") == 4 && pending.getInt("p") == -1
                        && unstarted.getString("i").equals("witchery:infinityegg") && unstarted.getInt("x") == 3
                        && unstarted.getInt("t") == 1,
                "The queued fall keeps its saved delay and the unstarted entity resumes from its source block once");
        var entities = imported.getList("entities", Tag.TAG_COMPOUND);
        var falling = entities.getCompound(0);
        helper.assertTrue(entities.size() == 2 && falling.getString("id").equals("minecraft:falling_block")
                        && falling.getCompound("BlockState").getString("Name").equals("witchery:infinityegg")
                        && falling.getInt("Time") == 8 && falling.getBoolean("DropItem")
                        && falling.getList("Motion", Tag.TAG_DOUBLE).getDouble(1) == -0.3
                        && entities.getCompound(1).getCompound("BlockState").getString("Name").equals("minecraft:sand"),
                "A falling egg keeps its block ID and motion while vanilla sand still uses the vanilla migration");
        helper.succeed();
    }
}
