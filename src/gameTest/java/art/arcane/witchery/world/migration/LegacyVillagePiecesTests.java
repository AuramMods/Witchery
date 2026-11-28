package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.TagParser;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.Set;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class LegacyVillagePiecesTests {
    @GameTest(template = "qa_coven_arena")
    public static void savedVillagePiecesKeepTheirPlacementAndProgress(GameTestHelper helper) throws Exception {
        var features = TagParser.parseTag("""
                {"[-13,2]":{Valid:1b,Children:[
                    {id:"witchery:witchhut",BB:[I;-210,4,51,-206,9,54],O:1,HPos:4,C:1b,T:1,WITCWCount:1},
                    {id:"witchery:Apothecary",BB:[I;-202,4,52,-197,12,60],O:3,HPos:-1,VCount:0,WITCApocChest:0b},
                    {id:"witchery:villagewatchtower",BB:[I;-222,74,50,-215,96,57],O:2,HPos:-1,Chest:0b,Guards:0},
                    {id:"witchery:bookshop",BB:[I;-220,74,61,-211,81,69],O:0,HPos:-1,Chest:0b}
                ]}, "[-8,2]":{Valid:0b,Children:[
                    {id:"witchery:witchhut",BB:[I;-127,74,51,-123,79,54],O:1,HPos:-1}
                ]}}
                """);
        var chunks = LegacyVillagePieces.load(features);
        var candidates = chunks.get(ChunkPos.asLong(-13, 3));
        var hut = candidates.stream().filter(piece -> piece.building() == LegacyVillagePieces.Building.WITCH_HUT)
                .findFirst().orElseThrow();
        var shop = candidates.stream().filter(piece -> piece.building() == LegacyVillagePieces.Building.APOTHECARY)
                .findFirst().orElseThrow();
        helper.assertTrue(hut.grounded() && hut.residentsPlaced() == 1 && hut.tallHut() && hut.furnishedHut(),
                "The already placed Witch Hut keeps its saved height, furniture and resident count");
        helper.assertTrue(shop.grounded() && !shop.chestPlaced() && shop.residentsPlaced() == 0,
                "The partially placed Apothecary keeps its adjusted height with both placements pending");
        checkPosition(helper, hut, new BlockPos(1, 1, 3), new BlockPos(-208, 5, 52));
        checkPosition(helper, shop, new BlockPos(7, 0, 2), new BlockPos(-201, 4, 59));
        var tower = chunks.get(ChunkPos.asLong(-14, 3)).stream()
                .filter(piece -> piece.building() == LegacyVillagePieces.Building.WATCHTOWER).findFirst().orElseThrow();
        var books = chunks.get(ChunkPos.asLong(-14, 4)).stream()
                .filter(piece -> piece.building() == LegacyVillagePieces.Building.BOOKSHOP).findFirst().orElseThrow();
        helper.assertTrue(!tower.grounded() && !books.grounded() && !chunks.containsKey(ChunkPos.asLong(-8, 3)),
                "Unstarted valid pieces wait for terrain and an invalid vanilla start contributes no buildings");
        checkPosition(helper, tower, new BlockPos(5, 13, 5), new BlockPos(-217, 17, 52));
        checkPosition(helper, books, new BlockPos(2, 1, 4), new BlockPos(-218, 5, 65));
        var inhabited = new BlockPos(-208, 5, 52);
        var pendingChest = new BlockPos(-201, 4, 59);
        var owner = LegacyVillagePopulation.owner(inhabited.getX(), inhabited.getZ());
        helper.assertTrue(owner.equals(new ChunkPos(-14, 2)), "The original village population overlaps two actual chunks");
        var remaining = LegacyVillagePieces.unpopulatedArea(new ChunkPos(-13, 3), -64, 319, Set.of(owner.toLong()));
        helper.assertTrue(remaining.stream().noneMatch(clip -> clip.isInside(inhabited))
                        && remaining.stream().anyMatch(clip -> clip.isInside(pendingChest)),
                "The two positions share a modern chunk. Only the pending chest area can be generated");
        helper.succeed();
    }

    private static void checkPosition(GameTestHelper helper, LegacyVillagePieces.Piece piece, BlockPos local, BlockPos expected) {
        var settings = piece.settings();
        var actual = StructureTemplate.calculateRelativePosition(settings, local).offset(piece.origin(settings, 4));
        helper.assertTrue(actual.equals(expected), "The saved orientation gives the original feature position " + expected);
    }
}
