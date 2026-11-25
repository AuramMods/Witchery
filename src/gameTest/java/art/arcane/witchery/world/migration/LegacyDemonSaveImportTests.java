package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
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
import net.minecraft.world.entity.ai.behavior.ResetProfession;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.nio.file.Files;
import java.util.Optional;
import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class LegacyDemonSaveImportTests {
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
