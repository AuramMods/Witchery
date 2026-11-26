package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.WereVillager;
import art.arcane.witchery.entity.Wolfman;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.lycanthropy.WerewolfVillagers;
import art.arcane.witchery.registry.WitcheryEntities;
import art.arcane.witchery.vampirism.BloodFeeding;
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
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.behavior.ResetProfession;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraft.world.level.chunk.storage.EntityStorage;
import net.minecraft.world.level.chunk.storage.RegionFile;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;
import org.apache.commons.io.FileUtils;

import java.nio.file.Files;
import java.util.Optional;
import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class LegacyWereVillagerSaveImportTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_pending_werevillager_offers")
    public static void keepsPendingStockThroughReversionAndCure(GameTestHelper helper) throws Exception {
        var wolfmanTag = TagParser.parseTag("""
                {FormerProfession:1,Infectious:1b,Offers:{Recipes:[
                  {buy:{id:"minecraft:emerald",Count:3b},buyB:{id:32321s,Count:2b,Damage:0s,tag:{Marker:"old price"}},
                   sell:{id:"minecraft:book",Count:1b},uses:2,maxUses:7},
                  {buy:{id:"minecraft:emerald",Count:1b},buyB:{id:"minecraft:air",Count:13b,tag:{Marker:"empty price"}},
                   sell:{id:"minecraft:paper",Count:1b},uses:1,maxUses:9}]}}
                """);
        var recipes = wolfmanTag.getCompound("Offers").getList("Recipes", Tag.TAG_COMPOUND);
        var pending = recipes.getCompound(0).copy();
        var level = helper.getLevel();
        var wolfman = WitcheryEntities.WOLFMAN.get().create(level);
        wolfman.load(wolfmanTag);
        var identity = wolfman.getUUID();
        var pos = helper.absolutePos(new BlockPos(2, 2, 2));
        wolfman.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        wolfman.setNoAi(true);
        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(level.isPositionEntityTicking(pos),
                "The Wolfman's spawn position is entity-ticking")).thenExecute(() -> {
            try {
                helper.assertTrue(level.addFreshEntity(wolfman) && WerewolfVillagers.revert(wolfman, true),
                        "Revert the saved Wolfman through its ordinary conversion");
                var were = (WereVillager) WerewolfVillagers.findByTaglock(level.getServer(), identity);
                helper.assertTrue(were.getOffers().size() == 1 && were.getOffers().get(0).getResult().is(Items.PAPER),
                        "The infected librarian exposes only the resolved offer");
                were.getOffers().get(0).increaseUses();
                helper.assertTrue(WerewolfVillagers.transform(were, true), "Return to Wolfman form with the used trade");
                var transformed = (Wolfman) WerewolfVillagers.findByTaglock(level.getServer(), identity);
                helper.assertTrue(WerewolfVillagers.revert(transformed, true), "Revert with the same saved stock again");
                were = (WereVillager) WerewolfVillagers.findByTaglock(level.getServer(), identity);
                helper.assertTrue(WerewolfVillagers.cleanse(were), "Cure the infected librarian through its normal conversion");
                var cured = (Villager) WerewolfVillagers.findByTaglock(level.getServer(), identity);
                var saved = cured.saveWithoutId(new CompoundTag());
                saved.putString("id", "minecraft:villager");
                var curedRows = saved.getCompound("Offers").getList("Recipes", Tag.TAG_COMPOUND);
                helper.assertTrue(cured.getType() == EntityType.VILLAGER && cured.getOffers().size() == 1
                                && curedRows.getCompound(0).equals(pending) && curedRows.getCompound(1).getInt("uses") == 2,
                        "The cure and the save retain the original pending row and updated available offer");

                var pendingOnly = EntityType.VILLAGER.create(level);
                var only = saved.copy();
                only.getCompound("Offers").getList("Recipes", Tag.TAG_COMPOUND).remove(1);
                pendingOnly.load(only);
                helper.assertTrue(pendingOnly.getOffers().isEmpty()
                                && pendingOnly.saveWithoutId(new CompoundTag()).getCompound("Offers").equals(only.getCompound("Offers")),
                        "A vanilla villager saves its pending list even without available trades");
                try {
                    verifyRecoveredTrade(helper, saved);
                } catch (java.io.IOException failure) {
                    throw new IllegalStateException("Verify the reverted villager through entity storage", failure);
                }
            } finally {
                var current = WerewolfVillagers.findByTaglock(level.getServer(), identity);
                if (current != null) current.discard();
                wolfman.discard();
            }
        }).thenSucceed();
    }

    private static void verifyRecoveredTrade(GameTestHelper helper, CompoundTag villager) throws java.io.IOException {
        var root = Files.createTempDirectory("witchery-reverted-merchant-stock");
        try {
            var registry = new CompoundTag();
            var entry = new CompoundTag();
            entry.putInt("V", 32321);
            entry.putString("K", "\u0002minecraft:gold_ingot");
            var entries = new ListTag();
            entries.add(entry);
            registry.put("ItemData", entries);
            NbtIo.writeCompressed(registry, root.resolve("witchery-legacy-ids.dat").toFile());
            var directory = Files.createDirectories(root.resolve("entities"));
            var chunk = new ChunkPos(0, 0);
            var entities = new ListTag();
            entities.add(villager);
            var data = NbtUtils.addCurrentDataVersion(new CompoundTag());
            data.putIntArray("Position", new int[]{0, 0});
            data.put("Entities", entities);
            try (var region = new RegionFile(directory.resolve("r.0.0.mca"), directory, true);
                 var stream = region.getChunkDataOutputStream(chunk)) {
                NbtIo.write(data, stream);
            }
            try (var storage = new EntityStorage(helper.getLevel(), directory, DataFixers.getDataFixer(), true, Runnable::run)) {
                var restored = (Villager) storage.loadEntities(chunk).join().getEntities().findFirst().orElseThrow();
                var trade = restored.getOffers().get(0);
                helper.assertTrue(restored.getOffers().size() == 2 && restored.getOffers().get(1).getUses() == 2
                                && trade.getCostB().is(Items.GOLD_INGOT) && trade.getCostB().getCount() == 2
                                && trade.getCostB().hasTag() && trade.getCostB().getTag().getString("Marker").equals("old price")
                                && trade.getUses() == 2 && trade.getMaxUses() == 7,
                        "Ordinary region loading recovers the marked second payment and the original trade history");
            }
        } finally {
            FileUtils.deleteDirectory(root.toFile());
        }
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_legacy_werevillager")
    public static void importsLegacyInfectedVillagerWithItsTradeStock(GameTestHelper helper) throws Exception {
        var registry = TagParser.parseTag("{ItemData:[{V:4096}]}");
        registry.getList("ItemData", Tag.TAG_COMPOUND).getCompound(0).putString("K", "\u0002witchery:ingredient");
        var original = TagParser.parseTag("""
                {Level:{xPos:-14,zPos:-68,TerrainPopulated:1b,Entities:[
                  {id:"witchery.werevillager",CustomName:"Legacy Infected Librarian",UUIDMost:3274408037664640634L,
                   UUIDLeast:-8273213417301270937L,Pos:[-211.5d,5.5d,-1080.5d],Health:20s,
                   Infectious:1b,WitcheryExtendedVillager:{Blood:77},Age:0,Profession:1,Riches:11,Offers:{Recipes:[
                     {buy:{id:35s,Count:4b,Damage:14s,tag:{PortMarker:"Wool"}},buyB:{id:388s,Count:6b,Damage:0s},
                      sell:{id:403s,Count:1b,Damage:0s,tag:{StoredEnchantments:[{id:16s,lvl:3s}]}},uses:3,maxUses:9},
                     {buy:{id:388s,Count:1b,Damage:0s},sell:{id:4096s,Count:1b,Damage:23s,tag:{PortMarker:"Gift"}},
                      uses:0,maxUses:7}]}}]}}
                """);
        var root = Files.createTempDirectory("witchery-legacy-werevillager");
        var snapshot = root.resolve("witchery-legacy-ids.dat");
        var level = helper.getLevel();
        CompoundTag imported;
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

        var entity = imported.getList("entities", Tag.TAG_COMPOUND).getCompound(0);
        var importedVillager = WitcheryEntities.WERE_VILLAGER.get().create(level);
        importedVillager.load(entity);
        var wereVillager = WitcheryEntities.WERE_VILLAGER.get().create(level);
        wereVillager.load(importedVillager.saveWithoutId(new CompoundTag()));
        var originalIdentity = new UUID(3274408037664640634L, -8273213417301270937L);
        helper.assertTrue(wereVillager.getUUID().equals(originalIdentity)
                        && wereVillager.saveWithoutId(new CompoundTag()).getBoolean("Infectious"),
                "The infected villager must keep its original UUID and infectious flag");
        helper.assertTrue(BloodFeeding.blood(wereVillager) == 77,
                "An imported infected villager must retain its original blood after an ordinary save");
        assertTrades(helper, wereVillager);

        BlockPos spawn = helper.absolutePos(new BlockPos(2, 2, 2));
        wereVillager.moveTo(spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5);
        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(level.isPositionEntityTicking(spawn),
                "The Werevillager's spawn position is entity-ticking")).thenExecute(() -> {
            helper.assertTrue(level.addFreshEntity(wereVillager) && level.getEntity(originalIdentity) == wereVillager,
                    "The migrated werevillager must be tracked under its saved UUID");
            helper.assertTrue(WerewolfVillagers.transform(wereVillager, true),
                    "The migrated werevillager must still transform through the ordinary conversion");
            var wolfman = WerewolfVillagers.findByTaglock(level.getServer(), originalIdentity);
            helper.assertTrue(wolfman instanceof Wolfman wolf && WerewolfVillagers.revert(wolf, false),
                    "The transformed villager must remain curable through its original identity");
            var restored = WerewolfVillagers.findByTaglock(level.getServer(), originalIdentity);
            helper.assertTrue(restored instanceof Villager && restored.getType() == EntityType.VILLAGER,
                    "The cured mob must be a normal villager");
            var cured = (Villager) restored;
            ResetProfession.create().tryStart(level, cured, level.getGameTime());
            assertTrades(helper, cured);
            cured.discard();
        }).thenSucceed();
    }

    private static void assertTrades(GameTestHelper helper, Villager villager) {
        helper.assertTrue(villager.getVillagerData().getProfession() == VillagerProfession.LIBRARIAN
                        && villager.getOffers().size() == 2,
                "The original profession and both offers must remain active");
        var books = villager.getOffers().get(0);
        helper.assertTrue(books.getBaseCostA().is(Items.RED_WOOL) && books.getBaseCostA().getCount() == 4
                        && books.getBaseCostA().hasTag() && books.getBaseCostA().getTag().getString("PortMarker").equals("Wool")
                        && books.getCostB().is(Items.EMERALD) && books.getCostB().getCount() == 6
                        && books.getResult().is(Items.ENCHANTED_BOOK)
                        && Integer.valueOf(3).equals(EnchantmentHelper.getEnchantments(books.getResult()).get(Enchantments.SHARPNESS))
                        && books.getUses() == 3 && books.getMaxUses() == 9,
                "Keep both currencies, the marked wool, Sharpness III and the saved uses");
        var gift = villager.getOffers().get(1);
        helper.assertTrue(gift.getResult().is(WitcheryIngredient.DEMON_HEART.item()) && gift.getResult().hasTag()
                        && gift.getResult().getTag().getString("PortMarker").equals("Gift") && gift.getMaxUses() == 7,
                "Resolve the Witchery result by its saved ID and subtype");
    }
}
