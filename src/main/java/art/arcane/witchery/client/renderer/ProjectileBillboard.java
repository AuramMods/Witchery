package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

final class ProjectileBillboard {
    static final ResourceLocation QUARTZ_SPHERE = ResourceLocation.fromNamespaceAndPath(
            Witchery.MODID, "textures/item/ingredient.quartzsphere.png");

    private ProjectileBillboard() {}

    static void draw(VertexConsumer vertices, PoseStack.Pose pose, int tint, int light) {
        vertex(vertices, pose, -0.5F, -0.25F, 0, 1, tint, light);
        vertex(vertices, pose, 0.5F, -0.25F, 1, 1, tint, light);
        vertex(vertices, pose, 0.5F, 0.75F, 1, 0, tint, light);
        vertex(vertices, pose, -0.5F, 0.75F, 0, 0, tint, light);
    }

    private static void vertex(VertexConsumer vertices, PoseStack.Pose pose, float x, float y,
                               float u, float v, int tint, int light) {
        vertices.vertex(pose.pose(), x, y, 0).color(tint >> 16 & 255, tint >> 8 & 255, tint & 255, 255).uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY).uv2(light).normal(pose.normal(), 0, 1, 0).endVertex();
    }
}
