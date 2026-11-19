package art.arcane.witchery.item;

import art.arcane.witchery.entity.ThrownBrew;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

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

    public static void splashInk(ThrownBrew brew, HitResult hit) {
        Vec3 impact = hit.getLocation();
        double radius = 4;
        AABB bounds = brew.getBoundingBox().move(impact.subtract(brew.position())).inflate(radius, 2, radius);
        for (LivingEntity entity : brew.level().getEntitiesOfClass(LivingEntity.class, bounds)) {
            boolean direct = hit instanceof EntityHitResult entityHit && entityHit.getEntity() == entity;
            double distance = entity.distanceToSqr(impact);
            if (direct || distance < radius * radius) {
                int duration = direct ? 400 : (int) Math.round((1 - Math.sqrt(distance) / radius) * 400);
                if (duration > 0) {
                    entity.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, duration), brew.getEffectSource());
                }
                if (entity instanceof Mob mob) {
                    mob.setTarget(null);
                    mob.getBrain().eraseMemory(MemoryModuleType.ATTACK_TARGET);
                }
            }
        }
    }
}
