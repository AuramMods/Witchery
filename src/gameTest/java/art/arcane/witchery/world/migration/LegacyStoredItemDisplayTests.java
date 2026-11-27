package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.item.BrewBagItem;
import art.arcane.witchery.item.LeonardsUrnItem;
import art.arcane.witchery.item.WitcheryIngredient;
import net.minecraft.SharedConstants;
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
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
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
                var stacks = storedItems(imported);
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
                var repairedItems = storedItems(repaired);
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

    @GameTest(template = "qa_coven_arena")
    public static void earlierPortChestsKeepWitcheryItemTextAndEnchantments(GameTestHelper helper) throws Exception {
        var original = TagParser.parseTag("""
                {xPos:0,zPos:0,block_entities:[{id:"minecraft:chest",x:1,y:4,z:1,Items:[
                  {id:"witchery:brewbag",Count:1b,Slot:0b,tag:{WITCColor:8639516,display:{Name:"Travel Brews"},
                    Inventory:{Items:[
                      {id:"witchery:brewbottle",Count:1b,Slot:6b,tag:{display:{Name:"Winter Flask",Lore:["For the road"]}}},
                      {id:"witchery:brewbottle",Count:1b,Slot:7b,tag:{display:{Name:'{"text":"Spare","color":"gold"}'}}}]} }},
                  {id:"witchery:leonardsurn",Count:1b,Slot:1b,tag:{UrnTier:3,display:{Name:"Leonard's Reserve"},
                    Inventory:{Items:[{id:"witchery:brewbottle",Count:1b,Slot:3b,tag:{display:{Name:"Last Flask"}}}]} }},
                  {id:"witchery:arthana",Count:1b,Slot:2b,tag:{Damage:9,
                    Enchantments:[{id:"minecraft:sharpness",lvl:3s}],ench:[{id:16s,lvl:1s},{id:34s,lvl:2s}]}}
                ]}]}
                """);
        original.putInt("DataVersion", SharedConstants.getCurrentVersion().getDataVersion().getVersion());
        var root = Files.createTempDirectory("witchery-converted-item-text");
        var level = helper.getLevel();
        try {
            for (boolean withRegistry : new boolean[]{false, true}) {
                if (withRegistry) {
                    var snapshot = TagParser.parseTag("{ItemData:[{K:\"\u0002witchery:brewbag\",V:4096}]}");
                    NbtIo.writeCompressed(snapshot, root.resolve("witchery-legacy-ids.dat").toFile());
                }
                try (var storage = new ChunkStorage(root.resolve("region"), level.getServer().getFixerUpper(), true)) {
                    var repaired = storage.upgradeChunkTag(Level.OVERWORLD, level::getDataStorage, original.copy(), Optional.empty());
                    var items = storedItems(repaired);
                    var bag = ItemStack.of(items.getCompound(0));
                    helper.assertTrue(bag.getHoverName().getString().equals("Travel Brews") && BrewBagItem.color(bag) == 8639516,
                            "The chest's Brew Bag should keep its original title and dye");
                    var bagSlots = BrewBagItem.contents(bag);
                    helper.assertTrue(bagSlots.get(6).getHoverName().getString().equals("Winter Flask"),
                            "The Brew Bag should keep the original name of its bottle in slot six");
                    assertLore(helper, bagSlots.get(6), "For the road");
                    helper.assertTrue(bagSlots.get(7).getHoverName().getStyle().getColor().getValue() == 0xFFAA00,
                            "A formatted bottle should retain its current color");
                    var urn = ItemStack.of(items.getCompound(1));
                    helper.assertTrue(urn.getHoverName().getString().equals("Leonard's Reserve")
                                    && LeonardsUrnItem.tier(urn) == 3
                                    && LeonardsUrnItem.contents(urn).get(3).getHoverName().getString().equals("Last Flask"),
                            "The Urn should keep its title, tier and named bottle in the fourth slot");
                    var sword = ItemStack.of(items.getCompound(2));
                    helper.assertTrue(sword.getDamageValue() == 9
                                    && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SHARPNESS, sword) == 3
                                    && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.UNBREAKING, sword) == 2,
                            "The Arthana should keep its wear and current Sharpness and recover the old Unbreaking level");
                    var reopened = storage.upgradeChunkTag(Level.OVERWORLD, level::getDataStorage, repaired.copy(), Optional.empty());
                    helper.assertTrue(items.equals(storedItems(reopened)), "Repeated loading should leave the repaired items unchanged");
                }
            }
        } finally {
            FileUtils.deleteDirectory(root.toFile());
        }
        helper.succeed();
    }

    private static ListTag storedItems(CompoundTag chunk) {
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
