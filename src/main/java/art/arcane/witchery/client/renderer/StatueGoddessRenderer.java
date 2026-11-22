package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.StatueGoddessBlock;
import art.arcane.witchery.block.entity.PlacedOwnerBlockEntity;
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

public final class StatueGoddessRenderer implements BlockEntityRenderer<PlacedOwnerBlockEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "statuegoddess"), "main");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Witchery.MODID, "textures/block/statuegoddess.png");
    private final ModelPart model;

    public StatueGoddessRenderer(BlockEntityRendererProvider.Context context) {
        model = context.bakeLayer(LAYER);
    }

    public static LayerDefinition layer() {
        MeshDefinition mesh = new MeshDefinition();
        var root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).mirror()
                .addBox(-4, -8, -4, 8, 8, 8), PartPose.ZERO);
        root.addOrReplaceChild("shoulders", CubeListBuilder.create().texOffs(35, 0).mirror()
                .addBox(0, -2, -2, 8, 4, 4), PartPose.offsetAndRotation(-4, 3, -2, (float) -Math.PI / 4, 0, 0));
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).mirror()
                .addBox(-4, 0, -2, 8, 12, 4), PartPose.ZERO);
        root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).mirror()
                .addBox(-3, -2, -2, 4, 12, 4), PartPose.offsetAndRotation(-5, 2, 0, -0.6F, 0.6F, 0.1F));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 16).mirror()
                .addBox(-1, -2, -2, 4, 12, 4), PartPose.offsetAndRotation(5, 2, 0, -0.6F, -0.6F, -0.1F));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).mirror()
                .addBox(-2, 0, -2, 4, 12, 4), PartPose.offset(-2, 12, 0));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16).mirror()
                .addBox(-2, 0, -2, 4, 12, 4), PartPose.offset(2, 12, 0));
        root.addOrReplaceChild("lower_dress", CubeListBuilder.create().texOffs(0, 45).mirror()
                .addBox(-6, 0, -4, 12, 7, 8), PartPose.offset(0, 17, 0));
        root.addOrReplaceChild("middle_dress", CubeListBuilder.create().texOffs(0, 33).mirror()
                .addBox(-5, 0, -3, 10, 5, 6), PartPose.offset(0, 12, 0));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void render(PlacedOwnerBlockEntity statue, float partialTick, PoseStack pose, MultiBufferSource buffers,
                       int light, int overlay) {
        pose.pushPose();
        pose.translate(0.5, 1.5, 0.5);
        pose.mulPose(Axis.ZP.rotationDegrees(180));
        pose.mulPose(Axis.YP.rotationDegrees(statue.getBlockState().getValue(StatueGoddessBlock.FACING).toYRot() - 180));
        model.render(pose, buffers.getBuffer(RenderType.entityCutout(TEXTURE)), light, overlay);
        pose.popPose();
    }
}
