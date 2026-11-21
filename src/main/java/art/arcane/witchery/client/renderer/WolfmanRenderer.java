package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.Wolfman;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Mob;

import java.util.function.ToIntFunction;

public final class WolfmanRenderer extends MobRenderer<Wolfman, WolfmanRenderer.Model<Wolfman>> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "wolfman"), "main");
    static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/entities/wolfman.png");
    private static final float THIGH_PITCH = -0.4098033F;

    public WolfmanRenderer(EntityRendererProvider.Context context) {
        super(context, new Model<>(context.bakeLayer(LAYER), Wolfman::attackTicks), 0.5F);
        addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(Wolfman entity) { return TEXTURE; }

    public static LayerDefinition layer() {
        MeshDefinition mesh = new MeshDefinition();
        var root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-3, -6, -2, 6, 6, 4, new CubeDeformation(0.05F))
                        .texOffs(16, 14).addBox(-3, -8, 1, 2, 2, 1).addBox(1, -8, 1, 2, 2, 1)
                        .texOffs(0, 10).addBox(-1.5F, -3.1F, -5, 3, 3, 4), PartPose.offset(0, 0, -2));
        var chest = root.addOrReplaceChild("chest", CubeListBuilder.create().texOffs(0, 35)
                        .addBox(-5, 0, -3.9F, 10, 7, 8), PartPose.offsetAndRotation(0, -0.1F, -2, 0.4098033F, 0, 0));
        chest.addOrReplaceChild("belly", CubeListBuilder.create().texOffs(3, 50).addBox(-4, 2, -2.3F, 8, 7, 5),
                PartPose.offset(0, 5, -1.5F));
        root.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(55, 52).addBox(-1, 0, -1, 2, 10, 2),
                PartPose.offsetAndRotation(0, 11.9F, 3.6F, 0.59184116F, 0, 0));
        root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(38, 46).addBox(-3, -2, -2, 4, 14, 4),
                PartPose.offset(-5.8F, 2, 0));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(38, 46).mirror().addBox(-1, -2, -2, 4, 14, 4),
                PartPose.offset(6, 2, 0));
        addLeg(root, "right_leg", -2, false);
        addLeg(root, "left_leg", 2, true);
        return LayerDefinition.create(mesh, 64, 64);
    }

    private static void addLeg(PartDefinition root, String name, float x, boolean mirror) {
        var thigh = root.addOrReplaceChild(name, CubeListBuilder.create().texOffs(38, 0).mirror(mirror)
                .addBox(-2, 0, -2, 4, 7, 4), PartPose.offsetAndRotation(x, 12, 0, THIGH_PITCH, 0, 0));
        thigh.addOrReplaceChild("shin", CubeListBuilder.create().texOffs(38, 13).addBox(-2, 3.5F, 2, 4, 8, 4), PartPose.ZERO);
    }

    static final class Model<T extends Mob> extends HierarchicalModel<T> implements ArmedModel {
        private final ModelPart root;
        private final ModelPart head;
        private final ModelPart rightArm;
        private final ModelPart leftArm;
        private final ModelPart rightLeg;
        private final ModelPart leftLeg;
        private final ToIntFunction<T> attackTicks;

        Model(ModelPart root, ToIntFunction<T> attackTicks) {
            this.root = root;
            this.attackTicks = attackTicks;
            head = root.getChild("head");
            rightArm = root.getChild("right_arm");
            leftArm = root.getChild("left_arm");
            rightLeg = root.getChild("right_leg");
            leftLeg = root.getChild("left_leg");
        }

        @Override
        public ModelPart root() { return root; }

        @Override
        public void translateToHand(HumanoidArm arm, PoseStack pose) {
            (arm == HumanoidArm.RIGHT ? rightArm : leftArm).translateAndRotate(pose);
            pose.translate(0, 0.13, 0);
        }

        @Override
        public void setupAnim(T wolfman, float swing, float amount, float age, float yaw, float pitch) {
            head.yRot = yaw * Mth.DEG_TO_RAD;
            head.xRot = pitch * Mth.DEG_TO_RAD;
            float walk = Mth.cos(swing * 0.6662F) * amount;
            rightLeg.xRot = Math.max(THIGH_PITCH + walk * 1.4F, -0.8F);
            leftLeg.xRot = Math.max(THIGH_PITCH - walk * 1.4F, -0.8F);
            rightArm.xRot = -walk;
            leftArm.xRot = walk;
            int attack = attackTicks.applyAsInt(wolfman);
            if (attack > 0) {
                float strike = Mth.triangleWave(attack - (age - wolfman.tickCount), 10);
                rightArm.xRot = -2 + 1.5F * strike;
                leftArm.xRot = -1 + 0.9F * strike;
            }
        }
    }
}
