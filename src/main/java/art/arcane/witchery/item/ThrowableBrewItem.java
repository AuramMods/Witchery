package art.arcane.witchery.item;

import art.arcane.witchery.entity.ThrownBrew;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

import java.util.function.BiConsumer;
import java.util.function.BiPredicate;

public final class ThrowableBrewItem extends Item {
    private final BiPredicate<ThrownBrew, HitResult> effect;

    public ThrowableBrewItem(BiConsumer<ThrownBrew, HitResult> effect) {
        this((brew, hit) -> {
            effect.accept(brew, hit);
            return true;
        });
    }

    private ThrowableBrewItem(BiPredicate<ThrownBrew, HitResult> effect) {
        super(new Properties());
        this.effect = effect;
    }

    public static ThrowableBrewItem withConditionalBreak(BiPredicate<ThrownBrew, HitResult> effect) {
        return new ThrowableBrewItem(effect);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return throwFromHand(level, player, hand);
    }

    static InteractionResultHolder<ItemStack> throwFromHand(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel server && !ThrownBrew.throwBottle(server, player, stack)) {
            return InteractionResultHolder.fail(stack);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    public void impact(ThrownBrew brew, HitResult hit) {
        impactWithBreakCue(brew, hit);
    }

    public boolean impactWithBreakCue(ThrownBrew brew, HitResult hit) {
        return effect.test(brew, hit);
    }
}
