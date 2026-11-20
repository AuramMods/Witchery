package art.arcane.witchery.client.model;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.WitchOwl;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.List;

public final class WitchOwlModel extends WitchPetModel<WitchOwl> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "owl"), "main");
    private final ModelPart body;
    private final ModelPart rightWing;
    private final ModelPart leftWing;
    private final ModelPart rightFoot;
    private final ModelPart leftFoot;

    public WitchOwlModel(ModelPart root) {
        super(root, 11, 0.15686274F, 0.41568628F, 0.3137255F, 0.24313726F,
                List.of(root.getChild("body"), root.getChild("right_wing"), root.getChild("left_wing")));
        body = root.getChild("body");
        rightWing = root.getChild("right_wing");
        leftWing = root.getChild("left_wing");
        rightFoot = body.getChild("right_foot");
        leftFoot = body.getChild("left_foot");
    }

    public static LayerDefinition layer() {
        MeshDefinition mesh = new MeshDefinition();
        var root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create().mirror()
                        .texOffs(37, 0).addBox(-5, -7, -1, 1, 3, 2).addBox(4, -7, -1, 1, 3, 2)
                        .texOffs(30, 0).addBox(-1, -3, -4, 2, 3, 1)
                        .texOffs(0, 0).addBox(-4, -6, -3, 8, 6, 6),
                PartPose.offset(0, 15, 0));
        var body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 16).addBox(-3, 0, -2, 6, 8, 4),
                PartPose.offset(0, 15, 0));
        root.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(40, 16).addBox(-1, -1, -2, 1, 8, 4),
                PartPose.offset(-3, 16, 0));
        root.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(40, 16).addBox(0, -1, -2, 1, 8, 4),
                PartPose.offset(3, 16, 0));
        body.addOrReplaceChild("right_foot", CubeListBuilder.create().texOffs(0, 16).addBox(-1, 0, -2, 2, 1, 4),
                PartPose.offset(-2, 8, -1));
        body.addOrReplaceChild("left_foot", CubeListBuilder.create().texOffs(0, 16).addBox(-1, 0, -2, 2, 1, 4),
                PartPose.offset(2, 8, -1));
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void setupAnim(WitchOwl owl, float swing, float walk, float age, float yaw, float pitch) {
        boolean perched = owl.onGround() && owl.getDeltaMovement().horizontalDistanceSqr() < 0.001;
        body.xRot = perched ? 0 : Mth.PI / 4 + Mth.cos(age * 0.1F) * 0.15F;
        rightFoot.xRot = leftFoot.xRot = body.xRot;
        rightWing.zRot = perched ? 0 : Mth.cos(age * 0.5F) * Mth.PI * 0.4F + 1.4F;
        leftWing.zRot = -rightWing.zRot;
        rightFoot.yRot = owl.isInSittingPose() ? 0.5F : 0.1F;
        leftFoot.yRot = -rightFoot.yRot;
        head.yRot = yaw * Mth.DEG_TO_RAD;
        head.xRot = pitch * Mth.DEG_TO_RAD;
    }

    public void translateToBeak(PoseStack pose) {
        if (young) {
            pose.scale(0.75F, 0.75F, 0.75F);
            pose.translate(0, 11 / 16.0, 0);
        }
        head.translateAndRotate(pose);
        pose.translate(0, -0.05, -0.25);
    }
}
