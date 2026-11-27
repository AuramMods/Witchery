package art.arcane.witchery.block;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraftforge.common.PlantType;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class PlantMineTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_plant_mine_support")
    public static void plainsMineAcceptsOriginalSoilsAndLeavesWebsOnContact(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(5, 3, 5)).atY(280);
        var block = (PlantMineBlock) WitcheryBlocks.getRequired("plantmine").get();
        var mine = block.defaultBlockState().setValue(PlantMineBlock.VARIANT, PlantMineBlock.Variant.DANDELION_WEBS);
        helper.assertTrue(block.getPlantType(level, pos) == PlantType.PLAINS && block.getPlant(level, pos).is(block),
                "Forge soils see the Plant Mine as a Plains plant even before the item is placed");
        for (Block soil : List.of(Blocks.GRASS_BLOCK, Blocks.DIRT, Blocks.PODZOL, Blocks.FARMLAND, Blocks.SAND, Blocks.RED_SAND, Blocks.MYCELIUM)) {
            level.setBlockAndUpdate(pos.below(), soil.defaultBlockState());
            helper.assertTrue(mine.canSurvive(level, pos), "The original soil supports a mine in daylight: " + soil);
        }
        for (Block soil : List.of(Blocks.STONE, Blocks.GLASS, Blocks.GRAVEL, Blocks.SOUL_SAND, Blocks.WATER)) {
            level.setBlockAndUpdate(pos.below(), soil.defaultBlockState());
            helper.assertFalse(mine.canSurvive(level, pos), "The original mine rejects " + soil);
        }
        level.setBlockAndUpdate(pos.below(), Blocks.DIRT.defaultBlockState());
        level.setBlockAndUpdate(pos, mine);
        helper.assertTrue(block.getPlant(level, pos).equals(mine), "Forge receives the existing mine's variant");
        var trigger = EntityType.PIG.create(level);
        helper.assertTrue(trigger != null, "Create the contact trigger");
        trigger.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0, 0);
        mine.entityInside(level, pos, trigger);
        helper.assertTrue(level.getBlockState(pos).is(Blocks.COBWEB) && level.getBlockState(pos.north()).is(Blocks.COBWEB)
                        && level.getBlockState(pos.above()).is(Blocks.COBWEB),
                "Triggering the mine consumes the plant and releases the normal Webs cross");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_plant_mine_light", timeoutTicks = 200)
    public static void coveredMineChecksTheOriginalLightBoundaryOnSideUpdates(GameTestHelper helper) {
        var level = helper.getLevel();
        var lit = helper.absolutePos(new BlockPos(5, 3, 5)).atY(280);
        var dim = lit.east(4);
        enclosedMineSite(level, lit, 8);
        enclosedMineSite(level, dim, 7);

        helper.startSequence().thenWaitUntil(() -> helper.assertTrue(!level.canSeeSky(lit) && !level.canSeeSky(dim)
                        && level.getMaxLocalRawBrightness(lit) == 8 && level.getMaxLocalRawBrightness(dim) == 7,
                "The covered plots have the intended light levels")).thenExecute(() -> {
            var mine = WitcheryBlocks.getRequired("plantmine").get().defaultBlockState();
            helper.assertTrue(mine.canSurvive(level, lit) && !mine.canSurvive(level, dim),
                    "Eight is the first supported light level under a roof");
            level.setBlock(lit, mine, Block.UPDATE_CLIENTS);
            level.setBlock(dim, mine, Block.UPDATE_CLIENTS);
            level.setBlockAndUpdate(lit.east(), Blocks.DEEPSLATE.defaultBlockState());
            level.setBlockAndUpdate(dim.east(), Blocks.DEEPSLATE.defaultBlockState());
            helper.assertTrue(level.getBlockState(lit).equals(mine) && level.isEmptyBlock(dim),
                    "The side notification removes the dim mine without a random tick");
        }).thenSucceed();
    }

    private static void enclosedMineSite(ServerLevel level, BlockPos pos, int brightness) {
        for (var cell : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 2, 1))) {
            level.setBlockAndUpdate(cell, Blocks.STONE.defaultBlockState());
        }
        level.setBlockAndUpdate(pos.below(), Blocks.DIRT.defaultBlockState());
        level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(pos.above(), Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, brightness + 1));
    }
}
