package art.arcane.witchery.client.renderer;

import art.arcane.witchery.entity.SymbolProjectile;
import art.arcane.witchery.infusion.SymbolSpell;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public final class SymbolProjectileRenderer extends EntityRenderer<SymbolProjectile> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/item/snowball.png");
    private static final RenderType SPRITE = RenderType.entityTranslucent(TEXTURE);
    private static final int ALPHA = Math.round(0.55F * 255);

    public SymbolProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(SymbolProjectile projectile, float yaw, float tick, PoseStack pose, MultiBufferSource buffers, int light) {
        SymbolSpell spell = projectile.spell();
        boolean hasProjectileStyle = spell != null && spell.size() > 0;
        float size = (hasProjectileStyle ? spell.size() : 1) * 0.325F;
        int color = hasProjectileStyle ? spell.color() : 0xFF0000;
        pose.pushPose();
        pose.mulPose(entityRenderDispatcher.cameraOrientation());
        pose.mulPose(Axis.YP.rotationDegrees(180));
        pose.scale(size, size, size);
        int tint = encodeChannel(color >> 16) << 16 | encodeChannel(color >> 8) << 8 | encodeChannel(color);
        ProjectileBillboard.draw(buffers.getBuffer(SPRITE), pose.last(), tint, ALPHA, LightTexture.FULL_BRIGHT);
        pose.popPose();
        super.render(projectile, yaw, tick, pose, buffers, light);
    }

    private static int encodeChannel(int channel) {
        return Math.round((channel & 255) * (255F / 256F));
    }

    @Override
    public ResourceLocation getTextureLocation(SymbolProjectile projectile) {
        return TEXTURE;
    }
}
