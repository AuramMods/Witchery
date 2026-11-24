package art.arcane.witchery.integration;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.config.WitcheryCommonConfig;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

public final class WoodwalkersCompat {
    private WoodwalkersCompat() {}

    @Nullable
    public static LivingEntity activeShape(Player player) {
        if (!ModList.get().isLoaded("walkers") || !WitcheryCommonConfig.ALLOW_MOD_INTEGRATION.get()) return null;
        return ShapeAccess.current(player);
    }

    private static final class ShapeAccess {
        private static final Method GET_CURRENT_SHAPE = bind();
        private static boolean reportedError;

        @Nullable
        private static Method bind() {
            try {
                return Class.forName("tocraft.walkers.api.PlayerShape").getMethod("getCurrentShape", Player.class);
            } catch (ReflectiveOperationException | LinkageError exception) {
                Witchery.LOGGER.warn("Could not bind the Woodwalkers shape API", exception);
                return null;
            }
        }

        @Nullable
        private static LivingEntity current(Player player) {
            if (GET_CURRENT_SHAPE == null) return null;
            try {
                return GET_CURRENT_SHAPE.invoke(null, player) instanceof LivingEntity shape && !(shape instanceof Player)
                        ? shape : null;
            } catch (ReflectiveOperationException | LinkageError exception) {
                if (!reportedError) {
                    reportedError = true;
                    Witchery.LOGGER.warn("Could not read the active Woodwalkers shape", exception);
                }
                return null;
            }
        }
    }
}
