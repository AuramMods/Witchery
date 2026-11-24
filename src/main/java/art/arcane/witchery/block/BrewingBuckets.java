package art.arcane.witchery.block;

import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;

final class BrewingBuckets {
    private BrewingBuckets() {}

    static boolean pour(Player player, InteractionHand hand, BlockEntity vessel, Direction side) {
        ItemStack held = player.getItemInHand(hand);
        FluidStack contents = FluidUtil.getFluidContained(held).orElse(FluidStack.EMPTY);
        // Legacy vessels accept as much of the bucket as fits and discard the excess.
        boolean poured = vessel.getCapability(ForgeCapabilities.FLUID_HANDLER, side)
                .map(tank -> tank.fill(contents, IFluidHandler.FluidAction.EXECUTE) > 0).orElse(false);
        if (poured && !player.getAbilities().instabuild) {
            player.setItemInHand(hand, ItemUtils.createFilledResult(held, player, new ItemStack(Items.BUCKET), false));
        }
        return poured;
    }
}
