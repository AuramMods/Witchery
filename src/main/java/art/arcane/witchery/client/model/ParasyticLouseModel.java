package art.arcane.witchery.client.model;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.ParasyticLouse;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public final class ParasyticLouseModel extends HierarchicalModel<ParasyticLouse> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "louse"), "main");

    private static final int[][] SEGMENT_SIZES = {
            {3, 2, 2}, {4, 3, 2}, {4, 3, 2}, {3, 3, 3}, {2, 2, 3}, {2, 1, 2}, {1, 1, 2}
    };
    private static final int[][] SEGMENT_UV = {{0, 0}, {0, 4}, {0, 9}, {0, 16}, {0, 22}, {11, 0}, {13, 4}};

    private final ModelPart root;
    private final ModelPart[] segments = new ModelPart[SEGMENT_SIZES.length];
    private final ModelPart frontPlate;
    private final ModelPart middlePlate;
    private final ModelPart rearPlate;

    public ParasyticLouseModel(ModelPart root) {
        this.root = root;
        for (int i = 0; i < segments.length; i++) segments[i] = root.getChild("segment_" + i);
        frontPlate = root.getChild("front_plate");
        middlePlate = root.getChild("middle_plate");
        rearPlate = root.getChild("rear_plate");
    }

    public static LayerDefinition layer() {
        var mesh = new MeshDefinition();
        var root = mesh.getRoot();
        float z = -3.5F;
        float[] offsets = new float[SEGMENT_SIZES.length];
        for (int i = 0; i < SEGMENT_SIZES.length; i++) {
            int[] size = SEGMENT_SIZES[i];
            int[] uv = SEGMENT_UV[i];
            root.addOrReplaceChild("segment_" + i, CubeListBuilder.create().texOffs(uv[0], uv[1])
                    .addBox(-size[0] / 2F, 0, -size[2] / 2F, size[0], size[1], size[2]),
                    PartPose.offset(0, 24 - size[1], z));
            offsets[i] = z;
            if (i + 1 < SEGMENT_SIZES.length) z += (size[2] + SEGMENT_SIZES[i + 1][2]) / 2F;
        }
        root.addOrReplaceChild("middle_plate", CubeListBuilder.create().texOffs(20, 0).addBox(-5, 0, -1, 10, 8, 2),
                PartPose.offset(0, 16, offsets[2]));
        root.addOrReplaceChild("rear_plate", CubeListBuilder.create().texOffs(20, 11).addBox(-3, 0, -1.5F, 6, 4, 3),
                PartPose.offset(0, 20, offsets[4]));
        root.addOrReplaceChild("front_plate", CubeListBuilder.create().texOffs(20, 18).addBox(-3, 0, -1.5F, 6, 5, 2),
                PartPose.offset(0, 19, offsets[1]));
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public ModelPart root() {
        return root;
    }

    @Override
    public void setupAnim(ParasyticLouse louse, float swing, float walk, float age, float yaw, float pitch) {
        for (int i = 0; i < segments.length; i++) {
            float phase = age * 0.9F + i * 0.15F * Mth.PI;
            int fromMiddle = Math.abs(i - 2);
            segments[i].yRot = Mth.cos(phase) * Mth.PI * 0.05F * (1 + fromMiddle);
            segments[i].x = Mth.sin(phase) * Mth.PI * 0.2F * fromMiddle;
        }
        middlePlate.yRot = segments[2].yRot;
        rearPlate.yRot = segments[4].yRot;
        rearPlate.x = segments[4].x;
        frontPlate.yRot = segments[1].yRot;
        frontPlate.x = segments[1].x;
    }
}
