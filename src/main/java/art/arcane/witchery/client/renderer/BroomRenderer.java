package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.WitchBroom;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public final class BroomRenderer extends EntityRenderer<WitchBroom> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "broom"), "main");
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/entities/broom.png");
    private static final float[][] BRISTLE_COLORS = {
            {1, 1, 1}, {0.85F, 0.5F, 0.2F}, {0.7F, 0.3F, 0.85F}, {0.4F, 0.6F, 0.85F},
            {0.9F, 0.9F, 0.2F}, {0.5F, 0.8F, 0.1F}, {0.95F, 0.5F, 0.65F}, {0.3F, 0.3F, 0.3F},
            {0.6F, 0.6F, 0.6F}, {0.3F, 0.5F, 0.6F}, {0.5F, 0.25F, 0.7F}, {0.2F, 0.3F, 0.7F},
            {0.4F, 0.3F, 0.2F}, {0.4F, 0.5F, 0.2F}, {0.6F, 0.2F, 0.2F}, {0.1F, 0.1F, 0.1F}
    };
    private final ModelPart handle;
    private final ModelPart bristles;

    public BroomRenderer(EntityRendererProvider.Context context) {
        super(context);
        ModelPart root = context.bakeLayer(LAYER);
        handle = root.getChild("handle");
        bristles = root.getChild("bristles");
        shadowRadius = 0.5F;
    }

    public static LayerDefinition layer() {
        var mesh = new MeshDefinition();
        var root = mesh.getRoot();
        root.addOrReplaceChild("handle", CubeListBuilder.create().texOffs(24, 0).mirror()
                .addBox(-1, -10, -1, 2, 24, 2), PartPose.offsetAndRotation(0, 11, -5, (float) (Math.PI / 2), 0, 0));
        var brush = root.addOrReplaceChild("bristles", CubeListBuilder.create(), PartPose.ZERO);
        bristle(brush, "upper_left", -1, 10, 0.1858931F, -0.1487144F, 10);
        bristle(brush, "lower_right", 1, 12, -0.1487144F, 0.1858931F, 10);
        bristle(brush, "upper_right", 1, 10, 0.2230717F, 0.1858931F, 9);
        bristle(brush, "upper_center", 0, 10, 0.2230717F, 0.0743572F, 10);
        bristle(brush, "lower_left", -1, 12, -0.2230717F, -0.1487144F, 10);
        bristle(brush, "center", 0, 11, -0.0371786F, 0.0743572F, 10);
        bristle(brush, "right", 1, 11, -0.0371786F, 0.2230717F, 10);
        bristle(brush, "left", -1, 11, -0.0743572F, -0.1487144F, 9);
        bristle(brush, "lower_center", 0, 12, -0.1858931F, 0, 9);
        return LayerDefinition.create(mesh, 32, 32);
    }

    private static void bristle(PartDefinition brush, String name, float x, float y, float pitch, float yaw, int length) {
        brush.addOrReplaceChild(name, CubeListBuilder.create().texOffs(0, length == 10 ? 0 : 12).mirror()
                .addBox(-0.5F, -0.5F, 0, 1, 1, length), PartPose.offsetAndRotation(x, y, 9, pitch, yaw, 0));
    }

    @Override
    public ResourceLocation getTextureLocation(WitchBroom broom) {
        return TEXTURE;
    }

    @Override
    public void render(WitchBroom broom, float yaw, float partialTick, PoseStack poses, MultiBufferSource buffers, int light) {
        poses.pushPose();
        poses.translate(0, 1, 0);
        poses.mulPose(Axis.YP.rotationDegrees(180 - yaw));
        poses.mulPose(Axis.XP.rotationDegrees(broom.wobble(partialTick)));
        poses.scale(-1, -1, 1);
        var consumer = buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        handle.render(poses, consumer, light, OverlayTexture.NO_OVERLAY);
        float[] color = BRISTLE_COLORS[broom.brushColor() < 0 ? 12 : broom.brushColor()];
        bristles.render(poses, consumer, light, OverlayTexture.NO_OVERLAY, color[0], color[1], color[2], 1);
        poses.popPose();
        super.render(broom, yaw, partialTick, poses, buffers, light);
    }
}
