package art.arcane.witchery.world.migration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.WitchLogBlock;
import art.arcane.witchery.block.WitchWoodType;
import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.TagParser;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class LegacyWitchLogTests {
    @GameTest(template = "qa_coven_arena")
    public static void keepsOriginalWitchLogAxesBarkAndFireValues(GameTestHelper helper) throws Exception {
        var entry = WitcheryBlocks.getRequired("witchlog");
        var axes = new Direction.Axis[]{Direction.Axis.Y, Direction.Axis.X, Direction.Axis.Z, Direction.Axis.Y};
        var species = WitchWoodType.values();
        var level = helper.getLevel();
        var pos = helper.absolutePos(BlockPos.ZERO);
        for (int orientation = 0; orientation < axes.length; orientation++) {
            for (int wood = 0; wood < species.length; wood++) {
                int metadata = 4 * orientation + wood;
                var state = LegacySchematicBlocks.read(entry.getId().toString(), metadata, 0);
                helper.assertTrue(state != null && state.is(entry.get()) && state.getValue(WitchLogBlock.AXIS) == axes[orientation]
                                && state.getValue(WitchLogBlock.BARK) == (orientation == 3)
                                && state.getValue(WitchWoodType.PROPERTY) == species[wood],
                        "Restore the axis, bark and species for legacy metadata " + metadata);
                int fire = metadata == 2 ? 1 : 5;
                helper.assertTrue(entry.get().getFlammability(state, level, pos, Direction.UP) == fire
                                && entry.get().getFireSpreadSpeed(state, level, pos, Direction.UP) == fire,
                        "Restore the original ignition and spread for legacy metadata " + metadata);
            }
        }

        var oldPort = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), TagParser.parseTag(
                "{Name:\"witchery:witchlog\",Properties:{axis:\"x\",wood_type:\"hawthorn\"}}"));
        helper.assertTrue(!oldPort.getValue(WitchLogBlock.BARK) && oldPort.getValue(WitchLogBlock.AXIS) == Direction.Axis.X
                        && oldPort.getValue(WitchWoodType.PROPERTY) == WitchWoodType.HAWTHORN,
                "Read an earlier port palette without the bark property");
        helper.succeed();
    }
}
