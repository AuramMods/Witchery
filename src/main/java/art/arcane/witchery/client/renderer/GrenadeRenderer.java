package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.GrenadeProjectile;
import art.arcane.witchery.item.GrenadeItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public final class GrenadeRenderer extends EntityRenderer<GrenadeProjectile> {
    private static final ResourceLocation QUARTZ = ResourceLocation.fromNamespaceAndPath(
            Witchery.MODID, "textures/item/ingredient.quartzsphere.png");
    private static final ResourceLocation SUN = ResourceLocation.fromNamespaceAndPath(
            Witchery.MODID, "textures/item/sungrenade.png");
    private static final ResourceLocation DUPLICATE = ResourceLocation.fromNamespaceAndPath(
            Witchery.MODID, "textures/item/dupgrenade.png");

    public GrenadeRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(GrenadeProjectile grenade, float yaw, float tick, PoseStack pose,
                       MultiBufferSource buffers, int light) {
        pose.pushPose();
        pose.mulPose(entityRenderDispatcher.cameraOrientation());
        pose.mulPose(Axis.YP.rotationDegrees(180));
        pose.scale(0.3F, 0.3F, 0.3F);
        drawSprite(buffers.getBuffer(RenderType.entityCutoutNoCull(QUARTZ)), pose.last(), light);
        drawSprite(buffers.getBuffer(RenderType.entityCutoutNoCullZOffset(getTextureLocation(grenade))), pose.last(), light);
        pose.popPose();
        super.render(grenade, yaw, tick, pose, buffers, light);
    }

    private static void drawSprite(VertexConsumer vertices, PoseStack.Pose pose, int light) {
        vertex(vertices, pose, -0.5F, -0.25F, 0, 1, light);
        vertex(vertices, pose, 0.5F, -0.25F, 1, 1, light);
        vertex(vertices, pose, 0.5F, 0.75F, 1, 0, light);
        vertex(vertices, pose, -0.5F, 0.75F, 0, 0, light);
    }

    private static void vertex(VertexConsumer vertices, PoseStack.Pose pose, float x, float y,
                               float u, float v, int light) {
        vertices.vertex(pose.pose(), x, y, 0).color(255, 255, 255, 255).uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(pose.normal(), 0, 1, 0).endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(GrenadeProjectile grenade) {
        return GrenadeItem.isDuplicate(grenade.getItem()) ? DUPLICATE : SUN;
    }
}
