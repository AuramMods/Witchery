package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.DreamCatcherBlock;
import art.arcane.witchery.block.DreamWeave;
import art.arcane.witchery.block.entity.DreamCatcherBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public final class DreamCatcherRenderer implements BlockEntityRenderer<DreamCatcherBlockEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "dreamcatcher"), "main");
    private static final RenderType TEXTURE = RenderType.entityCutoutNoCull(
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/block/dreamcatcher.png"));
    private final ModelPart frame;
    private final ModelPart nets;

    public DreamCatcherRenderer(BlockEntityRendererProvider.Context context) {
        ModelPart root = context.bakeLayer(LAYER);
        frame = root.getChild("frame");
        nets = root.getChild("nets");
    }

    public static LayerDefinition layer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("frame", CubeListBuilder.create()
                .texOffs(0, 2).addBox(-4, 10, 7, 1, 8, 1)
                .texOffs(0, 2).addBox(3, 10, 7, 1, 8, 1)
                .texOffs(0, 0).addBox(-3, 10, 7, 6, 1, 1)
                .texOffs(0, 0).addBox(-3, 17, 7, 6, 1, 1)
                .texOffs(0, 12).addBox(-4, 18, 7, 8, 6, 0), PartPose.ZERO);
        PartDefinition nets = root.addOrReplaceChild("nets", CubeListBuilder.create(), PartPose.ZERO);
        for (DreamWeave weave : DreamWeave.values()) {
            nets.addOrReplaceChild(weave.getSerializedName(),
                    CubeListBuilder.create().texOffs(weave.textureX(), weave.textureY()).addBox(-3, 11, 7.85F, 6, 6, 0),
                    PartPose.ZERO);
        }
        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void render(DreamCatcherBlockEntity catcher, float partialTick, PoseStack pose, MultiBufferSource buffers,
                       int light, int overlay) {
        var state = catcher.getBlockState();
        pose.pushPose();
        pose.translate(0.5, 1.5, 0.5);
        pose.mulPose(Axis.YP.rotationDegrees(180 - state.getValue(DreamCatcherBlock.FACING).toYRot()));
        pose.mulPose(Axis.ZP.rotationDegrees(180));
        VertexConsumer vertices = buffers.getBuffer(TEXTURE);
        frame.render(pose, vertices, light, overlay);
        nets.getChild(state.getValue(DreamCatcherBlock.WEAVE).getSerializedName()).render(pose, vertices, light, overlay);
        pose.popPose();
    }
}
