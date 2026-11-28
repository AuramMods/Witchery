package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.AlluringSkullBlock;
import art.arcane.witchery.block.MountedSkullBlock;
import art.arcane.witchery.block.WolfHeadBlock;
import net.minecraft.SharedConstants;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.util.Mth;
import net.minecraft.util.SimpleBitStorage;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import org.apache.commons.io.FileUtils;

import java.nio.file.Files;
import java.util.Optional;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class MountedSkullSaveImportTests {
    @GameTest(template = "qa_coven_arena")
    public static void earlierChunksRestoreOnlyMissingSkullProperties(GameTestHelper helper) throws Exception {
        var chunk = TagParser.parseTag("""
                {xPos:0,zPos:0,sections:[
                  {Y:-3b,block_states:{palette:[
                    {Name:"minecraft:air"},{Name:"witchery:alluringskull"},
                    {Name:"witchery:wolfhead",Properties:{facing:"west"}},
                    {Name:"witchery:alluringskull",Properties:{active:"false",rotation:"3",facing:"up"}},
                    {Name:"minecraft:stone"},{Name:"minecraft:granite"},{Name:"minecraft:diorite"},
                    {Name:"minecraft:andesite"},{Name:"minecraft:grass_block"},{Name:"minecraft:podzol"},
                    {Name:"minecraft:gravel"},{Name:"minecraft:clay"},{Name:"minecraft:acacia_log"},
                    {Name:"minecraft:gold_block"},{Name:"minecraft:emerald_block"},{Name:"minecraft:bedrock"}]}},
                  {Y:2b,block_states:{palette:[{Name:"witchery:wolfhead",Properties:{hellhound:"false"}}]}}
                ],block_entities:[
                  {id:"witchery:alluringskull",x:1,y:-35,z:2,SkullType:1b,Rot:7b},
                  {id:"witchery:alluringskull",x:2,y:-35,z:2,SkullType:0b,Rot:13b},
                  {id:"witchery:wolfhead",x:3,y:-35,z:2,SkullType:1b,Rot:5b},
                  {id:"witchery:alluringskull",x:4,y:-35,z:2,SkullType:1b,Rot:0b},
                  {id:"witchery:alluringskull",x:5,y:-35,z:2,SkullType:1b,Rot:3b},
                  {id:"witchery:wolfhead",x:1,y:32,z:2,SkullType:1b,Rot:11b}
                ]}
                """);
        chunk.putInt("DataVersion", SharedConstants.getCurrentVersion().getDataVersion().getVersion());
        var encoded = new SimpleBitStorage(4, 4096);
        int row = (13 << 8) | (2 << 4);
        encoded.set(row + 1, 1);
        encoded.set(row + 2, 1);
        encoded.set(row + 3, 2);
        encoded.set(row + 4, 3);
        encoded.set(row + 5, 4);
        encoded.set(row + 6, 1);
        encoded.set(4095, 15);
        chunk.getList("sections", Tag.TAG_COMPOUND).getCompound(0).getCompound("block_states").putLongArray("data", encoded.getRaw());

        var level = helper.getLevel();
        var root = Files.createTempDirectory("witchery-mounted-skulls");
        try (var storage = new ChunkStorage(root.resolve("region"), level.getServer().getFixerUpper(), true)) {
            var migrated = storage.upgradeChunkTag(Level.OVERWORLD, level::getDataStorage, chunk, Optional.empty());
            helper.assertTrue(migrated.getBoolean("shouldSave"), "The repaired chunk is scheduled to be saved after loading");
            var sections = migrated.getList("sections", Tag.TAG_COMPOUND);
            var lower = sections.getCompound(0);
            var active = stateAt(lower, row + 1);
            var inactive = stateAt(lower, row + 2);
            var hound = stateAt(lower, row + 3);
            helper.assertTrue(active.getValue(AlluringSkullBlock.ACTIVE) && active.getValue(MountedSkullBlock.ROTATION) == 7
                            && !inactive.getValue(AlluringSkullBlock.ACTIVE) && inactive.getValue(MountedSkullBlock.ROTATION) == 13,
                    "Distinct old tile payloads split their shared floor-skull palette entry");
            helper.assertTrue(hound.getValue(WolfHeadBlock.HELLHOUND) && hound.getValue(MountedSkullBlock.FACING) == Direction.WEST
                            && hound.getValue(MountedSkullBlock.ROTATION) == 0,
                    "The wall trophy keeps its attachment and restores its species");
            var current = stateAt(lower, row + 4);
            var noTile = stateAt(lower, row + 6);
            helper.assertTrue(!current.getValue(AlluringSkullBlock.ACTIVE) && current.getValue(MountedSkullBlock.ROTATION) == 3
                            && !noTile.getValue(AlluringSkullBlock.ACTIVE) && noTile.getValue(MountedSkullBlock.ROTATION) == 0,
                    "Explicit current state and another cell sharing the old palette entry remain intact");
            helper.assertTrue(stateAt(lower, row + 5).is(Blocks.STONE) && stateAt(lower, 4095).is(Blocks.BEDROCK),
                    "Repacking the palette keeps both the mismatched tile's block and the last cell");
            var single = stateAt(sections.getCompound(1), (2 << 4) | 1);
            helper.assertTrue(!single.getValue(WolfHeadBlock.HELLHOUND) && single.getValue(MountedSkullBlock.ROTATION) == 11,
                    "A single-entry palette restores missing rotation and keeps explicit modern species");
            var reopened = storage.upgradeChunkTag(Level.OVERWORLD, level::getDataStorage, migrated.copy(), Optional.empty());
            helper.assertTrue(reopened.getList("sections", Tag.TAG_COMPOUND).equals(sections),
                    "Reopening the migrated state applies the recovery only once");
        } finally {
            FileUtils.deleteDirectory(root.toFile());
        }
        helper.succeed();
    }

    private static BlockState stateAt(CompoundTag section, int index) {
        var blocks = section.getCompound("block_states");
        var palette = blocks.getList("palette", Tag.TAG_COMPOUND);
        var storage = new SimpleBitStorage(Math.max(4, Mth.ceillog2(palette.size())), 4096, blocks.getLongArray("data"));
        return NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), palette.getCompound(storage.get(index)));
    }
}
