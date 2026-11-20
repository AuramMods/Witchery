package art.arcane.witchery.item;

import art.arcane.witchery.block.KettleBlock;
import art.arcane.witchery.block.WitchesCauldronBlock;
import art.arcane.witchery.registry.WitcheryItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockSource;
import net.minecraft.core.Direction;
import net.minecraft.core.dispenser.OptionalDispenseItemBehavior;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class EndlessWaterBrewItem extends Item {
    public EndlessWaterBrewItem() {
        super(new Properties().durability(100));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flags) {
        tooltip.add(Component.translatable("tooltip.witchery.brew.water", stack.getMaxDamage() - stack.getDamageValue(),
                stack.getMaxDamage()));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getLevel().isClientSide) return InteractionResult.SUCCESS;
        if (!(context.getLevel() instanceof ServerLevel server) || context.getPlayer() == null) return InteractionResult.PASS;
        Player player = context.getPlayer();
        BlockPos clicked = context.getClickedPos();
        Direction face = context.getClickedFace();
        if (fillVessel(server, clicked, player, face) || !isVessel(server, clicked)
                && placeWater(server, clicked.relative(face), player, face, context.getItemInHand())) {
            context.getItemInHand().hurtAndBreak(1, player, user -> user.broadcastBreakEvent(context.getHand()));
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    public static void registerDispenser() {
        DispenserBlock.registerBehavior(WitcheryItems.getRequired("brew.water").get(), new OptionalDispenseItemBehavior() {
            @Override
            protected ItemStack execute(BlockSource source, ItemStack stack) {
                ServerLevel level = source.getLevel();
                Direction face = source.getBlockState().getValue(DispenserBlock.FACING);
                BlockPos target = source.getPos().relative(face);
                Player player = FakePlayerFactory.getMinecraft(level);
                boolean filled = fillVessel(level, target, player, face) || !isVessel(level, target)
                        && placeWater(level, target, player, face, stack);
                setSuccess(filled);
                if (filled && stack.hurt(1, level.getRandom(), null)) stack.shrink(1);
                return stack;
            }
        });
    }

    private static boolean isVessel(ServerLevel level, BlockPos pos) {
        var state = level.getBlockState(pos);
        return state.is(Blocks.CAULDRON) || state.is(Blocks.WATER_CAULDRON)
                || state.getBlock() instanceof WitchesCauldronBlock || state.getBlock() instanceof KettleBlock;
    }

    private static boolean fillVessel(ServerLevel level, BlockPos pos, Player player, Direction face) {
        var state = level.getBlockState(pos);
        if (!level.mayInteract(player, pos)) return false;
        boolean filled = false;
        if (state.is(Blocks.CAULDRON)
                || state.is(Blocks.WATER_CAULDRON) && state.getValue(LayeredCauldronBlock.LEVEL) < 3) {
            var full = Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3);
            filled = setWater(level, pos, full, player, face);
        } else if (state.getBlock() instanceof WitchesCauldronBlock || state.getBlock() instanceof KettleBlock) {
            var vessel = level.getBlockEntity(pos);
            if (vessel != null) {
                int volume = state.getBlock() instanceof WitchesCauldronBlock ? 3000 : 1000;
                filled = vessel.getCapability(ForgeCapabilities.FLUID_HANDLER, face)
                        .map(handler -> handler.fill(new FluidStack(Fluids.WATER, volume), IFluidHandler.FluidAction.EXECUTE) > 0)
                        .orElse(false);
            }
        }
        if (filled) waterPlaced(level, pos, player);
        return filled;
    }

    private static boolean placeWater(ServerLevel level, BlockPos pos, Player player, Direction face, ItemStack stack) {
        if (!level.isInWorldBounds(pos) || !level.mayInteract(player, pos) || !player.mayUseItemAt(pos, face, stack)
                || !level.getBlockState(pos).canBeReplaced() || !level.getFluidState(pos).isEmpty()) return false;
        if (!setWater(level, pos, Blocks.WATER.defaultBlockState(), player, face)) return false;
        waterPlaced(level, pos, player);
        return true;
    }

    private static boolean setWater(ServerLevel level, BlockPos pos, BlockState state, Player player, Direction face) {
        if (!level.getWorldBorder().isWithinBounds(pos)
                || ForgeEventFactory.onBlockPlace(player, BlockSnapshot.create(level.dimension(), level, pos), face)) return false;
        return level.setBlockAndUpdate(pos, state);
    }

    private static void waterPlaced(ServerLevel level, BlockPos pos, Player player) {
        level.playSound(null, pos, SoundEvents.GENERIC_SPLASH, SoundSource.BLOCKS, 0.8F, 1);
        level.gameEvent(player, GameEvent.FLUID_PLACE, pos);
    }
}
