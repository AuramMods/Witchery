package art.arcane.witchery.client.model;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.Nightmare;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class NightmareModel extends EntityModel<Nightmare> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "nightmare"), "main");
    private static final int[] MOTION_PERIODS = {12, 12, 13, 10, 12, 10, 10};

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart[] tendrils = new ModelPart[7];
    private float partialTick;
    private float opacity;

    public NightmareModel(ModelPart root) {
        super(RenderType::entityTranslucent);
        this.root = root;
        head = root.getChild("head");
        leftArm = root.getChild("left_arm");
        rightArm = root.getChild("right_arm");
        for (int index = 0; index < tendrils.length; index++) {
            tendrils[index] = root.getChild("tendril_" + index);
        }
    }

    public static LayerDefinition layer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4, -8, -3, 8, 8, 6),
                PartPose.offset(0, -6, 0));
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4, 0, -2, 8, 12, 4),
                PartPose.offset(0, -6, 0));
        arm(root, "right_arm", -5, 46, false);
        arm(root, "left_arm", 5, 53, true);
        int[][] tendrilShapes = {
                {-2, -1, 3, 0}, {2, 0, 14, 0}, {1, -1, 11, 12}, {-1, -1, 14, 8},
                {-2, 0, 12, 4}, {3, -1, 17, 8}, {0, 0, 16, 12}
        };
        for (int index = 0; index < tendrilShapes.length; index++) {
            int[] shape = tendrilShapes[index];
            root.addOrReplaceChild("tendril_" + index,
                    CubeListBuilder.create().texOffs(shape[3], 16).addBox(0, 0, 0, 1, shape[2], 1),
                    PartPose.offset(shape[0], 6, shape[1]));
        }
        return LayerDefinition.create(mesh, 64, 64);
    }

    private static void arm(PartDefinition root, String name, int x, int textureY, boolean left) {
        PartDefinition arm = root.addOrReplaceChild(name,
                CubeListBuilder.create().texOffs(40, 16).addBox(-1, -2, -1, 2, 16, 2),
                PartPose.offset(x, -4, 0));
        arm.addOrReplaceChild("index", CubeListBuilder.create().texOffs(4, textureY)
                        .addBox(left ? -1 : 0, 0, -1, 1, 6, 1),
                PartPose.offsetAndRotation(0, 12, 0, -0.148353F, 0, left ? 0.1134464F : -0.1134464F));
        arm.addOrReplaceChild("little", CubeListBuilder.create().texOffs(left ? 8 : 0, textureY)
                        .addBox(left ? 0 : -1, 0, -1, 1, 6, 1),
                PartPose.offsetAndRotation(0, 12, 0, -0.148353F, 0, left ? -0.1134464F : 0.1134464F));
        arm.addOrReplaceChild("thumb", CubeListBuilder.create().texOffs(left ? 0 : 8, textureY).addBox(-0.5F, 0, 0, 1, 6, 1),
                PartPose.offsetAndRotation(0, 12, 0, 0.1396263F, 0, 0));
    }

    @Override
    public void prepareMobModel(Nightmare nightmare, float limbSwing, float limbAmount, float partialTick) {
        this.partialTick = partialTick;
        opacity = nightmare.isDefended() ? 0.6F : 0.9F;
    }

    @Override
    public void setupAnim(Nightmare nightmare, float limbSwing, float limbAmount, float age, float headYaw, float headPitch) {
        head.yRot = headYaw * Mth.DEG_TO_RAD;
        head.xRot = headPitch * Mth.DEG_TO_RAD;
        for (int index = 0; index < tendrils.length; index++) {
            float frequency = 0.01F * (nightmare.getId() % MOTION_PERIODS[index]);
            float motion = nightmare.tickCount * frequency;
            ModelPart tendril = tendrils[index];
            tendril.xRot = -1.8F + Mth.sin(motion) * 4.5F * Mth.DEG_TO_RAD;
            tendril.yRot = -1.7F;
            tendril.zRot = 1.839205F + Mth.cos(motion) * 2.5F * Mth.DEG_TO_RAD;
        }
        int remaining = nightmare.swipeTicks();
        if (remaining > 0) {
            float phase = Mth.triangleWave(remaining - partialTick, 15);
            leftArm.xRot = rightArm.xRot = -1.5F + 0.8F * phase;
            leftArm.zRot = -1.5F + 1.5F * phase;
            rightArm.zRot = -leftArm.zRot;
        } else {
            leftArm.xRot = rightArm.xRot = -0.1F;
            leftArm.zRot = rightArm.zRot = 0;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertices, int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        root.render(poseStack, vertices, packedLight, packedOverlay, red, green, blue, alpha * opacity);
    }
}
