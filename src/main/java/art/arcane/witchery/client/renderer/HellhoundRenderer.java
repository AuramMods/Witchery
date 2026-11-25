package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.Hellhound;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class HellhoundRenderer extends MobRenderer<Hellhound, HellhoundRenderer.Model> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "hellhound"), "main");
    static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/entities/hellhound.png");

    public HellhoundRenderer(EntityRendererProvider.Context context) {
        super(context, new Model(context.bakeLayer(LAYER)), 0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(Hellhound entity) { return TEXTURE; }

    public static LayerDefinition layer() {
        MeshDefinition mesh = new MeshDefinition();
        var root = mesh.getRoot();
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(18, 17).addBox(-4, -2, -3, 6, 9, 6),
                PartPose.offsetAndRotation(0, 12.5F, 2, 1.1838568F, 0, 0));
        root.addOrReplaceChild("mane", CubeListBuilder.create().texOffs(22, 0).addBox(-4, -3, -3, 8, 8, 9),
                PartPose.offsetAndRotation(-1, 14, -3, Mth.HALF_PI, 0, 0));
        root.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(9, 19).addBox(-1, 0, -1, 2, 6, 2),
                PartPose.offset(-1, 13, 8));
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-3, -3, -2, 6, 6, 4)
                        .texOffs(16, 14).addBox(-3, -5, 1, 2, 2, 1).addBox(1, -5, 1, 2, 2, 1)
                        .texOffs(0, 10).addBox(-1.5F, 0, -5, 3, 3, 4), PartPose.offset(-1, 10.6F, -7.5F));
        addLeg(root, "left_hind_leg", 0.5F, 7);
        addLeg(root, "right_hind_leg", -2.5F, 7);
        addLeg(root, "left_front_leg", 0.5F, -4);
        addLeg(root, "right_front_leg", -2.5F, -4);
        return LayerDefinition.create(mesh, 64, 32);
    }

    private static void addLeg(PartDefinition root, String name, float x, float z) {
        root.addOrReplaceChild(name, CubeListBuilder.create().texOffs(0, 19).addBox(-1, 0, -1, 2, 8, 2),
                PartPose.offset(x, 16, z));
    }

    static final class Model extends HierarchicalModel<Hellhound> {
        private final ModelPart root;
        private final ModelPart head;
        private final ModelPart mane;
        private final ModelPart body;
        private final ModelPart tail;
        private final ModelPart leftHindLeg;
        private final ModelPart rightHindLeg;
        private final ModelPart leftFrontLeg;
        private final ModelPart rightFrontLeg;

        Model(ModelPart root) {
            this.root = root;
            head = root.getChild("head");
            mane = root.getChild("mane");
            body = root.getChild("body");
            tail = root.getChild("tail");
            leftHindLeg = root.getChild("left_hind_leg");
            rightHindLeg = root.getChild("right_hind_leg");
            leftFrontLeg = root.getChild("left_front_leg");
            rightFrontLeg = root.getChild("right_front_leg");
        }

        @Override
        public ModelPart root() { return root; }

        @Override
        public void setupAnim(Hellhound hound, float swing, float amount, float age, float yaw, float pitch) {
            head.xRot = pitch * Mth.DEG_TO_RAD + (hound.isConverting() ? 0.5F : 0.15F);
            head.yRot = yaw * Mth.DEG_TO_RAD;
            tail.xRot = hound.isConverting() ? 2.0F : 0.59184116F;
            if (amount > 0.1) tail.xRot += 1.5F * amount - 0.1;
            float stride = swing * 0.6662F;
            leftHindLeg.xRot = rightFrontLeg.xRot = Mth.cos(stride + Mth.PI) * 1.4F * amount;
            rightHindLeg.xRot = leftFrontLeg.xRot = Mth.cos(stride) * 1.4F * amount;
            float partialTick = age - hound.tickCount;
            head.zRot = hound.shakeAngle(partialTick, 0);
            mane.zRot = hound.shakeAngle(partialTick, -0.08F);
            body.zRot = hound.shakeAngle(partialTick, -0.16F);
            tail.zRot = hound.shakeAngle(partialTick, -0.2F);
        }
    }
}
