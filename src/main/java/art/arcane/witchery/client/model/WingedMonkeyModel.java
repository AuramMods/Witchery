package art.arcane.witchery.client.model;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.WingedMonkey;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class WingedMonkeyModel extends HierarchicalModel<WingedMonkey> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "wingedmonkey"), "main");
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart tail;
    private final ModelPart rightArm;
    private final ModelPart leftArm;
    private final ModelPart rightLeg;
    private final ModelPart leftLeg;
    private final ModelPart rightWing;
    private final ModelPart leftWing;
    private int color;

    public WingedMonkeyModel(ModelPart root) {
        this.root = root;
        head = root.getChild("head");
        body = root.getChild("body");
        tail = root.getChild("tail");
        rightArm = root.getChild("right_arm");
        leftArm = root.getChild("left_arm");
        rightLeg = root.getChild("right_leg");
        leftLeg = root.getChild("left_leg");
        rightWing = root.getChild("right_wing");
        leftWing = root.getChild("left_wing");
    }

    public static LayerDefinition layer() {
        var mesh = new MeshDefinition();
        var root = mesh.getRoot();
        root.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(18, 23).addBox(-.5F, -7.8F, -.5F, 1, 8, 1),
                PartPose.offsetAndRotation(0, 18.5F, 5.3F, -.63739425F, 0, 0));
        root.addOrReplaceChild("shoulder", CubeListBuilder.create().texOffs(32, 0).addBox(-3.5F, -3, -3, 7, 5, 7),
                PartPose.offsetAndRotation(0, 16, 1, Mth.HALF_PI, 0, 0));
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(36, 12).addBox(-2.5F, -2, -3, 5, 7, 5),
                PartPose.offsetAndRotation(0, 15.8F, 2, .59184116F, 0, 0));
        root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 19).addBox(-2, -1.1F, -1, 2, 11, 2),
                PartPose.offsetAndRotation(-3.5F, 14, 0, -.18203785F, 0, 0));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create().mirror().texOffs(0, 19).addBox(-.5F, -1, -1, 2, 11, 2),
                PartPose.offsetAndRotation(4, 14, 0, -.18203785F, 0, 0));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(9, 25).addBox(-1, 0, -1, 2, 5, 2),
                PartPose.offset(-1.4F, 19, 4.7F));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().mirror().texOffs(9, 25).addBox(-1, 0, -1, 2, 5, 2),
                PartPose.offset(1.4F, 19, 4.7F));
        root.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(28, 25).addBox(-12, -.5F, -3, 12, 1, 6),
                PartPose.offsetAndRotation(-1, 14, 2.5F, -.68294734F, .3642502F, .5462881F));
        root.addOrReplaceChild("left_wing", CubeListBuilder.create().mirror().texOffs(28, 25).addBox(0, -.5F, -3, 12, 1, 6),
                PartPose.offsetAndRotation(1, 14, 2.5F, -.68294734F, -.3642502F, -.5462881F));
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-3, -4, -3, 6, 6, 5)
                .texOffs(18, 14).addBox(-4.5F, -2.5F, -1.5F, 2, 3, 1)
                .mirror().texOffs(18, 14).addBox(2.5F, -2.5F, -1.5F, 2, 3, 1)
                .mirror(false).texOffs(5, 12).addBox(-2.5F, -3.5F, -3.5F, 5, 5, 1)
                .texOffs(9, 19).addBox(-2, -1.5F, -4.5F, 4, 3, 1), PartPose.offset(0, 12, -1.5F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(WingedMonkey monkey, float swing, float amount, float age, float yaw, float pitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        color = monkey.color();
        boolean landed = WitchOwlModel.isPerched(monkey);
        boolean sitting = monkey.isInSittingPose();
        float sway = Mth.sin(age * .067F) * .05F;
        float rock = Mth.cos(age * .09F) * .05F + .05F;
        if (!landed) {
            rightWing.zRot = Mth.cos(age * .5F) * Mth.PI * .4F + .2F;
            leftWing.zRot = -rightWing.zRot;
            rightWing.xRot = leftWing.xRot = 0;
            rightWing.y = leftWing.y = 12;
            rightArm.xRot = leftArm.xRot = .2F;
        }
        if (sitting || !landed) {
            float cycles = sitting && !landed ? 2 : 1;
            rightArm.zRot = rock * cycles;
            leftArm.zRot = -rightArm.zRot;
            rightArm.xRot += sway * cycles;
            leftArm.xRot -= sway * cycles;
        }
        if (sitting) {
            rightLeg.xRot = leftLeg.xRot = landed ? -1.3F : 0;
            rightLeg.y = leftLeg.y = 21;
            rightLeg.z = leftLeg.z = 2;
            body.xRot = .1F;
            body.y = 17;
            tail.y = 20;
            tail.z = 4;
            tail.xRot = -.9F;
        }
        if (amount > .1F) tail.xRot -= amount + .1F;
        tail.zRot += (amount > .1F ? 5 : 3) * Mth.cos(age * .09F) * .05F + .05F;
        head.yRot = yaw * Mth.DEG_TO_RAD;
        head.xRot = pitch * Mth.DEG_TO_RAD;
        if (monkey.attackTicks() > 0) rightArm.xRot = -2 + 1.5F * Mth.triangleWave(monkey.attackTicks() - yaw, 10);
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int light, int overlay, float r, float g, float b, float alpha) {
        if (color != 0) {
            float[] dye = LegacyDyeColors.components(color);
            r *= .41568628F + dye[0] * .15686274F;
            g *= .3137255F + dye[1] * .15686274F;
            b *= .24313726F + dye[2] * .15686274F;
        }
        super.renderToBuffer(pose, buffer, light, overlay, r, g, b, alpha);
    }
}
