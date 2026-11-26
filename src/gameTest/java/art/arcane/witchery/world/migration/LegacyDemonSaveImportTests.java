package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.AnimalMerchant;
import art.arcane.witchery.entity.Demon;
import art.arcane.witchery.entity.Hobgoblin;
import art.arcane.witchery.entity.Wolfman;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.lycanthropy.WerewolfVillagers;
import art.arcane.witchery.registry.WitcheryEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.util.datafix.DataFixers;
import net.minecraft.world.entity.ai.behavior.ResetProfession;
import net.minecraft.world.entity.animal.Cow;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.chunk.storage.EntityStorage;
import net.minecraft.world.level.chunk.storage.RegionFile;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import org.apache.commons.io.FileUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class LegacyDemonSaveImportTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_unresolved_merchant_stock")
    public static void keepsUnresolvedPaymentsUntilTheRegistryIsAvailable(GameTestHelper helper) throws Exception {
        var root = Files.createTempDirectory("witchery-unresolved-merchant-stock");
        try {
            var directory = Files.createDirectories(root.resolve("entities"));
            var chunk = new ChunkPos(0, 0);
            var input = TagParser.parseTag("""
                    {Position:[I;0,0],Entities:[
                      {id:"witchery:demon",Pos:[4d,70d,4d],Health:100f,PlayerCreated:1b,Bargains:{Recipes:[
                        {buy:{id:"minecraft:diamond",Count:3b},buyB:{id:32321s,Count:2b,Damage:0s,tag:{Marker:"old payment"}},
                         sell:{id:"witchery:demon_heart",Count:1b},uses:2,maxUses:7},
                        {buy:{id:"minecraft:emerald",Count:1b},sell:{id:"minecraft:redstone",Count:1b},uses:1,maxUses:9}]}},
                      {id:"witchery:goblin",Pos:[5d,70d,4d],Profession:1,Offers:{Recipes:[
                        {buy:{id:"minecraft:diamond",Count:3b},buyB:{id:32321s,Count:2b,Damage:0s},
                         sell:{id:"witchery:koboldite_nugget",Count:1b},uses:4,maxUses:8}]}}
                    ]}
                    """);
            writeTradeRegion(directory, chunk, input);
            var saved = new ListTag();
            try (var storage = new EntityStorage(helper.getLevel(), directory, DataFixers.getDataFixer(), true, Runnable::run)) {
                var merchants = storage.loadEntities(chunk).join().getEntities().toList();
                var demon = (Demon) merchants.get(0);
                var goblin = (Hobgoblin) merchants.get(1);
                helper.assertTrue(demon.getOffers().size() == 1 && demon.getOffers().get(0).getResult().is(Items.REDSTONE)
                                && goblin.getOffers().isEmpty(),
                        "An unresolved second payment never becomes a one-payment offer");
                demon.getOffers().get(0).increaseUses();
                saved.add(demon.saveWithoutId(new CompoundTag()));
                saved.add(goblin.saveWithoutId(new CompoundTag()));
            }
            saved.getCompound(0).putString("id", "witchery:demon");
            saved.getCompound(1).putString("id", "witchery:goblin");
            var originals = input.getList("Entities", Tag.TAG_COMPOUND);
            var demonRows = saved.getCompound(0).getCompound("Bargains").getList("Recipes", Tag.TAG_COMPOUND);
            var goblinRows = saved.getCompound(1).getCompound("Offers").getList("Recipes", Tag.TAG_COMPOUND);
            helper.assertTrue(demonRows.getCompound(0).equals(originals.getCompound(0).getCompound("Bargains")
                                    .getList("Recipes", Tag.TAG_COMPOUND).getCompound(0))
                            && goblinRows.equals(originals.getCompound(1).getCompound("Offers").getList("Recipes", Tag.TAG_COMPOUND))
                            && demonRows.getCompound(1).getInt("uses") == 2,
                    "Saving preserves both original pending rows and the use of the available row");

            input.put("Entities", saved);
            writeTradeRegion(directory, chunk, input);
            var registry = TagParser.parseTag("{ItemData:[{V:32321}]}");
            registry.getList("ItemData", Tag.TAG_COMPOUND).getCompound(0).putString("K", "\u0002minecraft:gold_ingot");
            NbtIo.writeCompressed(registry, root.resolve("witchery-legacy-ids.dat").toFile());
            try (var storage = new EntityStorage(helper.getLevel(), directory, DataFixers.getDataFixer(), true, Runnable::run)) {
                var merchants = storage.loadEntities(chunk).join().getEntities().toList();
                var demon = (Demon) merchants.get(0);
                var restored = demon.getOffers().get(0);
                var goblin = (Hobgoblin) merchants.get(1);
                helper.assertTrue(demon.getOffers().size() == 2 && demon.getOffers().get(1).getUses() == 2
                                && restored.getCostB().is(Items.GOLD_INGOT) && restored.getCostB().getCount() == 2
                                && restored.getCostB().getTag().getString("Marker").equals("old payment")
                                && restored.getUses() == 2 && restored.getMaxUses() == 7,
                        "The restored mapping recovers the price, tag and offer order");
                helper.assertTrue(goblin.getOffers().size() == 1 && goblin.getOffers().get(0).getCostB().is(Items.GOLD_INGOT)
                                && goblin.getOffers().get(0).getUses() == 4,
                        "The Goblin recovers its original only offer when both payments are available");
            }
        } finally {
            FileUtils.deleteDirectory(root.toFile());
        }
        helper.succeed();
    }

    private static void writeTradeRegion(Path directory, ChunkPos chunk, CompoundTag input) throws Exception {
        try (var region = new RegionFile(directory.resolve("r.0.0.mca"), directory, true);
             var stream = region.getChunkDataOutputStream(chunk)) {
            NbtIo.write(NbtUtils.addCurrentDataVersion(input), stream);
        }
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_mixed_merchant_stock")
    public static void repairsTradesInExistingEntityRegions(GameTestHelper helper) throws Exception {
        var root = Files.createTempDirectory("witchery-existing-merchant-stock");
        try {
            var registry = TagParser.parseTag("{ItemData:[{V:35},{V:378},{V:388},{V:4096}]}");
            String[] names = {"minecraft:wool", "minecraft:magma_cream", "minecraft:emerald", "witchery:ingredient"};
            var entries = registry.getList("ItemData", Tag.TAG_COMPOUND);
            for (int index = 0; index < names.length; index++) {
                entries.getCompound(index).putString("K", "\u0002" + names[index]);
            }
            NbtIo.writeCompressed(registry, root.resolve("witchery-legacy-ids.dat").toFile());
            var directory = Files.createDirectories(root.resolve("entities"));
            var chunk = new ChunkPos(0, 0);
            var saved = NbtUtils.addCurrentDataVersion(TagParser.parseTag("""
                    {Position:[I;0,0],Entities:[
                       {id:"witchery:demon",Pos:[4d,70d,4d],Health:100f,PlayerCreated:1b,Bargains:{Recipes:[
                         {buy:{id:378s,Count:2b,Damage:0s},buyB:{id:"witchery:ingredient",Count:1b,Damage:24s},
                          sell:{id:4096s,Count:1b,Damage:23s},uses:2,maxUses:7},
                         {buy:{id:"minecraft:emerald",Count:3b},sell:{id:"minecraft:book",Count:1b,
                          tag:{display:{Name:'{"text":"Current book","color":"blue"}'}}},uses:1,maxUses:4}]}},
                       {id:"witchery:goblin",Pos:[5d,70d,4d],Profession:1,Riches:12,Offers:{Recipes:[
                         {buy:{id:388s,Count:2b,Damage:0s},sell:{id:35s,Count:3b,Damage:14s},uses:4,maxUses:8}]}},
                       {id:"witchery:wolfman",Pos:[6d,70d,4d],Infectious:1b,FormerVillager:{Xp:3,
                         VillagerData:{profession:"minecraft:librarian",type:"minecraft:plains",level:1},Offers:{Recipes:[
                           {buy:{id:388s,Count:5b,Damage:0s},sell:{id:"minecraft:wool",Count:2b,Damage:11s},uses:3,maxUses:9}]}}},
                       {id:"minecraft:cow",Pos:[7d,70d,4d],ForgeData:{WitcheryShopStock:{Recipes:[
                         {buy:{id:388s,Count:2b,Damage:0s},sell:{id:35s,Count:1b,Damage:5s},uses:1,maxUses:3}]}}}]}
                    """));
            try (var region = new RegionFile(directory.resolve("r.0.0.mca"), directory, true);
                 var stream = region.getChunkDataOutputStream(chunk)) {
                NbtIo.write(saved, stream);
            }
            try (var storage = new EntityStorage(helper.getLevel(), directory, DataFixers.getDataFixer(), true, Runnable::run)) {
                var loaded = storage.loadEntities(chunk).join();
                var merchants = loaded.getEntities().toList();
                var demon = (Demon) merchants.get(0);
                var heart = demon.getOffers().get(0);
                helper.assertTrue(heart.getBaseCostA().is(Items.MAGMA_CREAM) && heart.getBaseCostA().getCount() == 2
                                && heart.getCostB().is(WitcheryIngredient.BAT_WOOL.item())
                                && heart.getCostB().getCount() == 1 && heart.getResult().is(WitcheryIngredient.DEMON_HEART.item())
                                && heart.getUses() == 2 && heart.getMaxUses() == 7,
                        "Ordinary entity loading restores the original payment, output and uses for the Demon Heart");
                var current = demon.getOffers().get(1);
                helper.assertTrue(current.getResult().save(new CompoundTag()).equals(saved.getList("Entities", Tag.TAG_COMPOUND)
                                .getCompound(0).getCompound("Bargains").getList("Recipes", Tag.TAG_COMPOUND)
                                .getCompound(1).getCompound("sell")) && current.getUses() == 1 && current.getMaxUses() == 4,
                        "A modern offer in the same list retains its styled name and usage history");
                var goblin = (Hobgoblin) merchants.get(1);
                var wool = goblin.getOffers().get(0);
                helper.assertTrue(wool.getBaseCostA().is(Items.EMERALD) && wool.getBaseCostA().getCount() == 2
                                && wool.getResult().is(Items.RED_WOOL) && wool.getResult().getCount() == 3
                                && wool.getUses() == 4 && wool.getMaxUses() == 8,
                        "The Hobgoblin keeps its sold quantity, wool metadata, price and uses");
                var wolfman = (Wolfman) merchants.get(2);
                var former = wolfman.formerVillagerData();
                var formerTrade = new MerchantOffers(former.getCompound("Offers")).get(0);
                helper.assertTrue(wolfman.isInfectious() && former.getInt("Xp") == 3
                                && formerTrade.getBaseCostA().is(Items.EMERALD) && formerTrade.getBaseCostA().getCount() == 5
                                && formerTrade.getResult().is(Items.BLUE_WOOL) && formerTrade.getResult().getCount() == 2
                                && formerTrade.getUses() == 3 && formerTrade.getMaxUses() == 9,
                        "The Wolfman retains the recovered sales alongside its former villager data");
                var cow = (Cow) merchants.get(3);
                var animalTrade = new MerchantOffers(cow.getPersistentData().getCompound(AnimalMerchant.STOCK_TAG)).get(0);
                helper.assertTrue(animalTrade.getBaseCostA().is(Items.EMERALD) && animalTrade.getResult().is(Items.LIME_WOOL)
                                && animalTrade.getUses() == 1 && animalTrade.getMaxUses() == 3,
                        "Repair the animal shop recipe inside Forge's persisted data");
            }
        } finally {
            FileUtils.deleteDirectory(root.toFile());
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_legacy_demon_and_imp")
    public static void preservesLegacyBargainsAndImpOwnership(GameTestHelper helper) throws Exception {
        var registry = TagParser.parseTag("{ItemData:[{V:264},{V:266},{V:388},{V:403},{V:35},{V:4096}]}");
        String[] itemNames = {"minecraft:diamond", "minecraft:gold_ingot", "minecraft:emerald",
                "minecraft:enchanted_book", "minecraft:wool", "witchery:ingredient"};
        var entries = registry.getList("ItemData", Tag.TAG_COMPOUND);
        for (int index = 0; index < itemNames.length; index++) {
            entries.getCompound(index).putString("K", "\u0002" + itemNames[index]);
        }
        var original = TagParser.parseTag("""
                {Level:{xPos:-14,zPos:-68,TerrainPopulated:1b,Entities:[
                  {id:"witchery.demon",CustomName:"Legacy Bargain Demon",UUIDMost:-4820664026303937241L,
                   UUIDLeast:-8737893356131159324L,Pos:[-216.5d,5.5d,-1082.5d],Health:100s,PlayerCreated:1b,
                   Bargains:{Recipes:[
                     {buy:{id:264s,Count:3b,Damage:0s},buyB:{id:266s,Count:2b,Damage:0s},
                      sell:{id:4096s,Count:1b,Damage:23s,tag:{LegacyBargain:"Heart"}},uses:2,maxUses:7},
                     {buy:{id:388s,Count:14b,Damage:0s},sell:{id:403s,Count:1b,Damage:0s,
                      tag:{StoredEnchantments:[{id:16s,lvl:3s}]}},uses:1,maxUses:4}]}},
                  {id:"witchery.wolfman",CustomName:"Legacy Wolf Trader",UUIDMost:3898142634792601333L,
                   UUIDLeast:-8057526008622512232L,Pos:[-214.5d,5.5d,-1082.5d],Health:80s,FormerProfession:1,
                   Infectious:1b,Riches:27,Offers:{Recipes:[{buy:{id:388s,Count:5b,Damage:0s},
                   sell:{id:35s,Count:2b,Damage:14s,tag:{LegacyBargain:"Wool"}},uses:3,maxUses:9}]}},
                  {id:"witchery.imp",CustomName:"Legacy Owned Imp",UUIDMost:-5580138881016446618L,
                   UUIDLeast:-4845020358879725485L,Pos:[-212.5d,5.5d,-1082.5d],Health:50s,
                   OwnerUUID:"25315451-cd27-4f35-a7e1-e7cef7ec05e9",Sitting:1b,Affection:17,SecretsShared:2,
                   HomeLocX:-212,HomeLocY:5,HomeLocZ:-1083}]}}
                """);
        long epochTicks = System.currentTimeMillis() / 50;
        var originalImp = original.getCompound("Level").getList("Entities", Tag.TAG_COMPOUND).getCompound(2);
        originalImp.putLong("LastGiftTime", epochTicks - 1_800);
        originalImp.putLong("PowerUpUntil2", epochTicks + 38_400);
        var root = Files.createTempDirectory("witchery-legacy-demon-and-imp");
        var snapshot = root.resolve("witchery-legacy-ids.dat");
        CompoundTag imported;
        var level = helper.getLevel();
        try {
            NbtIo.writeCompressed(registry, snapshot.toFile());
            var fixer = level.getServer().getFixerUpper();
            var intermediate = LegacyChunkUpgrade.upgrade(DataFixTypes.CHUNK, fixer, original, -1, 1493,
                    LegacyWorldIds.find(root));
            ChunkStorage.injectDatafixingContext(intermediate, Level.OVERWORLD, Optional.empty());
            imported = DataFixTypes.CHUNK.updateToCurrentVersion(fixer, intermediate, 1493);
            LegacyChunkUpgrade.finish(imported, Level.OVERWORLD);
        } finally {
            Files.deleteIfExists(snapshot);
            Files.deleteIfExists(root);
        }

        var entities = imported.getList("entities", Tag.TAG_COMPOUND);
        helper.assertTrue(entities.size() == 3, "All three saved entities must survive the chunk update");
        var demon = WitcheryEntities.DEMON.get().create(level);
        demon.load(entities.getCompound(0));
        var demonSave = demon.saveWithoutId(new CompoundTag());
        demon = WitcheryEntities.DEMON.get().create(level);
        demon.load(demonSave);
        helper.assertTrue(demon.getUUID().equals(new UUID(-4820664026303937241L, -8737893356131159324L))
                        && demonSave.getBoolean("PlayerCreated") && demon.getOffers().size() == 2,
                "Keep the summoned Demon and its saved offers through the modern resave");
        var heart = demon.getOffers().get(0);
        helper.assertTrue(heart.getBaseCostA().is(Items.DIAMOND) && heart.getBaseCostA().getCount() == 3
                        && heart.getCostB().is(Items.GOLD_INGOT) && heart.getCostB().getCount() == 2
                        && heart.getResult().is(WitcheryIngredient.DEMON_HEART.item())
                        && heart.getResult().hasTag()
                        && heart.getResult().getTag().getString("LegacyBargain").equals("Heart")
                        && heart.getUses() == 2 && heart.getMaxUses() == 7,
                "The original two-currency Demon Heart bargain must retain both inputs, output tag and uses");
        var book = demon.getOffers().get(1);
        helper.assertTrue(book.getBaseCostA().is(Items.EMERALD) && book.getBaseCostA().getCount() == 14
                        && book.getResult().is(Items.ENCHANTED_BOOK)
                        && Integer.valueOf(3).equals(EnchantmentHelper.getEnchantments(book.getResult()).get(Enchantments.SHARPNESS)),
                "The numeric enchanted book and its Sharpness enchantment must flatten together");

        var importedWolfman = WitcheryEntities.WOLFMAN.get().create(level);
        importedWolfman.load(entities.getCompound(1));
        var wolfman = WitcheryEntities.WOLFMAN.get().create(level);
        wolfman.load(importedWolfman.saveWithoutId(new CompoundTag()));
        var former = wolfman.formerVillagerData();
        var formerIdentity = new UUID(3898142634792601333L, -8057526008622512232L);
        helper.assertTrue(wolfman.getUUID().equals(formerIdentity) && wolfman.isInfectious()
                        && former.getCompound("VillagerData").getString("profession").equals("minecraft:librarian"),
                "Keep the infected Wolfman's UUID and former librarian profession");
        var offers = new MerchantOffers(former.getCompound("Offers"));
        helper.assertTrue(offers.size() == 1 && offers.get(0).getBaseCostA().is(Items.EMERALD)
                        && offers.get(0).getBaseCostA().getCount() == 5 && offers.get(0).getResult().is(Items.RED_WOOL)
                        && offers.get(0).getResult().getCount() == 2
                        && offers.get(0).getResult().hasTag()
                        && offers.get(0).getResult().getTag().getString("LegacyBargain").equals("Wool")
                        && offers.get(0).getUses() == 3 && offers.get(0).getMaxUses() == 9,
                "The Wolfman's saved villager trade must retain its wool color, tag, price and uses");
        var imp = WitcheryEntities.IMP.get().create(level);
        imp.load(entities.getCompound(2));
        var impSave = imp.saveWithoutId(new CompoundTag());
        imp = WitcheryEntities.IMP.get().create(level);
        imp.load(impSave);
        helper.assertTrue(imp.isTame() && imp.isOrderedToSit()
                        && UUID.fromString("25315451-cd27-4f35-a7e1-e7cef7ec05e9").equals(imp.getOwnerUUID())
                        && imp.hasRestriction() && imp.getRestrictCenter().equals(new BlockPos(-212, 5, -1083))
                        && impSave.getInt("Affection") == 17 && impSave.getInt("SecretsShared") == 2
                        && imp.isPowered() && impSave.getLong("LastGiftTime") == epochTicks - 1_800
                        && impSave.getLong("PowerUpUntil2") == epochTicks + 38_400,
                "The Imp must stay owned, seated and attached to its home with its original gift and contract progress");

        BlockPos spawn = helper.absolutePos(new BlockPos(2, 2, 2));
        wolfman.moveTo(spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5);
        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(level.isPositionEntityTicking(spawn),
                "The Wolfman's spawn position is entity-ticking")).thenExecute(() -> {
            helper.assertTrue(level.addFreshEntity(wolfman) && level.getEntity(formerIdentity) == wolfman,
                    "The imported Wolfman must be tracked under its saved UUID");
            helper.assertTrue(WerewolfVillagers.revert(wolfman, true), "The imported Wolfman should revert normally");
            var resident = WerewolfVillagers.findByTaglock(level.getServer(), formerIdentity);
            helper.assertTrue(resident instanceof Villager, "Find the reverted villager using its original identity");
            var villager = (Villager) resident;
            ResetProfession.create().tryStart(level, villager, level.getGameTime());
            helper.assertTrue(villager.getOffers().size() == 1
                            && villager.getOffers().get(0).getResult().is(Items.RED_WOOL)
                            && villager.getOffers().get(0).getUses() == 3 && villager.getVillagerXp() == 1,
                    "The imported Wolfman's trade should survive reversion and the villager's first job site check");
            villager.discard();
        }).thenSucceed();
    }
}
