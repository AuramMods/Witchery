package art.arcane.witchery.client.model;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.WitchToad;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.List;

public final class WitchToadModel extends WitchPetModel<WitchToad> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "toad"), "main");
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public WitchToadModel(ModelPart root) {
        super(root, 10, 1, 0.065205686F, 0.04921184F, 0.038139176F,
                List.of(root.getChild("body"), root.getChild("right_arm"), root.getChild("left_arm"),
                        root.getChild("right_leg"), root.getChild("left_leg")));
        rightArm = root.getChild("right_arm");
        leftArm = root.getChild("left_arm");
        rightLeg = root.getChild("right_leg");
        leftLeg = root.getChild("left_leg");
    }

    public static LayerDefinition layer() {
        MeshDefinition mesh = new MeshDefinition();
        var root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create().mirror()
                        .texOffs(0, 5).addBox(-2, -1, -4, 4, 2, 4)
                        .texOffs(0, 0).addBox(-2.5F, -3, -3, 2, 2, 2)
                        .texOffs(8, 0).addBox(0.5F, -3, -3, 2, 2, 2),
                PartPose.offset(0, 20, -1));
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 12).addBox(-2, -1, 0, 4, 2, 5),
                PartPose.offsetAndRotation(0, 20, -1, -0.4833219F, 0, 0));
        root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(13, 26).addBox(-1, 0, 0, 1, 4, 1),
                PartPose.offset(-2, 20, -1));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(18, 26).addBox(0, 0, 0, 1, 4, 1),
                PartPose.offset(2, 20, -1));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create().mirror()
                        .texOffs(0, 20).addBox(-2, -1, -2, 2, 2, 3)
                        .texOffs(0, 26).addBox(-3, 1, -4, 3, 0, 3),
                PartPose.offset(-2, 23, 3));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().mirror()
                        .texOffs(11, 20).addBox(0, -1, -2, 2, 2, 3)
                        .texOffs(0, 26).addBox(0, 1, -4, 3, 0, 3),
                PartPose.offset(2, 23, 3));
        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public void setupAnim(WitchToad toad, float swing, float walk, float age, float yaw, float pitch) {
        head.yRot = yaw * Mth.DEG_TO_RAD;
        head.xRot = pitch * Mth.DEG_TO_RAD;
        float hop = Mth.cos(swing * 0.6662F) * 1.4F * walk;
        if (toad.isInSittingPose()) {
            rightLeg.xRot = leftLeg.xRot = -Mth.PI / 8;
            leftArm.xRot = rightArm.xRot = -0.3346075F;
        } else {
            rightLeg.xRot = leftLeg.xRot = hop + (walk > 0.1F ? 0.5F : 0);
            rightArm.xRot = hop;
            leftArm.xRot = -hop;
        }
    }
}
