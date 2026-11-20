package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.client.model.DemonModel;
import art.arcane.witchery.entity.Demon;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public final class DemonRenderer extends MobRenderer<Demon, DemonModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/entities/demon.png");

    public DemonRenderer(EntityRendererProvider.Context context) {
        super(context, new DemonModel(context.bakeLayer(DemonModel.LAYER)), 0.7F);
    }

    @Override
    protected void setupRotations(Demon demon, PoseStack pose, float age, float yaw, float partialTick) {
        super.setupRotations(demon, pose, age, yaw, partialTick);
        if (demon.walkAnimation.speed() >= 0.01F) {
            float step = demon.walkAnimation.position(partialTick) + 6;
            float sway = (Math.abs(step % 13 - 6.5F) - 3.25F) / 3.25F;
            pose.mulPose(Axis.ZP.rotationDegrees(6.5F * sway));
        }
    }

    @Override
    public ResourceLocation getTextureLocation(Demon entity) {
        return TEXTURE;
    }
}
