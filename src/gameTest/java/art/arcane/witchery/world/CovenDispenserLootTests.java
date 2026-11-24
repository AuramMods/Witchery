package art.arcane.witchery.world;

import art.arcane.witchery.Witchery;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class CovenDispenserLootTests {
    @GameTest(template = "qa_coven_arena")
    public static void covenDispenserPreservesLegacyRollsAndSlots(GameTestHelper helper) {
        var level = helper.getLevel();
        var center = helper.absolutePos(new BlockPos(6, 3, 5));
        var floor = center.below();
        for (var pos : BlockPos.betweenClosed(floor.offset(-5, 0, -5), floor.offset(5, 0, 5))) {
            level.setBlockAndUpdate(pos, Blocks.STONE.defaultBlockState());
        }
        CovenStructures.circle(new SurfaceStructureSite(level, center, Direction.SOUTH, 11, 11, false),
                RandomSource.create(11L));

        var dispenser = (DispenserBlockEntity) level.getBlockEntity(center);
        helper.assertTrue(dispenser != null && dispenser.getContainerSize() == 9, "The basin has a nine-slot dispenser");
        var lootTable = ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "chests/mineshaft_corridor");
        helper.assertTrue(dispenser.saveWithoutMetadata().getString("LootTable").equals(lootTable.toString()),
                "New coven dispensers use the original Forge mineshaft loot pool");

        // Seed one rolls five times. Two occupied slots are replaced, leaving three unsplit stacks.
        dispenser.setLootTable(lootTable, 1L);
        dispenser.unpackLootTable(null);
        assertStack(helper, dispenser, 0, Items.BREAD, 2);
        assertStack(helper, dispenser, 1, Items.PUMPKIN_SEEDS, 4);
        assertStack(helper, dispenser, 7, Items.PUMPKIN_SEEDS, 3);
        for (int slot = 2; slot < dispenser.getContainerSize(); slot++) {
            if (slot != 7) helper.assertTrue(dispenser.getItem(slot).isEmpty(), "Unexpected item in slot " + slot);
        }
        helper.assertTrue(!dispenser.saveWithoutMetadata().contains("LootTable"),
                "Opening the dispenser stores its rolled contents");
        helper.succeed();
    }

    private static void assertStack(GameTestHelper helper, DispenserBlockEntity dispenser, int slot, Item item, int count) {
        var stack = dispenser.getItem(slot);
        helper.assertTrue(stack.is(item) && stack.getCount() == count, "Unexpected stack in slot " + slot);
    }
}
