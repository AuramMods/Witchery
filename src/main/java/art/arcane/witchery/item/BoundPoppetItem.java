package art.arcane.witchery.item;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class BoundPoppetItem extends Item {
    static final int USE_LIMIT = 1_000;
    private static final String DAMAGE = "WITCDamage";

    public BoundPoppetItem() {
        super(new Properties().stacksTo(1));
    }

    static ItemStack inInventory(ServerPlayer player, Item type) {
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            ItemStack stack = inventory.getItem(slot);
            if (stack.is(type) && TaglockItem.isBoundTo(stack, player)) return stack;
        }
        return ItemStack.EMPTY;
    }

    static void wear(ItemStack poppet, int amount) {
        int damage = Math.min(USE_LIMIT, wear(poppet) + amount);
        poppet.getOrCreateTag().putInt(DAMAGE, damage);
        if (damage >= USE_LIMIT) poppet.shrink(1);
    }

    private static int wear(ItemStack poppet) {
        CompoundTag tag = poppet.getTag();
        return tag == null ? 0 : Mth.clamp(tag.getInt(DAMAGE), 0, USE_LIMIT);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return wear(stack) > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13 * (1.0F - (float) wear(stack) / USE_LIMIT));
    }

    @Override
    public int getBarColor(ItemStack stack) {
        float remaining = 1.0F - (float) wear(stack) / USE_LIMIT;
        return Mth.hsvToRgb(remaining / 3.0F, 1, 1);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flags) {
        String name = TaglockItem.boundName(stack);
        Component status = name.isEmpty() ? Component.translatable("item.witchery.bound_poppet.unbound")
                : Component.translatable("item.witchery.bound_poppet.bound", name);
        tooltip.add(status.copy().withStyle(ChatFormatting.GRAY));
    }
}
