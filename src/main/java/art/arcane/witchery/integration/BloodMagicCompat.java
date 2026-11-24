package art.arcane.witchery.integration;

import art.arcane.witchery.config.WitcheryCommonConfig;
import art.arcane.witchery.vampirism.Vampirism;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.fml.ModList;
import wayoftime.bloodmagic.event.SacrificeKnifeUsedEvent;
import wayoftime.bloodmagic.util.helper.NetworkHelper;

public final class BloodMagicCompat {
    private static final String MOD_ID = "bloodmagic";

    private BloodMagicCompat() {}

    public static void register() {
        if (ModList.get().isLoaded(MOD_ID)) Hooks.register();
    }

    public static void drain(ServerPlayer player, float fraction) {
        if (enabled()) Hooks.drain(player, fraction);
    }

    public static void boost(ServerPlayer player, float health) {
        if (enabled()) Hooks.boost(player, health);
    }

    private static boolean enabled() {
        return ModList.get().isLoaded(MOD_ID) && WitcheryCommonConfig.ALLOW_MOD_INTEGRATION.get();
    }

    private static final class Hooks {
        private static final int[] ESSENCE_TIERS = {5_000, 25_000, 150_000, 1_000_000, 10_000_000, Integer.MAX_VALUE};

        static void register() {
            MinecraftForge.EVENT_BUS.addListener(EventPriority.LOWEST, Hooks::onSacrifice);
        }

        static void drain(ServerPlayer player, float fraction) {
            var network = NetworkHelper.getSoulNetwork(player);
            int essence = network.getCurrentEssence();
            if (essence <= 0) return;
            for (int tier : ESSENCE_TIERS) {
                if (essence <= tier) {
                    float reduction = Math.max(1, tier * fraction);
                    network.setCurrentEssence(Math.max(0, (int) (essence - reduction)));
                    return;
                }
            }
        }

        static void boost(ServerPlayer player, float health) {
            var network = NetworkHelper.getSoulNetwork(player);
            long essence = network.getCurrentEssence() + (long) (int) health * 100;
            network.setCurrentEssence((int) Math.min(Integer.MAX_VALUE, essence));
        }

        private static void onSacrifice(SacrificeKnifeUsedEvent event) {
            if (!WitcheryCommonConfig.ALLOW_MOD_INTEGRATION.get()
                    || !(event.player instanceof ServerPlayer player) || Vampirism.level(player) == 0) return;
            event.shouldDrainHealth = false;
            if (event.shouldFillAltar && !Vampirism.drawBlood(player, event.healthDrained * 100)) {
                event.shouldFillAltar = false;
            }
        }
    }
}
