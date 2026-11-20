package art.arcane.witchery.vampirism;

import art.arcane.witchery.item.VampireClothingItem;
import art.arcane.witchery.registry.WitcheryDamageTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public final class VampireMortality {
    private VampireMortality() {}

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
                || Vampirism.isVampire(attacker) || attacker instanceof LivingEntity living && Vampirism.isBoss(living);
    }

    private static boolean isFire(DamageSource source) {
        return source.is(DamageTypeTags.IS_FIRE) || source.is(WitcheryDamageTypes.VAMPIRE_FIRE);
    }

}
