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
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(27, 43).mirror()
                .addBox(-4, -8, -4, 8, 10, 8), PartPose.ZERO);
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).mirror()
                .addBox(-4, 0, -2, 8, 12, 4), PartPose.ZERO);
        var rightArm = root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).mirror()
                .addBox(-3, -2, -2, 4, 12, 4), PartPose.offset(-5, 2, 0));
        rightArm.addOrReplaceChild("scythe", scythe(), PartPose.offsetAndRotation(-0.8F, 8.1F, 0, Mth.HALF_PI, 0, 0));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 16).mirror()
                .addBox(-1, -2, -2, 4, 12, 4), PartPose.offset(5, 2, 0));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).mirror()
                .addBox(-1, 0, -1, 2, 12, 2), PartPose.offset(-2, 12, 0.1F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16).mirror()
                .addBox(-1, 0, -1, 2, 12, 2), PartPose.offset(2, 12, 0.1F));
        root.addOrReplaceChild("robe", CubeListBuilder.create().texOffs(0, 33).mirror()
                .addBox(-4, 0, -2.5F, 8, 23, 5, new CubeDeformation(0.2F, 0, 0.125F)), PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 64);
    }

    public static CubeListBuilder scythe() {
        return CubeListBuilder.create().mirror().texOffs(58, 5).addBox(-0.5F, -16, -0.5F, 1, 35, 1)
                .texOffs(36, 0).addBox(0, -16, -0.04F, 13, 4, 0.08F, Set.of(Direction.NORTH, Direction.SOUTH));
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(DeathBoss death, float walk, float stride, float age, float yaw, float pitch) {
        head.yRot = yaw * Mth.DEG_TO_RAD;
        head.xRot = pitch * Mth.DEG_TO_RAD;
        float step = Mth.cos(walk * 0.6662F) * stride;
        float sway = Mth.sin(age * 0.067F) * 0.05F;
        float roll = Mth.cos(age * 0.09F) * 0.05F + 0.05F;
        body.yRot = Mth.sin(Mth.sqrt(attackTime) * Mth.TWO_PI) * 0.2F;
        rightArm.setPos(-Mth.cos(body.yRot) * 5, 2, Mth.sin(body.yRot) * 5);
        leftArm.setPos(-rightArm.x, 2, -rightArm.z);
        rightArm.xRot = -Mth.HALF_PI - step + sway;
        rightArm.yRot = body.yRot * 3;
        leftArm.xRot = step - sway + body.yRot;
        leftArm.yRot = body.yRot;
        leftArm.zRot = -roll;
        rightLeg.xRot = step * 1.4F;
        leftLeg.xRot = -rightLeg.xRot;

        float remaining = 1 - attackTime;
        float swing = Mth.sin((1 - remaining * remaining * remaining * remaining) * Mth.PI);
        float follow = Mth.sin(attackTime * Mth.PI);
        rightArm.xRot -= swing * 1.2F - follow * (head.xRot - 0.7F) * 0.75F;
        rightArm.zRot = roll - follow * 0.4F;
    }
}
