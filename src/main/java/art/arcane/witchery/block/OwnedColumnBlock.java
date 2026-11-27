package art.arcane.witchery.block;

import art.arcane.witchery.block.entity.PlacedOwnerBlockEntity;
import art.arcane.witchery.event.WitcheryBlockBreak;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.concurrent.ThreadLocalRandom;

public abstract class OwnedColumnBlock extends Block implements EntityBlock {
    private final boolean requiresSneak;

    protected OwnedColumnBlock(Properties properties, boolean requiresSneak) {
        super(properties);
        this.requiresSneak = requiresSneak;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (!level.isClientSide && placer instanceof Player player
                && level.getBlockEntity(pos) instanceof PlacedOwnerBlockEntity owned) owned.placedBy(player);
    }

    @Override
    public void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (level instanceof ServerLevel server && !player.isCreative() && (!requiresSneak || player.isShiftKeyDown())
                && level.getBlockEntity(pos) instanceof PlacedOwnerBlockEntity owned && owned.canRecover(player)) {
            recoverColumn(server, pos, this, player);
        }
    }

    @Override
    public void playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (level instanceof ServerLevel server && player.isCreative()) {
            server.addFreshEntity(recoveryItem(server, pos, this));
            recoverColumn(server, pos.above(), this, player);
        }
        super.playerWillDestroy(level, pos, state, player);
    }

    static void recoverColumn(ServerLevel level, BlockPos start, Block block, Player player) {
        for (BlockPos pos = start; level.getBlockState(pos).is(block); pos = pos.above()) {
            BlockState state = level.getBlockState(pos);
            if (!level.mayInteract(player, pos) || WitcheryBlockBreak.isCanceled(level, pos, state, player)) return;
            // A rejected return leaves the placed block available for another recovery attempt.
            ItemEntity drop = recoveryItem(level, pos, block);
            if (!level.addFreshEntity(drop)) return;
            if (level.getBlockState(pos) != state || !level.removeBlock(pos, false)) {
                drop.discard();
                return;
            }
        }
    }

    static ItemEntity recoveryItem(ServerLevel level, BlockPos pos, Block block) {
        Vec3 center = Vec3.atCenterOf(pos);
        var random = ThreadLocalRandom.current();
        ItemEntity drop = new ItemEntity(level, center.x, center.y, center.z, new ItemStack(block),
                random.nextDouble(-0.1, 0.1), 0.2F, random.nextDouble(-0.1, 0.1));
        drop.setNoPickUpDelay();
        return drop;
    }
}
