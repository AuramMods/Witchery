package art.arcane.witchery.block;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(Witchery.MODID)
@PrefixGameTestTemplate(false)
public final class KettleDropTests {
    @GameTest(template = "qa_coven_arena")
    public static void kettleCollectsIngredientsAtItsRim(GameTestHelper helper) {
        var level = helper.getLevel();
        var relative = new BlockPos(5, 3, 5);
        helper.setBlock(relative, WitcheryBlocks.getRequired("kettle").get());
        helper.setBlock(relative.below(), Blocks.STONE);
        helper.setBlock(relative.east().below(), Blocks.STONE);
        BlockPos pos = helper.absolutePos(relative);
        var kettle = level.getBlockEntity(pos);
        var fluid = kettle.getCapability(ForgeCapabilities.FLUID_HANDLER).orElseThrow(IllegalStateException::new);
        var inventory = kettle.getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(IllegalStateException::new);
        helper.assertTrue(fluid.fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE) == 1000,
                "The fixture must fill the kettle");

        var atRim = new ItemEntity(level, pos.getX() + 0.81, pos.getY() + 1.35, pos.getZ() + 0.5,
                new ItemStack(Items.POPPY));
        var beside = new ItemEntity(level, pos.getX() + 1.5, pos.getY() + 1.35, pos.getZ() + 0.5,
                new ItemStack(Items.GOLDEN_CARROT));
        atRim.setDeltaMovement(0, -0.1, 0);
        beside.setDeltaMovement(0, -0.1, 0);
        level.addFreshEntity(atRim);
        level.addFreshEntity(beside);

        helper.runAfterDelay(25, () -> {
            helper.assertTrue(!atRim.isAlive() && inventory.getStackInSlot(0).is(Items.POPPY),
                    "An ingredient that lands on the inside edge of the kettle must enter its first slot");
            helper.assertTrue(beside.isAlive() && inventory.getStackInSlot(1).isEmpty(),
                    "An ingredient outside the kettle block must stay on the ground");
            helper.succeed();
        });
    }
}
