package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.MirrorFace;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class MirrorFaceRenderer extends EntityRenderer<MirrorFace> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Witchery.MODID, "textures/entities/mirrorface.png");
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "mirrorface"), "main");
    private final ModelPart head;

    public MirrorFaceRenderer(EntityRendererProvider.Context context) {
        super(context);
        head = context.bakeLayer(LAYER).getChild("head");
    }

    public static LayerDefinition layer() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-4, -8, -4, 8, 8, 8), PartPose.offset(0, 24, 0));
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void render(MirrorFace face, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
        float bodyYaw = Mth.rotLerp(partialTick, face.yBodyRotO, face.yBodyRot);
        float headYaw = Mth.rotLerp(partialTick, face.yHeadRotO, face.yHeadRot);
        head.yRot = (headYaw - bodyYaw) * Mth.DEG_TO_RAD;
        head.xRot = Mth.lerp(partialTick, face.xRotO, face.getXRot()) * Mth.DEG_TO_RAD;
        pose.pushPose();
        pose.mulPose(Axis.YP.rotationDegrees(180 - bodyYaw));
        pose.scale(-1, -1, 1);
        pose.translate(0, -1.5078125F, 0);
        pose.scale(0.75F, 0.75F, 0.75F);
        pose.translate(0, 0.4F, 0);
        head.render(pose, buffers.getBuffer(RenderType.entityTranslucent(TEXTURE)), light,
                OverlayTexture.NO_OVERLAY, 1, 1, 1, 0.8F);
        pose.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(MirrorFace face) {
        return TEXTURE;
    }
}
