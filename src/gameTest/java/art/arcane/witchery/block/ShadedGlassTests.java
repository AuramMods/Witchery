package art.arcane.witchery.block;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryBlocks;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class ShadedGlassTests {
    @GameTest(template = "qa_coven_arena", timeoutTicks = 100)
    public static void shadeFollowsRedstoneAndBlocksLight(GameTestHelper helper) throws Exception {
        var level = helper.getLevel();
        var clear = WitcheryBlocks.getRequired("shadedglass").get();
        var darkened = WitcheryBlocks.getRequired("shadedglass_active").get();
        BlockPos center = helper.absolutePos(new BlockPos(5, 3, 5));
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-2, -1, -1), center.offset(2, 1, 1))) {
            level.setBlockAndUpdate(pos, Blocks.STONE.defaultBlockState());
        }
        level.setBlockAndUpdate(center.west(), Blocks.GLOWSTONE.defaultBlockState());
        level.setBlockAndUpdate(center.east(), Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(center, clear.defaultBlockState().setValue(ShadedGlassBlock.COLOR, ShadedGlassBlock.Color.RED));

        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "GlassPlacement"));
        player.moveTo(helper.absoluteVec(new Vec3(2.5, 4, 2.5)));
        BlockPos poweredSurface = helper.absolutePos(new BlockPos(9, 2, 5));
        level.setBlockAndUpdate(poweredSurface, Blocks.REDSTONE_BLOCK.defaultBlockState());
        var silver = WitcheryIngredient.loadInventoryStack(TagParser.parseTag("{id:\"witchery:shadedglass\",Count:1b,Damage:8s}"));
        player.setItemInHand(InteractionHand.MAIN_HAND, silver);
        var hit = new BlockHitResult(Vec3.atCenterOf(poweredSurface).add(0, 0.5, 0), Direction.UP, poweredSurface, false);
        helper.assertTrue(((BlockItem) silver.getItem()).place(new BlockPlaceContext(player, InteractionHand.MAIN_HAND, silver, hit))
                .consumesAction(), "Place the imported light gray stack through the item interaction");
        BlockPos placed = poweredSurface.above();
        var state = level.getBlockState(placed);
        helper.assertTrue(state.is(darkened) && state.getValue(ShadedGlassBlock.COLOR) == ShadedGlassBlock.Color.SILVER,
                "Placement over redstone keeps the legacy color and enters the darkened state");
        var ordinaryTool = new ItemStack(Items.IRON_PICKAXE);
        helper.assertTrue(Block.getDrops(state, level, placed, null, player, ordinaryTool).isEmpty(), "Ordinary mining shatters the glass");
        var silkTool = ordinaryTool.copy();
        silkTool.enchant(Enchantments.SILK_TOUCH, 1);
        var drops = Block.getDrops(state, level, placed, null, player, silkTool);
        helper.assertTrue(drops.size() == 1 && drops.get(0).is(darkened.asItem())
                        && ShadedGlassBlock.Color.fromStack(drops.get(0)) == ShadedGlassBlock.Color.SILVER,
                "Silk Touch returns the original colored variant");

        helper.runAfterDelay(15, () -> {
            helper.assertTrue(level.getBrightness(LightLayer.BLOCK, center.east()) > 0, "Clear Shaded Glass transmits block light");
            level.setBlockAndUpdate(center.below(), Blocks.REDSTONE_BLOCK.defaultBlockState());
            var shaded = level.getBlockState(center);
            helper.assertTrue(shaded.is(darkened) && shaded.getValue(ShadedGlassBlock.COLOR) == ShadedGlassBlock.Color.RED
                            && shaded.getLightBlock(level, center) == 15 && shaded.getLightEmission() == 0
                            && !shaded.propagatesSkylightDown(level, center), "Redstone darkens the red pane without emitting light");
            helper.runAfterDelay(15, () -> {
                helper.assertTrue(level.getBrightness(LightLayer.BLOCK, center.east()) == 0, "Darkened glass blocks the lamp's light");
                level.setBlockAndUpdate(center.below(), Blocks.STONE.defaultBlockState());
                var restored = level.getBlockState(center);
                helper.assertTrue(restored.is(clear) && restored.getValue(ShadedGlassBlock.COLOR) == ShadedGlassBlock.Color.RED,
                        "Removing power restores the clear pane and keeps its color");
                helper.succeed();
            });
        });
    }

    @GameTest(template = "qa_coven_arena")
    public static void allSixteenColorsUseTheirOriginalRecipes(GameTestHelper helper) {
        var level = helper.getLevel();
        var recipes = level.getRecipeManager();
        var glass = WitcheryBlocks.getRequired("shadedglass").get().asItem();
        var grid = new TransientCraftingContainer(new AbstractContainerMenu(MenuType.CRAFTING, 0) {
            @Override public boolean stillValid(Player player) { return true; }
            @Override public ItemStack quickMoveStack(Player player, int slot) { return ItemStack.EMPTY; }
        }, 3, 3);
        for (var color : ShadedGlassBlock.Color.values()) {
            var stained = BuiltInRegistries.ITEM.get(ResourceLocation.withDefaultNamespace(color.dye().getName() + "_stained_glass"));
            for (int index = 0; index < grid.getContainerSize(); index++) {
                grid.setItem(index, new ItemStack(index == 4 ? Items.REDSTONE : stained));
            }
            var recipe = recipes.getRecipeFor(RecipeType.CRAFTING, grid, level).orElseThrow();
            var result = recipe.assemble(grid, level.registryAccess());
            helper.assertTrue(result.is(glass) && result.getCount() == 8 && ShadedGlassBlock.Color.fromStack(result) == color,
                    "Eight matching stained glass blocks around redstone produce eight " + color.getSerializedName() + " blocks");
        }
        grid.setItem(0, new ItemStack(Items.RED_STAINED_GLASS));
        helper.assertTrue(recipes.getRecipeFor(RecipeType.CRAFTING, grid, level).isEmpty(), "Mixed stained glass colors do not craft");
        helper.succeed();
    }
}
