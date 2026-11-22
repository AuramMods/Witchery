package art.arcane.witchery.entity;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class HobgoblinMateTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_hobgoblin_mate_stop", setupTicks = 5)
    public static void interruptedCourtshipReleasesBothPartners(GameTestHelper helper) {
        var level = helper.getLevel();
        var floor = BlockPos.betweenClosed(new BlockPos(3, 270, 3), new BlockPos(12, 270, 11));
        for (var position : floor) helper.setBlock(position, Blocks.STONE);
        var bed = Blocks.RED_BED.defaultBlockState().setValue(BedBlock.FACING, Direction.NORTH);
        for (int index = 0; index < 3; index++) {
            var foot = new BlockPos(4 + index * 3, 271, 5);
            helper.setBlock(foot, bed);
            helper.setBlock(foot.north(), bed.setValue(BedBlock.PART, BedPart.HEAD));
        }
        var first = helper.spawnWithNoFreeWill(WitcheryEntities.GOBLIN.get(), new BlockPos(6, 271, 8));
        var second = helper.spawnWithNoFreeWill(WitcheryEntities.GOBLIN.get(), new BlockPos(7, 271, 8));
        var home = helper.absolutePos(new BlockPos(4, 271, 4));
        var points = level.getPoiManager();
        helper.startSequence().thenWaitUntil(() -> {
            helper.assertTrue(points.getCountInRange(type -> type.is(PoiTypes.HOME), home, 16,
                            PoiManager.Occupancy.ANY) >= 3,
                    "The three village beds are registered as homes");
            helper.assertTrue(level.getEntity(first.getUUID()) == first && level.getEntity(second.getUUID()) == second,
                    "Both goblins are visible through the server entity lookup");
        }).thenExecute(() -> {
            try {
                helper.assertTrue(points.take(type -> type.is(PoiTypes.HOME), (type, position) -> position.equals(home), home, 1).isPresent(),
                        "A claimed home establishes the village");
                first.setNoAi(false);
                second.setNoAi(false);
                long seed = 0;
                while (RandomSource.create(seed).nextInt(500) != 0) seed++;
                first.getRandom().setSeed(seed);
                helper.assertTrue(first.mateGoal().canUse() && second.mateGoal().canContinueToUse(),
                        "Both adults enter the same courtship");
                second.setAge(6000);
                helper.assertFalse(first.mateGoal().canContinueToUse(), "A partner on cooldown interrupts courtship");
                first.mateGoal().stop();
                second.mateGoal().stop();

                second.setAge(0);
                second.getRandom().setSeed(seed);
                helper.assertTrue(second.mateGoal().canUse() && first.mateGoal().canContinueToUse(),
                        "Either adult can start a new courtship after interruption");
                first.mateGoal().stop();
                helper.assertFalse(second.mateGoal().canContinueToUse(), "Stopping from the other side also releases the pair");
            } finally {
                points.release(home);
                first.discard();
                second.discard();
            }
        }).thenSucceed();
    }
}
