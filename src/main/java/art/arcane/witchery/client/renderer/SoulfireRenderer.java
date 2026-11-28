package art.arcane.witchery.client.renderer;

import art.arcane.witchery.entity.Soulfire;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public final class SoulfireRenderer extends EntityRenderer<Soulfire> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/item/fire_charge.png");
    private static final RenderType SPRITE = RenderType.entityCutoutNoCull(TEXTURE);

    public SoulfireRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(Soulfire fireball, float yaw, float tick, PoseStack pose, MultiBufferSource buffers, int light) {
        pose.pushPose();
        pose.mulPose(entityRenderDispatcher.cameraOrientation());
        pose.mulPose(Axis.YP.rotationDegrees(180));
        pose.scale(0.5F, 0.5F, 0.5F);
        ProjectileBillboard.draw(buffers.getBuffer(SPRITE), pose.last(), 0xFFFFFF, LightTexture.FULL_BRIGHT);
        pose.popPose();
        super.render(fireball, yaw, tick, pose, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(Soulfire fireball) {
        return TEXTURE;
    }
}
