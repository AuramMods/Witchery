package art.arcane.witchery.client.model;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.Lilith;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class LilithModel extends HierarchicalModel<Lilith> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "lilith"), "main");
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final ModelPart frontSkirt;
    private final ModelPart backSkirt;

    public LilithModel(ModelPart root) {
        this.root = root;
        head = root.getChild("head");
        rightArm = root.getChild("right_arm");
        leftArm = root.getChild("left_arm");
        rightLeg = root.getChild("right_leg");
        leftLeg = root.getChild("left_leg");
        frontSkirt = root.getChild("front_skirt");
        backSkirt = root.getChild("back_skirt");
    }

    public static LayerDefinition layer() {
        MeshDefinition mesh = new MeshDefinition();
        var root = mesh.getRoot();
        root.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(24, 0).addBox(-1.5F, -1.5F, -1.5F, 3, 2, 3),
                PartPose.offset(0, -13, 0));
        root.addOrReplaceChild("chest", CubeListBuilder.create().texOffs(17, 17).addBox(-4, -1.5F, -1.5F, 8, 3, 3),
                PartPose.offsetAndRotation(0, -9.8F, -1.9F, Mth.PI / 4, 0, 0));
        root.addOrReplaceChild("shoulders", CubeListBuilder.create().texOffs(15, 6).addBox(-4, 0, -2, 8, 6, 4),
                PartPose.offset(0, -12.7F, 0));
        root.addOrReplaceChild("waist", CubeListBuilder.create().texOffs(20, 24).addBox(-3, 0, -1, 6, 10, 2),
                PartPose.offset(0, -7.5F, 0));
        for (String name : new String[]{"front_skirt", "back_skirt"}) {
            root.addOrReplaceChild(name, CubeListBuilder.create().texOffs(0, 49).addBox(-4.5F, 0, -2.5F, 9, 10, 5),
                    PartPose.offset(0, -0.9F, 0));
        }
        addLimbs(root, false);
        addLimbs(root, true);
        var head = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(40, 0).addBox(-3, -6, -3, 6, 6, 6)
                .texOffs(41, 0).addBox(-0.5F, -3.6F, -4, 1, 2, 1)
                .texOffs(20, 0).addBox(-1.5F, -1.6F, -3.6F, 1, 3, 1, new CubeDeformation(-0.35F))
                .mirror().texOffs(20, 0).addBox(0.5F, -1.6F, -3.6F, 1, 3, 1, new CubeDeformation(-0.35F)),
                PartPose.offset(0, -13.5F, 0));
        var hair = head.addOrReplaceChild("hair", CubeListBuilder.create().texOffs(42, 12).addBox(-2.5F, -5.5F, 1, 5, 5, 5),
                PartPose.rotation(-0.182F, 0, 0));
        hair.addOrReplaceChild("bun", CubeListBuilder.create().texOffs(44, 22).addBox(-2, -4.7F, 5.6F, 4, 4, 4), PartPose.ZERO);
        head.addOrReplaceChild("horns", CubeListBuilder.create().texOffs(52, 30).addBox(-7, -12.3F, 0, 6, 10, 0)
                        .mirror().addBox(1, -12.3F, 0, 6, 10, 0), PartPose.rotation(-0.182F, 0, 0));
        return LayerDefinition.create(mesh, 64, 64);
    }

    private static void addLimbs(PartDefinition root, boolean left) {
        String side = left ? "left" : "right";
        float armX = left ? -0.5F : -2.5F;
        var arm = root.addOrReplaceChild(side + "_arm", CubeListBuilder.create().mirror(left).texOffs(0, 0)
                .addBox(armX, -1.5F, -1.5F, 3, 13, 3), PartPose.offset(left ? 4.4F : -4.5F, -11.5F, 0));
        arm.addOrReplaceChild("forearm", CubeListBuilder.create().mirror(left).texOffs(8, 25)
                .addBox(armX, 9.8F, 0.8F, 3, 13, 3), PartPose.rotation(-0.228F, 0, 0));
        arm.addOrReplaceChild("wing", CubeListBuilder.create().mirror(left).texOffs(0, 13)
                .addBox(left ? 1 : -1, -19.6F, -12.7F, 0, 30, 4), PartPose.rotation(2.55F, left ? 0.175F : -0.175F, 0));
        var leg = root.addOrReplaceChild(side + "_leg", CubeListBuilder.create().mirror(left).texOffs(36, 30)
                .addBox(-2, 0, -2, 4, 13, 4), PartPose.offset(left ? 2.1F : -2.1F, 2.5F, 0));
        leg.addOrReplaceChild("shin", CubeListBuilder.create().mirror(left).texOffs(48, 47).addBox(-2, 8, 2, 4, 13, 4), PartPose.ZERO);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(Lilith entity, float walk, float distance, float age, float yaw, float pitch) {
        head.yRot = yaw * Mth.DEG_TO_RAD;
        head.xRot = pitch * Mth.DEG_TO_RAD;
        float cycle = walk * 0.6662F;
        float stride = Mth.cos(cycle) * distance;
        float sway = Mth.sin(age * 0.067F) * 0.05F;
        rightArm.xRot = -stride + sway;
        leftArm.xRot = stride - sway;
        rightArm.zRot = Mth.cos(age * 0.09F) * 0.05F + 0.05F;
        leftArm.zRot = -rightArm.zRot;
        rightLeg.xRot = Math.max(stride * 1.4F - 0.273F, -0.8F);
        leftLeg.xRot = Math.max(-stride * 1.4F - 0.273F, -0.8F);
        frontSkirt.xRot = Math.min(rightLeg.xRot, leftLeg.xRot);
        backSkirt.xRot = Math.max(0.2F, Math.max(rightLeg.xRot, leftLeg.xRot));
        if (entity.attackTicks() > 0) rightArm.xRot = -2 + 1.5F * Mth.triangleWave(entity.attackTicks(), 10);
    }
}
