package art.arcane.witchery.client.model;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.Hobgoblin;
import art.arcane.witchery.registry.WitcheryItems;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;

import java.util.List;

public final class HobgoblinModel extends AgeableListModel<Hobgoblin> implements ArmedModel {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "goblin"), "main");
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;

    public HobgoblinModel(ModelPart root) {
        super(true, 16, 0, 2, 2, 24);
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
        root.addOrReplaceChild("head", CubeListBuilder.create().mirror()
                .texOffs(0, 0).addBox(-4, -8, -4, 8, 8, 8)
                .texOffs(34, 3).addBox(-0.5F, -6, -5, 1, 3, 1)
                .texOffs(34, 0).addBox(-0.5F, -5, -6, 1, 1, 1)
                .texOffs(33, 9).addBox(-0.5F, -4, -7, 1, 2, 2)
                .texOffs(46, 0).addBox(6, -7, 0, 2, 2, 1)
                .texOffs(39, 0).addBox(4, -7, 0, 2, 3, 1)
                .texOffs(39, 0).addBox(-6, -7, 0, 2, 3, 1)
                .texOffs(46, 0).addBox(-8, -7, 0, 2, 2, 1), PartPose.offset(0, 11, 0));
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).mirror()
                .addBox(-4, 0, -2, 8, 7, 4), PartPose.offset(0, 11, 0));
        root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(40, 16).mirror()
                .addBox(-3, -3, -2, 4, 12, 4), PartPose.offset(-5, 12, 0));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(40, 16).mirror()
                .addBox(-1, -3, -2, 4, 12, 4), PartPose.offset(5, 12, 0));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 16).mirror()
                .addBox(-2, 0, -2, 4, 6, 4), PartPose.offset(-2, 18, 0));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 16).mirror()
                .addBox(-2, 0, -2, 4, 6, 4), PartPose.offset(2, 18, 0));
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    protected Iterable<ModelPart> headParts() {
        return List.of(head);
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return List.of(body, rightArm, leftArm, rightLeg, leftLeg);
    }

    @Override
    public void setupAnim(Hobgoblin goblin, float stride, float movement, float age, float yaw, float pitch) {
        head.yRot = yaw * Mth.DEG_TO_RAD;
        head.xRot = pitch * Mth.DEG_TO_RAD;
        float swing = Mth.cos(stride * 0.6662F) * movement;
        rightArm.xRot = -swing;
        leftArm.xRot = swing;
        rightLeg.xRot = swing * 1.4F;
        leftLeg.xRot = -swing * 1.4F;
        body.xRot = 0;
        head.y = body.y = 11;
        rightLeg.z = leftLeg.z = 0.1F;
        if (!goblin.getMainHandItem().isEmpty()) rightArm.xRot = 0.5F * rightArm.xRot - Mth.PI / 10;
        if (goblin.isWorking()) {
            boolean koboldite = goblin.getMainHandItem().is(WitcheryItems.getRequired("kobolditepickaxe").get());
            rightArm.xRot -= koboldite ? (age % 6) * 0.3F : (age % 20) * 0.1F;
        }
        rightArm.xRot += Mth.sin(age * 0.067F) * 0.05F - Mth.sin(attackTime * Mth.PI) * 1.2F;
        leftArm.xRot -= Mth.sin(age * 0.067F) * 0.05F;
        if (goblin.isWorshipping()) {
            body.xRot = head.xRot = 0.5F;
            rightArm.xRot -= 2.2F;
            leftArm.xRot -= 2.2F;
            rightLeg.z = leftLeg.z = 3;
            head.y = body.y = 13;
        }
        rightArm.zRot = Mth.cos(age * 0.09F) * 0.05F + 0.05F;
        leftArm.zRot = -rightArm.zRot;
    }

    @Override
    public void translateToHand(HumanoidArm arm, PoseStack pose) {
        (arm == HumanoidArm.RIGHT ? rightArm : leftArm).translateAndRotate(pose);
    }
}
