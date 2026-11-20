package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.Vampire;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class VampireRenderer extends HumanoidMobRenderer<Vampire, HumanoidModel<Vampire>> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "vampire"), "main");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Witchery.MODID, "textures/entities/vampire.png");

    public VampireRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(LAYER)), 0.5F);
    }

    public static LayerDefinition layer() {
        var mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0);
        var head = mesh.getRoot().addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(28, 46).addBox(-4, -10, -4, 8, 10, 8), PartPose.ZERO);
        head.addOrReplaceChild("nose", CubeListBuilder.create().texOffs(52, 46).addBox(-1, -1, -6, 2, 4, 2),
                PartPose.offset(0, -2, 0));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public ResourceLocation getTextureLocation(Vampire vampire) {
        return TEXTURE;
    }
}
