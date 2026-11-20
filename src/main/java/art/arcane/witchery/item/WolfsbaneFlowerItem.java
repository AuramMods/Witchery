package art.arcane.witchery.item;

import art.arcane.witchery.lycanthropy.Lycanthropy;
import art.arcane.witchery.registry.WitcherySounds;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class WolfsbaneFlowerItem extends Item {
    private static final double REACH = 2;

    public WolfsbaneFlowerItem() { super(new Properties()); }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player user, LivingEntity target, InteractionHand hand) {
        if (!target.isAlive()) return InteractionResult.PASS;
        var eye = user.getEyePosition();
        var bounds = target.getBoundingBox().inflate(target.getPickRadius());
        if (!bounds.contains(eye) && bounds.clip(eye, eye.add(user.getLookAngle().scale(REACH))).isEmpty()) {
            return InteractionResult.PASS;
        }
        if (user instanceof ServerPlayer tester) {
            var level = tester.serverLevel();
            if (Lycanthropy.isWerewolf(target)) {
                level.sendParticles(ParticleTypes.FLAME, target.getX(), target.getY(0.5), target.getZ(),
                        16, 0.25, 0.75, 0.25, 0.03);
                level.playSound(null, target, WitcherySounds.WOLFMAN_HOWL.get(), SoundSource.PLAYERS, 1, 1);
            } else {
                tester.playNotifySound(SoundEvents.NOTE_BLOCK_SNARE.value(), SoundSource.PLAYERS, 1, 1);
            }
            stack.shrink(1);
        }
        return InteractionResult.sidedSuccess(user.level().isClientSide);
    }
}
