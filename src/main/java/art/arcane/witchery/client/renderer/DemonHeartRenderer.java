package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.DemonHeartBlock;
import art.arcane.witchery.block.entity.DemonHeartBlockEntity;
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

public final class DemonHeartRenderer implements BlockEntityRenderer<DemonHeartBlockEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "demonheart"), "main");
    private static final RenderType SKIN = RenderType.entityCutout(
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/block/demonheart.png"));
    private final ModelPart staticParts;
    private final ModelPart pulseParts;

    public DemonHeartRenderer(BlockEntityRendererProvider.Context context) {
        ModelPart root = context.bakeLayer(LAYER);
        staticParts = root.getChild("vessels");
        pulseParts = root.getChild("ventricles");
    }

    public static LayerDefinition layer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition vessels = root.addOrReplaceChild("vessels", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3, 13, 1, 2, 1, 2), PartPose.ZERO);
        vessel(vessels, "tube_one", -3, 14, 1, 0.4089647F, 0.6320364F, 0);
        vessel(vessels, "tube_two", -2, 14, 1, -0.2974289F, -0.2230717F, -0.3346075F);
        vessel(vessels, "tube_three", 1, 13, -0.8F, -0.0743572F, 0.1487144F, -0.2602503F);
        vessel(vessels, "tube_four", 0, 15, 0, 0.2602503F, 0, 0.4089647F);
        vessel(vessels, "tube_five", 0, 14, 1, -0.2602503F, 0, 0);
        PartDefinition ventricles = root.addOrReplaceChild("ventricles", CubeListBuilder.create()
                .texOffs(14, 20).addBox(-3, 14, 0, 5, 8, 4)
                .texOffs(0, 7).addBox(-4, 15, -1, 3, 8, 6)
                .texOffs(13, 0).addBox(-5, 16, 0, 1, 6, 4), PartPose.ZERO);
        ventricles.addOrReplaceChild("large_vessel", CubeListBuilder.create().texOffs(3, 3).addBox(0, 0, 0, 3, 2, 2),
                PartPose.offsetAndRotation(-2, 15, -1, 0, 0.3717861F, 0.2230717F));
        return LayerDefinition.create(mesh, 32, 32);
    }

    private static void vessel(PartDefinition parent, String name, float x, float y, float z, float pitch, float yaw, float roll) {
        parent.addOrReplaceChild(name, CubeListBuilder.create().texOffs(19, 11).addBox(0, -3, 1, 1, 3, 1),
                PartPose.offsetAndRotation(x, y, z, pitch, yaw, roll));
    }

    @Override
    public void render(DemonHeartBlockEntity heart, float partialTick, PoseStack pose, MultiBufferSource buffers,
                       int light, int overlay) {
        pose.pushPose();
        pose.translate(0.5, 0.5, 0.5);
        pose.mulPose(Axis.ZP.rotationDegrees(180));
        pose.scale(0.7F, 0.7F, 0.7F);
        pose.mulPose(Axis.YP.rotationDegrees(heart.getBlockState().getValue(DemonHeartBlock.FACING).toYRot() - 180));
        pose.translate(0.1, -0.9, -0.15);
        VertexConsumer vertices = buffers.getBuffer(SKIN);
        staticParts.render(pose, vertices, light, overlay);
        double phase = heart.pulse() * (2.0 * Math.PI / DemonHeartBlockEntity.PULSE_TICKS);
        float scaled = (float) (0.165 * (7 + 0.25 * Math.sin(phase)));
        pose.translate(0, 1, 0);
        pose.scale(scaled, scaled, scaled);
        pose.translate(0, -1, 0);
        pulseParts.render(pose, vertices, light, overlay);
        pose.popPose();
    }
}
