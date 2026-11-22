package art.arcane.witchery.client.model;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.Mandrake;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class MandrakeModel extends HierarchicalModel<Mandrake> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "mandrake"), "main");
    private final ModelPart root;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public MandrakeModel(ModelPart root) {
        this.root = root;
        rightArm = root.getChild("right_arm");
        leftArm = root.getChild("left_arm");
        rightLeg = root.getChild("right_leg");
        leftLeg = root.getChild("left_leg");
    }

    public static LayerDefinition layer() {
        MeshDefinition mesh = new MeshDefinition();
        var root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create().mirror()
                .texOffs(0, 8).addBox(-2, -4, -2, 4, 4, 4)
                .texOffs(0, 0).addBox(-4, -12, 0, 8, 8, 0), PartPose.offset(0, 16, 0));
        root.addOrReplaceChild("body", CubeListBuilder.create().mirror()
                .texOffs(21, 0).addBox(-2.5F, 0, -2.5F, 5, 2, 5)
                .texOffs(17, 7).addBox(-3.5F, 2, -3.5F, 7, 3, 7), PartPose.offset(0, 16, 0));
        root.addOrReplaceChild("right_arm", arm(-1), PartPose.offsetAndRotation(-2, 17, 0, 0, 0, Mth.PI / 3));
        root.addOrReplaceChild("left_arm", arm(0), PartPose.offsetAndRotation(2, 17, 0, 0, 0, -Mth.PI / 3));
        root.addOrReplaceChild("right_leg", leg(), PartPose.offset(-1, 21, 0));
        root.addOrReplaceChild("left_leg", leg(), PartPose.offset(1, 21, 0));
        return LayerDefinition.create(mesh, 64, 32);
    }

    private static CubeListBuilder arm(float x) {
        return CubeListBuilder.create().texOffs(37, 0).addBox(x, 0, -0.5F, 1, 3, 1);
    }

    private static CubeListBuilder leg() {
        return CubeListBuilder.create().texOffs(27, 18).addBox(-1, 0, -1, 2, 3, 2);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(Mandrake entity, float limbSwing, float amount, float age, float yaw, float pitch) {
        float armSwing = Mth.triangleWave(limbSwing, 13.0F);
        rightArm.xRot = (-0.2F + 1.5F * armSwing) * amount;
        leftArm.xRot = (-0.2F - 1.5F * armSwing) * amount;
        rightLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 0.7F * amount;
        leftLeg.xRot = -rightLeg.xRot;
    }
}
