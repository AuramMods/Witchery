package art.arcane.witchery.brewing;

import art.arcane.witchery.network.WitcheryNetwork;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public final class HellishAuraEffect extends TrackedBrewEffect {
    public HellishAuraEffect(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        int interval = 25 >> Math.min(amplifier, 5);
        return interval == 0 || duration % interval == 0;
    }

    @Override
    public void applyEffectTick(LivingEntity host, int amplifier) {
        if (!(host.level() instanceof ServerLevel level)) return;
        var fire = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.ON_FIRE);
        var damage = new DamageSource(fire, host);
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, host.getBoundingBox().inflate(1.5, 0, 1.5),
                entity -> entity != host && entity.isAlive())) {
            target.hurt(damage, 1);
            WitcheryNetwork.sendHellishAuraPulse(level, target);
            if (amplifier > 0) target.setSecondsOnFire(amplifier);
        }
    }
}
