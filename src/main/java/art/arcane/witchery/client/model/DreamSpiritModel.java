package art.arcane.witchery.client.model;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.DreamSpirit;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.resources.ResourceLocation;

public final class DreamSpiritModel extends HierarchicalModel<DreamSpirit> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "spirit"), "main");
    private final ModelPart root;
    private int color;

    public DreamSpiritModel(ModelPart root) {
        this.root = root;
    }

    public static LayerDefinition layer() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("body", CubeListBuilder.create().mirror()
                .texOffs(2, 5).addBox(-2.5F, -2, -2.5F, 5, 1, 5)
                .texOffs(2, 21).addBox(-2.5F, 1, -2.5F, 5, 1, 5)
                .texOffs(0, 12).addBox(-3, -1, -3, 6, 2, 6)
                .texOffs(6, 0).addBox(-1.5F, -3, -1.5F, 3, 1, 3)
                .texOffs(6, 28).addBox(-1.5F, 2, -1.5F, 3, 1, 3), PartPose.offset(0, 21, 0));
        return LayerDefinition.create(mesh, 32, 32);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(DreamSpirit spirit, float limbSwing, float distance, float age, float yaw, float pitch) {
        color = spirit.color() == 0 ? 0xFFFFFF : spirit.color();
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer vertices, int light, int overlay,
                               float red, float green, float blue, float alpha) {
        root.render(pose, vertices, light, overlay, red * (color >> 16 & 255) / 255,
                green * (color >> 8 & 255) / 255, blue * (color & 255) / 255, alpha * 0.6F);
    }
}
