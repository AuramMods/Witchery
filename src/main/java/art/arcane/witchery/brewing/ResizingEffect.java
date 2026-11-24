package art.arcane.witchery.brewing;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.integration.WoodwalkersCompat;
import art.arcane.witchery.network.WitcheryNetwork;
import art.arcane.witchery.registry.WitcheryEffects;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingJumpEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerSleepInBedEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = Witchery.MODID)
public final class ResizingEffect extends TrackedBrewEffect {
    private static final UUID STEP_HEIGHT = UUID.fromString("90b65aed-63bb-43d1-bdb5-ac66ac2ec331");

    public ResizingEffect(int color) {
        super(MobEffectCategory.BENEFICIAL, color);
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributes, int amplifier) {
        super.removeAttributeModifiers(entity, attributes, amplifier);
        if (!entity.hasEffect(this)) {
            updateStepHeight(entity, null, 1);
            setScale(entity, 1);
        }
    }

    public interface Scaled {
        float witchery$scale();
        void witchery$setScale(float scale);
        @Nullable LivingEntity witchery$replaceResizedShape(@Nullable LivingEntity shape);
        boolean witchery$hasShapeScale();
        void witchery$setHasShapeScale(boolean hasShapeScale);
    }

    public static float scale(LivingEntity entity) {
        float scale = ((Scaled) entity).witchery$scale();
        return scale > 0 ? scale : 1;
    }

    public static void setScale(LivingEntity entity, float scale) {
        if (scale < 0.25F || scale > 3 || !Float.isFinite(scale)) return;
        boolean changed = scale != scale(entity);
        if (!changed && scale == 1) return;
        Scaled state = (Scaled) entity;
        if (changed) state.witchery$setScale(scale);
        if (entity instanceof Player player && syncShapeScale(player, state, scale)) changed = true;
        if (changed) entity.refreshDimensions();
    }

    private static boolean syncShapeScale(Player player, Scaled state, float scale) {
        // Woodwalkers renders its live shape and reads its eye height. It also rebuilds the
        // player's bounding box directly on a shape change, so reapply the effect to both.
        LivingEntity shape = scale == 1 ? null : WoodwalkersCompat.activeShape(player);
        LivingEntity old = state.witchery$replaceResizedShape(shape);
        if (old != null && old != shape) setShapeScale(old, 1);
        if (shape != null) setShapeScale(shape, scale);
        return old != shape;
    }

    private static void setShapeScale(LivingEntity shape, float scale) {
        ((Scaled) shape).witchery$setHasShapeScale(scale != 1);
        setScale(shape, scale);
    }

    public static void tick(LivingEntity entity) {
        MobEffectInstance active = entity.getEffect(WitcheryEffects.RESIZING.get());
        float current = scale(entity);
        if (active == null && current == 1) return;

        float aim = active == null ? 1 : scaleFactor(active.getAmplifier());
        updateStepHeight(entity, active, aim);
        // Refresh the hitbox and eye height together. Use the same growth rate on the server and clients.
        float increment = 0.03F * current / Math.max(entity.getBbHeight(), 0.01F);
        float next = active == null ? 1 : Mth.approach(current, aim, increment);
        setScale(entity, next);
    }

    @SuppressWarnings("deprecation")
    private static void updateStepHeight(LivingEntity entity, @Nullable MobEffectInstance active, float aim) {
        var attribute = entity.getAttribute(ForgeMod.STEP_HEIGHT_ADDITION.get());
        if (attribute == null) return;
        if (active == null) {
            attribute.removeModifier(STEP_HEIGHT);
        } else {
            float step = aim < 1 ? 0 : aim - 1;
            double amount = step - entity.maxUpStep();
            var previous = attribute.getModifier(STEP_HEIGHT);
            if (previous == null || previous.getAmount() != amount) {
                attribute.removeModifier(STEP_HEIGHT);
                attribute.addTransientModifier(new AttributeModifier(STEP_HEIGHT, "Witchery resizing",
                        amount, AttributeModifier.Operation.ADDITION));
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    @SuppressWarnings("removal")
    public static void adjustDimensions(EntityEvent.Size event) {
        if (!(event.getEntity() instanceof LivingEntity entity)) return;
        float scale = scale(entity);
        if (scale == 1) return;
        EntityDimensions size = event.getNewSize();
        event.setNewSize(new EntityDimensions(size.width * scale, size.height * scale, size.fixed));
        event.setNewEyeHeight(event.getNewEyeHeight() * scale);
    }

    @SubscribeEvent
    public static void onJump(LivingJumpEvent event) {
        LivingEntity entity = event.getEntity();
        var active = entity.getEffect(WitcheryEffects.RESIZING.get());
        if (active == null) return;
        float scale = scaleFactor(active.getAmplifier());
        var motion = entity.getDeltaMovement();
        entity.setDeltaMovement(motion.x, motion.y * (scale > 1 ? (scale + 1) * 0.5 : 0.75), motion.z);
    }

    @SubscribeEvent
    public static void onHurt(LivingHurtEvent event) {
        LivingEntity target = event.getEntity();
        if (!(target.level() instanceof ServerLevel)) return;
        var source = event.getSource();
        var defender = target.getEffect(WitcheryEffects.RESIZING.get());
        if (source.is(DamageTypes.FALL) && defender != null && scaleFactor(defender.getAmplifier()) > event.getAmount()) {
            event.setCanceled(true);
        } else if (CombatBrewEffects.isMelee(source) && source.getEntity() instanceof LivingEntity attacker) {
            var offense = attacker.getEffect(WitcheryEffects.RESIZING.get());
            if (offense != null || defender != null) {
                // The original damage ranks use integer division before applying the damage bounds.
                event.setAmount(event.getAmount() * Mth.clamp(damageRank(offense) / damageRank(defender), 0.5F, 3));
            }
        }
    }

    @SubscribeEvent
    public static void onSleep(PlayerSleepInBedEvent event) {
        if (event.getResultStatus() == null && event.getEntity() instanceof ServerPlayer player
                && player.hasEffect(WitcheryEffects.RESIZING.get())) {
            player.displayClientMessage(Component.translatable("witchery.nosleep.resized"), true);
            event.setResult(Player.BedSleepingProblem.OTHER_PROBLEM);
        }
    }

    @SubscribeEvent
    public static void onStartTracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer viewer && event.getTarget() instanceof LivingEntity target) {
            syncScale(viewer, target);
        }
    }

    @SubscribeEvent
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) syncScale(player, player);
    }

    @SubscribeEvent
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) syncScale(player, player);
    }

    private static void syncScale(ServerPlayer viewer, LivingEntity target) {
        float current = scale(target);
        if (current != 1) WitcheryNetwork.sendSyncEntitySize(viewer, target.getId(), current);
    }

    private static float scaleFactor(int amplifier) {
        return switch (amplifier) {
            case 1 -> 0.4F;
            case 2 -> 2;
            case 3 -> 3;
            default -> 0.25F;
        };
    }

    private static int damageRank(@Nullable MobEffectInstance effect) {
        if (effect == null) return 3;
        return switch (effect.getAmplifier()) {
            case 0 -> 1;
            case 1 -> 2;
            case 2 -> 4;
            case 3 -> 5;
            default -> 3;
        };
    }
}
