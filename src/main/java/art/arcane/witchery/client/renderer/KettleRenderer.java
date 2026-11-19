package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.KettleBlock;
import art.arcane.witchery.block.entity.KettleBlockEntity;
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
import net.minecraft.world.level.Level;

public final class KettleRenderer implements BlockEntityRenderer<KettleBlockEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "kettle"), "main");
    private static final RenderType RENDER_TYPE = RenderType.entityCutout(
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/block/kettle.png"));
    private final ModelPart model;

    public KettleRenderer(BlockEntityRendererProvider.Context context) {
        model = context.bakeLayer(LAYER);
    }

    public static LayerDefinition layer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("basin", CubeListBuilder.create().mirror()
                .texOffs(0, 0).addBox(-5, 18, -5, 9, 6, 1)
                .texOffs(0, 0).addBox(-4, 18, 4, 9, 6, 1)
                .texOffs(13, 0).addBox(-4, 23, -4, 8, 1, 8), PartPose.ZERO);
        root.addOrReplaceChild("wall_left", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(0, 0, 0, 9, 6, 1),
                PartPose.offsetAndRotation(-5, 18, 5, 0, (float) (Math.PI / 2), 0));
        root.addOrReplaceChild("wall_right", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(0, 0, 0, 9, 6, 1),
                PartPose.offsetAndRotation(4, 18, 4, 0, (float) (Math.PI / 2), 0));
        root.addOrReplaceChild("beam", CubeListBuilder.create().texOffs(0, 10).mirror().addBox(-12, 8.05F, -1, 24, 2, 2), PartPose.ZERO);
        chain(root, "chain_left_front", -2.75F, -1.1F);
        chain(root, "chain_left_back", 0.4F, 1.1F);
        chain(root, "chain_right_front", 0.4F, 2.05F);
        chain(root, "chain_right_back", -0.4F, 1.1F);
        bottle(root, "bottle_left", -4);
        bottle(root, "bottle_right", 0);
        return LayerDefinition.create(mesh, 64, 64);
    }

    private static void chain(PartDefinition parent, String name, float yaw, float roll) {
        parent.addOrReplaceChild(name, CubeListBuilder.create().texOffs(0, 15).addBox(0, -0.5F, 0, 11, 1, 0),
                PartPose.offsetAndRotation(0, 9, 0, 0, yaw, roll));
    }

    private static void bottle(PartDefinition parent, String name, int x) {
        parent.addOrReplaceChild(name, CubeListBuilder.create().mirror()
                .texOffs(52, 5).addBox(0, 2, 0, 3, 3, 3)
                .texOffs(60, 3).addBox(1, 1, 1, 1, 1, 1)
                .texOffs(56, 0).addBox(0.5F, 0, 0.5F, 2, 1, 2), PartPose.offset(x, 13, -6));
    }

    @Override
    public void render(KettleBlockEntity kettle, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        Level level = kettle.getLevel();
        if (level == null) {
            return;
        }
        pose.pushPose();
        pose.translate(0.5, 1.5, 0.5);
        pose.mulPose(Axis.ZP.rotationDegrees(180));
        pose.mulPose(Axis.YP.rotationDegrees(kettle.getBlockState().getValue(KettleBlock.FACING).toYRot() - 180));
        model.getChild("beam").visible = !level.getBlockState(kettle.getBlockPos().above()).isSolid();
        model.getChild("bottle_left").visible = kettle.bottleCount() > 0;
        model.getChild("bottle_right").visible = kettle.bottleCount() > 1;
        VertexConsumer vertices = buffers.getBuffer(RENDER_TYPE);
        model.render(pose, vertices, light, overlay);
        if (kettle.isFull()) {
            renderLiquid(pose.last(), vertices, light, overlay, kettle.liquidColor(), level.getGameTime());
        }
        pose.popPose();
    }

    private static void renderLiquid(PoseStack.Pose pose, VertexConsumer vertices, int light, int overlay, int color, long time) {
        float u0 = (time / 5 % 4) * 0.25F;
        float u1 = u0 + 0.125F;
        vertex(pose, vertices, -0.25F, -0.25F, u0, 0.25F, light, overlay, color);
        vertex(pose, vertices, 0.25F, -0.25F, u1, 0.25F, light, overlay, color);
        vertex(pose, vertices, 0.25F, 0.25F, u1, 0.375F, light, overlay, color);
        vertex(pose, vertices, -0.25F, 0.25F, u0, 0.375F, light, overlay, color);
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer vertices, float x, float z, float u, float v,
                               int light, int overlay, int color) {
        vertices.vertex(pose.pose(), x, 1.25F, z).color(color >> 16 & 255, color >> 8 & 255, color & 255, 255)
                .uv(u, v).overlayCoords(overlay).uv2(light).normal(pose.normal(), 0, -1, 0).endVertex();
    }
}
