package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.WolfAltarBlock;
import art.arcane.witchery.block.entity.WolfAltarBlockEntity;
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
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

public final class WolfAltarRenderer implements BlockEntityRenderer<WolfAltarBlockEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "wolfaltar"), "main");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/block/wolfaltar.png");
    private final ModelPart model;

    public WolfAltarRenderer(BlockEntityRendererProvider.Context context) {
        model = scaleStatue(context.bakeLayer(LAYER));
    }

    @Override
    public void render(WolfAltarBlockEntity altar, float partialTick, PoseStack pose, MultiBufferSource buffers,
                       int light, int overlay) {
        draw(model, altar.getBlockState().getValue(WolfAltarBlock.FACING), pose, buffers, light, overlay);
    }

    public static void draw(ModelPart model, Direction facing, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        pose.pushPose();
        pose.translate(0.5, 1.5, 0.5);
        pose.mulPose(Axis.ZP.rotationDegrees(180));
        pose.mulPose(Axis.YP.rotationDegrees(facing.toYRot() - 180));
        model.render(pose, buffers.getBuffer(RenderType.entityCutout(TEXTURE)), light, overlay);
        pose.popPose();
    }

    public static ModelPart scaleStatue(ModelPart model) {
        scale(model.getChild("huntsman"), 0.7F);
        scale(model.getChild("firstWolf"), 0.5F);
        scale(model.getChild("secondWolf"), 0.5F);
        return model;
    }

    private static void scale(ModelPart part, float scale) {
        part.xScale = scale;
        part.yScale = scale;
        part.zScale = scale;
    }

    public static LayerDefinition layer() {
        MeshDefinition mesh = new MeshDefinition();
        var root = mesh.getRoot();
        root.addOrReplaceChild("base", CubeListBuilder.create().texOffs(0, 0)
                .addBox(0.0F, 0.0F, 0.0F, 16, 2, 16), PartPose.offset(-8.0F, 22.0F, -8.0F));
        root.addOrReplaceChild("middleBase", CubeListBuilder.create().texOffs(0, 0)
                .addBox(0.0F, 0.0F, 0.0F, 14, 1, 14), PartPose.offset(-7.0F, 21.0F, -7.0F));
        root.addOrReplaceChild("upperBase", CubeListBuilder.create().texOffs(0, 0)
                .addBox(0.0F, 0.0F, 0.0F, 16, 1, 16), PartPose.offset(-8.0F, 20.0F, -8.0F));
        root.addOrReplaceChild("kneeler", CubeListBuilder.create().texOffs(0, 0)
                .addBox(0.0F, 0.0F, 0.0F, 4, 2, 4), PartPose.offset(-3.9F, 18.0F, -6.5F));
        var huntsman = root.addOrReplaceChild("huntsman", CubeListBuilder.create().texOffs(16, 16)
                .addBox(-4.0F, 0.0F, -2.0F, 8, 12, 4), PartPose.offset(0.0F, 3.1F, 0.0F));
        huntsman.addOrReplaceChild("bipedLeftLeg", CubeListBuilder.create().texOffs(0, 16).mirror()
                .addBox(0.0F, 12.0F, -2.0F, 4, 12, 4), PartPose.offset(0.0F, 0.0F, 0.0F));
        var wolfHeadMain0 = huntsman.addOrReplaceChild("wolfHeadMain0", CubeListBuilder.create().texOffs(0, 42)
                .addBox(-3.0F, -6.0F, -2.0F, 6, 6, 5), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.13665928F, 0.13665928F, 0.0F));
        wolfHeadMain0.addOrReplaceChild("wolfHeadMain3", CubeListBuilder.create().texOffs(0, 10)
                .addBox(-1.5F, -3.1F, -5.0F, 3, 3, 4), PartPose.offset(0.0F, 0.0F, 0.0F));
        wolfHeadMain0.addOrReplaceChild("wolfHeadMain2", CubeListBuilder.create().texOffs(16, 14)
                .addBox(1.0F, -8.0F, 2.0F, 2, 2, 1), PartPose.offset(0.0F, 0.0F, 0.0F));
        wolfHeadMain0.addOrReplaceChild("wolfHeadMain1", CubeListBuilder.create().texOffs(16, 14)
                .addBox(-3.0F, -8.0F, 2.0F, 2, 2, 1), PartPose.offset(0.0F, 0.0F, 0.0F));
        huntsman.addOrReplaceChild("bipedLeftArm", CubeListBuilder.create().texOffs(40, 16).mirror()
                .addBox(4.0F, -2.0F, -2.0F, 4, 12, 4), PartPose.offsetAndRotation(0.0F, 2.0F, 0.0F, -0.4553564F, -0.13665928F, 0.0F));
        var bipedRightArm = huntsman.addOrReplaceChild("bipedRightArm", CubeListBuilder.create().texOffs(40, 16).mirror()
                .addBox(-8.0F, -2.0F, -2.0F, 4, 12, 4), PartPose.offsetAndRotation(0.0F, 2.0F, 0.0F, -2.4586453F, 0.4098033F, 0.0F));
        bipedRightArm.addOrReplaceChild("shaft", CubeListBuilder.create().texOffs(0, 33)
                .addBox(-6.5F, 8.0F, -17.9F, 1, 1, 40), PartPose.offset(0.0F, 0.0F, 0.0F));
        var bipedRightLeg = huntsman.addOrReplaceChild("bipedRightLeg", CubeListBuilder.create().texOffs(0, 16).mirror()
                .addBox(-4.0F, 7.4F, 8.0F, 4, 6, 4), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.0471976F, 0.0F, 0.0F));
        bipedRightLeg.addOrReplaceChild("bipedRightLegLower", CubeListBuilder.create().texOffs(18, 0)
                .addBox(-4.0F, 15.7F, -6.2F, 4, 6, 4), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.95609134F, 0.0F, 0.0F));
        var firstWolf = root.addOrReplaceChild("firstWolf", CubeListBuilder.create().texOffs(18, 14)
                .addBox(-3.0F, -2.0F, -3.0F, 6, 6, 9), PartPose.offsetAndRotation(-5.0F, 14.0F, 3.5F, 0.0F, 0.27314404F, 0.0F));
        var wolfHeadMain0_1 = firstWolf.addOrReplaceChild("wolfHeadMain0_1", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-3.0F, -2.5F, -13.0F, 6, 6, 4), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.22759093F, -0.22759093F, 0.0F));
        wolfHeadMain0_1.addOrReplaceChild("wolfHeadMain1_1", CubeListBuilder.create().texOffs(16, 14)
                .addBox(-3.0F, -4.5F, -10.0F, 2, 2, 1), PartPose.offset(0.0F, 0.0F, 0.0F));
        wolfHeadMain0_1.addOrReplaceChild("wolfHeadMain3_1", CubeListBuilder.create().texOffs(0, 10)
                .addBox(-1.5F, 0.5F, -17.1F, 3, 3, 4), PartPose.offset(0.0F, 0.0F, 0.0F));
        wolfHeadMain0_1.addOrReplaceChild("wolfHeadMain2_1", CubeListBuilder.create().texOffs(16, 14)
                .addBox(1.0F, -4.5F, -10.0F, 2, 2, 1), PartPose.offset(0.0F, 0.0F, 0.0F));
        firstWolf.addOrReplaceChild("wolfLeg1", CubeListBuilder.create().texOffs(0, 18)
                .addBox(-2.5F, 4.0F, 3.0F, 2, 8, 2), PartPose.offset(0.0F, 0.0F, 0.0F));
        firstWolf.addOrReplaceChild("wolfTail", CubeListBuilder.create().texOffs(9, 18)
                .addBox(-1.0F, 0.0F, 4.0F, 2, 8, 2), PartPose.offsetAndRotation(0.0F, 0.6F, 0.0F, 0.31869712F, 0.0F, 0.0F));
        firstWolf.addOrReplaceChild("wolfLeg2", CubeListBuilder.create().texOffs(0, 18)
                .addBox(0.5F, 4.0F, 3.0F, 2, 8, 2), PartPose.offset(0.0F, 0.0F, 0.0F));
        firstWolf.addOrReplaceChild("wolfLeg4", CubeListBuilder.create().texOffs(0, 18)
                .addBox(0.5F, 4.0F, -8.0F, 2, 8, 2), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.091106184F, 0.0F));
        firstWolf.addOrReplaceChild("wolfLeg3", CubeListBuilder.create().texOffs(0, 18)
                .addBox(-2.5F, 4.0F, -8.0F, 2, 8, 2), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.091106184F, 0.0F));
        firstWolf.addOrReplaceChild("wolfMane", CubeListBuilder.create().texOffs(21, 0)
                .addBox(-4.0F, -3.0F, -9.0F, 8, 7, 6), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.13665928F, 0.0F));
        var secondWolf = root.addOrReplaceChild("secondWolf", CubeListBuilder.create().texOffs(18, 14)
                .addBox(-3.0F, -2.0F, -3.0F, 6, 6, 9), PartPose.offsetAndRotation(5.5F, 14.0F, -2.0F, 0.0F, 0.31869712F, 0.0F));
        secondWolf.addOrReplaceChild("wolfTail_1", CubeListBuilder.create().texOffs(9, 18)
                .addBox(-1.0F, 0.0F, 4.0F, 2, 8, 2), PartPose.offsetAndRotation(0.0F, 0.6F, 0.0F, 0.31869712F, 0.0F, 0.0F));
        secondWolf.addOrReplaceChild("wolfLeg3_1", CubeListBuilder.create().texOffs(0, 18)
                .addBox(-2.5F, 4.0F, -8.0F, 2, 8, 2), PartPose.offset(0.0F, 0.0F, 0.0F));
        secondWolf.addOrReplaceChild("wolfLeg1_1", CubeListBuilder.create().texOffs(0, 18)
                .addBox(-2.5F, 4.0F, 3.0F, 2, 8, 2), PartPose.offset(0.0F, 0.0F, 0.0F));
        secondWolf.addOrReplaceChild("wolfLeg4_1", CubeListBuilder.create().texOffs(0, 18)
                .addBox(0.5F, 4.0F, -8.0F, 2, 8, 2), PartPose.offset(0.0F, 0.0F, 0.0F));
        var wolfHeadMain0_2 = secondWolf.addOrReplaceChild("wolfHeadMain0_2", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-3.0F, -2.5F, -13.0F, 6, 6, 4), PartPose.offset(0.0F, 0.0F, 0.0F));
        wolfHeadMain0_2.addOrReplaceChild("wolfHeadMain3_2", CubeListBuilder.create().texOffs(0, 10)
                .addBox(-1.5F, 0.5F, -17.1F, 3, 3, 4), PartPose.offset(0.0F, 0.0F, 0.0F));
        wolfHeadMain0_2.addOrReplaceChild("wolfHeadMain1_2", CubeListBuilder.create().texOffs(16, 14)
                .addBox(-3.0F, -4.5F, -10.0F, 2, 2, 1), PartPose.offset(0.0F, 0.0F, 0.0F));
        wolfHeadMain0_2.addOrReplaceChild("wolfHeadMain2_2", CubeListBuilder.create().texOffs(16, 14)
                .addBox(1.0F, -4.5F, -10.0F, 2, 2, 1), PartPose.offset(0.0F, 0.0F, 0.0F));
        secondWolf.addOrReplaceChild("wolfLeg2_1", CubeListBuilder.create().texOffs(0, 18)
                .addBox(0.5F, 4.0F, 3.0F, 2, 8, 2), PartPose.offset(0.0F, 0.0F, 0.0F));
        secondWolf.addOrReplaceChild("wolfMane_1", CubeListBuilder.create().texOffs(21, 0)
                .addBox(-4.0F, -3.0F, -9.0F, 8, 7, 6), PartPose.offset(0.0F, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 128, 128);
    }
}
