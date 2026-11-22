package art.arcane.witchery.vampirism;

import art.arcane.witchery.integration.CuriosCompat;
import art.arcane.witchery.config.WitcheryServerConfig;
import art.arcane.witchery.item.VampireClothingItem;
import art.arcane.witchery.item.WitcheryIngredient;
import art.arcane.witchery.lycanthropy.Lycanthropy;
import art.arcane.witchery.registry.WitcheryDamageTypes;
import art.arcane.witchery.registry.WitcherySounds;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public final class VampireMortality {
    private VampireMortality() {}

    public static boolean stake(ServerPlayer attacker, ServerPlayer victim, ItemStack stack) {
        if (!WitcheryIngredient.WOODEN_STAKE.matches(stack) || !WitcheryServerConfig.ALLOW_STAKING_VAMPIRES.get()
                || !victim.isSleeping() || !Vampirism.isVampire(victim)) return false;

        var level = victim.serverLevel();
        level.sendParticles(DustParticleOptions.REDSTONE, victim.getX(), victim.getY(), victim.getZ(),
                16, victim.getBbWidth() / 2, victim.getBbHeight() / 2, victim.getBbWidth() / 2, 0.05);
        level.playSound(null, victim.blockPosition(), WitcherySounds.BLOOD_DRINK.get(), SoundSource.PLAYERS, 1, 1);
        if (!victim.getAbilities().instabuild) {
            WitcheryDamageTypes.killDirectly(victim, attacker, WitcheryDamageTypes.sunlight(level));
        }
        if (!attacker.getAbilities().instabuild) stack.shrink(1);
        return true;
    }

    public static boolean preventLethalDamage(LivingEntity victim, DamageSource source, float damage) {
        // Roll flame protection only at death, after totems and poppets have had their chance.
        return damage > 0 && damage >= victim.getHealth() && !isFire(source) && preventDeath(victim, source);
    }

    public static boolean preventDeath(LivingEntity victim, DamageSource source) {
        if (victim.level().isClientSide || !Vampirism.isVampire(victim) || canKill(victim, source)) return false;

        victim.setHealth(1);
        if (victim instanceof ServerPlayer player) player.causeFoodExhaustion(5);
        if (source.is(DamageTypeTags.IS_EXPLOSION) && victim.getRandom().nextInt(4) == 0) {
            victim.setSecondsOnFire(2);
        }
        return true;
    }

    private static boolean canKill(LivingEntity victim, DamageSource source) {
        if (isFire(source)) {
            int pieces = VampireClothingItem.lightPieces(victim);
            if (pieces < 2) return true;
            boolean oneInFour = victim.getRandom().nextInt(4) == 0;
            return pieces >= 4 ? oneInFour : !oneInFour;
        }

        Entity attacker = source.getEntity();
        return source.is(WitcheryDamageTypes.SUNLIGHT) || source.is(DamageTypes.IN_WALL)
                || source.is(DamageTypes.FELL_OUT_OF_WORLD) || source.is(DamageTypes.GENERIC_KILL)
                || victim instanceof ServerPlayer player && CuriosCompat.disablesVampireProtection(player)
                || Lycanthropy.isWerewolf(victim) && Lycanthropy.isSilver(source) || Vampirism.isVampire(attacker)
                || attacker instanceof LivingEntity living && (Vampirism.isBoss(living) || Lycanthropy.isShifted(living));
    }

    private static boolean isFire(DamageSource source) {
        return source.is(DamageTypeTags.IS_FIRE) || source.is(WitcheryDamageTypes.VAMPIRE_FIRE);
    }

}
