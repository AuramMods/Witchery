package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.WitcheryFollower;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class FollowerRenderer extends HumanoidMobRenderer<WitcheryFollower, HumanoidModel<WitcheryFollower>> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "follower"), "main");
    private static final ResourceLocation ELLE = ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/entities/lilithfol1.png");
    private static final ResourceLocation[] FAIREST = {
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/entities/fairest1.png"),
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/entities/fairest2.png"),
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/entities/fairest3.png"),
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/entities/fairest4.png")
    };

    public FollowerRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(LAYER)), 0.4F);
    }

    public static LayerDefinition layer() {
        return LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE, 0), 64, 32);
    }

    @Override
    public ResourceLocation getTextureLocation(WitcheryFollower follower) {
        int type = follower.followerType();
        if (type == 0) return ELLE;
        return FAIREST[type >= 1 && type <= FAIREST.length ? type - 1 : 0];
    }
}
