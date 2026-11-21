package art.arcane.witchery.brewing;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Witchery.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class MovementBrewEffect extends MobEffect {
    public enum Kind { FLOATING, FEATHER_FALL, SWIMMING }

    private final Kind kind;

    public MovementBrewEffect(Kind kind, int color) {
        super(MobEffectCategory.BENEFICIAL, color);
        this.kind = kind;
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        boolean controlsMovement = entity instanceof Player player ? player.isLocalPlayer() : !entity.level().isClientSide;
        switch (kind) {
            case FLOATING -> {
                entity.resetFallDistance();
                if (controlsMovement && !entity.isPassenger()) floatAboveGround(entity, amplifier);
            }
            case FEATHER_FALL -> {
                if (controlsMovement && entity.fallDistance >= Math.max(3, 5 - amplifier)
                        && entity.getDeltaMovement().y < -0.2) {
                    Vec3 velocity = entity.getDeltaMovement();
                    entity.setDeltaMovement(velocity.x, -0.2, velocity.z);
                    entity.fallDistance = Math.min(entity.fallDistance, fallLimit(amplifier));
                }
            }
            case SWIMMING -> {
                if (!(entity instanceof Player) && controlsMovement && entity.level().getGameTime() % 10 == 3
                        && entity.isInWater() && entity.hasEffect(WitcheryEffects.PARALYSIS.get())) {
                    double boost = 1.15 + 0.03 * amplifier;
                    entity.setDeltaMovement(entity.getDeltaMovement().multiply(boost, 1, boost));
                }
            }
        }
    }

    private static void floatAboveGround(LivingEntity entity, int amplifier) {
        BlockPos feet = entity.blockPosition();
        Vec3 velocity = entity.getDeltaMovement();
        for (int depth = 1; depth <= 3 + amplifier; depth++) {
            if (!entity.level().getBlockState(feet.below(depth)).isAir()) {
                entity.setDeltaMovement(velocity.x, 0.25, velocity.z);
                return;
            }
        }
        entity.setDeltaMovement(velocity.x, entity.level().random.nextInt(5) == 0 ? -0.05 : 0, velocity.z);
    }

    private static int fallLimit(int amplifier) {
        return Math.max(3, 6 - amplifier);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onFall(LivingFallEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.hasEffect(WitcheryEffects.FLOATING.get())) {
            event.setCanceled(true);
            return;
        }
        var featherFall = entity.getEffect(WitcheryEffects.FEATHER_FALL.get());
        if (featherFall != null) event.setDistance(Math.min(event.getDistance(), fallLimit(featherFall.getAmplifier())));
    }
}
