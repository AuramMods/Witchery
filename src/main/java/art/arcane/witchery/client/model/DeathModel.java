package art.arcane.witchery.client.model;

import art.arcane.witchery.entity.DeathBoss;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.Set;

public final class DeathModel extends HierarchicalModel<DeathBoss> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath("witchery", "death"), "main");
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public DeathModel(ModelPart root) {
        this.root = root;
        head = root.getChild("head");
        body = root.getChild("body");
        rightArm = root.getChild("right_arm");
        leftArm = root.getChild("left_arm");
        rightLeg = root.getChild("right_leg");
        leftLeg = root.getChild("left_leg");
    }

    public static LayerDefinition layer() {
        var mesh = new MeshDefinition();
        var root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(27, 43)
                .addBox(-4, -8, -4, 8, 10, 8), PartPose.ZERO);
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16)
                .addBox(-4, 0, -2, 8, 12, 4), PartPose.ZERO);
        var rightArm = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16)
                .addBox(-3, -2, -2, 4, 12, 4), PartPose.offset(-5, 2, 0));
        rightArm.addOrReplaceChild("scythe", scythe(), PartPose.offsetAndRotation(-0.8F, 8.1F, 0, Mth.HALF_PI, 0, 0));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 16)
                .addBox(-1, -2, -2, 4, 12, 4), PartPose.offset(5, 2, 0));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16)
                .addBox(-1, 0, -1, 2, 12, 2), PartPose.offset(-2, 12, 0.1F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16)
                .addBox(-1, 0, -1, 2, 12, 2), PartPose.offset(2, 12, 0.1F));
        root.addOrReplaceChild("robe", CubeListBuilder.create().texOffs(0, 33)
                .addBox(-4, 0, -2.5F, 8, 23, 5, new CubeDeformation(0.2F, 0, 0.125F)), PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 64);
    }

    public static CubeListBuilder scythe() {
        return CubeListBuilder.create().mirror().texOffs(58, 5).addBox(-0.5F, -16, -0.5F, 1, 35, 1)
                .texOffs(36, 0).addBox(0, -16, 0, 13, 4, 0, Set.of(Direction.NORTH, Direction.SOUTH));
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(DeathBoss death, float walk, float stride, float age, float yaw, float pitch) {
        head.yRot = yaw * Mth.DEG_TO_RAD;
        head.xRot = pitch * Mth.DEG_TO_RAD;
        float phase = walk * 0.6662F;
        float rightStep = Mth.cos(phase);
        float leftStep = Mth.cos(phase + Mth.PI);
        rightArm.xRot = leftStep * stride - Mth.HALF_PI;
        leftArm.xRot = rightStep * stride;
        rightArm.zRot = leftArm.zRot = 0;
        rightLeg.xRot = rightStep * 1.4F * stride;
        leftLeg.xRot = leftStep * 1.4F * stride;
        LegacyBipedMotion.swing(body, rightArm, leftArm, head.xRot, attackTime);
        LegacyBipedMotion.sway(rightArm, leftArm, age);
    }
}
