package art.arcane.witchery.ritual;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.config.WitcheryServerConfig;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class NaturesPowerConfigTests {
    @GameTest(template = "qa_imp_contracts")
    public static void naturesPowerReadsConfiguredBlocksAndStates(GameTestHelper helper) {
        var config = WitcheryServerConfig.NATURES_POWER_REPLACEABLE_BLOCKS;
        var previous = config.get();
        var mycelium = Blocks.MYCELIUM.defaultBlockState().setValue(BlockStateProperties.SNOWY, true);
        var stone = Blocks.STONE.defaultBlockState();
        var oak = Blocks.OAK_LOG.defaultBlockState();
        try {
            config.set(config.getDefault());
            var defaults = GrowthRites.replaceableSoil();
            helper.assertTrue(defaults.test(stone) && defaults.test(mycelium),
                    "The original default includes stone and snowy mycelium");

            config.set(List.of());
            var noExtras = GrowthRites.replaceableSoil();
            helper.assertTrue(noExtras.test(stone) && !noExtras.test(mycelium),
                    "An empty list removes mycelium and keeps the datapack tag active");

            config.set(List.of("minecraft:gold_block", "minecraft:oak_log[axis=x]", "stone,1", "wool",
                    "minecraft:spruce_log,32767", "minecraft:missing_block,4"));
            var custom = GrowthRites.replaceableSoil();
            helper.assertTrue(custom.test(Blocks.GOLD_BLOCK.defaultBlockState())
                            && custom.test(Blocks.GRANITE.defaultBlockState())
                            && custom.test(Blocks.BLUE_WOOL.defaultBlockState())
                            && custom.test(Blocks.SPRUCE_LOG.defaultBlockState().setValue(BlockStateProperties.AXIS, Direction.Axis.Z)),
                    "Block names, legacy metadata and legacy wildcards match the configured terrain");
            helper.assertTrue(custom.test(oak.setValue(BlockStateProperties.AXIS, Direction.Axis.X)) && !custom.test(oak)
                            && !custom.test(Blocks.ANDESITE.defaultBlockState()) && !custom.test(mycelium)
                            && !custom.test(Blocks.AIR.defaultBlockState()),
                    "A block state matches only its configured variant and invalid entries add no matches");
        } finally {
            config.set(previous);
        }
        helper.succeed();
    }
}
