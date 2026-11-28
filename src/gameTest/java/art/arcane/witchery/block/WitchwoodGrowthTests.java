package art.arcane.witchery.block;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class WitchwoodGrowthTests {
    @GameTest(template = "qa_coven_arena", batch = "witchery_sapling_retry")
    public static void obstructedSaplingsNeedToPrepareAgainAfterFailedGrowth(GameTestHelper helper) {
        var level = helper.getLevel();
        var sapling = (WitchSaplingBlock) WitcheryBlocks.getRequired("witchsapling").get();
        BlockPos pos = helper.absolutePos(new BlockPos(7, 3, 7));
        level.setBlockAndUpdate(pos.below(), Blocks.DIRT.defaultBlockState());
        for (int height = 1; height <= 16; height++) {
            level.setBlockAndUpdate(pos.above(height), Blocks.STONE.defaultBlockState());
        }
        for (var species : WitchWoodType.values()) {
            var seedling = sapling.defaultBlockState().setValue(WitchWoodType.PROPERTY, species);
            level.setBlockAndUpdate(pos, seedling.setValue(WitchSaplingBlock.STAGE, 1));
            sapling.advanceTree(level, pos, level.getBlockState(pos), RandomSource.create(42));
            helper.assertTrue(level.getBlockState(pos) == seedling,
                    "An obstructed " + species.getSerializedName() + " keeps its variant and resets to the unmarked stage");

            var meal = new ItemStack(Items.BONE_MEAL);
            level.getRandom().setSeed(42);
            helper.assertTrue(BoneMealItem.growCrop(meal, level, pos) && meal.isEmpty()
                            && level.getBlockState(pos) == seedling.setValue(WitchSaplingBlock.STAGE, 1),
                    "The next accepted bone meal application marks the sapling for another attempt");
        }
        helper.succeed();
    }
}
