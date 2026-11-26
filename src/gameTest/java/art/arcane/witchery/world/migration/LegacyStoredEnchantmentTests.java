package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryItems;
import net.minecraft.SharedConstants;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
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
public final class LegacyStoredEnchantmentTests {
    @GameTest(template = "qa_coven_arena")
    public static void legacyWitcheryInventoriesKeepAppliedAndStoredEnchantments(GameTestHelper helper) throws Exception {
        var original = TagParser.parseTag("""
                {Level:{xPos:0,zPos:0,TerrainPopulated:1b,TileEntities:[
                  {id:"witchery:grassper",x:1,y:4,z:1,Items:[
                    {id:4096s,Damage:9s,Slot:0b,Count:1b,tag:{Fixture:"worn",ench:[{id:16s,lvl:3s},{id:34s,lvl:2s}]}}]},
                  {id:"witchery:leechchest",x:3,y:4,z:1,Items:[
                    {id:403s,Damage:0s,Slot:4b,Count:1b,tag:{Fixture:"book",StoredEnchantments:[{id:35s,lvl:2s}]}}]}]}}
                """);
        var imported = upgrade(helper, original);
        var arthana = ItemStack.of(stored(imported, "witchery:grassper"));
        helper.assertTrue(arthana.is(WitcheryItems.getRequired("arthana").get()) && arthana.getDamageValue() == 9
                        && arthana.getTag().getString("Fixture").equals("worn"),
                "The imported Arthana should keep its wear and custom tag");
        helper.assertTrue(EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SHARPNESS, arthana) == 3
                        && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.UNBREAKING, arthana) == 2,
                "The Grassper's Arthana should retain usable Sharpness III and Unbreaking II");
        var bookTag = stored(imported, "witchery:leechchest");
        var book = ItemStack.of(bookTag);
        helper.assertTrue(book.is(Items.ENCHANTED_BOOK) && bookTag.getByte("Slot") == 4
                        && book.getTag().getString("Fixture").equals("book")
                        && EnchantmentHelper.getEnchantments(book).getOrDefault(Enchantments.BLOCK_FORTUNE, 0) == 2,
                "The Leech Chest book should retain its slot, marker and usable Fortune II");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void earlierPortInventoriesRepairNumericEnchantmentsOnce(GameTestHelper helper) throws Exception {
        var oldPort = TagParser.parseTag("""
                {xPos:0,zPos:0,block_entities:[
                  {id:"witchery:grassper",x:1,y:4,z:1,Items:[
                    {id:"witchery:arthana",Slot:0b,Count:1b,tag:{Damage:9,Fixture:"worn",
                      ench:[{id:16s,lvl:1s},{id:34s,lvl:2s},{id:120s,lvl:4s}],
                      Enchantments:[{id:"minecraft:sharpness",lvl:3s}]}}]},
                  {id:"witchery:leechchest",x:3,y:4,z:1,Items:[
                    {id:"minecraft:enchanted_book",Slot:4b,Count:1b,tag:{StoredEnchantments:[
                      {id:"minecraft:fortune",lvl:2s},{id:35s,lvl:1s},{id:32s,lvl:4s}]}}]}]}
                """);
        oldPort.putInt("DataVersion", SharedConstants.getCurrentVersion().getDataVersion().getVersion());
        var repaired = upgrade(helper, oldPort);
        var swordTag = stored(repaired, "witchery:grassper");
        var sword = ItemStack.of(swordTag);
        helper.assertTrue(EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SHARPNESS, sword) == 3
                        && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.UNBREAKING, sword) == 2
                        && sword.getTag().getList("ench", Tag.TAG_COMPOUND).size() == 1
                        && sword.getTag().getList("ench", Tag.TAG_COMPOUND).getCompound(0).getShort("id") == 120,
                "Repair should preserve the named level, recover Unbreaking and keep an unresolved mod enchantment");
        var bookTag = stored(repaired, "witchery:leechchest");
        var book = ItemStack.of(bookTag);
        var enchantments = EnchantmentHelper.getEnchantments(book);
        helper.assertTrue(enchantments.getOrDefault(Enchantments.BLOCK_FORTUNE, 0) == 2
                        && enchantments.getOrDefault(Enchantments.BLOCK_EFFICIENCY, 0) == 4
                        && book.getTag().getList("StoredEnchantments", Tag.TAG_COMPOUND).size() == 2,
                "Repair should retain the named Fortune level and recover Efficiency without duplicate entries");
        var reopened = upgrade(helper, repaired);
        helper.assertTrue(swordTag.equals(stored(reopened, "witchery:grassper"))
                        && bookTag.equals(stored(reopened, "witchery:leechchest")),
                "Reopening the repaired data should retain both stacks exactly");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void earlierPortChestsRepairVanillaStacksWithoutReplacingModernData(GameTestHelper helper) throws Exception {
        var oldPort = TagParser.parseTag("""
                {xPos:0,zPos:0,block_entities:[{id:"minecraft:chest",x:1,y:4,z:1,Items:[
                  {id:267s,Damage:27s,Slot:0b,Count:1b,tag:{Fixture:"numeric",
                    display:{Name:"Legacy blade",Lore:["Forged before the port"]},ench:[{id:16s,lvl:3s}]}},
                  {id:"minecraft:iron_sword",Damage:27s,Slot:4b,Count:1b,tag:{Damage:8,Fixture:"mixed",
                    display:{Name:'{"text":"Modern blade","color":"aqua"}',Lore:['{"text":"Still blue","color":"aqua"}']},
                    Enchantments:[{id:"minecraft:sharpness",lvl:3s}],ench:[{id:16s,lvl:1s},{id:34s,lvl:2s}]}},
                  {id:"minecraft:wool",Damage:14s,Slot:8b,Count:3b},
                  {id:"minecraft:netherite_sword",Slot:12b,Count:1b,tag:{Damage:7,Fixture:"current"}}]}]}
                """);
        oldPort.putInt("DataVersion", SharedConstants.getCurrentVersion().getDataVersion().getVersion());
        var currentItem = oldPort.getList("block_entities", Tag.TAG_COMPOUND).getCompound(0)
                .getList("Items", Tag.TAG_COMPOUND).getCompound(3).copy();
        var repaired = upgrade(helper, oldPort);
        var items = repaired.getList("block_entities", Tag.TAG_COMPOUND).getCompound(0).getList("Items", Tag.TAG_COMPOUND);
        var original = ItemStack.of(items.getCompound(0));
        helper.assertTrue(original.is(Items.IRON_SWORD) && original.getDamageValue() == 27
                        && original.getHoverName().getString().equals("Legacy blade")
                        && original.getTag().getString("Fixture").equals("numeric")
                        && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SHARPNESS, original) == 3
                        && original.getTag().getCompound("display").getList("Lore", Tag.TAG_STRING).getString(0)
                            .contains("Forged before the port"),
                "Recover a worn, named and enchanted vanilla sword from its original numeric stack");
        var mixed = ItemStack.of(items.getCompound(1));
        helper.assertTrue(mixed.is(Items.IRON_SWORD) && mixed.getDamageValue() == 8
                        && mixed.getTag().getString("Fixture").equals("mixed")
                        && "{\"text\":\"Modern blade\",\"color\":\"aqua\"}".equals(
                            mixed.getTag().getCompound("display").getString("Name"))
                        && "{\"text\":\"Still blue\",\"color\":\"aqua\"}".equals(
                            mixed.getTag().getCompound("display").getList("Lore", Tag.TAG_STRING).getString(0))
                        && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SHARPNESS, mixed) == 3
                        && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.UNBREAKING, mixed) == 2,
                "Keep explicit current wear, styled text and enchantments when the original fields are also present");
        helper.assertTrue(ItemStack.of(items.getCompound(2)).is(Items.RED_WOOL)
                        && items.getCompound(2).getByte("Count") == 3
                        && items.getCompound(2).getByte("Slot") == 8
                        && currentItem.equals(items.getCompound(3)),
                "Flatten a legacy wool subtype while preserving its count, slot and adjacent current stack");
        var reopened = upgrade(helper, repaired);
        helper.assertTrue(items.equals(reopened.getList("block_entities", Tag.TAG_COMPOUND)
                .getCompound(0).getList("Items", Tag.TAG_COMPOUND)), "Repeated repair leaves the inventory stable");
        helper.succeed();
    }

    private static CompoundTag upgrade(GameTestHelper helper, CompoundTag chunk) throws Exception {
        var root = Files.createTempDirectory("witchery-stored-enchantments");
        var level = helper.getLevel();
        try {
            var snapshot = TagParser.parseTag("""
                    {ItemData:[{K:"\u0002witchery:arthana",V:4096},{K:"\u0002minecraft:enchanted_book",V:403},
                    {K:"\u0002minecraft:iron_sword",V:267},{K:"\u0002minecraft:wool",V:35}]}
                    """);
            NbtIo.writeCompressed(snapshot, root.resolve("witchery-legacy-ids.dat").toFile());
            try (var storage = new ChunkStorage(root.resolve("region"), level.getServer().getFixerUpper(), true)) {
                return storage.upgradeChunkTag(Level.OVERWORLD, level::getDataStorage, chunk, Optional.empty());
            }
        } finally {
            FileUtils.deleteDirectory(root.toFile());
        }
    }

    private static CompoundTag stored(CompoundTag chunk, String type) {
        for (Tag entry : chunk.getList("block_entities", Tag.TAG_COMPOUND)) {
            var tile = (CompoundTag) entry;
            if (tile.getString("id").equals(type)) return tile.getList("Items", Tag.TAG_COMPOUND).getCompound(0);
        }
        throw new IllegalStateException("Missing upgraded " + type);
    }
}
