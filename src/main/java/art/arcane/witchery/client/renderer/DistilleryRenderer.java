package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.DistilleryBlock;
import art.arcane.witchery.block.entity.DistilleryBlockEntity;
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
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public final class DistilleryRenderer implements BlockEntityRenderer<DistilleryBlockEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "distillery"), "main");
    private static final RenderType TEXTURE = RenderType.entityCutout(
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/block/distillery.png"));

    private final ModelPart still;
    private final ModelPart[] jars = new ModelPart[4];

    public DistilleryRenderer(BlockEntityRendererProvider.Context context) {
        ModelPart root = context.bakeLayer(LAYER);
        still = root.getChild("still");
        for (int jar = 0; jar < jars.length; jar++) {
            jars[jar] = root.getChild("jar_" + jar);
        }
    }

    public static LayerDefinition layer() {
        var mesh = new MeshDefinition();
        var root = mesh.getRoot();
        var still = root.addOrReplaceChild("still", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-5, 18, -2, 10, 6, 10)
                .texOffs(0, 6).addBox(-3, 14, 0, 6, 4, 6)
                .texOffs(25, 9).addBox(-2, 11, 1, 4, 3, 4)
                .texOffs(0, 0).addBox(-1, 9, -1, 2, 2, 4)
                .texOffs(30, 6).addBox(-8, 15, -6, 16, 1, 1)
                .texOffs(47, 24).addBox(-8, 16, -6, 1, 7, 1)
                .texOffs(47, 24).addBox(7, 16, -6, 1, 7, 1)
                .texOffs(22, 0).addBox(-8, 23, -8, 16, 1, 5), PartPose.ZERO);
        still.addOrReplaceChild("tube", CubeListBuilder.create().texOffs(46, 10)
                .addBox(-0.5F, -0.5F, 0, 1, 1, 8), PartPose.offsetAndRotation(0, 10, 0, -2.341978F, 0, 0));
        float[] positions = {-7, -3.3F, 0.4F, 4};
        for (int jar = 0; jar < positions.length; jar++) {
            root.addOrReplaceChild("jar_" + jar, CubeListBuilder.create().mirror()
                    .texOffs(52, 26).addBox(0, 2, 0, 3, 3, 3)
                    .texOffs(60, 24).addBox(1, 1, 1, 1, 1, 1)
                    .texOffs(56, 21).addBox(0.5F, 0, 0.5F, 2, 1, 2),
                    PartPose.offsetAndRotation(positions[jar], 16, -7, jar == 1 ? 0.0174533F : 0, 0, 0));
        }
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void render(DistilleryBlockEntity distillery, float partialTick, PoseStack pose, MultiBufferSource buffers,
                       int light, int overlay) {
        var state = distillery.getBlockState();
        pose.pushPose();
        pose.translate(0.5, 1.5, 0.5);
        pose.mulPose(Axis.ZP.rotationDegrees(180));
        pose.mulPose(Axis.YP.rotationDegrees(state.getValue(DistilleryBlock.FACING).toYRot() - 180));
        var vertices = buffers.getBuffer(TEXTURE);
        still.render(pose, vertices, light, overlay);
        int visibleJars = state.getValue(DistilleryBlock.VISIBLE_JARS);
        for (int jar = 0; jar < visibleJars; jar++) {
            jars[jar].render(pose, vertices, light, overlay);
        }
        pose.popPose();
    }
}
