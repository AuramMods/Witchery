package art.arcane.witchery.integration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.config.WitcheryCommonConfig;
import art.arcane.witchery.lycanthropy.Lycanthropy;
import art.arcane.witchery.registry.WitcheryEffects;
import art.arcane.witchery.vampirism.VampireFlight;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

import java.lang.reflect.Method;

@Mod.EventBusSubscriber(modid = Witchery.MODID)
public final class ArsMagicaCompat {
    private static final String MOD_ID = "arsmagicalegacy";

    private ArsMagicaCompat() {}

    public static void drain(LivingEntity entity, float fraction) {
        if (enabled()) ManaAccess.drain(entity, fraction);
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onSuffocation(LivingAttackEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !event.getSource().is(DamageTypes.IN_WALL)
                || !enabled()) return;

        var resizing = player.getEffect(WitcheryEffects.RESIZING.get());
        boolean small = resizing != null && resizing.getAmplifier() <= 1;
        if (!small && !Lycanthropy.isWolf(player) && !VampireFlight.isBat(player)) return;

        var pos = player.blockPosition();
        if (!player.level().getBlockState(pos).isSolidRender(player.level(), pos)) event.setCanceled(true);
    }

    private static boolean enabled() {
        return ModList.get().isLoaded(MOD_ID) && WitcheryCommonConfig.ALLOW_MOD_INTEGRATION.get();
    }

    private record ManaApi(Object helper, Method currentMana, Method maxMana, Method setMana) {
        private static ManaApi bind() {
            try {
                var apiType = Class.forName("com.github.minecraftschurlimods.arsmagicalegacy.api.ArsMagicaAPI");
                var getHelper = apiType.getMethod("getManaHelper");
                var helper = getHelper.invoke(apiType.getMethod("get").invoke(null));
                var helperType = getHelper.getReturnType();
                return new ManaApi(helper, helperType.getMethod("getMana", LivingEntity.class),
                        helperType.getMethod("getMaxMana", LivingEntity.class),
                        helperType.getMethod("setMana", LivingEntity.class, float.class));
            } catch (ReflectiveOperationException | LinkageError exception) {
                Witchery.LOGGER.warn("Could not bind the Ars Magica Legacy mana API", exception);
                return null;
            }
        }
    }

    private static final class ManaAccess {
        private static final ManaApi API = ManaApi.bind();
        private static boolean reportedError;

        private static void drain(LivingEntity entity, float fraction) {
            if (API == null) return;
            try {
                float mana = (float) API.currentMana().invoke(API.helper(), entity);
                float maximum = (float) API.maxMana().invoke(API.helper(), entity);
                if (mana > 0 && maximum > 0) {
                    float debit = Math.max(1, maximum * fraction);
                    API.setMana().invoke(API.helper(), entity, Math.max(0, mana - debit));
                }
            } catch (ReflectiveOperationException | LinkageError exception) {
                if (!reportedError) {
                    Witchery.LOGGER.warn("Could not drain Ars Magica Legacy mana", exception);
                    reportedError = true;
                }
            }
        }
    }
}
