package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.client.ClientPlayerAppearance;
import art.arcane.witchery.entity.WitcheryFollower;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.MultiBufferSource;
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
    private final HumanoidModel<WitcheryFollower> normal;
    private final HumanoidModel<WitcheryFollower> slim;

    public FollowerRenderer(EntityRendererProvider.Context context) {
        super(context, new LegacyBipedModel<>(context.bakeLayer(LAYER)), 0.5F);
        normal = new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false);
        slim = new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM), true);
        addLayer(new HumanoidArmorLayer<>(this, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)), context.getModelManager()));
    }

    public static LayerDefinition layer() {
        return LayerDefinition.create(HumanoidModel.createMesh(CubeDeformation.NONE, 0), 64, 32);
    }

    @Override
    public ResourceLocation getTextureLocation(WitcheryFollower follower) {
        int type = follower.followerType();
        if (type == 0) return ELLE;
        if (type == 5) return ClientPlayerAppearance.of(follower.playerSkin()).texture();
        return FAIREST[type >= 1 && type <= FAIREST.length ? type - 1 : 0];
    }

    @Override
    public void render(WitcheryFollower follower, float yaw, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int light) {
        HumanoidModel<WitcheryFollower> previous = model;
        if (follower.followerType() == 5) {
            model = "slim".equals(ClientPlayerAppearance.of(follower.playerSkin()).model()) ? slim : normal;
            LegacyBipedModel.prepareHeldItemPose(model, follower);
        }
        try {
            super.render(follower, yaw, partialTick, pose, buffers, light);
        } finally {
            model = previous;
        }
    }
}
