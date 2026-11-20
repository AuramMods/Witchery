package art.arcane.witchery.client.model;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.DarkMark;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class DarkMarkModel extends HierarchicalModel<DarkMark> {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "darkmark"), "main");
    private final ModelPart root;
    private final ModelPart skull;

    public DarkMarkModel(ModelPart root) {
        this.root = root;
        skull = root.getChild("skull");
    }

    public static LayerDefinition layer() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("skull", CubeListBuilder.create().mirror()
                .texOffs(0, 0).addBox(-8, -12, -8, 16, 12, 16)
                .texOffs(0, 29).addBox(-5, 0, -8, 10, 4, 12), PartPose.offset(0, 20, 0));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(DarkMark mark, float limbSwing, float swingAmount, float age, float yaw, float pitch) {
        skull.yRot = yaw * Mth.DEG_TO_RAD;
        skull.xRot = pitch * Mth.DEG_TO_RAD;
    }
}
