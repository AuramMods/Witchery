package art.arcane.witchery.client.model;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.LordOfTorment;
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

public final class LordOfTormentModel extends EntityModel<LordOfTorment> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "lordoftorment"), "main");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart rightArm;
    private final ModelPart rightExtraArm;
    private final ModelPart leftArm;
    private final ModelPart leftExtraArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final ModelPart leftWing;
    private final ModelPart rightWing;

    public LordOfTormentModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.root = root;
        head = root.getChild("head");
        rightArm = root.getChild("right_arm");
        rightExtraArm = root.getChild("right_extra_arm");
        leftArm = root.getChild("left_arm");
        leftExtraArm = root.getChild("left_extra_arm");
        rightLeg = root.getChild("right_leg");
        leftLeg = root.getChild("left_leg");
        leftWing = root.getChild("left_wing");
        rightWing = root.getChild("right_wing");
    }

    public static LayerDefinition layer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create().mirror()
                .texOffs(0, 0).addBox(-4, -8, -4, 8, 8, 8)
                .texOffs(34, 0).addBox(-3, 0, -4, 1, 7, 1).addBox(-2, 0, -5, 1, 5, 1)
                .addBox(-1, 0, -4, 1, 9, 1).addBox(0, 0, -5, 1, 6, 1)
                .addBox(1, 0, -4, 1, 4, 1).addBox(2, 0, -5, 1, 8, 1)
                .texOffs(40, 0).addBox(-3, -4, -5, 6, 4, 1)
                .texOffs(40, 6).addBox(-2, -6, -5, 4, 2, 1), PartPose.offset(0, -6, 0));
        head.addOrReplaceChild("right_horn", CubeListBuilder.create().texOffs(55, 0).addBox(-2, -15, 0, 1, 9, 1),
                PartPose.rotation(0.5948578F, 0, -0.1858931F));
        head.addOrReplaceChild("left_horn", CubeListBuilder.create().texOffs(55, 0).addBox(1, -15, 0, 1, 9, 1),
                PartPose.rotation(0.5948578F, 0, 0.1858931F));
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-4, -6, -2, 8, 14, 4), PartPose.ZERO);
        addArm(root, "right_arm", -5, -3);
        addArm(root, "right_extra_arm", -5, -3);
        addArm(root, "left_arm", 5, -1);
        addArm(root, "left_extra_arm", 5, -1);
        root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2, 0, -2, 4, 15, 4),
                PartPose.offset(-2, 8, 0));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16).addBox(-2, 0, -2, 4, 15, 4),
                PartPose.offset(2, 8, 0));
        root.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(0, 42).addBox(-20, -20, 0, 20, 40, 0),
                PartPose.offset(0, 1, 5));
        root.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(0, 82).addBox(0, -20, 0, 20, 40, 0),
                PartPose.offset(0, 1, 5));
        return LayerDefinition.create(mesh, 128, 128);
    }

    private static void addArm(PartDefinition root, String name, int position, int side) {
        root.addOrReplaceChild(name, CubeListBuilder.create().texOffs(40, 16).addBox(side, -2, -2, 4, 20, 4),
                PartPose.offset(position, -4, 0));
    }

    @Override
    public void setupAnim(LordOfTorment lord, float swing, float walk, float age, float yaw, float pitch) {
        head.yRot = yaw * Mth.DEG_TO_RAD;
        head.xRot = pitch * Mth.DEG_TO_RAD;
        float stride = Mth.cos(swing * 0.6662F) * walk;
        float wingSway = Mth.cos(age * 0.09F) * 0.3F;
        boolean moving = lord.getDeltaMovement().horizontalDistanceSqr() > 0.001;
        leftWing.yRot = moving ? 0.4F : -stride + wingSway;
        rightWing.yRot = moving ? -0.4F : -stride - wingSway;
        float armSway = Mth.cos(age * 0.09F) * 0.05F + 0.05F;
        float attack = lord.attackTicks() > 0 ? -1.85F + Mth.sin(lord.attackTicks() * 0.3F) * 0.4F : 0;
        rightArm.xRot = attack - stride;
        rightExtraArm.xRot = attack - stride * 0.5F;
        leftArm.xRot = attack + stride;
        leftExtraArm.xRot = attack + stride * 0.5F;
        rightArm.zRot = armSway;
        rightExtraArm.zRot = -armSway;
        leftArm.zRot = -armSway;
        leftExtraArm.zRot = armSway;
        rightLeg.xRot = stride * 0.5F;
        leftLeg.xRot = -rightLeg.xRot;
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer vertices, int light, int overlay,
                               float red, float green, float blue, float alpha) {
        root.render(pose, vertices, light, overlay, red, green, blue, alpha);
    }
}
