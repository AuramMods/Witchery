package art.arcane.witchery.mixin;

import art.arcane.witchery.brewing.BrewDye;
import art.arcane.witchery.client.renderer.BrewTintBuffer;
import art.arcane.witchery.registry.WitcheryEffects;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntityRenderer.class)
public abstract class BrewColorRenderMixin {
    @Redirect(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/EntityModel;renderToBuffer(Lcom/mojang/blaze3d/vertex/PoseStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;IIFFFF)V"))
    private void witchery$tintBody(EntityModel<?> model, PoseStack pose, VertexConsumer vertices, int light,
                                  int overlay, float red, float green, float blue, float alpha,
                                  LivingEntity entity, float yaw, float partialTick, PoseStack renderPose,
                                  MultiBufferSource buffers, int renderLight) {
        int rgb = witchery$brewTint(entity);
        model.renderToBuffer(pose, rgb >= 0 ? new BrewTintBuffer(vertices, rgb) : vertices,
                light, overlay, red, green, blue, alpha);
    }

    @Redirect(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/entity/layers/RenderLayer;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/Entity;FFFFFF)V"))
    private void witchery$tintLayer(RenderLayer<LivingEntity, ?> layer, PoseStack pose, MultiBufferSource buffers,
                                   int light, Entity entity, float limbSwing, float limbAmount,
                                   float partialTick, float age, float headYaw, float headPitch) {
        LivingEntity living = (LivingEntity) entity;
        int rgb = witchery$brewTint(living);
        MultiBufferSource tinted = rgb >= 0 ? type -> new BrewTintBuffer(buffers.getBuffer(type), rgb) : buffers;
        layer.render(pose, tinted, light, living, limbSwing, limbAmount, partialTick, age, headYaw, headPitch);
    }

    @Unique
    private static int witchery$brewTint(LivingEntity entity) {
        var effect = entity.getEffect(WitcheryEffects.COLORFUL.get());
        return effect == null ? -1 : BrewDye.byLegacyId(effect.getAmplifier()).rgb();
    }
}
