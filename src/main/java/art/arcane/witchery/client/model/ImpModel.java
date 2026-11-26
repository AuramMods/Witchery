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
                PartPose.offsetAndRotation(4, -5, 0, 0.4089647F, 0, 0.7435722F));
        head.addOrReplaceChild("right_horn", CubeListBuilder.create().texOffs(0, 21).addBox(-1, -5, -1, 2, 5, 2),
                PartPose.offsetAndRotation(-4, -5, 0, 0.4089647F, 0, -0.7435722F));
        head.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(9, 21).addBox(-1, 0, -1, 2, 4, 2),
                PartPose.offsetAndRotation(0, -4, -3, -0.9666439F, 0, 0));
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
                PartPose.offsetAndRotation(-2, 10, -1, 0.3047198F, -0.6698132F, -0.6283185F));
        root.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(23, 21).addBox(0, 0, 0, 14, 21, 0),
                PartPose.offsetAndRotation(2, 10, -1, -0.3047198F, 3.811406F, 0.6283185F));
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
        float cycle = limbSwing * 0.6662F;
        rightArm.xRot = Mth.cos(cycle + Mth.PI) * strength;
        leftArm.xRot = Mth.cos(cycle) * strength;
        rightLeg.xRot = Mth.cos(cycle) * 1.4F * strength;
        leftLeg.xRot = Mth.cos(cycle + Mth.PI) * 1.4F * strength;
        if (riding) LegacyBipedMotion.ride(rightArm, leftArm, rightLeg, leftLeg);
        LegacyBipedMotion.swing(body, rightArm, leftArm, head.xRot, attackTime);
        LegacyBipedMotion.sway(rightArm, leftArm, age);
    }
}
