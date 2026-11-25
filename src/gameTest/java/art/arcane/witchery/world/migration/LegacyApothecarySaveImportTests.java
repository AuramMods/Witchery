package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.lycanthropy.WerewolfVillagers;
import art.arcane.witchery.registry.WitcheryApothecary;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.storage.ChunkStorage;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class LegacyApothecarySaveImportTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_legacy_apothecary")
    public static void restoresConfiguredApothecaryAcrossAllVillagerForms(GameTestHelper helper) throws Exception {
        int profession = LegacyWorldIds.captureApothecaryProfession(List.of("    I:ApothecaryVillagerID = 3267"));
        helper.assertTrue(profession == 3267 && LegacyWorldIds.captureApothecaryProfession(List.of()) == 2435,
                "Use the saved configuration and the original default ID");
        var registry = TagParser.parseTag("{ItemData:[{V:4096},{V:388}]}");
        var itemIds = registry.getList("ItemData", Tag.TAG_COMPOUND);
        itemIds.getCompound(0).putString("K", "\u0002witchery:ingredient");
        itemIds.getCompound(1).putString("K", "\u0002minecraft:emerald");
        registry.putInt("ApothecaryProfession", profession);
        String stock = """
                Offers:{Recipes:[{buy:{id:388s,Count:1b,Damage:0s},
                sell:{id:4096s,Count:3b,Damage:24s,tag:{PortMarker:"Original stock"}},uses:3,maxUses:9}]}
                """;
        var original = TagParser.parseTag("""
                {Level:{xPos:5,zPos:2,TerrainPopulated:1b,Entities:[
                  {id:"Villager",CustomName:"Old Apothecary",Profession:%d,%s},
                  {id:"witchery.werevillager",Profession:%d,Infectious:1b,%s},
                  {id:"witchery.wolfman",FormerProfession:%d,Infectious:1b,%s},
                  {id:"Villager",Profession:2435},
                  {id:"Villager",Profession:1},
                  {id:"Villager",Profession:%d}]}}
                """.formatted(profession, stock, profession, stock, profession, stock, profession));
        var root = Files.createTempDirectory("witchery-legacy-apothecary");
        var snapshot = root.resolve("witchery-legacy-ids.dat");
        var level = helper.getLevel();
        CompoundTag fixed;
        try {
            NbtIo.writeCompressed(registry, snapshot.toFile());
            var fixer = level.getServer().getFixerUpper();
            var intermediate = LegacyChunkUpgrade.upgrade(DataFixTypes.CHUNK, fixer, original, -1, 1493,
                    LegacyWorldIds.find(root));
            ChunkStorage.injectDatafixingContext(intermediate, Level.OVERWORLD, Optional.empty());
            fixed = DataFixTypes.CHUNK.updateToCurrentVersion(fixer, intermediate, 1493);
            LegacyChunkUpgrade.finish(fixed, Level.OVERWORLD);
        } finally {
            Files.deleteIfExists(snapshot);
            Files.deleteIfExists(root);
        }

        var entities = fixed.getList("entities", Tag.TAG_COMPOUND);
        var apothecary = EntityType.VILLAGER.create(level);
        apothecary.load(entities.getCompound(0));
        var reloaded = EntityType.VILLAGER.create(level);
        reloaded.load(apothecary.saveWithoutId(new CompoundTag()));
        ResetProfession.create().tryStart(level, reloaded, level.getGameTime());
        assertTrade(helper, reloaded);
        helper.assertTrue(reloaded.getName().getString().equals("Old Apothecary"), "Keep the original custom name");

        var infected = WitcheryEntities.WERE_VILLAGER.get().create(level);
        infected.load(entities.getCompound(1));
        assertTrade(helper, infected);
        helper.assertTrue(infected.saveWithoutId(new CompoundTag()).getBoolean("Infectious"), "Keep the curse state");
        var wolfman = WitcheryEntities.WOLFMAN.get().create(level);
        wolfman.load(entities.getCompound(2));
        var former = EntityType.VILLAGER.create(level);
        former.load(wolfman.formerVillagerData());
        assertTrade(helper, former);

        var control = EntityType.VILLAGER.create(level);
        control.load(entities.getCompound(3));
        helper.assertTrue(control.getVillagerData().getProfession() != WitcheryApothecary.PROFESSION.get(),
                "The default number belongs to a different profession in a custom world");
        control.load(entities.getCompound(4));
        helper.assertTrue(control.getVillagerData().getProfession() == VillagerProfession.LIBRARIAN,
                "Ordinary villagers keep the usual migration");
        var untraded = EntityType.VILLAGER.create(level);
        untraded.load(entities.getCompound(5));
        helper.assertTrue(untraded.getVillagerData().getProfession() == WitcheryApothecary.PROFESSION.get()
                        && untraded.getOffers().size() == 8,
                "An untraded Apothecary receives its full original trade list");

        BlockPos spawn = helper.absolutePos(new BlockPos(2, 2, 2));
        wolfman.moveTo(spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5);
        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(level.isPositionEntityTicking(spawn),
                "The Wolfman's spawn position is ticking")).thenExecute(() -> {
            helper.assertTrue(level.addFreshEntity(wolfman) && level.getEntity(wolfman.getUUID()) == wolfman,
                    "The Wolfman is tracked");
            helper.assertTrue(WerewolfVillagers.revert(wolfman, false), "The imported Wolfman can be cured");
            var restored = WerewolfVillagers.findByTaglock(level.getServer(), wolfman.getUUID());
            helper.assertTrue(restored instanceof Villager, "The cure restores the saved merchant");
            assertTrade(helper, (Villager) restored);
            restored.discard();
        }).thenSucceed();
    }

    private static void assertTrade(GameTestHelper helper, Villager villager) {
        helper.assertTrue(villager.getVillagerData().getProfession() == WitcheryApothecary.PROFESSION.get()
                        && villager.getVillagerXp() > 0 && villager.getOffers().size() == 1,
                "Keep the original profession and the saved trade list");
        var offer = villager.getOffers().get(0);
        var result = offer.getResult();
        helper.assertTrue(offer.getBaseCostA().is(Items.EMERALD) && result.is(WitcheryIngredient.BAT_WOOL.item())
                        && result.getCount() == 3 && "Original stock".equals(result.getOrCreateTag().getString("PortMarker"))
                        && offer.getUses() == 3 && offer.getMaxUses() == 9,
                "Keep the trade's currency, Witchery item, tag, quantity and remaining stock");
    }
}
