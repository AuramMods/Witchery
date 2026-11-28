package art.arcane.witchery.item;

import art.arcane.witchery.entity.CreatureAlignment;
import art.arcane.witchery.lycanthropy.Lycanthropy;
import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.registry.WitcherySounds;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public final class WolfsbaneFlowerItem extends Item {
    private static final double REACH = 2;

    public WolfsbaneFlowerItem() { super(new Properties()); }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return new InteractionResultHolder<>(test(player, hand), player.getItemInHand(hand));
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player user, LivingEntity target, InteractionHand hand) {
        return test(user, hand);
    }

    public InteractionResult test(Player user, InteractionHand hand) {
        ItemStack held = user.getItemInHand(hand);
        if (!held.is(this)) return InteractionResult.PASS;
        Entity target = findTarget(user);
        if (target == null) return InteractionResult.PASS;
        if (user instanceof ServerPlayer tester) {
            if (target instanceof LivingEntity living && (Lycanthropy.isWerewolf(living) || CreatureAlignment.werewolf(living))) {
                WitcheryNetwork.sendWolfsbaneTestCue(tester.serverLevel(), target.position());
            } else {
                WitcherySounds.playLegacyPlayerCue(tester, SoundEvents.NOTE_BLOCK_SNARE.value(), 0.5F);
            }
            held.shrink(1);
        }
        return InteractionResult.sidedSuccess(user.level().isClientSide);
    }

    @Nullable
    private static Entity findTarget(Player user) {
        var eye = user.getEyePosition();
        var end = eye.add(user.getLookAngle().scale(REACH));
        var search = user.getBoundingBox().inflate(1.1 * REACH);
        double closest = REACH * REACH;
        Entity selected = null;
        // The original test traced entity bounds independently of the block crosshair.
        for (Entity entity : user.level().getEntities(user, search, Entity::isPickable)) {
            var bounds = entity.getBoundingBox().inflate(Math.max(0.1F, entity.getPickRadius()));
            if (bounds.contains(eye)) {
                if (!bounds.contains(end)) {
                    selected = entity;
                    closest = 0;
                }
                continue;
            }
            var impact = bounds.clip(eye, end);
            if (impact.isEmpty()) continue;
            double distance = eye.distanceToSqr(impact.get());
            if (distance < closest || closest == 0) {
                selected = entity;
                closest = distance;
            }
        }
        return selected;
    }
}
