package art.arcane.witchery.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public final class OwnershipContractItem extends Item {
    public OwnershipContractItem() {
        super(new Properties());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flags) {
        String name = TaglockItem.boundName(stack);
        Component description = name.isEmpty() ? Component.translatable("item.witchery.contract_of_ownership.unsigned")
                : Component.translatable("item.witchery.contract_of_ownership.signed", name);
        tooltip.add(description.copy().withStyle(ChatFormatting.GRAY));
    }
}
