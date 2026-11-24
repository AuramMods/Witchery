package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.block.SpinningWheelBlock;
import art.arcane.witchery.block.entity.SpinningWheelBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.Util;
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

public final class SpinningWheelRenderer implements BlockEntityRenderer<SpinningWheelBlockEntity> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "spinningwheel"), "main");
    private static final RenderType TEXTURE = RenderType.entityCutout(
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/block/spinningwheel.png"));
    private static final float LEG_TILT = 0.1745329F;
    private final ModelPart model;
    private final ModelPart rim;
    private final ModelPart thread;

    public SpinningWheelRenderer(BlockEntityRendererProvider.Context context) {
        model = context.bakeLayer(LAYER);
        rim = model.getChild("wheel");
        thread = model.getChild("thread");
    }

    public static LayerDefinition layer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("seat", CubeListBuilder.create().texOffs(0, 0).addBox(-2, -1, -7, 4, 1, 14),
                PartPose.offsetAndRotation(0, 18, 0, 0.2602503F, 0, 0));
        leg(root, "rear_left", -1, 16, 5, 9, LEG_TILT, 32, 0);
        leg(root, "rear_right", 1, 16, 5, 9, LEG_TILT, 32, 0);
        leg(root, "front_left", -1, 19, -6, 6, -LEG_TILT, 0, 6);
        leg(root, "front_right", 1, 19, -6, 6, -LEG_TILT, 0, 6);
        root.addOrReplaceChild("thread", CubeListBuilder.create().texOffs(23, 0).addBox(-1, -3, -1, 2, 3, 2),
                PartPose.offset(0, 12, 5));
        root.addOrReplaceChild("thread_pole", CubeListBuilder.create().texOffs(9, 7).addBox(-0.5F, 0, -0.5F, 1, 4, 1),
                PartPose.offset(0, 12, 5));
        root.addOrReplaceChild("arm_left", CubeListBuilder.create().texOffs(28, 6).addBox(-0.5F, -7, -0.5F, 1, 7, 1),
                PartPose.offsetAndRotation(-1, 18, -2, 0.2268928F, 0, 0));
        root.addOrReplaceChild("arm_right", CubeListBuilder.create().texOffs(28, 6).addBox(-0.5F, -7, -0.5F, 1, 7, 1),
                PartPose.offsetAndRotation(1, 18, -2, 0.2268928F, 0, 0));
        root.addOrReplaceChild("wheel", CubeListBuilder.create().mirror()
                .texOffs(0, -6).addBox(0, -3, -3, 0, 6, 6)
                .texOffs(0, 7).addBox(-0.5F, -4, -3, 1, 1, 6)
                .texOffs(0, 7).addBox(-0.5F, 3, -3, 1, 1, 6)
                .texOffs(23, 5).addBox(-0.5F, -4, 3, 1, 8, 1)
                .texOffs(23, 5).addBox(-0.5F, -4, -4, 1, 8, 1), PartPose.offset(0, 12, -3.5F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    private static void leg(PartDefinition root, String name, int x, int y, int z, int length, float pitch, int u, int v) {
        root.addOrReplaceChild(name, CubeListBuilder.create().texOffs(u, v).addBox(x < 0 ? -1 : 0, 0, 0, 1, length, 1),
                PartPose.offsetAndRotation(x, y, z, pitch, 0, -x * LEG_TILT));
    }

    @Override
    public void render(SpinningWheelBlockEntity wheel, float partialTick, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        var state = wheel.getBlockState();
        boolean spinning = state.getValue(SpinningWheelBlock.SPINNING);
        // The legacy animation steps its radian angles every 75 ms and 50 ms.
        long step = spinning ? Util.getMillis() / 25 : 0;
        rim.xRot = -(step / 3 % 360);
        thread.yRot = step / 2 % 360;
        pose.pushPose();
        pose.translate(0.5, 1.5, 0.5);
        pose.mulPose(Axis.ZP.rotationDegrees(180));
        pose.mulPose(Axis.YP.rotationDegrees(state.getValue(SpinningWheelBlock.FACING).toYRot() - 180));
        model.render(pose, buffers.getBuffer(TEXTURE), light, overlay);
        pose.popPose();
    }
}
