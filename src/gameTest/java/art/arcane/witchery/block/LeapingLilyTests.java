package art.arcane.witchery.block;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryBlocks;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class LeapingLilyTests {
    @GameTest(template = "qa_coven_arena")
    public static void craftPlaceAndUseTheOriginalLeapingLily(GameTestHelper helper) {
        var level = helper.getLevel();
        var lily = WitcheryBlocks.getRequired("leapinglily").get();
        var crafting = new TransientCraftingContainer(new AbstractContainerMenu(MenuType.CRAFTING, 0) {
            @Override public boolean stillValid(Player player) { return true; }
            @Override public ItemStack quickMoveStack(Player player, int slot) { return ItemStack.EMPTY; }
        }, 3, 3);
        for (int slot : new int[]{0, 2, 4, 6, 8}) crafting.setItem(slot, new ItemStack(Items.LILY_PAD));
        for (int slot : new int[]{3, 5}) crafting.setItem(slot, new ItemStack(Items.GLOWSTONE_DUST));
        crafting.setItem(7, new ItemStack(WitcheryIngredient.BREATH_OF_THE_GODDESS.item()));
        crafting.setItem(1, PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.LONG_SWIFTNESS));
        helper.assertTrue(level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, crafting, level).isEmpty(),
                "The extended potion does not satisfy the original recipe");
        crafting.setItem(1, PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.SWIFTNESS));
        var recipe = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, crafting, level).orElseThrow();
        var stack = recipe.assemble(crafting, level.registryAccess());
        helper.assertTrue(stack.is(lily.asItem()) && stack.getCount() == 5, "The original grid makes five Leaping Lilies");

        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "LilyPlacement"));
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        BlockPos dry = helper.absolutePos(new BlockPos(4, 3, 4));
        level.setBlockAndUpdate(dry, Blocks.STONE.defaultBlockState());
        player.moveTo(dry.getX() + 0.5, dry.getY() + 1, dry.getZ() + 2.5, 0, 0);
        var hit = new BlockHitResult(Vec3.atCenterOf(dry).add(0, 0.5, 0), Direction.UP, dry, false);
        helper.assertTrue(stack.getItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit)).consumesAction()
                        && level.getBlockState(dry.above()).is(lily), "Place a crafted lily on a solid block");

        BlockPos water = helper.absolutePos(new BlockPos(8, 3, 4));
        level.setBlockAndUpdate(water.below(), Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(water, Blocks.WATER.defaultBlockState());
        player.moveTo(water.getX() + 0.5, water.getY() + 2, water.getZ() + 0.5, 0, 90);
        var floorHit = new BlockHitResult(Vec3.atBottomCenterOf(water), Direction.UP, water.below(), false);
        helper.assertTrue(stack.getItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, floorHit)).consumesAction()
                        && level.getBlockState(water.above()).is(lily) && level.getFluidState(water).isSource()
                        && stack.getCount() == 3,
                "A block hit through shallow water places one lily above the source and keeps the water");

        BlockPos deepWater = water.south(3);
        level.setBlockAndUpdate(deepWater, Blocks.WATER.defaultBlockState());
        player.moveTo(deepWater.getX() + 0.5, deepWater.getY() + 2, deepWater.getZ() + 0.5, 0, 90);
        helper.assertTrue(stack.getItem().use(level, player, InteractionHand.MAIN_HAND).getResult().consumesAction()
                        && level.getBlockState(deepWater.above()).is(lily) && stack.getCount() == 2,
                "An ordinary item use can also place a single lily over source water");

        BlockPos flowing = water.east(3);
        level.setBlockAndUpdate(flowing.below(), Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(flowing, Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL, 1));
        player.moveTo(flowing.getX() + 0.5, flowing.getY() + 2, flowing.getZ() + 0.5, 0, 90);
        var flowingHit = new BlockHitResult(Vec3.atBottomCenterOf(flowing), Direction.UP, flowing.below(), false);
        helper.assertTrue(!stack.getItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, flowingHit)).consumesAction()
                        && level.getBlockState(flowing).is(Blocks.WATER) && !level.getFluidState(flowing).isSource()
                        && level.isEmptyBlock(flowing.above()) && stack.getCount() == 2,
                "Flowing water remains in place and no lily is spent");

        BlockPos gas = flowing.south(3);
        level.setBlockAndUpdate(gas.below(), Blocks.STONE.defaultBlockState());
        level.setBlockAndUpdate(gas, WitcheryBlocks.getRequired("brewgas").get().defaultBlockState());
        player.moveTo(gas.getX() + 0.5, gas.getY() + 2, gas.getZ() + 0.5, 0, 90);
        var gasFloor = new BlockHitResult(Vec3.atBottomCenterOf(gas), Direction.UP, gas.below(), false);
        helper.assertTrue(stack.getItem().useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, gasFloor)).consumesAction()
                        && level.getBlockState(gas).is(lily) && stack.getCount() == 1,
                "The lily can displace gas over a solid block");
        helper.assertTrue(level.getBlockState(dry.above()).getLightEmission() == 6, "Leaping Lilies emit the original light level");

        var pig = helper.spawn(EntityType.PIG, new BlockPos(4, 5, 4));
        var boosted = helper.spawn(EntityType.PIG, new BlockPos(8, 5, 4));
        boosted.addEffect(new MobEffectInstance(MobEffects.JUMP, 200, 6));
        helper.succeedWhen(() -> {
            var jump = pig.getEffect(MobEffects.JUMP);
            var speed = pig.getEffect(MobEffects.MOVEMENT_SPEED);
            helper.assertTrue(jump != null && jump.getAmplifier() == 4 && jump.getDuration() <= 10
                            && speed != null && speed.getAmplifier() == 0 && speed.getDuration() <= 10,
                    "Contact gives the original brief Jump Boost V and Speed I");
            helper.assertTrue(boosted.hasEffect(MobEffects.MOVEMENT_SPEED) && boosted.getEffect(MobEffects.JUMP).getAmplifier() == 6,
                    "Contact leaves an existing jump effect in place");
        });
    }
}
