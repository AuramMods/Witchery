package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.item.BarkBeltItem;
import art.arcane.witchery.item.WitchClothingItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

public final class BarkBeltLayer<T extends LivingEntity, M extends HumanoidModel<T>> extends RenderLayer<T, M> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "bark_belt"), "bark");
    private static final RenderType TEXTURE = RenderType.entityCutoutNoCull(
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/entities/witchclothes2_legs.png"));

    private final ModelPart root;

    public BarkBeltLayer(RenderLayerParent<T, M> parent, ModelPart root) {
        super(parent);
        this.root = root;
    }

    public static LayerDefinition layer() {
        var mesh = new MeshDefinition();
        var root = mesh.getRoot();
        var trunk = root.addOrReplaceChild("trunk", CubeListBuilder.create(), PartPose.ZERO);
        trunk.addOrReplaceChild("front", CubeListBuilder.create().mirror().texOffs(111, 0)
                .addBox(0, 0, 0, 6, 9, 1), PartPose.offsetAndRotation(-2.5F, 1, -3.1F, 0, 0, 0.1487144F));
        trunk.addOrReplaceChild("back", CubeListBuilder.create().mirror().texOffs(111, 0)
                .addBox(0, 0, 0, 6, 9, 1), PartPose.offsetAndRotation(-2.5F, 1, 2.1F, 0, 0, 0.0887144F));
        var spikes = root.addOrReplaceChild("spikes", CubeListBuilder.create(), PartPose.ZERO);
        var spike = CubeListBuilder.create().mirror().texOffs(120, 0).addBox(-0.5F, -6, -0.5F, 1, 6, 1);
        spikes.addOrReplaceChild("lower_left", spike, PartPose.offsetAndRotation(1, 7, 2, -0.7807508F, 0.1858931F, 0));
        spikes.addOrReplaceChild("lower_right", spike, PartPose.offsetAndRotation(-1, 7, 2, -0.7807508F, -0.1858931F, 0));
        spikes.addOrReplaceChild("upper_left", spike, PartPose.offsetAndRotation(2, 3, 2, -0.7807508F, 0.1858931F, 0));
        spikes.addOrReplaceChild("upper_right", spike, PartPose.offsetAndRotation(-2, 3, 2, -0.7807508F, -0.1858931F, 0));
        addLeg(mesh, "right", -2.1F, -0.2230717F);
        addLeg(mesh, "left", -0.9F, 0.1230717F);
        addArm(mesh, "right", -3, 1, 7, -0.1487144F, -4, -1, -0.1858931F);
        addArm(mesh, "left", 0, 0, 6, 0.0687144F, 3, -1.5F, 0.0371786F);
        addShoulder(mesh, "right", -4, -3.5F, -2.5F, 0.0371786F, -0.1115358F, -0.1230717F);
        addShoulder(mesh, "left", -1, -4.5F, -1.5F, 0, 0.2974289F, 0.1830717F);
        return LayerDefinition.create(mesh, 128, 64);
    }

    private static void addLeg(MeshDefinition mesh, String side, float x, float tilt) {
        var leg = mesh.getRoot().addOrReplaceChild(side + "_leg", CubeListBuilder.create(), PartPose.ZERO);
        var bark = CubeListBuilder.create().mirror().texOffs(95, 0).addBox(0, 0, 0, 2, 7, 1);
        leg.addOrReplaceChild("front", bark, PartPose.offsetAndRotation(x, 1, -3, 0, 0, tilt));
        leg.addOrReplaceChild("back", bark, PartPose.offsetAndRotation(x, 1, 2, 0, 0, tilt));
    }

    private static void addArm(MeshDefinition mesh, String side, float x, float y, int height, float tilt,
                               float outerX, float outerZ, float outerTilt) {
        var arm = mesh.getRoot().addOrReplaceChild(side + "_arm", CubeListBuilder.create(), PartPose.ZERO);
        var bark = CubeListBuilder.create().mirror().texOffs(102, 0).addBox(0, 0, 0, 3, height, 1);
        arm.addOrReplaceChild("front", bark, PartPose.offsetAndRotation(x, y, -3, 0, 0, tilt));
        arm.addOrReplaceChild("back", bark, PartPose.offsetAndRotation(x, y, 2, 0, 0, tilt));
        arm.addOrReplaceChild("outer", CubeListBuilder.create().mirror().texOffs(120, 0)
                .addBox(0, 0, 0, 1, height, 3), PartPose.offsetAndRotation(outerX, 0, outerZ, outerTilt, 0, 0));
    }

    private static void addShoulder(MeshDefinition mesh, String side, float x, float y, float z,
                                    float pitch, float yaw, float roll) {
        var head = mesh.getRoot().addOrReplaceChild(side + "_shoulder", CubeListBuilder.create(), PartPose.ZERO);
        head.addOrReplaceChild("pad", CubeListBuilder.create().mirror().texOffs(108, 0)
                .addBox(0, 0, 0, 5, 1, 5), PartPose.offsetAndRotation(x, y, z, pitch, yaw, roll));
        head.addOrReplaceChild("spike", CubeListBuilder.create().mirror().texOffs(124, 0)
                .addBox(-0.5F, -5, -0.5F, 1, 5, 1),
                PartPose.offsetAndRotation(x < -1 ? 1 : -1, -2, 0, -0.1487144F, 0, x < -1 ? -0.4089647F : 0.4089647F));
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffers, int light, T wearer,
                       float limbSwing, float limbSwingAmount, float partialTick, float age, float yaw, float pitch) {
        int charges = BarkBeltItem.visibleCharges(wearer);
        if (charges == 0 || wearer.isInvisible()) return;
        var model = getParentModel();
        var buffer = buffers.getBuffer(TEXTURE);
        renderPiece("trunk", model.body, pose, buffer, light);
        if (charges >= 2) renderPiece("spikes", model.body, pose, buffer, light);
        if (WitchClothingItem.style(wearer.getItemBySlot(EquipmentSlot.FEET)) != null) {
            if (charges >= 3) renderPiece("right_leg", model.rightLeg, pose, buffer, light);
            if (charges >= 4) renderPiece("left_leg", model.leftLeg, pose, buffer, light);
            charges -= 2;
        }
        if (WitchClothingItem.style(wearer.getItemBySlot(EquipmentSlot.CHEST)) != null) {
            if (charges >= 3) renderPiece("right_arm", model.rightArm, pose, buffer, light);
            if (charges >= 4) renderPiece("left_arm", model.leftArm, pose, buffer, light);
            charges -= 2;
        }
        if (WitchClothingItem.style(wearer.getItemBySlot(EquipmentSlot.HEAD)) != null) {
            if (charges >= 3) renderPiece("right_shoulder", model.rightArm, pose, buffer, light);
            if (charges >= 4) renderPiece("left_shoulder", model.leftArm, pose, buffer, light);
        }
    }

    private void renderPiece(String piece, ModelPart anchor, PoseStack pose, VertexConsumer buffer, int light) {
        pose.pushPose();
        anchor.translateAndRotate(pose);
        root.getChild(piece).render(pose, buffer, light, OverlayTexture.NO_OVERLAY);
        pose.popPose();
    }
}
