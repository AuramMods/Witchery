package art.arcane.witchery.client.model;

import art.arcane.witchery.entity.Leonard;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class LeonardModel extends HierarchicalModel<Leonard> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath("witchery", "leonard"), "main");

    private final ModelPart root;
    private final ModelPart neck;
    private final ModelPart body;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;

    public LeonardModel(ModelPart root) {
        this.root = root;
        neck = root.getChild("neck");
        body = root.getChild("body");
        leftArm = root.getChild("leftarm");
        rightArm = root.getChild("rightarm");
        leftLeg = root.getChild("leftleg");
        rightLeg = root.getChild("rightleg");
    }

    public static LayerDefinition layer() {
        var mesh = new MeshDefinition();
        var root = mesh.getRoot();
        var neck = root.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(48, 0).mirror()
                .addBox(-2, -1, -2, 4, 2, 4), PartPose.offsetAndRotation(0, 0, 0, 0.1745329F, 0, 0));
        var head = neck.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).mirror()
                .addBox(-3, -5, -1, 6, 4, 4), PartPose.offsetAndRotation(0, 0, 0, 0.1745329F, 0, 0));
        head.addOrReplaceChild("snout", CubeListBuilder.create().texOffs(16, 2).mirror()
                .addBox(-2, -5, -7, 4, 4, 7), PartPose.offsetAndRotation(0, 0, 0, 0.1745329F, 0, 0));
        head.addOrReplaceChild("beard", CubeListBuilder.create().texOffs(0, 10).mirror()
                .addBox(-2, -0.2F, -7, 4, 2, 2), PartPose.offsetAndRotation(0, 0, 0, -0.0113601F, 0, 0));
        head.addOrReplaceChild("earLeft", CubeListBuilder.create().texOffs(38, 0).mirror()
                .addBox(3.5F, 1, -0.5F, 1, 3, 1), PartPose.offsetAndRotation(0, 0, 0, -0.5129616F, -0.2617994F, -1.180008F));
        head.addOrReplaceChild("earRight", CubeListBuilder.create().texOffs(38, 0).mirror()
                .addBox(-4.5F, 1, 0.5F, 1, 3, 1), PartPose.offsetAndRotation(0, 0, 0, -0.3346075F, 0.0371786F, 1.226894F));
        head.addOrReplaceChild("hornLeft", CubeListBuilder.create().texOffs(43, 0).mirror()
                .addBox(-0.5F, -12, -0.5F, 1, 8, 1), PartPose.offsetAndRotation(0, 0, 0, -0.2268928F, 0, 0.3665191F));
        head.addOrReplaceChild("hornMiddle", CubeListBuilder.create().texOffs(43, 0).mirror()
                .addBox(-0.5F, -10, -0.5F, 1, 6, 1), PartPose.offsetAndRotation(0, 0, 0, -0.2974289F, 0, 0));
        head.addOrReplaceChild("hornRight", CubeListBuilder.create().texOffs(43, 0).mirror()
                .addBox(-0.5F, -12, -0.5F, 1, 8, 1), PartPose.offsetAndRotation(0, 0, 0, -0.2268928F, 0, -0.3665191F));
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).mirror()
                .addBox(-4, 0, -2, 8, 12, 4), PartPose.offsetAndRotation(0, 0, 0, 0, 0, 0));
        root.addOrReplaceChild("rightarm", CubeListBuilder.create().texOffs(40, 16).mirror()
                .addBox(-3, -2, -2, 4, 12, 4), PartPose.offsetAndRotation(-5, 2, 0, 0, 0, 0));
        root.addOrReplaceChild("leftarm", CubeListBuilder.create().texOffs(40, 16).mirror()
                .addBox(-1, -2, -2, 4, 12, 4), PartPose.offsetAndRotation(5, 2, 0, 0, 0, 0));
        var rightleg = root.addOrReplaceChild("rightleg", CubeListBuilder.create().texOffs(0, 16).mirror()
                .addBox(-2, 0, -2, 4, 12, 4), PartPose.offset(-2, 12, 0.1F));
        var leftleg = root.addOrReplaceChild("leftleg", CubeListBuilder.create().texOffs(0, 16).mirror()
                .addBox(-2, 0, -2, 4, 12, 4), PartPose.offset(2, 12, 0.1F));
        leftleg.addOrReplaceChild("gownLowerLeft", CubeListBuilder.create().texOffs(21, 33).mirror()
                .addBox(0, 12, -2.5F, 5, 11, 5), PartPose.offsetAndRotation(-2, -12, 0, 0, 0, 0));
        rightleg.addOrReplaceChild("gownLowerRight", CubeListBuilder.create().texOffs(0, 33).mirror()
                .addBox(-5, 12, -2.5F, 5, 11, 5), PartPose.offsetAndRotation(2, -12, 0, 0, 0, 0));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public ModelPart root() { return root; }

    @Override
    public void setupAnim(Leonard leonard, float walk, float stride, float age, float yaw, float pitch) {
        neck.yRot = yaw * Mth.DEG_TO_RAD;
        neck.xRot = pitch * Mth.DEG_TO_RAD;
        float cycle = walk * 0.6662F;
        rightArm.xRot = Mth.cos(cycle + Mth.PI) * stride;
        leftArm.xRot = Mth.cos(cycle) * stride;
        rightLeg.xRot = Mth.cos(cycle) * 1.4F * stride;
        leftLeg.xRot = Mth.cos(cycle + Mth.PI) * 1.4F * stride;
        rightLeg.yRot = leftLeg.yRot = 0;
        leftArm.zRot = 0;
        if (riding) LegacyBipedMotion.ride(rightArm, leftArm, rightLeg, leftLeg);
        LegacyBipedMotion.swing(body, rightArm, leftArm, neck.xRot, attackTime);
        LegacyBipedMotion.sway(rightArm, leftArm, age);
        if (leonard.attackTicks() > 0) {
            rightArm.xRot = -2 + 1.5F * Mth.triangleWave(leonard.attackTicks() - yaw, 10);
        }
    }
}
