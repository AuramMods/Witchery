package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.config.WitcheryClientConfig;
import art.arcane.witchery.entity.HornedHuntsman;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.GraphicsStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class HornedHuntsmanRenderer extends MobRenderer<HornedHuntsman, HornedHuntsmanRenderer.Model> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "hornedhuntsman"), "main");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/entities/avatar.png");

    public HornedHuntsmanRenderer(EntityRendererProvider.Context context) {
        super(context, new Model(context.bakeLayer(LAYER)), 0.5F);
        addLayer(new RenderLayer<>(this) {
            @Override
            public void render(PoseStack pose, MultiBufferSource buffers, int light, HornedHuntsman huntsman,
                               float swing, float amount, float partialTick, float age, float yaw, float pitch) {
                if (!huntsman.isInvisible() && Minecraft.getInstance().options.graphicsMode().get() != GraphicsStatus.FAST
                        && WitcheryClientConfig.RENDER_HUNTSMAN_GLINT.get()) {
                    getParentModel().renderToBuffer(pose, buffers.getBuffer(RenderType.entityGlint()), light,
                            OverlayTexture.NO_OVERLAY, 1, 1, 1, 1);
                }
            }
        });
    }

    @Override
    public ResourceLocation getTextureLocation(HornedHuntsman huntsman) { return TEXTURE; }

    @Override
    protected void setupRotations(HornedHuntsman huntsman, PoseStack pose, float age, float yaw, float partialTick) {
        super.setupRotations(huntsman, pose, age, yaw, partialTick);
        if (huntsman.walkAnimation.speed(partialTick) >= 0.01F) {
            pose.mulPose(Axis.ZP.rotationDegrees(5.5F * Mth.triangleWave(huntsman.walkAnimation.position(partialTick) + 6, 13)));
        }
    }

    public static LayerDefinition layer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        var head = root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(4, 112).addBox(-4, -8, -4, 8, 8, 8),
                PartPose.offset(0, -16, 0));
        head.addOrReplaceChild("horns", CubeListBuilder.create().texOffs(0, 88).addBox(-10, -24, 0, 20, 17, 0), PartPose.ZERO);
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(12, 61).addBox(-6, 0, -3, 12, 8, 6),
                PartPose.offset(0, -8, 0));
        root.addOrReplaceChild("chest", CubeListBuilder.create().texOffs(0, 43).addBox(0, 0, 0, 20, 8, 10),
                PartPose.offset(-10, -16, -5));
        root.addOrReplaceChild("hips", CubeListBuilder.create().texOffs(8, 75).addBox(-7, 0, -4, 14, 4, 8), PartPose.ZERO);
        var rightForearm = addArm(root, "right_arm", -1);
        rightForearm.addOrReplaceChild("spear", CubeListBuilder.create().mirror()
                .texOffs(61, 14).addBox(-0.5F, -30, -0.5F, 1, 50, 1)
                .texOffs(60, 8).addBox(-1.5F, -35, 0, 3, 6, 0)
                .texOffs(60, 5).addBox(0, -35, -1.5F, 0, 6, 3),
                PartPose.offsetAndRotation(0, 12, 0, 1.5F, 0, 0));
        addArm(root, "left_arm", 1);
        addLeg(root, "right_leg", -4);
        addLeg(root, "left_leg", 4);
        return LayerDefinition.create(mesh, 128, 128);
    }

    private static PartDefinition addArm(PartDefinition root, String name, int side) {
        var arm = root.addOrReplaceChild(name, CubeListBuilder.create().texOffs(72, 50)
                .addBox(side < 0 ? -4 : 0, -2, -2, 4, 13, 4), PartPose.offset(10 * side, -13, 0));
        return arm.addOrReplaceChild("forearm", CubeListBuilder.create().texOffs(68, 67).addBox(-3, 0, -3, 6, 14, 6),
                PartPose.offsetAndRotation(2 * side, 10, 0, -0.5235988F, 0, 0));
    }

    private static void addLeg(PartDefinition root, String name, int x) {
        var leg = root.addOrReplaceChild(name, CubeListBuilder.create().texOffs(72, 0).addBox(-2, 0, -2, 4, 10, 4),
                PartPose.offsetAndRotation(x, 3, -1, 0.5F, 0, 0));
        var shin = leg.addOrReplaceChild("shin", CubeListBuilder.create().texOffs(68, 14).addBox(-3, -2, -3, 6, 14, 6),
                PartPose.offsetAndRotation(0, 10, 0, -1, 0, 0));
        shin.addOrReplaceChild("foot", CubeListBuilder.create().texOffs(69, 34).addBox(-2, 0, -5, 4, 3, 7),
                PartPose.offsetAndRotation(0, 10, 0, 0.5F, 0, 0));
    }

    static final class Model extends HierarchicalModel<HornedHuntsman> {
        private final ModelPart root;
        private final ModelPart head;
        private final ModelPart rightArm;
        private final ModelPart leftArm;
        private final ModelPart rightLeg;
        private final ModelPart leftLeg;

        Model(ModelPart root) {
            this.root = root;
            head = root.getChild("head");
            rightArm = root.getChild("right_arm");
            leftArm = root.getChild("left_arm");
            rightLeg = root.getChild("right_leg");
            leftLeg = root.getChild("left_leg");
        }

        @Override
        public ModelPart root() { return root; }

        @Override
        public void setupAnim(HornedHuntsman huntsman, float swing, float amount, float age, float yaw, float pitch) {
            head.yRot = yaw * Mth.DEG_TO_RAD;
            head.xRot = pitch * Mth.DEG_TO_RAD;
            float stride = Mth.triangleWave(swing, 13) * amount;
            stride(rightLeg, stride);
            stride(leftLeg, -stride);
            rightArm.xRot = -0.04F * amount + 0.3F * stride;
            leftArm.xRot = -0.04F * amount - 0.3F * stride;
            if (huntsman.attackTicks() > 0) {
                rightArm.xRot = -2 + 1.5F * Mth.triangleWave(huntsman.attackTicks() - (age - huntsman.tickCount), 10);
            }
        }

        private static void stride(ModelPart leg, float step) {
            leg.xRot = 0.5F + 1.3F * step;
            var shin = leg.getChild("shin");
            shin.xRot = -1 - 1.04F * step;
            shin.getChild("foot").xRot = Math.max(0.5F + 1.82F * step, 0.2F);
        }
    }
}
