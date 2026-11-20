package art.arcane.witchery.item;

import art.arcane.witchery.entity.ThrownBrew;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

import java.util.function.BiConsumer;

public final class ThrowableBrewItem extends Item {
    private final BiConsumer<ThrownBrew, HitResult> effect;

    public ThrowableBrewItem(BiConsumer<ThrownBrew, HitResult> effect) {
        super(new Properties());
        this.effect = effect;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        level.playSound(null, player, SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS,
                0.5F, 0.4F / (level.random.nextFloat() * 0.4F + 0.8F));
        if (!level.isClientSide) {
            ThrownBrew brew = new ThrownBrew(player, level);
            brew.setItem(stack);
            brew.shootFromRotation(player, player.getXRot(), player.getYRot(), -20.0F, 0.5F, 1.0F);
            level.addFreshEntity(brew);
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    public void impact(ThrownBrew brew, HitResult hit) {
        effect.accept(brew, hit);
    }
}
