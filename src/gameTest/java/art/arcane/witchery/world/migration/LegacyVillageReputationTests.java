package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class LegacyVillageReputationTests {
    @GameTest(template = "qa_coven_arena")
    public static void originalVillageScoresStayWithTheSavedCenter(GameTestHelper helper) throws Exception {
        var data = TagParser.parseTag("""
                {Tick:200,Villages:[
                  {CX:-32,CY:70,CZ:-12,Radius:32,Players:[{Name:"Recruit",S:10},{Name:"Outlaw",S:-15}]},
                  {CX:28,CY:70,CZ:-12,Radius:32,Players:[{Name:"Recruit",S:9},{Name:"Outlaw",S:5}]},
                  {CX:0,CY:70,CZ:0,Radius:0,Players:[{Name:"Recruit",S:10}]}]}
                """);
        var villages = LegacyVillages.load(data);
        var west = villages.nearest(new BlockPos(-4, 70, -12), 0).orElseThrow();
        var east = villages.nearest(new BlockPos(0, 70, -12), 0).orElseThrow();
        helper.assertTrue(west.reputation("Recruit") == 10 && west.reputation("Outlaw") == -15
                        && west.reputation("Visitor") == 0
                        && east.reputation("Recruit") == 9 && east.reputation("Outlaw") == 5,
                "The original player scores belong to the closest village, and unrecorded players start neutral");
        var scout = new BlockPos(-96, 70, -12);
        helper.assertTrue(villages.nearest(scout, 0).isEmpty()
                        && villages.nearest(scout, 32).map(village -> new ChunkPos(village.center()))
                                .filter(new ChunkPos(-2, -1)::equals).isPresent()
                        && villages.nearest(scout.west(), 32).isEmpty(),
                "The scouting margin uses the original center chunk and stops at its actual boundary");
        helper.succeed();
    }
}
