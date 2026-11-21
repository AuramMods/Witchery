package art.arcane.witchery.client.renderer;

import art.arcane.witchery.entity.ThrownBrew;
import art.arcane.witchery.item.BrewBottleItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.resources.ResourceLocation;

public final class BrewProjectileRenderer extends ThrownItemRenderer<ThrownBrew> {
    private static final ResourceLocation SPELL =
            ResourceLocation.fromNamespaceAndPath("witchery", "textures/item/ingredient.quartzsphere.png");

    public BrewProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(ThrownBrew brew, float yaw, float tick, PoseStack pose, MultiBufferSource buffers, int light) {
        if (!brew.isNoGravity()) {
            super.render(brew, yaw, tick, pose, buffers, light);
            return;
        }

        pose.pushPose();
        pose.mulPose(entityRenderDispatcher.cameraOrientation());
        pose.mulPose(Axis.YP.rotationDegrees(180));
        pose.scale(0.5F, 0.5F, 0.5F);
        var vertices = buffers.getBuffer(RenderType.entityTranslucent(SPELL));
        int color = BrewBottleItem.color(brew.getItem());
        SymbolProjectileRenderer.vertex(vertices, pose.last(), -0.5F, -0.25F, 0, 1, color);
        SymbolProjectileRenderer.vertex(vertices, pose.last(), 0.5F, -0.25F, 1, 1, color);
        SymbolProjectileRenderer.vertex(vertices, pose.last(), 0.5F, 0.75F, 1, 0, color);
        SymbolProjectileRenderer.vertex(vertices, pose.last(), -0.5F, 0.75F, 0, 0, color);
        pose.popPose();
    }
}
