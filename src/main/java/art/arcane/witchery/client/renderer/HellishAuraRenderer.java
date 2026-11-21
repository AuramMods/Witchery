package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryEffects;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Witchery.MODID, value = Dist.CLIENT)
public final class HellishAuraRenderer {
    private HellishAuraRenderer() {}

    @SubscribeEvent
    public static void render(RenderLivingEvent.Post<?, ?> event) {
        var entity = event.getEntity();
        if (!entity.hasEffect(WitcheryEffects.HELLISH_AURA.get())) return;

        PoseStack pose = event.getPoseStack();
        pose.pushPose();
        float size = entity.getBbWidth() * 1.4F;
        float height = entity.getBbHeight() / size;
        pose.scale(size, size, size);
        pose.mulPose(Axis.YP.rotationDegrees(-Minecraft.getInstance().gameRenderer.getMainCamera().getYRot()));
        pose.translate(0, 0, -0.3F + (int) height * 0.02F);
        var fire0 = ModelBakery.FIRE_0.sprite();
        var fire1 = ModelBakery.FIRE_1.sprite();
        var vertices = event.getMultiBufferSource().getBuffer(Sheets.cutoutBlockSheet());
        var transform = pose.last();
        float halfWidth = 0.5F;
        for (int layer = 0; layer * 0.45F < height; layer++) {
            var sprite = layer % 2 == 0 ? fire0 : fire1;
            boolean flip = layer / 2 % 2 == 0;
            float left = flip ? sprite.getU1() : sprite.getU0();
            float right = flip ? sprite.getU0() : sprite.getU1();
            float bottom = layer * 0.45F;
            float front = layer * 0.03F;
            vertex(transform, vertices, halfWidth, bottom, front, right, sprite.getV1());
            vertex(transform, vertices, -halfWidth, bottom, front, left, sprite.getV1());
            vertex(transform, vertices, -halfWidth, bottom + 1.4F, front, left, sprite.getV0());
            vertex(transform, vertices, halfWidth, bottom + 1.4F, front, right, sprite.getV0());
            halfWidth *= 0.9F;
        }
        pose.popPose();
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer vertices, float x, float y, float z, float u, float v) {
        vertices.vertex(pose.pose(), x, y, z).color(0, 255, 0, 255).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT).normal(pose.normal(), 0, 1, 0).endVertex();
    }
}
