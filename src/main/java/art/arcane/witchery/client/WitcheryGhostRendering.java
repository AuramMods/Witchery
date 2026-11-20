package art.arcane.witchery.client;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.capability.WitcheryPlayerData;
import art.arcane.witchery.capability.WitcheryPlayerDataProvider;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.model.pipeline.VertexConsumerWrapper;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Witchery.MODID, value = Dist.CLIENT)
public final class WitcheryGhostRendering {
    private static final float OPACITY = 0.51F;
    private static boolean renderingGhost;

    private WitcheryGhostRendering() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        if (renderingGhost || !(event.getEntity() instanceof AbstractClientPlayer player)
                || !WitcheryPlayerDataProvider.get(player).map(WitcheryPlayerData::isGhost).orElse(false)) {
            return;
        }
        ResourceLocation skin = event.getRenderer().getTextureLocation(player);
        RenderType model = event.getRenderer().getModel().renderType(skin);
        MultiBufferSource source = event.getMultiBufferSource();
        MultiBufferSource translucentSkin = type -> type == model
                ? ghostSkin(source.getBuffer(RenderType.entityTranslucentCull(skin)))
                : source.getBuffer(type);
        event.setCanceled(true);
        renderingGhost = true;
        try {
            float yaw = Mth.rotLerp(event.getPartialTick(), player.yRotO, player.getYRot());
            event.getRenderer().render(player, yaw, event.getPartialTick(), event.getPoseStack(), translucentSkin, event.getPackedLight());
        } finally {
            renderingGhost = false;
        }
    }

    private static VertexConsumer ghostSkin(VertexConsumer source) {
        return new VertexConsumerWrapper(source) {
            @Override
            public VertexConsumer color(int red, int green, int blue, int alpha) {
                return super.color(red, green, blue, Math.round(alpha * OPACITY));
            }
        };
    }
}
