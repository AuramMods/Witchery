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
                .addBox(-4, -8, -4, 8, 8, 8), PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void render(MirrorFace face, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
        pose.pushPose();
        pose.translate(0, 0.3, 0);
        pose.mulPose(Axis.YP.rotationDegrees(180 - Mth.rotLerp(partialTick, face.yHeadRotO, face.yHeadRot)));
        pose.scale(-0.75F, -0.75F, 0.75F);
        pose.mulPose(Axis.XP.rotationDegrees(Mth.lerp(partialTick, face.xRotO, face.getXRot())));
        head.render(pose, buffers.getBuffer(RenderType.entityTranslucent(TEXTURE)), light,
                OverlayTexture.NO_OVERLAY, 1, 1, 1, 0.8F);
        pose.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(MirrorFace face) {
        return TEXTURE;
    }
}
