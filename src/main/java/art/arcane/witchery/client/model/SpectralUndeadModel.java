package art.arcane.witchery.client.model;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.SpectralUndead;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class SpectralUndeadModel extends HierarchicalModel<SpectralUndead> {
    public static final ModelLayerLocation ROBED_LAYER = layerId("spectre");
    public static final ModelLayerLocation POLTERGEIST_LAYER = layerId("poltergeist");

    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final boolean poltergeist;
    private float opacity;

    public SpectralUndeadModel(ModelPart root, boolean poltergeist) {
        this.root = root;
        head = root.getChild("head");
        leftArm = root.getChild("left_arm");
        rightArm = root.getChild("right_arm");
        this.poltergeist = poltergeist;
    }

    private static ModelLayerLocation layerId(String name) {
        return new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Witchery.MODID, name), "main");
    }

    public static LayerDefinition robedLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = part(root, "head", 0, 16, -4, -8, -4, 8, 8, 8, 0, 2, 0);
        part(head, "mouth", 56, 0, 0, 0, 0, 4, 5, 0, -2, -4, -4.02F);
        part(root, "body", 16, 0, -4, 0, -2, 8, 10, 4, 0, 2, 0);
        part(root, "right_arm", 0, 0, -3, -2, -2, 4, 12, 4, -5, 4, 0);
        part(root, "left_arm", 0, 0, -1, -2, -2, 4, 12, 4, 5, 4, 0);
        part(root, "robe_upper", 38, 9, -4, 0, -2, 8, 6, 5, 0, 12, 0);
        part(root, "robe_lower", 32, 20, -5, 0, -2, 10, 6, 6, 0, 18, 0);
        return LayerDefinition.create(mesh, 64, 32);
    }

    public static LayerDefinition poltergeistLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        part(root, "head", 0, 0, -4, -8, -3, 8, 8, 6, 0, 0, 0);
        part(root, "body", 16, 16, -4, 0, -1, 8, 11, 2, 0, 0, 0);
        part(root, "right_arm", 40, 0, -1, -2, -1, 2, 18, 2, -5, 2, 0);
        part(root, "left_arm", 40, 0, -1, -2, -1, 2, 18, 2, 5, 2, 0);
        part(root, "right_second_arm", 40, 0, -1, -2, -1, 2, 18, 2, -5, 2, 0);
        part(root, "left_second_arm", 40, 0, -1, -2, -1, 2, 18, 2, 5, 2, 0);
        part(root, "right_leg", 0, 16, -1, 0, -1, 2, 13, 2, -2, 11, 0.1F);
        part(root, "left_leg", 0, 16, -1, 0, -1, 2, 13, 2, 2, 11, 0.1F);
        return LayerDefinition.create(mesh, 64, 32);
    }

    private static PartDefinition part(PartDefinition parent, String name, int u, int v, float x, float y, float z,
                                       int width, int height, int depth, float anchorX, float anchorY, float anchorZ) {
        return parent.addOrReplaceChild(name, CubeListBuilder.create().texOffs(u, v).mirror().addBox(x, y, z, width, height, depth),
                PartPose.offset(anchorX, anchorY, anchorZ));
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(SpectralUndead mob, float walk, float speed, float age, float yaw, float pitch) {
        head.yRot = yaw * Mth.DEG_TO_RAD;
        head.xRot = pitch * Mth.DEG_TO_RAD;
        opacity = switch (mob.kind()) {
            case SPECTRE -> mob.obscured() ? 0.15F : 0.6F;
            case BANSHEE -> 0.7F;
            case POLTERGEIST -> 0.4F;
        };
        float idle = Mth.sin(age * 0.067F) * 0.05F;
        if (poltergeist) {
            animatePoltergeist(mob, walk, speed, age, idle);
        } else {
            boolean screaming = mob.screaming();
            float reach = mob.kind() == SpectralUndead.Kind.SPECTRE ? -1.5F : -0.2F;
            rightArm.xRot = reach + idle;
            leftArm.xRot = reach - idle;
            rightArm.zRot = screaming ? 1 : 0;
            leftArm.zRot = -rightArm.zRot;
            head.getChild("mouth").visible = screaming;
        }
    }

    private void animatePoltergeist(SpectralUndead mob, float walk, float speed, float age, float idle) {
        float swing = Mth.cos(walk * 0.6662F) * speed;
        float sway = Mth.cos(age * 0.09F) * 0.05F + 0.05F;
        ModelPart rightSecond = root.getChild("right_second_arm");
        ModelPart leftSecond = root.getChild("left_second_arm");
        rightArm.xRot = -swing + idle;
        leftArm.xRot = swing - idle;
        rightSecond.xRot = -swing * 0.5F - idle;
        leftSecond.xRot = swing * 0.5F + idle;
        rightArm.zRot = leftSecond.zRot = sway;
        leftArm.zRot = rightSecond.zRot = -sway;
        root.getChild("right_leg").xRot = swing * 1.4F;
        root.getChild("left_leg").xRot = -swing * 1.4F;
        if (mob.throwAnimation() > 0) {
            float partial = age - mob.tickCount;
            float wave = (Math.abs((mob.throwAnimation() - partial) % 15 - 7.5F) - 3.75F) / 3.75F;
            rightArm.xRot = leftArm.xRot = rightSecond.xRot = leftSecond.xRot = -1.5F + 0.8F * wave;
            rightArm.zRot = 1.5F - 1.5F * wave;
            leftArm.zRot = -rightArm.zRot;
        }
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer buffer, int packedLight, int overlay,
                               float red, float green, float blue, float alpha) {
        root.render(pose, buffer, packedLight, overlay, red, green, blue, alpha * opacity);
    }
}
