package art.arcane.witchery.client.model;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Mob;

abstract class HobgoblinBipedModel<T extends Mob> extends HumanoidModel<T> {
    HobgoblinBipedModel(ModelPart root) {
        super(root);
    }

    protected final void poseStride(T mob, float stride, float amount, float yaw, float pitch) {
        head.resetPose();
        body.resetPose();
        rightArm.resetPose();
        leftArm.resetPose();
        rightLeg.resetPose();
        leftLeg.resetPose();

        head.yRot = yaw * Mth.DEG_TO_RAD;
        head.xRot = pitch * Mth.DEG_TO_RAD;
        float step = stride * 0.6662F;
        float right = Mth.cos(step) * amount;
        float left = Mth.cos(step + Mth.PI) * amount;
        rightArm.xRot = left;
        leftArm.xRot = right;
        rightLeg.xRot = right * 1.4F;
        leftLeg.xRot = left * 1.4F;
        if (riding) {
            rightArm.xRot -= Mth.PI / 5;
            leftArm.xRot -= Mth.PI / 5;
            rightLeg.xRot = leftLeg.xRot = -Mth.PI * 0.4F;
            rightLeg.yRot = Mth.PI / 10;
            leftLeg.yRot = -rightLeg.yRot;
        }

        var mainArm = mob.getMainArm();
        if (!mob.getMainHandItem().isEmpty()) lowerHeldArm(mainArm);
        if (!mob.getOffhandItem().isEmpty()) lowerHeldArm(mainArm.getOpposite());
    }

    private void lowerHeldArm(HumanoidArm side) {
        ModelPart arm = getArm(side);
        arm.xRot = arm.xRot * 0.5F - Mth.PI / 10;
    }

    protected final void poseSwing(T mob) {
        HumanoidArm side = mob.swingingArm == InteractionHand.OFF_HAND ? mob.getMainArm().getOpposite() : mob.getMainArm();
        ModelPart strikingArm = getArm(side);
        ModelPart freeArm = getArm(side.getOpposite());
        float direction = side == HumanoidArm.RIGHT ? 1 : -1;
        body.yRot = direction * Mth.sin(Mth.sqrt(attackTime) * Mth.TWO_PI) * 0.2F;
        rightArm.z = Mth.sin(body.yRot) * 5;
        rightArm.x = -Mth.cos(body.yRot) * 5;
        leftArm.z = -rightArm.z;
        leftArm.x = -rightArm.x;
        rightArm.yRot = leftArm.yRot = body.yRot;
        freeArm.xRot += body.yRot * direction;

        float ease = 1 - attackTime;
        ease *= ease;
        ease *= ease;
        float swing = Mth.sin((1 - ease) * Mth.PI);
        float faceTarget = Mth.sin(attackTime * Mth.PI) * -(head.xRot - 0.7F) * 0.75F;
        strikingArm.xRot -= swing * 1.2F + faceTarget;
        strikingArm.yRot += body.yRot * 2;
        strikingArm.zRot = -direction * Mth.sin(attackTime * Mth.PI) * 0.4F;
    }

    protected final void swayArms(float age) {
        float roll = Mth.cos(age * 0.09F) * 0.05F + 0.05F;
        float pitch = Mth.sin(age * 0.067F) * 0.05F;
        rightArm.zRot += roll;
        leftArm.zRot -= roll;
        rightArm.xRot += pitch;
        leftArm.xRot -= pitch;
    }
}
