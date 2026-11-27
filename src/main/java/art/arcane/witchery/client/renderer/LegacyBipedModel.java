package art.arcane.witchery.client.renderer;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.Mob;

class LegacyBipedModel<T extends Mob> extends HumanoidModel<T> {
    LegacyBipedModel(ModelPart root) {
        super(root);
    }

    @Override
    public void prepareMobModel(T mob, float swing, float amount, float partialTick) {
        prepareHeldItemPose(this, mob);
        super.prepareMobModel(mob, swing, amount, partialTick);
    }

    static void prepareHeldItemPose(HumanoidModel<?> model, Mob mob) {
        ArmPose main = mob.getMainHandItem().isEmpty() ? ArmPose.EMPTY : ArmPose.ITEM;
        ArmPose off = mob.getOffhandItem().isEmpty() ? ArmPose.EMPTY : ArmPose.ITEM;
        boolean rightHanded = mob.getMainArm() == HumanoidArm.RIGHT;
        model.rightArmPose = rightHanded ? main : off;
        model.leftArmPose = rightHanded ? off : main;
        model.crouching = mob.isShiftKeyDown();
    }

    @Override
    public void setupAnim(T mob, float swing, float amount, float age, float yaw, float pitch) {
        super.setupAnim(mob, swing, amount, age, yaw, pitch);
        head.y = crouching ? 1 : 0;
        body.y = 0;
        rightArm.y = leftArm.y = 2;
        rightLeg.y = leftLeg.y = crouching ? 9 : 12;
        rightLeg.z = leftLeg.z = crouching ? 4 : 0.1F;
        rightLeg.zRot = leftLeg.zRot = 0;
        rightLeg.yRot = riding ? Mth.PI / 10 : 0;
        leftLeg.yRot = -rightLeg.yRot;
        if (riding) rightLeg.xRot = leftLeg.xRot = -Mth.PI * 0.4F;
        hat.copyFrom(head);
    }
}
