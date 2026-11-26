package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.world.WitcheryDimensions;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.nio.file.Files;
import java.util.Comparator;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class LegacyVillageReputationTests {
    @GameTest(template = "qa_coven_arena")
    public static void recoversOnlyMissingVillageCentersFromTheCapturedArchive(GameTestHelper helper) throws Exception {
        var root = Files.createTempDirectory("witchery-split-villages");
        try {
            var registry = TagParser.parseTag("{Dimensions:{dream:64}}");
            NbtIo.writeCompressed(registry, root.resolve("witchery-legacy-ids.dat").toFile());
            var archived = LegacyWorldIds.dimensionArchive(root, WitcheryDimensions.DREAM);
            helper.assertTrue(root.resolve("DIM64").equals(archived), "Use the captured custom Dream ID");
            var live = root.resolve("dimensions/witchery/dream");
            Files.createDirectories(live.resolve("data"));
            Files.createDirectories(archived.resolve("data"));
            var current = TagParser.parseTag("""
                    {data:{Villages:[
                      {CX:0,CY:70,CZ:0,Radius:40,Players:[{Name:"Recruit",S:9}]},
                      {CX:80,CY:70,CZ:0,Radius:32,Players:[{Name:"Recruit",S:5}]}]}}
                    """);
            var original = TagParser.parseTag("""
                    {data:{Villages:[
                      {CX:0,CY:70,CZ:0,Radius:32,Players:[{Name:"Recruit",S:10}]},
                      {CX:-80,CY:70,CZ:0,Radius:32,Players:[{Name:"Recruit",S:10}]}]}}
                    """);
            NbtIo.writeCompressed(current, live.resolve("data/villages.dat").toFile());
            NbtIo.writeCompressed(original, archived.resolve("data/villages.dat").toFile());

            var villages = LegacyVillages.read(live, archived);
            helper.assertTrue(villages.nearest(new BlockPos(-80, 70, 0), 0).orElseThrow().reputation("Recruit") == 10,
                    "Recover the missing archived village and its reputation");
            var overlap = villages.nearest(new BlockPos(0, 70, 0), 0).orElseThrow();
            helper.assertTrue(overlap.radius() == 40 && overlap.reputation("Recruit") == 9
                            && villages.nearest(new BlockPos(80, 70, 0), 0).orElseThrow().reputation("Recruit") == 5,
                    "Keep the live dimension's radius and reputation at shared and current-only centers");
        } finally {
            try (var files = Files.walk(root)) {
                for (var file : files.sorted(Comparator.reverseOrder()).toList()) Files.delete(file);
            }
        }
        helper.succeed();
    }

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
