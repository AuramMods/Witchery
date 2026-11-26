package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.SpiritPortalBlock;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class SpiritPortalSaveImportTests {
    @GameTest(template = "qa_coven_arena")
    public static void importedDreamFluidsWaitForTheirSavedUpdates(GameTestHelper helper) throws Exception {
        var registry = TagParser.parseTag("{ItemData:[{V:438},{V:439}]}");
        var entries = registry.getList("ItemData", Tag.TAG_COMPOUND);
        entries.getCompound(0).putString("K", "\u0001witchery:spiritflowing");
        entries.getCompound(1).putString("K", "\u0001witchery:hollowtears");
        var original = TagParser.parseTag("""
                {Level:{xPos:0,zPos:0,TerrainPopulated:1b,TileTicks:[
                   {i:438,x:1,y:10,z:3,t:110,p:-1},
                   {i:"witchery:spiritflowing",x:1,y:9,z:3,t:90,p:1},
                   {i:439,x:1,y:10,z:4,t:70,p:0},
                   {i:439,x:2,y:10,z:4,t:60,p:-2},
                   {i:439,x:3,y:10,z:4,t:80,p:0}]}}
                """);
        byte[] low = new byte[4096];
        byte[] high = new byte[2048];
        byte[] data = new byte[2048];
        int spirit = (10 << 8) | (3 << 4) | 1;
        int falling = spirit - 256;
        int tears = spirit + 16;
        int spreading = tears + 1;
        low[spirit] = low[falling] = (byte) 438;
        low[tears] = low[spreading] = (byte) 439;
        high[spirit / 2] = high[falling / 2] = high[tears / 2] = 0x10;
        high[spreading / 2] = 1;
        data[falling / 2] = 0x20;
        data[spreading / 2] = 1;
        var section = new CompoundTag();
        section.putByte("Y", (byte) 0);
        section.putByteArray("Blocks", low);
        section.putByteArray("Add", high);
        section.putByteArray("Data", data);
        var sections = new ListTag();
        sections.add(section);
        original.getCompound("Level").put("Sections", sections);

        var root = Files.createTempDirectory("witchery-legacy-dream-fluids");
        var snapshot = root.resolve("witchery-legacy-ids.dat");
        try {
            NbtIo.writeCompressed(registry, snapshot.toFile());
            var level = helper.getLevel();
            var fixer = level.getServer().getFixerUpper();
            var intermediate = LegacyChunkUpgrade.upgrade(DataFixTypes.CHUNK, fixer, original, -1, 1493,
                    LegacyWorldIds.find(root));
            var immediate = intermediate.getCompound("Level").getList("PostProcessing", Tag.TAG_LIST);
            helper.assertTrue(immediate.stream().map(ListTag.class::cast).allMatch(ListTag::isEmpty),
                    "Import must avoid running either fluid immediately during chunk post-processing");
            ChunkStorage.injectDatafixingContext(intermediate, Level.OVERWORLD, Optional.empty());
            var imported = DataFixTypes.CHUNK.updateToCurrentVersion(fixer, intermediate, 1493);
            LegacyChunkUpgrade.finish(imported, Level.OVERWORLD);
            var expected = TagParser.parseTag("""
                    {fluid_ticks:[{i:"witchery:fluidspirit",x:1,y:10,z:3,t:110,p:-1},
                     {i:"witchery:flowing_fluidspirit",x:1,y:9,z:3,t:90,p:1},
                     {i:"witchery:hollowtears",x:1,y:10,z:4,t:70,p:0},
                     {i:"witchery:flowing_hollowtears",x:2,y:10,z:4,t:60,p:-2}]}
                    """);
            helper.assertTrue(imported.getList("fluid_ticks", Tag.TAG_COMPOUND).equals(expected.get("fluid_ticks")),
                    "Keep both sources and both flowing ticks with their delays and priorities and discard the stale update");
            helper.assertTrue(WitcheryFluids.getRequiredSource("fluidspirit").get().getTickDelay(level) == 10
                            && WitcheryFluids.getRequiredSource("hollowtears").get().getTickDelay(level) == 7,
                    "Subsequent updates use the original viscosity-derived tick rates");
        } finally {
            Files.deleteIfExists(snapshot);
            Files.deleteIfExists(root);
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void legacyFrameSurvivesMixedResolvedAndUnresolvedCells(GameTestHelper helper) {
        var bottom = helper.absolutePos(new BlockPos(4, 2, 4));
        verifyFrame(helper, bottom, Direction.EAST);
        verifyFrame(helper, bottom, Direction.SOUTH);
        helper.succeed();
    }

    private static void verifyFrame(GameTestHelper helper, BlockPos bottom, Direction direction) {
        var level = helper.getLevel();
        var portal = WitcheryBlocks.getRequired("spiritportal").get().defaultBlockState()
                .setValue(SpiritPortalBlock.LEGACY_ORIENTATION, true);
        for (int along = -1; along <= 2; along++) {
            for (int height = -1; height <= 2; height++) {
                boolean edge = along == -1 || along == 2 || height == -1 || height == 2;
                level.setBlock(bottom.relative(direction, along).above(height),
                        edge ? Blocks.SNOW_BLOCK.defaultBlockState() : portal, Block.UPDATE_KNOWN_SHAPE);
            }
        }
        var cells = List.of(bottom, bottom.relative(direction), bottom.above(), bottom.relative(direction).above());
        for (BlockPos cell : cells) {
            var resolved = Block.updateFromNeighbourShapes(portal, level, cell);
            helper.assertTrue(resolved.is(portal.getBlock()) && resolved.getValue(SpiritPortalBlock.AXIS) == direction.getAxis()
                            && !resolved.getValue(SpiritPortalBlock.LEGACY_ORIENTATION),
                    "Resolve each imported portal cell using the same complete snow frame");
            level.setBlock(cell, resolved, Block.UPDATE_KNOWN_SHAPE);
        }
        level.setBlockAndUpdate(bottom.relative(direction, -1), Blocks.AIR.defaultBlockState());
        helper.assertTrue(cells.stream().allMatch(level::isEmptyBlock), "The resolved portal collapses when its snow frame breaks");
    }
}
