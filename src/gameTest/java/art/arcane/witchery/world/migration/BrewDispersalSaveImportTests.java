package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.BrewDispersalBlock;
import art.arcane.witchery.block.entity.BrewFluidBlockEntity;
import art.arcane.witchery.brewing.BrewMixture;
import art.arcane.witchery.item.WitcheryIngredient;
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
import net.minecraft.util.RandomSource;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class BrewDispersalSaveImportTests {
    @GameTest(template = "qa_coven_arena")
    public static void importedDispersalsResumeWithTheirOwnTicks(GameTestHelper helper) throws Exception {
        var registry = TagParser.parseTag("{ItemData:[{V:442},{V:443}]}");
        var entries = registry.getList("ItemData", Tag.TAG_COMPOUND);
        entries.getCompound(0).putString("K", "\u0001witchery:brewgas");
        entries.getCompound(1).putString("K", "\u0001witchery:brewliquid");
        var original = TagParser.parseTag("""
                {Level:{xPos:0,zPos:0,TerrainPopulated:1b,TileEntities:[
                  {id:"witchery:brewliquid",x:1,y:0,z:0,Color:5465106,Duration:50},
                  {id:"witchery:brewliquid",x:2,y:0,z:0,Color:1193046,Duration:50}],
                  TileTicks:[{i:442,x:1,y:0,z:0,t:4,p:-1}]}}
                """);
        byte[] lowIds = new byte[4096];
        byte[] highIds = new byte[2048];
        byte[] levels = new byte[2048];
        lowIds[1] = (byte) 442;
        lowIds[2] = (byte) 443;
        highIds[0] = 0x10;
        highIds[1] = 1;
        levels[0] = 0x20;
        levels[1] = 4;
        var section = new CompoundTag();
        section.putByte("Y", (byte) 0);
        section.putByteArray("Blocks", lowIds);
        section.putByteArray("Add", highIds);
        section.putByteArray("Data", levels);
        var sections = new ListTag();
        sections.add(section);
        original.getCompound("Level").put("Sections", sections);
        var root = Files.createTempDirectory("witchery-brew-dispersal-import");
        var snapshot = root.resolve("witchery-legacy-ids.dat");
        try {
            NbtIo.writeCompressed(registry, snapshot.toFile());
            var fixer = helper.getLevel().getServer().getFixerUpper();
            var intermediate = LegacyChunkUpgrade.upgrade(DataFixTypes.CHUNK, fixer, original, -1, 1493,
                    LegacyWorldIds.find(root));
            ChunkStorage.injectDatafixingContext(intermediate, Level.OVERWORLD, Optional.empty());
            var imported = DataFixTypes.CHUNK.updateToCurrentVersion(fixer, intermediate, 1493);
            LegacyChunkUpgrade.finish(imported, Level.OVERWORLD);
            var tiles = imported.getList("block_entities", Tag.TAG_COMPOUND);
            helper.assertTrue(tiles.getCompound(0).getString("id").equals("witchery:brewgas")
                            && tiles.getCompound(0).getInt("Color") == 5465106
                            && tiles.getCompound(1).getString("id").equals("witchery:brewliquid"),
                    "Resolve the original shared tile ID from each dispersal block");
            var ticks = imported.getList("block_ticks", Tag.TAG_COMPOUND);
            var gas = ticks.getCompound(0);
            var liquid = ticks.getCompound(1);
            helper.assertTrue(ticks.size() == 2 && gas.getString("i").equals("witchery:brewgas")
                            && gas.getInt("x") == 1 && gas.getInt("t") == 4 && gas.getInt("p") == -1
                            && liquid.getString("i").equals("witchery:brewliquid")
                            && liquid.getInt("x") == 2 && liquid.getInt("t") == 5,
                    "Keep the saved gas tick and start exactly one tick for dormant runoff");
        } finally {
            Files.deleteIfExists(snapshot);
            Files.deleteIfExists(root);
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void liquidBrewStopsAfterItsFifthFlowingBlock(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos source = helper.absolutePos(new BlockPos(3, 2, 4));
        var liquid = (BrewDispersalBlock) WitcheryBlocks.getRequired("brewliquid").get();
        for (int distance = 0; distance <= 6; distance++) {
            BlockPos pos = source.east(distance);
            level.setBlockAndUpdate(pos.below(), Blocks.STONE.defaultBlockState());
            level.setBlockAndUpdate(pos.north(), Blocks.STONE.defaultBlockState());
            level.setBlockAndUpdate(pos.south(), Blocks.STONE.defaultBlockState());
            level.setBlockAndUpdate(pos, distance < 6 ? liquid.defaultBlockState().setValue(LiquidBlock.LEVEL, distance)
                    : Blocks.AIR.defaultBlockState());
        }
        var ingredients = List.of(new ItemStack(Items.NETHER_WART), new ItemStack(Items.SPIDER_EYE),
                new ItemStack(WitcheryIngredient.WORMWOOD.item()));
        var brew = BrewMixture.from(ingredients);
        BlockPos last = source.east(5);
        var fluid = (BrewFluidBlockEntity) level.getBlockEntity(last);
        fluid.initialize(brew.bottleTag(ingredients), brew, brew.color(), null);
        liquid.tick(level.getBlockState(last), level, last, RandomSource.create(2));
        helper.assertTrue(level.getBlockState(last).is(liquid) && level.getBlockState(last.east()).isAir(),
                "On level ground the original liquid fills five cells beyond its source");
        helper.succeed();
    }
}
