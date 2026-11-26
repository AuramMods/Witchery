package art.arcane.witchery.client.model;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

final class LegacyBipedMotion {
    private LegacyBipedMotion() {}

    static void ride(ModelPart rightArm, ModelPart leftArm, ModelPart rightLeg, ModelPart leftLeg) {
        rightArm.xRot -= Mth.PI / 5;
        leftArm.xRot -= Mth.PI / 5;
        rightLeg.xRot = leftLeg.xRot = -2 * Mth.PI / 5;
        rightLeg.yRot = Mth.PI / 10;
        leftLeg.yRot = -rightLeg.yRot;
    }

    static void swing(ModelPart torso, ModelPart rightArm, ModelPart leftArm, float headPitch, float progress) {
        torso.yRot = Mth.sin(Mth.sqrt(progress) * Mth.TWO_PI) * 0.2F;
        rightArm.x = -Mth.cos(torso.yRot) * 5;
        leftArm.x = -rightArm.x;
        rightArm.z = Mth.sin(torso.yRot) * 5;
        leftArm.z = -rightArm.z;
        rightArm.yRot = torso.yRot * 3;
        leftArm.yRot = torso.yRot;
        leftArm.xRot += torso.yRot;

        float remaining = 1 - progress;
        remaining *= remaining;
        remaining *= remaining;
        float stroke = Mth.sin((1 - remaining) * Mth.PI);
        float follow = Mth.sin(progress * Mth.PI);
        rightArm.xRot -= stroke * 1.2F + follow * (0.7F - headPitch) * 0.75F;
        rightArm.zRot = -follow * 0.4F;
    }

    static void sway(ModelPart rightArm, ModelPart leftArm, float age) {
        float roll = Mth.cos(age * 0.09F) * 0.05F + 0.05F;
        rightArm.zRot += roll;
        leftArm.zRot -= roll;
        float bob = Mth.sin(age * 0.067F) * 0.05F;
        rightArm.xRot += bob;
        leftArm.xRot -= bob;
    }
}
