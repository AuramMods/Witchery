package art.arcane.witchery.world;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.data.LegacyMineshaftLoot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
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

        // The original Forge hook rolls five times with seed 31. One slot is chosen twice.
        dispenser.setLootTable(lootTable, 31L);
        dispenser.unpackLootTable(null);
        assertStack(helper, dispenser, 0, Items.ENCHANTED_BOOK, 1);
        assertStack(helper, dispenser, 1, Items.COAL, 8);
        assertStack(helper, dispenser, 6, Items.BREAD, 1);
        assertStack(helper, dispenser, 7, Items.MELON_SEEDS, 4);
        for (int slot = 2; slot < dispenser.getContainerSize(); slot++) {
            if (slot != 6 && slot != 7) helper.assertTrue(dispenser.getItem(slot).isEmpty(), "Unexpected item in slot " + slot);
        }
        var book = EnchantmentHelper.deserializeEnchantments(EnchantedBookItem.getEnchantments(dispenser.getItem(0)));
        helper.assertTrue(book.size() == 1 && book.getOrDefault(Enchantments.SHARPNESS, 0) == 3,
                "The original book is prepared before the count and slot rolls");
        helper.assertTrue(!dispenser.saveWithoutMetadata().contains("LootTable"),
                "Opening the dispenser stores its rolled contents");

        var params = new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(center))
                .create(LootContextParamSets.CHEST);
        var context = new LootContext.Builder(params).withOptionalRandomSeed(1L).create(null);
        var rolls = new LegacyMineshaftLoot.Rolls(3, 6).getInt(context);
        var prepared = ((LegacyMineshaftLoot.Context) context).witchery$getMineshaftBook();
        var enchantments = EnchantmentHelper.deserializeEnchantments(EnchantedBookItem.getEnchantments(prepared));
        helper.assertTrue(rolls == 3 && enchantments.size() == 3
                        && enchantments.getOrDefault(Enchantments.ALL_DAMAGE_PROTECTION, 0) == 3
                        && enchantments.getOrDefault(Enchantments.SILK_TOUCH, 0) == 1
                        && enchantments.getOrDefault(Enchantments.FISHING_SPEED, 0) == 2,
                "The prepared book honors both directions of the original Forge compatibility rules");
        helper.succeed();
    }

    private static void assertStack(GameTestHelper helper, DispenserBlockEntity dispenser, int slot, Item item, int count) {
        var stack = dispenser.getItem(slot);
        helper.assertTrue(stack.is(item) && stack.getCount() == count, "Unexpected stack in slot " + slot);
    }
}
