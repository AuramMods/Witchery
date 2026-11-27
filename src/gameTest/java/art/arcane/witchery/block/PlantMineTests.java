package art.arcane.witchery.block;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
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
}
