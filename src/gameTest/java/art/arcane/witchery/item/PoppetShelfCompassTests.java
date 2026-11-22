package art.arcane.witchery.item;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class PoppetShelfCompassTests {
    @GameTest(template = "qa_coven_arena")
    public static void shelfCompassUsesTheOriginalPatternAndItemId(GameTestHelper helper) throws Exception {
        var level = helper.getLevel();
        var grid = new TransientCraftingContainer(new AbstractContainerMenu(MenuType.CRAFTING, 0) {
            @Override
            public ItemStack quickMoveStack(Player player, int slot) {
                return ItemStack.EMPTY;
            }

            @Override
            public boolean stillValid(Player player) {
                return true;
            }
        }, 3, 3);
        for (int slot : new int[]{0, 2, 6, 8}) grid.setItem(slot, new ItemStack(Items.GOLD_INGOT));
        for (int slot : new int[]{1, 3, 5}) grid.setItem(slot, new ItemStack(Items.DIAMOND));
        grid.setItem(4, new ItemStack(Items.CLOCK));
        grid.setItem(7, new ItemStack(WitcheryIngredient.NULL_CATALYST.item()));
        var recipe = (CraftingRecipe) level.getRecipeManager()
                .byKey(ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "shelfcompass")).orElseThrow();
        var compass = WitcheryItems.getRequired("shelfcompass").get();
        helper.assertTrue(recipe.matches(grid, level) && recipe.assemble(grid, level.registryAccess()).is(compass),
                "The original pattern makes a Poppet Shelf Compass");
        grid.setItem(4, new ItemStack(Items.COMPASS));
        helper.assertFalse(recipe.matches(grid, level), "The recipe requires a clock");
        var legacy = WitcheryIngredient.loadInventoryStack(TagParser.parseTag(
                "{id:\"witchery:shelfcompass\",Damage:5s,Count:2b,tag:{Keepsake:1b}}"));
        helper.assertTrue(legacy.is(compass) && legacy.getCount() == 2 && legacy.getMaxStackSize() == 64
                        && !legacy.isDamageableItem() && legacy.getTag().getBoolean("Keepsake"),
                "A saved compass keeps its count and other tags while the old icon value cannot wear it out");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "witchery_shelf_compass", setupTicks = 5)
    public static void shelfCompassScansLoadedShelvesAtTheOriginalHorizontalRanges(GameTestHelper helper) {
        var level = helper.getLevel();
        var shelfBlock = WitcheryBlocks.getRequired("poppetshelf").get();
        var shelf = helper.absolutePos(new BlockPos(3, 28, 5));
        var nearer = shelf.east(15);
        level.setBlockAndUpdate(shelf, shelfBlock.defaultBlockState());
        try {
            int x = shelf.getX();
            int z = shelf.getZ();
            for (int band = 0; band < 5; band++) {
                int boundary = 8 << band;
                helper.assertTrue(PoppetShelfCompassItem.signal(level, x + boundary - 0.25, z) == 5 - band,
                        "The nearer band includes a distance just below " + boundary);
                helper.assertTrue(PoppetShelfCompassItem.signal(level, x + boundary, z) == 4 - band,
                        "The band ends strictly before " + boundary);
            }
            helper.assertTrue(PoppetShelfCompassItem.signal(level, x + 6, z + 6) == 4,
                    "Diagonal distance uses the original horizontal radius");
            level.setBlockAndUpdate(nearer, shelfBlock.defaultBlockState());
            helper.assertTrue(PoppetShelfCompassItem.signal(level, x + 18, z) == 5,
                    "The nearest of two shelves determines the signal");
            level.setBlockAndUpdate(nearer, Blocks.CHEST.defaultBlockState());
            helper.assertTrue(PoppetShelfCompassItem.signal(level, x + 18, z) == 3,
                    "Removing the nearer shelf immediately changes the next reading");
            var chunks = level.getChunkSource();
            helper.assertTrue(chunks.getChunkNow(64_000, 64_000) == null
                            && PoppetShelfCompassItem.signal(level, 1_024_000, 1_024_000) == 0
                            && chunks.getChunkNow(64_000, 64_000) == null,
                    "Scanning empty terrain does not load an absent chunk");
            helper.succeed();
        } finally {
            level.removeBlock(nearer, false);
            level.removeBlock(shelf, false);
        }
    }
}
