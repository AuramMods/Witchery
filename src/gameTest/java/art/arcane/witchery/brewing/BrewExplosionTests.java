package art.arcane.witchery.brewing;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.entity.SlurpBlockEntity;
import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class BrewExplosionTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_brew_explosions")
    public static void blastsDestroyBrewCellsAndReachTheBlocksBehindThem(GameTestHelper helper) {
        var level = helper.getLevel();
        var origin = helper.absolutePos(new BlockPos(6, 4, 5));
        var cell = origin.east();
        var glass = cell.east();
        for (String path : List.of("brew", "brewgas", "brewliquid", "disease")) {
            var block = WitcheryBlocks.getRequired(path).get();
            var source = block.defaultBlockState();
            var flowing = source.setValue(LiquidBlock.LEVEL, 4);
            for (int y = -3; y <= 3; y++) {
                for (int z = -3; z <= 3; z++) {
                    level.setBlock(cell.offset(0, y, z), (y + z) % 2 == 0 ? source : flowing, Block.UPDATE_CLIENTS);
                }
            }
            level.setBlockAndUpdate(glass, Blocks.GLASS.defaultBlockState());
            helper.assertTrue(source.getShape(level, cell).isEmpty(), path + " cannot be targeted by normal mining");

            blast(level, origin);

            if (path.equals("disease")) {
                helper.assertTrue(level.getBlockState(cell).is(block) && level.getBlockState(glass).is(Blocks.GLASS),
                        "Disease fluid retains its original blast protection");
            } else {
                helper.assertTrue(level.isEmptyBlock(cell) && level.isEmptyBlock(cell.above())
                                && level.isEmptyBlock(glass) && level.getBlockEntity(cell) == null,
                        "A blast clears source and flowing " + path + " and reaches the glass behind it");
            }
        }
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_brew_explosions", timeoutTicks = 50)
    public static void blastsCancelFluidHoldsAndIntactHoldsRestoreExactLevels(GameTestHelper helper) {
        var level = helper.getLevel();
        var destroyedWater = helper.absolutePos(new BlockPos(6, 4, 8));
        var destroyedLava = helper.absolutePos(new BlockPos(19, 4, 8));
        var intactWater = helper.absolutePos(new BlockPos(6, 4, 3));
        var intactLava = helper.absolutePos(new BlockPos(19, 4, 3));
        var water = Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL, 3);
        var lava = Blocks.LAVA.defaultBlockState().setValue(LiquidBlock.LEVEL, 3);
        for (var intact : List.of(intactWater, intactLava)) {
            for (Direction direction : Direction.values()) {
                level.setBlockAndUpdate(intact.relative(direction), Blocks.OBSIDIAN.defaultBlockState());
            }
        }
        hold(helper, destroyedWater, water);
        hold(helper, destroyedLava, lava);
        hold(helper, intactWater, water);
        hold(helper, intactLava, lava);

        for (var destroyed : List.of(destroyedWater, destroyedLava)) {
            var glass = destroyed.east();
            level.setBlockAndUpdate(glass, Blocks.GLASS.defaultBlockState());
            blast(level, destroyed.west());
            helper.assertTrue(level.isEmptyBlock(destroyed) && level.getBlockEntity(destroyed) == null
                    && level.isEmptyBlock(glass), "A blast releases the cell and reaches the glass behind Slurp");
        }

        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(level.getBlockState(intactWater).equals(water)
                        && level.getBlockState(intactLava).equals(lava), "Intact holds restore exact fluid levels on expiry"))
                .thenExecute(() -> helper.assertTrue(level.isEmptyBlock(destroyedWater) && level.isEmptyBlock(destroyedLava),
                        "Destroyed holds stay empty after their timer would have expired"))
                .thenSucceed();
    }

    private static void hold(GameTestHelper helper, BlockPos pos, BlockState fluid) {
        var level = helper.getLevel();
        level.setBlockAndUpdate(pos, fluid);
        BrewTerrainEffects.holdFluid(level, pos, 1, 30, fluid.getBlock(), null);
        helper.assertTrue(level.getBlockEntity(pos) instanceof SlurpBlockEntity, "The brew starts a fluid hold");
    }

    private static void blast(ServerLevel level, BlockPos pos) {
        var center = Vec3.atCenterOf(pos);
        level.explode(null, center.x, center.y, center.z, 4.0F, false, Level.ExplosionInteraction.BLOCK);
    }
}
