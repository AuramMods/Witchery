package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.SymbolProjectile;
import art.arcane.witchery.infusion.SymbolSpell;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public final class SymbolProjectileRenderer extends EntityRenderer<SymbolProjectile> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/entities/spelleffect.png");
    private static final float SPRITE_EDGE = 16F / 256;

    public SymbolProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(SymbolProjectile projectile, float yaw, float tick, PoseStack pose, MultiBufferSource buffers, int light) {
        SymbolSpell spell = projectile.spell();
        if (spell == null) return;
        pose.pushPose();
        pose.mulPose(entityRenderDispatcher.cameraOrientation());
        pose.mulPose(Axis.YP.rotationDegrees(180));
        float size = spell.size() * 0.325F;
        pose.scale(size, size, size);
        VertexConsumer vertices = buffers.getBuffer(RenderType.entityTranslucent(TEXTURE));
        vertex(vertices, pose.last(), -0.5F, -0.25F, 0, SPRITE_EDGE, spell.color());
        vertex(vertices, pose.last(), 0.5F, -0.25F, SPRITE_EDGE, SPRITE_EDGE, spell.color());
        vertex(vertices, pose.last(), 0.5F, 0.75F, SPRITE_EDGE, 0, spell.color());
        vertex(vertices, pose.last(), -0.5F, 0.75F, 0, 0, spell.color());
        pose.popPose();
        super.render(projectile, yaw, tick, pose, buffers, light);
    }

    static void vertex(VertexConsumer vertices, PoseStack.Pose pose, float x, float y, float u, float v, int color) {
        vertices.vertex(pose.pose(), x, y, 0).color(color >> 16 & 255, color >> 8 & 255, color & 255, 140)
                .uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(LightTexture.FULL_BRIGHT)
                .normal(pose.normal(), 0, 1, 0).endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(SymbolProjectile projectile) {
        return TEXTURE;
    }
}
