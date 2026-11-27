package art.arcane.witchery.world;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.data.WitcheryLootTables;
import art.arcane.witchery.item.WitcheryIngredient;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.Map;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class StructureChestLootTests {
    @GameTest(template = "qa_coven_arena")
    public static void structureChestRollsMatchOriginalWitchery(GameTestHelper helper) {
        assertContents(helper, new BlockPos(2, 2, 2), WitcheryLootTables.WITCH_SHACK, 14L,
                Map.of(21, new ItemStack(Items.SPRUCE_SAPLING), 26, new ItemStack(Items.APPLE, 3)));
        assertContents(helper, new BlockPos(4, 2, 2), table("village_apothecary"), 1L,
                Map.of(5, new ItemStack(WitcheryIngredient.DOG_TONGUE.item(), 5),
                        10, new ItemStack(WitcheryIngredient.MUTANDIS.item(), 4),
                        24, new ItemStack(WitcheryIngredient.BAT_WOOL.item(), 3)));
        assertContents(helper, new BlockPos(6, 2, 2), table("village/keep"), 1L,
                Map.of(5, new ItemStack(Items.GOLDEN_HELMET), 6, new ItemStack(Items.GOLD_INGOT),
                        10, new ItemStack(Items.GOLD_INGOT, 2), 16, new ItemStack(Items.GOLD_NUGGET, 13),
                        24, new ItemStack(Items.GOLD_INGOT, 2)));
        assertContents(helper, new BlockPos(8, 2, 2), table("village/watchtower"), 1L,
                Map.of(5, new ItemStack(Items.BREAD, 2), 10, new ItemStack(Items.IRON_AXE),
                        24, new ItemStack(Items.APPLE, 2)));
        helper.succeed();
    }

    private static ResourceLocation table(String path) {
        return ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "chests/" + path);
    }

    private static void assertContents(GameTestHelper helper, BlockPos local, ResourceLocation table, long seed,
                                       Map<Integer, ItemStack> expected) {
        helper.setBlock(local, Blocks.CHEST);
        var chest = (ChestBlockEntity) helper.getBlockEntity(local);
        chest.setLootTable(table, seed);
        chest.unpackLootTable(null);
        for (int slot = 0; slot < chest.getContainerSize(); slot++) {
            helper.assertTrue(ItemStack.matches(chest.getItem(slot), expected.getOrDefault(slot, ItemStack.EMPTY)),
                    "The original " + table + " seed " + seed + " differed at slot " + slot);
        }
        helper.assertTrue(!chest.saveWithoutMetadata().contains("LootTable"), "The filled chest saves its items");
    }
}
