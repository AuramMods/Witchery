package art.arcane.witchery.brewing;

import art.arcane.witchery.Witchery;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class BrewLandscapeTests {
    @GameTest(template = "qa_coven_arena")
    public static void landscapeCastsCheckAirBeforeChangingTerrain(GameTestHelper helper) {
        var level = helper.getLevel();
        var actor = FakePlayerFactory.getMinecraft(level);
        var center = helper.absolutePos(new BlockPos(6, 6, 6));
        var blight = center.above();
        var fill = center.east();
        var erode = center.above(4);
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-2, 0, -2), center.offset(2, 5, 2))) {
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        }
        level.setBlockAndUpdate(center, Blocks.GRASS_BLOCK.defaultBlockState());
        var checks = new ArrayList<BlockPos>();
        Consumer<BlockEvent.BreakEvent> protect = event -> {
            if (event.getLevel() == level && event.getPlayer() == actor
                    && List.of(blight, fill, erode).contains(event.getPos())) {
                checks.add(event.getPos());
                event.setCanceled(true);
            }
        };
        MinecraftForge.EVENT_BUS.addListener(protect);
        try {
            BrewLandscape.blight(level, blight, 1, actor);
            helper.assertTrue(checks.equals(List.of(blight)) && level.getBlockState(center).is(Blocks.GRASS_BLOCK),
                    "Blight checks the air cell before rolling on the ground below");

            level.setBlockAndUpdate(center, Blocks.GRANITE.defaultBlockState());
            BrewLevelling.levelTerrain(level, center, 3, 0, false, actor);
            helper.assertTrue(checks.contains(fill) && level.isEmptyBlock(fill)
                            && level.getBlockState(center.west()).is(Blocks.STONE),
                    "Levelling skips protected air and uses the legacy granite reserve on free cells");

            BrewErosion.erodeFromCauldron(level, erode, 1, actor);
            helper.assertTrue(checks.contains(erode), "Erosion checks permission before a visit to an empty cell");
            helper.succeed();
        } finally {
            MinecraftForge.EVENT_BUS.unregister(protect);
        }
    }

    @GameTest(template = "qa_coven_arena")
    public static void transposeSkipsAProtectedEmptyArrival(GameTestHelper helper) {
        var level = helper.getLevel();
        var source = helper.absolutePos(new BlockPos(6, 5, 6));
        var destination = source.above(5);
        var actor = FakePlayerFactory.getMinecraft(level);
        level.setBlockAndUpdate(source, Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(source.east(), Blocks.GLASS.defaultBlockState());
        level.setBlockAndUpdate(destination, Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(destination.east(), Blocks.AIR.defaultBlockState());
        Consumer<BlockEvent.BreakEvent> protect = event -> {
            if (event.getLevel() == level && event.getPlayer() == actor && event.getPos().equals(destination)) {
                event.setCanceled(true);
            }
        };
        MinecraftForge.EVENT_BUS.addListener(protect);
        try {
            BrewTransposition.moveTerrain(new BrewTransposition.Site(level, source),
                    new BrewTransposition.Site(level, destination));
            helper.assertTrue(level.getBlockState(source).is(Blocks.STONE) && level.isEmptyBlock(destination)
                            && level.isEmptyBlock(source.east()) && level.getBlockState(destination.east()).is(Blocks.GLASS),
                    "Transpose preserves the protected pair and moves its unprotected neighbor");
            helper.succeed();
        } finally {
            MinecraftForge.EVENT_BUS.unregister(protect);
        }
    }

    @GameTest(template = "qa_coven_arena")
    public static void raiseLandLiftsAnEntityOverAnEmptyColumn(GameTestHelper helper) {
        var level = helper.getLevel();
        var surface = helper.absolutePos(new BlockPos(6, 6, 6));
        for (BlockPos pos : BlockPos.betweenClosed(surface.below(2), surface.above(6))) {
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        }
        var pig = helper.spawn(EntityType.PIG, 6, 7, 6);
        pig.setPos(surface.getX() + 0.5, surface.getY() + 1, surface.getZ() + 0.5);
        pig.setNoGravity(true);
        pig.setNoAi(true);
        helper.startSequence().thenWaitUntil(() ->
                helper.assertTrue(level.getEntity(pig.getUUID()) == pig, "The pig is tracked in the loaded column"))
                .thenExecute(() -> {
                    BrewLandscape.raiseColumn(level, surface, 3, null);
                    helper.assertTrue(pig.getY() == surface.getY() + 4, "An empty column still lifts the nearby pig");
                }).thenSucceed();
    }
}
