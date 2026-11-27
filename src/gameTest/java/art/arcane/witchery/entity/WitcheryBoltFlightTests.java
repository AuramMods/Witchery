package art.arcane.witchery.entity;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.List;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class WitcheryBoltFlightTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_bolt_water_flight", setupTicks = 5)
    public static void boltsUseTheOriginalWaterContactAndDrag(GameTestHelper helper) {
        var level = helper.getLevel();
        var chunk = new ChunkPos(helper.absolutePos(new BlockPos(5, 0, 5)));
        var deep = new BlockPos(chunk.getMinBlockX() + 1, level.getMaxBuildHeight() - 24, chunk.getMinBlockZ() + 2);
        var boundary = deep.south(4);
        var surface = deep.south(8);
        var shallow = deep.south(12);
        var water = Blocks.WATER.defaultBlockState();
        var placed = new ArrayList<BlockPos>();
        try {
            fill(level, deep.below(3), deep.offset(12, 4, 0), water, placed);
            fill(level, boundary.offset(2, -3, 0), boundary.offset(3, 4, 0), water, placed);
            fill(level, surface.below(3), surface.east(12), water, placed);
            fill(level, shallow, shallow.east(12), water.setValue(LiquidBlock.LEVEL, 4), placed);

            // Water contact on ticks 1–5, measured on Witchery 0.24.1.
            checkFlight(helper, "fully submerged", deep, 0.25, 0.07, true, true, true, true, true);
            checkFlight(helper, "entering and leaving", boundary, 0.25, 0.07, false, true, true, false, false);
            checkFlight(helper, "crossing the surface", surface, 0.72, -0.16, false, true, true, true, true);
            checkFlight(helper, "shallow water", shallow, 0.22, 0, true, true, true, true, true);
            checkFlight(helper, "upper portion of a submerged cell", deep, 0.5, 0.12, true, false, false, false, false);
        } finally {
            placed.forEach(pos -> level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS));
        }
        helper.succeed();
    }

    private static void fill(ServerLevel level, BlockPos first, BlockPos last, BlockState state, List<BlockPos> placed) {
        for (BlockPos pos : BlockPos.betweenClosed(first, last)) {
            level.setBlock(pos, state, Block.UPDATE_CLIENTS);
            placed.add(pos.immutable());
        }
    }

    private static void checkFlight(GameTestHelper helper, String scenario, BlockPos start, double height, double lift,
                                    boolean... originalWaterContact) {
        var bolt = WitcheryEntities.BOLT.get().create(helper.getLevel());
        var position = Vec3.atBottomCenterOf(start).add(0, height, 0);
        var motion = new Vec3(1.6, lift, 0);
        bolt.setPos(position);
        bolt.setDeltaMovement(motion);
        try {
            for (int index = 0; index < originalWaterContact.length; index++) {
                boolean wet = originalWaterContact[index];
                position = position.add(motion);
                motion = motion.scale(wet ? 0.8F : 0.99F).subtract(0, 0.05F, 0);
                bolt.tick();
                String checkpoint = scenario + " at tick " + (index + 1);
                helper.assertTrue(bolt.isInWater() == wet, checkpoint + " should have the original water contact");
                helper.assertTrue(bolt.position().distanceTo(position) < 0.000002,
                        checkpoint + " should reach " + position + ", reached " + bolt.position());
                helper.assertTrue(bolt.getDeltaMovement().distanceTo(motion) < 0.000002,
                        checkpoint + " should have velocity " + motion + ", got " + bolt.getDeltaMovement());
            }
        } finally {
            bolt.discard();
        }
    }
}
