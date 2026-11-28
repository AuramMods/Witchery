package art.arcane.witchery.block;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class LegacyContainerDrops {
    private LegacyContainerDrops() {}

    public static void scatter(Level level, BlockPos pos, ItemStack stack) {
        scatter(level, pos, stack, level.random);
    }

    public static void scatter(Level level, BlockPos pos, Container inventory, RandomSource random) {
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            scatter(level, pos, inventory.getItem(slot), random);
        }
    }

    private static void scatter(Level level, BlockPos pos, ItemStack stack, RandomSource random) {
        if (stack.isEmpty()) return;
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
