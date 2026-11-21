package art.arcane.witchery.client;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.brewing.ResizingEffect;
import art.arcane.witchery.vampirism.VampireFlight;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Witchery.MODID, value = Dist.CLIENT)
public final class ClientBatForm {
    private static Bat modelEntity;

    private ClientBatForm() {}

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void renderBat(RenderPlayerEvent.Pre event) {
        var player = event.getEntity();
        if (!VampireFlight.isBat(player)) return;
        if (modelEntity == null || modelEntity.level() != player.level()) modelEntity = EntityType.BAT.create(player.level());
        if (modelEntity == null) return;
        modelEntity.setResting(false);
        ResizingEffect.setScale(modelEntity, ResizingEffect.scale(player));
        modelEntity.setPos(player.position());
        modelEntity.setInvisible(player.isInvisible());
        modelEntity.tickCount = player.tickCount;
        modelEntity.yBodyRotO = player.yBodyRotO;
        modelEntity.yBodyRot = player.yBodyRot;
        modelEntity.yHeadRotO = player.yHeadRotO;
        modelEntity.yHeadRot = player.yHeadRot;
        modelEntity.xRotO = player.xRotO;
        modelEntity.setXRot(player.getXRot());
        event.setCanceled(true);
        Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(modelEntity).render(modelEntity, player.getYRot(),
                event.getPartialTick(), event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight());
    }

    @SubscribeEvent
    public static void hideEmptyHand(RenderHandEvent event) {
        var player = Minecraft.getInstance().player;
        if (player != null && event.getItemStack().isEmpty() && VampireFlight.isBat(player)) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        modelEntity = null;
    }
}
