package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.entity.ThrownBrew;
import art.arcane.witchery.item.BrewBottleItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.resources.ResourceLocation;

public final class BrewProjectileRenderer extends ThrownItemRenderer<ThrownBrew> {
    private static final RenderType SPELL = RenderType.entityCutoutNoCull(ProjectileBillboard.QUARTZ_SPHERE);
    private static final RenderType LIQUID = RenderType.entityCutoutNoCull(ResourceLocation.fromNamespaceAndPath(
            Witchery.MODID, "textures/item/brew_overlay.png"));
    private static final RenderType SPLASH_GLASS = RenderType.entityCutoutNoCullZOffset(ResourceLocation.fromNamespaceAndPath(
            Witchery.MODID, "textures/item/brew_splash.png"));
    private static final RenderType DRINKABLE_GLASS = RenderType.entityCutoutNoCullZOffset(ResourceLocation.fromNamespaceAndPath(
            Witchery.MODID, "textures/item/brew_drinkable.png"));

    public BrewProjectileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(ThrownBrew brew, float yaw, float tick, PoseStack pose, MultiBufferSource buffers, int light) {
        var bottle = brew.getItem();
        if (!(bottle.getItem() instanceof BrewBottleItem)) {
            super.render(brew, yaw, tick, pose, buffers, light);
            return;
        }

        pose.pushPose();
        pose.mulPose(entityRenderDispatcher.cameraOrientation());
        pose.mulPose(Axis.YP.rotationDegrees(180));
        pose.scale(0.5F, 0.5F, 0.5F);
        int color = BrewBottleItem.color(bottle);
        int packedLight = brew.displayFireAnimation() ? LightTexture.FULL_BRIGHT : light;
        if (brew.isNoGravity()) {
            ProjectileBillboard.draw(buffers.getBuffer(SPELL), pose.last(), color, packedLight);
        } else {
            ProjectileBillboard.draw(buffers.getBuffer(LIQUID), pose.last(), color, packedLight);
            var glass = BrewBottleItem.isSplash(bottle) ? SPLASH_GLASS : DRINKABLE_GLASS;
            ProjectileBillboard.draw(buffers.getBuffer(glass), pose.last(), 0xFFFFFF, packedLight);
        }
        pose.popPose();
    }
}
