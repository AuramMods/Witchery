package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.GrenadeProjectile;
import art.arcane.witchery.item.GrenadeItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public final class GrenadeRenderer extends EntityRenderer<GrenadeProjectile> {
    private static final ResourceLocation SUN = ResourceLocation.fromNamespaceAndPath(
            Witchery.MODID, "textures/item/sungrenade.png");
    private static final ResourceLocation DUPLICATE = ResourceLocation.fromNamespaceAndPath(
            Witchery.MODID, "textures/item/dupgrenade.png");

    public GrenadeRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(GrenadeProjectile grenade, float yaw, float tick, PoseStack pose,
                       MultiBufferSource buffers, int light) {
        pose.pushPose();
        pose.mulPose(entityRenderDispatcher.cameraOrientation());
        pose.mulPose(Axis.YP.rotationDegrees(180));
        pose.scale(0.3F, 0.3F, 0.3F);
        ProjectileBillboard.draw(buffers.getBuffer(RenderType.entityCutoutNoCull(ProjectileBillboard.QUARTZ_SPHERE)),
                pose.last(), 0xFFFFFF, light);
        ProjectileBillboard.draw(buffers.getBuffer(RenderType.entityCutoutNoCullZOffset(getTextureLocation(grenade))),
                pose.last(), 0xFFFFFF, light);
        pose.popPose();
        super.render(grenade, yaw, tick, pose, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(GrenadeProjectile grenade) {
        return GrenadeItem.isDuplicate(grenade.getItem()) ? DUPLICATE : SUN;
    }
}
