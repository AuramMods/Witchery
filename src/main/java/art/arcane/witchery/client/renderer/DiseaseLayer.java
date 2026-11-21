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

public final class DiseaseLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/entities/disease_overlay.png");

    public DiseaseLayer(RenderLayerParent<T, M> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffers, int light, T entity, float limbSwing,
                       float limbAmount, float partialTick, float age, float headYaw, float headPitch) {
        if (!entity.hasEffect(WitcheryEffects.DISEASED.get())) return;
        var viewer = Minecraft.getInstance().player;
        if (entity.isInvisible() && (viewer == null || entity.isInvisibleTo(viewer))) return;
        pose.pushPose();
        pose.scale(1.01F, 1.01F, 1.01F);
        getParentModel().renderToBuffer(pose, buffers.getBuffer(RenderType.entityTranslucent(TEXTURE)), light,
                OverlayTexture.NO_OVERLAY, 1, 1, 1, entity.isInvisible() ? 0.15F : 1);
        pose.popPose();
    }
}
