package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.DreamCatcherBlock;
import art.arcane.witchery.block.DreamWeave;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.nio.file.Files;
import java.util.Optional;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class MountedBlockSaveImportTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_mounted_block_import")
    public static void restoresDreamWeaveAndBothFrozenPlateDelaysDuringConversion(GameTestHelper helper) throws Exception {
        var registry = TagParser.parseTag("""
                {ItemData:[{K:"%switchery:dreamcatcher",V:401},{K:"%switchery:icepressureplate",V:402},
                           {K:"%switchery:snowpressureplate",V:403}]}
                """.formatted("\u0001", "\u0001", "\u0001"));
        var original = TagParser.parseTag("""
                {Level:{xPos:0,zPos:0,TerrainPopulated:1b,TileEntities:[
                    {id:"witchery:dreamcatcher",x:1,y:0,z:0,WITCWeaveID:3}],
                  TileTicks:[{i:402,x:2,y:0,z:0,t:47,p:-1},
                             {i:"witchery:snowpressureplate",x:3,y:0,z:0,t:65,p:1}]}}
                """);
        byte[] blocks = new byte[4096];
        byte[] highIds = new byte[2048];
        byte[] metadata = new byte[2048];
        blocks[1] = (byte) 401;
        blocks[2] = (byte) 402;
        blocks[3] = (byte) 403;
        highIds[0] = 0x10;
        highIds[1] = 0x11;
        metadata[0] = 0x20;
        metadata[1] = 0x11;
        var section = new CompoundTag();
        section.putByteArray("Blocks", blocks);
        section.putByteArray("Add", highIds);
        section.putByteArray("Data", metadata);
        var sections = new ListTag();
        sections.add(section);
        original.getCompound("Level").put("Sections", sections);

        var directory = Files.createTempDirectory("witchery-mounted-block-import");
        var snapshot = directory.resolve("witchery-legacy-ids.dat");
        try {
            NbtIo.writeCompressed(registry, snapshot.toFile());
            var fixer = helper.getLevel().getServer().getFixerUpper();
            var flattened = LegacyChunkUpgrade.upgrade(DataFixTypes.CHUNK, fixer, original, -1, 1493,
                    LegacyWorldIds.find(directory));
            ChunkStorage.injectDatafixingContext(flattened, Level.OVERWORLD, Optional.empty());
            var imported = DataFixTypes.CHUNK.updateToCurrentVersion(fixer, flattened, 1493);
            LegacyChunkUpgrade.finish(imported, Level.OVERWORLD);

            var palette = imported.getList("sections", Tag.TAG_COMPOUND).stream().map(CompoundTag.class::cast)
                    .filter(value -> value.getInt("Y") == 0).findFirst().orElseThrow()
                    .getCompound("block_states").getList("palette", Tag.TAG_COMPOUND);
            var catcher = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), paletteState(palette, "dreamcatcher"));
            helper.assertTrue(catcher.getValue(DreamCatcherBlock.FACING) == Direction.NORTH
                            && catcher.getValue(DreamCatcherBlock.WEAVE) == DreamWeave.NIGHTMARES,
                    "The flattened block state should already carry the saved weave and mount direction");
            var ticks = imported.getList("block_ticks", Tag.TAG_COMPOUND);
            helper.assertTrue(ticks.size() == 2, "Each plate should retain a single release tick");
            verifyPlate(helper, palette, ticks, "icepressureplate", 2, 47, -1);
            verifyPlate(helper, palette, ticks, "snowpressureplate", 3, 65, 1);
        } finally {
            Files.deleteIfExists(snapshot);
            Files.deleteIfExists(directory);
        }
        helper.succeed();
    }

    private static void verifyPlate(GameTestHelper helper, ListTag palette, ListTag ticks, String path,
                                    int x, int delay, int priority) {
        var state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), paletteState(palette, path));
        helper.assertTrue(state.getValue(BlockStateProperties.POWERED), "Preserve the powered state of " + path);
        helper.assertTrue(ticks.stream().map(CompoundTag.class::cast).anyMatch(tick ->
                        tick.getString("i").equals("witchery:" + path) && tick.getInt("x") == x
                                && tick.getInt("t") == delay && tick.getInt("p") == priority),
                "Preserve the exact release delay and priority for " + path);
    }

    private static CompoundTag paletteState(ListTag palette, String block) {
        return palette.stream().map(CompoundTag.class::cast)
                .filter(state -> state.getString("Name").equals("witchery:" + block)).findFirst().orElseThrow();
    }
}
