package art.arcane.witchery.block.entity;

import net.minecraft.world.item.ItemStack;

final class WorkstationOutput {
    private WorkstationOutput() {}

    static boolean fits(ItemStack existing, ItemStack result, int containerLimit) {
        int limit = Math.min(containerLimit, result.getMaxStackSize());
        if (existing.isEmpty()) return result.getCount() <= limit;

        // Plain recipe results can grow a matching stack while preserving its saved data.
        boolean compatible = result.hasTag() ? ItemStack.isSameItemSameTags(existing, result)
                : ItemStack.isSameItem(existing, result) && existing.getDamageValue() == result.getDamageValue()
                    && existing.areCapsCompatible(result);
        return compatible && existing.getCount() + result.getCount() <= Math.min(limit, existing.getMaxStackSize());
    }
}
