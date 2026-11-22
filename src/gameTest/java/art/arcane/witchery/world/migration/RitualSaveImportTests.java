package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.CoffinBlock;
import art.arcane.witchery.block.TormentPortalBlock;
import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.datafix.PackedBitStorage;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class RitualSaveImportTests {
    private record LegacyBlock(String name, int metadata, String property, String expected) {}

    @GameTest(template = "qa_coven_arena")
    public static void wickerBundlesKeepAxisAndType(GameTestHelper helper) {
        var wicker = new ResourceLocation(Witchery.MODID, "wickerbundle");
        var axes = List.of("y", "x", "z");
        for (int axis = 0; axis < axes.size(); axis++) {
            for (int type = 0; type <= 1; type++) {
                int metadata = axis * 4 + type;
                var state = LegacyChunkUpgrade.restoreState(wicker, metadata, 0);
                var expectedType = type == 0 ? "plain" : "bloodied";
                helper.assertTrue(state != null && state.getCompound("Properties").getString("axis").equals(axes.get(axis))
                                && state.getCompound("Properties").getString("bundle_type").equals(expectedType),
                        "Legacy Wicker metadata " + metadata + " should retain its axis and " + expectedType + " type");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void restoresWorkshopAndConstructionStates(GameTestHelper helper) throws Exception {
        var blocks = List.of(new LegacyBlock("grassper", 12, "facing", "west"),
                new LegacyBlock("circleglyphritual", 11, "pattern", "11"),
                new LegacyBlock("circleglyphotherwhere", 3, "pattern", "3"),
                new LegacyBlock("circleglyphinfernal", 6, "pattern", "6"),
                new LegacyBlock("circle", 1, "active", "true"),
                new LegacyBlock("scarecrow", 5, "facing", "east"),
                new LegacyBlock("trent", 3, "facing", "south"),
                new LegacyBlock("witchsladder", 12, "facing", "west"),
                new LegacyBlock("wickerbundle", 1, "bundle_type", "bloodied"),
                new LegacyBlock("chalice", 1, "filled", "true"),
                new LegacyBlock("witchwoodslab", 10, "wood_type", "hawthorn"),
                new LegacyBlock("witchwooddoubleslab", 1, "type", "double"),
                new LegacyBlock("stockade", 8, "wood_type", "dark_oak"),
                new LegacyBlock("icestockade", 0, "covered", "false"),
                new LegacyBlock("icefence", 0, "west", "false"),
                new LegacyBlock("icefencegate", 13, "facing", "west"));
        var entries = new ListTag();
        byte[] lowIds = new byte[4096];
        byte[] highIds = new byte[2048];
        byte[] metadata = new byte[2048];
        for (int i = 0; i < blocks.size(); i++) {
            LegacyBlock block = blocks.get(i);
            int id = 410 + i;
            var entry = new CompoundTag();
            entry.putString("K", "\u0001witchery:" + block.name());
            entry.putInt("V", id);
            entries.add(entry);
            lowIds[i] = (byte) id;
            int shift = (i & 1) * 4;
            highIds[i >> 1] |= (byte) ((id >> 8) << shift);
            metadata[i >> 1] |= (byte) (block.metadata() << shift);
        }
        int iceDoorId = 450;
        var doorEntry = new CompoundTag();
        doorEntry.putString("K", "\u0001witchery:icedoor");
        doorEntry.putInt("V", iceDoorId);
        entries.add(doorEntry);
        int coffinId = 451;
        var coffinEntry = new CompoundTag();
        coffinEntry.putString("K", "\u0001witchery:coffinblock");
        coffinEntry.putInt("V", coffinId);
        entries.add(coffinEntry);
        lowIds[16] = lowIds[17] = (byte) coffinId;
        highIds[8] = (byte) ((coffinId >> 8) * 0x11);
        metadata[8] = 0x33;
        int lowerIndex = 15 * 256;
        lowIds[lowerIndex] = (byte) iceDoorId;
        highIds[lowerIndex >> 1] = (byte) (iceDoorId >> 8);
        metadata[lowerIndex >> 1] = 7;
        var section = new CompoundTag();
        section.putByte("Y", (byte) 0);
        section.putByteArray("Blocks", lowIds);
        section.putByteArray("Add", highIds);
        section.putByteArray("Data", metadata);
        var sections = new ListTag();
        sections.add(section);
        var upperSection = new CompoundTag();
        upperSection.putByte("Y", (byte) 1);
        var upperIds = new byte[4096];
        var upperHighIds = new byte[2048];
        var upperMetadata = new byte[2048];
        upperIds[0] = (byte) iceDoorId;
        upperHighIds[0] = (byte) (iceDoorId >> 8);
        upperMetadata[0] = 11;
        int upperGateIndex = 256 + 5 * 16 + 3;
        int gateId = 410 + blocks.size() - 1;
        upperIds[upperGateIndex] = (byte) gateId;
        upperHighIds[upperGateIndex >> 1] = (byte) ((gateId >> 8) << 4);
        upperMetadata[upperGateIndex >> 1] = 6 << 4;
        upperSection.putByteArray("Blocks", upperIds);
        upperSection.putByteArray("Add", upperHighIds);
        upperSection.putByteArray("Data", upperMetadata);
        sections.add(upperSection);
        var level = TagParser.parseTag("""
                {TerrainPopulated:1b,TileEntities:[
                  {id:"witchery:witchsladder",x:5,y:0,z:0,EffectTypeID:1,BlockColor:11b},
                  {id:"witchery:witchsladder",x:6,y:0,z:0,EffectTypeID:2,BlockColor:14b},
                  {id:"witchery:witchsladder",x:7,y:0,z:0,EffectTypeID:3,Spectral:1b},
                  {id:"witchery:coffinblock",x:0,y:0,z:1,Opened:1b},
                  {id:"witchery:coffinblock",x:1,y:0,z:1,Opened:0b}]}
                """);
        level.put("Sections", sections);
        var original = new CompoundTag();
        original.put("Level", level);

        var root = Files.createTempDirectory("witchery-ritual-save-test");
        var snapshot = root.resolve("witchery-legacy-ids.dat");
        try {
            var arthanaId = new CompoundTag();
            arthanaId.putString("K", "\u0002witchery:arthana");
            arthanaId.putInt("V", 4109);
            entries.add(arthanaId);
            var registry = new CompoundTag();
            registry.put("ItemData", entries);
            NbtIo.writeCompressed(registry, snapshot.toFile());
            var ids = LegacyWorldIds.find(root);
            var blades = TagParser.parseTag("""
                    {Items:[{id:4109s,Count:1b,Damage:14s,tag:{Keepsake:1b}},
                      {id:"witchery:arthana",Count:1b,Damage:14s,tag:{Damage:0}}]}
                    """);
            ids.upgradeItems(blades);
            var bladesList = blades.getList("Items", Tag.TAG_COMPOUND);
            var legacyWear = bladesList.getCompound(0).getCompound("tag");
            helper.assertTrue(legacyWear.getInt("Damage") == 14 && legacyWear.getBoolean("Keepsake")
                            && bladesList.getCompound(1).getCompound("tag").getInt("Damage") == 0,
                    "Keep stamped durability with custom tags and preserve explicit modern wear");
            var fixer = helper.getLevel().getServer().getFixerUpper();
            var intermediate = LegacyChunkUpgrade.upgrade(DataFixTypes.CHUNK, fixer, original, -1, 1493, ids);
            var fixed = intermediate.getCompound("Level");
            var flattened = fixed.getList("Sections", Tag.TAG_COMPOUND).getCompound(0);
            var palette = flattened.getList("Palette", Tag.TAG_COMPOUND);
            var states = new PackedBitStorage(Math.max(4, Mth.ceillog2(palette.size())), 4096, flattened.getLongArray("BlockStates"));
            for (int i = 0; i < blocks.size(); i++) {
                var block = blocks.get(i);
                var state = palette.getCompound(states.get(i));
                helper.assertTrue(state.getString("Name").equals("witchery:" + block.name())
                                && state.getCompound("Properties").getString(block.property()).equals(block.expected()),
                        "Expected " + block.name() + " with " + block.property() + "=" + block.expected() + ", found " + state);
            }
            helper.assertTrue(palette.getCompound(states.get(10)).getCompound("Properties").getString("type").equals("top"),
                    "The Hawthorn slab must keep the high metadata bit for its top half");
            var lowerDoor = palette.getCompound(states.get(lowerIndex));
            var flatUpper = fixed.getList("Sections", Tag.TAG_COMPOUND).getCompound(1);
            var upperPalette = flatUpper.getList("Palette", Tag.TAG_COMPOUND);
            var upperStates = new PackedBitStorage(Math.max(4, Mth.ceillog2(upperPalette.size())), 4096,
                    flatUpper.getLongArray("BlockStates"));
            assertDoor(helper, lowerDoor, "lower");
            assertDoor(helper, upperPalette.getCompound(upperStates.get(0)), "upper");
            var poweredGate = palette.getCompound(states.get(15)).getCompound("Properties");
            var openGate = upperPalette.getCompound(upperStates.get(upperGateIndex)).getCompound("Properties");
            helper.assertTrue(poweredGate.getString("open").equals("true") && poweredGate.getString("powered").equals("true")
                            && openGate.getString("facing").equals("north") && openGate.getString("open").equals("true")
                            && openGate.getString("powered").equals("false"),
                    "Keep the ice gate's facing, open bit and powered bit independently");
            var openCoffin = palette.getCompound(states.get(16)).getCompound("Properties");
            var closedCoffin = palette.getCompound(states.get(17)).getCompound("Properties");
            helper.assertTrue(openCoffin.getString("open").equals("true") && closedCoffin.getString("open").equals("false"),
                    "Coffins with matching block metadata retain their distinct lid states");
            ChunkStorage.injectDatafixingContext(intermediate, Level.OVERWORLD, Optional.empty());
            var upgraded = DataFixTypes.CHUNK.updateToCurrentVersion(fixer, intermediate, 1493);
            var queued = upgraded.getList("PostProcessing", Tag.TAG_LIST);
            helper.assertTrue(queued.getList(4).equals(TagParser.parseTag("{Offsets:[12s,13s,14s,15s,256s,257s]}").getList("Offsets", Tag.TAG_SHORT))
                            && queued.getList(5).equals(TagParser.parseTag("{Offsets:[1299s]}").getList("Offsets", Tag.TAG_SHORT)),
                    "Queue shape updates in the correct sections and axes after the Overworld height expansion");
            var tiles = fixed.getList("TileEntities", Tag.TAG_COMPOUND);
            helper.assertTrue(tiles.getCompound(0).getString("id").equals("witchery:scarecrow")
                            && tiles.getCompound(0).getByte("BlockColor") == 11
                            && tiles.getCompound(1).getString("id").equals("witchery:trent")
                            && tiles.getCompound(1).getInt("EffectTypeID") == 2
                            && tiles.getCompound(2).getString("id").equals("witchery:witchsladder")
                            && tiles.getCompound(2).getBoolean("Spectral"),
                    "The shared tile ID must follow each physical fetish block and retain its saved settings");
        } finally {
            Files.deleteIfExists(snapshot);
            Files.deleteIfExists(root);
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void importedCoffinKeepsHeadOccupancy(GameTestHelper helper) {
        var level = helper.getLevel();
        var footPos = helper.absolutePos(new BlockPos(3, 1, 3));
        var headPos = footPos.east();
        var foot = WitcheryBlocks.getRequired("coffinblock").get().defaultBlockState()
                .setValue(CoffinBlock.FACING, Direction.EAST);
        var head = foot.setValue(CoffinBlock.PART, BedPart.HEAD).setValue(CoffinBlock.OCCUPIED, true);
        level.setBlock(footPos, foot, Block.UPDATE_KNOWN_SHAPE);
        level.setBlock(headPos, head, Block.UPDATE_KNOWN_SHAPE);

        var restoredHead = Block.updateFromNeighbourShapes(head, level, headPos);
        var restoredFoot = Block.updateFromNeighbourShapes(foot, level, footPos);
        helper.assertTrue(restoredHead.getValue(CoffinBlock.OCCUPIED) && restoredFoot.getValue(CoffinBlock.OCCUPIED),
                "Post-processing must keep the original head's occupied flag and copy it to the foot");
        level.setBlock(footPos, restoredFoot, Block.UPDATE_KNOWN_SHAPE);
        level.setBlock(headPos, restoredHead.setValue(CoffinBlock.OCCUPIED, false), Block.UPDATE_ALL);
        helper.assertTrue(!level.getBlockState(footPos).getValue(CoffinBlock.OCCUPIED),
                "Waking must clear both halves of the reconstructed coffin");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void importedTormentPortalResolvesOrientationOnce(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(3, 2, 3));
        var portal = WitcheryBlocks.getRequired("tormentportal").get().defaultBlockState()
                .setValue(TormentPortalBlock.LEGACY_ORIENTATION, true);
        level.setBlockAndUpdate(pos.west(), Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(pos.east(), Blocks.AIR.defaultBlockState());

        var open = Block.updateFromNeighbourShapes(portal, level, pos);
        helper.assertTrue(open.getValue(BlockStateProperties.HORIZONTAL_AXIS) == Direction.Axis.Z
                        && !open.getValue(TormentPortalBlock.LEGACY_ORIENTATION),
                "An imported portal with both sides clear faces along Z and consumes its orientation marker");
        level.setBlockAndUpdate(pos.west(), Blocks.STONE.defaultBlockState());
        var framed = Block.updateFromNeighbourShapes(portal, level, pos);
        helper.assertTrue(framed.getValue(BlockStateProperties.HORIZONTAL_AXIS) == Direction.Axis.X
                        && !framed.getValue(TormentPortalBlock.LEGACY_ORIENTATION),
                "A framed imported portal takes the original maze axis");
        helper.assertTrue(Block.updateFromNeighbourShapes(open, level, pos).equals(open),
                "The resolved modern portal keeps its explicit axis when neighboring blocks change");
        helper.succeed();
    }

    private static void assertDoor(GameTestHelper helper, CompoundTag state, String half) {
        var properties = state.getCompound("Properties");
        helper.assertTrue(state.getString("Name").equals("witchery:icedoor") && properties.getString("half").equals(half)
                        && properties.getString("facing").equals("north") && properties.getString("hinge").equals("right")
                        && properties.getString("open").equals("true") && properties.getString("powered").equals("true"),
                "Both door halves should combine the original metadata across sections. Found " + state);
    }
}
