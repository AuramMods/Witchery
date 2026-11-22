package art.arcane.witchery.item;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryItems;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class DivinerTests {
    @GameTest(template = "qa_coven_arena")
    public static void divinersUseTheOriginalRecipesAndKeepLegacyWear(GameTestHelper helper) throws Exception {
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
        var waterRecipe = (CraftingRecipe) level.getRecipeManager()
                .byKey(ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "divinerwater")).orElseThrow();
        var bottle = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER);
        for (int slot : new int[]{0, 2, 3, 5}) grid.setItem(slot, bottle.copy());
        for (int slot : new int[]{1, 4, 6, 8}) grid.setItem(slot, new ItemStack(Items.STICK));
        grid.setItem(7, new ItemStack(WitcheryIngredient.TEAR_OF_THE_GODDESS.item()));
        helper.assertTrue(waterRecipe.matches(grid, level), "Four Water Bottles fit the original Water Diviner pattern");
        var water = waterRecipe.assemble(grid, level.registryAccess());
        helper.assertTrue(water.is(WitcheryItems.getRequired("divinerwater").get())
                        && water.getMaxDamage() == 50 && water.getMaxStackSize() == 1 && water.getRarity() == Rarity.UNCOMMON,
                "Crafting gives one uncommon Water Diviner with 50 durability");
        grid.setItem(5, PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.AWKWARD));
        helper.assertFalse(waterRecipe.matches(grid, level), "Another potion cannot replace a Water Bottle");

        grid.clearContent();
        for (int slot : new int[]{1, 6, 8}) grid.setItem(slot, new ItemStack(Items.BLAZE_ROD));
        water.setDamageValue(24);
        grid.setItem(4, water);
        var lavaRecipe = (CraftingRecipe) level.getRecipeManager()
                .byKey(ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "divinerlava")).orElseThrow();
        helper.assertTrue(lavaRecipe.matches(grid, level), "Three Blaze Rods can upgrade a used Water Diviner");
        var lava = lavaRecipe.assemble(grid, level.registryAccess());
        helper.assertTrue(lava.is(WitcheryItems.getRequired("divinerlava").get()) && lava.getDamageValue() == 0
                && lava.getMaxDamage() == 50, "The upgrade makes a new Lava Diviner");

        var imported = WitcheryIngredient.loadInventoryStack(TagParser.parseTag(
                "{id:\"witchery:divinerlava\",Damage:17s,Count:1b,tag:{Keepsake:1b}}"));
        var saved = imported.save(new CompoundTag());
        helper.assertTrue(imported.is(lava.getItem()) && imported.getDamageValue() == 17
                        && saved.getCompound("tag").getBoolean("Keepsake")
                        && WitcheryIngredient.loadInventoryStack(saved).getDamageValue() == 17,
                "Legacy wear and custom data survive a modern save");
        saved.putShort("Damage", (short) 40);
        helper.assertTrue(WitcheryIngredient.loadInventoryStack(saved).getDamageValue() == 17,
                "The modern nested durability takes precedence when both values are present");
        helper.succeed();
    }

    @GameTest(template = "qa_coven_arena", batch = "diviner_water")
    public static void waterDivinerSamplesOneBlockPerTick(GameTestHelper helper) {
        var level = helper.getLevel();
        var surface = helper.absolutePos(new BlockPos(5, 5, 5));
        level.setBlock(surface, Blocks.STONE.defaultBlockState(), 2);
        level.setBlock(surface.below(), Blocks.LAVA.defaultBlockState(), 2);
        level.setBlock(surface.below(2), Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL, 2), 2);
        level.setBlock(surface.below(3), Blocks.WATER.defaultBlockState(), 2);
        var player = playerAbove(helper, surface);
        var water = WitcheryItems.getRequired("divinerwater").get();
        var rod = new ItemStack(water);
        player.setItemInHand(InteractionHand.MAIN_HAND, rod);
        try {
            helper.assertTrue(water.use(level, player, InteractionHand.MAIN_HAND).getResult().consumesAction()
                            && player.isUsingItem() && rod.getUseAnimation() == UseAnim.BOW && rod.getUseDuration() == 400,
                    "Holding use starts the original drawn animation");
            for (int tick = 0; tick < 3; tick++) {
                water.onUseTick(level, player, rod, rod.getUseDuration() - tick);
                helper.assertTrue(player.isUsingItem() && rod.getDamageValue() == 0,
                        "Stone, the wrong fluid, and running water do not end the search");
            }
            water.onUseTick(level, player, rod, rod.getUseDuration() - 3);
            helper.assertTrue(!player.isUsingItem() && rod.getDamageValue() == 1,
                    "The water source ends the scan with one point of wear");
            helper.succeed();
        } finally {
            player.discard();
        }
    }

    @GameTest(template = "qa_coven_arena", batch = "diviner_lava")
    public static void lavaDivinerCancelsBadAimAndStopsAtBedrock(GameTestHelper helper) {
        var level = helper.getLevel();
        var surface = helper.absolutePos(new BlockPos(5, 5, 5));
        level.setBlock(surface, Blocks.STONE.defaultBlockState(), 2);
        level.setBlock(surface.below(), Blocks.BEDROCK.defaultBlockState(), 2);
        level.setBlock(surface.below(2), Blocks.LAVA.defaultBlockState(), 2);
        var player = playerAbove(helper, surface);
        var lava = WitcheryItems.getRequired("divinerlava").get();
        var rod = new ItemStack(lava);
        player.setItemInHand(InteractionHand.OFF_HAND, rod);
        try {
            player.setXRot(-90);
            lava.use(level, player, InteractionHand.OFF_HAND);
            lava.onUseTick(level, player, rod, rod.getUseDuration());
            helper.assertTrue(!player.isUsingItem() && rod.getDamageValue() == 0, "Bad aim cancels without wear");
            player.setXRot(90);
            lava.use(level, player, InteractionHand.OFF_HAND);
            lava.onUseTick(level, player, rod, rod.getUseDuration());
            helper.assertTrue(player.isUsingItem(), "A stone top face keeps the scan active");
            lava.onUseTick(level, player, rod, rod.getUseDuration() - 1);
            helper.assertTrue(!player.isUsingItem() && rod.getDamageValue() == 1, "Bedrock ends the search early");
            level.setBlock(surface.below(), Blocks.STONE.defaultBlockState(), 2);
            rod.setDamageValue(49);
            lava.use(level, player, InteractionHand.OFF_HAND);
            lava.onUseTick(level, player, rod, rod.getUseDuration() - 1);
            helper.assertTrue(player.isUsingItem(), "The next scan can pass the former bedrock position");
            lava.onUseTick(level, player, rod, rod.getUseDuration() - 2);
            helper.assertTrue(!player.isUsingItem() && !rod.isEmpty() && rod.getDamageValue() == 50,
                    "Finding lava uses one point of wear, and the legacy rod stays usable at 50");
            lava.use(level, player, InteractionHand.OFF_HAND);
            lava.onUseTick(level, player, rod, rod.getUseDuration() - 2);
            helper.assertTrue(!player.isUsingItem() && player.getOffhandItem().isEmpty(),
                    "The next find breaks the spent rod in the offhand");
            helper.succeed();
        } finally {
            player.discard();
        }
    }

    private static FakePlayer playerAbove(GameTestHelper helper, BlockPos surface) {
        var player = new FakePlayer(helper.getLevel(), new GameProfile(UUID.randomUUID(), "DivinerQA"));
        player.gameMode.changeGameModeForPlayer(GameType.SURVIVAL);
        player.moveTo(surface.getX() + 0.5, surface.getY() + 2, surface.getZ() + 0.5, 0, 90);
        return player;
    }
}
