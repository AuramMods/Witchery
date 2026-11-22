package art.arcane.witchery.client.model;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.Mindrake;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.resources.ResourceLocation;

public final class MindrakeModel extends HierarchicalModel<Mindrake> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "mindrake"), "main");
    private final ModelPart root;

    public MindrakeModel(ModelPart root) {
        this.root = root;
    }

    public static LayerDefinition layer() {
        var mesh = new MeshDefinition();
        var root = mesh.getRoot();
        root.addOrReplaceChild("leaves", CubeListBuilder.create().mirror().texOffs(0, 0)
                .addBox(-3, 0, 0, 6, 6, 0), PartPose.offset(0, 13, 0));
        root.addOrReplaceChild("neck", CubeListBuilder.create().mirror().texOffs(0, 7)
                .addBox(-1.5F, 0, -1.5F, 3, 1, 3), PartPose.offset(0, 19, 0));
        root.addOrReplaceChild("bulb", CubeListBuilder.create().mirror().texOffs(0, 12)
                .addBox(-2, 0, -2, 4, 3, 4), PartPose.offset(0, 20, 0));
        for (int x : new int[]{-1, 1}) {
            root.addOrReplaceChild(x < 0 ? "right_leg" : "left_leg", CubeListBuilder.create().mirror().texOffs(0, 20)
                    .addBox(-0.5F, 0, -0.5F, 1, 1, 1), PartPose.offset(x, 23, 0));
        }
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(Mindrake entity, float limbSwing, float amount, float age, float yaw, float pitch) {
    }
}
