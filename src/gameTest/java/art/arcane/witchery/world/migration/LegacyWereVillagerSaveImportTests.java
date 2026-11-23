package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.Wolfman;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.lycanthropy.WerewolfVillagers;
import art.arcane.witchery.registry.WitcheryEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.TagParser;
import net.minecraft.util.datafix.DataFixTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.behavior.ResetProfession;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.nio.file.Files;
import java.util.Optional;
import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class LegacyWereVillagerSaveImportTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_legacy_werevillager")
    public static void importsLegacyInfectedVillagerWithItsTradeStock(GameTestHelper helper) throws Exception {
        var registry = TagParser.parseTag("{ItemData:[{V:4096}]}");
        registry.getList("ItemData", Tag.TAG_COMPOUND).getCompound(0).putString("K", "\u0002witchery:ingredient");
        var original = TagParser.parseTag("""
                {Level:{xPos:-14,zPos:-68,TerrainPopulated:1b,Entities:[
                  {id:"witchery.werevillager",CustomName:"Legacy Infected Librarian",UUIDMost:3274408037664640634L,
                   UUIDLeast:-8273213417301270937L,Pos:[-211.5d,5.5d,-1080.5d],Health:20s,
                   Infectious:1b,Age:0,Profession:1,Riches:11,Offers:{Recipes:[
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
        assertTrades(helper, wereVillager);

        BlockPos spawn = helper.absolutePos(new BlockPos(2, 2, 2));
        wereVillager.moveTo(spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5);
        helper.assertTrue(level.addFreshEntity(wereVillager) && WerewolfVillagers.transform(wereVillager, true),
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
        helper.succeed();
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
