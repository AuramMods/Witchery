package art.arcane.witchery.block;

import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;

final class BrewingContainers {
    private BrewingContainers() {}

    static boolean isWaterBottle(ItemStack stack) {
        return stack.is(Items.POTION) && PotionUtils.getPotion(stack) == Potions.WATER;
    }

    static boolean pourBucket(Player player, InteractionHand hand, BlockEntity vessel, Direction side) {
        ItemStack held = player.getItemInHand(hand);
        FluidStack contents = FluidUtil.getFluidContained(held).orElse(FluidStack.EMPTY);
        boolean poured = pour(vessel, side, contents);
        if (poured && !player.getAbilities().instabuild) {
            player.setItemInHand(hand, ItemUtils.createFilledResult(held, player, new ItemStack(Items.BUCKET), false));
        }
        return poured;
    }

    static boolean pourWaterBottle(Player player, InteractionHand hand, BlockEntity vessel, Direction side) {
        // Forge 1.7.10 registered a Water Bottle as one bucket of water.
        boolean poured = pour(vessel, side, new FluidStack(Fluids.WATER, FluidType.BUCKET_VOLUME));
        if (poured && !player.getAbilities().instabuild) player.getItemInHand(hand).shrink(1);
        return poured;
    }

    static boolean fillWaterBottle(Player player, InteractionHand hand, BlockEntity vessel, Direction side) {
        var tank = vessel.getCapability(ForgeCapabilities.FLUID_HANDLER, side).orElse(null);
        if (tank == null) return false;
        FluidStack liquid = tank.getFluidInTank(0);
        if (liquid.getFluid() != Fluids.WATER) return false;
        FluidStack portion = new FluidStack(liquid, FluidType.BUCKET_VOLUME);
        if (tank.drain(portion, IFluidHandler.FluidAction.SIMULATE).getAmount() != FluidType.BUCKET_VOLUME) return false;

        if (!player.getAbilities().instabuild) {
            ItemStack empty = player.getItemInHand(hand);
            ItemStack filled = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER);
            if (empty.getCount() == 1) {
                player.setItemInHand(hand, filled);
            } else {
                if (!player.addItem(filled)) return false;
                empty.shrink(1);
            }
        }
        tank.drain(portion, IFluidHandler.FluidAction.EXECUTE);
        player.level().playSound(null, vessel.getBlockPos(), SoundEvents.GENERIC_SWIM, SoundSource.BLOCKS, 1, 1);
        return true;
    }

    private static boolean pour(BlockEntity vessel, Direction side, FluidStack contents) {
        return vessel.getCapability(ForgeCapabilities.FLUID_HANDLER, side)
                .map(tank -> tank.fill(contents, IFluidHandler.FluidAction.EXECUTE) > 0).orElse(false);
    }
}
