package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.item.BrewBagItem;
import art.arcane.witchery.item.LeonardsUrnItem;
import com.mojang.authlib.GameProfile;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
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
public final class LegacyStoredBrewImportTests {
    @GameTest(template = "qa_coven_arena")
    public static void restoresSplashBrewsInsideOriginalBagsAndUrns(GameTestHelper helper) throws Exception {
        var root = Files.createTempDirectory("witchery-stored-brew-import");
        try (var access = LevelStorageSource.createDefault(root).createAccess("world")) {
            var snapshot = TagParser.parseTag("""
                    {ItemData:[{K:"%sminecraft:nether_wart",V:372},{K:"%sminecraft:spider_eye",V:375},
                               {K:"%sminecraft:gunpowder",V:289},{K:"%switchery:brewbottle",V:4103},
                               {K:"%switchery:brewbag",V:4104},{K:"%switchery:leonardsurn",V:4105}]}
                    """.formatted("\u0002", "\u0002", "\u0002", "\u0002", "\u0002", "\u0002"));
            var world = access.getLevelPath(LevelResource.ROOT);
            NbtIo.writeCompressed(snapshot, world.resolve("witchery-legacy-ids.dat").toFile());

            ListTag original = legacyInventory();
            var chunk = TagParser.parseTag("{Level:{xPos:0,zPos:0,TerrainPopulated:1b,TileEntities:[{id:\"Chest\",x:1,y:4,z:1}]}}");
            chunk.getCompound("Level").getList("TileEntities", Tag.TAG_COMPOUND).getCompound(0).put("Items", original.copy());
            var fixer = helper.getLevel().getServer().getFixerUpper();
            try (var storage = new ChunkStorage(world.resolve("region"), fixer, true)) {
                var imported = storage.upgradeChunkTag(Level.OVERWORLD, helper.getLevel()::getDataStorage, chunk, Optional.empty());
                assertBrews(helper, chest(imported));
                restoreOldIngredients(chest(imported), original);
                var repaired = storage.upgradeChunkTag(Level.OVERWORLD, helper.getLevel()::getDataStorage, imported, Optional.empty());
                assertBrews(helper, chest(repaired));
            }

            var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "LegacyBrews"));
            var players = access.createPlayerStorage();
            var file = access.getLevelPath(LevelResource.PLAYER_DATA_DIR).resolve(player.getStringUUID() + ".dat").toFile();
            var legacyPlayer = TagParser.parseTag("{Dimension:0,Health:20f}");
            legacyPlayer.put("Inventory", original.copy());
            NbtIo.writeCompressed(legacyPlayer, file);
            players.load(player);
            assertBrews(helper, player.saveWithoutId(new CompoundTag()).getList("Inventory", Tag.TAG_COMPOUND));

            players.save(player);
            var earlierImport = NbtIo.readCompressed(file);
            restoreOldIngredients(earlierImport.getList("Inventory", Tag.TAG_COMPOUND), original);
            NbtIo.writeCompressed(earlierImport, file);
            players.load(player);
            assertBrews(helper, player.saveWithoutId(new CompoundTag()).getList("Inventory", Tag.TAG_COMPOUND));
        } finally {
            FileUtils.deleteDirectory(root.toFile());
        }
        helper.succeed();
    }

    private static ListTag legacyInventory() throws Exception {
        var bottle = TagParser.parseTag("""
                {id:4103s,Damage:0s,Count:2b,Slot:0b,tag:{Color:4547128,Splash:1b,Items:[
                  {id:372s,Damage:0s,Count:1b},{id:375s,Damage:0s,Count:1b},{id:289s,Damage:0s,Count:1b}]}}
                """);
        var bag = TagParser.parseTag("{id:4104s,Damage:0s,Count:1b,Slot:1b,tag:{WITCColor:5732898,Inventory:{}}}");
        var urn = TagParser.parseTag("{id:4105s,Damage:3s,Count:1b,Slot:2b,tag:{Inventory:{}}}");
        for (var container : new CompoundTag[]{bag, urn}) {
            var stored = bottle.copy();
            stored.putByte("Slot", (byte) 3);
            var items = new ListTag();
            items.add(stored);
            container.getCompound("tag").getCompound("Inventory").put("Items", items);
        }
        var stacks = new ListTag();
        stacks.add(bottle);
        stacks.add(bag);
        stacks.add(urn);
        return stacks;
    }

    private static ListTag chest(CompoundTag chunk) {
        return chunk.getList("block_entities", Tag.TAG_COMPOUND).getCompound(0).getList("Items", Tag.TAG_COMPOUND);
    }

    private static void restoreOldIngredients(ListTag inventory, ListTag original) {
        for (int slot = 1; slot <= 2; slot++) {
            var bottle = inventory.getCompound(slot).getCompound("tag").getCompound("Inventory")
                    .getList("Items", Tag.TAG_COMPOUND).getCompound(0);
            // The earlier importer flattened the bottle's own ID, but left its custom ingredient list intact.
            bottle.getCompound("tag").put("Items", original.getCompound(0).getCompound("tag").getList("Items", Tag.TAG_COMPOUND).copy());
        }
    }

    private static void assertBrews(GameTestHelper helper, ListTag inventory) {
        assertPoison(helper, ItemStack.of(inventory.getCompound(0)), "loose bottle");
        var bag = ItemStack.of(inventory.getCompound(1));
        var urn = ItemStack.of(inventory.getCompound(2));
        helper.assertTrue(BrewBagItem.color(bag) == 5732898 && LeonardsUrnItem.tier(urn) == 3,
                "The Brew Bag must keep its dye and the Urn must keep all four slots");
        assertPoison(helper, BrewBagItem.contents(bag).get(3), "Brew Bag");
        assertPoison(helper, LeonardsUrnItem.contents(urn).get(3), "Leonard's Urn");
    }

    private static void assertPoison(GameTestHelper helper, ItemStack bottle, String holder) {
        var brew = LeonardsUrnItem.splash(bottle);
        helper.assertTrue(bottle.getCount() == 2 && brew != null && brew.color() == 4547128
                        && brew.effects().stream().anyMatch(effect -> effect.effect() == MobEffects.POISON),
                "The " + holder + " must retain both servings of the colored splash Poison brew");
        var ingredients = bottle.getOrCreateTag().getList("Items", Tag.TAG_COMPOUND);
        helper.assertTrue(ingredients.size() == 3 && ingredients.getCompound(0).getString("id").equals("minecraft:nether_wart")
                        && ingredients.getCompound(1).getString("id").equals("minecraft:spider_eye")
                        && ingredients.getCompound(2).getString("id").equals("minecraft:gunpowder"),
                "The " + holder + " must retain the three ingredients in order");
    }
}
