package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.util.Mth;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.datafix.PackedBitStorage;
import net.minecraft.world.level.Level;
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
                  {id:"witchery:witchsladder",x:7,y:0,z:0,EffectTypeID:3,Spectral:1b}]}
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
            ChunkStorage.injectDatafixingContext(intermediate, Level.OVERWORLD, Optional.empty());
            var upgraded = DataFixTypes.CHUNK.updateToCurrentVersion(fixer, intermediate, 1493);
            var queued = upgraded.getList("PostProcessing", Tag.TAG_LIST);
            helper.assertTrue(queued.getList(4).equals(TagParser.parseTag("{Offsets:[12s,13s,14s,15s]}").getList("Offsets", Tag.TAG_SHORT))
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

    private static void assertDoor(GameTestHelper helper, CompoundTag state, String half) {
        var properties = state.getCompound("Properties");
        helper.assertTrue(state.getString("Name").equals("witchery:icedoor") && properties.getString("half").equals(half)
                        && properties.getString("facing").equals("north") && properties.getString("hinge").equals("right")
                        && properties.getString("open").equals("true") && properties.getString("powered").equals("true"),
                "Both door halves should combine the original metadata across sections. Found " + state);
    }
}
