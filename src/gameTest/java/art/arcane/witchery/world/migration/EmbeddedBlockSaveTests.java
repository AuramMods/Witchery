package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.WitchWoodType;
import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.chunk.storage.EntityStorage;
import net.minecraft.world.level.chunk.storage.RegionFile;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import org.apache.commons.io.FileUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class EmbeddedBlockSaveTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_embedded_blocks")
    public static void importsMovingWitchwoodFromOriginalChunks(GameTestHelper helper) throws Exception {
        var world = Files.createTempDirectory("witchery-original-moving-blocks");
        var level = helper.getLevel();
        var original = TagParser.parseTag("""
                {Level:{xPos:0,zPos:0,TerrainPopulated:1b,TileEntities:[
                  {id:"Piston",x:1,y:80,z:1,blockId:154,blockData:9,facing:5,progress:0.5f,extending:1b},
                  {id:"Piston",x:2,y:80,z:1,blockId:17,blockData:5,facing:5,progress:0.5f,extending:1b}],Entities:[
                  {id:"FallingSand",TileID:410,Tile:-102b,Data:10b,Time:3b,DropItem:1b,
                   Pos:[5.5d,82.5d,1.5d],Motion:[0d,-0.3d,0d]}]}}
                """);
        try {
            writeRegistry(world);
            try (var storage = new ChunkStorage(world.resolve("region"), level.getServer().getFixerUpper(), true)) {
                var imported = storage.upgradeChunkTag(Level.OVERWORLD, level::getDataStorage, original, Optional.empty());
                var tiles = imported.getList("block_entities", Tag.TAG_COMPOUND);
                var piston = tiles.getCompound(0);
                var slab = piston.getCompound("blockState");
                helper.assertTrue(slab.getString("Name").equals("witchery:witchwoodslab")
                                && slab.getCompound("Properties").getString("wood_type").equals("alder")
                                && slab.getCompound("Properties").getString("type").equals("top")
                                && piston.getInt("facing") == 5 && piston.getFloat("progress") == 0.5F
                                && piston.getBoolean("extending"),
                        "The original piston retains the Alder top slab and its extension state");
                var vanilla = tiles.getCompound(1).getCompound("blockState");
                helper.assertTrue(vanilla.getString("Name").equals("minecraft:spruce_log")
                                && vanilla.getCompound("Properties").getString("axis").equals("x"),
                        "Vanilla still converts the spruce log");
                var falling = imported.getList("entities", Tag.TAG_COMPOUND).getCompound(0);
                var log = falling.getCompound("BlockState");
                helper.assertTrue(log.getString("Name").equals("witchery:witchlog")
                                && log.getCompound("Properties").getString("wood_type").equals("hawthorn")
                                && log.getCompound("Properties").getString("axis").equals("z")
                                && falling.getInt("Time") == 3 && falling.getBoolean("DropItem")
                                && falling.getList("Motion", Tag.TAG_DOUBLE).getDouble(1) == -0.3,
                        "The falling entity prefers the full ID and retains log metadata and motion");
            }
        } finally {
            FileUtils.deleteDirectory(world.toFile());
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_embedded_blocks")
    public static void repairsMixedMovingPayloadsOnOrdinaryLoad(GameTestHelper helper) throws Exception {
        var world = Files.createTempDirectory("witchery-mixed-moving-blocks");
        var level = helper.getLevel();
        var fixer = level.getServer().getFixerUpper();
        try {
            writeRegistry(world);
            var chunk = NbtUtils.addCurrentDataVersion(TagParser.parseTag("""
                    {xPos:0,zPos:0,block_entities:[
                      {id:"minecraft:piston",x:1,y:80,z:1,blockId:154,blockData:9,facing:5,progress:0.375f,extending:0b},
                      {id:"minecraft:piston",x:2,y:80,z:1,blockId:154,blockData:9,blockState:{Name:"minecraft:stone"}}]}
                    """));
            try (var storage = new ChunkStorage(world.resolve("region"), fixer, true)) {
                var repaired = storage.upgradeChunkTag(Level.OVERWORLD, level::getDataStorage, chunk, Optional.empty());
                var tiles = repaired.getList("block_entities", Tag.TAG_COMPOUND);
                var piston = tiles.getCompound(0);
                helper.assertTrue(piston.getCompound("blockState").getString("Name").equals("witchery:witchwoodslab")
                                && piston.getFloat("progress") == 0.375F && !piston.getBoolean("extending")
                                && tiles.getCompound(1).getCompound("blockState").getString("Name").equals("minecraft:stone"),
                        "Ordinary chunk loading repairs the old payload and retains explicit current state");
            }

            var position = new ChunkPos(0, 0);
            var entities = Files.createDirectories(world.resolve("entities"));
            var saved = NbtUtils.addCurrentDataVersion(TagParser.parseTag("""
                    {Position:[I;0,0],Entities:[
                      {id:"minecraft:falling_block",Tile:-102b,Data:9b,Time:8,DropItem:1b,Pos:[3.5d,81d,1.5d]},
                      {id:"minecraft:falling_block",TileID:410,Data:10b,Time:8,BlockState:{Name:"minecraft:gravel"},Pos:[5.5d,81d,1.5d]},
                      {id:"minecraft:falling_block",TileID:410,Data:5b,Time:8,Pos:[7.5d,81d,1.5d]}]}
                    """));
            try (var region = new RegionFile(entities.resolve("r.0.0.mca"), entities, true);
                 var stream = region.getChunkDataOutputStream(position)) {
                NbtIo.write(saved, stream);
            }
            try (var storage = new EntityStorage(level, entities, fixer, true, Runnable::run)) {
                var falling = storage.loadEntities(position).join().getEntities().map(FallingBlockEntity.class::cast).toList();
                helper.assertTrue(falling.size() == 3
                                && falling.get(0).getBlockState().is(WitcheryBlocks.getRequired("witchwoodslab").get())
                                && falling.get(0).getBlockState().getValue(WitchWoodType.PROPERTY) == WitchWoodType.ALDER
                                && falling.get(0).getBlockState().getValue(SlabBlock.TYPE) == SlabType.TOP
                                && falling.get(1).getBlockState().is(Blocks.GRAVEL)
                                && falling.get(2).getBlockState().getValue(WitchWoodType.PROPERTY) == WitchWoodType.ALDER
                                && falling.get(2).getBlockState().getValue(RotatedPillarBlock.AXIS) == Direction.Axis.X,
                        "Ordinary entity loading repairs both legacy ID encodings and preserves an explicit gravel state");
            }
        } finally {
            FileUtils.deleteDirectory(world.toFile());
        }
        helper.succeed();
    }

    private static void writeRegistry(Path world) throws Exception {
        var registry = new CompoundTag();
        var records = TagParser.parseTag("{ItemData:[{V:154},{V:410}]}").getList("ItemData", Tag.TAG_COMPOUND);
        records.getCompound(0).putString("K", "\u0001witchery:witchwoodslab");
        records.getCompound(1).putString("K", "\u0001witchery:witchlog");
        registry.put("ItemData", records);
        NbtIo.writeCompressed(registry, world.resolve("witchery-legacy-ids.dat").toFile());
    }
}
