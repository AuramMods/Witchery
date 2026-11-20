package art.arcane.witchery.item;

import art.arcane.witchery.world.WitcheryDimensions;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class PlayerCompassItem extends Item {
    private static final String NEEDLE = "Needle";
    private static final int DIRECTIONS = 32;

    public PlayerCompassItem() {
        super(new Properties().stacksTo(1));
    }

    public static float needle(ItemStack stack) {
        return stack.getTag() == null ? 0 : Mth.clamp(stack.getTag().getInt(NEEDLE), 0, DIRECTIONS) / (float) DIRECTIONS;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity holder, int slot, boolean held) {
        if (!(level instanceof ServerLevel server) || level.getGameTime() % 10 != 2 || !TaglockItem.isBound(stack)) return;
        var target = TaglockItem.resolveTarget(server, stack);
        int frame;
        if (target == null || !target.isAlive() || target.level() != level
                && !(level.dimension().equals(WitcheryDimensions.DREAM) && target.level().dimension().equals(Level.OVERWORLD))) {
            frame = 1 + level.getRandom().nextInt(DIRECTIONS);
        } else {
            double angle = Math.atan2(target.getZ() - holder.getZ(), target.getX() - holder.getX())
                    - Math.toRadians(holder.getYRot() - 90);
            frame = 1 + Math.floorMod(Mth.floor(angle * DIRECTIONS / (Math.PI * 2)), DIRECTIONS);
        }
        stack.getOrCreateTag().putInt(NEEDLE, frame);
    }

    @Override
    public boolean onDroppedByPlayer(ItemStack item, Player player) {
        item.removeTagKey(NEEDLE);
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flags) {
        String name = TaglockItem.boundName(stack);
        tooltip.add((name.isEmpty() ? Component.translatable("item.witchery.taglockkit.unbound")
                : Component.translatable("item.witchery.taglockkit.bound", name)).withStyle(ChatFormatting.GRAY));
    }
}
