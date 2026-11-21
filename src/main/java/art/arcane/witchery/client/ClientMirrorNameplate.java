package art.arcane.witchery.client;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.config.WitcheryServerConfig;
import art.arcane.witchery.world.MirrorDisguise;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderNameTagEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Witchery.MODID, value = Dist.CLIENT)
public final class ClientMirrorNameplate {
    private ClientMirrorNameplate() {}

    @SubscribeEvent
    public static void renderNameplate(RenderNameTagEvent event) {
        if (!(event.getEntity() instanceof Player player) || !WitcheryServerConfig.ALLOW_NAMEPLATE_MASQUERADING.get()) return;
        String alias = MirrorDisguise.alias(player);
        if (!alias.isEmpty()) event.setContent(PlayerTeam.formatNameForTeam(player.getTeam(), Component.literal(alias)));
    }
}
