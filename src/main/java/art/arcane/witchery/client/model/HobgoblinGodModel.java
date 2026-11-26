package art.arcane.witchery.client.model;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.HobgoblinGod;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import java.util.List;

public final class HobgoblinGodModel extends HobgoblinBipedModel<HobgoblinGod> {
    public static final ModelLayerLocation MOG = layerId("goblinmog");
    public static final ModelLayerLocation GULG = layerId("goblingulg");
    private final ModelPart chest;
    private final ModelPart skirt;

    public HobgoblinGodModel(ModelPart root) {
        super(root);
        chest = root.getChild("chest");
        skirt = root.getChild("skirt");
    }

    private static ModelLayerLocation layerId(String path) {
        return new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Witchery.MODID, path), "main");
    }

    public static LayerDefinition layer(boolean mog) {
        var mesh = new MeshDefinition();
        var root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create().mirror()
                .texOffs(0, 0).addBox(-4, -8, -4, 8, 8, 8)
                .texOffs(0, 4).addBox(-2, -4, -5, 1, 2, 1)
                .texOffs(0, 4).addBox(1, -4, -5, 1, 2, 1)
                .texOffs(25, 0).addBox(-1, -6, -6, 2, 3, 2)
                .texOffs(34, 0).addBox(-2, -2, -6, 4, 1, 2), PartPose.ZERO);
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16)
                .addBox(-4, 0, -2, 8, mog ? 12 : 4, 4), PartPose.offset(0, mog ? 0 : 8, 0));
        if (mog) {
            root.addOrReplaceChild("chest", CubeListBuilder.create().texOffs(35, 5)
                    .addBox(-4, -2, -5, 8, 4, 4), PartPose.offsetAndRotation(0, 2, 0, Mth.PI / 4, 0, 0));
        } else {
            root.addOrReplaceChild("chest", CubeListBuilder.create().texOffs(12, 35)
                    .addBox(-5, 0, -3, 10, 8, 6), PartPose.ZERO);
        }
        var skirt = CubeListBuilder.create();
        if (mog) skirt.texOffs(14, 34).addBox(-4.5F, 0, -2.5F, 9, 11, 5);
        root.addOrReplaceChild("skirt", skirt, PartPose.offset(0, 12, 0));
        root.addOrReplaceChild("right_arm", arm(-3, mog), PartPose.offset(mog ? -5 : -6, 2, 0));
        root.addOrReplaceChild("left_arm", arm(mog ? -1 : 0, mog), PartPose.offset(5, 2, 0));
        root.addOrReplaceChild("right_leg", leg(), PartPose.offset(-2, 12, 0));
        root.addOrReplaceChild("left_leg", leg(), PartPose.offset(2, 12, 0));
        return LayerDefinition.create(mesh, 64, 64);
    }

    private static CubeListBuilder arm(int x, boolean mog) {
        return CubeListBuilder.create().texOffs(40, 14).addBox(x, -2, -2, 4, mog ? 14 : 16, 4);
    }

    private static CubeListBuilder leg() {
        return CubeListBuilder.create().texOffs(0, 16).addBox(-2, 0, -2, 4, 12, 4);
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return List.of(body, chest, skirt, rightArm, leftArm, rightLeg, leftLeg);
    }

    @Override
    public void setupAnim(HobgoblinGod god, float stride, float speed, float age, float yaw, float pitch) {
        poseStride(god, stride, speed, yaw, pitch);
        poseSwing(god);
        rightLeg.z = leftLeg.z = 0.1F;
        if (god.isShiftKeyDown()) {
            body.xRot = 0.5F;
            rightArm.xRot += 0.4F;
            leftArm.xRot += 0.4F;
            rightLeg.y = leftLeg.y = 9;
            rightLeg.z = leftLeg.z = 4;
            head.y = 1;
        }
        swayArms(age);
        if (!god.isMog() && god.attackTicks() > 0) {
            rightArm.xRot = -2 + 1.5F * Mth.triangleWave(god.attackTicks() - yaw, 10);
        }
    }
}
