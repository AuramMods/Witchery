package art.arcane.witchery.client.renderer;

import art.arcane.witchery.Witchery;
import art.arcane.witchery.client.model.MandrakeModel;
import art.arcane.witchery.entity.Mandrake;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;

public final class MandrakeRenderer extends MobRenderer<Mandrake, MandrakeModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Witchery.MODID, "textures/entities/mandrake.png");

    public MandrakeRenderer(EntityRendererProvider.Context context) {
        super(context, new MandrakeModel(context.bakeLayer(MandrakeModel.LAYER)), 0.5F);
    }

    @Override
    protected void setupRotations(Mandrake entity, PoseStack pose, float age, float yaw, float partialTick) {
        super.setupRotations(entity, pose, age, yaw, partialTick);
        swayBody(entity, pose, partialTick);
    }

    static void swayBody(LivingEntity entity, PoseStack pose, float partialTick) {
        if (entity.walkAnimation.speed() >= 0.01F) {
            float cycle = entity.walkAnimation.position(partialTick) + 6;
            pose.mulPose(Axis.ZP.rotationDegrees(6.5F * Mth.triangleWave(cycle, 13)));
        }
    }

    @Override
    public ResourceLocation getTextureLocation(Mandrake entity) {
        return TEXTURE;
    }
}
