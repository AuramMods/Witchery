package art.arcane.witchery.client.model;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.Imp;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class ImpModel extends HierarchicalModel<Imp> {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "imp"), "main");
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public ImpModel(ModelPart root) {
        this.root = root;
        head = root.getChild("head");
        body = root.getChild("body");
        rightArm = root.getChild("right_arm");
        leftArm = root.getChild("left_arm");
        rightLeg = root.getChild("right_leg");
        leftLeg = root.getChild("left_leg");
    }

    public static LayerDefinition layer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 0).addBox(-5, -8, -4, 10, 8, 10), PartPose.offset(0, 8, 0));
        head.addOrReplaceChild("left_horn", CubeListBuilder.create().texOffs(0, 21).addBox(-1, -5, -1, 2, 5, 2),
                PartPose.offsetAndRotation(4, -5, 0, 0.409F, 0, 0.744F));
        head.addOrReplaceChild("right_horn", CubeListBuilder.create().texOffs(0, 21).addBox(-1, -5, -1, 2, 5, 2),
                PartPose.offsetAndRotation(-4, -5, 0, 0.409F, 0, -0.744F));
        head.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(9, 21).addBox(-1, 0, -1, 2, 4, 2),
                PartPose.offsetAndRotation(0, -4, -3, -0.967F, 0, 0));
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 48).addBox(-4, 0, -4, 8, 9, 7),
                PartPose.offset(0, 9, 0));
        root.addOrReplaceChild("chest", CubeListBuilder.create().texOffs(4, 41).addBox(-4, 0, -2, 6, 2, 4),
                PartPose.offset(1, 8, 0));
        root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(41, 0).addBox(-2, -2, -1.5F, 3, 13, 3),
                PartPose.offset(-5, 11, 0));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(41, 0).addBox(-1, -2, -1.5F, 3, 13, 3),
                PartPose.offset(5, 11, 0));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(33, 48).addBox(-1.5F, 0, -1.5F, 3, 6, 3),
                PartPose.offset(-1.5F, 18, 0));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(33, 48).addBox(-1.5F, 0, -1.5F, 3, 6, 3),
                PartPose.offset(1.5F, 18, 0));
        root.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(23, 21).addBox(0, 0, 0, 14, 21, 0),
                PartPose.offsetAndRotation(-2, 10, -1, 0.305F, -0.670F, -0.628F));
        root.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(23, 21).addBox(0, 0, 0, 14, 21, 0),
                PartPose.offsetAndRotation(2, 10, -1, -0.305F, 3.811F, 0.628F));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(Imp imp, float limbSwing, float strength, float age, float headYaw, float headPitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        head.xRot = headPitch * Mth.DEG_TO_RAD;
        head.yRot = headYaw * Mth.DEG_TO_RAD;
        float stride = Mth.cos(limbSwing * 0.6662F) * strength;
        rightArm.xRot = -stride;
        leftArm.xRot = stride;
        rightLeg.xRot = 1.4F * stride;
        leftLeg.xRot = -1.4F * stride;
        if (attackTime > 0) {
            body.yRot = Mth.sin(Mth.sqrt(attackTime) * Mth.TWO_PI) * 0.2F;
            rightArm.yRot = body.yRot * 3;
            leftArm.yRot = body.yRot;
            float windup = 1.0F - (float) Math.pow(1.0F - attackTime, 4);
            rightArm.xRot -= Mth.sin(windup * Mth.PI) * 1.2F;
            rightArm.zRot = -0.4F * Mth.sin(attackTime * Mth.PI);
        }
        float sway = Mth.cos(age * 0.09F) * 0.05F + 0.05F;
        rightArm.zRot += sway;
        leftArm.zRot -= sway;
        float bob = Mth.sin(age * 0.067F) * 0.05F;
        rightArm.xRot += bob;
        leftArm.xRot -= bob;
    }
}
