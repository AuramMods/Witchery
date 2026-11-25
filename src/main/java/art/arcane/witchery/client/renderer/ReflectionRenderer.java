package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.client.ClientPlayerAppearance;
import art.arcane.witchery.entity.Reflection;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;

public final class ReflectionRenderer extends HumanoidMobRenderer<Reflection, HumanoidModel<Reflection>> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(
            Witchery.MODID, "textures/entities/reflection.png");
    private final HumanoidModel<Reflection> classic;
    private final HumanoidModel<Reflection> slim;
    private final WolfmanRenderer<Reflection> wolfman;

    public ReflectionRenderer(EntityRendererProvider.Context context) {
        super(context, new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER), false), 0.5F);
        classic = new HumanoidModel<>(context.bakeLayer(FollowerRenderer.LAYER));
        slim = new PlayerModel<>(context.bakeLayer(ModelLayers.PLAYER_SLIM), true);
        addLayer(new HumanoidArmorLayer<>(this, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)), context.getModelManager()));
        wolfman = new WolfmanRenderer<>(context, Reflection::attackTicks);
    }

    @Override
    public ResourceLocation getTextureLocation(Reflection reflection) {
        return reflection.ownerSkin().isEmpty() ? TEXTURE : ClientPlayerAppearance.of(reflection.ownerSkin()).texture();
    }

    @Override
    public void render(Reflection reflection, float yaw, float partialTick, PoseStack pose, MultiBufferSource buffers, int light) {
        if (reflection.hasWolfmanModel()) {
            wolfman.render(reflection, yaw, partialTick, pose, buffers, light);
            return;
        }
        HumanoidModel<Reflection> previous = model;
        if (reflection.ownerSkin().isEmpty()) model = classic;
        else if ("slim".equals(ClientPlayerAppearance.of(reflection.ownerSkin()).model())) model = slim;
        try {
            super.render(reflection, yaw, partialTick, pose, buffers, light);
        } finally {
            model = previous;
        }
    }
}
