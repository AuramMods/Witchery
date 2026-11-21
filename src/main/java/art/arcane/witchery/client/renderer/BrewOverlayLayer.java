package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.registry.WitcheryEffects;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public final class BrewOverlayLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private static final ResourceLocation DISEASE =
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/entities/disease_overlay.png");
    private static final ResourceLocation VINE =
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/entities/vine_overlay.png");
    private static final ResourceLocation CACTUS =
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/entities/cactus_overlay.png");

    public BrewOverlayLayer(RenderLayerParent<T, M> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffers, int light, T entity, float limbSwing,
                       float limbAmount, float partialTick, float age, float headYaw, float headPitch) {
        boolean diseased = entity.hasEffect(WitcheryEffects.DISEASED.get());
        boolean wrapped = entity.hasEffect(WitcheryEffects.WRAPPED_IN_VINE.get());
        boolean spiked = entity.hasEffect(WitcheryEffects.SPIKED.get());
        if (!diseased && !wrapped && !spiked) return;
        var viewer = Minecraft.getInstance().player;
        if (entity.isInvisible() && (viewer == null || entity.isInvisibleTo(viewer))) return;
        float alpha = entity.isInvisible() ? 0.15F : 1;
        if (diseased) draw(pose, buffers, light, DISEASE, 1.01F, alpha);
        if (wrapped) draw(pose, buffers, light, VINE, 1.02F, alpha);
        if (spiked) draw(pose, buffers, light, CACTUS, 1.03F, alpha);
    }

    private void draw(PoseStack pose, MultiBufferSource buffers, int light, ResourceLocation texture, float scale, float alpha) {
        pose.pushPose();
        pose.scale(scale, scale, scale);
        getParentModel().renderToBuffer(pose, buffers.getBuffer(RenderType.entityTranslucent(texture)), light,
                OverlayTexture.NO_OVERLAY, 1, 1, 1, alpha);
        pose.popPose();
    }
}
