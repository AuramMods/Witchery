package art.arcane.witchery.item;

import art.arcane.witchery.block.KettleBlock;
import art.arcane.witchery.block.WitchesCauldronBlock;
import art.arcane.witchery.event.WitcheryBlockBreak;
import art.arcane.witchery.event.WitcheryBlockPlacement;
import art.arcane.witchery.registry.WitcheryItems;
import art.arcane.witchery.registry.WitcherySounds;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionResult;
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
import net.minecraftforge.common.util.FakePlayerFactory;
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
        if (!(context.getLevel() instanceof ServerLevel level) || !(context.getPlayer() instanceof ServerPlayer player)) {
            return InteractionResult.PASS;
        }
        BlockPos clicked = context.getClickedPos();
        Direction face = context.getClickedFace();
        ItemStack bottle = context.getItemInHand();
        BlockState state = level.getBlockState(clicked);
        boolean poured;
        if (isBrewingVessel(state)) {
            poured = fillBrewingVessel(level, clicked, state, player, face);
        } else if (state.is(Blocks.CAULDRON) || state.is(Blocks.WATER_CAULDRON)) {
            poured = fillVanillaCauldron(level, clicked, state, player, face);
        } else {
            poured = placeWater(level, clicked.relative(face), player, face, bottle, false);
        }
        if (!poured) return InteractionResult.PASS;
        bottle.hurtAndBreak(1, player, user -> user.broadcastBreakEvent(context.getHand()));
        return InteractionResult.CONSUME;
    }

    public static void registerDispenser() {
        DispenserBlock.registerBehavior(WitcheryItems.getRequired("brew.water").get(), (source, bottle) -> {
            ServerLevel level = source.getLevel();
            Direction face = source.getBlockState().getValue(DispenserBlock.FACING);
            BlockPos target = source.getPos().relative(face);
            ServerPlayer player = FakePlayerFactory.getMinecraft(level);
            BlockState state = level.getBlockState(target);
            boolean poured = isBrewingVessel(state) ? fillBrewingVessel(level, target, state, player, face)
                    : placeWater(level, target, player, face, bottle, true);
            if (poured && bottle.hurt(1, level.getRandom(), null)) bottle.shrink(1);
            return bottle;
        });
    }

    private static boolean isBrewingVessel(BlockState state) {
        return state.getBlock() instanceof WitchesCauldronBlock || state.getBlock() instanceof KettleBlock;
    }

    private static boolean fillVanillaCauldron(ServerLevel level, BlockPos pos, BlockState state,
                                               ServerPlayer player, Direction face) {
        if (!level.mayInteract(player, pos)
                || state.is(Blocks.WATER_CAULDRON) && state.getValue(LayeredCauldronBlock.LEVEL) == 3) return false;
        var full = Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3);
        if (!setWater(level, pos, full, player, face)) return false;
        waterPlaced(level, pos, player, SoundEvents.GENERIC_SPLASH, true);
        return true;
    }

    private static boolean fillBrewingVessel(ServerLevel level, BlockPos pos, BlockState state,
                                               ServerPlayer player, Direction face) {
        var vessel = level.getBlockEntity(pos);
        if (vessel == null || !level.mayInteract(player, pos)) return false;
        int volume = state.getBlock() instanceof WitchesCauldronBlock ? 3000 : 1000;
        boolean filled = vessel.getCapability(ForgeCapabilities.FLUID_HANDLER, face)
                .map(handler -> handler.fill(new FluidStack(Fluids.WATER, volume), IFluidHandler.FluidAction.EXECUTE) > 0)
                .orElse(false);
        if (filled) waterPlaced(level, pos, player, SoundEvents.GENERIC_SWIM, false);
        return filled;
    }

    private static boolean placeWater(ServerLevel level, BlockPos pos, ServerPlayer player, Direction face,
                                      ItemStack bottle, boolean fromDispenser) {
        if (!level.isInWorldBounds(pos) || !level.mayInteract(player, pos) || !player.mayUseItemAt(pos, face, bottle)) return false;
        BlockState previous = level.getBlockState(pos);
        if (!previous.canBeReplaced()) return false;
        var fluid = previous.getFluidState();
        if (!fluid.isEmpty() && !fluid.is(FluidTags.LAVA) && (fromDispenser || !fluid.is(FluidTags.WATER))) return false;
        if (WitcheryBlockBreak.isCanceled(level, pos, previous, player)) return false;
        var water = Blocks.WATER.defaultBlockState();
        if (previous != water && !setWater(level, pos, water, player, face)) return false;
        waterPlaced(level, pos, player, SoundEvents.GENERIC_SPLASH, !fromDispenser);
        return true;
    }

    private static boolean setWater(ServerLevel level, BlockPos pos, BlockState state, ServerPlayer player, Direction face) {
        // Resolve the pour before durability and sound. The item's outer Forge capture still owns its other changes.
        return level.getWorldBorder().isWithinBounds(pos) && WitcheryBlockPlacement.placeNow(level, pos, state, player, face);
    }

    private static void waterPlaced(ServerLevel level, BlockPos pos, ServerPlayer player, SoundEvent sound, boolean atPlayer) {
        if (atPlayer) {
            WitcherySounds.playLegacyPlayerCue(player, sound, 0.5F);
        } else {
            float pitch = 0.4F / ((float) level.getRandom().nextDouble() * 0.4F + 0.8F);
            level.playSound(null, pos.getX(), pos.getY(), pos.getZ(), sound, SoundSource.PLAYERS, 0.5F, pitch);
        }
        level.gameEvent(player, GameEvent.FLUID_PLACE, pos);
    }
}
