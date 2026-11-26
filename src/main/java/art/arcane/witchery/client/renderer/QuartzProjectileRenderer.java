package art.arcane.witchery.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.function.ToIntFunction;

public final class QuartzProjectileRenderer<T extends Entity> extends EntityRenderer<T> {
    private static final RenderType SPRITE = RenderType.entityCutoutNoCull(ProjectileBillboard.QUARTZ_SPHERE);
    private final ToIntFunction<T> tint;

    public QuartzProjectileRenderer(EntityRendererProvider.Context context, ToIntFunction<T> tint) {
        super(context);
        this.tint = tint;
    }

    @Override
    public void render(T projectile, float yaw, float tick, PoseStack pose, MultiBufferSource buffers, int light) {
        pose.pushPose();
        pose.mulPose(entityRenderDispatcher.cameraOrientation());
        pose.mulPose(Axis.YP.rotationDegrees(180));
        pose.scale(0.5F, 0.5F, 0.5F);
        ProjectileBillboard.draw(buffers.getBuffer(SPRITE), pose.last(), tint.applyAsInt(projectile),
                projectile.displayFireAnimation() ? LightTexture.FULL_BRIGHT : light);
        pose.popPose();
        super.render(projectile, yaw, tick, pose, buffers, light);
    }

    @Override
    public ResourceLocation getTextureLocation(T projectile) {
        return ProjectileBillboard.QUARTZ_SPHERE;
    }
}
