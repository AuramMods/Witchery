package art.arcane.witchery.block;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryItems;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class AlluringSkullTests {
    @GameTest(template = "qa_coven_arena", timeoutTicks = 160)
    public static void craftsActivatesLuresAndRecoversTheSkull(GameTestHelper helper) {
        var level = helper.getLevel();
        var skull = WitcheryBlocks.getRequired("alluringskull").get();
        var crafting = new TransientCraftingContainer(new AbstractContainerMenu(MenuType.CRAFTING, 0) {
            @Override public boolean stillValid(Player player) { return true; }
            @Override public ItemStack quickMoveStack(Player player, int slot) { return ItemStack.EMPTY; }
        }, 3, 3);
        crafting.setItem(1, new ItemStack(WitcheryIngredient.NECROMANTIC_STONE.item()));
        crafting.setItem(3, new ItemStack(Items.GLOWSTONE_DUST));
        crafting.setItem(5, new ItemStack(Items.GLOWSTONE_DUST));
        crafting.setItem(7, new ItemStack(WitcheryItems.getRequired("voodoo_poppet").get()));
        crafting.setItem(4, new ItemStack(Items.ZOMBIE_HEAD));
        helper.assertTrue(level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, crafting, level).isEmpty(),
                "A Zombie Head does not satisfy the Alluring Skull recipe");
        crafting.setItem(4, new ItemStack(Items.SKELETON_SKULL));
        var recipe = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, crafting, level).orElseThrow();
        var stack = recipe.assemble(crafting, level.registryAccess());
        helper.assertTrue(stack.is(skull.asItem()) && stack.getMaxStackSize() == 1,
                "Craft one unstackable Alluring Skull from a Skeleton Skull");
        crafting.setItem(4, new ItemStack(Items.WITHER_SKELETON_SKULL));
        helper.assertTrue(level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, crafting, level).orElseThrow().equals(recipe),
                "The same recipe accepts a Wither Skeleton Skull");

        var player = new FakePlayer(level, new GameProfile(UUID.randomUUID(), "SkullPlacement"));
        var support = new BlockPos(7, 2, 7);
        helper.setBlock(support, Blocks.STONE);
        BlockPos absolute = helper.absolutePos(support);
        var hit = new BlockHitResult(Vec3.atCenterOf(absolute).add(0, 0.5, 0), Direction.UP, absolute, false);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        player.setYRot(135);
        helper.assertTrue(stack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit)).consumesAction() && stack.isEmpty(),
                "The original placement spends the crafted skull");
        BlockPos skullPos = absolute.above();
        var inactive = level.getBlockState(skullPos);
        helper.assertTrue(inactive.is(skull) && !inactive.getValue(AlluringSkullBlock.ACTIVE)
                        && inactive.getValue(AlluringSkullBlock.ROTATION) == 6 && inactive.getLightEmission() == 7,
                "Placement keeps floor rotation and the original constant light level");

        var use = new BlockHitResult(Vec3.atCenterOf(skullPos), Direction.UP, skullPos, false);
        var stone = new ItemStack(WitcheryIngredient.NECROMANTIC_STONE.item());
        player.setItemInHand(InteractionHand.MAIN_HAND, stone);
        helper.assertTrue(inactive.use(level, player, InteractionHand.MAIN_HAND, use).consumesAction()
                        && level.getBlockState(skullPos).getValue(AlluringSkullBlock.ACTIVE),
                "Right-clicking with a Necromantic Stone arms the skull");
        var undead = new Zombie(EntityType.ZOMBIE, level) {
            @Override protected void registerGoals() {}
        };
        Vec3 undeadStart = Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(3, 2, 3)));
        undead.setPos(undeadStart);
        undead.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.CARVED_PUMPKIN));
        level.addFreshEntity(undead);
        var pig = new Pig(EntityType.PIG, level) {
            @Override protected void registerGoals() {}
        };
        pig.setPos(Vec3.atBottomCenterOf(helper.absolutePos(new BlockPos(3, 2, 4))));
        level.addFreshEntity(pig);

        helper.runAfterDelay(115, () -> {
            var path = undead.getNavigation().getPath();
            helper.assertTrue(path != null && path.getTarget().distManhattan(skullPos) <= 1
                            && undead.position().distanceTo(undeadStart) > 0.1,
                    "The skull gives the zombie a route to the skull. Path: " + path + ", zombie: " + undead.position());
            helper.assertTrue(pig.getNavigation().getPath() == null, "The skull leaves living mobs alone");
            helper.assertTrue(level.getBlockState(skullPos).use(level, player, InteractionHand.MAIN_HAND, use).consumesAction()
                            && level.isEmptyBlock(skullPos) && stone.getCount() == 1,
                    "A second use recovers the skull without using up the stone");
            helper.assertItemEntityCountIs(skull.asItem(), support.above(), 3, 1);
            helper.succeed();
        });
    }
}
