package art.arcane.witchery.item;

import art.arcane.witchery.block.MirrorBlock;
import art.arcane.witchery.block.entity.MirrorBlockEntity;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.world.MirrorTravel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class MirrorItem extends Item {
    public MirrorItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Direction side = context.getClickedFace();
        Player player = context.getPlayer();
        if (!side.getAxis().isHorizontal() || player == null) return InteractionResult.FAIL;
        Level level = context.getLevel();
        BlockPos upper = context.getClickedPos().relative(side);
        if (!level.isEmptyBlock(upper) || !level.isEmptyBlock(upper.below())
                || !level.mayInteract(player, upper) || !level.mayInteract(player, upper.below())
                || !player.mayUseItemAt(upper, side, context.getItemInHand())
                || !player.mayUseItemAt(upper.below(), side, context.getItemInHand())) {
            return InteractionResult.FAIL;
        }
        if (level instanceof ServerLevel server) {
            MirrorBlock mirror = (MirrorBlock) WitcheryBlocks.getRequired("mirrorblock").get();
            if (!mirror.placePair(level, upper, side)) return InteractionResult.FAIL;
            if (level.getBlockEntity(upper) instanceof MirrorBlockEntity mounted) {
                mounted.applyItem(context.getItemInHand());
                MirrorTravel.reconnect(server, mounted);
            }
            if (!player.getAbilities().instabuild) context.getItemInHand().shrink(1);
            level.playSound(null, upper, SoundEvents.GLASS_PLACE, SoundSource.BLOCKS, 1, 0.85F);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flags) {
        boolean slain = stack.hasTag() && stack.getTag().getBoolean("DemonSlain");
        tooltip.add(Component.translatable(slain ? "item.witchery.mirror.bridge" : "item.witchery.mirror.inhabited"));
    }
}
