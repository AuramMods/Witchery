package art.arcane.witchery.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class LegacyContainerDrops {
    private LegacyContainerDrops() {}

    public static void scatter(Level level, BlockPos pos, ItemStack stack) {
        if (stack.isEmpty()) return;
        var random = level.random;
        double x = pos.getX() + (random.nextFloat() * 0.8F + 0.1F);
        double y = pos.getY() + (random.nextFloat() * 0.8F + 0.1F);
        double z = pos.getZ() + (random.nextFloat() * 0.8F + 0.1F);
        int remaining = stack.getCount();
        while (remaining > 0) {
            int count = Math.min(remaining, 10 + random.nextInt(21));
            var drop = new ItemEntity(level, x, y, z, stack.copyWithCount(count),
                    (float) random.nextGaussian() * 0.05F, (float) random.nextGaussian() * 0.05F + 0.2F,
                    (float) random.nextGaussian() * 0.05F);
            level.addFreshEntity(drop);
            remaining -= count;
        }
    }
}
