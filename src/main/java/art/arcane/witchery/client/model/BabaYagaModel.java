package art.arcane.witchery.client.model;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.BabaYaga;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class BabaYagaModel extends HierarchicalModel<BabaYaga> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "babayaga"), "main");
    private final ModelPart root;
    private final ModelPart figure;
    private final ModelPart head;
    private final ModelPart nose;
    private final ModelPart pestle;

    public BabaYagaModel(ModelPart root) {
        this.root = root;
        figure = root.getChild("figure");
        head = figure.getChild("head");
        nose = head.getChild("nose");
        pestle = figure.getChild("pestle");
    }

    public static LayerDefinition layer() {
        MeshDefinition mesh = new MeshDefinition();
        var figure = mesh.getRoot().addOrReplaceChild("figure", CubeListBuilder.create(), PartPose.offset(0, -3.2F, 0));
        var head = figure.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-4, -10, -4, 8, 10, 8), PartPose.ZERO);
        var nose = head.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(24, 0)
                .addBox(-1, -1, -6, 2, 4, 2), PartPose.offset(0, -2, 0));
        nose.addOrReplaceChild("wart", CubeListBuilder.create().texOffs(0, 0)
                .addBox(0, 3, -6.75F, 1, 1, 1, new CubeDeformation(-0.15F)), PartPose.offset(0, -2, 0));
        var brim = head.addOrReplaceChild("brim", CubeListBuilder.create().texOffs(0, 98)
                .addBox(0, 0, 0, 14, 2, 14), PartPose.offset(-7, -10.03125F, -7));
        var crown = brim.addOrReplaceChild("crown", CubeListBuilder.create().texOffs(0, 76).addBox(0, 0, 0, 7, 4, 7),
                PartPose.offsetAndRotation(3.75F, -4, 4, -0.05235988F, 0, 0.02617994F));
        var top = crown.addOrReplaceChild("top", CubeListBuilder.create().texOffs(0, 87).addBox(0, 0, 0, 4, 4, 4),
                PartPose.offsetAndRotation(1.75F, -4, 2, -0.10471976F, 0, 0.05235988F));
        top.addOrReplaceChild("tip", CubeListBuilder.create().texOffs(0, 95)
                        .addBox(0, 0, 0, 1, 2, 1, new CubeDeformation(0.25F)),
                PartPose.offsetAndRotation(1.75F, -2, 2, -0.20943952F, 0, 0.10471976F));

        figure.addOrReplaceChild("body", CubeListBuilder.create().texOffs(16, 20).addBox(-4, 0, -3, 8, 12, 6)
                .texOffs(0, 38).addBox(-4, 0, -3, 8, 18, 6, new CubeDeformation(0.5F)), PartPose.ZERO);
        figure.addOrReplaceChild("arms", CubeListBuilder.create().texOffs(44, 22).addBox(-8, -2, -2, 4, 8, 4)
                .texOffs(44, 22).mirror().addBox(4, -2, -2, 4, 8, 4).mirror(false)
                .texOffs(40, 38).addBox(-4, 2, -2, 8, 4, 4), PartPose.offsetAndRotation(0, 3, -1, -0.75F, 0, 0));
        figure.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 22).addBox(-2, 0, -2, 4, 12, 4),
                PartPose.offset(-2, 12, 0));
        figure.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 22).mirror().addBox(-2, 0, -2, 4, 12, 4),
                PartPose.offset(2, 12, 0));
        figure.addOrReplaceChild("cloak", CubeListBuilder.create().texOffs(94, 0).addBox(0, 0, 0, 8, 10, 0),
                PartPose.offsetAndRotation(-3.5F, -0.5F, 3.5F, 0.1F, 0, 0));
        figure.addOrReplaceChild("mortar", CubeListBuilder.create().mirror().texOffs(80, 88).addBox(1, 7, 2, 12, 7, 12)
                .texOffs(72, 107).addBox(0, 0, 1, 14, 7, 14), PartPose.offset(-7, 10, -8));
        figure.addOrReplaceChild("pestle", CubeListBuilder.create().mirror().texOffs(124, 0).addBox(-1, -7, 0, 1, 12, 1)
                        .texOffs(116, 13).addBox(-2, 5, -1, 3, 12, 3),
                PartPose.offsetAndRotation(-3, 6, -4, -1.152537F, -2.305074F, 1.839205F));
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(BabaYaga baba, float limbSwing, float limbAmount, float age, float yaw, float pitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        head.yRot = yaw * Mth.DEG_TO_RAD;
        head.xRot = pitch * Mth.DEG_TO_RAD;
        float phase = baba.tickCount * (0.01F * (baba.getId() % 10));
        float sway = Mth.sin(phase) * 4.5F * Mth.DEG_TO_RAD;
        float wobble = Mth.cos(phase) * 2.5F * Mth.DEG_TO_RAD;
        nose.xRot = sway;
        nose.zRot = wobble;
        if (!baba.getMainHandItem().isEmpty()) {
            nose.xRot = -0.9F;
            nose.y += 3;
            nose.z -= 1.5F;
        }
        pestle.xRot += sway;
        pestle.zRot += wobble;
    }

    public void translateToBottle(PoseStack pose) {
        figure.translateAndRotate(pose);
        head.translateAndRotate(pose);
        nose.translateAndRotate(pose);
        pose.translate(-0.0625F, 0.53125F, 0.21875F);
    }
}
