package art.arcane.witchery.client.model;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.Treefyd;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class TreefydModel extends HierarchicalModel<Treefyd> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "treefyd"), "main");
    private final ModelPart root;
    private final ModelPart head;
    private final ModelPart leftFront;
    private final ModelPart rightFront;
    private final ModelPart leftBack;
    private final ModelPart rightBack;

    public TreefydModel(ModelPart root) {
        this.root = root;
        head = root.getChild("head");
        leftFront = root.getChild("left_front");
        rightFront = root.getChild("right_front");
        leftBack = root.getChild("left_back");
        rightBack = root.getChild("right_back");
    }

    public static LayerDefinition layer() {
        var mesh = new MeshDefinition();
        var root = mesh.getRoot();
        root.addOrReplaceChild("head", CubeListBuilder.create().mirror()
                .texOffs(0, 24).addBox(-2, -4, -2, 4, 4, 4)
                .texOffs(0, 0).addBox(-5, -7, 0, 10, 10, 0)
                .texOffs(25, 18).addBox(0, -3, -6, 0, 10, 4), PartPose.offset(0, 3, 0));
        root.addOrReplaceChild("stem", CubeListBuilder.create().texOffs(16, 14).addBox(-1, 0, -1, 2, 16, 2),
                PartPose.offset(0, 3, 0));
        root.addOrReplaceChild("leaves_side", CubeListBuilder.create().texOffs(40, 6).addBox(0, 0, -6, 0, 14, 12),
                PartPose.offsetAndRotation(0, 6, 0, 0, Mth.PI / 4, 0));
        root.addOrReplaceChild("leaves_front", CubeListBuilder.create().texOffs(40, 0).addBox(-6, 0, 0, 12, 14, 0),
                PartPose.offsetAndRotation(0, 6, 0, 0, Mth.PI / 4, 0));
        root.addOrReplaceChild("base", CubeListBuilder.create().texOffs(15, 6).addBox(0, 0, 0, 6, 1, 6),
                PartPose.offset(-3, 19, -3));
        leg(root, "left_front", -2, -4);
        leg(root, "right_front", 2, -4);
        leg(root, "left_back", -2, 4);
        leg(root, "right_back", 2, 4);
        return LayerDefinition.create(mesh, 64, 32);
    }

    private static void leg(PartDefinition root, String name, int x, int z) {
        root.addOrReplaceChild(name, CubeListBuilder.create().texOffs(0, 16).addBox(-2, 0, -2, 4, 4, 4),
                PartPose.offset(x, 20, z));
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(Treefyd treefyd, float swing, float amount, float age, float yaw, float pitch) {
        head.yRot = yaw * Mth.DEG_TO_RAD;
        head.xRot = pitch * Mth.DEG_TO_RAD;
        float phase = swing * 0.6662F;
        leftBack.xRot = rightFront.xRot = Mth.cos(phase) * 1.4F * amount;
        leftFront.xRot = rightBack.xRot = Mth.cos(phase + Mth.PI) * 1.4F * amount;
    }
}
