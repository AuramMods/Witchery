package art.arcane.witchery.client.model;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.Hobgoblin;
import art.arcane.witchery.registry.WitcheryItems;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.resources.ResourceLocation;

public final class HobgoblinModel extends HobgoblinBipedModel<Hobgoblin> {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "goblin"), "main");
    public HobgoblinModel(ModelPart root) {
        super(root);
    }

    public static LayerDefinition layer() {
        var mesh = new MeshDefinition();
        var root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create().mirror()
                .texOffs(0, 0).addBox(-4, -8, -4, 8, 8, 8)
                .texOffs(34, 3).addBox(-0.5F, -6, -5, 1, 3, 1)
                .texOffs(34, 0).addBox(-0.5F, -5, -6, 1, 1, 1)
                .texOffs(33, 9).addBox(-0.5F, -4, -7, 1, 2, 2)
                .texOffs(46, 0).addBox(6, -7, 0, 2, 2, 1)
                .texOffs(39, 0).addBox(4, -7, 0, 2, 3, 1)
                .texOffs(39, 0).addBox(-6, -7, 0, 2, 3, 1)
                .texOffs(46, 0).addBox(-8, -7, 0, 2, 2, 1), PartPose.offset(0, 11, 0));
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16)
                .addBox(-4, 0, -2, 8, 7, 4), PartPose.offset(0, 11, 0));
        root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16)
                .addBox(-3, -3, -2, 4, 12, 4), PartPose.offset(-5, 12, 0));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 16)
                .addBox(-1, -3, -2, 4, 12, 4), PartPose.offset(5, 12, 0));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16)
                .addBox(-2, 0, -2, 4, 6, 4), PartPose.offset(-2, 18, 0));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16)
                .addBox(-2, 0, -2, 4, 6, 4), PartPose.offset(2, 18, 0));
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(Hobgoblin goblin, float stride, float movement, float age, float yaw, float pitch) {
        poseStride(goblin, stride, movement, yaw, pitch);
        rightLeg.z = leftLeg.z = 0.1F;
        if (goblin.isWorking() && !goblin.getMainHandItem().isEmpty()) {
            boolean koboldite = goblin.getMainHandItem().is(WitcheryItems.getRequired("kobolditepickaxe").get());
            var arm = getArm(goblin.getMainArm());
            arm.xRot -= koboldite ? (goblin.tickCount % 6) * 0.3F : (goblin.tickCount % 20) * 0.1F;
        }
        poseSwing(goblin);
        if (goblin.isShiftKeyDown() || goblin.isWorshipping()) {
            body.xRot = head.xRot = 0.5F;
            rightArm.xRot -= 2.2F;
            leftArm.xRot -= 2.2F;
            rightLeg.z = leftLeg.z = 3;
            head.y = body.y = 13;
        }
        swayArms(age);
    }
}
