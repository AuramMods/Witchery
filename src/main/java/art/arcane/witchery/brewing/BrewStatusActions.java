package art.arcane.witchery.brewing;

import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

import java.util.List;

final class BrewStatusActions {
    private BrewStatusActions() {}

    static void cleanse(LivingEntity target, @Nullable LivingEntity caster, int strength) {
        boolean diseased = target.hasEffect(WitcheryEffects.DISEASED.get());
        for (MobEffectInstance effect : List.copyOf(target.getActiveEffects())) {
            if (eligible(effect, true, strength)) target.removeEffect(effect.getEffect());
        }
        if (!diseased || strength < 2) return;
        target.removeEffect(WitcheryEffects.DISEASED.get());
        if (strength < 3 || !(target.level() instanceof ServerLevel level)) return;
        BlockPos center = target.blockPosition();
        var disease = WitcheryBlocks.getRequired("disease").get();
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-9, -9, -9), center.offset(9, 9, 9))) {
            if (!level.isInWorldBounds(pos) || !level.hasChunkAt(pos)) continue;
            if (level.getBlockState(pos).is(disease)) {
                BrewTerrain.setBlock(caster, level, pos, Blocks.AIR.defaultBlockState(), Direction.UP);
            }
        }
    }

    static void ritualCleanse(LivingEntity target, int strength) {
        if (!(target.level() instanceof ServerLevel level)) return;
        boolean worsened = false;
        for (MobEffectInstance effect : List.copyOf(target.getActiveEffects())) {
            if (!harmful(effect) || !target.removeEffect(effect.getEffect())) continue;
            int amplifier = effect.getAmplifier();
            double chance = amplifier < strength ? 0.01 : amplifier == strength ? 0.25 : 0.75;
            if (level.random.nextDouble() < chance) {
                target.addEffect(new MobEffectInstance(effect.getEffect(), effect.getDuration(), amplifier + 1));
                worsened = true;
            }
        }
        level.sendParticles(worsened ? ParticleTypes.ENTITY_EFFECT : ParticleTypes.EFFECT,
                target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(), 16, 0.5, 0.6, 0.5, 0);
        level.playSound(null, target.blockPosition(), worsened ? SoundEvents.ENDER_DRAGON_GROWL
                : SoundEvents.PLAYER_LEVELUP, SoundSource.NEUTRAL, 1, 1);
    }

    static void removeBuffs(LivingEntity target, int strength) {
        for (MobEffectInstance effect : List.copyOf(target.getActiveEffects())) {
            if (eligible(effect, false, strength)) target.removeEffect(effect.getEffect());
        }
    }

    static void wasting(LivingEntity target, @Nullable Entity source, int strength, double duration, boolean inverted) {
        if (inverted) {
            if (target instanceof Player player) {
                int food = 6 + 2 * strength;
                player.getFoodData().eat(food, food);
            } else {
                target.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 600, strength), source);
            }
            return;
        }
        int hungerTicks = Mth.ceil(400 * duration);
        int amplifier = 1 + strength / 2;
        if (target instanceof Player player) {
            int loss = 10 - strength;
            if (player.getFoodData().getFoodLevel() > loss) player.getFoodData().eat(-loss, 2.0F);
            target.addEffect(new MobEffectInstance(MobEffects.HUNGER, hungerTicks, amplifier), source);
        } else {
            target.addEffect(new MobEffectInstance(MobEffects.WITHER, hungerTicks, amplifier), source);
        }
        target.addEffect(new MobEffectInstance(MobEffects.POISON, Math.max(Mth.ceil(60 * duration), 40)), source);
    }

    static void extendBuffs(LivingEntity target, int strength, double scale) {
        if (target.hasEffect(WitcheryEffects.QUEASY.get())) {
            boolean blind = target.getRandom().nextInt(3) == 0;
            target.addEffect(new MobEffectInstance(blind ? MobEffects.BLINDNESS : MobEffects.CONFUSION,
                    blind ? 2400 : 6000, 0, true, true));
            return;
        }
        var effects = List.copyOf(target.getActiveEffects());
        if (effects.isEmpty()) return;
        int boost = Mth.ceil(3600 * scale);
        target.removeAllEffects();
        for (MobEffectInstance effect : effects) {
            if (harmful(effect)) continue;
            int duration = effect.getDuration();
            if (!effect.isInfiniteDuration()) {
                duration += Math.min(Math.min(duration, boost), Integer.MAX_VALUE - duration);
            }
            target.addEffect(new MobEffectInstance(effect.getEffect(), duration, effect.getAmplifier(), effect.isAmbient(), true));
        }
        target.addEffect(new MobEffectInstance(WitcheryEffects.QUEASY.get(),
                3600 * Math.max(1, 4 - strength), 0, true, true));
    }

    static void stealBuffs(LivingEntity target, int strength, double scale) {
        int remaining = Mth.ceil(2 * (strength + 1) * scale);
        for (LivingEntity donor : neighbors(target, (strength + 1) * 8)) {
            for (MobEffectInstance effect : List.copyOf(donor.getActiveEffects())) {
                if (!eligible(effect, false, strength)) continue;
                MobEffectInstance existing = target.getEffect(effect.getEffect());
                if (existing != null && (existing.getAmplifier() > effect.getAmplifier()
                        || existing.isInfiniteDuration() && !effect.isInfiniteDuration()
                        || !effect.isInfiniteDuration() && existing.getDuration() > effect.getDuration())) continue;
                // If the effect removal is vetoed, leave the donor and recipient unchanged.
                if (!donor.removeEffect(effect.getEffect())) continue;
                target.addEffect(new MobEffectInstance(effect));
                if (--remaining <= 0) return;
            }
        }
    }

    static void spreadDebuffs(LivingEntity target, int strength, double scale) {
        int remaining = Mth.ceil((strength + 1) * scale);
        int amplifierLimit = Math.max(strength - 1, 1);
        var recipients = neighbors(target, (strength + 1) * 4);
        for (MobEffectInstance effect : List.copyOf(target.getActiveEffects())) {
            if (!eligible(effect, true, amplifierLimit) || !target.removeEffect(effect.getEffect())) continue;
            for (LivingEntity recipient : recipients) recipient.addEffect(new MobEffectInstance(effect));
            if (--remaining <= 0) return;
        }
    }

    private static List<LivingEntity> neighbors(LivingEntity target, int radius) {
        return target.level().getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(radius),
                neighbor -> neighbor != target && neighbor.isAlive() && neighbor.distanceToSqr(target) < radius * radius);
    }

    private static boolean eligible(MobEffectInstance effect, boolean debuff, int strength) {
        return harmful(effect) == debuff && effect.getAmplifier() <= strength && !effect.getCurativeItems().isEmpty();
    }

    private static boolean harmful(MobEffectInstance effect) {
        return effect.getEffect().getCategory() == MobEffectCategory.HARMFUL;
    }
}
