package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.WitcheryBolt;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class WitcheryBoltRenderer extends EntityRenderer<WitcheryBolt> {
    private static final float SCALE = 0.05625F;
    private static final float TEXTURE_ROW = 5F / 32F;
    private static final ResourceLocation STAKE = texture("bolt");
    private static final ResourceLocation ANTI_MAGIC = texture("bolt2");
    private static final ResourceLocation HOLY = texture("bolt3");
    private static final ResourceLocation SILVER = texture("bolt4");

    public WitcheryBoltRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(WitcheryBolt bolt, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTick, bolt.yRotO, bolt.getYRot()) - 90F));
        pose.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTick, bolt.xRotO, bolt.getXRot())));
        float shake = bolt.shakeTime - partialTick;
        if (shake > 0) pose.mulPose(Axis.ZP.rotationDegrees(-Mth.sin(shake * 3F) * shake));

        pose.mulPose(Axis.XP.rotationDegrees(45F));
        pose.scale(SCALE, SCALE, SCALE);
        pose.translate(-3F, 0F, 0F);
        var vertices = buffers.getBuffer(RenderType.entityCutout(getTextureLocation(bolt)));
        drawTail(vertices, pose.last(), light);

        // The original Bolt moves only the four shaft faces forward from the tail.
        pose.translate(0.9F, 0F, 0F);
        for (int side = 0; side < 4; side++) {
            pose.mulPose(Axis.XP.rotationDegrees(90F));
            var face = pose.last();
            vertex(vertices, face, -8, -2, 0, 0F, 0F, 0, 1, light);
            vertex(vertices, face, 8, -2, 0, 0.3F, 0F, 0, 1, light);
            vertex(vertices, face, 8, 2, 0, 0.3F, TEXTURE_ROW, 0, 1, light);
            vertex(vertices, face, -8, 2, 0, 0F, TEXTURE_ROW, 0, 1, light);
        }
        pose.popPose();
        super.render(bolt, yaw, partialTick, pose, buffers, light);
    }

    private static void drawTail(VertexConsumer vertices, PoseStack.Pose pose, int light) {
        float tailBottom = 2F * TEXTURE_ROW;
        vertex(vertices, pose, -7, -2, -2, 0F, TEXTURE_ROW, 1, 0, light);
        vertex(vertices, pose, -7, -2, 2, TEXTURE_ROW, TEXTURE_ROW, 1, 0, light);
        vertex(vertices, pose, -7, 2, 2, TEXTURE_ROW, tailBottom, 1, 0, light);
        vertex(vertices, pose, -7, 2, -2, 0F, tailBottom, 1, 0, light);

        vertex(vertices, pose, -7, 2, -2, 0F, TEXTURE_ROW, -1, 0, light);
        vertex(vertices, pose, -7, 2, 2, TEXTURE_ROW, TEXTURE_ROW, -1, 0, light);
        vertex(vertices, pose, -7, -2, 2, TEXTURE_ROW, tailBottom, -1, 0, light);
        vertex(vertices, pose, -7, -2, -2, 0F, tailBottom, -1, 0, light);
    }

    private static void vertex(VertexConsumer vertices, PoseStack.Pose pose, int x, int y, int z,
                               float u, float v, int normalX, int normalZ, int light) {
        vertices.vertex(pose.pose(), x, y, z).color(255, 255, 255, 255).uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light)
                .normal(pose.normal(), normalX, 0, normalZ).endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(WitcheryBolt bolt) {
        return switch (bolt.kind()) {
            case STAKE -> STAKE;
            case ANTI_MAGIC, POWERED_ANTI_MAGIC -> ANTI_MAGIC;
            case HOLY -> HOLY;
            case SILVER -> SILVER;
        };
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/entities/" + name + ".png");
    }
}
