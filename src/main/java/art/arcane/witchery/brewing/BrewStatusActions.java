package art.arcane.witchery.brewing;

import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.registry.WitcheryBlocks;
import art.arcane.witchery.registry.WitcheryEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.List;

public final class BrewStatusActions {
    private BrewStatusActions() {}

    public static void drinkPurifiedMilk(LivingEntity drinker) {
        var effects = List.copyOf(drinker.getActiveEffects());
        if (effects.isEmpty() || drinker.getRandom().nextInt(2) != 0) return;
        MobEffectInstance chosen = effects.get(drinker.getRandom().nextInt(effects.size()));
        if (!retainedByPlayer(drinker, chosen)) drinker.removeEffect(chosen.getEffect());
    }

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
        var effects = target.getActiveEffects().stream().filter(BrewStatusActions::harmful).toList();
        var worsened = new LinkedHashMap<MobEffect, MobEffectInstance>();
        // Roll for the original set of debuffs before removals notify Forge listeners.
        for (MobEffectInstance effect : effects) {
            int amplifier = effect.getAmplifier();
            double chance = amplifier < strength ? 0.01 : amplifier == strength ? 0.25 : 0.75;
            if (level.random.nextDouble() < chance) {
                worsened.put(effect.getEffect(), new MobEffectInstance(effect.getEffect(), effect.getDuration(), amplifier + 1));
            }
        }
        for (MobEffectInstance effect : effects) {
            if (!target.removeEffect(effect.getEffect())) worsened.remove(effect.getEffect());
        }
        worsened.values().forEach(target::addEffect);
        WitcheryNetwork.sendTaglockPurificationCue(level, target.position(), !worsened.isEmpty());
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

    static void boostDuration(LivingEntity target, int strength, double scale) {
        if (target.hasEffect(WitcheryEffects.QUEASY.get())) {
            boolean blind = target.level().getRandom().nextInt(3) == 0;
            target.addEffect(new MobEffectInstance(blind ? MobEffects.BLINDNESS : MobEffects.CONFUSION,
                    blind ? 2400 : 6000, 0, true, true));
            return;
        }
        var effects = List.copyOf(target.getActiveEffects());
        if (effects.isEmpty()) return;
        int boost = Mth.ceil(3600 * scale);
        for (MobEffectInstance effect : effects) {
            if (effect.getEffect().isInstantenous()) {
                target.removeEffect(effect.getEffect());
                continue;
            }
            int duration = effect.mapDuration(remaining -> remaining + Math.min(Math.min(remaining, boost), Integer.MAX_VALUE - remaining));
            if (duration == effect.getDuration()) continue;
            // Updating the active tier keeps its Forge cures and any weaker effect queued behind it.
            target.addEffect(new MobEffectInstance(effect.getEffect(), duration, effect.getAmplifier(),
                    effect.isAmbient(), effect.isVisible(), effect.showIcon()));
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
                boolean canTake = existing == null || existing.getAmplifier() <= effect.getAmplifier()
                        && (effect.isInfiniteDuration()
                        || !existing.isInfiniteDuration() && existing.getDuration() <= effect.getDuration());
                // If the effect removal is vetoed, leave the donor and recipient unchanged.
                if (canTake && donor.removeEffect(effect.getEffect())) {
                    target.addEffect(new MobEffectInstance(effect));
                    --remaining;
                }
                if (remaining <= 0) return;
            }
            // The original still tries the first donor at the zero-exposure boundary.
            if (remaining <= 0) return;
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

    private static boolean retainedByPlayer(LivingEntity target, MobEffectInstance effect) {
        // The original player tick restored incurable effects with more than five ticks remaining.
        return target instanceof Player && (effect.isInfiniteDuration() || effect.getDuration() > 5)
                && effect.getEffect().getCurativeItems().isEmpty();
    }

    private static boolean harmful(MobEffectInstance effect) {
        return effect.getEffect().getCategory() == MobEffectCategory.HARMFUL;
    }
}
