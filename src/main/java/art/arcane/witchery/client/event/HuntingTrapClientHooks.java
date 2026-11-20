package art.arcane.witchery.client.event;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.entity.HuntingTrapBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHighlightEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Witchery.MODID, value = Dist.CLIENT)
public final class HuntingTrapClientHooks {
    private HuntingTrapClientHooks() {}

    @SubscribeEvent
    public static void hideTrapOutline(RenderHighlightEvent.Block event) {
        var client = Minecraft.getInstance();
        if (client.level != null && client.level.getBlockEntity(event.getTarget().getBlockPos()) instanceof HuntingTrapBlockEntity trap
                && !trap.visibleTo(client.player)) event.setCanceled(true);
    }
}
