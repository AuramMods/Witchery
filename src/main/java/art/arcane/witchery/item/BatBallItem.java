package art.arcane.witchery.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public final class BatBallItem extends Item {
    private static final double SPREAD = 0.0075F;

    public BatBallItem() { super(new Properties()); }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack held = player.getItemInHand(hand);
        if (!held.is(this)) return InteractionResultHolder.pass(held);
        if (level.isClientSide) return InteractionResultHolder.success(held);

        float yaw = player.getYRot() / 180.0F * (float) Math.PI;
        float pitch = player.getXRot() / 180.0F * (float) Math.PI;
        double x = -Mth.sin(yaw) * Mth.cos(pitch) * 0.4F;
        double y = -Mth.sin(pitch) * 0.4F;
        double z = Mth.cos(yaw) * Mth.cos(pitch) * 0.4F;
        double length = (float) Math.sqrt(x * x + y * y + z * z);
        x = x / length + level.random.nextGaussian() * SPREAD;
        y = y / length + level.random.nextGaussian() * SPREAD;
        z = z / length + level.random.nextGaussian() * SPREAD;

        var thrown = new ItemEntity(level, player.getX() - Mth.cos(yaw) * 0.16F,
                player.getEyeY() + 0.125 - 0.1F, player.getZ() - Mth.sin(yaw) * 0.16F,
                new ItemStack(this), x, y, z);
        thrown.setPickUpDelay(5);
        thrown.yRotO = (float) Math.toDegrees(Math.atan2(x, z));
        thrown.xRotO = (float) Math.toDegrees(Math.atan2(y, (float) Math.sqrt(x * x + z * z)));
        thrown.setYRot(thrown.yRotO);
        thrown.setXRot(thrown.xRotO);
        if (!level.addFreshEntity(thrown)) return InteractionResultHolder.fail(held);
        held.shrink(1);
        return InteractionResultHolder.consume(held);
    }

    public static void dropCarried(ServerPlayer player) {
        var inventory = player.getInventory();
        inventory.items.replaceAll(stack -> dropOnHit(player, stack));
        inventory.offhand.replaceAll(stack -> dropOnHit(player, stack));
    }

    private static ItemStack dropOnHit(ServerPlayer player, ItemStack stack) {
        if (!(stack.getItem() instanceof BatBallItem)) return stack;
        ItemEntity dropped = player.drop(stack.copy(), true);
        return dropped != null && dropped.isAddedToWorld() ? ItemStack.EMPTY : stack;
    }
}
