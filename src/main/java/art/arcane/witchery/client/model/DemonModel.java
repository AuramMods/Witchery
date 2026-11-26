package art.arcane.witchery.client.model;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.Demon;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class DemonModel extends EntityModel<Demon> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "demon"), "main");
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;
    private float partialTick;

    public DemonModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.root = root;
        head = root.getChild("head");
        leftArm = root.getChild("left_arm");
        rightArm = root.getChild("right_arm");
        leftLeg = root.getChild("left_leg");
        rightLeg = root.getChild("right_leg");
    }

    public static LayerDefinition layer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("head", headGeometry(), PartPose.offset(0, -9, 0));
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(64, 0).addBox(-4, 0, -3, 8, 14, 6),
                PartPose.offset(0, -9, 0));
        root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(48, 0).addBox(-3, -2, -2, 4, 20, 4),
                PartPose.offset(-5, -7, 0));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(48, 0).addBox(-1, -2, -2, 4, 20, 4),
                PartPose.offset(5, -7, 0));
        root.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(32, 0).addBox(-2, 0, -2, 4, 20, 4),
                PartPose.offset(-2, 4, 0));
        root.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(32, 0).addBox(-2, 0, -2, 4, 20, 4),
                PartPose.offset(2, 4, 0));
        root.addOrReplaceChild("right_wing", CubeListBuilder.create().texOffs(93, 0).addBox(0, 0, 0, 14, 21, 0),
                PartPose.offsetAndRotation(1, -8, 3, 0.3047198F, -0.6698132F, -0.6283185F));
        root.addOrReplaceChild("left_wing", CubeListBuilder.create().texOffs(93, 0).addBox(0, 0, 0, 14, 21, 0),
                PartPose.offsetAndRotation(-1, -8, 3, -0.3047198F, 3.811406F, 0.6283185F));
        return LayerDefinition.create(mesh, 128, 32);
    }

    public static CubeListBuilder headGeometry() {
        return CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4, -8, -4, 8, 8, 8)
                .texOffs(0, 16).addBox(4, -12, -0.5F, 1, 8, 1).addBox(-5, -12, -0.5F, 1, 8, 1)
                .texOffs(4, 16).addBox(1, -4, -5, 1, 2, 1).addBox(-2, -4, -5, 1, 2, 1)
                .texOffs(8, 16).addBox(-2, -2, -6, 4, 1, 2)
                .texOffs(20, 16).addBox(-1, -6, -6, 2, 3, 2);
    }

    @Override
    public void prepareMobModel(Demon demon, float swing, float walk, float partialTick) {
        this.partialTick = partialTick;
    }

    @Override
    public void setupAnim(Demon demon, float swing, float walk, float age, float yaw, float pitch) {
        head.yRot = yaw * Mth.DEG_TO_RAD;
        head.xRot = pitch * Mth.DEG_TO_RAD;
        float stride = 1.5F * triangle(swing, 13);
        rightLeg.xRot = stride * walk;
        leftLeg.xRot = -rightLeg.xRot;
        leftArm.xRot = (-0.2F - stride) * walk;
        rightArm.xRot = demon.attackTicks() > 0 ? -2 + 1.5F * triangle(demon.attackTicks() - partialTick, 10)
                : (-0.2F + stride) * walk;
    }

    private static float triangle(float time, float period) {
        return (Math.abs(time % period - period * 0.5F) - period * 0.25F) / (period * 0.25F);
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer vertices, int light, int overlay,
                               float red, float green, float blue, float alpha) {
        root.render(pose, vertices, light, overlay, red, green, blue, alpha);
    }
}
