package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.entity.CursedControlBlockEntity;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.nio.file.Files;
import java.util.Optional;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class CursedControlSaveImportTests {
    @GameTest(template = "qa_coven_arena")
    public static void recoversChargedControlsAndTheirScheduledTicks(GameTestHelper helper) throws Exception {
        var registry = TagParser.parseTag("""
                {ItemData:[{K:"lever",V:442},{K:"button",V:449},{K:"wart",V:372},{K:"head",V:397},{K:"eye",V:375}]}
                """);
        var entries = registry.getList("ItemData", Tag.TAG_COMPOUND);
        entries.getCompound(0).putString("K", "\u0001witchery:clever");
        entries.getCompound(1).putString("K", "\u0001witchery:cbuttonwood");
        entries.getCompound(2).putString("K", "\u0002minecraft:nether_wart");
        entries.getCompound(3).putString("K", "\u0002minecraft:skull");
        entries.getCompound(4).putString("K", "\u0002minecraft:spider_eye");
        var original = TagParser.parseTag("""
                {Level:{xPos:0,zPos:0,TerrainPopulated:1b,TileEntities:[
                  {id:"witchery:cbuttonstone",x:1,y:0,z:0,Count:1,Thrower:"LegacyBrewer",Effect:{
                    Items:[{id:372s,Damage:0s,Count:1b,Slot:0b},{id:397s,Damage:2s,Count:1b,Slot:1b},
                      {id:375s,Damage:0s,Count:1b,Slot:2b}]}}],
                  TileTicks:[{i:449,x:2,y:0,z:0,t:37,p:-1}]}}
                """);
        byte[] blocks = new byte[4096];
        byte[] highIds = new byte[2048];
        byte[] metadata = new byte[2048];
        blocks[1] = (byte) 442;
        blocks[2] = (byte) 449;
        highIds[0] = 0x10;
        highIds[1] = 1;
        metadata[0] = (byte) 0xA0;
        metadata[1] = 12;
        var section = new CompoundTag();
        section.putByte("Y", (byte) 0);
        section.putByteArray("Blocks", blocks);
        section.putByteArray("Add", highIds);
        section.putByteArray("Data", metadata);
        var sections = new ListTag();
        sections.add(section);
        original.getCompound("Level").put("Sections", sections);

        var root = Files.createTempDirectory("witchery-cursed-control-import");
        var snapshot = root.resolve("witchery-legacy-ids.dat");
        try {
            NbtIo.writeCompressed(registry, snapshot.toFile());
            var level = helper.getLevel();
            var fixer = level.getServer().getFixerUpper();
            var intermediate = LegacyChunkUpgrade.upgrade(DataFixTypes.CHUNK, fixer, original, -1, 1493,
                    LegacyWorldIds.find(root));
            ChunkStorage.injectDatafixingContext(intermediate, Level.OVERWORLD, Optional.empty());
            var imported = DataFixTypes.CHUNK.updateToCurrentVersion(fixer, intermediate, 1493);
            LegacyChunkUpgrade.finish(imported, Level.OVERWORLD);

            var ticks = imported.getList("block_ticks", Tag.TAG_COMPOUND);
            var tick = ticks.getCompound(0);
            helper.assertTrue(ticks.size() == 1 && tick.getString("i").equals("witchery:cbuttonwood")
                            && tick.getInt("x") == 2 && tick.getInt("t") == 37 && tick.getInt("p") == -1,
                    "Restore the button release with its original delay and priority exactly once");
            var tile = imported.getList("block_entities", Tag.TAG_COMPOUND).getCompound(0);
            helper.assertTrue(tile.getString("id").equals("witchery:clever") && tile.getString("Thrower").equals("LegacyBrewer")
                            && tile.contains("Count", Tag.TAG_INT) && tile.getInt("Count") == 1,
                    "Resolve the tile using its block and preserve the numeric charge field");

            var palette = imported.getList("sections", Tag.TAG_COMPOUND).stream().map(CompoundTag.class::cast)
                    .filter(value -> value.getInt("Y") == 0).findFirst().orElseThrow()
                    .getCompound("block_states").getList("palette", Tag.TAG_COMPOUND);
            var encodedLever = palette.stream().map(CompoundTag.class::cast)
                    .filter(state -> state.getString("Name").equals("witchery:clever")).findFirst().orElseThrow();
            var state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), encodedLever);
            helper.assertTrue(state.getValue(BlockStateProperties.ATTACH_FACE) == AttachFace.WALL
                            && state.getValue(BlockStateProperties.HORIZONTAL_FACING) == Direction.WEST
                            && state.getValue(BlockStateProperties.POWERED), "Restore the powered west-facing wall lever");
            var relative = new BlockPos(4, 2, 4);
            BlockPos pos = helper.absolutePos(relative);
            level.setBlockAndUpdate(pos.east(), Blocks.STONE.defaultBlockState());
            level.setBlockAndUpdate(pos, state);
            if (level.getBlockEntity(pos) instanceof CursedControlBlockEntity control) control.load(tile);
            var player = helper.makeMockSurvivalPlayer();
            helper.useBlock(relative, player);
            helper.assertTrue(player.hasEffect(MobEffects.POISON) && level.getBlockState(pos).is(Blocks.LEVER)
                            && !level.getBlockState(pos).getValue(BlockStateProperties.POWERED),
                    "The saved numeric brew affects the player and its last charge restores the lever");
        } finally {
            Files.deleteIfExists(snapshot);
            Files.deleteIfExists(root);
        }
        helper.succeed();
    }
}
