package art.arcane.witchery.client.model;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.Ent;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class EntModel extends HierarchicalModel<Ent> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "ent"), "main");
    private static final float[] ROOT_PHASE = {0, Mth.PI, Mth.HALF_PI, 3 * Mth.HALF_PI};
    private static final float[] ROOT_ANGLE = {2, 1, -1, -2};
    private final ModelPart root;
    private final ModelPart face;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart[] leftRoots = new ModelPart[4];
    private final ModelPart[] rightRoots = new ModelPart[4];
    private float partialTick;

    public EntModel(ModelPart root) {
        this.root = root;
        face = root.getChild("face");
        leftArm = root.getChild("left_arm");
        rightArm = root.getChild("right_arm");
        for (int i = 0; i < leftRoots.length; i++) {
            leftRoots[i] = root.getChild("left_root" + i);
            rightRoots[i] = root.getChild("right_root" + i);
        }
    }

    public static LayerDefinition layer() {
        MeshDefinition mesh = new MeshDefinition();
        var root = mesh.getRoot();
        root.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(82, 0).addBox(0, -22, -3, 6, 24, 6),
                PartPose.offset(8, -4, 0));
        root.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(82, 0).addBox(-6, -22, -3, 6, 24, 6),
                PartPose.offset(-8, -4, 0));
        root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 50).addBox(-8, -46, -8, 16, 48, 16),
                PartPose.offset(0, 20, 0));
        root.addOrReplaceChild("face", CubeListBuilder.create().texOffs(0, 116).addBox(-8, -46, -9, 16, 24, 16),
                PartPose.offset(0, 20, 0));
        root.addOrReplaceChild("inner_lower_leaves", CubeListBuilder.create().texOffs(24, 59).addBox(0, 0, 0, 56, 14, 56),
                PartPose.offset(-28, -41, -28));
        root.addOrReplaceChild("inner_upper_leaves", CubeListBuilder.create().texOffs(108, 14).addBox(0, 0, 0, 28, 14, 28),
                PartPose.offset(-14, -57, -14));
        root.addOrReplaceChild("lower_leaves", CubeListBuilder.create().texOffs(0, 180).addBox(0, 0, 0, 60, 16, 60),
                PartPose.offset(-30, -42, -30));
        root.addOrReplaceChild("upper_leaves", CubeListBuilder.create().texOffs(56, 130).addBox(0, 0, 0, 32, 16, 32),
                PartPose.offset(-16, -58, -16));
        for (int i = 0; i < 4; i++) {
            root.addOrReplaceChild("left_root" + i, CubeListBuilder.create().texOffs(18, 0).addBox(-3, -1, -1, 16, 2, 2),
                    PartPose.offset(4, 20, 2 - i));
            root.addOrReplaceChild("right_root" + i, CubeListBuilder.create().texOffs(18, 0).addBox(-13, -1, -1, 16, 2, 2),
                    PartPose.offset(-4, 20, 2 - i));
        }
        return LayerDefinition.create(mesh, 256, 256);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void prepareMobModel(Ent ent, float swing, float walk, float partialTick) {
        this.partialTick = partialTick;
    }

    @Override
    public void setupAnim(Ent ent, float swing, float walk, float age, float yaw, float pitch) {
        face.visible = ent.isScreaming();
        if (ent.attackTicks() > 0) {
            float attack = triangle(ent.attackTicks() - partialTick, 10);
            rightArm.xRot = 3 - 1.3F * attack;
            leftArm.xRot = 2.5F - 1.2F * attack;
        } else {
            rightArm.xRot = leftArm.xRot = 0;
        }
        rightArm.zRot = (-0.2F + 0.1F * triangle(swing, 13)) * walk - 0.1F;
        leftArm.zRot = -rightArm.zRot;

        for (int i = 0; i < leftRoots.length; i++) {
            float phase = ROOT_PHASE[i];
            float turn = -Mth.cos(swing * 1.3324F + phase) * 0.4F * walk;
            float lift = Math.abs(Mth.sin(swing * 0.6662F + phase)) * 0.4F * walk;
            rightRoots[i].yRot = ROOT_ANGLE[i] * Mth.PI / 8 + turn;
            leftRoots[i].yRot = -rightRoots[i].yRot;
            float spread = Mth.PI / 4 * (Math.abs(ROOT_ANGLE[i]) == 2 ? 1 : 0.74F);
            rightRoots[i].zRot = -spread + lift;
            leftRoots[i].zRot = -rightRoots[i].zRot;
        }
    }

    private static float triangle(float time, float period) {
        return (Math.abs(time % period - period / 2) - period / 4) / (period / 4);
    }
}
