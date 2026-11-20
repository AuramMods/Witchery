package art.arcane.witchery.client.model;

import art.arcane.witchery.entity.WitchPet;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.geom.ModelPart;

import java.util.List;

abstract class WitchPetModel<T extends WitchPet> extends AgeableListModel<T> {
    private static final float[][] COLORS = {
            {1, 1, 1}, {0.85F, 0.5F, 0.2F}, {0.7F, 0.3F, 0.85F}, {0.4F, 0.6F, 0.85F},
            {0.9F, 0.9F, 0.2F}, {0.5F, 0.8F, 0.1F}, {0.95F, 0.5F, 0.65F}, {0.3F, 0.3F, 0.3F},
            {0.6F, 0.6F, 0.6F}, {0.3F, 0.5F, 0.6F}, {0.5F, 0.25F, 0.7F}, {0.2F, 0.3F, 0.7F},
            {0.4F, 0.3F, 0.2F}, {0.4F, 0.5F, 0.2F}, {0.6F, 0.2F, 0.2F}, {0.1F, 0.1F, 0.1F}
    };

    protected final ModelPart head;
    private final List<ModelPart> headParts;
    private final List<ModelPart> bodyParts;
    private final float tintScale;
    private final float redBase;
    private final float greenBase;
    private final float blueBase;
    private int color;

    protected WitchPetModel(ModelPart root, float babyHeadOffset, float tintScale,
                            float redBase, float greenBase, float blueBase, List<ModelPart> bodyParts) {
        super(true, babyHeadOffset, 0, 2, 2, 24);
        head = root.getChild("head");
        headParts = List.of(head);
        this.bodyParts = bodyParts;
        this.tintScale = tintScale;
        this.redBase = redBase;
        this.greenBase = greenBase;
        this.blueBase = blueBase;
    }

    @Override
    protected Iterable<ModelPart> headParts() {
        return headParts;
    }

    @Override
    protected Iterable<ModelPart> bodyParts() {
        return bodyParts;
    }

    @Override
    public void prepareMobModel(T creature, float swing, float walk, float partialTick) {
        color = creature.color();
    }

    @Override
    public void renderToBuffer(PoseStack pose, VertexConsumer vertices, int light, int overlay,
                               float red, float green, float blue, float alpha) {
        if (color != 0) {
            float[] dye = COLORS[color];
            red *= Math.min(1, dye[0] * tintScale + redBase);
            green *= Math.min(1, dye[1] * tintScale + greenBase);
            blue *= Math.min(1, dye[2] * tintScale + blueBase);
        }
        super.renderToBuffer(pose, vertices, light, overlay, red, green, blue, alpha);
    }
}
