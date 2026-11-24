package art.arcane.witchery.vampirism;

import art.arcane.witchery.Witchery;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class VillagerBloodImportTests {
    @GameTest(template = "qa_coven_arena")
    public static void restoresLegacyBloodAndKeepsNewerValues(GameTestHelper helper) throws Exception {
        var level = helper.getLevel();
        var villager = EntityType.VILLAGER.create(level);
        villager.load(TagParser.parseTag("""
                {WitcheryExtendedVillager:{Blood:203},ForgeData:{TestMarker:"kept"}}
                """));
        helper.assertTrue(BloodFeeding.blood(villager) == 203, "The original blood count must be available to feeding");
        helper.assertTrue(BloodFeeding.takeFromVillager(villager, 10) == 7, "An awake villager gives seven blood from a ten-point bite");

        var reloaded = EntityType.VILLAGER.create(level);
        var saved = villager.saveWithoutId(new CompoundTag());
        reloaded.load(saved);
        helper.assertTrue(BloodFeeding.blood(reloaded) == 196
                        && reloaded.getPersistentData().getString("TestMarker").equals("kept"),
                "An ordinary modern save keeps the remaining blood and unrelated Forge data");

        saved.put("WitcheryExtendedVillager", TagParser.parseTag("{Blood:203}"));
        reloaded.load(saved);
        helper.assertTrue(BloodFeeding.blood(reloaded) == 196, "Current blood takes precedence over legacy data");

        var drained = EntityType.VILLAGER.create(level);
        drained.load(TagParser.parseTag("{WitcheryExtendedVillager:{Blood:0}}"));
        helper.assertTrue(BloodFeeding.takeFromVillager(drained, 10) == 0, "An imported drained villager remains drained");
        helper.succeed();
    }
}
