package art.arcane.witchery.item;

import art.arcane.witchery.client.PlayerCompassProperty;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class PlayerCompassItem extends Item {
    private static final String SAVED_NEEDLE = "Needle";

    public PlayerCompassItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity holder, int slot, boolean held) {
        if (!level.isClientSide()) {
            stack.removeTagKey(SAVED_NEEDLE);
        } else if (level.getDayTime() % 10 == 2) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> PlayerCompassProperty.tick(stack, level, holder));
        }
    }

    @Override
    public boolean onDroppedByPlayer(ItemStack item, Player player) {
        item.removeTagKey(SAVED_NEEDLE);
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flags) {
        String name = TaglockItem.boundName(stack);
        tooltip.add((name.isEmpty() ? Component.translatable("item.witchery.taglockkit.unbound")
                : Component.translatable("item.witchery.taglockkit.bound", name)).withStyle(ChatFormatting.GRAY));
    }
}
