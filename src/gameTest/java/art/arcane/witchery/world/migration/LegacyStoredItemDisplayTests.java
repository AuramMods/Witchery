package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.item.WitcheryIngredient;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import org.apache.commons.io.FileUtils;

import java.nio.file.Files;
import java.util.Optional;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class LegacyStoredItemDisplayTests {
    @GameTest(template = "qa_coven_arena")
    public static void preservesLegacyFumeNamesAndLore(GameTestHelper helper) throws Exception {
        var root = Files.createTempDirectory("witchery-stored-item-text");
        var level = helper.getLevel();
        try {
            var snapshot = TagParser.parseTag("""
                    {ItemData:[{K:"\u0002witchery:ingredient",V:4096},{K:"\u0002minecraft:book",V:340}]}
                    """);
            NbtIo.writeCompressed(snapshot, root.resolve("witchery-legacy-ids.dat").toFile());
            var original = TagParser.parseTag("""
                    {Level:{xPos:0,zPos:0,TerrainPopulated:1b,TileEntities:[
                      {id:"witchery:witchesovenidle",x:1,y:4,z:1,Items:[
                        {id:4096s,Damage:34s,Slot:3b,Count:2b,
                         tag:{Fixture:"first-batch",display:{Name:'{"text":"Literal fume"}',Lore:["First Batch"]}}},
                        {id:340s,Damage:0s,Slot:2b,Count:1b,tag:{display:{Name:"Oven Book",Lore:["Book Lore"]}}}]}]}}
                    """);
            try (var storage = new ChunkStorage(root.resolve("region"), level.getServer().getFixerUpper(), true)) {
                var imported = storage.upgradeChunkTag(Level.OVERWORLD, level::getDataStorage, original, Optional.empty());
                var stacks = ovenItems(imported);
                var stack = stacks.getCompound(0);
                assertDisplay(helper, stack, "{\"text\":\"Literal fume\"}");
                assertBook(helper, stacks.getCompound(1));

                var display = stack.getCompound("tag").getCompound("display");
                display.putString("Name", "Legacy fume");
                ListTag lore = display.getList("Lore", Tag.TAG_STRING);
                lore.set(0, StringTag.valueOf("First Batch"));
                var styledLine = StringTag.valueOf("{\"text\":\"Already formatted\",\"color\":\"gold\"}");
                lore.add(styledLine);
                stacks.getCompound(1).getCompound("tag").getCompound("display").getList("Lore", Tag.TAG_STRING)
                        .set(0, StringTag.valueOf("Book Lore"));
                var repaired = storage.upgradeChunkTag(Level.OVERWORLD, level::getDataStorage, imported, Optional.empty());
                var repairedItems = ovenItems(repaired);
                var repairedStack = repairedItems.getCompound(0);
                assertDisplay(helper, repairedStack, "Legacy fume");
                assertBook(helper, repairedItems.getCompound(1));
                helper.assertTrue(repairedStack.getCompound("tag").getCompound("display")
                                .getList("Lore", Tag.TAG_STRING).get(1).equals(styledLine),
                        "Previously converted components should retain their original styling");
            }
        } finally {
            FileUtils.deleteDirectory(root.toFile());
        }
        helper.succeed();
    }

    private static ListTag ovenItems(CompoundTag chunk) {
        return chunk.getList("block_entities", Tag.TAG_COMPOUND).getCompound(0).getList("Items", Tag.TAG_COMPOUND);
    }

    private static void assertDisplay(GameTestHelper helper, CompoundTag tag, String expectedName) {
        ItemStack stack = ItemStack.of(tag);
        helper.assertTrue(stack.is(WitcheryIngredient.WHIFF_OF_MAGIC.item()) && stack.getCount() == 2
                        && tag.getByte("Slot") == 3 && stack.getTag().getString("Fixture").equals("first-batch"),
                "The oven output should retain the fume subtype, count, slot, and custom data");
        helper.assertTrue(stack.getHoverName().getString().equals(expectedName), "The oven output should keep its literal name");
        assertLore(helper, stack, "First Batch");
    }

    private static void assertBook(GameTestHelper helper, CompoundTag tag) {
        ItemStack book = ItemStack.of(tag);
        helper.assertTrue(book.is(Items.BOOK) && book.getHoverName().getString().equals("Oven Book"),
                "The oven should keep the vanilla book and its custom name");
        assertLore(helper, book, "Book Lore");
    }

    private static void assertLore(GameTestHelper helper, ItemStack stack, String expected) {
        var lore = stack.getTag().getCompound("display").getList("Lore", Tag.TAG_STRING);
        var line = Component.Serializer.fromJson(lore.getString(0));
        helper.assertTrue(line != null && line.getString().equals(expected), "The original lore should render as a chat component");
    }
}
