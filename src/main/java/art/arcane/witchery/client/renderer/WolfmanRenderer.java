package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.Wolfman;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
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
import net.minecraft.client.renderer.entity.layers.CustomHeadLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.UseAnim;

import java.util.List;
import java.util.function.ToIntFunction;

public final class WolfmanRenderer<T extends Mob> extends MobRenderer<T, WolfmanRenderer.Model<T>> {
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "wolfman");
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(MODEL, "main");
    public static final ModelLayerLocation INNER_ARMOR = new ModelLayerLocation(MODEL, "inner_armor");
    public static final ModelLayerLocation OUTER_ARMOR = new ModelLayerLocation(MODEL, "outer_armor");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/entities/wolfman.png");
    private static final float BODY_PITCH = 0.4098033F;
    private static final float THIGH_PITCH = -0.4098033F;
    private static final float TAIL_PITCH = 0.59184116F;

    public WolfmanRenderer(EntityRendererProvider.Context context, ToIntFunction<T> attackTicks) {
        super(context, new Model<>(context.bakeLayer(LAYER), attackTicks), 0.5F);
        addLayer(new CustomHeadLayer<>(this, context.getModelSet(), context.getItemInHandRenderer()));
        addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
        addLayer(new HumanoidArmorLayer<>(this, new Model<>(context.bakeLayer(INNER_ARMOR), attackTicks),
                new Model<>(context.bakeLayer(OUTER_ARMOR), attackTicks), context.getModelManager()));
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) { return TEXTURE; }

    public static LayerDefinition layer() { return layer(CubeDeformation.NONE, false); }

    public static LayerDefinition innerArmorLayer() { return layer(new CubeDeformation(0.5F), true); }

    public static LayerDefinition outerArmorLayer() { return layer(new CubeDeformation(1), true); }

    private static LayerDefinition layer(CubeDeformation deformation, boolean armor) {
        MeshDefinition mesh = new MeshDefinition();
        var root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0)
                        .addBox(-3, -6, -2, 6, 6, 4, deformation.extend(0.05F))
                        .texOffs(16, 14).addBox(-3, -8, 1, 2, 2, 1, deformation).addBox(1, -8, 1, 2, 2, 1, deformation)
                        .texOffs(0, 10).addBox(-1.5F, -3.1F, -5, 3, 3, 4, deformation), PartPose.offset(0, 0, -2));
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        var chest = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 35)
                        .addBox(-5, 0, -3.9F, 10, 7, 8, deformation), PartPose.offsetAndRotation(0, -0.1F, -2, BODY_PITCH, 0, 0));
        chest.addOrReplaceChild("belly", CubeListBuilder.create().texOffs(3, 50).addBox(-4, 2, -2.3F, 8, 7, 5, deformation),
                PartPose.offset(0, 5, -1.5F));
        var tail = CubeListBuilder.create();
        if (!armor) tail.texOffs(55, 52).addBox(-1, 0, -1, 2, 10, 2);
        root.addOrReplaceChild("tail", tail, PartPose.offsetAndRotation(0, 11.9F, 3.6F, TAIL_PITCH, 0, 0));
        root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(38, 46).addBox(-3, -2, -2, 4, 14, 4, deformation),
                PartPose.offset(-5, 2, 0));
        root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(38, 46).mirror().addBox(-1, -2, -2, 4, 14, 4, deformation),
                PartPose.offset(5, 2, 0));
        addLeg(root, "right_leg", -2, false, deformation);
        addLeg(root, "left_leg", 2, true, deformation);
        return LayerDefinition.create(mesh, 64, 64);
    }

    private static void addLeg(PartDefinition root, String name, float x, boolean mirror, CubeDeformation deformation) {
        var thigh = root.addOrReplaceChild(name, CubeListBuilder.create().texOffs(38, 0).mirror(mirror)
                .addBox(-2, 0, -2, 4, 7, 4, deformation), PartPose.offsetAndRotation(x, 12, 0.1F, THIGH_PITCH, 0, 0));
        thigh.addOrReplaceChild("shin", CubeListBuilder.create().texOffs(38, 13).addBox(-2, 3.5F, 2, 4, 8, 4, deformation), PartPose.ZERO);
    }

    static final class Model<T extends Mob> extends HumanoidModel<T> {
        private final ModelPart tail;
        private final List<ModelPart> bodyParts;
        private final ToIntFunction<T> attackTicks;

        Model(ModelPart root, ToIntFunction<T> attackTicks) {
            super(root);
            this.attackTicks = attackTicks;
            tail = root.getChild("tail");
            bodyParts = List.of(body, rightArm, leftArm, rightLeg, leftLeg, tail);
        }

        @Override
        protected Iterable<ModelPart> bodyParts() { return bodyParts; }

        @Override
        public void translateToHand(HumanoidArm arm, PoseStack pose) {
            super.translateToHand(arm, pose);
            pose.translate(0, 0.13, 0);
        }

        @Override
        public void setupAnim(T wolfman, float swing, float amount, float age, float yaw, float pitch) {
            head.yRot = yaw * Mth.DEG_TO_RAD;
            head.xRot = pitch * Mth.DEG_TO_RAD;
            float walk = Mth.cos(swing * 0.6662F) * amount;
            rightLeg.xRot = Math.max(THIGH_PITCH + walk * 1.4F, -0.8F);
            leftLeg.xRot = Math.max(THIGH_PITCH - walk * 1.4F, -0.8F);
            rightLeg.yRot = leftLeg.yRot = 0;
            rightArm.xRot = -walk;
            leftArm.xRot = walk;
            rightArm.zRot = leftArm.zRot = 0;
            HumanoidArm strikeArm = wolfman.swingingArm == InteractionHand.OFF_HAND
                    ? wolfman.getMainArm().getOpposite() : wolfman.getMainArm();
            int attack = attackTicks.applyAsInt(wolfman);
            if (attack > 0) {
                float strike = Mth.triangleWave(attack - (age - wolfman.tickCount), 10);
                getArm(strikeArm).xRot = -2 + 1.5F * strike;
                getArm(strikeArm.getOpposite()).xRot = -1 + 0.9F * strike;
            }

            if (riding || wolfman instanceof Wolfman proxy && proxy.isPlayerRiding()) {
                rightLeg.xRot = leftLeg.xRot = -Mth.PI * 0.4F;
                rightLeg.yRot = Mth.PI * 0.1F;
                leftLeg.yRot = -rightLeg.yRot;
                rightArm.xRot -= Mth.PI * 0.2F;
                leftArm.xRot -= Mth.PI * 0.2F;
            }

            InteractionHand rightHand = wolfman.getMainArm() == HumanoidArm.RIGHT ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
            InteractionHand leftHand = rightHand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            InteractionHand activeHand = wolfman.isUsingItem() ? wolfman.getUsedItemHand()
                    : wolfman instanceof Wolfman proxy ? proxy.playerUseHand() : null;
            UseAnim use = activeHand == null ? UseAnim.NONE : wolfman.getItemInHand(activeHand).getUseAnimation();
            if (!wolfman.getItemInHand(rightHand).isEmpty()) hold(rightArm, activeHand == rightHand && use == UseAnim.BLOCK);
            if (!wolfman.getItemInHand(leftHand).isEmpty()) hold(leftArm, activeHand == leftHand && use == UseAnim.BLOCK);

            float swingYaw = swingArms(strikeArm);
            crouching = wolfman.isShiftKeyDown();
            body.xRot = crouching ? 0.5F : BODY_PITCH;
            body.yRot = crouching ? swingYaw : 0;
            rightLeg.y = leftLeg.y = crouching ? 9 : 12;
            rightLeg.z = leftLeg.z = crouching ? 4 : 0.1F;
            if (crouching) {
                rightArm.xRot += 0.4F;
                leftArm.xRot += 0.4F;
            }

            float idleRoll = Mth.cos(age * 0.09F) * 0.05F;
            boolean moving = amount > 0.1F;
            tail.xRot = TAIL_PITCH + (moving ? amount - 0.1F : 0);
            tail.zRot = (moving ? 5 : 3) * idleRoll + 0.05F;
            if (use == UseAnim.BOW) {
                aimBow(activeHand == rightHand ? HumanoidArm.RIGHT : HumanoidArm.LEFT);
            }
            float idlePitch = Mth.sin(age * 0.067F) * 0.05F;
            rightArm.xRot += idlePitch;
            leftArm.xRot -= idlePitch;
            rightArm.zRot += idleRoll + 0.05F;
            leftArm.zRot -= idleRoll + 0.05F;
        }

        private static void hold(ModelPart arm, boolean blocking) {
            arm.xRot = arm.xRot * 0.5F - Mth.PI * (blocking ? 0.3F : 0.1F);
        }

        private float swingArms(HumanoidArm strikeArm) {
            float side = strikeArm == HumanoidArm.RIGHT ? 1 : -1;
            float yaw = Mth.sin(Mth.sqrt(attackTime) * Mth.TWO_PI) * 0.2F * side;
            float armX = Mth.cos(yaw) * 5;
            float armZ = Mth.sin(yaw) * 5;
            rightArm.x = -armX;
            leftArm.x = armX;
            rightArm.z = armZ;
            leftArm.z = -armZ;
            rightArm.yRot = leftArm.yRot = yaw;

            var striking = getArm(strikeArm);
            var other = getArm(strikeArm.getOpposite());
            float remaining = 1 - attackTime;
            remaining *= remaining;
            float swingPitch = Mth.sin((1 - remaining * remaining) * Mth.PI);
            float swingRoll = Mth.sin(attackTime * Mth.PI);
            striking.xRot -= 1.2F * swingPitch - swingRoll * (head.xRot - 0.7F) * 0.75F;
            striking.yRot += yaw * 2;
            striking.zRot = -0.4F * swingRoll * side;
            other.xRot += yaw * side;
            return yaw;
        }

        private void aimBow(HumanoidArm drawingArm) {
            float side = drawingArm == HumanoidArm.RIGHT ? 1 : -1;
            getArm(drawingArm).yRot = head.yRot - 0.1F * side;
            getArm(drawingArm.getOpposite()).yRot = head.yRot + 0.5F * side;
            rightArm.xRot = leftArm.xRot = head.xRot - Mth.HALF_PI;
            rightArm.zRot = leftArm.zRot = 0;
        }
    }
}
