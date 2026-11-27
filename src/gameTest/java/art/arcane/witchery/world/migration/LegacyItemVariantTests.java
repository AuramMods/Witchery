package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.item.TaglockItem;
import art.arcane.witchery.item.WitcheryIngredient;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class LegacyItemVariantTests {
    @GameTest(template = "qa_coven_arena")
    public static void restoresVariantStacksOnAnOrdinaryLoad(GameTestHelper helper) throws Exception {
        var nativeNames = List.of("witchsapling", "shadedglass", "stockade", "crittersnare", "wickerbundle", "bramble",
                "leonardsurn", "brew.fuel", "vampirebook", "louse", "spectralstone", "plantmine", "wolfhead", "pitdirt",
                "ingredient", "poppet", "alluringskull", "witchhat", "biomenote", "circletalisman");
        int[] metadata = {2, 5, 4, 9, 1, 0, 3, 3, 7, 8194, 33, 7, 1, 2, 26, 6, 1, 7, 2, 17};
        var registry = new CompoundTag();
        var registrations = new ListTag();
        var inventory = new ListTag();
        for (int slot = 0; slot < nativeNames.size(); slot++) {
            String name = "witchery:" + nativeNames.get(slot);
            short id = (short) (1201 + slot);
            var entry = new CompoundTag();
            entry.putString("K", "\u0002" + name);
            entry.putInt("V", id);
            registrations.add(entry);
            var stack = TagParser.parseTag("{Count:1b,tag:{Marker:\"kept\",display:{Name:\"Archive variant\"}}}");
            if (slot % 2 == 0) stack.putShort("id", id);
            else stack.putString("id", name);
            stack.putShort("Damage", (short) metadata[slot]);
            stack.putByte("Slot", (byte) slot);
            inventory.add(stack);
        }
        registry.put("ItemData", registrations);

        var directory = Files.createTempDirectory("witchery-legacy-item-variants");
        var snapshot = directory.resolve("witchery-legacy-ids.dat");
        try {
            NbtIo.writeCompressed(registry, snapshot.toFile());
            var ids = LegacyWorldIds.find(directory);
            var firstImport = inventory.copy();
            ids.upgradeItems(firstImport);
            var fixer = helper.getLevel().getServer().getFixerUpper();
            ids.repairStoredItems(inventory, fixer);
            for (int slot = 0; slot < inventory.size(); slot++) {
                var saved = inventory.getCompound(slot);
                var original = ItemStack.of(firstImport.getCompound(slot));
                var restored = ItemStack.of(saved);
                helper.assertTrue(!original.isEmpty() && restored.is(original.getItem())
                                && !saved.contains("Damage") && saved.getByte("Slot") == slot,
                        "Ordinary loading restores the original stack and slot for " + nativeNames.get(slot));
                helper.assertTrue(restored.getHoverName().getString().equals("Archive variant")
                                && restored.getOrCreateTag().getString("Marker").equals("kept"),
                        "Ordinary loading keeps the name and unrelated item data for " + nativeNames.get(slot));
                restored.removeTagKey("display");
                original.removeTagKey("display");
                helper.assertTrue(ItemStack.matches(original, restored),
                        "First import and later repair resolve the same variant for " + nativeNames.get(slot));
            }
            var saved = inventory.copy();
            ids.repairStoredItems(inventory, fixer);
            helper.assertTrue(saved.equals(inventory), "Reopening the same inventory a second time preserves its converted data");
        } finally {
            Files.deleteIfExists(snapshot);
            Files.deleteIfExists(directory);
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void sharedLoaderKeepsCurrentItemVariants(GameTestHelper helper) throws Exception {
        var stacks = TagParser.parseTag("""
                {Items:[
                  {id:"witchery:crittersnare",Count:1b,tag:{BlockStateTag:{critter:"bat",stocked:"true"}}},
                  {id:"witchery:crittersnare",Count:1b,Damage:9s,tag:{BlockStateTag:{critter:"silverfish",stocked:"false"}}},
                  {id:"witchery:wickerbundle",Count:1b,Damage:1s,tag:{BlockStateTag:{bundle_type:"plain"}}},
                  {id:"witchery:bramble",Count:1b,Damage:1s,tag:{BlockStateTag:{wild:"false"}}},
                  {id:"witchery:leonardsurn",Count:1b,Damage:3s,tag:{UrnTier:0}},
                  {id:"witchery:brew.fuel",Count:1b,Damage:3s,tag:{FuelTier:1}},
                  {id:"witchery:vampirebook",Count:1b,Damage:7s,tag:{VampireBookPages:2}},
                  {id:"witchery:witchhat",Count:1b,Damage:7s,tag:{Damage:3}}
                ]}
                """).getList("Items", Tag.TAG_COMPOUND);
        for (var entry : stacks) {
            var encoded = (CompoundTag) entry;
            var expected = ItemStack.of(encoded.copy());
            var restored = WitcheryIngredient.loadInventoryStack(encoded);
            helper.assertTrue(!restored.isEmpty() && ItemStack.matches(expected, restored),
                    "The shared inventory loader keeps the current variant of " + encoded.getString("id") + " "
                            + expected.getTagElement(BlockItem.BLOCK_STATE_TAG));
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void restoresFilledLegacyTaglockWithoutASignature(GameTestHelper helper) throws Exception {
        var oldKit = TagParser.parseTag("{id:\"witchery:taglockkit\",Count:1b,Damage:1s,tag:{Marker:\"kept\"}}");
        var restored = WitcheryIngredient.loadInventoryStack(oldKit);
        var reopened = ItemStack.of(restored.save(new CompoundTag()));
        helper.assertTrue(TaglockItem.isFilledKit(reopened) && !TaglockItem.isBound(reopened)
                        && "kept".equals(reopened.getOrCreateTag().getString("Marker")),
                "A legacy filled kit keeps its subtype without adding a signature");
        oldKit.putShort("Damage", (short) 0);
        helper.assertFalse(TaglockItem.isFilledKit(WitcheryIngredient.loadInventoryStack(oldKit)),
                "The original empty kit stays empty");
        TaglockItem.clearBinding(reopened, 1);
        helper.assertFalse(TaglockItem.isFilledKit(reopened), "Clearing the kit also clears the imported fill state");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void repairsNamedStacksWithoutInventingNumericMappings(GameTestHelper helper) throws Exception {
        var record = TagParser.parseTag("""
                {Items:[
                  {id:"witchery:ingredient",Slot:0b,Count:2b,tag:{LegacyIngredientDamage:162,display:{Name:"Wooden stake"}}},
                  {id:"witchery:ingredient",Slot:1b,Count:1b,tag:{LegacyIngredientDamage:200,Marker:"unknown"}},
                  {id:1201s,Slot:2b,Count:1b,Damage:2s},
                  {id:"witchery:brewbag",Slot:3b,Count:1b,tag:{Inventory:{Items:[
                    {id:"witchery:ingredient",Slot:0b,Count:1b,Damage:57s}
                  ]}}},
                  {id:"witchery:bound_waystone",Slot:4b,Count:1b,Damage:0s,tag:{PosD:-77,PosX:1,PosY:57,PosZ:4}},
                  {id:"witchery:witchhat",Slot:5b,Count:1b,Damage:7s,tag:{Damage:3,display:{Name:'{"text":"Hat","color":"aqua"}'}}}
                ]}
                """);
        var items = record.getList("Items", Tag.TAG_COMPOUND);
        var unresolved = items.getCompound(1).copy();
        var numeric = items.getCompound(2).copy();
        var location = items.getCompound(4).getCompound("tag").copy();
        var hat = items.getCompound(5).getCompound("tag").copy();
        LegacyWorldIds.repairNamedStoredItems(record);

        var restored = ItemStack.of(items.getCompound(0));
        helper.assertTrue(restored.is(WitcheryIngredient.WOODEN_STAKE.item()) && restored.getCount() == 2
                        && restored.getHoverName().getString().equals("Wooden stake"),
                "Recover the named fallback subtype and its literal name and count");
        helper.assertTrue(items.getCompound(1).equals(unresolved) && items.getCompound(2).equals(numeric)
                        && items.getCompound(4).getCompound("tag").equals(location),
                "Keep unknown subtypes, numeric registry IDs and numeric destinations for the captured table");
        var contents = items.getCompound(3).getCompound("tag").getCompound("Inventory")
                .getList("Items", Tag.TAG_COMPOUND).getCompound(0);
        helper.assertTrue(ItemStack.of(contents).is(WitcheryIngredient.BREW_OF_VINES.item()),
                "Repair named stacks inside a bag item");
        helper.assertTrue(items.getCompound(5).getCompound("tag").equals(hat),
                "Keep explicit current wear and styled text");
        var saved = record.copy();
        LegacyWorldIds.repairNamedStoredItems(record);
        helper.assertTrue(record.equals(saved), "A second load preserves the resulting item data");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena")
    public static void restoresTextInsideSavedFlightRoutes(GameTestHelper helper) throws Exception {
        var original = TagParser.parseTag("""
                {Level:{xPos:5,zPos:1,TerrainPopulated:1b,Entities:[
                  {id:"witchery.owl",WITCWaypoint:{id:"witchery:ingredient",Count:1b,Damage:13s,
                    tag:{PosD:0,PosX:45,PosY:67,PosZ:72,display:{Name:'{"text":"Literal"}',Lore:["Old route"]}}}}
                ]}}
                """);
        var folder = Files.createTempDirectory("witchery-legacy-flight-route");
        var snapshot = folder.resolve("witchery-legacy-ids.dat");
        var level = helper.getLevel();
        var fixer = level.getServer().getFixerUpper();
        try {
            NbtIo.writeCompressed(new CompoundTag(), snapshot.toFile());
            var ids = LegacyWorldIds.find(folder);
            CompoundTag route;
            try (var storage = new ChunkStorage(folder.resolve("region"), fixer, true)) {
                var saved = storage.upgradeChunkTag(Level.OVERWORLD, level::getDataStorage, original, Optional.empty());
                route = saved.getList("entities", Tag.TAG_COMPOUND).getCompound(0).getCompound("WITCWaypoint");
            }
            helper.assertTrue(route.getString("id").equals("witchery:bound_waystone")
                            && ItemStack.of(route).getHoverName().getString().equals("{\"text\":\"Literal\"}")
                            && route.getCompound("tag").getInt("PosX") == 45,
                    "The original Waystone keeps its destination and the literal, component-shaped name");
            helper.assertTrue(routeLore(route).equals("Old route"), "Convert the original route lore to text components");

            var record = TagParser.parseTag("""
                    {id:"minecraft:boat",Passengers:[{id:"witchery:wingedmonkey",
                      WITCWaypoint:{id:"witchery:taglockkit",Count:1b,tag:{WITCPlayer1:"Owner",
                        display:{Name:'{"text":"Taglock","color":"aqua"}',Lore:["Old route"]}}}}]}
                    """);
            var withoutRegistry = record.copy();
            ids.repairStoredItems(record, fixer);
            LegacyWorldIds.repairNamedStoredItems(withoutRegistry);
            var nested = record.getList("Passengers", Tag.TAG_COMPOUND).getCompound(0).getCompound("WITCWaypoint");
            helper.assertTrue(record.equals(withoutRegistry) && routeLore(nested).equals("Old route")
                            && nested.getCompound("tag").getString("WITCPlayer1").equals("Owner")
                            && nested.getCompound("tag").getCompound("display").getString("Name")
                            .equals("{\"text\":\"Taglock\",\"color\":\"aqua\"}"),
                    "Ordinary loads restore the nested lore and keep the current name and target with or without a registry");
            var converted = record.copy();
            ids.repairStoredItems(record, fixer);
            helper.assertTrue(record.equals(converted), "Reopening the saved journey preserves its repaired text");
        } finally {
            Files.deleteIfExists(snapshot);
            Files.deleteIfExists(folder);
        }
        helper.succeed();
    }

    private static String routeLore(CompoundTag route) {
        var lore = route.getCompound("tag").getCompound("display").getList("Lore", Tag.TAG_STRING);
        var text = Component.Serializer.fromJson(lore.getString(0));
        return text == null ? "" : text.getString();
    }
}
