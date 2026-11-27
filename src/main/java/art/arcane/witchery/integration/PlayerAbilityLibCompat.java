package art.arcane.witchery.integration;

import art.arcane.witchery.Witchery;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

public final class PlayerAbilityLibCompat {
    private PlayerAbilityLibCompat() {}

    public static boolean setBatFlight(Player player, boolean active) {
        return ModList.get().isLoaded("playerabilitylib") && FlightAccess.set(player, active);
    }

    private record FlightApi(Object source, Object ability, Method grant, Method revoke) {
        @Nullable
        private static FlightApi bind() {
            try {
                var pal = Class.forName("io.github.ladysnake.pal.Pal");
                var getSource = pal.getMethod("getAbilitySource", String.class, String.class);
                var allowance = Class.forName("io.github.ladysnake.pal.VanillaAbilities").getField("ALLOW_FLYING");
                var sourceType = getSource.getReturnType();
                return new FlightApi(getSource.invoke(null, Witchery.MODID, "vampire_bat"), allowance.get(null),
                        sourceType.getMethod("grantTo", Player.class, allowance.getType()),
                        sourceType.getMethod("revokeFrom", Player.class, allowance.getType()));
            } catch (ReflectiveOperationException | LinkageError exception) {
                Witchery.LOGGER.warn("Could not bind the PlayerAbilityLib flight API", exception);
                return null;
            }
        }
    }

    private static final class FlightAccess {
        private static final FlightApi API = FlightApi.bind();
        private static boolean reportedError;

        private static boolean set(Player player, boolean active) {
            if (API == null) return false;
            try {
                (active ? API.grant() : API.revoke()).invoke(API.source(), player, API.ability());
                return true;
            } catch (ReflectiveOperationException | LinkageError exception) {
                if (!reportedError) {
                    reportedError = true;
                    Witchery.LOGGER.warn("Could not update the PlayerAbilityLib Bat flight grant", exception);
                }
                return false;
            }
        }
    }
}
