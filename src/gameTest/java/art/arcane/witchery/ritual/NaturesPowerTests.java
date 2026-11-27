package art.arcane.witchery.ritual;

import art.arcane.witchery.Witchery;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.entity.player.BonemealEvent;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.ArrayList;
import java.util.function.Consumer;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class NaturesPowerTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_natures_power_surface")
    public static void naturesPowerFindsBuriedFullSnowAndRepeatsItsGrowthPasses(GameTestHelper helper) {
        var level = helper.getLevel();
        var heart = helper.absolutePos(new BlockPos(16, 14, 7));
        var column = heart.offset(-12, -3, -2);
        var sample = column.east(2);
        for (BlockPos pos : BlockPos.betweenClosed(column.offset(-4, 0, -4), column.offset(4, 0, 4))) {
            level.setBlock(pos, Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        var progress = new LandscapeRites.Progress(ItemRiteRecipe.Action.NATURES_POWER, null,
                heart, 0, null, false, 0);
        var attempts = new ArrayList<BlockPos>();
        var fake = FakePlayerFactory.getMinecraft(level);
        Consumer<BonemealEvent> observe = event -> {
            if (event.getLevel() == level && event.getEntity() == fake) {
                attempts.add(event.getPos());
                event.setCanceled(true);
            }
        };
        MinecraftForge.EVENT_BUS.addListener(observe);
        try {
            var thinSnow = Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, 3);
            level.setBlock(column.above(), thinSnow, Block.UPDATE_CLIENTS);
            level.getRandom().setSeed(27);
            LandscapeRites.pulse(level, heart, progress, 0, null, Runnable::run, true);
            helper.assertTrue(attempts.isEmpty() && level.getBlockState(sample).is(Blocks.STONE),
                    "A thin snow layer does not expose the lower surface");

            level.setBlock(column.above(), Blocks.SNOW_BLOCK.defaultBlockState(), Block.UPDATE_CLIENTS);
            level.setBlock(column.above(2), Blocks.DANDELION.defaultBlockState(), Block.UPDATE_CLIENTS);
            level.getRandom().setSeed(27);
            LandscapeRites.pulse(level, heart, progress, 0, null, Runnable::run, true);
            helper.assertTrue(level.getBlockState(column.above()).is(Blocks.SNOW_BLOCK)
                            && level.getBlockState(sample).is(Blocks.GRASS_BLOCK),
                    "The full snow block exposes its lower patch and stays intact");
            helper.assertTrue(attempts.size() == 18 && attempts.stream().distinct().count() < attempts.size(),
                    "The seeded patch revisits overlapping rows and repeats bonemeal at their shared cells");
        } finally {
            MinecraftForge.EVENT_BUS.unregister(observe);
        }
        helper.succeed();
    }
}
