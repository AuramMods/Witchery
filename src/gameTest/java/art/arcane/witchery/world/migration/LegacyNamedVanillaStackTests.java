package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import com.mojang.authlib.GameProfile;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.chunk.storage.EntityStorage;
import net.minecraft.world.level.chunk.storage.RegionFile;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import org.apache.commons.io.FileUtils;

import java.nio.file.Files;
import java.util.Optional;
import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class LegacyNamedVanillaStackTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_named_vanilla_inventory")
    public static void repairsEarlierPlayerInventoriesWithoutRegistry(GameTestHelper helper) throws Exception {
        var root = Files.createTempDirectory("witchery-named-vanilla-inventory");
        try (var access = LevelStorageSource.createDefault(root).createAccess("world")) {
            var storage = access.createPlayerStorage();
            var profile = new GameProfile(UUID.randomUUID(), "SavedVanillaInventory");
            var path = access.getLevelPath(LevelResource.PLAYER_DATA_DIR).resolve(profile.getId() + ".dat").toFile();
            var playerTag = NbtUtils.addCurrentDataVersion(TagParser.parseTag("""
                    {Health:20f,Dimension:"minecraft:overworld",Inventory:[
                      {Slot:0b,id:"minecraft:diamond_sword",Count:1b,Damage:29s,
                       tag:{Trace:"weapon",display:{Name:"Old sword"},ench:[{id:34s,lvl:2s}]}},
                      {Slot:1b,id:"minecraft:wool",Count:2b,Damage:14s},
                      {Slot:2b,id:"minecraft:diamond_sword",Count:1b,Damage:6s,
                       tag:{Damage:31,display:{Name:'{"text":"Current sword","color":"aqua"}'},
                         Enchantments:[{id:"minecraft:sharpness",lvl:3s}],ench:[{id:16s,lvl:1s}],
                         AttributeModifiers:[{AttributeName:"minecraft:generic.attack_damage",Name:"Current offhand blade",
                           Slot:"offhand",UUID:[I;0,51,0,99],UUIDMost:51L,UUIDLeast:98L,Amount:2d,Operation:0}]}},
                      {Slot:3b,id:"minecraft:shulker_box",Count:1b,tag:{BlockEntityTag:{Items:[
                        {Slot:4b,id:"minecraft:planks",Count:4b,Damage:2s,tag:{Trace:"nested"}},
                        {Slot:5b,id:"minecraft:iron_sword",Count:1b,tag:{Damage:9,AttributeModifiers:[
                         {AttributeName:"generic.maxHealth",Name:"Original Vanguard",UUIDMost:23L,UUIDLeast:71L,
                           Amount:4d,Operation:0}]}}]}}},
                      {Slot:4b,id:"witchery:arthana",Count:1b,tag:{Damage:17,AttributeModifiers:[
                         {AttributeName:"minecraft:generic.attackDamage",Name:"Original Altar Cut",UUIDMost:17L,UUIDLeast:41L,
                           Amount:6d,Operation:0}]}}],
                     EnderItems:[{Slot:5b,id:"minecraft:wool",Count:3b,Damage:11s}]}
                    """));
            NbtIo.writeCompressed(playerTag, path);
            var player = new FakePlayer(helper.getLevel(), profile);
            storage.load(player);

            var inventory = player.getInventory();
            var sword = inventory.getItem(0);
            helper.assertTrue(sword.is(Items.DIAMOND_SWORD) && sword.getDamageValue() == 29
                            && sword.getHoverName().getString().equals("Old sword")
                            && sword.getOrCreateTag().getString("Trace").equals("weapon")
                            && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.UNBREAKING, sword) == 2,
                    "Recover weapon wear, custom name, original enchantment and additional data");
            var mixedSword = inventory.getItem(2);
            helper.assertTrue(mixedSword.getDamageValue() == 31
                            && mixedSword.getHoverName().getStyle().getColor().getValue() == 0x55FFFF
                            && EnchantmentHelper.getItemEnchantmentLevel(Enchantments.SHARPNESS, mixedSword) == 3,
                    "Current wear, styled names and named enchantments take precedence");
            var offhand = mixedSword.getAttributeModifiers(EquipmentSlot.OFFHAND).get(Attributes.ATTACK_DAMAGE);
            helper.assertTrue(offhand.size() == 1 && offhand.iterator().next().getId().equals(new UUID(51, 99))
                            && mixedSword.getAttributeModifiers(EquipmentSlot.MAINHAND).isEmpty(),
                    "The explicit equipment slot and UUID take precedence over an original UUID pair");
            var nestedItems = inventory.getItem(3).getOrCreateTag().getCompound("BlockEntityTag")
                    .getList("Items", Tag.TAG_COMPOUND);
            var nested = nestedItems.getCompound(0);
            helper.assertTrue(inventory.getItem(1).is(Items.RED_WOOL) && inventory.getItem(1).getCount() == 2
                            && player.getEnderChestInventory().getItem(5).is(Items.BLUE_WOOL)
                            && player.getEnderChestInventory().getItem(5).getCount() == 3
                            && nested.getByte("Slot") == 4 && ItemStack.of(nested).is(Items.BIRCH_PLANKS)
                            && nested.getCompound("tag").getString("Trace").equals("nested"),
                    "Recover variants in the main inventory, Ender Chest and a nested item");
            var vanguard = ItemStack.of(nestedItems.getCompound(1));
            var vigor = vanguard.getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.MAX_HEALTH);
            var arthana = inventory.getItem(4);
            var attack = arthana.getAttributeModifiers(EquipmentSlot.MAINHAND).get(Attributes.ATTACK_DAMAGE);
            helper.assertTrue(vanguard.is(Items.IRON_SWORD) && vanguard.getDamageValue() == 9 && vigor.size() == 1
                            && vigor.iterator().next().getId().equals(new UUID(23, 71))
                            && vigor.iterator().next().getAmount() == 4
                            && arthana.getDamageValue() == 17 && attack.size() == 1
                            && attack.iterator().next().getId().equals(new UUID(17, 41))
                            && attack.iterator().next().getAmount() == 6
                            && attack.iterator().next().getOperation() == AttributeModifier.Operation.ADDITION,
                    "Previously flattened vanilla and Witchery stacks recover their original equipment bonuses");

            var saved = player.saveWithoutId(new CompoundTag());
            storage.save(player);
            var reopened = new FakePlayer(helper.getLevel(), profile);
            storage.load(reopened);
            var restored = reopened.saveWithoutId(new CompoundTag());
            helper.assertTrue(saved.get("Inventory").equals(restored.get("Inventory"))
                            && saved.get("EnderItems").equals(restored.get("EnderItems")),
                    "Saving and reloading leaves the recovered inventories unchanged");
        } finally {
            FileUtils.deleteDirectory(root.toFile());
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_named_vanilla_regions")
    public static void repairsEarlierContainerAndDroppedStacksWithoutRegistry(GameTestHelper helper) throws Exception {
        var root = Files.createTempDirectory("witchery-named-vanilla-regions");
        var level = helper.getLevel();
        var fixer = level.getServer().getFixerUpper();
        try {
            var earlier = NbtUtils.addCurrentDataVersion(TagParser.parseTag("""
                    {xPos:0,zPos:0,block_entities:[{id:"minecraft:chest",x:1,y:70,z:1,Items:[
                      {id:"minecraft:stone",Damage:5s,Count:6b,Slot:11b,tag:{Trace:"chest"}}]}]}
                    """));
            try (var storage = new ChunkStorage(root.resolve("region"), fixer, true)) {
                var converted = storage.upgradeChunkTag(Level.OVERWORLD, level::getDataStorage, earlier, Optional.empty());
                var stack = converted.getList("block_entities", Tag.TAG_COMPOUND).getCompound(0)
                        .getList("Items", Tag.TAG_COMPOUND).getCompound(0);
                helper.assertTrue(ItemStack.of(stack).is(Items.ANDESITE) && stack.getByte("Count") == 6
                                && stack.getByte("Slot") == 11 && stack.getCompound("tag").getString("Trace").equals("chest"),
                        "Recover the named Stone variant in a saved Chest");
            }

            var directory = Files.createDirectories(root.resolve("entities"));
            var chunk = new ChunkPos(0, 0);
            var entities = NbtUtils.addCurrentDataVersion(TagParser.parseTag("""
                    {Position:[I;0,0],Entities:[{id:"minecraft:item",Pos:[4.5d,70d,4.5d],
                      Item:{id:"minecraft:wool",Damage:4s,Count:5b,tag:{Trace:"dropped"}}}]}
                    """));
            try (var region = new RegionFile(directory.resolve("r.0.0.mca"), directory, true);
                 var stream = region.getChunkDataOutputStream(chunk)) {
                NbtIo.write(entities, stream);
            }
            try (var storage = new EntityStorage(level, directory, fixer, true, Runnable::run)) {
                var loaded = storage.loadEntities(chunk).join();
                var dropped = (ItemEntity) loaded.getEntities().findFirst().orElseThrow();
                helper.assertTrue(dropped.getItem().is(Items.YELLOW_WOOL) && dropped.getItem().getCount() == 5
                                && dropped.getItem().getOrCreateTag().getString("Trace").equals("dropped"),
                        "Recover the saved dropped Wool variant and item data");
                storage.storeEntities(loaded);
                storage.flush(true);
                var reopened = (ItemEntity) storage.loadEntities(chunk).join().getEntities().findFirst().orElseThrow();
                helper.assertTrue(reopened.getUUID().equals(dropped.getUUID())
                                && ItemStack.matches(reopened.getItem(), dropped.getItem()),
                        "The repaired drop and its identity survive a normal entity save");
            }
        } finally {
            FileUtils.deleteDirectory(root.toFile());
        }
        helper.succeed();
    }
}
