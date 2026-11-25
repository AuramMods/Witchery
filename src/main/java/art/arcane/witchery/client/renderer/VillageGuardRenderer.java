package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.VillageGuard;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

import java.util.List;

public final class VillageGuardRenderer extends VillageDefenderRenderer<VillageGuard> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "villageguard"), "main");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Witchery.MODID, "textures/entities/villageguard.png");

    public VillageGuardRenderer(EntityRendererProvider.Context context) {
        super(context, new Model(context.bakeLayer(LAYER)));
    }

    public static LayerDefinition layer() {
        var mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0);
        var head = mesh.getRoot().addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(28, 46).addBox(-4, -10, -4, 8, 10, 8), PartPose.ZERO);
        head.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(52, 46).addBox(-1, -1, -6, 2, 4, 2),
                PartPose.offset(0, -2, 0));
        mesh.getRoot().addOrReplaceChild("robe", CubeListBuilder.create().texOffs(0, 38)
                .addBox(-4, 0, -3, 8, 18, 6, new CubeDeformation(0.5F)), PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public ResourceLocation getTextureLocation(VillageGuard guard) {
        return TEXTURE;
    }

    private static final class Model extends LegacyBipedModel<VillageGuard> {
        private final List<ModelPart> parts;

        private Model(ModelPart root) {
            super(root);
            parts = ImmutableList.<ModelPart>builder().addAll(super.bodyParts()).add(root.getChild("robe")).build();
        }

        @Override
        protected Iterable<ModelPart> bodyParts() { return parts; }

        @Override
        public void setupAnim(VillageGuard guard, float swing, float amount, float age, float yaw, float pitch) {
            super.setupAnim(guard, swing, amount, age, yaw, pitch);
            float stride = swing * 0.6662F;
            rightLeg.xRot = Mth.cos(stride) * 1.4F * amount * 0.5F;
            leftLeg.xRot = Mth.cos(stride + Mth.PI) * 1.4F * amount * 0.5F;
            rightLeg.yRot = leftLeg.yRot = 0;
            hat.y += 0.3F;
        }
    }
}
