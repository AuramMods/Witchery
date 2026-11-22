package art.arcane.witchery.item;

import art.arcane.witchery.infusion.OtherwhereInfusion;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;

public final class DivinerItem extends Item {
    private final Block fluidBlock;

    public DivinerItem(Block fluidBlock) {
        super(new Properties().durability(50).rarity(Rarity.UNCOMMON));
        this.fluidBlock = fluidBlock;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 400;
    }

    @Override
    public void onUseTick(Level level, LivingEntity user, ItemStack stack, int remaining) {
        if (!(level instanceof ServerLevel server) || !(user instanceof ServerPlayer player)) return;
        if (!(OtherwhereInfusion.rayTrace(player, 6) instanceof BlockHitResult hit)
                || hit.getType() != BlockHitResult.Type.BLOCK || hit.getDirection() != Direction.UP) {
            server.playSound(null, player, SoundEvents.NOTE_BLOCK_SNARE.value(), SoundSource.PLAYERS, 1, 1);
            player.stopUsingItem();
            return;
        }

        BlockPos surface = hit.getBlockPos();
        BlockPos sample = surface.below(getUseDuration(stack) - remaining);
        var state = server.getBlockState(sample);
        boolean found = state.is(fluidBlock) && state.getFluidState().isSource();
        if (!found && !state.is(Blocks.BEDROCK) && sample.getY() > server.getMinBuildHeight() + 1) return;

        double x = surface.getX() + 0.5;
        double y = surface.getY() + 1;
        double z = surface.getZ() + 0.5;
        server.sendParticles(found ? ParticleTypes.ENCHANTED_HIT : ParticleTypes.SMOKE, x, y, z,
                8, 0.5, 0.5, 0.5, 0);
        server.playSound(null, x, y, z, found ? SoundEvents.EXPERIENCE_ORB_PICKUP : SoundEvents.NOTE_BLOCK_SNARE.value(),
                SoundSource.PLAYERS, 1, 1);
        InteractionHand hand = player.getUsedItemHand();
        player.stopUsingItem();
        // The original rod could still complete one scan at its maximum damage.
        if (!player.getAbilities().instabuild && stack.hurt(1, player.getRandom(), player)
                && stack.getDamageValue() > stack.getMaxDamage()) {
            player.broadcastBreakEvent(hand);
            player.awardStat(Stats.ITEM_BROKEN.get(this));
            stack.shrink(1);
            stack.setDamageValue(0);
        }
    }
}
